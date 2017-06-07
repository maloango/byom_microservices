package nl.strohalm.cyclos.webservices.rest.payments;

import nl.strohalm.cyclos.annotations.Inject;
import nl.strohalm.cyclos.entities.Relationship;
import nl.strohalm.cyclos.entities.access.User;
import nl.strohalm.cyclos.entities.accounts.Account;
import nl.strohalm.cyclos.entities.accounts.MemberAccount;
import nl.strohalm.cyclos.entities.accounts.SystemAccount;
import nl.strohalm.cyclos.entities.accounts.transactions.Payment;
import nl.strohalm.cyclos.entities.accounts.transactions.Transfer;
import nl.strohalm.cyclos.entities.members.Member;
import nl.strohalm.cyclos.services.transactions.PaymentService;
import nl.strohalm.cyclos.utils.RelationshipHelper;
import nl.strohalm.cyclos.utils.conversion.CoercionHelper;
import nl.strohalm.cyclos.utils.validation.ValidationException;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class SuccessfulPaymentController extends BaseRestController {
	private static final Relationship[] FETCH = {
			RelationshipHelper.nested(Payment.Relationships.FROM,
					MemberAccount.Relationships.MEMBER),
			RelationshipHelper.nested(Payment.Relationships.TO,
					MemberAccount.Relationships.MEMBER),
			Payment.Relationships.TYPE };
	private PaymentService paymentService;

	@Inject
	public void setPaymentService(final PaymentService paymentService) {
		this.paymentService = paymentService;
	}

	public static class SuccessfulPaymentRequestDto {
		private long transferId;
		private int count;
		private String selectMember;
		private String from;

		public int getCount() {
			return count;
		}

		public String getFrom() {
			return from;
		}

		public String getSelectMember() {
			return selectMember;
		}

		public long getTransferId() {
			return transferId;
		}

		public void setCount(final int count) {
			this.count = count;
		}

		public void setFrom(final String from) {
			this.from = from;
		}

		public void setSelectMember(final String selectMember) {
			this.selectMember = selectMember;
		}

		public void setTransferId(final long transferId) {
			this.transferId = transferId;
		}

		User user;

		public Object getElement() {
			return user.getElement();
		}
	}

	public static class SuccessfulPaymentResponseDto {
		boolean toSystem;
		boolean selfPayment;
		boolean pendingAuthorization;
		boolean selectMember;
		int nextAttempt;
		Transfer transfer;
		Long relatedMemberId;
		Long fromMemberId;
		Long toMemberId;

		public SuccessfulPaymentResponseDto(boolean toSystem,
				boolean selfPayment, boolean pendingAuthorization,
				boolean selectMember, int nextAttempt, Transfer transfer,
				Long relatedMemberId, Long fromMemberId, Long toMemberId) {
			super();
			this.toSystem = toSystem;
			this.selfPayment = selfPayment;
			this.pendingAuthorization = pendingAuthorization;
			this.selectMember = selectMember;
			this.nextAttempt = nextAttempt;
			this.transfer = transfer;
			this.relatedMemberId = relatedMemberId;
			this.fromMemberId = fromMemberId;
			this.toMemberId = toMemberId;
		}

	}

	@RequestMapping(value = "/admin/managePasswords", method = RequestMethod.POST)
	@ResponseBody
	protected SuccessfulPaymentResponseDto executeAction(
			@RequestBody SuccessfulPaymentRequestDto form) throws Exception {
		// final SuccessfulPaymentForm form = context.getForm();
		final long transferId = form.getTransferId();
		if (transferId <= 0L) {
			throw new ValidationException();
		}
		// final HttpServletRequest request = context.getRequest();

		// Load the transfer. If it's pending payment wait a bit and try again
		final Transfer transfer = paymentService.load(transferId, FETCH);

		boolean toSystem = false;
		boolean selfPayment = false;
		boolean pendingAuthorization = false;
		boolean selectMember = false;
		final int nextAttempt = 0;

		final Account from = transfer.getFrom();
		Member fromMember = null;
		if (from instanceof MemberAccount) {
			fromMember = ((MemberAccount) from).getMember();
		}
		final Account to = transfer.getTo();
		Member toMember = null;
		if (to instanceof MemberAccount) {
			toMember = ((MemberAccount) to).getMember();
		}
		// Find the related member
		Member relatedMember = null;
		if (fromMember != null && !form.getElement().equals(fromMember)) {
			relatedMember = fromMember;
		} else if (toMember != null && !form.getElement().equals(toMember)) {
			relatedMember = toMember;
		}

		pendingAuthorization = transfer.getProcessDate() == null;
		selectMember = CoercionHelper.coerce(Boolean.TYPE,
				form.getSelectMember());
		toSystem = to instanceof SystemAccount;
		selfPayment = from.getOwner().equals(to.getOwner());

		// request.setAttribute("transfer", transfer);
		Long relatedMemberId = null;
		Long fromMemberId = null;
		Long toMemberId = null;
		if (relatedMember != null) {
			relatedMemberId = relatedMember.getId();
		}
		if (fromMember != null && !form.getElement().equals(fromMember)) {
			fromMemberId = fromMember.getId();
		}
		if (toMember != null) {
			toMemberId = toMember.getId();
		}
		SuccessfulPaymentResponseDto response = new SuccessfulPaymentResponseDto(
				toSystem, selfPayment, pendingAuthorization, selectMember,
				nextAttempt, transfer, relatedMemberId, fromMemberId,
				toMemberId);
		return response;
	}
}
