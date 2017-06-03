package nl.strohalm.cyclos.webservices.rest.accounts.transfertypes;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import nl.strohalm.cyclos.annotations.Inject;
import nl.strohalm.cyclos.controls.ActionContext;
import nl.strohalm.cyclos.controls.accounts.transfertypes.EditTransferTypeForm;
import nl.strohalm.cyclos.entities.access.Channel;
import nl.strohalm.cyclos.entities.accounts.AccountType;
import nl.strohalm.cyclos.entities.accounts.loans.Loan;
import nl.strohalm.cyclos.entities.accounts.loans.LoanParameters;
import nl.strohalm.cyclos.entities.accounts.transactions.TransferType;
import nl.strohalm.cyclos.entities.accounts.transactions.TransferType.Context;
import nl.strohalm.cyclos.entities.accounts.transactions.TransferType.TransactionHierarchyVisibility;
import nl.strohalm.cyclos.entities.members.Member;
import nl.strohalm.cyclos.entities.members.Reference;
import nl.strohalm.cyclos.entities.settings.LocalSettings;
import nl.strohalm.cyclos.entities.settings.events.LocalSettingsChangeListener;
import nl.strohalm.cyclos.entities.settings.events.LocalSettingsEvent;
import nl.strohalm.cyclos.services.access.ChannelService;
import nl.strohalm.cyclos.services.accounts.AccountTypeService;
import nl.strohalm.cyclos.services.customization.PaymentCustomFieldService;
import nl.strohalm.cyclos.services.settings.SettingsService;
import nl.strohalm.cyclos.services.transfertypes.TransactionFeeService;
import nl.strohalm.cyclos.services.transfertypes.TransferTypeService;
import nl.strohalm.cyclos.services.transfertypes.exceptions.HasPendingPaymentsException;
import nl.strohalm.cyclos.utils.ActionHelper;
import nl.strohalm.cyclos.utils.binding.BeanBinder;
import nl.strohalm.cyclos.utils.binding.DataBinder;
import nl.strohalm.cyclos.utils.binding.DataBinderHelper;
import nl.strohalm.cyclos.utils.binding.PropertyBinder;
import nl.strohalm.cyclos.utils.binding.SimpleCollectionBinder;
import nl.strohalm.cyclos.utils.conversion.IdConverter;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;

