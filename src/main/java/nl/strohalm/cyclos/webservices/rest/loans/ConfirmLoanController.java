package nl.strohalm.cyclos.webservices.rest.loans;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import nl.strohalm.cyclos.annotations.Inject;
import nl.strohalm.cyclos.controls.ActionContext;
//import nl.strohalm.cyclos.controls.loans.ConfirmLoanForm;
import nl.strohalm.cyclos.entities.access.TransactionPassword;
import nl.strohalm.cyclos.entities.access.User;
import nl.strohalm.cyclos.entities.accounts.AccountType;
import nl.strohalm.cyclos.entities.accounts.loans.Loan;
import nl.strohalm.cyclos.entities.accounts.loans.LoanGroup;
import nl.strohalm.cyclos.entities.accounts.loans.LoanPayment;
import nl.strohalm.cyclos.entities.accounts.transactions.TransferType;
import nl.strohalm.cyclos.entities.customization.fields.CustomField;
import nl.strohalm.cyclos.entities.customization.fields.CustomFieldValue;
import nl.strohalm.cyclos.entities.customization.fields.PaymentCustomField;
import nl.strohalm.cyclos.entities.customization.fields.PaymentCustomFieldValue;
import nl.strohalm.cyclos.entities.groups.Group;
import nl.strohalm.cyclos.entities.groups.OperatorGroup;
import nl.strohalm.cyclos.entities.members.Element;
import nl.strohalm.cyclos.entities.members.Member;
import nl.strohalm.cyclos.entities.settings.LocalSettings;
import nl.strohalm.cyclos.services.access.AccessService;
import nl.strohalm.cyclos.services.access.exceptions.BlockedCredentialsException;
import nl.strohalm.cyclos.services.access.exceptions.InvalidCredentialsException;
import nl.strohalm.cyclos.services.customization.PaymentCustomFieldService;
import nl.strohalm.cyclos.services.elements.ElementService;
import nl.strohalm.cyclos.services.groups.GroupService;
import nl.strohalm.cyclos.services.loangroups.LoanGroupService;
import nl.strohalm.cyclos.services.permissions.PermissionService;
import nl.strohalm.cyclos.services.settings.SettingsService;
import nl.strohalm.cyclos.services.transactions.DoPaymentDTO;
import nl.strohalm.cyclos.services.transactions.GrantLoanDTO;
import nl.strohalm.cyclos.services.transactions.GrantLoanWithInterestDTO;
import nl.strohalm.cyclos.services.transactions.GrantMultiPaymentLoanDTO;
import nl.strohalm.cyclos.services.transactions.LoanService;
import nl.strohalm.cyclos.services.transactions.PaymentService;
import nl.strohalm.cyclos.services.transactions.ProjectionDTO;
import nl.strohalm.cyclos.services.transactions.exceptions.AuthorizedPaymentInPastException;
import nl.strohalm.cyclos.services.transactions.exceptions.CreditsException;
import nl.strohalm.cyclos.services.transfertypes.TransactionFeeService;
import nl.strohalm.cyclos.services.transfertypes.TransferTypeService;
import nl.strohalm.cyclos.utils.CustomFieldHelper;
import nl.strohalm.cyclos.utils.CustomFieldHelper.Entry;
import nl.strohalm.cyclos.utils.RelationshipHelper;
import nl.strohalm.cyclos.utils.conversion.CoercionHelper;
import nl.strohalm.cyclos.utils.validation.RequiredError;
import nl.strohalm.cyclos.utils.validation.ValidationException;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class ConfirmLoanController extends BaseRestController {
	private LoanService loanService;
	private LoanGroupService loanGroupService;
	private TransactionFeeService transactionFeeService;
	private PaymentCustomFieldService paymentCustomFieldService;
	private PaymentService paymentService;
	private TransferTypeService transferTypeService;
	private GroupService groupService;
	private ElementService elementService;
	private PermissionService permissionService;
	private SettingsService settingsService;

    public GroupService getGroupService() {
        return groupService;
    }

    public void setGroupService(GroupService groupService) {
        this.groupService = groupService;
    }

    public ElementService getElementService() {
        return elementService;
    }

    public void setElementService(ElementService elementService) {
        this.elementService = elementService;
    }

    public PermissionService getPermissionService() {
        return permissionService;
    }

    public void setPermissionService(PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    public SettingsService getSettingsService() {
        return settingsService;
    }

    public void setSettingsService(SettingsService settingsService) {
        this.settingsService = settingsService;
    }
        
	private CustomFieldHelper customFieldHelper;

	@Inject
	public void setCustomFieldHelper(final CustomFieldHelper customFieldHelper) {
		this.customFieldHelper = customFieldHelper;
	}

	@Inject
	public void setLoanGroupService(final LoanGroupService loanGroupService) {
		this.loanGroupService = loanGroupService;
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

	public static class ConfirmLoanRequestDto {
		private String transactionPassword;
		private long memberId;
		private long loanGroupId;

		public long getLoanGroupId() {
			return loanGroupId;
		}

		public long getMemberId() {
			return memberId;
		}

		public String getTransactionPassword() {
			return transactionPassword;
		}

		public void setLoanGroupId(final long loanGroupId) {
			this.loanGroupId = loanGroupId;
		}

		public void setMemberId(final long memberId) {
			this.memberId = memberId;
		}

		public void setTransactionPassword(final String transactionPassword) {
			this.transactionPassword = transactionPassword;
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

		public void validateTransactionPassword() {

		}

		  public GrantLoanDTO getAttribute(String name){
			  return null;////kindly check with..
		  }
		
	}

	public static class ConfirmLoanResponseDto {
		long memberId;
		long loanGroupId;
		String message;

		public void setMemberId(long memberId) {
			this.memberId = memberId;
		}

		public void setLoanGroupId(long loanGroupId) {
			this.loanGroupId = loanGroupId;
		}

		public void setMessage(String message) {
			this.message = message;
		}

	}

	@RequestMapping(value = "admin/confirmLoan", method = RequestMethod.POST)
	@ResponseBody
	protected ConfirmLoanResponseDto handleSubmit(
			final ConfirmLoanRequestDto form) throws Exception {
		// final ConfirmLoanForm form = context.getForm();
		final GrantLoanDTO dto = validateLoan(form);

		// Check for the transaction password, if needed
		if (form.isTransactionPasswordEnabled()) {
			form.checkTransactionPassword(form.getTransactionPassword());
		}

		// Check which method we need to use to grant the loan
		Loan loan = null;
		try {
			loan = loanService.grant(dto);
		} catch (final CreditsException e) {
			// return
			// context.sendError(actionHelper.resolveErrorKey(e),actionHelper.resolveParameters(e));
		} catch (final AuthorizedPaymentInPastException e) {
			// return context.sendError("payment.error.authorizedInPast");
		}
		final boolean pending = loan.getTransfer().getProcessDate() == null;
		String message = null;
		ConfirmLoanResponseDto response = new ConfirmLoanResponseDto();
		if (pending) {
			message = "loan.awaitingAuthorization";
			response.setMessage(message);
		} else {
			message = "loan.granted";
			response.setMessage(message);
		}
		if (form.getMemberId() > 0) {
			long memberId = form.getMemberId();
			response.setMemberId(memberId);
			return response;
		} else {
			long loanGroupId = form.getLoanGroupId();
			response.setLoanGroupId(loanGroupId);
			return response;
		}
	}

	protected void prepareForm(final ConfirmLoanRequestDto context)
			throws Exception {
		final GrantLoanDTO loan = validateLoan(context);

		
		final boolean requestTransactionPassword = context
				.isTransactionPasswordEnabled();
		if (requestTransactionPassword) {
			context.validateTransactionPassword();
		}
		
		final Member member = elementService.load(loan.getMember().getId(),
				Element.Relationships.USER);
		final TransferType transferType = transferTypeService.load(loan
				.getTransferType().getId(), RelationshipHelper.nested(
				TransferType.Relationships.FROM,
				AccountType.Relationships.CURRENCY),
				TransferType.Relationships.TO);
		final LoanGroup loanGroup = loan.getLoanGroup() == null ? null
				: loanGroupService.load(loan.getLoanGroup().getId(),
						LoanGroup.Relationships.MEMBERS);
		final BigDecimal amount = loan.getAmount();
		loan.setLoanGroup(loanGroup);
		loan.setMember(member);
		loan.setTransferType(transferType);
		

		if (loanGroup != null) {
			
			final List<Member> membersInGroup = new ArrayList<Member>(
					loanGroup.getMembers());
			final LocalSettings localSettings = settingsService
					.getLocalSettings();
			Collections.sort(membersInGroup,
					localSettings.getMemberComparator());
			membersInGroup.remove(member);
			membersInGroup.add(0, member);
			
		}

		// Get the loan payments
		List<LoanPayment> payments = null;
		switch (loan.getLoanType()) {
		case MULTI_PAYMENT:
			payments = ((GrantMultiPaymentLoanDTO) loan).getPayments();
			break;
		case WITH_INTEREST:
			final GrantLoanWithInterestDTO dto = (GrantLoanWithInterestDTO) loan;
			final ProjectionDTO projection = new ProjectionDTO();
			projection.setAmount(dto.getAmount());
			projection.setDate(dto.getDate());
			projection.setFirstExpirationDate(dto.getFirstRepaymentDate());
			projection.setTransferType(dto.getTransferType());
			projection.setPaymentCount(dto.getPaymentCount());
			payments = loanService.calculatePaymentProjection(projection);
			break;
		}
		
		final Collection<PaymentCustomFieldValue> customValues = loan
				.getCustomValues();
		if (customValues != null) {
			final List<PaymentCustomField> customFields = paymentCustomFieldService
					.list(transferType, false);
			final Collection<Entry> entries = customFieldHelper.buildEntries(
					customFields, customValues);
			
			for (final Entry entry : entries) {
				final CustomField field = entry.getField();
				final CustomFieldValue fieldValue = entry.getValue();
				if (field.getType() == CustomField.Type.ENUMERATED) {
					final Long possibleValueId = CoercionHelper.coerce(
							Long.class, fieldValue.getValue());
					if (possibleValueId != null) {
						fieldValue.setPossibleValue(paymentCustomFieldService
								.loadPossibleValue(possibleValueId));
					}
				} else if (field.getType() == CustomField.Type.MEMBER) {
					final Long memberId = CoercionHelper.coerce(Long.class,
							fieldValue.getValue());
					if (memberId != null) {
						final Element element = elementService.load(memberId);
						if (element instanceof Member) {
							fieldValue.setMemberValue((Member) element);
						}
					}
				}

			}
			
		}

		
		final DoPaymentDTO payment = new DoPaymentDTO();
		payment.setTransferType(loan.getTransferType());
		payment.setAmount(loan.getAmount());
		payment.setTo(member);
		
	}

//	protected void validateForm(final ActionContext context) {
//		if (context.isTransactionPasswordEnabled()) {
//			final ConfirmLoanForm form = context.getForm();
//			if (StringUtils.isEmpty(form.getTransactionPassword())) {
//				throw new ValidationException("_transactionPassword",
//						"login.transactionPassword", new RequiredError());
//			}
//		}
//	}

	private GrantLoanDTO validateLoan(final ConfirmLoanRequestDto context) {
		final GrantLoanDTO payment = (GrantLoanDTO) context
				.getAttribute("loan");//kindly check with..
		if (payment == null) {
			throw new ValidationException();
		}
		return payment;
	}

	public static <T> T bean(final Class<T> requiredType) {
		return bean(requiredType);
	}
}
