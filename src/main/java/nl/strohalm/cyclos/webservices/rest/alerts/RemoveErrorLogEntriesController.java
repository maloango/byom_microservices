package nl.strohalm.cyclos.webservices.rest.alerts;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import nl.strohalm.cyclos.annotations.Inject;
import nl.strohalm.cyclos.services.alerts.ErrorLogService;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;
import nl.strohalm.cyclos.webservices.rest.GenericResponse;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
@Controller
public class RemoveErrorLogEntriesController extends BaseRestController{

    public static class ErrorLogParameters{
        private Long[] entryIds;

        public Long[] getEntryIds() {
            return entryIds;
        }

        public void setEntryIds(Long[] entryIds) {
            this.entryIds = entryIds;
        }
        
    }
    @RequestMapping(value = "admin/removeErrorLogEntries", method=RequestMethod.POST)
    @ResponseBody
    protected GenericResponse executeAction(@RequestBody ErrorLogParameters params) throws Exception {
    GenericResponse response=new GenericResponse();
     int i=errorLogService.remove(params.getEntryIds());
        response.setMessage(i+" errorLog.removed");
        response.setStatus(0);
        return response;
    }
	

}
