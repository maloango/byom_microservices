package nl.strohalm.cyclos.webservices.rest.loans;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import nl.strohalm.cyclos.annotations.Inject;
import nl.strohalm.cyclos.controls.ActionContext;
import nl.strohalm.cyclos.controls.loans.LoanDetailsForm;
import nl.strohalm.cyclos.controls.loans.RepayLoanForm;
import nl.strohalm.cyclos.entities.Relationship;
import nl.strohalm.cyclos.entities.access.AdminUser;
import nl.strohalm.cyclos.entities.access.TransactionPassword;
import nl.strohalm.cyclos.entities.access.User;
import nl.strohalm.cyclos.entities.accounts.AccountOwner;
import nl.strohalm.cyclos.entities.accounts.AccountType;
import nl.strohalm.cyclos.entities.accounts.MemberAccount;
import nl.strohalm.cyclos.entities.accounts.MemberGroupAccountSettings;
import nl.strohalm.cyclos.entities.accounts.loans.Loan;
import nl.strohalm.cyclos.entities.accounts.transactions.Payment;
import nl.strohalm.cyclos.entities.accounts.transactions.TransferType;
import nl.strohalm.cyclos.entities.customization.fields.CustomFieldValue;
import nl.strohalm.cyclos.entities.customization.fields.PaymentCustomField;
import nl.strohalm.cyclos.entities.customization.fields.PaymentCustomFieldValue;
import nl.strohalm.cyclos.entities.exceptions.EntityNotFoundException;
import nl.strohalm.cyclos.entities.exceptions.UnexpectedEntityException;
import nl.strohalm.cyclos.entities.groups.Group;
import nl.strohalm.cyclos.entities.groups.OperatorGroup;
import nl.strohalm.cyclos.entities.members.Element;
import nl.strohalm.cyclos.entities.members.Member;
import nl.strohalm.cyclos.entities.settings.LocalSettings;
import nl.strohalm.cyclos.services.access.AccessService;
import nl.strohalm.cyclos.services.access.exceptions.BlockedCredentialsException;
import nl.strohalm.cyclos.services.access.exceptions.InvalidCredentialsException;
import nl.strohalm.cyclos.services.groups.GroupService;
import nl.strohalm.cyclos.services.settings.SettingsService;
import nl.strohalm.cyclos.services.transactions.LoanPaymentDTO;
import nl.strohalm.cyclos.services.transactions.LoanService;
import nl.strohalm.cyclos.services.transactions.PaymentService;
import nl.strohalm.cyclos.services.transactions.RepayLoanDTO;
import nl.strohalm.cyclos.services.transactions.exceptions.NotEnoughCreditsException;
import nl.strohalm.cyclos.services.transactions.exceptions.PartialInterestsAmountException;
import nl.strohalm.cyclos.services.transactions.exceptions.UpperCreditLimitReachedException;
import nl.strohalm.cyclos.utils.ActionHelper;
import nl.strohalm.cyclos.utils.RelationshipHelper;
import nl.strohalm.cyclos.utils.ResponseHelper;
import nl.strohalm.cyclos.utils.binding.BeanBinder;
import nl.strohalm.cyclos.utils.binding.BeanCollectionBinder;
import nl.strohalm.cyclos.utils.binding.DataBinder;
import nl.strohalm.cyclos.utils.binding.MapBean;
import nl.strohalm.cyclos.utils.binding.PropertyBinder;
import nl.strohalm.cyclos.utils.conversion.HtmlConverter;
import nl.strohalm.cyclos.utils.conversion.UnitsConverter;
import nl.strohalm.cyclos.utils.validation.RequiredError;
import nl.strohalm.cyclos.utils.validation.ValidationException;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;
import nl.strohalm.cyclos.webservices.rest.loans.ManageExpiredStatusController.ManageExpiredStatusRequestDto;

