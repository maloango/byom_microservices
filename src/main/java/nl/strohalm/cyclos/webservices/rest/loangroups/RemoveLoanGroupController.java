package nl.strohalm.cyclos.webservices.rest.loangroups;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import nl.strohalm.cyclos.annotations.Inject;
import nl.strohalm.cyclos.services.loangroups.LoanGroupService;
import static nl.strohalm.cyclos.utils.access.LoggedUser.member;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;
import org.springframework.web.bind.annotation.PathVariable;
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
                public RemoveLoanGroupResponseDTO(){
                }
    	
    }
    
    @RequestMapping(value= "admin/removeLoanGroup/{loanGroupId}", method =RequestMethod.GET)
    @ResponseBody
    protected RemoveLoanGroupResponseDTO executeAction(@PathVariable ("loanGroupId") long loanGroupId) throws Exception {
    	RemoveLoanGroupResponseDTO response = new RemoveLoanGroupResponseDTO();
        
        try {
            loanGroupService.remove(loanGroupId);
            
                
            response.setMessage("loanGroup.removed");
        } 
        catch (final Exception e) 
        {
            response.setMessage("loanGroup.errorRemoving");
            e.printStackTrace();
           
        }
        
       
          return response;
		
    }
}

