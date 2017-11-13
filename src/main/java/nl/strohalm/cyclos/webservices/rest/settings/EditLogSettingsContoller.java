package nl.strohalm.cyclos.webservices.rest.settings;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import nl.strohalm.cyclos.controls.ActionContext;
//import nl.strohalm.cyclos.controls.settings.EditLogSettingsForm;
import nl.strohalm.cyclos.entities.settings.LogSettings;
import nl.strohalm.cyclos.entities.settings.LogSettings.AccountFeeLevel;
import nl.strohalm.cyclos.entities.settings.LogSettings.ScheduledTaskLevel;
import nl.strohalm.cyclos.entities.settings.LogSettings.TraceLevel;
import nl.strohalm.cyclos.entities.settings.LogSettings.TransactionLevel;
import nl.strohalm.cyclos.entities.settings.LogSettings.WebServiceLevel;
import nl.strohalm.cyclos.services.settings.SettingsService;
import nl.strohalm.cyclos.utils.FileUnits;
import nl.strohalm.cyclos.utils.RequestHelper;
import nl.strohalm.cyclos.utils.binding.BeanBinder;
import nl.strohalm.cyclos.utils.binding.DataBinder;
import nl.strohalm.cyclos.utils.binding.PropertyBinder;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;
import nl.strohalm.cyclos.webservices.rest.GenericResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class EditLogSettingsContoller extends BaseRestController {

    public static class EditLogSettingsResponse extends GenericResponse {

        private LogSettings settings;
        private List<TraceLevel> traceLevels;
        private List<WebServiceLevel> webServiceLevels;
        private List<TransactionLevel> transactionLevels;
        private List<AccountFeeLevel> accountFeeLevels;
        private List<ScheduledTaskLevel> scheduledTaskLevels;
        private List<FileUnits> fileUnits = new ArrayList();

        public List<FileUnits> getFileUnits() {
            return fileUnits;
        }

        public void setFileUnits(List<FileUnits> fileUnits) {
            this.fileUnits = fileUnits;
        }

        public List<ScheduledTaskLevel> getScheduledTaskLevels() {
            return scheduledTaskLevels;
        }

        public void setScheduledTaskLevels(List<ScheduledTaskLevel> scheduledTaskLevels) {
            this.scheduledTaskLevels = scheduledTaskLevels;
        }

        public List<AccountFeeLevel> getAccountFeeLevels() {
            return accountFeeLevels;
        }

        public void setAccountFeeLevels(List<AccountFeeLevel> accountFeeLevels) {
            this.accountFeeLevels = accountFeeLevels;
        }

        public List<TransactionLevel> getTransactionLevels() {
            return transactionLevels;
        }

        public void setTransactionLevels(List<TransactionLevel> transactionLevels) {
            this.transactionLevels = transactionLevels;
        }

        public List<WebServiceLevel> getWebServiceLevels() {
            return webServiceLevels;
        }

        public void setWebServiceLevels(List<WebServiceLevel> webServiceLevels) {
            this.webServiceLevels = webServiceLevels;
        }

        public List<TraceLevel> getTraceLevels() {
            return traceLevels;
        }

        public void setTraceLevels(List<TraceLevel> traceLevels) {
            this.traceLevels = traceLevels;
        }

        public LogSettings getSettings() {
            return settings;
        }

        public void setSettings(LogSettings settings) {
            this.settings = settings;
        }

    }

    @RequestMapping(value = "admin/editLogSettings", method = RequestMethod.GET)
    @ResponseBody
    public EditLogSettingsResponse prepareForm() throws Exception {
        EditLogSettingsResponse response = new EditLogSettingsResponse();
        final LogSettings settings = settingsService.getLogSettings();
        response.setSettings(settings);

        //RequestHelper.storeEnum(request, TraceLevel.class, "traceLevels");
        List<TraceLevel> traceLevels = new ArrayList();
        traceLevels.add(TraceLevel.OFF);
        traceLevels.add(TraceLevel.SIMPLE);
        traceLevels.add(TraceLevel.DETAILED);
        traceLevels.add(TraceLevel.ERRORS);
        response.setTraceLevels(traceLevels);

        // RequestHelper.storeEnum(request, WebServiceLevel.class, "webServiceLevels");
        List<WebServiceLevel> webServiceLevels = new ArrayList();
        webServiceLevels.add(WebServiceLevel.OFF);
        webServiceLevels.add(WebServiceLevel.DETAILED);
        webServiceLevels.add(WebServiceLevel.ERRORS);
        webServiceLevels.add(WebServiceLevel.SIMPLE);
        response.setWebServiceLevels(webServiceLevels);

        //RequestHelper.storeEnum(request, TransactionLevel.class, "transactionLevels");
        List<TransactionLevel> transactionLevels = new ArrayList();
        transactionLevels.add(TransactionLevel.OFF);
        transactionLevels.add(TransactionLevel.DETAILED);
        transactionLevels.add(TransactionLevel.NORMAL);
        response.setTransactionLevels(transactionLevels);

        //  RequestHelper.storeEnum(request, AccountFeeLevel.class, "accountFeeLevels");
        List<AccountFeeLevel> accountFeeLevels = new ArrayList();
        accountFeeLevels.add(AccountFeeLevel.OFF);
        accountFeeLevels.add(AccountFeeLevel.DETAILED);
        accountFeeLevels.add(AccountFeeLevel.ERRORS);
        accountFeeLevels.add(AccountFeeLevel.STATUS);
        response.setAccountFeeLevels(accountFeeLevels);

        // RequestHelper.storeEnum(request, ScheduledTaskLevel.class, "scheduledTaskLevels");
        List<ScheduledTaskLevel> scheduledTaskLevels = new ArrayList();
        scheduledTaskLevels.add(ScheduledTaskLevel.OFF);
        scheduledTaskLevels.add(ScheduledTaskLevel.DETAILED);
        scheduledTaskLevels.add(ScheduledTaskLevel.ERRORS);
        scheduledTaskLevels.add(ScheduledTaskLevel.INFO);

        // RequestHelper.storeEnum(request, FileUnits.class, "fileUnits");
        List<FileUnits> fileUnits = new ArrayList();
        fileUnits.add(FileUnits.BYTES);
        fileUnits.add(FileUnits.KILO_BYTES);
        fileUnits.add(FileUnits.MEGA_BYTES);
        response.setFileUnits(fileUnits);
        response.setStatus(0);
        response.setMessage("Log settings");
        return response;
    }

    @RequestMapping(value = "admin/editLogSettings", method = RequestMethod.POST)
    @ResponseBody
    public GenericResponse formAction(@RequestBody LogSettings settings) throws Exception {
        GenericResponse response = new GenericResponse();
        settings = settingsService.save(settings);
        response.setMessage("settings.log.modified");
        response.setStatus(0);
        return response;
    }
}
