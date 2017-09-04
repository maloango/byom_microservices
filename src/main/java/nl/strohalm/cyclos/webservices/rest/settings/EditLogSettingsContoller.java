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

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class EditLogSettingsContoller extends BaseRestController {
	private DataBinder<LogSettings> dataBinder;
	private SettingsService settingsService;

	public DataBinder<LogSettings> getDataBinder() {
		if (dataBinder == null) {
			final BeanBinder<LogSettings> binder = BeanBinder
					.instance(LogSettings.class);
			binder.registerBinder("traceLevel",
					PropertyBinder.instance(TraceLevel.class, "traceLevel"));
			binder.registerBinder("traceFile",
					PropertyBinder.instance(String.class, "traceFile"));
			binder.registerBinder("traceWritesOnly",
					PropertyBinder.instance(Boolean.TYPE, "traceWritesOnly"));
			binder.registerBinder("webServiceLevel", PropertyBinder.instance(
					WebServiceLevel.class, "webServiceLevel"));
			binder.registerBinder("webServiceFile",
					PropertyBinder.instance(String.class, "webServiceFile"));
			binder.registerBinder("restLevel",
					PropertyBinder.instance(WebServiceLevel.class, "restLevel"));
			binder.registerBinder("restFile",
					PropertyBinder.instance(String.class, "restFile"));
			binder.registerBinder("transactionLevel", PropertyBinder.instance(
					TransactionLevel.class, "transactionLevel"));
			binder.registerBinder("transactionFile",
					PropertyBinder.instance(String.class, "transactionFile"));
			binder.registerBinder("accountFeeLevel", PropertyBinder.instance(
					AccountFeeLevel.class, "accountFeeLevel"));
			binder.registerBinder("accountFeeFile",
					PropertyBinder.instance(String.class, "accountFeeFile"));
			binder.registerBinder("scheduledTaskLevel", PropertyBinder
					.instance(ScheduledTaskLevel.class, "scheduledTaskLevel"));
			binder.registerBinder("scheduledTaskFile",
					PropertyBinder.instance(String.class, "scheduledTaskFile"));
			binder.registerBinder("maxFilesPerLog",
					PropertyBinder.instance(Integer.TYPE, "maxFilesPerLog"));
			binder.registerBinder("maxLengthPerFile",
					PropertyBinder.instance(Integer.TYPE, "maxLengthPerFile"));
			binder.registerBinder("maxLengthPerFileUnits", PropertyBinder
					.instance(FileUnits.class, "maxLengthPerFileUnits"));
			dataBinder = binder;
		}
		return dataBinder;
	}

	public SettingsService getSettingsService() {
		return settingsService;
	}

	public static class EditLogSettingsRequestDto {
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

	public static class EditLogSettingsResponseDto {
		private String message;

		public String getMessage() {
			return message;
		}

		public void setMessage(String message) {
			this.message = message;
		}
	}

	@RequestMapping(value = "admin/editLogSettings", method = RequestMethod.POST)
	@ResponseBody
	protected EditLogSettingsResponseDto formAction(
			@RequestBody EditLogSettingsRequestDto form) throws Exception {
		EditLogSettingsResponseDto response =null;
            try{
		LogSettings settings = getDataBinder()
				.readFromString(form.getSetting());
		settings = settingsService.save(settings);
                 response = new EditLogSettingsResponseDto();
		response.setMessage("settings.log.modified");}
                catch(Exception e){
                    e.printStackTrace();
                }
		return response;
	}

//	protected void prepareForm(final ActionContext context) throws Exception {
//		final HttpServletRequest request = context.getRequest();
//		final EditLogSettingsForm form = context.getForm();
//		final LogSettings settings = settingsService.getLogSettings();
//		getDataBinder().writeAsString(form.getSetting(), settings);
//
//		RequestHelper.storeEnum(request, TraceLevel.class, "traceLevels");
//		RequestHelper.storeEnum(request, WebServiceLevel.class,
//				"webServiceLevels");
//		RequestHelper.storeEnum(request, TransactionLevel.class,
//				"transactionLevels");
//		RequestHelper.storeEnum(request, AccountFeeLevel.class,
//				"accountFeeLevels");
//		RequestHelper.storeEnum(request, ScheduledTaskLevel.class,
//				"scheduledTaskLevels");
//		RequestHelper.storeEnum(request, FileUnits.class, "fileUnits");
//	}
//
//	protected void validateForm(final ActionContext context) {
//		final EditLogSettingsForm form = context.getForm();
//		final LogSettings settings = getDataBinder().readFromString(
//				form.getSetting());
//		settingsService.validate(settings);
//	}
}
