/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.strohalm.cyclos.webservices.rest.members.agreements;

import java.util.List;
import nl.strohalm.cyclos.access.AdminSystemPermission;
import nl.strohalm.cyclos.entities.members.RegistrationAgreement;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;
import nl.strohalm.cyclos.webservices.rest.GenericResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 *
 * @author Lue Infoservices
 */
@Controller
public class ListRegistrationAgreementsController extends BaseRestController {

    public static class RegistrationAgreementsResponse extends GenericResponse {

        List<RegistrationAgreement> registrationAgreements;
        private boolean editable;

        public boolean isEditable() {
            return editable;
        }

        public void setEditable(boolean editable) {
            this.editable = editable;
        }

        public List<RegistrationAgreement> getRegistrationAgreements() {
            return registrationAgreements;
        }

        public void setRegistrationAgreements(List<RegistrationAgreement> registrationAgreements) {
            this.registrationAgreements = registrationAgreements;
        }

    }

    @RequestMapping(value = "admin/listRegistrationAgreements", method = RequestMethod.GET)
    @ResponseBody
    public RegistrationAgreementsResponse prepareForm() {
        RegistrationAgreementsResponse response = new RegistrationAgreementsResponse();
        final List<RegistrationAgreement> registrationAgreements = registrationAgreementService.listAll();
        response.setRegistrationAgreements(registrationAgreements);
        response.setEditable(permissionService.hasPermission(AdminSystemPermission.REGISTRATION_AGREEMENTS_MANAGE));
        response.setStatus(0);
        response.setMessage("");
        return response;
    }
}
