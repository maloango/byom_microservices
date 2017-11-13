package nl.strohalm.cyclos.webservices.rest.settings;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import nl.strohalm.cyclos.controls.ActionContext;
//import nl.strohalm.cyclos.controls.settings.EditAccessSettingsForm;
import nl.strohalm.cyclos.entities.settings.AccessSettings;
import nl.strohalm.cyclos.services.settings.SettingsService;
import nl.strohalm.cyclos.utils.RangeConstraint;
import nl.strohalm.cyclos.utils.RequestHelper;
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
public class EditAccessSettingsController extends BaseRestController {

    public static class AccessSettingsResponse extends GenericResponse {

        private AccessSettings accessSetting;
        private List< AccessSettings.UsernameGeneration> usernameGenerations;
        private List<TimePeriod.Field> timePeriodFields;

        public List<TimePeriod.Field> getTimePeriodFields() {
            return timePeriodFields;
        }

        public void setTimePeriodFields(List<TimePeriod.Field> timePeriodFields) {
            this.timePeriodFields = timePeriodFields;
        }

        public List<AccessSettings.UsernameGeneration> getUsernameGenerations() {
            return usernameGenerations;
        }

        public void setUsernameGenerations(List<AccessSettings.UsernameGeneration> usernameGenerations) {
            this.usernameGenerations = usernameGenerations;
        }

        public AccessSettings getAccessSetting() {
            return accessSetting;
        }

        public void setAccessSetting(AccessSettings accessSetting) {
            this.accessSetting = accessSetting;
        }

    }

    @RequestMapping(value = "admin/editAccessSettings", method = RequestMethod.GET)
    @ResponseBody
    protected AccessSettingsResponse formAction() throws Exception {
        AccessSettingsResponse response = new AccessSettingsResponse();
        AccessSettings accessSetting = settingsService.getAccessSettings();
        response.setAccessSetting(accessSetting);
        // RequestHelper.storeEnum(request, AccessSettings.UsernameGeneration.class, "usernameGenerations");
        List<AccessSettings.UsernameGeneration> usernameGenerations = new ArrayList();
        usernameGenerations.add(AccessSettings.UsernameGeneration.NONE);
        usernameGenerations.add(AccessSettings.UsernameGeneration.RANDOM);
        response.setUsernameGenerations(usernameGenerations);

        response.setTimePeriodFields(Arrays.asList(TimePeriod.Field.SECONDS, TimePeriod.Field.MINUTES, TimePeriod.Field.HOURS, TimePeriod.Field.DAYS));
        response.setStatus(0);
        return response;
    }

    public static class AccessSettingParameters {

        private boolean virtualKeyboard;
        private boolean virtualKeyboardTransactionPassword;
        private boolean numericPassword;
        private boolean allowOperatorLogin;
        private boolean allowMultipleLogins;

        //private TimePeriod                   adminTimeout                       = new TimePeriod(15, TimePeriod.Field.MINUTES);
        private String adminTimeout_field;
        private int adminTimeout_number;
        private String administrationWhitelist;

        // private UsernameGeneration           usernameGeneration                 = UsernameGeneration.NONE;
        private String usernameGeneration;
        // private RangeConstraint              usernameLength                     = new RangeConstraint(4, 12);
        private int usernameLength_min;
        private int usernameLength_max;

        private int generatedUsernameLength;
        //  private TimePeriod                   memberTimeout                      = new TimePeriod(10, TimePeriod.Field.MINUTES);
        private String memberTimeout_field;
        private int memberTimeout_number;
        private String transactionPasswordChars;
        // private TimePeriod                   poswebTimeout                      = new TimePeriod(1, TimePeriod.Field.DAYS);
        private String poswebTimeout_field;
        private int poswebTimeout_number;

        private String usernameRegex;

        public boolean isVirtualKeyboard() {
            return virtualKeyboard;
        }

        public void setVirtualKeyboard(boolean virtualKeyboard) {
            this.virtualKeyboard = virtualKeyboard;
        }

        public boolean isVirtualKeyboardTransactionPassword() {
            return virtualKeyboardTransactionPassword;
        }

        public void setVirtualKeyboardTransactionPassword(boolean virtualKeyboardTransactionPassword) {
            this.virtualKeyboardTransactionPassword = virtualKeyboardTransactionPassword;
        }

        public boolean isNumericPassword() {
            return numericPassword;
        }

        public void setNumericPassword(boolean numericPassword) {
            this.numericPassword = numericPassword;
        }

        public boolean isAllowOperatorLogin() {
            return allowOperatorLogin;
        }

        public void setAllowOperatorLogin(boolean allowOperatorLogin) {
            this.allowOperatorLogin = allowOperatorLogin;
        }

        public boolean isAllowMultipleLogins() {
            return allowMultipleLogins;
        }

        public void setAllowMultipleLogins(boolean allowMultipleLogins) {
            this.allowMultipleLogins = allowMultipleLogins;
        }

        public String getAdminTimeout_field() {
            return adminTimeout_field;
        }

