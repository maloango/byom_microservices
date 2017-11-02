/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.strohalm.cyclos.webservices.rest.members.agreements;

import nl.strohalm.cyclos.webservices.rest.BaseRestController;
import nl.strohalm.cyclos.webservices.rest.GenericResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 *
 * @author Lue Infoservices
 */
@Controller
public class RemoveRegistrationAgreementController extends BaseRestController {

    @RequestMapping(value = "admin/removeRegistrationAgreement/{id}", method = RequestMethod.GET)
    @ResponseBody
    public GenericResponse removeAgreement(@PathVariable("id") Long id) {
        GenericResponse response = new GenericResponse();
        try {
            registrationAgreementService.remove(id);
            response.setMessage("registrationAgreement.removed");
        } catch (final Exception e) {
            response.setMessage("registrationAgreement.error.removing");
        }
        response.setStatus(0);
        return response;
    }
}
