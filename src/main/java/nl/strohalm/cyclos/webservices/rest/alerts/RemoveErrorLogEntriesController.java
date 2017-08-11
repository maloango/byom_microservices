package nl.strohalm.cyclos.webservices.rest.alerts;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import nl.strohalm.cyclos.annotations.Inject;
import nl.strohalm.cyclos.services.alerts.ErrorLogService;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;
import org.springframework.web.bind.annotation.PathVariable;
@Controller
public class RemoveErrorLogEntriesController extends BaseRestController{
	
	private ErrorLogService errorLogService;

    public final ErrorLogService getErrorLogService() {
		return errorLogService;
	}



	@Inject
    public void setErrorLogService(final ErrorLogService errorLogService) {
        this.errorLogService = errorLogService;
    }
    
    public static class RemoveErrorLogEntriesRequestDTO {
    	
    	private Long[]            entryIds;

        public Long[] getEntryIds() {
            return entryIds;
        }

        public void setEntryIds(final Long[] entryIds) {
            this.entryIds = entryIds;
        }

	}

	public static class RemoveErrorLogEntriesResponseDTO {

		public String message;
                private Long[]            entryIds;

        public Long[] getEntryIds() {
            return entryIds;
        }

        public void setEntryIds(Long[] entryIds) {
            this.entryIds = entryIds;
        }
                
		public final String getMessage() {
			return message;
		}

		public final void setMessage(String message) {
			this.message = message;
		}
		public RemoveErrorLogEntriesResponseDTO(){
                }
		
	}
    
    @RequestMapping(value = "admin/removeErrorLogEntries/{entryIds}", method=RequestMethod.GET)
    @ResponseBody
    protected RemoveErrorLogEntriesResponseDTO executeAction(@PathVariable ("entryIds")long entryIds ) throws Exception {
        RemoveErrorLogEntriesResponseDTO  response = new RemoveErrorLogEntriesResponseDTO();
        try{
           
        errorLogService.remove(response.getEntryIds());
        
        response.setMessage("errorLog.removed");
        }
        catch(Exception e){
            e.printStackTrace();
            
        }
        return response;
    }
	

}
