package nl.strohalm.cyclos.webservices.rest.payments;

import java.util.HashMap;
import java.util.Map;

import nl.strohalm.cyclos.annotations.Inject;
import nl.strohalm.cyclos.controls.ActionContext;
import nl.strohalm.cyclos.entities.accounts.transactions.Payment;
import nl.strohalm.cyclos.entities.accounts.transactions.ScheduledPayment;
import nl.strohalm.cyclos.entities.accounts.transactions.Transfer;
import nl.strohalm.cyclos.entities.accounts.transactions.TransferType;
import nl.strohalm.cyclos.entities.exceptions.UnexpectedEntityException;
import nl.strohalm.cyclos.services.access.AccessService;
import nl.strohalm.cyclos.services.access.exceptions.BlockedCredentialsException;
import nl.strohalm.cyclos.services.access.exceptions.InvalidCredentialsException;
import nl.strohalm.cyclos.services.customization.PaymentCustomFieldService;
import nl.strohalm.cyclos.services.elements.ElementService;
import nl.strohalm.cyclos.services.transactions.DoPaymentDTO;
import nl.strohalm.cyclos.services.transactions.PaymentService;
import nl.strohalm.cyclos.services.transactions.exceptions.AuthorizedPaymentInPastException;
import nl.strohalm.cyclos.services.transactions.exceptions.CreditsException;
import nl.strohalm.cyclos.services.transfertypes.TransactionFeeService;
import nl.strohalm.cyclos.services.transfertypes.TransferTypeService;
import nl.strohalm.cyclos.utils.CustomFieldHelper;
import nl.strohalm.cyclos.utils.validation.ValidationException;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;

