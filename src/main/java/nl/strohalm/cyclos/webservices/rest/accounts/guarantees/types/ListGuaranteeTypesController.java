package nl.strohalm.cyclos.webservices.rest.accounts.guarantees.types;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import nl.strohalm.cyclos.annotations.Inject;
import nl.strohalm.cyclos.entities.accounts.guarantees.GuaranteeType;
import nl.strohalm.cyclos.entities.accounts.guarantees.GuaranteeTypeQuery;
import nl.strohalm.cyclos.services.accounts.guarantees.GuaranteeTypeService;
import nl.strohalm.cyclos.services.permissions.PermissionService;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class ListGuaranteeTypesController extends BaseRestController {
	private GuaranteeTypeService guaranteeTypeService;
	public final PermissionService getPermissionService() {
		return permissionService;
	}

	public final void setPermissionService(PermissionService permissionService) {
		this.permissionService = permissionService;
	}

	public final GuaranteeTypeService getGuaranteeTypeService() {
		return guaranteeTypeService;
	}

	private PermissionService permissionService;

	public static class ListGuaranteeTypesRequestDto {
            private long GuaranteeTypesID;

        public long getGuaranteeTypesID() {
            return GuaranteeTypesID;
        }

        public void setGuaranteeTypesID(long GuaranteeTypesID) {
            this.GuaranteeTypesID = GuaranteeTypesID;
        }
            

	}

	public static class ListGuaranteeTypesResponseDto {
		List<GuaranteeType> listGuaranteeTypes;

        public List<GuaranteeType> getListGuaranteeTypes() {
            return listGuaranteeTypes;
        }

        public void setListGuaranteeTypes(List<GuaranteeType> listGuaranteeTypes) {
            this.listGuaranteeTypes = listGuaranteeTypes;
        }
                

		public ListGuaranteeTypesResponseDto(
				List<GuaranteeType> listGuaranteeTypes) {
			super();
			this.listGuaranteeTypes = listGuaranteeTypes;
		}
                public ListGuaranteeTypesResponseDto(){
                }

	}

    /**
     *
     * @param GuaranteeTypesID
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "admin/listGuaranteeTypes{GuaranteeTypesID}", method = RequestMethod.GET)
	@ResponseBody
	public ListGuaranteeTypesResponseDto executeAction(@PathVariable ("GuaranteeTypesID")long GuaranteeTypesID) throws Exception {
			
            
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

