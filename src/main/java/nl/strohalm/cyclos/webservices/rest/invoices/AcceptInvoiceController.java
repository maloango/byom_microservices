package nl.strohalm.cyclos.webservices.rest.invoices;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import nl.strohalm.cyclos.annotations.Inject;
import nl.strohalm.cyclos.entities.access.User;
import nl.strohalm.cyclos.entities.accounts.AccountOwner;
import nl.strohalm.cyclos.entities.accounts.transactions.Invoice;
import nl.strohalm.cyclos.entities.accounts.transactions.Transfer;
import nl.strohalm.cyclos.entities.accounts.transactions.TransferType;
import nl.strohalm.cyclos.entities.exceptions.UnexpectedEntityException;
import nl.strohalm.cyclos.services.access.AccessService;
import nl.strohalm.cyclos.services.customization.PaymentCustomFieldService;
import nl.strohalm.cyclos.services.groups.GroupService;
import nl.strohalm.cyclos.services.transactions.InvoiceService;
import nl.strohalm.cyclos.services.transactions.PaymentService;
import nl.strohalm.cyclos.services.transactions.exceptions.CreditsException;
import nl.strohalm.cyclos.services.transactions.exceptions.MaxAmountPerDayExceededException;
import nl.strohalm.cyclos.services.transactions.exceptions.NotEnoughCreditsException;
import nl.strohalm.cyclos.services.transactions.exceptions.TransferMinimumPaymentException;
import nl.strohalm.cyclos.services.transactions.exceptions.UpperCreditLimitReachedException;
import nl.strohalm.cyclos.services.transfertypes.TransactionFeeService;
import nl.strohalm.cyclos.services.transfertypes.TransferTypeService;
import nl.strohalm.cyclos.utils.ActionHelper;
import nl.strohalm.cyclos.utils.CustomFieldHelper;
import nl.strohalm.cyclos.utils.DateHelper;
import nl.strohalm.cyclos.utils.binding.BeanBinder;
import nl.strohalm.cyclos.utils.binding.DataBinder;
import nl.strohalm.cyclos.utils.binding.PropertyBinder;
import nl.strohalm.cyclos.utils.conversion.IdConverter;
import nl.strohalm.cyclos.utils.conversion.ReferenceConverter;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;
import nl.strohalm.cyclos.webservices.rest.RestHelper;
import nl.strohalm.cyclos.webservices.utils.RestUserHelper;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class AcceptInvoiceController extends BaseRestController {
	private DataBinder<Invoice> dataBinder;
	private InvoiceService invoiceService;
	private TransactionFeeService transactionFeeService;
	private PaymentService paymentService;
	private PaymentCustomFieldService paymentCustomFieldService;
	private TransferTypeService transferTypeService;
	private  AccessService accessService;
	private GroupService groupService;
	private RestUserHelper restHelper;

	private CustomFieldHelper customFieldHelper;

	public DataBinder<Invoice> getDataBinder() {
		if (dataBinder == null) {
			final BeanBinder<Invoice> binder = BeanBinder
					.instance(Invoice.class);
			binder.registerBinder("id", PropertyBinder.instance(Long.class,
					"invoiceId", IdConverter.instance()));
			binder.registerBinder("transferType", PropertyBinder.instance(
					TransferType.class, "transferTypeId",
					ReferenceConverter.instance(TransferType.class)));
			dataBinder = binder;
		}
		return dataBinder;
	}

	public InvoiceService getInvoiceService() {
		return invoiceService;
	}

	public TransactionFeeService getTransactionFeeService() {
		return transactionFeeService;
	}

	@Inject
	public void setCustomFieldHelper(final CustomFieldHelper customFieldHelper) {
		this.customFieldHelper = customFieldHelper;
	}

	@Inject
	public void setInvoiceService(final InvoiceService invoiceService) {
		this.invoiceService = invoiceService;
	}

	@Inject
	public void setPaymentCustomFieldService(
			final PaymentCustomFieldService paymentCustomFieldService) {
		this.paymentCustomFieldService = paymentCustomFieldService;
	}

	@Inject
	public void setPaymentService(final PaymentService paymentService) {
		this.paymentService = paymentService;
	}

	@Inject
	public void setTransactionFeeService(
			final TransactionFeeService transactionFeeService) {
		this.transactionFeeService = transactionFeeService;
	}

	@Inject
	public void setTransferTypeService(
			final TransferTypeService transferTypeService) {
		this.transferTypeService = transferTypeService;
	}

	public static class AcceptInvoiceRequestDto {
		private ActionHelper actionHelper;

		public ActionHelper getActionHelper() {
			return actionHelper;
		}

		public void setActionHelper(ActionHelper actionHelper) {
			this.actionHelper = actionHelper;
		}

		public String resolveErrorKey(final CreditsException exception) {
			if (exception instanceof MaxAmountPerDayExceededException) {
				final MaxAmountPerDayExceededException e = (MaxAmountPerDayExceededException) exception;
				final Calendar date = e.getDate();
				if (date == null
						|| DateHelper.sameDay(date, Calendar.getInstance())) {
					return "payment.error.maxAmountOnDayExceeded";
				} else {
					return "payment.error.maxAmountOnDayExceeded.at";
				}
			} else if (exception instanceof NotEnoughCreditsException) {
				if (((NotEnoughCreditsException) exception).isOriginalAccount()) {
					return "payment.error.enoughCredits";
				} else {
					return "payment.error.enoughCreditsOtherAccount";
				}
			} else if (exception instanceof TransferMinimumPaymentException) {
				return "payment.error.transferMinimum";
			} else if (exception instanceof UpperCreditLimitReachedException) {
				return "payment.error.upperCreditLimit";
			} else {
				return "error.general";
			}
		}

		private long transferTypeId;
		private String transactionPassword;
		private long invoiceId;
		private long memberId;
		private long accountFeeLogId;

		public long getAccountFeeLogId() {
			return accountFeeLogId;
		}

		public long getInvoiceId() {
			return invoiceId;
		}

		public long getMemberId() {
			return memberId;
		}

		public void setAccountFeeLogId(final long accountFeeLogId) {
			this.accountFeeLogId = accountFeeLogId;
		}

		public void setInvoiceId(final long invoiceId) {
			this.invoiceId = invoiceId;
		}

		public void setMemberId(final long memberId) {
			this.memberId = memberId;
		}

		public String getTransactionPassword() {
			return transactionPassword;
		}

		public long getTransferTypeId() {
			return transferTypeId;
		}

		public void setTransactionPassword(final String transactionPassword) {
			this.transactionPassword = transactionPassword;
		}

		public void setTransferTypeId(final long transferTypeId) {
			this.transferTypeId = transferTypeId;
		}

	}

	public static class AcceptInvoiceResponseDto {
		private String message;
		private Map<String, Object> params;

		public AcceptInvoiceResponseDto(String message,
				Map<String, Object> params) {
			super();
			this.message = message;
			this.params = params;
		}

		public String getMessage() {
			return message;
		}

		public void setMessage(String message) {
			this.message = message;
		}
	}

	@RequestMapping(value = "/admin/managePasswords", method = RequestMethod.POST)
	@ResponseBody
	protected AcceptInvoiceResponseDto handleSubmit(@RequestBody AcceptInvoiceRequestDto form) throws Exception {
		// Initialize Rest Helper
		User user=null;
		
		restHelper=new RestUserHelper(user,groupService,accessService);
		
		String message = null;
		final Map<String, Object> params = new HashMap<String, Object>();
		AcceptInvoiceResponseDto response = new AcceptInvoiceResponseDto(
				message, params);
		try {
			// final AcceptInvoiceForm form = context.getForm();

			Invoice invoice = getDataBinder().readFromString(form);
			final boolean requestTransactionPassword = shouldValidateTransactionPassword(
					form, invoice, invoice.getTransferType());
			if (requestTransactionPassword) {
				restHelper.checkTransactionPassword(form.getTransactionPassword());
			}
			invoice = invoiceService.accept(invoice);
			final Transfer transfer = invoice.getTransfer();
			if (transfer != null && transfer.getProcessDate() == null) {
				message = "invoice.accepted.withAuthorization";
			} else {
				message = "invoice.accepted";
			}
			params.put("invoiceId", invoice.getId());
			params.put("memberId", form.getMemberId());
			params.put("accountFeeLogId", form.getAccountFeeLogId());
			return response;
		} catch (final CreditsException e) {
			message = "Credit Exception occured";
			return response;
			/*
			 * return context.sendError(actionHelper.resolveErrorKey(e),
			 * actionHelper.resolveParameters(e));
			 */
		} catch (final UnexpectedEntityException e) {
			message = "payment.error.invalidTransferType";
			return response;
		}
	}


	private boolean shouldValidateTransactionPassword(
			final AcceptInvoiceRequestDto form, Invoice invoice,
			TransferType transferType) {
		final AccountOwner loggedOwner = restHelper.getAccountOwner();
		invoice = invoiceService.load(invoice.getId(),
				Invoice.Relationships.TO_MEMBER);
		if (loggedOwner.equals(invoice.getToMember())) {
			// When a logged member accepting an invoice to himself
			transferType = transferTypeService.load(transferType.getId(),
					TransferType.Relationships.FROM);
			return restHelper.isTransactionPasswordEnabled(transferType.getFrom());
		} else {
			return restHelper.isTransactionPasswordEnabled();
		}
	}
	
	

}
