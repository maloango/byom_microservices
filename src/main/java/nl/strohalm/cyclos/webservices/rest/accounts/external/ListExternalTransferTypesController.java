package nl.strohalm.cyclos.webservices.rest.accounts.external;

import java.util.List;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import nl.strohalm.cyclos.access.AdminSystemPermission;
import nl.strohalm.cyclos.annotations.Inject;
import nl.strohalm.cyclos.entities.accounts.external.ExternalAccount;
import nl.strohalm.cyclos.entities.accounts.external.ExternalTransferType;
import nl.strohalm.cyclos.services.accounts.external.ExternalTransferTypeService;
import nl.strohalm.cyclos.services.permissions.PermissionService;

public class ListExternalTransferTypesController {
	// later will be the implementation if required..
	private ExternalTransferTypeService externalTransferTypeService;
	private PermissionService permissionService;

	@Inject
	public void setExternalTransferTypeService(
			final ExternalTransferTypeService externalTransferTypeService) {
		this.externalTransferTypeService = externalTransferTypeService;
	}

	public static class ListExternalTransferTypesRequestDto {
		public Object getAttribute(String name) {
			//kindly check with sir..
			return null;
		}
	}

	public static class ListExternalTransferTypesResponseDto {
		List<ExternalTransferType> externalTransferTypes;
		boolean editable;

		public ListExternalTransferTypesResponseDto(
				List<ExternalTransferType> externalTransferTypes,
				boolean editable) {
			super();
			this.externalTransferTypes = externalTransferTypes;
			this.editable = editable;
		}

	}

	@RequestMapping(value = "admin/listExternalTransferTypes", method = RequestMethod.GET)
	@ResponseBody
	protected ListExternalTransferTypesResponseDto executeAction(
			@RequestBody ListExternalTransferTypesRequestDto form)
			throws Exception {
		// final HttpServletRequest request = context.getRequest();
		final ExternalAccount externalAccount = (ExternalAccount) form
				.getAttribute("externalAccount");
		final List<ExternalTransferType> externalTransferTypes = externalTransferTypeService
				.listByAccount(externalAccount);
		boolean editable = permissionService
				.hasPermission(AdminSystemPermission.EXTERNAL_ACCOUNTS_MANAGE);
		ListExternalTransferTypesResponseDto response = new ListExternalTransferTypesResponseDto(
				externalTransferTypes, editable);
		return response;
	}
}
