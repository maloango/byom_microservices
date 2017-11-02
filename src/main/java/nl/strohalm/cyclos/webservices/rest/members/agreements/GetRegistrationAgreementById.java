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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 *
 * @author Lue Infoservices
 */
@Controller
public class GetRegistrationAgreementById extends BaseRestController {
    
    public static class AgreementResponse extends GenericResponse {
        
        private AgreementEntity registrationAgreement;
        
        public AgreementEntity getRegistrationAgreement() {
            return registrationAgreement;
        }
        
        public void setRegistrationAgreement(AgreementEntity registrationAgreement) {
            this.registrationAgreement = registrationAgreement;
        }
        
    }
    
    public static class AgreementEntity {
        
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
    
    @RequestMapping(value = "admin/getRegistrationAgreementById/{id}", method = RequestMethod.GET)
    @ResponseBody
    public AgreementResponse getAgreement(@PathVariable("id") Long id) {
        AgreementResponse response = new AgreementResponse();
        RegistrationAgreement agreement = registrationAgreementService.load(id);
        AgreementEntity registrationAgreement = new AgreementEntity();
        registrationAgreement.setId(agreement.getId());
        registrationAgreement.setName(agreement.getName());
        registrationAgreement.setContents(agreement.getContents());
        
        response.setRegistrationAgreement(registrationAgreement);
        response.setStatus(0);
        response.setMessage("");
        return response;
        
    }
}
