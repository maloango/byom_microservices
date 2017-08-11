package nl.strohalm.cyclos.webservices.rest.accounts.transfertypes;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import nl.strohalm.cyclos.annotations.Inject;
import nl.strohalm.cyclos.services.transfertypes.TransferTypeService;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class RemoveTransferTypeController extends BaseRestController {
	private TransferTypeService transferTypeService;

	public TransferTypeService getTransferTypeService() {
		return transferTypeService;
	}

	@Inject
	public void setTransferTypeService(
			final TransferTypeService transferTypeService) {
		this.transferTypeService = transferTypeService;
	}

	public static class RemoveTransferTypeRequestDto {
		private long accountTypeId;
		private long transferTypeId;
                
		public long getAccountTypeId() {
			return accountTypeId;
		}

		public long getTransferTypeId() {
			return transferTypeId;
		}

		public void setAccountTypeId(final long accountTypeId) {
			this.accountTypeId = accountTypeId;
		}

		public void setTransferTypeId(final long transferTypeId) {
			this.transferTypeId = transferTypeId;
		}
	}

	public static class RemoveTransferTypeResponseDto {
		public String message;
                
		public String getMessage() {
			return message;
		}

		public void setMessage(String message) {
			this.message = message;
		}
                public RemoveTransferTypeResponseDto(){
                    
                }

	}

	@RequestMapping(value = "admin/removeTransferType/{transferTypeId}", method = RequestMethod.GET)
	@ResponseBody
	protected RemoveTransferTypeResponseDto executeAction(@PathVariable ("transferTypeId")long transferTypeId) throws Exception 
			{
	
		RemoveTransferTypeResponseDto response = new RemoveTransferTypeResponseDto();
		try {
			transferTypeService.remove(transferTypeId);
			response.setMessage("transferType.removed");
		} catch (final Exception e) {
			response.setMessage("transferType.error.removing");
                        e.printStackTrace();
		}
		return response;
	}

}
