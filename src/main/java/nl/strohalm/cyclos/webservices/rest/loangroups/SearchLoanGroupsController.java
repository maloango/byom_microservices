package nl.strohalm.cyclos.webservices.rest.loangroups;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import nl.strohalm.cyclos.annotations.Inject;
import nl.strohalm.cyclos.controls.ActionContext;
//import nl.strohalm.cyclos.controls.loangroups.SearchLoanGroupsForm;
import nl.strohalm.cyclos.entities.EntityReference;
import nl.strohalm.cyclos.entities.accounts.loans.LoanGroup;
import nl.strohalm.cyclos.entities.accounts.loans.LoanGroupQuery;
import nl.strohalm.cyclos.entities.customization.fields.CustomFieldValue;
import nl.strohalm.cyclos.entities.members.Element;
import nl.strohalm.cyclos.entities.members.Member;
import nl.strohalm.cyclos.services.elements.ElementService;
import nl.strohalm.cyclos.services.loangroups.LoanGroupService;
import nl.strohalm.cyclos.utils.access.LoggedUser;
import nl.strohalm.cyclos.utils.binding.BeanBinder;
import nl.strohalm.cyclos.utils.binding.DataBinder;
import nl.strohalm.cyclos.utils.binding.PropertyBinder;
import nl.strohalm.cyclos.utils.conversion.ReferenceConverter;
import nl.strohalm.cyclos.utils.query.QueryParameters;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;
import nl.strohalm.cyclos.webservices.rest.GenericResponse;

@Controller
public class SearchLoanGroupsController extends BaseRestController {

    public static class LoanGroupsResponse extends GenericResponse {

        private List<LoanGroupsEntity> loanGroups;

        public List<LoanGroupsEntity> getLoanGroups() {
            return loanGroups;
        }

        public void setLoanGroups(List<LoanGroupsEntity> loanGroups) {
            this.loanGroups = loanGroups;
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

    @RequestMapping(value = "admin/listLoanGroups", method = RequestMethod.GET)
    @ResponseBody
    protected LoanGroupsResponse listGroups() {
        LoanGroupsResponse response = new LoanGroupsResponse();
        final LoanGroupQuery query = new LoanGroupQuery();
        final List<LoanGroup> loanGroupsList = loanGroupService.search(query);
        List<LoanGroupsEntity> loanGroups = new ArrayList();
        for (LoanGroup group : loanGroupsList) {
            LoanGroupsEntity loanGroupsEntity = new LoanGroupsEntity();
            loanGroupsEntity.setId(group.getId());
            loanGroupsEntity.setName(group.getName());
            loanGroupsEntity.setDescription(group.getDescription());
            loanGroups.add(loanGroupsEntity);
        }
        response.setLoanGroups(loanGroups);
        response.setMessage("");
        response.setStatus(0);
        return response;
    }

    public static class LoanGroupParameteres {

        private String name;
        private String description;
        private Long member;

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

        public Long getMember() {
            return member;
        }

        public void setMember(Long member) {
            this.member = member;
        }

    }

    @RequestMapping(value = "admin/searchLoanGroups", method = RequestMethod.POST)
    @ResponseBody
    public LoanGroupsResponse searchLoanGroups(@RequestBody LoanGroupParameteres params) {
        LoanGroupsResponse response = new LoanGroupsResponse();
        final LoanGroupQuery query = new LoanGroupQuery();
        query.setName(params.getName());
        query.setDescription(params.getDescription());
        if(params.getMember()!=null && params.getMember()>0L)
        query.setMember((Member) elementService.load(params.getMember(), Element.Relationships.USER));
        final List<LoanGroup> loanGroupsList = loanGroupService.search(query);
        List<LoanGroupsEntity> loanGroups = new ArrayList();
        for (LoanGroup group : loanGroupsList) {
            LoanGroupsEntity loanGroupsEntity = new LoanGroupsEntity();
            loanGroupsEntity.setId(group.getId());
            loanGroupsEntity.setName(group.getName());
            loanGroupsEntity.setDescription(group.getDescription());
            loanGroups.add(loanGroupsEntity);
        }
        response.setLoanGroups(loanGroups);
        response.setMessage("");
        response.setStatus(0);
        return response;
    }

}
