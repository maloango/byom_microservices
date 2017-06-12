package nl.strohalm.cyclos.webservices.rest.loans;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import nl.strohalm.cyclos.entities.Relationship;
import nl.strohalm.cyclos.entities.access.TransactionPassword;
import nl.strohalm.cyclos.entities.access.User;
import nl.strohalm.cyclos.entities.accounts.AccountOwner;
import nl.strohalm.cyclos.entities.accounts.AccountType;
import nl.strohalm.cyclos.entities.accounts.MemberAccount;
import nl.strohalm.cyclos.entities.accounts.MemberGroupAccountSettings;
import nl.strohalm.cyclos.entities.accounts.loans.Loan;
import nl.strohalm.cyclos.entities.accounts.transactions.Payment;
import nl.strohalm.cyclos.entities.exceptions.EntityNotFoundException;
import nl.strohalm.cyclos.entities.groups.Group;
import nl.strohalm.cyclos.entities.groups.OperatorGroup;
import nl.strohalm.cyclos.entities.members.Element;
import nl.strohalm.cyclos.entities.members.Member;
import nl.strohalm.cyclos.entities.settings.LocalSettings;
import nl.strohalm.cyclos.services.groups.GroupService;
import nl.strohalm.cyclos.services.settings.SettingsService;
import nl.strohalm.cyclos.services.transactions.LoanPaymentDTO;
import nl.strohalm.cyclos.services.transactions.LoanService;
import nl.strohalm.cyclos.utils.RelationshipHelper;
import nl.strohalm.cyclos.utils.SpringHelper;
import nl.strohalm.cyclos.utils.binding.BeanBinder;
import nl.strohalm.cyclos.utils.binding.DataBinder;
import nl.strohalm.cyclos.utils.binding.PropertyBinder;
import nl.strohalm.cyclos.utils.validation.ValidationException;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class DiscardLoanController extends BaseRestController {
	private LoanService loanService;
	private SettingsService settingsService;
	private DataBinder<? extends LoanPaymentDTO> dataBinder;

	public static class DiscardLoanRequestDto {
		private String date;
		private String transactionPassword;
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

		public AccountOwner getAccountOwner() {
			try {
				final Element element = getElement();
				return element.getAccountOwner();
			} catch (final NullPointerException e) {
				return null;
			}
		}

		User user;

		public <E extends Element> E getElement() {
			return (E) user.getElement();

		}

		public void checkTransactionPassword(String transactionPassword2) {

		}

		public boolean isTransactionPasswordEnabled(AccountType to) {
			if (!isTransactionPasswordEnabled()) {
				return false;
			} else if (isAdmin()) {
				return true; // the group settings is true
			} else { // checks the member-group settings
				final Member member = (Member) getAccountOwner();
				final GroupService groupService = SpringHelper.bean(
						getServletContext(), GroupService.class);
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

		public boolean isTransactionPasswordEnabled() {
			Group loggedGroup = getGroup();
			if (loggedGroup instanceof OperatorGroup) {
				final GroupService groupService = SpringHelper.bean(
						getServletContext(), GroupService.class);
				loggedGroup = groupService.load(loggedGroup.getId(),
						RelationshipHelper.nested(
								OperatorGroup.Relationships.MEMBER,
								Element.Relationships.GROUP));
			}
			final TransactionPassword transactionPassword = loggedGroup
					.getBasicSettings().getTransactionPassword();
			return transactionPassword.isUsed();
		}
	}

	public static class DiscardLoanResponseDto {
		private String message;
		Map<String, Object> params;

		public DiscardLoanResponseDto(String message, Map<String, Object> params) {
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
	protected DiscardLoanResponseDto handleSubmit(
			@RequestBody DiscardLoanRequestDto form) throws Exception {
		// final DiscardLoanForm form = context.getForm();

		final LoanPaymentDTO dto = resolveLoanDTO(form);
		final Loan loan = dto.getLoan();
		if (shouldValidateTransactionPassword(form, loan)) {
			form.checkTransactionPassword(form.getTransactionPassword());
		}
		loanService.discard(dto);

		final Map<String, Object> params = new HashMap<String, Object>();
		params.put("loanId", form.getLoanId());
		params.put("memberId", form.getMemberId());
		params.put("loanGroupId", form.getLoanGroupId());
		String message = "loan.discarded";
		DiscardLoanResponseDto response = new DiscardLoanResponseDto(message,
				params);
		return response;
	}

	private LoanPaymentDTO resolveLoanDTO(DiscardLoanRequestDto form) {
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

	public DataBinder<? extends LoanPaymentDTO> getDataBinder() {
		if (dataBinder == null) {
			final BeanBinder<? extends LoanPaymentDTO> binder = BeanBinder
					.instance(getDtoClass());
			initDataBinder(binder);
			dataBinder = binder;
		}
		return dataBinder;
	}

	protected Class<? extends LoanPaymentDTO> getDtoClass() {
		return LoanPaymentDTO.class;
	}

	private boolean shouldValidateTransactionPassword(
			DiscardLoanRequestDto form, Loan loan) {
		if (form.getAccountOwner().equals(loan.getMember())) {
			// When a logged member performing an operation over a loan to
			// himself
			return form.isTransactionPasswordEnabled(loan.getTransferType()
					.getTo());
		} else {
			return form.isTransactionPasswordEnabled();
		}
	}

	protected void initDataBinder(
			final BeanBinder<? extends LoanPaymentDTO> binder) {
		// super.initDataBinder(binder);
		final LocalSettings localSettings = settingsService.getLocalSettings();
		binder.registerBinder(
				"date",
				PropertyBinder.instance(Calendar.class, "date",
						localSettings.getRawDateConverter()));
	}

}
