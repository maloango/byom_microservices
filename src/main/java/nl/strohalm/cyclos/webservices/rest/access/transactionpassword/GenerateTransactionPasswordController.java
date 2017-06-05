package nl.strohalm.cyclos.webservices.rest.access.transactionpassword;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import nl.strohalm.cyclos.controls.ActionContext;
import nl.strohalm.cyclos.controls.BaseAjaxAction.ContentType;
import nl.strohalm.cyclos.exceptions.PermissionDeniedException;
import nl.strohalm.cyclos.services.access.AccessService;
import nl.strohalm.cyclos.utils.JSONBuilder;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;

@Controller
public class GenerateTransactionPasswordController extends BaseRestController {
	private AccessService accessService;

	protected ContentType contentType() {
		return ContentType.JSON;
	}

	public static class GenerateTransactionPasswordRequestDTO {
	}

	public static class GenerateTransactionPasswordResponseDTO {
		String transactionPassword;
		String errorKey;
		public GenerateTransactionPasswordResponseDTO(String transactionPassword, String errorKey) {
			super();
			this.transactionPassword = transactionPassword;
			this.errorKey = errorKey;
		}
		
	}

	@RequestMapping(value = "path=/member/generateTransactionPassword", method = RequestMethod.GET, produces = "application/json")
	@ResponseBody
	protected GenerateTransactionPasswordResponseDTO renderContent(
			@RequestBody GenerateTransactionPasswordRequestDTO context) throws Exception {
		String transactionPassword = null;
		String errorKey = null;
		try {
			transactionPassword = accessService.generateTransactionPassword();
		} catch (final PermissionDeniedException e) {
			errorKey = "transactionPassword.error.permissionDenied";
		} catch (final Exception e) {
			errorKey = "transactionPassword.error.generating";
		}
		GenerateTransactionPasswordResponseDTO response = new GenerateTransactionPasswordResponseDTO(transactionPassword, errorKey);

		/*final JSONBuilder json = new JSONBuilder();
		json.set("status", transactionPassword != null);
		if (transactionPassword != null) {
			json.set("transactionPassword", transactionPassword);
		} else {
			json.set("errorMessage", context.message(errorKey));
		}*/
		return response;
		
	}
}
