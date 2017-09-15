package nl.strohalm.cyclos.webservices.rest.settings;

import java.util.HashMap;
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
