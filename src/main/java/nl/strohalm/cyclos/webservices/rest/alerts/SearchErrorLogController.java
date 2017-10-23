package nl.strohalm.cyclos.webservices.rest.alerts;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import nl.strohalm.cyclos.annotations.Inject;
import nl.strohalm.cyclos.entities.access.User;
import nl.strohalm.cyclos.entities.alerts.Alert;
import nl.strohalm.cyclos.entities.alerts.ErrorLogEntry;
import nl.strohalm.cyclos.entities.alerts.ErrorLogEntryQuery;
import nl.strohalm.cyclos.entities.settings.LocalSettings;
import static nl.strohalm.cyclos.http.AttributeHolder.Factory.context;
import nl.strohalm.cyclos.services.alerts.AlertService;
import nl.strohalm.cyclos.services.alerts.ErrorLogService;
import nl.strohalm.cyclos.services.settings.SettingsService;
import nl.strohalm.cyclos.utils.Period;
import nl.strohalm.cyclos.utils.query.QueryParameters;
import org.springframework.stereotype.Controller;

import nl.strohalm.cyclos.webservices.rest.BaseRestController;
import nl.strohalm.cyclos.webservices.rest.GenericResponse;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class SearchErrorLogController extends BaseRestController {

    private ErrorLogService errorLogService;
    private SettingsService settingsService;

    public ErrorLogService getErrorLogService() {
        return errorLogService;
    }

    @Inject
    public void setErrorLogService(ErrorLogService errorLogService) {
        this.errorLogService = errorLogService;
    }

    public SettingsService getSettingsService() {
        return settingsService;
    }

    @Inject
    public void setSettingsService(SettingsService settingsService) {
        this.settingsService = settingsService;
    }

    public static class SerachErrorLogRequest {

        private String begin;
        private String end;

        public String getBegin() {
            return begin;
        }

        public void setBegin(String begin) {
            this.begin = begin;
        }

        public String getEnd() {
            return end;
        }

        public void setEnd(String end) {
            this.end = end;
        }

    }

    public static class SearchErrorLogResponse extends GenericResponse {

        List<ErrorLogEntity> errorHistory;

        public List<ErrorLogEntity> getErrorHistory() {
            return errorHistory;
        }

        public void setErrorHistory(List<ErrorLogEntity> errorHistory) {
            this.errorHistory = errorHistory;
        }

    }

    public static class ErrorLogEntity {

        private Calendar date;
        private String path;
        private String stackTrace;
        private String loggedUserName;
        private boolean removed;
        private Long id;
        private Map<String, String> parameters;

        public Map<String, String> getParameters() {
            return parameters;
        }

        public void setParameters(Map<String, String> parameters) {
            this.parameters = parameters;
        }

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

        public String getLoggedUserName() {
            return loggedUserName;
        }

        public void setLoggedUserName(String loggedUserName) {
            this.loggedUserName = loggedUserName;
        }

        public boolean isRemoved() {
            return removed;
        }

        public void setRemoved(boolean removed) {
            this.removed = removed;
        }

    }

    @RequestMapping(value = "admin/searchErrorHistory", method = RequestMethod.POST)
    @ResponseBody
    public SearchErrorLogResponse searchErrorLog(@RequestBody SerachErrorLogRequest request) {
        SearchErrorLogResponse response = new SearchErrorLogResponse();
        final LocalSettings localSettings = settingsService.getLocalSettings();
        Period period = new Period();
        period.setBegin(localSettings.getDateConverter().valueOf(request.getBegin()));
        period.setEnd(localSettings.getDateConverter().valueOf(request.getEnd()));
        final ErrorLogEntryQuery query = new ErrorLogEntryQuery();
        query.setPeriod(period);
        query.setShowRemoved(true);
        query.setResultType(QueryParameters.ResultType.LIST);
        final List<ErrorLogEntry> errorList = errorLogService.search(query);
        List<ErrorLogEntity> errorHistory = new ArrayList();
        for (ErrorLogEntry log : errorList) {
            ErrorLogEntity errorLogEntity = new ErrorLogEntity();
            errorLogEntity.setId(log.getId());
            errorLogEntity.setDate(log.getDate());
            errorLogEntity.setLoggedUserName(log.getLoggedUser().getUsername());
            errorLogEntity.setPath(log.getPath());
            errorLogEntity.setStackTrace(log.getStackTrace());
            errorLogEntity.setRemoved(log.isRemoved());
            errorLogEntity.setParameters(log.getParameters());
            errorHistory.add(errorLogEntity);

        }

        System.out.println("alerts size------" + errorHistory.size());
        response.setErrorHistory(errorHistory);
        response.setStatus(0);
        response.setMessage("error history list!!");
        return response;

    }

//    public Calendar formatDate(String d) {
//        Calendar cal = Calendar.getInstance();
//        try {
//            DateFormat df = new SimpleDateFormat("dd/MM/yyyy");
//            cal.setTime(df.parse(d));
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return cal;
//    }
}
