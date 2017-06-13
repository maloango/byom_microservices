package nl.strohalm.cyclos.webservices.rest.accounts.external;

import nl.strohalm.cyclos.annotations.Inject;
import nl.strohalm.cyclos.controls.accounts.external.RemoveExternalAccountForm;
import nl.strohalm.cyclos.exceptions.PermissionDeniedException;
import nl.strohalm.cyclos.services.accounts.external.ExternalAccountService;
import nl.strohalm.cyclos.utils.validation.ValidationException;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class RemoveExternalAccountController extends BaseRestController {
	private ExternalAccountService externalAccountService;

	@Inject
	public void setExternalAccountService(
			final ExternalAccountService externalAccountService) {
		this.externalAccountService = externalAccountService;
	}

	public static class RemoveExternalAccountRequestDto {
		private long externalAccountId;

		public long getExternalAccountId() {
			return externalAccountId;
		}

		public void setExternalAccountId(final long externalAccountId) {
			this.externalAccountId = externalAccountId;
		}
	}

	public static class RemoveExternalAccountResponseDto {
		public String message;

		public String getMessage() {
			return message;
		}

		public void setMessage(String message) {
			this.message = message;
		}
	}

	@RequestMapping(value = "", method = RequestMethod.DELETE)
	@ResponseBody
	protected RemoveExternalAccountResponseDto executeAction(
			@RequestBody RemoveExternalAccountRequestDto form) throws Exception {
		// final RemoveExternalAccountForm form = context.getForm();
		final long id = form.getExternalAccountId();
		if (id <= 0L) {
			throw new ValidationException();
		}
		RemoveExternalAccountResponseDto response = new RemoveExternalAccountResponseDto();
		try {
			externalAccountService.remove(id);
			response.setMessage("externalAccount.removed");
		} catch (final PermissionDeniedException e) {
			throw e;
		} catch (final Exception e) {
			response.setMessage("externalAccount.error.removing");
		}
		return response;
	}

}
