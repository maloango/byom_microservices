/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.strohalm.cyclos.webservices.rest.members.agreements;

import nl.strohalm.cyclos.entities.members.RegistrationAgreement;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;
import nl.strohalm.cyclos.webservices.rest.GenericResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 *
 * @author Lue Infoservices
 */
@Controller
public class EditRegistrationAgreementController extends BaseRestController {

    public static class EditRegistrationParamenters {

        private Long id;
        private String name;
        private String contents;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getContents() {
            return contents;
        }

        public void setContents(String contents) {
            this.contents = contents;
        }

    }

    @RequestMapping(value = "admin/editRegistrationAgreement", method = RequestMethod.POST)
    @ResponseBody
    public GenericResponse editRegistrationAgreement(@RequestBody EditRegistrationParamenters params) {
        GenericResponse response = new GenericResponse();
        final RegistrationAgreement registrationAgreement = new RegistrationAgreement();
        if(params.getId()!=null && params.getId()>0L)
        registrationAgreement.setId(params.getId());
        registrationAgreement.setName(params.getName());
        registrationAgreement.setContents(params.getContents());
        final boolean isInsert = registrationAgreement.isTransient();
        registrationAgreementService.save(registrationAgreement);
        if (isInsert) {
            response.setMessage("registrationAgreement.inserted");
        } else {
            response.setMessage("registrationAgreement.modified");
        }
        response.setStatus(0);
        return response;
    }

}
