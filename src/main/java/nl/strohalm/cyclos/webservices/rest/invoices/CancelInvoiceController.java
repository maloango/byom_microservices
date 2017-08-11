package nl.strohalm.cyclos.webservices.rest.invoices;

import java.util.HashMap;
import java.util.Map;

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
public class CancelInvoiceController extends BaseRestController {
	private InvoiceService invoiceService;

	public InvoiceService getInvoiceService() {
		return invoiceService;
	}

	@Inject
	public void setInvoiceService(final InvoiceService invoiceService) {
		this.invoiceService = invoiceService;
	}

	public static class CancelInvoiceRequestDto {
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

	public static class CancelInvoiceResponseDto {
		private String message;
		private Map<String, Object> params;

		public CancelInvoiceResponseDto(String message,
				Map<String, Object> params) {
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
                public CancelInvoiceResponseDto(){
                    
                }
	}

	@RequestMapping(value = "member/cancelInvoice", method = RequestMethod.POST)
	@ResponseBody
	protected CancelInvoiceResponseDto executeAction(
			@RequestBody CancelInvoiceRequestDto form) throws Exception {
		CancelInvoiceResponseDto response = null;
                try{
		String message = null;
		final long id = form.getInvoiceId();
		if (id <= 0) {
			throw new ValidationException();
		}
		Invoice invoice = EntityHelper.reference(Invoice.class, id);
		invoice = invoiceService.cancel(invoice);
		message = "invoice.cancelled";
		final Map<String, Object> params = new HashMap<String, Object>();
		response = new CancelInvoiceResponseDto(
				message, params);
		params.put("invoiceId", invoice.getId());
		params.put("accountFeeLogId", form.getAccountFeeLogId());}
                catch(Exception e){
                e.printStackTrace();
                }
		return response;
	}
}
