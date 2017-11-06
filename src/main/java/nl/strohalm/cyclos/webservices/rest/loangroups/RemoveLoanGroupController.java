package nl.strohalm.cyclos.webservices.rest.loangroups;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import nl.strohalm.cyclos.annotations.Inject;
import nl.strohalm.cyclos.services.loangroups.LoanGroupService;
import static nl.strohalm.cyclos.utils.access.LoggedUser.member;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;
import nl.strohalm.cyclos.webservices.rest.GenericResponse;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class RemoveLoanGroupController extends BaseRestController {
    
    @RequestMapping(value = "admin/removeLoanGroup/{loanGroupId}", method = RequestMethod.GET)
    @ResponseBody
    protected GenericResponse executeAction(@PathVariable("loanGroupId") long loanGroupId) throws Exception {
        GenericResponse response = new GenericResponse();
        try {
            loanGroupService.remove(loanGroupId);
            response.setMessage("loanGroup.removed");
        } catch (final Exception e) {
            response.setMessage("loanGroup.errorRemoving");
        }
        response.setStatus(0);
        return response;
        
    }
}
