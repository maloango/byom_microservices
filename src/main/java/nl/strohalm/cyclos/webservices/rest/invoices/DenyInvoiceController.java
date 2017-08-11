package nl.strohalm.cyclos.webservices.rest.invoices;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import nl.strohalm.cyclos.annotations.Inject;
import nl.strohalm.cyclos.entities.accounts.transactions.Invoice;
import nl.strohalm.cyclos.services.transactions.InvoiceService;
import nl.strohalm.cyclos.utils.EntityHelper;
import nl.strohalm.cyclos.utils.validation.ValidationException;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;

@Controller
public class DenyInvoiceController extends BaseRestController {
	private InvoiceService invoiceService;

	public InvoiceService getInvoiceService() {
		return invoiceService;
	}

	@Inject
	public void setInvoiceService(final InvoiceService invoiceService) {
		this.invoiceService = invoiceService;
	}

	public static class DenyInvoiceRequestDto {
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
	}

	public static class DenyInvoiceResponseDto {
		private String message;
		private Long invoiceId;

		public DenyInvoiceResponseDto(String message, Long invoiceId) {
			super();
			this.message = message;
			this.invoiceId = invoiceId;
		}

		public String getMessage() {
			return message;
		}

		public void setMessage(String message) {
			this.message = message;
		}
	}

	@RequestMapping(value = "member/denyInvoice", method = RequestMethod.GET)
	@ResponseBody
	protected DenyInvoiceResponseDto executeAction(
			@RequestBody DenyInvoiceRequestDto form) throws Exception {
		DenyInvoiceResponseDto response = null;
                try{
		String message = null;
		final long id = form.getInvoiceId();
		if (id <= 0) {
			throw new ValidationException();
		}
		Invoice invoice = EntityHelper.reference(Invoice.class, id);
		invoice = invoiceService.deny(invoice);
		message = "invoice.denied";
		Long invoiceId = invoice.getId();
		response = new DenyInvoiceResponseDto(message,
				invoiceId);}
                catch(Exception e){
                    e.printStackTrace();
                }
		return response;
	}
}
