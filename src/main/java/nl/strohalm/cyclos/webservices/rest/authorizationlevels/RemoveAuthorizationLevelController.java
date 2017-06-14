package nl.strohalm.cyclos.webservices.rest.authorizationlevels;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import nl.strohalm.cyclos.annotations.Inject;
import nl.strohalm.cyclos.services.transfertypes.AuthorizationLevelService;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;

@Controller
public class RemoveAuthorizationLevelController extends BaseRestController {
	private AuthorizationLevelService authorizationLevelService;

	@Inject
	public void setAuthorizationLevelService(
			final AuthorizationLevelService authorizationLevelService) {
		this.authorizationLevelService = authorizationLevelService;
	}

	public static class RemoveAuthorizationLevelRequestDto {
		private long authorizationLevelId;
		private long accountTypeId;
		private long transferTypeId;

		public long getAccountTypeId() {
			return accountTypeId;
		}

		public long getAuthorizationLevelId() {
			return authorizationLevelId;
		}

		public long getTransferTypeId() {
			return transferTypeId;
		}

		public void setAccountTypeId(final long accountTypeId) {
			this.accountTypeId = accountTypeId;
		}

		public void setAuthorizationLevelId(final long authorizationLevelId) {
			this.authorizationLevelId = authorizationLevelId;
		}

		public void setTransferTypeId(final long transferTypeId) {
			this.transferTypeId = transferTypeId;
		}

	}

	public static class RemoveAuthorizationLevelResponseDto {
		private String message;
		Map<String, Object> params;

		public RemoveAuthorizationLevelResponseDto(String message,
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
	}

	@RequestMapping(value = "/admin/removeAuthorizationLevel", method = RequestMethod.DELETE)
	@ResponseBody
	public RemoveAuthorizationLevelResponseDto removeAuthorization(
			@RequestBody RemoveAuthorizationLevelRequestDto form)
			throws Exception {

		// final RemoveAuthorizationLevelForm form = context.getForm();
		String message = null;
		try {
			authorizationLevelService.remove(form.getAuthorizationLevelId());
			message = "authorizationLevel.removed";
		} catch (final Exception e) {
			message = "authorizationLevel.error.removing";
		}
		final Map<String, Object> params = new HashMap<String, Object>();
		params.put("accountTypeId", form.getAccountTypeId());
		params.put("transferTypeId", form.getTransferTypeId());
		RemoveAuthorizationLevelResponseDto response = new RemoveAuthorizationLevelResponseDto(
				message, params);
		return response;
	}
}