import org.apache.struts.action.ActionForward;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class EditTransferTypeController extends BaseRestController implements
		LocalSettingsChangeListener {
	private AccountTypeService accountTypeService;

	private ChannelService channelService;
	private TransferTypeService transferTypeService;
	private TransactionFeeService transactionFeeService;
	private PaymentCustomFieldService paymentCustomFieldService;
	private DataBinder<TransferType> dataBinder;
	private ReadWriteLock lock = new ReentrantReadWriteLock(true);
	private SettingsService settingsService;

	public SettingsService getSettingsService() {
		return settingsService;
	}

	public void setSettingsService(SettingsService settingsService) {
		this.settingsService = settingsService;
	}

	public ChannelService getChannelService() {
		return channelService;
	}

	public TransactionFeeService getTransactionFeeService() {
		return transactionFeeService;
	}

	public PaymentCustomFieldService getPaymentCustomFieldService() {
		return paymentCustomFieldService;
	}

	public AccountTypeService getAccountTypeService() {
		return accountTypeService;
	}

	public DataBinder<TransferType> getDataBinder() {
		try {
			lock.readLock().lock();
			if (dataBinder == null) {
				final LocalSettings localSettings = settingsService
						.getLocalSettings();

				final BeanBinder<Context> contextBinder = BeanBinder.instance(
						TransferType.Context.class, "context");
				contextBinder.registerBinder("payment",
						PropertyBinder.instance(Boolean.TYPE, "payment"));
				contextBinder.registerBinder("selfPayment",
						PropertyBinder.instance(Boolean.TYPE, "selfPayment"));

				final BeanBinder<LoanParameters> loanBinder = BeanBinder
						.instance(LoanParameters.class, "loan");
				loanBinder.registerBinder("type",
						PropertyBinder.instance(Loan.Type.class, "type"));
				loanBinder
						.registerBinder("repaymentDays", PropertyBinder
								.instance(Integer.class, "repaymentDays"));
				loanBinder.registerBinder("repaymentType", PropertyBinder
						.instance(TransferType.class, "repaymentType"));
				loanBinder.registerBinder("monthlyInterest", PropertyBinder
						.instance(BigDecimal.class, "monthlyInterest",
								localSettings.getNumberConverter()));
				loanBinder.registerBinder("monthlyInterestRepaymentType",
						PropertyBinder.instance(TransferType.class,
								"monthlyInterestRepaymentType"));
				loanBinder.registerBinder("grantFee", DataBinderHelper
						.amountConverter("grantFee", localSettings));
				loanBinder.registerBinder("grantFeeRepaymentType",
						PropertyBinder.instance(TransferType.class,
								"grantFeeRepaymentType"));
				loanBinder.registerBinder("expirationFee", DataBinderHelper
						.amountConverter("expirationFee", localSettings));
				loanBinder.registerBinder("expirationFeeRepaymentType",
						PropertyBinder.instance(TransferType.class,
								"expirationFeeRepaymentType"));
				loanBinder.registerBinder("expirationDailyInterest",
						PropertyBinder.instance(BigDecimal.class,
								"expirationDailyInterest",
								localSettings.getNumberConverter()));
				loanBinder
						.registerBinder("expirationDailyInterestRepaymentType",
								PropertyBinder.instance(TransferType.class,
										"expirationDailyInterestRepaymentType"));

				final BeanBinder<TransferType> binder = BeanBinder
						.instance(TransferType.class);
				binder.registerBinder(
						"id",
						PropertyBinder.instance(Long.class, "id",
								IdConverter.instance()));
				binder.registerBinder("name",
						PropertyBinder.instance(String.class, "name"));
				binder.registerBinder("description",
						PropertyBinder.instance(String.class, "description"));
				binder.registerBinder("confirmationMessage", PropertyBinder
						.instance(String.class, "confirmationMessage"));
				binder.registerBinder("context", contextBinder);
				binder.registerBinder("channels", SimpleCollectionBinder
						.instance(Channel.class, "channels"));
				binder.registerBinder("priority",
						PropertyBinder.instance(Boolean.TYPE, "priority"));
				binder.registerBinder("from",
						PropertyBinder.instance(AccountType.class, "from"));
				binder.registerBinder("to",
						PropertyBinder.instance(AccountType.class, "to"));
				binder.registerBinder("maxAmountPerDay", PropertyBinder
						.instance(BigDecimal.class, "maxAmountPerDay",
								localSettings.getNumberConverter()));
				binder.registerBinder("minAmount", PropertyBinder.instance(
						BigDecimal.class, "minAmount",
						localSettings.getNumberConverter()));
				binder.registerBinder("conciliable",
						PropertyBinder.instance(Boolean.TYPE, "conciliable"));
				binder.registerBinder("loan", loanBinder);
				binder.registerBinder("requiresAuthorization", PropertyBinder
						.instance(Boolean.TYPE, "requiresAuthorization"));
				binder.registerBinder("allowsScheduledPayments", PropertyBinder
						.instance(Boolean.TYPE, "allowsScheduledPayments"));
				binder.registerBinder("requiresFeedback", PropertyBinder
						.instance(Boolean.TYPE, "requiresFeedback"));
				binder.registerBinder("feedbackExpirationTime",
						DataBinderHelper
								.timePeriodBinder("feedbackExpirationTime"));
				binder.registerBinder(
						"feedbackReplyExpirationTime",
						DataBinderHelper
								.timePeriodBinder("feedbackReplyExpirationTime"));
				binder.registerBinder("defaultFeedbackComments", PropertyBinder
						.instance(String.class, "defaultFeedbackComments"));
				binder.registerBinder("defaultFeedbackLevel",
						PropertyBinder.instance(Reference.Level.class,
								"defaultFeedbackLevel"));
				binder.registerBinder("fixedDestinationMember", PropertyBinder
						.instance(Member.class, "fixedDestinationMember"));
				binder.registerBinder("reserveTotalAmountOnScheduling",
						PropertyBinder.instance(Boolean.TYPE,
								"reserveTotalAmountOnScheduling"));
				binder.registerBinder("allowCancelScheduledPayments",
						PropertyBinder.instance(Boolean.TYPE,
								"allowCancelScheduledPayments"));
				binder.registerBinder("allowBlockScheduledPayments",
						PropertyBinder.instance(Boolean.TYPE,
								"allowBlockScheduledPayments"));
				binder.registerBinder("showScheduledPaymentsToDestination",
						PropertyBinder.instance(Boolean.TYPE,
								"showScheduledPaymentsToDestination"));
				binder.registerBinder("allowSmsNotification", PropertyBinder
						.instance(Boolean.TYPE, "allowSmsNotification"));
				binder.registerBinder("transferListenerClass", PropertyBinder
						.instance(String.class, "transferListenerClass"));
				binder.registerBinder("transactionHierarchyVisibility",
						PropertyBinder.instance(
								TransactionHierarchyVisibility.class,
								"transactionHierarchyVisibility"));
				dataBinder = binder;
			}
			return dataBinder;
		} finally {
			lock.readLock().unlock();
		}
	}

	public TransferTypeService getTransferTypeService() {
		return transferTypeService;
	}

	@Override
	public void onLocalSettingsUpdate(final LocalSettingsEvent event) {
		try {
			lock.writeLock().lock();
			dataBinder = null;
		} finally {
			lock.writeLock().unlock();
		}
	}

	@Inject
	public void setAccountTypeService(
			final AccountTypeService accountTypeService) {
		this.accountTypeService = accountTypeService;
	}

	@Inject
	public void setChannelService(final ChannelService channelService) {
		this.channelService = channelService;
	}

	@Inject
	public void setPaymentCustomFieldService(
			final PaymentCustomFieldService paymentCustomFieldService) {
		this.paymentCustomFieldService = paymentCustomFieldService;
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

	public static class EditTransferTypeRequestDto {

	}

	public static class EditTransferTypeResponseDto {

	}

	@RequestMapping(value = "", method = RequestMethod.PUT)
	@ResponseBody
	protected ActionForward handleSubmit(final ActionContext context)
			throws Exception {
		final EditTransferTypeForm form = context.getForm();
		TransferType transferType = retrieveTransferType(form);
		final boolean isInsert = transferType.getId() == null;
		try {
			transferType = transferTypeService.save(transferType);
		} catch (final HasPendingPaymentsException e) {
			return context.sendError("transferType.error.hasPendingPayments");
		}
		context.sendMessage(isInsert ? "transferType.inserted"
				: "transferType.modified");
		final Map<String, Object> params = new HashMap<String, Object>();
		params.put("accountTypeId", form.getAccountTypeId());
		params.put("transferTypeId", transferType.getId());
		return ActionHelper.redirectWithParams(context.getRequest(),
				context.getSuccessForward(), params);
	}

	private TransferType retrieveTransferType(final EditTransferTypeForm form) {
		final TransferType transferType = getDataBinder().readFromString(
				form.getTransferType());
		transferType.setFrom(accountTypeService.load(transferType.getFrom()
				.getId()));
		transferType.setTo(accountTypeService
				.load(transferType.getTo().getId()));
		final Context context = transferType.getContext();
		if (transferType.isFromSystem() || transferType.isToSystem()
				|| transferType.getFrom().equals(transferType.getTo())) {
			final boolean enabled = "true".equals(form
					.getTransferType("enabled"));
			final boolean selfPayment = enabled && transferType.isFromSystem()
					&& transferType.isToSystem();
			final boolean payment = enabled && !selfPayment;
			context.setSelfPayment(selfPayment);
			context.setPayment(payment);
		}
		if (transferType.isFromMember() && context.isSelfPayment()) {
			transferType.setChannels(null);
		}
		return transferType;
	}
}
