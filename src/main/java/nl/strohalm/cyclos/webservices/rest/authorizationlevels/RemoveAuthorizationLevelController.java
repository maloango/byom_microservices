package nl.strohalm.cyclos.webservices.rest.authorizationlevels;

import java.util.HashMap;
import java.util.Map;

import nl.strohalm.cyclos.annotations.Inject;
import nl.strohalm.cyclos.controls.ActionContext;
import nl.strohalm.cyclos.controls.accounts.authorizationlevels.RemoveAuthorizationLevelForm;
import nl.strohalm.cyclos.services.transfertypes.AuthorizationLevelService;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class RemoveAuthorizationLevelController extends BaseRestController {
	private AuthorizationLevelService authorizationLevelService;

	@Inject
	public void setAuthorizationLevelService(
			final AuthorizationLevelService authorizationLevelService) {
		this.authorizationLevelService = authorizationLevelService;
	}

	@RequestMapping(value = "/admin/removeAuthorizationLevel", method = RequestMethod.DELETE)
	@ResponseBody
	public ResponseEntity<Void> removeAuthorization(final ActionContext context)
			throws Exception {

		final RemoveAuthorizationLevelForm form = context.getForm();
		try {
			authorizationLevelService.remove(form.getAuthorizationLevelId());
			context.sendMessage("authorizationLevel.removed");
		} catch (final Exception e) {
			context.sendMessage("authorizationLevel.error.removing");
		}
		final Map<String, Object> params = new HashMap<String, Object>();
		params.put("accountTypeId", form.getAccountTypeId());
		params.put("transferTypeId", form.getTransferTypeId());
		return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
	}
}
