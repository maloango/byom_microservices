package nl.strohalm.cyclos.webservices.rest.alerts;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import nl.strohalm.cyclos.annotations.Inject;
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

        List<ErrorLogEntry> errorHistory;

        public List<ErrorLogEntry> getErrorHistory() {
            return errorHistory;
        }

        public void setErrorHistory(List<ErrorLogEntry> errorHistory) {
            this.errorHistory = errorHistory;
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
        System.out.println("query------" + query);
        final List<ErrorLogEntry> errorHistory = errorLogService.search(query);
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
