package nl.strohalm.cyclos.webservices.rest.loans;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Controller;

import nl.strohalm.cyclos.access.AdminMemberPermission;
import nl.strohalm.cyclos.access.MemberPermission;
import nl.strohalm.cyclos.access.OperatorPermission;
import nl.strohalm.cyclos.annotations.Inject;
import nl.strohalm.cyclos.controls.ActionContext;
import nl.strohalm.cyclos.controls.loans.BaseLoanActionForm;
import nl.strohalm.cyclos.controls.loans.LoanDetailsForm;
import nl.strohalm.cyclos.entities.Relationship;
import nl.strohalm.cyclos.entities.accounts.MemberAccount;
import nl.strohalm.cyclos.entities.accounts.loans.Loan;
import nl.strohalm.cyclos.entities.accounts.loans.LoanParameters;
import nl.strohalm.cyclos.entities.accounts.loans.LoanPayment;
import nl.strohalm.cyclos.entities.accounts.loans.LoanPayment.Status;
import nl.strohalm.cyclos.entities.accounts.loans.LoanRepaymentAmountsDTO;
import nl.strohalm.cyclos.entities.accounts.transactions.Payment;
import nl.strohalm.cyclos.entities.accounts.transactions.Transfer;
import nl.strohalm.cyclos.entities.accounts.transactions.TransferType;
import nl.strohalm.cyclos.entities.customization.fields.PaymentCustomField;
import nl.strohalm.cyclos.entities.members.Element;
import nl.strohalm.cyclos.entities.members.Member;
import nl.strohalm.cyclos.entities.members.Operator;
import nl.strohalm.cyclos.entities.settings.LocalSettings;
import nl.strohalm.cyclos.entities.settings.events.LocalSettingsEvent;
import nl.strohalm.cyclos.services.customization.PaymentCustomFieldService;
import nl.strohalm.cyclos.services.elements.ElementService;
import nl.strohalm.cyclos.services.groups.GroupService;
import nl.strohalm.cyclos.services.permissions.PermissionService;
import nl.strohalm.cyclos.services.settings.SettingsService;
import nl.strohalm.cyclos.services.transactions.LoanPaymentDTO;
import nl.strohalm.cyclos.services.transactions.LoanService;
import nl.strohalm.cyclos.services.transactions.PaymentService;
import nl.strohalm.cyclos.utils.Amount.Type;
import nl.strohalm.cyclos.utils.CustomFieldHelper;
import nl.strohalm.cyclos.utils.CustomFieldHelper.Entry;
import nl.strohalm.cyclos.utils.RelationshipHelper;
import nl.strohalm.cyclos.utils.binding.BeanBinder;
import nl.strohalm.cyclos.utils.binding.DataBinder;
import nl.strohalm.cyclos.utils.binding.PropertyBinder;
import nl.strohalm.cyclos.utils.validation.RequiredError;
import nl.strohalm.cyclos.utils.validation.ValidationException;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;

@Controller
public class LoanDetailsController extends BaseRestController {//later
	protected LoanService loanService;
	protected PaymentService paymentService;
	private PaymentCustomFieldService paymentCustomFieldService;
	private DataBinder<? extends LoanPaymentDTO> dataBinder;
	private GroupService groupService;
	private ElementService elementService;
	private PermissionService permissionService;
	private SettingsService settingsService;

	private CustomFieldHelper customFieldHelper;

	public DataBinder<? extends LoanPaymentDTO> getDataBinder() {
		if (dataBinder == null) {
			final BeanBinder<? extends LoanPaymentDTO> binder = BeanBinder
					.instance(getDtoClass());
			initDataBinder(binder);
			dataBinder = binder;
		}
		return dataBinder;
	}

	public void onLocalSettingsUpdate(final LocalSettingsEvent event) {
		dataBinder = null;
	}

	@Inject
	public void setCustomFieldHelper(final CustomFieldHelper customFieldHelper) {
		this.customFieldHelper = customFieldHelper;
	}

