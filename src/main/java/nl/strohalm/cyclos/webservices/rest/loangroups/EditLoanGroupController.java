package nl.strohalm.cyclos.webservices.rest.loangroups;

import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import nl.strohalm.cyclos.access.AdminSystemPermission;
import nl.strohalm.cyclos.entities.accounts.loans.LoanGroup;
import nl.strohalm.cyclos.entities.customization.fields.CustomFieldValue;
import nl.strohalm.cyclos.entities.customization.fields.LoanGroupCustomField;
import nl.strohalm.cyclos.entities.customization.fields.LoanGroupCustomFieldValue;
import nl.strohalm.cyclos.entities.groups.AdminGroup;
import nl.strohalm.cyclos.services.groups.GroupService;
import nl.strohalm.cyclos.services.permissions.PermissionService;
import nl.strohalm.cyclos.utils.ActionHelper;
import nl.strohalm.cyclos.utils.binding.BeanBinder;
import nl.strohalm.cyclos.utils.binding.BeanCollectionBinder;
import nl.strohalm.cyclos.utils.binding.DataBinder;
import nl.strohalm.cyclos.utils.binding.MapBean;
import nl.strohalm.cyclos.utils.binding.PropertyBinder;
import nl.strohalm.cyclos.utils.conversion.HtmlConverter;
import nl.strohalm.cyclos.utils.conversion.IdConverter;
import nl.strohalm.cyclos.utils.conversion.ReferenceConverter;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;
import nl.strohalm.cyclos.webservices.rest.GenericResponse;

@Controller
public class EditLoanGroupController extends BaseRestController {
    
    public static class LoanGroupParameters {
        
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
    
    @RequestMapping(value = "admin/editLoanGroup", method = RequestMethod.POST)
    @ResponseBody
    public GenericResponse handleDisplay(@RequestBody LoanGroupParameters params) throws Exception {
        GenericResponse response = new GenericResponse();
        final LoanGroup loanGroup = new LoanGroup();
        if (params.getId() != null && params.getId() > 0L) {
            loanGroup.setId(params.getId());
        }
        loanGroup.setName(params.getName());
        loanGroup.setDescription(params.getDescription());
        final boolean isInsert = loanGroup.getId() == null;
        loanGroupService.save(loanGroup);
        response.setMessage(isInsert ? "loanGroup.inserted" : "loanGroup.modified");
        response.setStatus(0);
        return response;
    }
    
}
