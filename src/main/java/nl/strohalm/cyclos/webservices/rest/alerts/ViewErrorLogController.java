package nl.strohalm.cyclos.webservices.rest.alerts;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import nl.strohalm.cyclos.annotations.Inject;
import nl.strohalm.cyclos.entities.access.User;
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

        private List<ErrorLogEntity>alerts;

        public List<ErrorLogEntity> getAlerts() {
            return alerts;
        }

        public void setAlerts(List<ErrorLogEntity> alerts) {
            this.alerts = alerts;
        }

    

    }

    public static class ErrorLogEntity {

        private Calendar date;
        private String path;
        private String stackTrace;
        private User loggedUser;
        private boolean removed;
        private Long id;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }
        
        public Calendar getDate() {
            return date;
        }

        public void setDate(Calendar date) {
            this.date = date;
        }

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }

        public String getStackTrace() {
            return stackTrace;
        }

        public void setStackTrace(String stackTrace) {
            this.stackTrace = stackTrace;
        }

        public User getLoggedUser() {
            return loggedUser;
        }

        public void setLoggedUser(User loggedUser) {
            this.loggedUser = loggedUser;
        }

        public boolean isRemoved() {
            return removed;
        }

        public void setRemoved(boolean removed) {
            this.removed = removed;
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
        final List<ErrorLogEntry> alertsList = errorLogService.search(query);
        List<ErrorLogEntity>alerts=new ArrayList();
        for(ErrorLogEntry errorLog:alertsList){
            ErrorLogEntity errorLogEntity=new ErrorLogEntity();
            errorLogEntity.setId(errorLog.getId());
            errorLogEntity.setDate(errorLog.getDate());
            errorLogEntity.setPath(errorLog.getPath());
            errorLogEntity.setRemoved(errorLog.isRemoved());
            errorLogEntity.setStackTrace(errorLog.getStackTrace());
            //errorLogEntity.setLoggedUser(errorLog.getLoggedUser());
            alerts.add(errorLogEntity);
        }
        response.setAlerts(alerts);
        response.setStatus(0);
        response.setMessage("Error log list");
        return response;

    }

}
