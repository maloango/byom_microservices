/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.strohalm.cyclos.webservices.rest.accounts.external;

import java.util.ArrayList;
import nl.strohalm.cyclos.entities.accounts.external.ExternalAccount;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;
import nl.strohalm.cyclos.webservices.rest.GenericResponse;
import nl.strohalm.cyclos.webservices.rest.accounts.external.ListExternalAccountsController.ExternalAccountEntity;
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
public class GetExternalAccountByIdController extends BaseRestController {

    public static class GetExternalAccountByIdResponse extends GenericResponse {

        private Long id;
        private String name;
        private String description;

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }
        

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

    }

    @RequestMapping(value = "admin/getExternalAccountById/{accountId}", method = RequestMethod.GET)
    @ResponseBody
    protected GetExternalAccountByIdResponse executeAction(@PathVariable("accountId") Long accountId)
            throws Exception {
        GetExternalAccountByIdResponse response = new GetExternalAccountByIdResponse();
        final ExternalAccount externalAccount = externalAccountService.load(accountId, ExternalAccount.Relationships.TYPES);
        response.setId(externalAccount.getId());
        response.setName(externalAccount.getName());
        response.setDescription(externalAccount.getDescription());
        return response;
    }

}
