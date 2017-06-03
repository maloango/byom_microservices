package nl.strohalm.cyclos.webservices.rest.guarantees.types;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import nl.strohalm.cyclos.access.AdminSystemPermission;
import nl.strohalm.cyclos.annotations.Inject;
import nl.strohalm.cyclos.controls.ActionContext;
import nl.strohalm.cyclos.entities.accounts.guarantees.GuaranteeType;
import nl.strohalm.cyclos.entities.accounts.guarantees.GuaranteeTypeQuery;
import nl.strohalm.cyclos.services.accounts.guarantees.GuaranteeTypeService;
import nl.strohalm.cyclos.services.permissions.PermissionService;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;

import org.apache.struts.action.ActionForward;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;

@Controller
public class ListGuaranteeTypesController extends BaseRestController {
	private GuaranteeTypeService guaranteeTypeService;
	private PermissionService permissionService;
	public static class ListGuaranteeTypesRequestDto {

	}

	public static class ListGuaranteeTypesResponseDto {

	}

	public List<GuaranteeType> executeAction(@RequestBody ListGuaranteeTypesRequestDto form)
			throws Exception {
		final GuaranteeTypeQuery guaranteeTypeQuery = new GuaranteeTypeQuery();
		final List<GuaranteeType> lstGuaranteeTypes = guaranteeTypeService
				.search(guaranteeTypeQuery);

		/*request.setAttribute("editable", permissionService
				.hasPermission(AdminSystemPermission.GUARANTEE_TYPES_MANAGE));
		request.setAttribute("listGuaranteeTypes", lstGuaranteeTypes);*/

		return lstGuaranteeTypes;
	}

	@Inject
	public void setGuaranteeTypeService(
			final GuaranteeTypeService guaranteeTypeService) {
		this.guaranteeTypeService = guaranteeTypeService;
	}
}
