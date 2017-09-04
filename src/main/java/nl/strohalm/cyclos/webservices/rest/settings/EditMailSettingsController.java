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

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class EditMailSettingsController extends BaseRestController {
	private DataBinder<MailSettings> dataBinder;
	private SettingsService settingsService;

	public DataBinder<MailSettings> getDataBinder() {
		if (dataBinder == null) {
			final BeanBinder<MailSettings> binder = BeanBinder
					.instance(MailSettings.class);
			binder.registerBinder("fromMail",
					PropertyBinder.instance(String.class, "fromMail"));
			binder.registerBinder("smtpServer",
					PropertyBinder.instance(String.class, "smtpServer"));
			binder.registerBinder("smtpPort",
					PropertyBinder.instance(Integer.TYPE, "smtpPort"));
			binder.registerBinder("smtpUsername",
					PropertyBinder.instance(String.class, "smtpUsername"));
			binder.registerBinder("smtpPassword",
					PropertyBinder.instance(String.class, "smtpPassword"));
			binder.registerBinder("smtpUseTLS",
					PropertyBinder.instance(boolean.class, "smtpUseTLS"));
			dataBinder = binder;
		}
		return dataBinder;
	}

	public SettingsService getSettingsService() {
		return settingsService;
	}

	public static class EditMailSettingsRequestDto {
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

		protected Map<String, Object> values;

		public Map<String, Object> getValues() {
			return values;
		}

		public void setValues(final Map<String, Object> values) {
			this.values = values;
		}
	}

	public static class EditMailSettingsResponseDto {
		private String message;

		public String getMessage() {
			return message;
		}

		public void setMessage(String message) {
			this.message = message;
		}
	}

	@RequestMapping(value = "admin/editMailSettings", method = RequestMethod.PUT)
	@ResponseBody
	protected EditMailSettingsResponseDto formAction(
			final EditMailSettingsRequestDto form) throws Exception {
	
		MailSettings settings = getDataBinder().readFromString(
				form.getSetting());
                EditMailSettingsResponseDto response =null;
                try{
		settings = settingsService.save(settings);
		 response = new EditMailSettingsResponseDto();
		response.setMessage("settings.mail.modified");}
                catch(Exception e){
                    e.printStackTrace();
                }
		return response;
	}

//	protected void prepareForm(final ActionContext context) throws Exception {
//		final EditMailSettingsForm form = context.getForm();
//		getDataBinder().writeAsString(form.getSetting(),
//				settingsService.getMailSettings());
//	}
//
//	protected void validateForm(final ActionContext context) {
//		final EditMailSettingsForm form = context.getForm();
//		final MailSettings settings = getDataBinder().readFromString(
//				form.getSetting());
//		settingsService.validate(settings);
//	}

}
