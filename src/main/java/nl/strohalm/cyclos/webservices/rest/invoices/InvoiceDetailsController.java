package nl.strohalm.cyclos.webservices.rest.invoices;

import java.util.Collection;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import nl.strohalm.cyclos.annotations.Inject;
import nl.strohalm.cyclos.entities.Relationship;
import nl.strohalm.cyclos.entities.accounts.AccountType;
import nl.strohalm.cyclos.entities.accounts.transactions.Invoice;
import nl.strohalm.cyclos.entities.accounts.transactions.Payment;
import nl.strohalm.cyclos.entities.accounts.transactions.ScheduledPayment;
import nl.strohalm.cyclos.entities.accounts.transactions.Transfer;
import nl.strohalm.cyclos.entities.accounts.transactions.TransferType;
import nl.strohalm.cyclos.entities.customization.fields.PaymentCustomField;
import nl.strohalm.cyclos.entities.groups.MemberGroup;
import nl.strohalm.cyclos.entities.members.Administrator;
import nl.strohalm.cyclos.entities.members.Element;
import nl.strohalm.cyclos.entities.members.Member;
import nl.strohalm.cyclos.entities.members.Operator;
import nl.strohalm.cyclos.services.customization.PaymentCustomFieldService;
import nl.strohalm.cyclos.services.elements.ElementService;
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
	private ElementService elementService;

	private CustomFieldHelper customFieldHelper;

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

	}

	public static class InvoiceDetailsResponseDto {
		private Invoice invoice;
		private Member member;
		private String pattern;
		private List<TransferType> possibleTransferTypes;
		private boolean toMe;
		private boolean fromMe;
		private boolean canAccept;
		private boolean canDeny;
		private boolean canCancel;
		private boolean showDestinationAccountType;
		private boolean showPerformedBy;
		private boolean showSentBy;
		private String message;
		private Long transferId;
		private Long paymentId;
		private Collection<Entry> entries;

		public InvoiceDetailsResponseDto(Invoice invoice, Member member,
				String pattern, List<TransferType> possibleTransferTypes,
				boolean toMe, boolean fromMe, boolean canAccept,
				boolean canDeny, boolean canCancel,
				boolean showDestinationAccountType, boolean showPerformedBy,
				boolean showSentBy, String message, Long transferId,
				Long paymentId, Collection<Entry> entries) {
			super();
			this.invoice = invoice;
			this.member = member;
			this.pattern = pattern;
			this.possibleTransferTypes = possibleTransferTypes;
			this.toMe = toMe;
			this.fromMe = fromMe;
			this.canAccept = canAccept;
			this.canDeny = canDeny;
			this.canCancel = canCancel;
			this.showDestinationAccountType = showDestinationAccountType;
			this.showPerformedBy = showPerformedBy;
			this.showSentBy = showSentBy;
			this.message = message;
			this.transferId = transferId;
			this.paymentId = paymentId;
			this.entries = entries;
		}

		public InvoiceDetailsResponseDto(Invoice invoice, Member member,
				String pattern, List<TransferType> possibleTransferTypes,
				boolean toMe, boolean fromMe, boolean canAccept,
				boolean canDeny, boolean canCancel,
				boolean showDestinationAccountType, boolean showPerformedBy,
				boolean showSentBy, String message, Long transferId,
				Long paymentId) {
			super();
			this.invoice = invoice;
			this.member = member;
			this.pattern = pattern;
			this.possibleTransferTypes = possibleTransferTypes;
			this.toMe = toMe;
			this.fromMe = fromMe;
			this.canAccept = canAccept;
			this.canDeny = canDeny;
			this.canCancel = canCancel;
			this.showDestinationAccountType = showDestinationAccountType;
			this.showPerformedBy = showPerformedBy;
			this.showSentBy = showSentBy;
			this.message = message;
			this.transferId = transferId;
			this.paymentId = paymentId;
		}

		public Long getTransferId() {
			return transferId;
		}

		public void setTransferId(Long transferId) {
			this.transferId = transferId;
		}

		public Long getPaymentId() {
			return paymentId;
		}

		public void setPaymentId(Long paymentId) {
			this.paymentId = paymentId;
		}

		public String getMessage() {
			return message;
		}

		public void setMessage(String message) {
			this.message = message;
		}

		public Invoice getInvoice() {
			return invoice;
		}

		public void setInvoice(Invoice invoice) {
			this.invoice = invoice;
		}

		public Member getMember() {
			return member;
		}

		public void setMember(Member member) {
			this.member = member;
		}

		public String getPattern() {
			return pattern;
		}

		public void setPattern(String pattern) {
			this.pattern = pattern;
		}

		public List<TransferType> getPossibleTransferTypes() {
			return possibleTransferTypes;
		}

		public void setPossibleTransferTypes(
				List<TransferType> possibleTransferTypes) {
			this.possibleTransferTypes = possibleTransferTypes;
		}

		public boolean isToMe() {
			return toMe;
		}

		public void setToMe(boolean toMe) {
			this.toMe = toMe;
		}

		public boolean isFromMe() {
			return fromMe;
		}

		public void setFromMe(boolean fromMe) {
			this.fromMe = fromMe;
		}

		public boolean isCanAccept() {
			return canAccept;
		}

		public void setCanAccept(boolean canAccept) {
			this.canAccept = canAccept;
		}

		public boolean isCanDeny() {
			return canDeny;
		}

		public void setCanDeny(boolean canDeny) {
			this.canDeny = canDeny;
		}

		public boolean isCanCancel() {
			return canCancel;
		}

		public void setCanCancel(boolean canCancel) {
			this.canCancel = canCancel;
		}

		public boolean isShowDestinationAccountType() {
			return showDestinationAccountType;
		}

		public void setShowDestinationAccountType(
				boolean showDestinationAccountType) {
			this.showDestinationAccountType = showDestinationAccountType;
		}

		public boolean isShowPerformedBy() {
			return showPerformedBy;
		}

		public void setShowPerformedBy(boolean showPerformedBy) {
			this.showPerformedBy = showPerformedBy;
		}

		public boolean isShowSentBy() {
			return showSentBy;
		}

		public void setShowSentBy(boolean showSentBy) {
			this.showSentBy = showSentBy;
		}

	}

	@RequestMapping(value = "/admin/managePasswords", method = RequestMethod.POST)
	@ResponseBody
	protected InvoiceDetailsResponseDto executeAction(
			@RequestBody InvoiceDetailsRequestDto form) throws Exception {
		// final HttpServletRequest request = context.getRequest();
		// final InvoiceDetailsForm form = context.getForm();
		final long id = form.getInvoiceId();
		if (id <= 0) {
			throw new ValidationException();
		}
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
			final Collection<Entry> entries = customFieldHelper.buildEntries(
					customFields, invoice.getCustomValues());
		}

		final TransferType transferType = invoice.getTransferType();
		AccountType accountType = invoice.getDestinationAccountType();
		if (accountType == null && transferType != null) {
			accountType = transferType.getTo();
		}

		final boolean toMe = form.getAccountOwner().equals(invoice.getTo());
		final boolean fromMe = context.getAccountOwner().equals(
				invoice.getFrom());

		// Only show the destination account type when not logged as the invoice
		// to and the invoice sender has more than one accounts
		boolean showDestinationAccountType = false;
		if (!context.getElement().equals(invoice.getToMember())
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
		if (payment instanceof Transfer) {
			Long transferId = payment.getId();
		} else if (payment instanceof ScheduledPayment) {
			Long paymentId = payment.getId();
		}
		final boolean canAccept = invoiceService.canAccept(invoice);
		final boolean canDeny = invoiceService.canDeny(invoice);
		final boolean canCancel = invoiceService.canCancel(invoice);
		InvoiceDetailsResponseDto response = new InvoiceDetailsResponseDto(
				invoice, member, pattern, possibleTransferTypes, toMe, fromMe,
				canAccept, canDeny, canCancel, showDestinationAccountType,
				showPerformedBy, showSentBy, message, transferId, paymentId);
		return response;
	}

	/**
	 * Returns whether the 'performed by' should be shown to the user
	 */
	private boolean shouldShowPerformedBy(
			final InvoiceDetailsRequestDto context, final Invoice invoice) {
		boolean showPerformedBy = false;
		final HttpServletRequest request = context.getRequest();
		final Element performedBy = invoice.getPerformedBy();
		if (performedBy != null) {
			if (performedBy instanceof Administrator) {
				if (context.isAdmin()) {
					request.setAttribute("performedByAdmin", performedBy);
					showPerformedBy = true;
				} else {
					// Don't disclose to member which admin performed the action
					request.setAttribute("performedBySystem", true);
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
						&& (context.isMemberOf((Operator) performedBy) || context
								.isOperator())) {
					request.setAttribute("performedByOperator", performedBy);
					showPerformedBy = true;
				} else {
					request.setAttribute("performedByMember",
							performedBy.getAccountOwner());
					showPerformedBy = true;
				}
			}
		}
		return showPerformedBy;
	}

	/**
	 * Returns whether the 'sent by' should be shown to the user
	 */
	private boolean shouldShowSentBy(final InvoiceDetailsRequestDto context,
			final Invoice invoice) {
		boolean showSentBy = false;
		final HttpServletRequest request = context.getRequest();
		final Element sentBy = invoice.getSentBy();
		if (sentBy != null) {
			if (sentBy instanceof Administrator) {
				if (context.isAdmin()) {
					request.setAttribute("sentByAdmin", sentBy);
					showSentBy = true;
				} else {
					// Don't disclose to member which admin sent the invoice
					request.setAttribute("sentBySystem", true);
					showSentBy = !invoice.isFromSystem(); // Only show sent by
															// if not a regular
															// system -> member
															// invoice,
				}
			} else if ((sentBy instanceof Operator)
					&& (context.isMemberOf((Operator) sentBy) || context
							.isOperator())) {
				request.setAttribute("sentByOperator", sentBy);
				showSentBy = true;
			} else if (!invoice.getFromMember().equals(sentBy)) {
				final Member member = (Member) sentBy.getAccountOwner();
				request.setAttribute("sentByMember", member);
				showSentBy = !invoice.getFrom().equals(member);
			}
		}
		return showSentBy;
	}
}
