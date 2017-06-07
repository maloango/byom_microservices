package nl.strohalm.cyclos.webservices.rest.external;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import nl.strohalm.cyclos.annotations.Inject;
import nl.strohalm.cyclos.entities.accounts.external.ExternalAccount;
import nl.strohalm.cyclos.services.accounts.external.ExternalAccountService;
import nl.strohalm.cyclos.services.permissions.PermissionService;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;

@Controller
public class ListExternalAccountsController extends BaseRestController {
	private ExternalAccountService externalAccountService;

	public void setPermissionService(PermissionService permissionService) {
		this.permissionService = permissionService;
	}

	private PermissionService permissionService;

	@Inject
	public void setExternalAccountService(
			final ExternalAccountService externalAccountService) {
		this.externalAccountService = externalAccountService;
	}

	public static class ListExternalAccountsRequestDto {

	}

	public static class ListExternalAccountsResponseDto {

	}

	@RequestMapping(value = "", method = RequestMethod.GET)
	@ResponseBody
	protected List<ExternalAccount> executeAction(@RequestBody ListExternalAccountsRequestDto form)
			throws Exception {
		final List<ExternalAccount> externalAccounts = externalAccountService
				.search();
		/*form.setAttribute("externalAccounts", externalAccounts);
		form.setAttribute("editable", permissionService
				.hasPermission(AdminSystemPermission.EXTERNAL_ACCOUNTS_MANAGE));*/
		return externalAccounts;
	}
}
