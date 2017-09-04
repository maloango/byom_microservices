package nl.strohalm.cyclos.webservices.rest.settings;

import javax.servlet.http.HttpServletRequest;

import nl.strohalm.cyclos.controls.ActionContext;
//import nl.strohalm.cyclos.controls.settings.EditMessageSettingForm;
import nl.strohalm.cyclos.entities.settings.MessageSettings;
import nl.strohalm.cyclos.services.settings.SettingsService;
import nl.strohalm.cyclos.utils.ActionHelper;
import nl.strohalm.cyclos.utils.PropertyHelper;
import nl.strohalm.cyclos.utils.TextFormat;
import nl.strohalm.cyclos.utils.binding.PropertyException;
import nl.strohalm.cyclos.utils.conversion.HtmlConverter;
import nl.strohalm.cyclos.utils.conversion.StringTrimmerConverter;
import nl.strohalm.cyclos.utils.validation.ValidationException;
import nl.strohalm.cyclos.utils.validation.Validator;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;

import org.apache.commons.lang.StringUtils;
import org.apache.struts.action.ActionForward;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class EditMessageSettingController extends BaseRestController {
	public static final int MAX_SUBJECT_SIZE = 400;
	public static final int MAX_BODY_SIZE = 4000;
	public static final int MAX_SMS_SIZE = 256;
	private SettingsService settingsService;

	public static class EditMessageSettingRequestDto {
		private String setting;
		private String value;
		private String subject;
		private String body;
		private String sms;
		private boolean hasGeneral;
		private boolean hasSubject;
		private boolean hasBody;
		private boolean hasSms;

		public String getBody() {
			return body;
		}

		public String getSetting() {
			return setting;
		}

		public String getSms() {
			return sms;
		}

		public String getSubject() {
			return subject;
		}

		public String getValue() {
			return value;
		}

		public boolean isHasBody() {
			return hasBody;
		}

		public boolean isHasGeneral() {
			return hasGeneral;
		}

		public boolean isHasSms() {
			return hasSms;
		}

		public boolean isHasSubject() {
			return hasSubject;
		}

		public void setBody(final String body) {
			this.body = body;
		}

		public void setHasBody(final boolean hasBody) {
			this.hasBody = hasBody;
		}

		public void setHasGeneral(final boolean hasGeneral) {
			this.hasGeneral = hasGeneral;
		}

		public void setHasSms(final boolean hasSms) {
			this.hasSms = hasSms;
		}

		public void setHasSubject(final boolean hasSubject) {
			this.hasSubject = hasSubject;
		}

		public void setSetting(final String setting) {
			this.setting = setting;
		}

		public void setSms(final String sms) {
			this.sms = sms;
		}

		public void setSubject(final String subject) {
			this.subject = subject;
		}

		public void setValue(final String value) {
			this.value = value;
		}

	}

	public static class EditMessageSettingResponseDto {
		private String message;
		private String setting;

		public EditMessageSettingResponseDto(String message, String setting) {
			super();
			this.message = message;
			this.setting = setting;
		}

		public String getMessage() {
			return message;
		}

		public void setMessage(String message) {
			this.message = message;
		}
	}

	@RequestMapping(value = "admin/editMessageSetting", method = RequestMethod.POST)
	@ResponseBody
	protected EditMessageSettingResponseDto handleSubmit(
			final EditMessageSettingRequestDto form) throws Exception {
		EditMessageSettingResponseDto response = null;
                try{
		final String setting = form.getSetting();
		StringUtils.trimToNull(form.getValue());
		final MessageSettings messageSettings = settingsService
				.getMessageSettings();
		if (form.isHasGeneral()) {
			final boolean isHtml = setting.toLowerCase().endsWith("html");
			String value;
			if (isHtml) {
				value = HtmlConverter.instance().valueOf(form.getValue());
			} else {
				value = StringTrimmerConverter.instance().valueOf(
						form.getValue());
			}
			PropertyHelper.set(messageSettings, setting, value);
		}
		if (form.isHasSubject()) {
			PropertyHelper.set(messageSettings, setting + "Subject",
					StringTrimmerConverter.instance()
							.valueOf(form.getSubject()));
		}
		if (form.isHasBody()) {
			PropertyHelper.set(messageSettings, setting + "Message",
					HtmlConverter.instance().valueOf(form.getBody()));
		}
		if (form.isHasSms()) {
			PropertyHelper.set(messageSettings, setting + "Sms",
					StringTrimmerConverter.instance().valueOf(form.getSms()));
		}
		settingsService.save(messageSettings);

		String message = "settings.message.modified";
		response = new EditMessageSettingResponseDto(
				message, setting);}
                catch(Exception e){
                    e.printStackTrace();
                }
		return response;
	}

//	protected void prepareForm(final ActionContext context) throws Exception {
//		final HttpServletRequest request = context.getRequest();
//		final EditMessageSettingForm form = context.getForm();
//		final MessageSettings messageSettings = settingsService
//				.getMessageSettings();
//		final String setting = StringUtils.trimToNull(form.getSetting());
//		if (setting == null) {
//			throw new ValidationException();
//		}
//
//		// Try the setting name itself (like global settings)
//		try {
//			final String value = PropertyHelper.get(messageSettings, setting);
//			form.setValue(value);
//			form.setHasGeneral(true);
//			request.setAttribute("generalIsHtml", setting.toLowerCase()
//					.endsWith("html"));
//		} catch (final PropertyException e) {
//			// Ignore - probably didn't have this setting
//		}
//
//		// Try the subject
//		try {
//			final String property = setting + "Subject";
//			final String value = PropertyHelper.get(messageSettings, property);
//			form.setSubject(value);
//			form.setHasSubject(true);
//		} catch (final PropertyException e) {
//			// Ignore - probably didn't have this setting
//		}
//
//		// Try the body
//		try {
//			final String property = setting + "Message";
//			final String value = PropertyHelper.get(messageSettings, property);
//			form.setBody(value);
//			form.setHasBody(true);
//		} catch (final PropertyException e) {
//			// Ignore - probably didn't have this setting
//		}
//
//		// Try the sms
//		try {
//			final String property = setting + "Sms";
//			final String value = PropertyHelper.get(messageSettings, property);
//			form.setSms(value);
//			form.setHasSms(true);
//		} catch (final PropertyException e) {
//			// Ignore - probably didn't have this setting
//		}
//
//		request.setAttribute("setting", setting);
//		request.setAttribute("format", TextFormat.RICH);
//	}
//
//	protected void validateForm(final ActionContext context) {
//		final EditMessageSettingForm form = context.getForm();
//		final Validator validator = new Validator("settings.message");
//		if (form.isHasGeneral()) {
//			validator.property("value").required().maxLength(MAX_SUBJECT_SIZE);
//		}
//		if (form.isHasSubject()) {
//			validator.property("subject").required()
//					.maxLength(MAX_SUBJECT_SIZE);
//		}
//		if (form.isHasBody()) {
//			validator.property("body").required().maxLength(MAX_BODY_SIZE);
//		}
//		if (form.isHasSms()) {
//			validator.property("sms").required().maxLength(MAX_SMS_SIZE);
//		}
//		validator.validate(form);
//	}

}
