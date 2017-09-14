package nl.strohalm.cyclos.webservices.rest.settings;

import java.util.HashMap;
import java.util.Map;

import nl.strohalm.cyclos.controls.ActionContext;
//import nl.strohalm.cyclos.controls.settings.EditMailSettingsForm;
import nl.strohalm.cyclos.entities.settings.MailSettings;
import nl.strohalm.cyclos.services.settings.SettingsService;
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
public class EditMailSettingsController extends BaseRestController {

    public static class EditMailSettingResponse extends GenericResponse {

        private String fromMail = "noreply@cyclos.org";
        private String smtpServer = "localhost";
        private int smtpPort = 25;
        private String smtpUsername;
        private String smtpPassword;
        private boolean smtpUseTLS = false;

        public String getFromMail() {
            return fromMail;
        }

        public void setFromMail(String fromMail) {
            this.fromMail = fromMail;
        }

        public String getSmtpServer() {
            return smtpServer;
        }

        public void setSmtpServer(String smtpServer) {
            this.smtpServer = smtpServer;
        }

        public int getSmtpPort() {
            return smtpPort;
        }

        public void setSmtpPort(int smtpPort) {
            this.smtpPort = smtpPort;
        }

        public String getSmtpUsername() {
            return smtpUsername;
        }

        public void setSmtpUsername(String smtpUsername) {
            this.smtpUsername = smtpUsername;
        }

        public String getSmtpPassword() {
            return smtpPassword;
        }

        public void setSmtpPassword(String smtpPassword) {
            this.smtpPassword = smtpPassword;
        }

        public boolean isSmtpUseTLS() {
            return smtpUseTLS;
        }

        public void setSmtpUseTLS(boolean smtpUseTLS) {
            this.smtpUseTLS = smtpUseTLS;
        }

    }

    public static class EditMailSettingsRequest extends MailSettings{

        private String fromMail;
        private String smtpServer;
        private int smtpPort;
        private String smtpUsername;
        private String smtpPassword;
        private boolean smtpUseTLS;

        public String getFromMail() {
            return fromMail;
        }

        public void setFromMail(String fromMail) {
            this.fromMail = fromMail;
        }

        public String getSmtpServer() {
            return smtpServer;
        }

        public void setSmtpServer(String smtpServer) {
            this.smtpServer = smtpServer;
        }

        public int getSmtpPort() {
            return smtpPort;
        }

        public void setSmtpPort(int smtpPort) {
            this.smtpPort = smtpPort;
        }

        public String getSmtpUsername() {
            return smtpUsername;
        }

        public void setSmtpUsername(String smtpUsername) {
            this.smtpUsername = smtpUsername;
        }

        public String getSmtpPassword() {
            return smtpPassword;
        }

        public void setSmtpPassword(String smtpPassword) {
            this.smtpPassword = smtpPassword;
        }

        public boolean isSmtpUseTLS() {
            return smtpUseTLS;
        }

        public void setSmtpUseTLS(boolean smtpUseTLS) {
            this.smtpUseTLS = smtpUseTLS;
        }
        
    }

    @RequestMapping(value = "admin/editMailSettings", method = RequestMethod.GET)
    @ResponseBody
    public EditMailSettingResponse prepareForm() throws Exception {
        EditMailSettingResponse response = new EditMailSettingResponse();
        response.setFromMail(settingsService.getMailSettings().getFromMail());
        response.setSmtpUsername(settingsService.getMailSettings().getSmtpUsername());
        response.setSmtpPassword(settingsService.getMailSettings().getSmtpPassword());

        response.setStatus(0);
        response.setMessage("Mail setting data");
        return response;
    }

    @RequestMapping(value = "admin/editMailSettings", method = RequestMethod.POST)
    @ResponseBody
    public GenericResponse formAction(@RequestBody EditMailSettingsRequest request) throws Exception {
      GenericResponse response=new GenericResponse();
        MailSettings settings =request;
        settings = settingsService.save(settings);
        response.setMessage("settings.mail.modified");
        response.setStatus(0);
        return response;
        
    }

}
