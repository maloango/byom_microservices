package nl.strohalm.cyclos.webservices.rest.settings;

import java.util.HashMap;
import java.util.Map;

import nl.strohalm.cyclos.controls.ActionContext;
//import nl.strohalm.cyclos.controls.settings.EditMailTranslationForm;
import nl.strohalm.cyclos.entities.settings.MailTranslation;
import nl.strohalm.cyclos.services.settings.SettingsService;
import nl.strohalm.cyclos.utils.binding.BeanBinder;
import nl.strohalm.cyclos.utils.binding.DataBinder;
import nl.strohalm.cyclos.utils.binding.PropertyBinder;
import nl.strohalm.cyclos.utils.conversion.HtmlConverter;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;
import nl.strohalm.cyclos.webservices.rest.GenericResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class EditMailTranslationController extends BaseRestController {
    
    public static class MailTranslationParameters {
        
        private String invitationSubject;
        private String invitationMessage;
        private String activationSubject;
        private String activationMessageWithPassword;
        private String activationMessageWithoutPassword;
        private String mailValidationSubject;
        private String mailValidationMessage;
        private String resetPasswordSubject;
        private String resetPasswordMessage;
        
        public String getInvitationSubject() {
            return invitationSubject;
        }
        
        public void setInvitationSubject(String invitationSubject) {
            this.invitationSubject = invitationSubject;
        }
        
        public String getInvitationMessage() {
            return invitationMessage;
        }
        
        public void setInvitationMessage(String invitationMessage) {
            this.invitationMessage = invitationMessage;
        }
        
        public String getActivationSubject() {
            return activationSubject;
        }
        
        public void setActivationSubject(String activationSubject) {
            this.activationSubject = activationSubject;
        }
        
        public String getActivationMessageWithPassword() {
            return activationMessageWithPassword;
        }
        
        public void setActivationMessageWithPassword(String activationMessageWithPassword) {
            this.activationMessageWithPassword = activationMessageWithPassword;
        }
        
        public String getActivationMessageWithoutPassword() {
            return activationMessageWithoutPassword;
        }
        
        public void setActivationMessageWithoutPassword(String activationMessageWithoutPassword) {
            this.activationMessageWithoutPassword = activationMessageWithoutPassword;
        }
        
        public String getMailValidationSubject() {
            return mailValidationSubject;
        }
        
        public void setMailValidationSubject(String mailValidationSubject) {
            this.mailValidationSubject = mailValidationSubject;
        }
        
        public String getMailValidationMessage() {
            return mailValidationMessage;
        }
        
        public void setMailValidationMessage(String mailValidationMessage) {
            this.mailValidationMessage = mailValidationMessage;
        }
        
        public String getResetPasswordSubject() {
            return resetPasswordSubject;
        }
        
        public void setResetPasswordSubject(String resetPasswordSubject) {
            this.resetPasswordSubject = resetPasswordSubject;
        }
        
        public String getResetPasswordMessage() {
            return resetPasswordMessage;
        }
        
        public void setResetPasswordMessage(String resetPasswordMessage) {
            this.resetPasswordMessage = resetPasswordMessage;
        }
        
    }
    
    @RequestMapping(value = "admin/editMailTranslation", method = RequestMethod.POST)
    @ResponseBody
    protected GenericResponse formAction(@RequestBody MailTranslationParameters params) throws Exception {
        GenericResponse response = new GenericResponse();
        MailTranslation settings = new MailTranslation();
        settings.setActivationMessageWithPassword(params.getActivationMessageWithPassword());
        settings.setActivationMessageWithoutPassword(params.getActivationMessageWithoutPassword());
        settings.setActivationSubject(params.getActivationSubject());
        settings.setInvitationMessage(params.getInvitationMessage());
        settings.setInvitationSubject(params.getInvitationSubject());
        settings.setMailValidationSubject(params.getMailValidationSubject());
        settings.setMailValidationMessage(params.getMailValidationMessage());
        settings.setResetPasswordMessage(params.getResetPasswordMessage());
        settings.setResetPasswordSubject(params.getResetPasswordSubject());
        settings = settingsService.save(settings);
        if (settings != null) {
            response.setMessage("Mail translation modified!");
        }
        response.setStatus(0);
        
        return response;
    }
    
    public static class MailTranslationResponse extends GenericResponse {

        private MailTranslation mailTransactions;
        
        public MailTranslation getMailTransactions() {
            return mailTransactions;
        }
        
        public void setMailTransactions(MailTranslation mailTransactions) {
            this.mailTransactions = mailTransactions;
        }
        
    }
    
    @RequestMapping(value = "admin/editMailTranslation", method = RequestMethod.GET)
    @ResponseBody
    public MailTranslationResponse prepareForm() {
        MailTranslationResponse response = new MailTranslationResponse();
        MailTranslation settings = settingsService.getMailTranslation();
        response.setMailTransactions(settings);
        response.setStatus(0);
        return response;
    }
    
}
