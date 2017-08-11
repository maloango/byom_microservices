package nl.strohalm.cyclos.webservices.rest.invoices;

import java.util.Collection;
import java.util.List;

import nl.strohalm.cyclos.annotations.Inject;
import nl.strohalm.cyclos.entities.Relationship;
import nl.strohalm.cyclos.entities.access.AdminUser;
import nl.strohalm.cyclos.entities.access.MemberUser;
import nl.strohalm.cyclos.entities.access.OperatorUser;
import nl.strohalm.cyclos.entities.access.User;
import nl.strohalm.cyclos.entities.accounts.AccountOwner;
import nl.strohalm.cyclos.entities.accounts.AccountType;
import nl.strohalm.cyclos.entities.accounts.transactions.Invoice;
import nl.strohalm.cyclos.entities.accounts.transactions.Payment;
import nl.strohalm.cyclos.entities.accounts.transactions.ScheduledPayment;
import nl.strohalm.cyclos.entities.accounts.transactions.Transfer;
import nl.strohalm.cyclos.entities.accounts.transactions.TransferType;
import nl.strohalm.cyclos.entities.customization.fields.PaymentCustomField;
import nl.strohalm.cyclos.entities.exceptions.EntityNotFoundException;
import nl.strohalm.cyclos.entities.groups.MemberGroup;
import nl.strohalm.cyclos.entities.members.Administrator;
import nl.strohalm.cyclos.entities.members.Element;
import nl.strohalm.cyclos.entities.members.Member;
import nl.strohalm.cyclos.entities.members.Operator;
import nl.strohalm.cyclos.services.customization.PaymentCustomFieldService;
import nl.strohalm.cyclos.services.elements.ElementService;
import nl.strohalm.cyclos.services.permissions.PermissionService;
import nl.strohalm.cyclos.services.transactions.InvoiceService;
import nl.strohalm.cyclos.services.transfertypes.TransferTypeService;
import nl.strohalm.cyclos.utils.CustomFieldHelper;
import nl.strohalm.cyclos.utils.CustomFieldHelper.Entry;
import nl.strohalm.cyclos.utils.RelationshipHelper;
import nl.strohalm.cyclos.utils.validation.ValidationException;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class InvoiceDetailsController extends BaseRestController {
	private static final Relationship[] FETCH = {
			Invoice.Relationships.CUSTOM_VALUES,
			RelationshipHelper.nested(Invoice.Relationships.FROM_MEMBER,
					Element.Relationships.USER),
			RelationshipHelper.nested(Invoice.Relationships.TO_MEMBER,
					Element.Relationships.USER),
			RelationshipHelper.nested(Invoice.Relationships.SENT_BY,
					Element.Relationships.USER),
			RelationshipHelper.nested(Invoice.Relationships.PERFORMED_BY,
					Element.Relationships.USER),
			RelationshipHelper.nested(
					Invoice.Relationships.DESTINATION_ACCOUNT_TYPE,
					AccountType.Relationships.CURRENCY),
			Invoice.Relationships.TRANSFER,
			RelationshipHelper.nested(Invoice.Relationships.TRANSFER_TYPE,
					TransferType.Relationships.TO),
			Invoice.Relationships.PAYMENTS };
	private InvoiceService invoiceService;
	private TransferTypeService transferTypeService;
	private PaymentCustomFieldService paymentCustomFieldService;
	private CustomFieldHelper customFieldHelper;
	private ElementService elementService;
	private static PermissionService permissionService;

	public InvoiceService getInvoiceService() {
		return invoiceService;
	}

	public TransferTypeService getTransferTypeService() {
		return transferTypeService;
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
	public void setTransferTypeService(
			final TransferTypeService transferTypeService) {
		this.transferTypeService = transferTypeService;
	}

	public static class InvoiceDetailsRequestDto {
		private long invoiceId;
		private long memberId;
		private long transferTypeId;
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

		public long getTransferTypeId() {
			return transferTypeId;
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

		public void setTransferTypeId(final long transferTypeId) {
			this.transferTypeId = transferTypeId;
		}

		public boolean isBroker() {
			if (!isMember()) {
				return false;
			}
			final Member member = getElement();
			return member.getMemberGroup().isBroker();
		}

		public boolean isMember() {
			return user instanceof MemberUser;
		}

		public <E extends Element> E getElement() {
			return (E) user.getElement();
		}

		User user;

		public boolean isAdmin() {
			return user instanceof AdminUser;
		}

		public AccountOwner getAccountOwner() {
			try {
				final Element element = getElement();
				return element.getAccountOwner();
			} catch (final NullPointerException e) {
				return null;
			}
		}

		public boolean isMemberOf(final Operator operator) {
			if (operator == null || !isMember()) {
				return false;
			}

			return permissionService.manages(operator);
		}

		public boolean isOperator() {
			return user instanceof OperatorUser;
		}
	}

	public static class InvoiceDetailsResponseDto {
		Collection<Entry> customFields;
		Invoice invoice;
		Member member;
		List<TransferType> possibleTransferTypes;
		boolean toMe;
		boolean fromMe;
		boolean canAccept;
		boolean canDeny;
		boolean canCancel;
		boolean showDestinationAccountType;
		boolean showPerformedBy;
		boolean showSentBy;
		String unitsPattern;
		long transferId;
		long paymentId;

		public InvoiceDetailsResponseDto(Collection<Entry> customFields,
				Invoice invoice, Member member,
				List<TransferType> possibleTransferTypes, boolean toMe,
				boolean fromMe, boolean canAccept, boolean canDeny,
				boolean canCancel, boolean showDestinationAccountType,
				boolean showPerformedBy, boolean showSentBy,
				String unitsPattern, long transferId, long paymentId) {
			super();
			this.customFields = customFields;
			this.invoice = invoice;
			this.member = member;
			this.possibleTransferTypes = possibleTransferTypes;
			this.toMe = toMe;
			this.fromMe = fromMe;
			this.canAccept = canAccept;
			this.canDeny = canDeny;
			this.canCancel = canCancel;
			this.showDestinationAccountType = showDestinationAccountType;
			this.showPerformedBy = showPerformedBy;
			this.showSentBy = showSentBy;
			this.unitsPattern = unitsPattern;
			this.transferId = transferId;
			this.paymentId = paymentId;
		}
                public InvoiceDetailsResponseDto(){
                    
                }

	}

	@RequestMapping(value = "admin/invoiceDetails", method = RequestMethod.POST)
	@ResponseBody
	protected InvoiceDetailsResponseDto executeAction(
			@RequestBody InvoiceDetailsRequestDto form) throws Exception {
		InvoiceDetailsResponseDto response =null;
                try{
		final long id = form.getInvoiceId();
		if (id <= 0) {
			throw new ValidationException();
		}
		Collection<Entry> entries = null;
		long memberId;
		if (form.isBroker() || form.isAdmin()) {
			memberId = form.getMemberId();
		} else { // member or operator
			memberId = ((Member) form.getAccountOwner()).getId();
		}
		final Member member = (Member) (memberId <= 0 ? null : elementService
				.load(memberId));

		final Invoice invoice = invoiceService.load(id, FETCH);

		if (member != null && !member.equals(invoice.getFromMember())
				&& !member.equals(invoice.getToMember())) {
			// The passed member id is not related to the invoice
			throw new ValidationException();
		}

		// Get the custom values
		final List<TransferType> possibleTransferTypes = invoiceService
				.getPossibleTransferTypes(invoice);
		if (possibleTransferTypes.size() == 1) {
			final TransferType transferType = possibleTransferTypes.iterator()
					.next();
			final List<PaymentCustomField> customFields = paymentCustomFieldService
					.list(transferType, false);
			entries = customFieldHelper.buildEntries(customFields,
					invoice.getCustomValues());
			// request.setAttribute("customFields", entries);
		}

		final TransferType transferType = invoice.getTransferType();
		AccountType accountType = invoice.getDestinationAccountType();
		if (accountType == null && transferType != null) {
			accountType = transferType.getTo();
		}

		final boolean toMe = form.getAccountOwner().equals(invoice.getTo());
		final boolean fromMe = form.getAccountOwner().equals(invoice.getFrom());

		// Only show the destination account type when not logged as the invoice
		// to and the invoice sender has more than one accounts
		boolean showDestinationAccountType = false;
		if (!form.getElement().equals(invoice.getToMember())
				&& transferType == null
				&& invoice.getDestinationAccountType() != null) {
			final Member fromMember = elementService.load(invoice
					.getFromMember().getId(), RelationshipHelper.nested(
					Element.Relationships.GROUP,
					MemberGroup.Relationships.ACCOUNT_SETTINGS));
			final int accounts = fromMember.getMemberGroup()
					.getAccountSettings().size();
			showDestinationAccountType = accounts > 1;
		}

		final boolean showSentBy = shouldShowSentBy(form, invoice);
		final boolean showPerformedBy = shouldShowPerformedBy(form, invoice);

		final Payment payment = invoice.getPayment();
		long transferId = 0;
		long paymentId = 0;
		if (payment instanceof Transfer) {
			transferId = payment.getId();
		} else if (payment instanceof ScheduledPayment) {
			paymentId = payment.getId();
		}

		final boolean canAccept = invoiceService.canAccept(invoice);
		final boolean canDeny = invoiceService.canDeny(invoice);
		final boolean canCancel = invoiceService.canCancel(invoice);
		String unitsPattern = accountType.getCurrency().getPattern();
		// request.setAttribute("unitsPattern", pattern);
		response = new InvoiceDetailsResponseDto(
				entries, invoice, member, possibleTransferTypes, toMe, fromMe,
				canAccept, canDeny, canCancel, showDestinationAccountType,
				showPerformedBy, showSentBy, unitsPattern, transferId,
				paymentId);}
                catch(EntityNotFoundException e){
                    e.printStackTrace();
                } catch (ValidationException e) {
                    e.printStackTrace();
            }
		return response;
	}

	/**
	 * Returns whether the 'performed by' should be shown to the user
	 */
        
	private boolean shouldShowPerformedBy(final InvoiceDetailsRequestDto form,
			final Invoice invoice) {
		boolean showPerformedBy = false;
		String message = null;
		// final HttpServletRequest request = form.getRequest();
		final Element performedBy = invoice.getPerformedBy();
		if (performedBy != null) {
			if (performedBy instanceof Administrator) {
				if (form.isAdmin()) {
					// request.setAttribute("performedByAdmin", performedBy);
					showPerformedBy = true;
				} else {
					// Don't disclose to member which admin performed the action
					// request.setAttribute("performedBySystem", true);
					// Only show performed by if invoice was not performed by
					// the normal owner
					final boolean shouldBeFromSystem = invoice.getStatus() == Invoice.Status.CANCELLED;
					showPerformedBy = shouldBeFromSystem
							&& !invoice.isFromSystem();
				}
			} else {
				Member shouldHavePerformed = null;
				switch (invoice.getStatus()) {
				case ACCEPTED:
				case DENIED:
					shouldHavePerformed = invoice.getToMember();
					break;
				case CANCELLED:
					shouldHavePerformed = invoice.getFromMember();
					break;
				}
				if (performedBy.equals(shouldHavePerformed)) {
					showPerformedBy = false;
				} else if ((performedBy instanceof Operator)
						&& (form.isMemberOf((Operator) performedBy) || form
								.isOperator())) {
					// request.setAttribute("performedByOperator", performedBy);
					showPerformedBy = true;
				} else {
					// request.setAttribute("performedByMember",performedBy.getAccountOwner());
					showPerformedBy = true;
				}
			}
		}
		return showPerformedBy;
	}

	/**
	 * Returns whether the 'sent by' should be shown to the user
	 */
	private boolean shouldShowSentBy(final InvoiceDetailsRequestDto form,
			final Invoice invoice) {
		boolean showSentBy = false;
		// final HttpServletRequest request = form.getRequest();
		final Element sentBy = invoice.getSentBy();
		if (sentBy != null) {
			if (sentBy instanceof Administrator) {
				if (form.isAdmin()) {
					// request.setAttribute("sentByAdmin", sentBy);
					showSentBy = true;
				} else {
					// Don't disclose to member which admin sent the invoice
					// request.setAttribute("sentBySystem", true);
					showSentBy = !invoice.isFromSystem(); // Only show sent by
															// if not a regular
															// system -> member
															// invoice,
				}
			} else if ((sentBy instanceof Operator)
					&& (form.isMemberOf((Operator) sentBy) || form.isOperator())) {
				// request.setAttribute("sentByOperator", sentBy);
				showSentBy = true;
			} else if (!invoice.getFromMember().equals(sentBy)) {
				final Member member = (Member) sentBy.getAccountOwner();
				// request.setAttribute("sentByMember", member);
				showSentBy = !invoice.getFrom().equals(member);
			}
		}
		return showSentBy;
	}
}
