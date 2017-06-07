package nl.strohalm.cyclos.webservices.rest.settings;

import java.util.HashMap;
import java.util.Map;

import nl.strohalm.cyclos.controls.ActionContext;
import nl.strohalm.cyclos.controls.settings.EditMailTranslationForm;
import nl.strohalm.cyclos.entities.settings.MailTranslation;
import nl.strohalm.cyclos.services.settings.SettingsService;
import nl.strohalm.cyclos.utils.binding.BeanBinder;
import nl.strohalm.cyclos.utils.binding.DataBinder;
import nl.strohalm.cyclos.utils.binding.PropertyBinder;
import nl.strohalm.cyclos.utils.conversion.HtmlConverter;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class EditMailTranslationController extends BaseRestController {
	private DataBinder<MailTranslation> dataBinder;
	private SettingsService settingsService;

	public SettingsService getSettingsService() {
		return settingsService;
	}

	public void setSettingsService(SettingsService settingsService) {
		this.settingsService = settingsService;
	}

	public DataBinder<MailTranslation> getDataBinder() {
		if (dataBinder == null) {
			final BeanBinder<MailTranslation> binder = BeanBinder
					.instance(MailTranslation.class);
			binder.registerBinder("invitationSubject",
					PropertyBinder.instance(String.class, "invitationSubject"));
			binder.registerBinder("invitationMessage",
					PropertyBinder.instance(String.class, "invitationMessage",
							HtmlConverter.instance()));
			binder.registerBinder("activationSubject",
					PropertyBinder.instance(String.class, "activationSubject"));
			binder.registerBinder("activationMessageWithPassword",
					PropertyBinder.instance(String.class,
							"activationMessageWithPassword",
							HtmlConverter.instance()));
			binder.registerBinder("activationMessageWithoutPassword",
					PropertyBinder.instance(String.class,
							"activationMessageWithoutPassword",
							HtmlConverter.instance()));
			binder.registerBinder("resetPasswordSubject", PropertyBinder
					.instance(String.class, "resetPasswordSubject"));
			binder.registerBinder("resetPasswordMessage", PropertyBinder
					.instance(String.class, "resetPasswordMessage",
							HtmlConverter.instance()));
			binder.registerBinder("mailValidationSubject", PropertyBinder
					.instance(String.class, "mailValidationSubject"));
			binder.registerBinder("mailValidationMessage", PropertyBinder
					.instance(String.class, "mailValidationMessage",
							HtmlConverter.instance()));
			dataBinder = binder;
		}
		return dataBinder;
	}

	public SettingsService getTranslationService() {
		return settingsService;
	}

	public static class EditMailTranslationRequestDto {
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

	public static class EditMailTranslationResponseDto {
		private String message;

		public String getMessage() {
			return message;
		}

		public void setMessage(String message) {
			this.message = message;
		}
	}

	@RequestMapping(value = "", method = RequestMethod.PUT)
	@ResponseBody
	protected EditMailTranslationResponseDto formAction(
			@RequestBody EditMailTranslationRequestDto form) throws Exception {
		// final EditMailTranslationForm form = context.getForm();
		MailTranslation settings = getDataBinder().readFromString(
				form.getSetting());
		settings = settingsService.save(settings);
		EditMailTranslationResponseDto response = new EditMailTranslationResponseDto();
		response.setMessage("settings.mailTranslation.modified");
		return response;
	}

	protected void prepareForm(final ActionContext context) throws Exception {
		final EditMailTranslationForm form = context.getForm();
		getDataBinder().writeAsString(form.getSetting(),
				settingsService.getMailTranslation());
	}

	protected void validateForm(final ActionContext context) {
		final EditMailTranslationForm form = context.getForm();
		final MailTranslation settings = getDataBinder().readFromString(
				form.getSetting());
		settingsService.validate(settings);
	}

}
