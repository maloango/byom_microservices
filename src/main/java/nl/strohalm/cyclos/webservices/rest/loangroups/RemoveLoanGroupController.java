package nl.strohalm.cyclos.webservices.rest.loangroups;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import nl.strohalm.cyclos.annotations.Inject;
import nl.strohalm.cyclos.services.loangroups.LoanGroupService;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;
@Controller
public class RemoveLoanGroupController extends BaseRestController{

	private LoanGroupService loanGroupService;

    public LoanGroupService getLoanGroupService() {
        return loanGroupService;
    }

    @Inject
    public void setLoanGroupService(final LoanGroupService loanGroupService) {
        this.loanGroupService = loanGroupService;
    }
    
    public static class RemoveLoanGroupRequestDTO{
    	private long              loanGroupId;

        public void RemoveLoanGroupForm() {
        }

        public long getLoanGroupId() {
            return loanGroupId;
        }

        public void setLoanGroupId(final long loanGroupId) {
            this.loanGroupId = loanGroupId;
        }
    	
    }
    public static class RemoveLoanGroupResponseDTO{
    	String message;

		public final String getMessage() {
			return message;
		}

		public final void setMessage(String message) {
			this.message = message;
		}
    	
    }
    
    @RequestMapping(value= "admin/removeLoanGroup", method =RequestMethod.DELETE)
    @ResponseBody
    protected RemoveLoanGroupResponseDTO executeAction(@RequestBody RemoveLoanGroupRequestDTO form) throws Exception {
    	//String message = null;
        try {
            loanGroupService.remove(form.getLoanGroupId());
          
        } catch (final Exception e) {
        	
        	RemoveLoanGroupResponseDTO response = new RemoveLoanGroupResponseDTO();
			
            response.setMessage("loanGroup.errorRemoving");
            response.setMessage("loanGroup.errorRemoving");
            return response;
        }
        //return context.getSuccessForward();
		return null;
    }

}
