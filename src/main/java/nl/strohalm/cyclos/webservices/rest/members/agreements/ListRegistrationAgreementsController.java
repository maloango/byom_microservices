package nl.strohalm.cyclos.webservices.rest.members.agreements;

import java.util.List;


import nl.strohalm.cyclos.access.AdminSystemPermission;
import nl.strohalm.cyclos.annotations.Inject;
import nl.strohalm.cyclos.entities.members.RegistrationAgreement;
import nl.strohalm.cyclos.services.elements.RegistrationAgreementService;
import nl.strohalm.cyclos.services.permissions.PermissionService;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class ListRegistrationAgreementsController extends BaseRestController {

	private RegistrationAgreementService registrationAgreementService;
	private PermissionService permissionService;

	@Inject
	public void setRegistrationAgreementService(
			final RegistrationAgreementService registrationAgreementService) {
		this.registrationAgreementService = registrationAgreementService;
	}

	public static class ListRegistrationAgreementsRequestDto {
             private String            name;
             private String            contents;

    public String getContents() {
        return contents;
    }

    public String getName() {
        return name;
    }

    public void setContents(final String contents) {
        this.contents = contents;
    }

    public void setName(final String name) {
        this.name = name;
    }

	}

	public static class ListRegistrationAgreementsResponseDto {
		private List<RegistrationAgreement> registrationAgreements;
		private boolean editableHasPermission;

		public List<RegistrationAgreement> getRegistrationAgreements() {
			return registrationAgreements;
		}

		public void setRegistrationAgreements(
				List<RegistrationAgreement> registrationAgreements) {
			this.registrationAgreements = registrationAgreements;
		}

		public boolean isEditableHasPermission() {
			return editableHasPermission;
		}

		public void setEditableHasPermission(boolean editableHasPermission) {
			this.editableHasPermission = editableHasPermission;
		}

		public ListRegistrationAgreementsResponseDto(
				List<RegistrationAgreement> registrationAgreements,
				boolean editableHasPermission) {
			super();
			this.registrationAgreements = registrationAgreements;
			this.editableHasPermission = editableHasPermission;
		}

	}

	@RequestMapping(value = "admin/listRegistrationAgreements", method = RequestMethod.GET)
	@ResponseBody
	protected ListRegistrationAgreementsResponseDto executeAction(
			@RequestBody ListRegistrationAgreementsRequestDto form)
			throws Exception {
		ListRegistrationAgreementsResponseDto response =null;
                try{
		final List<RegistrationAgreement> registrationAgreements = registrationAgreementService
				.listAll();
		boolean editableHasPermission = permissionService
				.hasPermission(AdminSystemPermission.REGISTRATION_AGREEMENTS_MANAGE);
		response = new ListRegistrationAgreementsResponseDto(
				registrationAgreements, editableHasPermission);}
                catch(Exception e){
                    e.printStackTrace();
                }
		return response;
	}
}
