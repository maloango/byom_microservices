package nl.strohalm.cyclos.webservices.rest.accounts.accounttypes;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import nl.strohalm.cyclos.annotations.Inject;
import nl.strohalm.cyclos.services.accounts.AccountTypeService;
import nl.strohalm.cyclos.utils.validation.ValidationException;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;

@Controller
public class RemoveAccountTypeController extends BaseRestController {
	private AccountTypeService accountTypeService;

	public static class RemoveAccountTypeRequestDto {
		private long accountTypeId;

		public long getAccountTypeId() {
			return accountTypeId;
		}

		public void setAccountTypeId(final long accountTypeId) {
			this.accountTypeId = accountTypeId;
		}
	}

	public static class RemoveAccountTypeResponseDto {
		public String message;

		public String getMessage() {
			return message;
		}

		public void setMessage(String message) {
			this.message = message;
		}

	}

	public AccountTypeService getAccountTypeService() {
		return accountTypeService;
	}
        public RemoveAccountTypeController(){
        }


	@Inject
	public void setAccountTypeService(
			final AccountTypeService accountTypeService) {
		this.accountTypeService = accountTypeService;
	}

	@RequestMapping(value = "admin/removeAccountType", method = RequestMethod.POST)
	@ResponseBody
	protected RemoveAccountTypeResponseDto executeAction(
			@RequestBody RemoveAccountTypeRequestDto form) throws Exception {
		RemoveAccountTypeResponseDto response = new RemoveAccountTypeResponseDto();
                try{
		final long id = form.getAccountTypeId();
		if (id <= 0) {
			throw new ValidationException();
		}
		
			accountTypeService.remove(id);
			try{
			response.setMessage("accountType.removed");
		} 
                catch (final Exception e) {
                    e.printStackTrace();
                }

			response.setMessage("accountType.error.removing");}
                catch(ValidationException e){
                    e.printStackTrace();
                }
		
		return response;
	}

}
