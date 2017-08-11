package nl.strohalm.cyclos.webservices.rest.accounts.external;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import nl.strohalm.cyclos.access.AdminSystemPermission;
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

	public final ExternalAccountService getExternalAccountService() {
		return externalAccountService;
	}

	public final PermissionService getPermissionService() {
		return permissionService;
	}

	private PermissionService permissionService;

	@Inject
	public void setExternalAccountService(final ExternalAccountService externalAccountService) {
		this.externalAccountService = externalAccountService;
	}

	public static class ListExternalAccountsRequestDto {

	}

	public static class ListExternalAccountsResponseDto {
		List<ExternalAccount> externalAccounts;
		boolean editable;

		public ListExternalAccountsResponseDto(List<ExternalAccount> externalAccounts, boolean editable) {
			super();
			this.externalAccounts = externalAccounts;
			this.editable = editable;
		}

	}

	@RequestMapping(value = "admin/listExternalAccounts", method = RequestMethod.GET)
	@ResponseBody
	protected ListExternalAccountsResponseDto executeAction(@RequestBody ListExternalAccountsRequestDto form)
			throws Exception {
		final List<ExternalAccount> externalAccounts = externalAccountService.search();
		boolean editable = permissionService.hasPermission(AdminSystemPermission.EXTERNAL_ACCOUNTS_MANAGE);
		ListExternalAccountsResponseDto response = new ListExternalAccountsResponseDto(externalAccounts, editable);
		return response;
	}
}