import org.apache.struts.action.ActionForward;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class ConfirmPaymentController extends BaseRestController {

	private PaymentService paymentService;
	private TransferTypeService transferTypeService;
	private TransactionFeeService transactionFeeService;
	private PaymentCustomFieldService paymentCustomFieldService;
	private ElementService elementService;
	private CustomFieldHelper customFieldHelper;
	private static AccessService accessService;

	@Inject
	public void setCustomFieldHelper(final CustomFieldHelper customFieldHelper) {
		this.customFieldHelper = customFieldHelper;
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

	public static class ConfirmPaymentRequestDto {
		private String transactionPassword;
		private String selectMember;
		private String from;
		private DoPaymentDTO payment;
		

		public DoPaymentDTO getPayment() {
			return payment;
		}

		public void setPayment(DoPaymentDTO payment) {
			this.payment = payment;
		}

		public String getFrom() {
			return from;
		}

		public String getSelectMember() {
			return selectMember;
		}

		public String getTransactionPassword() {
			return transactionPassword;
		}

		public void setFrom(final String from) {
			this.from = from;
		}

		public void setSelectMember(final String selectMember) {
			this.selectMember = selectMember;
		}

		public void setTransactionPassword(final String transactionPassword) {
			this.transactionPassword = transactionPassword;
		}

		public DoPaymentDTO getAttribute(String string) {

			return getPayment();
		}

		public void checkTransactionPassword(String transactionPassword2) {
			try {
				accessService.checkTransactionPassword(transactionPassword);
			} catch (final InvalidCredentialsException e) {
				throw new ValidationException(
						"transactionPassword.error.invalid");
			} catch (final BlockedCredentialsException e) {
				throw new ValidationException(
						"transactionPassword.error.blockedByTrials");
			} catch (final RuntimeException e) {
				throw e;
			}
		}
	}

	public static class ConfirmPaymentResponseDto {
		private Map<String, Object> params;
		String message;

		public ConfirmPaymentResponseDto(Map<String, Object> params,
				String message) {
			super();
			this.params = params;
			this.message = message;
		}

	}

	@RequestMapping(value = "/admin/managePasswords", method = RequestMethod.POST)
	@ResponseBody
	protected ConfirmPaymentResponseDto handleSubmit(
			@RequestBody ConfirmPaymentRequestDto form) throws Exception {
		// final ConfirmPaymentForm form = context.getForm();
		final DoPaymentDTO paymentDTO = validatePayment(form);
		// Validate the transaction password if needed
		if (shouldValidateTransactionPassword(form, paymentDTO)) {
			form.checkTransactionPassword(form.getTransactionPassword());
		}
		// Perform the actual payment
		Payment payment;
		String message = null;
		Map<String, Object> params = null;
		ConfirmPaymentResponseDto response = new ConfirmPaymentResponseDto(
				params, message);
		try {
			payment = paymentService.doPayment(paymentDTO);
			// context.getSession().removeAttribute("payment");
		} catch (final CreditsException e) {
			message = e.toString();
			return response;
		} catch (final UnexpectedEntityException e) {
			message = "payment.error.invalidTransferType";
			return response;
		} catch (final AuthorizedPaymentInPastException e) {
			message = "payment.error.authorizedInPast";
			return response;
		}
		// Redirect to the next action
		params = new HashMap<String, Object>();
		ActionForward forward;
		if (payment instanceof Transfer) {
			params.put("transferId", payment.getId());
			// forward = context.getSuccessForward();
		} else if (payment instanceof ScheduledPayment) {
			params.put("paymentId", payment.getId());
			// forward = context.findForward("scheduledPayment");
		} else {
			throw new IllegalStateException("Unknown payment type: " + payment);
		}
		params.put("selectMember", form.getSelectMember());
		params.put("from", form.getFrom());

		return response;
	}

	private boolean shouldValidateTransactionPassword(
			ConfirmPaymentRequestDto form, DoPaymentDTO paymentDTO) {
		// TODO Auto-generated method stub
		return false;
	}

	/*
	 * protected void prepareForm(final ActionContext context) throws Exception
	 * { final DoPaymentDTO payment = validatePayment(context);
	 * 
	 * // Clear the from when the same as logged owner if (payment.getFrom() !=
	 * null && context.getAccountOwner().equals(payment.getFrom())) {
	 * payment.setFrom(null); }
	 * 
	 * // Check for transaction password final HttpServletRequest request =
	 * context.getRequest(); final boolean requestTransactionPassword =
	 * shouldValidateTransactionPassword( context, payment); if
	 * (requestTransactionPassword) { context.validateTransactionPassword(); }
	 * 
	 * final boolean wouldRequireAuthorization = paymentService
	 * .wouldRequireAuthorization(payment);
	 * request.setAttribute("requestTransactionPassword",
	 * requestTransactionPassword);
	 * request.setAttribute("wouldRequireAuthorization",
	 * wouldRequireAuthorization);
	 * 
	 * if (wouldRequireAuthorization && payment.getDate() != null) { throw new
	 * ValidationException("payment.error.authorizedInPast"); }
	 * 
	 * // Fetch related data AccountOwner from = payment.getFrom(); AccountOwner
	 * to = payment.getTo(); final TransferType transferType =
	 * transferTypeService.load(payment .getTransferType().getId(),
	 * RelationshipHelper.nested( TransferType.Relationships.FROM,
	 * AccountType.Relationships.CURRENCY), TransferType.Relationships.TO);
	 * final BigDecimal amount = payment.getAmount(); if (from instanceof
	 * Member) { from = (Member) elementService.load(((Member) from).getId());
	 * request.setAttribute("fromMember", from); payment.setFrom(from); } if (to
	 * instanceof Member) { to = (Member) elementService.load(((Member)
	 * to).getId()); request.setAttribute("toMember", to); payment.setTo(to); }
	 * // request.setAttribute("relatedMember", from != null ? from.g : to);
	 * payment.setTransferType(transferType);
	 * request.setAttribute("unitsPattern", transferType.getFrom()
	 * .getCurrency().getPattern());
	 * 
	 * // Store the transaction fees final TransactionFeePreviewDTO preview =
	 * transactionFeeService.preview( from, to, transferType, amount);
	 * request.setAttribute("finalAmount", preview.getFinalAmount());
	 * request.setAttribute("fees", preview.getFees());
	 * 
	 * // Show the total amount when the original amount has changed (there //
	 * where fees which deducted from it) if
	 * (!preview.getAmount().equals(preview.getFinalAmount())) {
	 * request.setAttribute("totalAmount", preview.getAmount()); }
	 * 
	 * // Calculate the transaction fees for every scheduled payment final
	 * List<ScheduledPaymentDTO> payments = payment.getPayments(); final boolean
	 * isScheduled = CollectionUtils.isNotEmpty(payments); if (isScheduled) {
	 * for (final ScheduledPaymentDTO current : payments) { final
	 * TransactionFeePreviewDTO currentPreview = transactionFeeService
	 * .preview(from, to, transferType, current.getAmount());
	 * current.setFinalAmount(currentPreview.getFinalAmount()); } }
	 * request.setAttribute("isScheduled", isScheduled);
	 * 
	 * // Return the custom field values final
	 * Collection<PaymentCustomFieldValue> customValues = payment
	 * .getCustomValues(); if (customValues != null) { final
	 * List<PaymentCustomField> customFields = paymentCustomFieldService
	 * .list(transferType, false); final Collection<Entry> entries =
	 * customFieldHelper.buildEntries( customFields, customValues); // Load the
	 * value for enumerated values, since this collection was // built from
	 * direct databinding with ids only for (final Entry entry : entries) {
	 * final CustomField field = entry.getField(); final CustomFieldValue
	 * fieldValue = entry.getValue(); if (field.getType() ==
	 * CustomField.Type.ENUMERATED) { Long possibleValueId; final
	 * CustomFieldPossibleValue possibleValue = fieldValue .getPossibleValue();
	 * if (possibleValue != null) { possibleValueId = possibleValue.getId(); }
	 * else { possibleValueId = CoercionHelper.coerce(Long.class,
	 * fieldValue.getValue()); } if (possibleValueId != null) {
	 * fieldValue.setPossibleValue(paymentCustomFieldService
	 * .loadPossibleValue(possibleValueId)); } } else if (field.getType() ==
	 * CustomField.Type.MEMBER) { final Long memberId =
	 * CoercionHelper.coerce(Long.class, fieldValue.getValue()); if (memberId !=
	 * null) { final Element element = elementService.load(memberId); if
	 * (element instanceof Member) { fieldValue.setMemberValue((Member)
	 * element); } } } } request.setAttribute("customFields", entries); } }
	 */
	private boolean shouldValidateTransactionPassword(
			final ActionContext context, final DoPaymentDTO payment) {
		if (payment.getFrom() == null) {
			// When a logged member performing payments from himself
			final TransferType transferType = transferTypeService
					.load(payment.getTransferType().getId(),
							TransferType.Relationships.FROM);
			return context.isTransactionPasswordEnabled(transferType.getFrom());
		} else {
			return context.isTransactionPasswordEnabled();
		}
	}

	private DoPaymentDTO validatePayment(final ConfirmPaymentRequestDto context) {
		final DoPaymentDTO payment = (DoPaymentDTO) context
				.getAttribute("payment");
		if (payment == null) {
			throw new ValidationException();
		}
		return payment;
	}
}