	@Inject
	public void setLoanService(final LoanService loanService) {
		this.loanService = loanService;
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

	protected void prepareForm(final ActionContext context) throws Exception {
		final HttpServletRequest request = context.getRequest();
		final LoanPaymentDTO loanDTO = resolveLoanDTO(context);
		final Loan loan = loanDTO.getLoan();
		final LoanPayment firstOpenPayment = loan.getFirstOpenPayment();
		final boolean closed = loan.getStatus().isClosed();
		final String currencyPattern = loan.getTransferType().getCurrency()
				.getPattern();
		request.setAttribute("loan", loan);
		request.setAttribute("showRelatedTransfer",
				paymentService.isVisible(loan.getTransfer()));
		request.setAttribute("currencyPattern", currencyPattern);
		if (loan.getParameters().getType() == Loan.Type.WITH_INTEREST) {
			final LoanParameters params = loan.getParameters();
			if (params.getMonthlyInterestAmount() != null) {
				request.setAttribute(
						"monthlyInterestPattern",
						params.getMonthlyInterestAmount().getType() == Type.FIXED ? currencyPattern
								: "");
			}
			if (params.getExpirationDailyInterestAmount() != null) {
				request.setAttribute(
						"expirationDailyInterestPattern",
						params.getExpirationDailyInterestAmount().getType() == Type.FIXED ? currencyPattern
								: "");
			}
			if (params.getExpirationFee() != null) {
				request.setAttribute(
						"expirationFeePattern",
						params.getExpirationFee().getType() == Type.FIXED ? currencyPattern
								: "");
			}
			if (params.getGrantFee() != null) {
				request.setAttribute("grantFeePattern", params.getGrantFee()
						.getType() == Type.FIXED ? currencyPattern : "");
			}
		}

		final Member member = elementService.load(loan.getMember().getId(),
				Element.Relationships.GROUP);
		// Get the custom values
		final Transfer transfer = loan.getTransfer();
		final List<PaymentCustomField> customFields = paymentCustomFieldService
				.list(transfer.getType(), true);
		final Collection<Entry> entries = customFieldHelper.buildEntries(
				customFields, transfer.getCustomValues());
		request.setAttribute("customFields", entries);

		if (CollectionUtils.isNotEmpty(loan.getToMembers())) {
			// Ensure the responsible is the first member shown and the list
			// will be sorted
			final List<Member> membersInGroup = new ArrayList<Member>(
					loan.getToMembers());
			final LocalSettings localSettings = settingsService
					.getLocalSettings();
			Collections.sort(membersInGroup,
					localSettings.getMemberComparator());
			membersInGroup.remove(member);
			membersInGroup.add(0, member);
			request.setAttribute("membersInLoan", membersInGroup);
		}

		boolean canRepay = false;
		boolean canDiscard = false;
		boolean canMarkAsInProcess = false;
		boolean canMarkAsUnrecoverable = false;
		boolean canMarkAsRecovered = false;
		if (!closed && transfer.getProcessDate() != null) {
			final Status firstOpenPaymentStatus = firstOpenPayment.getStatus();
			if ((firstOpenPaymentStatus == LoanPayment.Status.IN_PROCESS)) {
				// Admins can mark in-process loans as unrecoverable or
				// recovered
				if (permissionService
						.hasPermission(AdminMemberPermission.LOANS_MANAGE_EXPIRED_STATUS)) {
					canMarkAsRecovered = true;
					canMarkAsUnrecoverable = true;
				}
			} else {
				if (context.isMember()) {
					final Member loggedElement = context.getElement();
					final boolean repayByGroup = loggedElement.getMemberGroup()
							.getMemberSettings().isRepayLoanByGroup();
					if (loggedElement.equals(member)
							|| (repayByGroup && loan.getToMembers().contains(
									loggedElement))) {
						canRepay = permissionService
								.hasPermission(MemberPermission.LOANS_REPAY);
					}
				} else if (context.isOperator()) {
					final Operator operator = context.getElement();
					if (operator.getMember().equals(member)) {
						canRepay = permissionService
								.hasPermission(OperatorPermission.LOANS_REPAY);
					}
				} else if (context.isAdmin()) {
					// When a member is passed, it has to be the responsible of
					// the loan
					final boolean isThroughMember = ((LoanDetailsForm) context
							.getForm()).getMemberId() > 0;
					final boolean isTheResponsible = ((LoanDetailsForm) context
							.getForm()).getMemberId() == loan.getMember()
							.getId();

					canRepay = permissionService
							.hasPermission(AdminMemberPermission.LOANS_REPAY)
							&& (!isThroughMember || (isThroughMember && isTheResponsible));
					canDiscard = permissionService
							.hasPermission(AdminMemberPermission.LOANS_DISCARD);
					canMarkAsInProcess = firstOpenPaymentStatus == LoanPayment.Status.EXPIRED
							&& permissionService
									.hasPermission(AdminMemberPermission.LOANS_MANAGE_EXPIRED_STATUS);
				}
			}
		}

		final boolean canPerformExpiredAction = canMarkAsInProcess
				|| canMarkAsUnrecoverable || canMarkAsRecovered;
		request.setAttribute("canRepay", canRepay);
		request.setAttribute("canDiscard", canDiscard);
		request.setAttribute("canMarkAsInProcess", canMarkAsInProcess);
		request.setAttribute("canMarkAsRecovered", canMarkAsRecovered);
		request.setAttribute("canMarkAsUnrecoverable", canMarkAsUnrecoverable);
		request.setAttribute("canPerformExpiredAction", canPerformExpiredAction);

		if (canRepay || canDiscard || canPerformExpiredAction) {
			// Loans with interests only accept the total amount
			final boolean allowPartialRepayments = transfer.getType().getLoan()
					.getType().allowsPartialRepayments();
			request.setAttribute("allowPartialRepayments",
					allowPartialRepayments);

			final LoanRepaymentAmountsDTO dto = loanService
					.getLoanPaymentAmount(loanDTO);
			request.setAttribute("repaymentAmounts", dto);

			final boolean requestTransactionPassword = shouldValidateTransactionPassword(
					context, loan);
			if (requestTransactionPassword) {
				context.validateTransactionPassword();
			}
			request.setAttribute("requestTransactionPassword",
					requestTransactionPassword);
		}

		if (canRepay) {
			final TransferType repaymentType = loan.getTransfer().getType()
					.getLoan().getRepaymentType();
			request.setAttribute("repaymentTransferTypeId",
					repaymentType.getId());
		}
	}

	protected LoanPaymentDTO resolveLoanDTO(final ActionContext context) {
		final LoanDetailsForm form = context.getForm();
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

	protected boolean shouldValidateTransactionPassword(
			final ActionContext context, final Loan loan) {
		if (context.getAccountOwner().equals(loan.getMember())) {
			// When a logged member performing an operation over a loan to
			// himself
			return context.isTransactionPasswordEnabled(loan.getTransferType()
					.getTo());
		} else {
			return context.isTransactionPasswordEnabled();
		}
	}

	protected void validateForm(final ActionContext context) {
		final LoanPaymentDTO loanDTO = resolveLoanDTO(context);
		if (shouldValidateTransactionPassword(context, loanDTO.getLoan())) {
			final BaseLoanActionForm form = context.getForm();
			if (StringUtils.isEmpty(form.getTransactionPassword())) {
				throw new ValidationException("_transactionPassword",
						"login.transactionPassword", new RequiredError());
			}
		}
	}

}
