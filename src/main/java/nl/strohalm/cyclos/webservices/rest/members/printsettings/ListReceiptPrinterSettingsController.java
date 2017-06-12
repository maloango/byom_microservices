package nl.strohalm.cyclos.webservices.rest.members.printsettings;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import nl.strohalm.cyclos.annotations.Inject;
import nl.strohalm.cyclos.controls.restapi.BaseRestController;
import nl.strohalm.cyclos.entities.members.Member;
import nl.strohalm.cyclos.entities.members.printsettings.ReceiptPrinterSettings;
import nl.strohalm.cyclos.services.preferences.ReceiptPrinterSettingsService;
@Controller
public class ListReceiptPrinterSettingsController extends BaseRestController{
	private ReceiptPrinterSettingsService receiptPrinterSettingsService;

    @Inject
    public void setReceiptPrinterSettingsService(final ReceiptPrinterSettingsService receiptPrinterSettingsService) {
        this.receiptPrinterSettingsService = receiptPrinterSettingsService;
    }
    
    public static class ListReceiptPrinterSettingsRequestDTO{
    	private Member            member;
        private String            name;
        private String            printerName;
        private String            beginOfDocCommand;
        private String            endOfDocCommand;
        private String            paymentAdditionalMessage;

        public String getBeginOfDocCommand() {
            return beginOfDocCommand;
        }

        public String getEndOfDocCommand() {
            return endOfDocCommand;
        }

        public Member getMember() {
            return member;
        }

        //@Override
        public String getName() {
            return name;
        }

        public String getPaymentAdditionalMessage() {
            return paymentAdditionalMessage;
        }

        public String getPrinterName() {
            return printerName;
        }

        public void setBeginOfDocCommand(final String beginOfDocCommand) {
            this.beginOfDocCommand = beginOfDocCommand;
        }

        public void setEndOfDocCommand(final String endOfDocCommand) {
            this.endOfDocCommand = endOfDocCommand;
        }

        public void setMember(final Member member) {
            this.member = member;
        }

        public void setName(final String name) {
            this.name = name;
        }

        public void setPaymentAdditionalMessage(final String paymentAdditionalMessage) {
            this.paymentAdditionalMessage = paymentAdditionalMessage;
        }

        public void setPrinterName(final String printerName) {
            this.printerName = printerName;
        }

        @Override
        public String toString() {
            return getId() + " " + name + ", printer: " + printerName + ", member: " + member;
        }

		private String getId() {
			// TODO Auto-generated method stub
			return null;
		}

    }
    public static class ListReceiptPrinterSettingsResponseDTO{
    	String message;
    	public final String getMessage() {
			return message;
		}

		public final void setMessage(String message, List<ReceiptPrinterSettings> receiptPrinterSettings2) {
			this.message = message;
		}

		List<ReceiptPrinterSettings> receiptPrinterSettings;

		public final List<ReceiptPrinterSettings> getReceiptPrinterSettings() {
			return receiptPrinterSettings;
		}

		public final void setReceiptPrinterSettings(List<ReceiptPrinterSettings> receiptPrinterSettings) {
			this.receiptPrinterSettings = receiptPrinterSettings;
		}

		public ListReceiptPrinterSettingsResponseDTO(List<ReceiptPrinterSettings> receiptPrinterSettings) {
			super();
			this.receiptPrinterSettings = receiptPrinterSettings;
		}
    }

    @RequestMapping(value = "/member/listReceiptPrinterSettings",method =RequestMethod.GET)
    @ResponseBody
    protected ListReceiptPrinterSettingsResponseDTO executeAction(@RequestBody ListReceiptPrinterSettingsResponseDTO form) throws Exception {
        
        final List<ReceiptPrinterSettings> receiptPrinterSettings = receiptPrinterSettingsService.list();
        ListReceiptPrinterSettingsResponseDTO response = new ListReceiptPrinterSettingsResponseDTO(receiptPrinterSettings);
        response.setMessage("receiptPrinterSettings", receiptPrinterSettings);
        return response;
        
    }

}
