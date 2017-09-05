package nl.strohalm.cyclos.webservices.rest.alerts;

import java.util.List;
import nl.strohalm.cyclos.annotations.Inject;
import nl.strohalm.cyclos.entities.alerts.Alert;
import nl.strohalm.cyclos.entities.alerts.ErrorLogEntry;
import nl.strohalm.cyclos.entities.alerts.ErrorLogEntryQuery;
import nl.strohalm.cyclos.services.alerts.ErrorLogService;
import nl.strohalm.cyclos.utils.conversion.CoercionHelper;
import nl.strohalm.cyclos.utils.query.PageParameters;
import org.springframework.stereotype.Controller;

import nl.strohalm.cyclos.webservices.rest.BaseRestController;
import nl.strohalm.cyclos.webservices.rest.GenericResponse;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class ViewErrorLogController extends BaseRestController {

    private ErrorLogService errorLogService;

    @Inject
    public ErrorLogService getErrorLogService() {
        return errorLogService;
    }

    @Inject
    public void setErrorLogService(ErrorLogService errorLogService) {
        this.errorLogService = errorLogService;
    }

    public static class ViewErrorLogResponse extends GenericResponse {

        private List<ErrorLogEntry> alerts;

        public List<ErrorLogEntry> getAlerts() {
            return alerts;
        }

        public void setAlerts(List<ErrorLogEntry> alerts) {
            this.alerts = alerts;
        }

    }

    @RequestMapping(value = "admin/viewErrorLogList", method = RequestMethod.GET)
    @ResponseBody
    public ViewErrorLogResponse listErrorLog() {
        ViewErrorLogResponse response = new ViewErrorLogResponse();

//        final SearchErrorLogForm form = context.getForm();
//        // Since there's only the current page and not showing removed, no data binder will be used
        final ErrorLogEntryQuery query = new ErrorLogEntryQuery();
        query.fetch(ErrorLogEntry.Relationships.LOGGED_USER, ErrorLogEntry.Relationships.PARAMETERS);
        query.setShowRemoved(false);
//        final int currentPage = CoercionHelper.coerce(Integer.TYPE, form.getQuery("currentPage"));
//        query.setPageParameters(new PageParameters(0, currentPage));
//        return query;
        final List<ErrorLogEntry> alerts = errorLogService.search(query);
        response.setAlerts(alerts);
        response.setStatus(0);
        response.setMessage("Error log list");
        return response;

    }

}
