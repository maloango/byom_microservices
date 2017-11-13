package nl.strohalm.cyclos.webservices.rest.settings;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import nl.strohalm.cyclos.controls.ActionContext;
//import nl.strohalm.cyclos.controls.settings.EditAlertSettingsForm;
import nl.strohalm.cyclos.entities.settings.AlertSettings;
import nl.strohalm.cyclos.services.settings.SettingsService;
import nl.strohalm.cyclos.utils.TimePeriod;
import nl.strohalm.cyclos.utils.binding.BeanBinder;
import nl.strohalm.cyclos.utils.binding.DataBinder;
import nl.strohalm.cyclos.utils.binding.DataBinderHelper;
import nl.strohalm.cyclos.utils.binding.PropertyBinder;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;
import nl.strohalm.cyclos.webservices.rest.GenericResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class EditAlertSettingsController extends BaseRestController {

    public static class AlertSettingsResponse extends GenericResponse {

        private AlertSettings alertSetting;
        private List<TimePeriod.Field> timePeriodFields;

        public List<TimePeriod.Field> getTimePeriodFields() {
            return timePeriodFields;
        }

        public void setTimePeriodFields(List<TimePeriod.Field> timePeriodFields) {
            this.timePeriodFields = timePeriodFields;
        }

        public AlertSettings getAlertSetting() {
            return alertSetting;
        }

        public void setAlertSetting(AlertSettings alertSetting) {
            this.alertSetting = alertSetting;
        }

    }

    @RequestMapping(value = "admin/editAlertSettings", method = RequestMethod.GET)
    @ResponseBody
    protected AlertSettingsResponse prepareForm() {
        AlertSettingsResponse response = new AlertSettingsResponse();
        AlertSettings alertSetting = settingsService.getAlertSettings();
        response.setAlertSetting(alertSetting);
        response.setTimePeriodFields(Arrays.asList(TimePeriod.Field.DAYS, TimePeriod.Field.WEEKS, TimePeriod.Field.MONTHS));
        response.setStatus(0);
        return response;
    }

    public static class AlertSettingParameters {

        private Integer givenVeryBadRefs;
        private Integer receivedVeryBadRefs;
        private Integer idleInvoiceExpiration_number;
        private String idleInvoiceExpiration_field;
        private Integer amountDeniedInvoices;
        private Integer amountIncorrectLogin;

        public Integer getGivenVeryBadRefs() {
            return givenVeryBadRefs;
        }

        public void setGivenVeryBadRefs(Integer givenVeryBadRefs) {
            this.givenVeryBadRefs = givenVeryBadRefs;
        }

        public Integer getReceivedVeryBadRefs() {
            return receivedVeryBadRefs;
        }

        public void setReceivedVeryBadRefs(Integer receivedVeryBadRefs) {
            this.receivedVeryBadRefs = receivedVeryBadRefs;
        }

        public Integer getIdleInvoiceExpiration_number() {
            return idleInvoiceExpiration_number;
        }

        public void setIdleInvoiceExpiration_number(Integer idleInvoiceExpiration_number) {
            this.idleInvoiceExpiration_number = idleInvoiceExpiration_number;
        }

        public String getIdleInvoiceExpiration_field() {
            return idleInvoiceExpiration_field;
        }

        public void setIdleInvoiceExpiration_field(String idleInvoiceExpiration_field) {
            this.idleInvoiceExpiration_field = idleInvoiceExpiration_field;
        }

        public Integer getAmountDeniedInvoices() {
            return amountDeniedInvoices;
        }

        public void setAmountDeniedInvoices(Integer amountDeniedInvoices) {
            this.amountDeniedInvoices = amountDeniedInvoices;
        }

        public Integer getAmountIncorrectLogin() {
            return amountIncorrectLogin;
        }

        public void setAmountIncorrectLogin(Integer amountIncorrectLogin) {
            this.amountIncorrectLogin = amountIncorrectLogin;
        }

    }

    @RequestMapping(value = "admin/editAlertSettings", method = RequestMethod.POST)
    @ResponseBody
    public GenericResponse edit(@RequestBody AlertSettingParameters params) {
        GenericResponse response = new GenericResponse();
        AlertSettings settings = new AlertSettings();
        settings.setAmountDeniedInvoices(params.getAmountDeniedInvoices());
        settings.setAmountIncorrectLogin(params.getAmountIncorrectLogin());
        settings.setGivenVeryBadRefs(params.getGivenVeryBadRefs());
        TimePeriod tp = new TimePeriod();
        tp.setNumber(params.getIdleInvoiceExpiration_number());
        tp.setField(TimePeriod.Field.valueOf(params.getIdleInvoiceExpiration_field()));
        settings.setIdleInvoiceExpiration(tp);
        settings.setReceivedVeryBadRefs(params.getReceivedVeryBadRefs());
        settings = settingsService.save(settings);
        response.setMessage("settings.alert.modified");
        response.setStatus(0);
        return response;

    }

}