        public void setAdminTimeout_field(String adminTimeout_field) {
            this.adminTimeout_field = adminTimeout_field;
        }

        public int getAdminTimeout_number() {
            return adminTimeout_number;
        }

        public void setAdminTimeout_number(int adminTimeout_number) {
            this.adminTimeout_number = adminTimeout_number;
        }

        public String getAdministrationWhitelist() {
            return administrationWhitelist;
        }

        public void setAdministrationWhitelist(String administrationWhitelist) {
            this.administrationWhitelist = administrationWhitelist;
        }

        public String getUsernameGeneration() {
            return usernameGeneration;
        }

        public void setUsernameGeneration(String usernameGeneration) {
            this.usernameGeneration = usernameGeneration;
        }

        public int getUsernameLength_min() {
            return usernameLength_min;
        }

        public void setUsernameLength_min(int usernameLength_min) {
            this.usernameLength_min = usernameLength_min;
        }

        public int getUsernameLength_max() {
            return usernameLength_max;
        }

        public void setUsernameLength_max(int usernameLength_max) {
            this.usernameLength_max = usernameLength_max;
        }

        public int getGeneratedUsernameLength() {
            return generatedUsernameLength;
        }

        public void setGeneratedUsernameLength(int generatedUsernameLength) {
            this.generatedUsernameLength = generatedUsernameLength;
        }

        public String getMemberTimeout_field() {
            return memberTimeout_field;
        }

        public void setMemberTimeout_field(String memberTimeout_field) {
            this.memberTimeout_field = memberTimeout_field;
        }

        public int getMemberTimeout_number() {
            return memberTimeout_number;
        }

        public void setMemberTimeout_number(int memberTimeout_number) {
            this.memberTimeout_number = memberTimeout_number;
        }

        public String getTransactionPasswordChars() {
            return transactionPasswordChars;
        }

        public void setTransactionPasswordChars(String transactionPasswordChars) {
            this.transactionPasswordChars = transactionPasswordChars;
        }

        public String getPoswebTimeout_field() {
            return poswebTimeout_field;
        }

        public void setPoswebTimeout_field(String poswebTimeout_field) {
            this.poswebTimeout_field = poswebTimeout_field;
        }

        public int getPoswebTimeout_number() {
            return poswebTimeout_number;
        }

        public void setPoswebTimeout_number(int poswebTimeout_number) {
            this.poswebTimeout_number = poswebTimeout_number;
        }

        public String getUsernameRegex() {
            return usernameRegex;
        }

        public void setUsernameRegex(String usernameRegex) {
            this.usernameRegex = usernameRegex;
        }

    }

    @RequestMapping(value = "admin/editAccessSettings", method = RequestMethod.POST)
    @ResponseBody
    public GenericResponse edit(@RequestBody AccessSettingParameters params) {
        GenericResponse response = new GenericResponse();
        AccessSettings settings = new AccessSettings();
        settings.setAdministrationWhitelist(params.getAdministrationWhitelist());
        settings.setAllowMultipleLogins(params.isAllowMultipleLogins());
        settings.setAllowOperatorLogin(params.isAllowOperatorLogin());
        settings.setGeneratedUsernameLength(params.getGeneratedUsernameLength());
        settings.setNumericPassword(params.isNumericPassword());
        settings.setTransactionPasswordChars(params.getTransactionPasswordChars());
        settings.setUsernameRegex(params.getUsernameRegex());
        settings.setVirtualKeyboard(params.isVirtualKeyboard());
        settings.setVirtualKeyboardTransactionPassword(params.isVirtualKeyboardTransactionPassword());
        TimePeriod adminTimeout = new TimePeriod();
        adminTimeout.setNumber(params.getAdminTimeout_number());
        adminTimeout.setField(TimePeriod.Field.valueOf(params.getAdminTimeout_field()));
        settings.setAdminTimeout(adminTimeout);
        TimePeriod memberTimeout = new TimePeriod();
        memberTimeout.setNumber(params.getMemberTimeout_number());
        memberTimeout.setField(TimePeriod.Field.valueOf(params.getMemberTimeout_field()));
        settings.setMemberTimeout(memberTimeout);
        TimePeriod posWebTimeout = new TimePeriod();
        posWebTimeout.setNumber(params.getPoswebTimeout_number());
        posWebTimeout.setField(TimePeriod.Field.valueOf(params.getPoswebTimeout_field()));
        settings.setPoswebTimeout(posWebTimeout);
        settings.setUsernameGeneration(AccessSettings.UsernameGeneration.valueOf(params.getUsernameGeneration()));
        RangeConstraint usernameLenght = new RangeConstraint();
        usernameLenght.setMax(params.getUsernameLength_max());
        usernameLenght.setMin(params.getUsernameLength_min());
        settings.setUsernameLength(usernameLenght);
        System.out.println("before save---:"+settings);
        settings = settingsService.save(settings);
        System.out.println("after save--:"+settings);
        response.setMessage("settings.access.modified");
        response.setStatus(0);
        return response;
    }

}
