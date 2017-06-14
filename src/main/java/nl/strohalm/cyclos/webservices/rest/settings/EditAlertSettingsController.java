package nl.strohalm.cyclos.webservices.rest.settings;

import java.util.Arrays;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import nl.strohalm.cyclos.controls.ActionContext;
import nl.strohalm.cyclos.controls.settings.EditAlertSettingsForm;
import nl.strohalm.cyclos.entities.settings.AlertSettings;
import nl.strohalm.cyclos.services.settings.SettingsService;
import nl.strohalm.cyclos.utils.TimePeriod;
import nl.strohalm.cyclos.utils.binding.BeanBinder;
import nl.strohalm.cyclos.utils.binding.DataBinder;
import nl.strohalm.cyclos.utils.binding.DataBinderHelper;
import nl.strohalm.cyclos.utils.binding.PropertyBinder;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;

@Controller
public class EditAlertSettingsController extends BaseRestController {

	private DataBinder<AlertSettings> dataBinder;
	SettingsService settingsService;

	public DataBinder<AlertSettings> getDataBinder() {
		if (dataBinder == null) {
			final BeanBinder<AlertSettings> binder = BeanBinder
					.instance(AlertSettings.class);
			binder.registerBinder("givenVeryBadRefs",
					PropertyBinder.instance(Integer.TYPE, "givenVeryBadRefs"));
			binder.registerBinder("receivedVeryBadRefs", PropertyBinder
					.instance(Integer.TYPE, "receivedVeryBadRefs"));
			binder.registerBinder("idleInvoiceExpiration",
					DataBinderHelper.timePeriodBinder("idleInvoiceExpiration"));
			binder.registerBinder("amountDeniedInvoices", PropertyBinder
					.instance(Integer.TYPE, "amountDeniedInvoices"));
			binder.registerBinder("amountIncorrectLogin", PropertyBinder
					.instance(Integer.TYPE, "amountIncorrectLogin"));
			dataBinder = binder;
		}
		return dataBinder;
	}

	public SettingsService getSettingsService() {
		return settingsService;
	}

	public static class EditAlertSettingsRequestDto {
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

	public static class EditAlertSettingsResponseDto {
		private String message;

		public String getMessage() {
			return message;
		}

		public void setMessage(String message) {
			this.message = message;
		}
	}

	@RequestMapping(value = "", method = RequestMethod.POST)
	@ResponseBody
	protected EditAlertSettingsResponseDto formAction(
			@RequestBody EditAlertSettingsRequestDto form) throws Exception {
		// final EditAlertSettingsForm form = context.getForm();
		AlertSettings settings = getDataBinder().readFromString(
				form.getSetting());
		settings = settingsService.save(settings);
		EditAlertSettingsResponseDto response = new EditAlertSettingsResponseDto();
		response.setMessage("settings.alert.modified");
		return response;
	}

	protected void prepareForm(final ActionContext context) throws Exception {
		final HttpServletRequest request = context.getRequest();
		final EditAlertSettingsForm form = context.getForm();
		getDataBinder().writeAsString(form.getSetting(),
				settingsService.getAlertSettings());
		request.setAttribute("timePeriodFields", Arrays.asList(
				TimePeriod.Field.DAYS, TimePeriod.Field.WEEKS,
				TimePeriod.Field.MONTHS));
	}

	protected void validateForm(final ActionContext context) {
		final EditAlertSettingsForm form = context.getForm();
		final AlertSettings settings = getDataBinder().readFromString(
				form.getSetting());
		settingsService.validate(settings);
	}
}
