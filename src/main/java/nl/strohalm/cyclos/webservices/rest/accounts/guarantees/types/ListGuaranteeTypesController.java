package nl.strohalm.cyclos.webservices.rest.accounts.guarantees.types;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;

import nl.strohalm.cyclos.annotations.Inject;
import nl.strohalm.cyclos.entities.accounts.guarantees.GuaranteeType;
import nl.strohalm.cyclos.entities.accounts.guarantees.GuaranteeTypeQuery;
import nl.strohalm.cyclos.services.accounts.guarantees.GuaranteeTypeService;
import nl.strohalm.cyclos.services.permissions.PermissionService;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;

@Controller
public class ListGuaranteeTypesController extends BaseRestController {
	private GuaranteeTypeService guaranteeTypeService;
	private PermissionService permissionService;

	public static class ListGuaranteeTypesRequestDto {

	}

	public static class ListGuaranteeTypesResponseDto {
		List<GuaranteeType> lstGuaranteeTypes;

		public ListGuaranteeTypesResponseDto(
				List<GuaranteeType> lstGuaranteeTypes) {
			super();
			this.lstGuaranteeTypes = lstGuaranteeTypes;
		}

	}

	public ListGuaranteeTypesResponseDto executeAction(
			@RequestBody ListGuaranteeTypesRequestDto form) throws Exception {
		final GuaranteeTypeQuery guaranteeTypeQuery = new GuaranteeTypeQuery();
		final List<GuaranteeType> lstGuaranteeTypes = guaranteeTypeService
				.search(guaranteeTypeQuery);
		ListGuaranteeTypesResponseDto response = new ListGuaranteeTypesResponseDto(
				lstGuaranteeTypes);
		return response;
	}

	@Inject
	public void setGuaranteeTypeService(
			final GuaranteeTypeService guaranteeTypeService) {
		this.guaranteeTypeService = guaranteeTypeService;
	}
}
