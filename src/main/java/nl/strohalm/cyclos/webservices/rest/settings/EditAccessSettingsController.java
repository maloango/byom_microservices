package nl.strohalm.cyclos.webservices.rest.settings;

import java.util.Arrays;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import nl.strohalm.cyclos.controls.ActionContext;
import nl.strohalm.cyclos.controls.settings.EditAccessSettingsForm;
import nl.strohalm.cyclos.entities.settings.AccessSettings;
import nl.strohalm.cyclos.services.settings.SettingsService;
import nl.strohalm.cyclos.utils.RequestHelper;
import nl.strohalm.cyclos.utils.TimePeriod;
import nl.strohalm.cyclos.utils.binding.BeanBinder;
import nl.strohalm.cyclos.utils.binding.DataBinder;
import nl.strohalm.cyclos.utils.binding.DataBinderHelper;
import nl.strohalm.cyclos.utils.binding.PropertyBinder;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class EditAccessSettingsController extends BaseRestController {
	private DataBinder<AccessSettings> dataBinder;

	public DataBinder<AccessSettings> getDataBinder() {
		if (dataBinder == null) {
			final BeanBinder<AccessSettings> binder = BeanBinder
					.instance(AccessSettings.class);
			binder.registerBinder("virtualKeyboard",
					PropertyBinder.instance(Boolean.TYPE, "virtualKeyboard"));
			binder.registerBinder("virtualKeyboardTransactionPassword",
					PropertyBinder.instance(Boolean.TYPE,
							"virtualKeyboardTransactionPassword"));
			binder.registerBinder("numericPassword",
					PropertyBinder.instance(Boolean.TYPE, "numericPassword"));
			binder.registerBinder("allowMultipleLogins", PropertyBinder
					.instance(Boolean.TYPE, "allowMultipleLogins"));
			binder.registerBinder("allowOperatorLogin",
					PropertyBinder.instance(Boolean.TYPE, "allowOperatorLogin"));
			binder.registerBinder("adminTimeout",
					DataBinderHelper.timePeriodBinder("adminTimeout"));
			binder.registerBinder("administrationWhitelist", PropertyBinder
					.instance(String.class, "administrationWhitelist"));
			binder.registerBinder("memberTimeout",
					DataBinderHelper.timePeriodBinder("memberTimeout"));
			binder.registerBinder("poswebTimeout",
					DataBinderHelper.timePeriodBinder("poswebTimeout"));
			binder.registerBinder("usernameLength",
					DataBinderHelper.rangeConstraintBinder("usernameLength"));
			binder.registerBinder("usernameGeneration", PropertyBinder
					.instance(AccessSettings.UsernameGeneration.class,
							"usernameGeneration"));
			binder.registerBinder("generatedUsernameLength", PropertyBinder
					.instance(Integer.TYPE, "generatedUsernameLength"));
			binder.registerBinder("transactionPasswordChars", PropertyBinder
					.instance(String.class, "transactionPasswordChars"));
			binder.registerBinder("usernameRegex",
					PropertyBinder.instance(String.class, "usernameRegex"));
			dataBinder = binder;
		}
		return dataBinder;
	}

	private SettingsService settingsService;

	public SettingsService getSettingsService() {
		return settingsService;
	}

	public static class EditAccessSettingsRequestDto {
		protected Map<String, Object> values;

		public Map<String, Object> getValues() {
			return values;
		}

		public void setValues(final Map<String, Object> values) {
			this.values = values;
		}

		public Map<String, Object> getSetting() {
			return values;
		}

		public Object getSetting(final String key) {
			return values.get(key);
		}

		public void setSetting(final Map<String, Object> map) {
			values = map;
		}

		public void setSetting(final String key, final Object value) {
			values.put(key, value);
		}
	}

	public static class EditAccessSettingsResponseDto {
		private String message;

		public String getMessage() {
			return message;
		}

		public void setMessage(String message) {
			this.message = message;
		}
	}
	@RequestMapping(value = "admin/editAccessSettings", method = RequestMethod.POST)
	@ResponseBody
	protected EditAccessSettingsResponseDto formAction(@RequestBody EditAccessSettingsRequestDto form) throws Exception {
		
		AccessSettings settings = getDataBinder().readFromString(
				form.getSetting());
                EditAccessSettingsResponseDto response =null;
                try{
		settings = settingsService.save(settings);
                response=new EditAccessSettingsResponseDto();
		response.setMessage("settings.access.modified");}
                catch(Exception e){
                    e.printStackTrace();
                }
		return response;
	}

	protected void prepareForm(final ActionContext context) throws Exception {
		final HttpServletRequest request = context.getRequest();
		final EditAccessSettingsForm form = context.getForm();
		getDataBinder().writeAsString(form.getSetting(),
				settingsService.getAccessSettings());
		RequestHelper.storeEnum(request,
				AccessSettings.UsernameGeneration.class, "usernameGenerations");
		request.setAttribute("timePeriodFields", Arrays.asList(
				TimePeriod.Field.SECONDS, TimePeriod.Field.MINUTES,
				TimePeriod.Field.HOURS, TimePeriod.Field.DAYS));
	}

	protected void validateForm(final ActionContext context) {
		final EditAccessSettingsForm form = context.getForm();
		final AccessSettings settings = getDataBinder().readFromString(
				form.getSetting());
		settingsService.validate(settings);
	}

}
