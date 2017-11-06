/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.strohalm.cyclos.webservices.rest.loangroups;

import java.util.ArrayList;
import java.util.List;
import nl.strohalm.cyclos.entities.accounts.loans.LoanGroup;
import nl.strohalm.cyclos.entities.accounts.loans.LoanGroupQuery;
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
public class GetLoanGroupById extends BaseRestController {

    public static class LoanGroupsResponse extends GenericResponse {

        private LoanGroupsEntity loanGroup;

        public LoanGroupsEntity getLoanGroup() {
            return loanGroup;
        }

        public void setLoanGroup(LoanGroupsEntity loanGroup) {
            this.loanGroup = loanGroup;
        }

    }

    public static class LoanGroupsEntity {

        private Long id;
        private String name;
        private String description;

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

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }
    }

    @RequestMapping(value = "admin/getLoanGroupById/{id}", method = RequestMethod.GET)
    @ResponseBody
    public LoanGroupsResponse getLoanGroup(@PathVariable("id") Long id) {
        LoanGroupsResponse response = new LoanGroupsResponse();

        final LoanGroup loanGroup = loanGroupService.load(id, LoanGroup.Relationships.LOANS);
        LoanGroupsEntity loanGroupEntity = new LoanGroupsEntity();

        loanGroupEntity.setId(loanGroup.getId());
        loanGroupEntity.setName(loanGroup.getName());
        loanGroupEntity.setDescription(loanGroup.getDescription());

        response.setLoanGroup(loanGroupEntity);
        response.setMessage("");
        response.setStatus(0);
        return response;
    }

}
