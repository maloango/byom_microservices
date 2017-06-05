package nl.strohalm.cyclos.webservices.rest.alerts;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import nl.strohalm.cyclos.annotations.Inject;
import nl.strohalm.cyclos.controls.restapi.BaseRestController;
import nl.strohalm.cyclos.services.alerts.ErrorLogService;
@Controller
public class RemoveErrorLogEntriesController extends BaseRestController{
	
	private ErrorLogService errorLogService;

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

		public static RemoveErrorLogEntriesRequestDTO getSuccessForward() {
			// TODO Auto-generated method stub
			return null;
		}

		public static void setMessage(String string) {
			// TODO Auto-generated method stub
			
		}
    	
	}

	public static class RemoveErrorLogEntriesResponseDTO {

		public Long getEntryIds() {
			// TODO Auto-generated method stub
			return null;
		}
		
		public String message;

		public final String getMessage() {
			return message;
		}

		public final void setMessage(String message) {
			this.message = message;
		}
		
		
	}
    
    

    @RequestMapping(value = "/admin/removeErrorLogEntries", method=RequestMethod.DELETE)
    @ResponseBody
    protected RemoveErrorLogEntriesRequestDTO executeAction(@RequestBody final RemoveErrorLogEntriesResponseDTO form ) throws Exception {
        //final RemoveErrorLogEntriesForm form = context.getForm();
        errorLogService.remove(form.getEntryIds());
        RemoveErrorLogEntriesResponseDTO response = new RemoveErrorLogEntriesResponseDTO();
        response.setMessage("errorLog.removed");
        return RemoveErrorLogEntriesRequestDTO.getSuccessForward();
    }
	

}
