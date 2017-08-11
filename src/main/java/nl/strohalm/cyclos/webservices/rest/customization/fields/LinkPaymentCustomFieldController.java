package nl.strohalm.cyclos.webservices.rest.customization.fields;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import nl.strohalm.cyclos.annotations.Inject;
import nl.strohalm.cyclos.entities.accounts.transactions.TransferType;
import nl.strohalm.cyclos.entities.customization.fields.PaymentCustomField;
import nl.strohalm.cyclos.services.customization.PaymentCustomFieldService;
import nl.strohalm.cyclos.utils.EntityHelper;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class LinkPaymentCustomFieldController extends BaseRestController {

	// later will be the implementation if required..

	private PaymentCustomFieldService paymentCustomFieldService;

	public final PaymentCustomFieldService getPaymentCustomFieldService() {
		return paymentCustomFieldService;
	}

	@Inject
	public void setPaymentCustomFieldService(
			final PaymentCustomFieldService paymentCustomFieldService) {
		this.paymentCustomFieldService = paymentCustomFieldService;
	}

	public static class LinkPaymentCustomFieldRequestDto {
		private long accountTypeId;
		private long transferTypeId;
		private long customFieldId;

		public long getAccountTypeId() {
			return accountTypeId;
		}

		public long getCustomFieldId() {
			return customFieldId;
		}

		public long getTransferTypeId() {
			return transferTypeId;
		}

		public void setAccountTypeId(final long accountTypeId) {
			this.accountTypeId = accountTypeId;
		}

		public void setCustomFieldId(final long customFieldId) {
			this.customFieldId = customFieldId;
		}

		public void setTransferTypeId(final long transferTypeId) {
			this.transferTypeId = transferTypeId;
		}
	}

	public static class LinkPaymentCustomFieldResponseDto {
		Map<String, Object> parameters;
                private long transferTypeId;
		private long customFieldId;
                private long accountTypeId;

        public long getAccountTypeId() {
            return accountTypeId;
        }

        public void setAccountTypeId(long accountTypeId) {
            this.accountTypeId = accountTypeId;
        }
        public Map<String, Object> getParameters() {
            return parameters;
        }

        public void setParameters(Map<String, Object> parameters) {
            this.parameters = parameters;
        }

        public long getTransferTypeId() {
            return transferTypeId;
        }

        public void setTransferTypeId(long transferTypeId) {
            this.transferTypeId = transferTypeId;
        }

        public long getCustomFieldId() {
            return customFieldId;
        }

        public void setCustomFieldId(long customFieldId) {
            this.customFieldId = customFieldId;
        }

		public LinkPaymentCustomFieldResponseDto(Map<String, Object> parameters) {
			super();
			this.parameters = parameters;
		}

	}

	@RequestMapping(value = "admin/linkPaymentCustomField/{customFieldId}", method = RequestMethod.GET)
	@ResponseBody
	protected LinkPaymentCustomFieldResponseDto executeAction(@PathVariable ("customFieldId") long customFieldId )
			throws Exception {
			
		LinkPaymentCustomFieldResponseDto response =null;
                try{
		final TransferType transferType = EntityHelper.reference(
				TransferType.class, response.getTransferTypeId());
		final PaymentCustomField customField = EntityHelper.reference(
				PaymentCustomField.class, response.getCustomFieldId());

		paymentCustomFieldService.link(transferType, customField);

		final Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("transferTypeId", response.getTransferTypeId());
		parameters.put("accountTypeId", response.getAccountTypeId());
		response = new LinkPaymentCustomFieldResponseDto(
				parameters);}
                catch(Exception e){
                    e.printStackTrace();
                }
		return response;
	}
}
