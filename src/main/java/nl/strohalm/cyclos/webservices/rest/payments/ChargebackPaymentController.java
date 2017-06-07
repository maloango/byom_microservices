package nl.strohalm.cyclos.webservices.rest.payments;

import org.apache.struts.action.ActionForward;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import nl.strohalm.cyclos.controls.ActionContext;
import nl.strohalm.cyclos.controls.accounts.details.ViewTransactionForm;
import nl.strohalm.cyclos.controls.payments.ChargebackPaymentForm;
import nl.strohalm.cyclos.entities.accounts.transactions.Payment;
import nl.strohalm.cyclos.entities.accounts.transactions.Transfer;
import nl.strohalm.cyclos.services.transactions.PaymentService;
import nl.strohalm.cyclos.services.transactions.exceptions.CreditsException;
import nl.strohalm.cyclos.utils.ActionHelper;
import nl.strohalm.cyclos.utils.validation.ValidationException;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;

@Controller
public class ChargebackPaymentController extends BaseRestController {
	protected PaymentService paymentService;

	public static class ChargebackPaymentRequestDto {
		private long transferId;
		private long memberId;
		private long typeId;
		private String transactionPassword;
		private boolean showActions;

		public long getMemberId() {
			return memberId;
		}

		public String getTransactionPassword() {
			return transactionPassword;
		}

		public long getTransferId() {
			return transferId;
		}

		public long getTypeId() {
			return typeId;
		}

		public boolean isShowActions() {
			return showActions;
		}

		public void setMemberId(final long memberId) {
			this.memberId = memberId;
		}

		public void setShowActions(final boolean showActions) {
			this.showActions = showActions;
		}

		public void setTransactionPassword(final String transactionPassword) {
			this.transactionPassword = transactionPassword;
		}

		public void setTransferId(final long transferId) {
			this.transferId = transferId;
		}

		public void setTypeId(final long accountId) {
			typeId = accountId;
		}

	}

	public static class ChargebackPaymentResponseDto {
		private String message;
		private Long transferId;
		private Long id;

		public ChargebackPaymentResponseDto(String message, Long transferId,
				Long id) {
			super();
			this.message = message;
			this.transferId = transferId;
			this.id = id;
		}

		public String getMessage() {
			return message;
		}

		public void setMessage(String message) {
			this.message = message;
		}
	}

	@RequestMapping(value = "", method = RequestMethod.POST)
	@ResponseBody
	protected ChargebackPaymentResponseDto handleSubmit(
			@RequestBody ChargebackPaymentRequestDto form) throws Exception {
		// final ChargebackPaymentForm form = context.getForm();
		final long transferId = form.getTransferId();
		String message = null;
		Transfer chargeback = null;
		Long id = null;
		ChargebackPaymentResponseDto response = new ChargebackPaymentResponseDto(
				message, transferId, id);
		if (transferId <= 0L) {
			throw new ValidationException();
		}
		final Transfer transfer = paymentService.load(transferId,
				Payment.Relationships.FROM);
		checkTransactionPassword(form, transfer);
		try {
			chargeback = paymentService.chargeback(transfer);
		} catch (final CreditsException e) {
			message = e.toString();
			return response;
		}
		message = "payment.chargedBack";
		id = chargeback.getId();
		return response;
	}

	protected void checkTransactionPassword(
			final ChargebackPaymentRequestDto form, final Transfer transfer) {

	}

}
