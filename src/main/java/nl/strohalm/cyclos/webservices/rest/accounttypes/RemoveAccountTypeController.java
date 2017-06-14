package nl.strohalm.cyclos.webservices.rest.accounttypes;

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

	@Inject
	public void setAccountTypeService(
			final AccountTypeService accountTypeService) {
		this.accountTypeService = accountTypeService;
	}

	@RequestMapping(value = "/admin/removeAccountType", method = RequestMethod.DELETE)
	@ResponseBody
	protected RemoveAccountTypeResponseDto executeAction(
			@RequestBody RemoveAccountTypeRequestDto form) throws Exception {
		
		final long id = form.getAccountTypeId();
		if (id <= 0) {
			throw new ValidationException();
		}
		RemoveAccountTypeResponseDto respose = new RemoveAccountTypeResponseDto();
		try {
			accountTypeService.remove(id);
			// context.sendMessage("accountType.removed");
			respose.setMessage("accountType.removed");
		} catch (final Exception e) {

			// return context.sendError("accountType.error.removing");
			respose.setMessage("accountType.error.removing");
		}
		return respose;
	}

}
