package nl.strohalm.cyclos.webservices.rest.access.transactionpassword;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import nl.strohalm.cyclos.exceptions.PermissionDeniedException;
import nl.strohalm.cyclos.services.access.AccessService;
import nl.strohalm.cyclos.utils.JSONBuilder;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;

@Controller
public class GenerateTransactionPasswordController extends BaseRestController {
	/*
	 * @Override protected ContentType contentType() { return ContentType.JSON;
	 * }
	 */
	private AccessService accessService;

	public static class GenerateTransactionPasswordRequestDTO {

	}

	public static class GenerateTransactionPasswordResponseDTO {
		String transactionPassword;
		String errorKey;

		public final void setTransactionPassword(String transactionPassword) {
			this.transactionPassword = transactionPassword;
		}

		public final void setErrorKey(String errorKey) {
			this.errorKey = errorKey;
		}

	}

	@RequestMapping(value = "admin/generateTransactionPassword", method = RequestMethod.GET, produces = "json/application")
	@ResponseBody
	protected GenerateTransactionPasswordResponseDTO renderContent(
			@RequestBody GenerateTransactionPasswordRequestDTO form) throws Exception {
		String transactionPassword = null;
		String errorKey = null;
		GenerateTransactionPasswordResponseDTO response = new GenerateTransactionPasswordResponseDTO();
		try {
			transactionPassword = accessService.generateTransactionPassword();
		} catch (final PermissionDeniedException e) {
			errorKey = "transactionPassword.error.permissionDenied";
		} catch (final Exception e) {
			errorKey = "transactionPassword.error.generating";
		}

		final JSONBuilder json = new JSONBuilder();
		json.set("status", transactionPassword != null);
		if (transactionPassword != null) {
			// json.set("transactionPassword", transactionPassword);
			response.setTransactionPassword(transactionPassword);
			return response;
		} else {
			// json.set("errorMessage", context.message(errorKey));
			response.setErrorKey(errorKey);
			return response;
		}
		// responseHelper.writeJSON(context.getResponse(), json);
		
	}
}