import org.apache.struts.action.ActionForward;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class RepayLoanController extends BaseRestController {

	private static final Relationship FETCH = RelationshipHelper
			.nested(Loan.Relationships.TRANSFER, Payment.Relationships.TYPE,
					TransferType.Relationships.FROM,
					AccountType.Relationships.CURRENCY);
	private PaymentService paymentService;
	private SettingsService settingsService;
	private LoanService loanService;
	private DataBinder<? extends LoanPaymentDTO> dataBinder;

	public PaymentService getPaymentService() {
		return paymentService;
	}

	@Inject
	public void setPaymentService(final PaymentService paymentService) {
		this.paymentService = paymentService;
	}

	protected Class<? extends LoanPaymentDTO> getDtoClass() {
		return RepayLoanDTO.class;
	}

	public static class RepayLoanRequestDto {
		private long loanGroupId;
		private long loanId;
		private long loanPaymentId;
		private long guaranteeId;
		private long memberId;

		public long getGuaranteeId() {
			return guaranteeId;
		}

		public long getLoanGroupId() {
			return loanGroupId;
		}

		public long getLoanId() {
			return loanId;
		}

		public long getLoanPaymentId() {
			return loanPaymentId;
		}

		public long getMemberId() {
			return memberId;
		}

		public void setGuaranteeId(final long guaranteeId) {
			this.guaranteeId = guaranteeId;
		}

		public void setLoanGroupId(final long loanGroupId) {
			this.loanGroupId = loanGroupId;
		}

		public void setLoanId(final long loanId) {
			this.loanId = loanId;
		}

		public void setLoanPaymentId(final long loanPaymentId) {
			this.loanPaymentId = loanPaymentId;
		}

		public void setMemberId(final long memberId) {
			this.memberId = memberId;
		}

		private String date;
		private String transactionPassword;

		public String getDate() {
			return date;
		}

		public String getTransactionPassword() {
			return transactionPassword;
		}

		public void setDate(final String date) {
			this.date = date;
		}

		public void setTransactionPassword(final String transactionPassword) {
			this.transactionPassword = transactionPassword;
		}

		private String amount;
		private MapBean customValues = new MapBean(true, "field", "value");

		public String getAmount() {
			return amount;
		}

		public MapBean getCustomValues() {
			return customValues;
		}

		public void setAmount(final String amount) {
			this.amount = amount;
		}

		public void setCustomValues(final MapBean customValues) {
			this.customValues = customValues;
		}

		public AccountOwner getAccountOwner() {
			try {
				final Element element = getElement();
				return element.getAccountOwner();
			} catch (final NullPointerException e) {
				return null;
			}
		}

		User user;

		@SuppressWarnings("unchecked")
		public <E extends Element> E getElement() {
			return (E) user.getElement();
		}

		@SuppressWarnings("unchecked")
		public <G extends Group> G getGroup() {
			final Element element = getElement();
			return (G) element.getGroup();
		}

		public boolean isTransactionPasswordEnabled() {
			Group loggedGroup = getGroup();
			if (loggedGroup instanceof OperatorGroup) {
				final GroupService groupService = bean(GroupService.class);
				loggedGroup = groupService.load(loggedGroup.getId(),
						RelationshipHelper.nested(
								OperatorGroup.Relationships.MEMBER,
								Element.Relationships.GROUP));
			}
			final TransactionPassword transactionPassword = loggedGroup
					.getBasicSettings().getTransactionPassword();
			return transactionPassword.isUsed();
		}

		public boolean isAdmin() {
			return user instanceof AdminUser;
		}

		public boolean isTransactionPasswordEnabled(
				final AccountType accountType) {
			if (!isTransactionPasswordEnabled()) {
				return false;
			} else if (isAdmin()) {
				return true; // the group settings is true
			} else { // checks the member-group settings
				final Member member = (Member) getAccountOwner();
				final GroupService groupService = bean(GroupService.class);
				try {
					final MemberGroupAccountSettings mgas = groupService
							.loadAccountSettings(member.getGroup().getId(),
									accountType.getId());
					return mgas.isTransactionPasswordRequired();
				} catch (final EntityNotFoundException e) {
					return false;
				}
			}
		}

		public void checkTransactionPassword(final String transactionPassword) {
			try {
				final AccessService accessService = bean(AccessService.class);
				accessService.checkTransactionPassword(transactionPassword);
			} catch (final InvalidCredentialsException e) {
				throw new ValidationException(
						"transactionPassword.error.invalid");
			} catch (final BlockedCredentialsException e) {
				// final HttpSession session = getSession();
				// session.setAttribute("errorReturnTo",
				// session.getAttribute("pathPrefix") + "/home");
				throw new ValidationException(
						"transactionPassword.error.blockedByTrials");
			} catch (final RuntimeException e) {
				throw e;
			}
		}
	}

	public static class RepayLoanResponseDto {
		private String message;
		Map<String, Object> params;
		String error;

		public void setError(String error) {
			this.error = error;
		}

		public void setMessage(String message) {
			this.message = message;
		}

		public void setParams(Map<String, Object> params) {
			this.params = params;
		}

	}

	@RequestMapping(value = "", method = RequestMethod.POST)
	@ResponseBody
	protected RepayLoanResponseDto handleSubmit(
			@RequestBody RepayLoanRequestDto form) throws Exception {
		// final RepayLoanForm form = context.getForm();

		final RepayLoanDTO dto = (RepayLoanDTO) resolveLoanDTO(form);
		final Loan loan = dto.getLoan();
		if (shouldValidateTransactionPassword(form, loan)) {
			form.checkTransactionPassword(form.getTransactionPassword());
		}
		String message;
		String error;
		RepayLoanResponseDto response = new RepayLoanResponseDto();
		// Check which method we have to call
		try {
			loanService.repay(dto);
		} catch (final NotEnoughCreditsException e) {
			error = "loan.repayment.error.enoughCredits";
			response.setError(error);
			return response;
		} catch (final UpperCreditLimitReachedException e) {
			error = "loan.repayment.error.upperCreditLimit";
			response.setError(error);
			return response;
		} catch (final PartialInterestsAmountException e) {
			final AccountType accountType = loan.getTransfer().getType()
					.getTo();
			final LocalSettings localSettings = settingsService
					.getLocalSettings();
			final UnitsConverter nc = localSettings
					.getUnitsConverter(accountType.getCurrency().getPattern());
			final BigDecimal baseRemainingAmount = e.getBaseRemainingAmount();
			final BigDecimal totalRemainingAmount = baseRemainingAmount.add(e
					.getInterestsAmount());
			error = "loan.repayment.error.partialInterestsAmount "
					+ nc.toString(baseRemainingAmount) + "  "
					+ nc.toString(totalRemainingAmount);
			response.setError(error);
			return response;
		}
		final Map<String, Object> params = new HashMap<String, Object>();
		params.put("loanId", form.getLoanId());
		params.put("memberId", form.getMemberId());
		params.put("loanGroupId", form.getLoanGroupId());
		message = "loan.repaid";
		response.setMessage(message);
		response.setParams(params);
		return response;
	}

	protected boolean shouldValidateTransactionPassword(
			final RepayLoanRequestDto form, final Loan loan) {
		if (form.getAccountOwner().equals(loan.getMember())) {
			// When a logged member performing an operation over a loan to
			// himself
			return form.isTransactionPasswordEnabled(loan.getTransferType()
					.getTo());
		} else {
			return form.isTransactionPasswordEnabled();
		}
	}

	public DataBinder<? extends LoanPaymentDTO> getDataBinder() {
		if (dataBinder == null) {
			final BeanBinder<? extends LoanPaymentDTO> binder = BeanBinder
					.instance(getDtoClass());
			initDataBinder(binder);
			dataBinder = binder;
		}
		return dataBinder;
	}

	protected LoanPaymentDTO resolveLoanDTO(final RepayLoanRequestDto form) {
		// final LoanDetailsForm form = context.getForm();
		final LoanPaymentDTO dto = getDataBinder().readFromString(form);
		// because it comes from a data binder it's an entity reference
		// then we can use the load method
		Loan loan = dto.getLoan();
		if (loan == null) {
			throw new ValidationException();
		}
		final Relationship[] relationships = new Relationship[] {
				RelationshipHelper.nested(Loan.Relationships.TRANSFER,
						Payment.Relationships.CUSTOM_VALUES),
				RelationshipHelper.nested(Loan.Relationships.TRANSFER,
						Payment.Relationships.TYPE),
				RelationshipHelper.nested(Loan.Relationships.TRANSFER,
						Payment.Relationships.TO,
						MemberAccount.Relationships.MEMBER,
						Element.Relationships.USER),
				Loan.Relationships.PAYMENTS,
				RelationshipHelper.nested(Loan.Relationships.TRANSFER,
						Payment.Relationships.CUSTOM_VALUES),
				Loan.Relationships.LOAN_GROUP, Loan.Relationships.TO_MEMBERS };
		loan = loanService.load(loan.getId(), relationships);

		dto.setLoan(loan);
		return dto;
	}

	protected ActionForward handleValidation(final RepayLoanRequestDto context) {
		try {
			// The super validation will handle the transaction password
			// super.validateForm(context);

			final RepayLoanDTO dto = (RepayLoanDTO) resolveLoanDTO(context);
			Loan loan = dto.getLoan();
			final BigDecimal amount = dto.getAmount();

			final ValidationException val = new ValidationException();
			val.setPropertyKey("amount", "loan.repayment.amount");
			if (loan == null) {
				val.addPropertyError("loan", new RequiredError());
			}
			if (amount == null
					|| amount.compareTo(paymentService.getMinimumPayment()) == -1) {
				val.addPropertyError("amount", new RequiredError());
			}
			val.throwIfHasErrors();

			loan = loanService.load(loan.getId(), FETCH);
			AccountType accountType;
			try {
				accountType = loan.getTransfer().getType().getLoan()
						.getRepaymentType().getFrom();
			} catch (final Exception e) {
				throw new UnexpectedEntityException(
						"Unable to retrieve loan account type");
			}
			final LocalSettings settings = settingsService.getLocalSettings();
			final UnitsConverter unitsConverter = settings
					.getUnitsConverter(accountType.getCurrency().getPattern());

			final Map<String, Object> fields = new HashMap<String, Object>();
			// fields.put("confirmationMessage", context.message(
			// "loan.repayment.confirmationMessage",unitsConverter.toString(amount)));

			// responseHelper.writeStatus(context.getResponse(),ResponseHelper.Status.SUCCESS,
			// fields);
		} catch (final ValidationException e) {
			// responseHelper.writeValidationErrors(context.getResponse(), e);
		}
		return null;
	}

	public static <T> BeanBinder<T> instance(final Class<T> beanClass) {
		return instance(beanClass, null);
	}

	public static <T> BeanBinder<T> instance(final Class<T> beanClass,
			final String path) {
		final BeanBinder<T> binder = new BeanBinder<T>();
		binder.setType(beanClass);
		binder.setPath(path);
		return binder;
	}

	public static <T> T bean(final Class<T> requiredType) {
		return bean(requiredType);
	}

	protected void initDataBinder(
			final BeanBinder<? extends LoanPaymentDTO> binder) {
		// super.initDataBinder(binder);
		final LocalSettings localSettings = settingsService.getLocalSettings();
		final BeanBinder<? extends CustomFieldValue> customValueBinder = BeanBinder
				.instance(PaymentCustomFieldValue.class);
		binder.registerBinder("amount", PropertyBinder.instance(
				BigDecimal.class, "amount", localSettings.getNumberConverter()));
		binder.registerBinder(
				"date",
				PropertyBinder.instance(Calendar.class, "date",
						localSettings.getRawDateConverter()));
		customValueBinder.registerBinder("field",
				PropertyBinder.instance(PaymentCustomField.class, "field"));
		customValueBinder.registerBinder(
				"value",
				PropertyBinder.instance(String.class, "value",
						HtmlConverter.instance()));
		binder.registerBinder("customValues", BeanCollectionBinder.instance(
				customValueBinder, "customValues"));
	}

}
