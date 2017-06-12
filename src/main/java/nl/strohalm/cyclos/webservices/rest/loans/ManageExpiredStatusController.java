package nl.strohalm.cyclos.webservices.rest.loans;

import java.util.HashMap;
import java.util.Map;

import nl.strohalm.cyclos.entities.Relationship;
import nl.strohalm.cyclos.entities.access.AdminUser;
import nl.strohalm.cyclos.entities.access.TransactionPassword;
import nl.strohalm.cyclos.entities.access.User;
import nl.strohalm.cyclos.entities.accounts.AccountOwner;
import nl.strohalm.cyclos.entities.accounts.AccountType;
import nl.strohalm.cyclos.entities.accounts.MemberAccount;
import nl.strohalm.cyclos.entities.accounts.MemberGroupAccountSettings;
import nl.strohalm.cyclos.entities.accounts.loans.Loan;
import nl.strohalm.cyclos.entities.accounts.loans.LoanPayment;
import nl.strohalm.cyclos.entities.accounts.transactions.Payment;
import nl.strohalm.cyclos.entities.exceptions.EntityNotFoundException;
import nl.strohalm.cyclos.entities.groups.Group;
import nl.strohalm.cyclos.entities.groups.OperatorGroup;
import nl.strohalm.cyclos.entities.members.Element;
import nl.strohalm.cyclos.entities.members.Member;
import nl.strohalm.cyclos.services.groups.GroupService;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class ManageExpiredStatusController extends BaseRestController {
	private LoanService loanService;
	private DataBinder<? extends LoanPaymentDTO> dataBinder;

	public static class ManageExpiredStatusRequestDto {
		private String status;
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

		public String getStatus() {
			return status;
		}

		public void setStatus(final String status) {
			this.status = status;
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

		public void checkTransactionPassword(String transactionPassword2) {
			//

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

		public boolean isTransactionPasswordEnabled(
				final AccountType accountType) {
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

		public boolean isAdmin() {
			return user instanceof AdminUser;
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

	public static class ManageExpiredStatusResponseDto {
		private String statusMsg;
		Map<String, Object> params;

		public ManageExpiredStatusResponseDto(String statusMsg,
				Map<String, Object> params) {
			super();
			this.statusMsg = statusMsg;
			this.params = params;
		}

		public String getMessage() {
			return statusMsg;
		}

		public void setMessage(String message) {
			this.statusMsg = message;
		}
	}

	@RequestMapping(value = "", method = RequestMethod.PUT)
	@ResponseBody
	protected ManageExpiredStatusResponseDto handleSubmit(
			final ManageExpiredStatusRequestDto form) throws Exception {

		LoanPayment.Status status;
		try {
			status = LoanPayment.Status.valueOf(form.getStatus());
		} catch (final Exception e) {
			throw new ValidationException();
		}

		final Loan loan = resolveLoanDTO(form).getLoan();
		if (shouldValidateTransactionPassword(form, loan)) {
			form.checkTransactionPassword(form.getTransactionPassword());
		}

		switch (status) {
		case IN_PROCESS:
			loanService.markAsInProcess(loan);
			break;
		case RECOVERED:
			loanService.markAsRecovered(loan);
			break;
		case UNRECOVERABLE:
			loanService.markAsUnrecoverable(loan);
			break;
		default:
			throw new ValidationException();
		}

		final String statusMsg = "loan.status." + status.name()
				+ "loan.changedExpiredStatus";

		final Map<String, Object> params = new HashMap<String, Object>();
		params.put("loanId", form.getLoanId());
		params.put("memberId", form.getMemberId());
		params.put("loanGroupId", form.getLoanGroupId());
		ManageExpiredStatusResponseDto response = new ManageExpiredStatusResponseDto(
				statusMsg, params);
		return response;
	}

	private boolean shouldValidateTransactionPassword(
			ManageExpiredStatusRequestDto form, Loan loan) {
		//
		if (form.getAccountOwner().equals(loan.getMember())) {
			// When a logged member performing an operation over a loan to
			// himself
			return form.isTransactionPasswordEnabled(loan.getTransferType()
					.getTo());
		} else {
			return form.isTransactionPasswordEnabled();
		}
		return false;
	}

	protected LoanPaymentDTO resolveLoanDTO(
			final ManageExpiredStatusRequestDto form) {
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

	protected void initDataBinder(
			final BeanBinder<? extends LoanPaymentDTO> binder) {
		binder.registerBinder("loan",
				PropertyBinder.instance(Loan.class, "loanId"));
		binder.registerBinder("loanPayment",
				PropertyBinder.instance(LoanPayment.class, "loanPaymentId"));
	}

}
