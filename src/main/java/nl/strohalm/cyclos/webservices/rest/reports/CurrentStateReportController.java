package nl.strohalm.cyclos.webservices.rest.reports;

import java.util.Calendar;
import java.util.Map;

import org.apache.struts.action.ActionForward;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import nl.strohalm.cyclos.annotations.Inject;
import nl.strohalm.cyclos.controls.ActionContext;
//import nl.strohalm.cyclos.controls.reports.CurrentStateReportForm;
import nl.strohalm.cyclos.entities.reports.CurrentStateReportVO;
import nl.strohalm.cyclos.entities.settings.LocalSettings;
import nl.strohalm.cyclos.services.reports.CurrentStateReportParameters;
import nl.strohalm.cyclos.services.reports.CurrentStateReportParameters.TimePointType;
import nl.strohalm.cyclos.services.reports.CurrentStateReportService;
import nl.strohalm.cyclos.services.settings.SettingsService;
import nl.strohalm.cyclos.utils.binding.BeanBinder;
import nl.strohalm.cyclos.utils.binding.DataBinder;
import nl.strohalm.cyclos.utils.binding.PropertyBinder;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;

@Controller
public class CurrentStateReportController extends BaseRestController {
	private CurrentStateReportService currentStateReportService;
	private DataBinder<CurrentStateReportParameters> dataBinder;
	private SettingsService settingsService;

	@Inject
	public void setCurrentStateReportService(
			final CurrentStateReportService currentStateReportService) {
		this.currentStateReportService = currentStateReportService;
	}

//	protected ActionForward handleDisplay(final ActionContext context)
//			throws Exception {
//		try {
//			prepareForm(context);
//		} catch (final Exception e) {
//			return context.sendError("reports.error.formDisplayError");
//		}
//		return context.getInputForward();
//	}

	public static class CurrentStateReportRequestDto {
		protected Map<String, Object> values;

		public Map<String, Object> getValues() {
			return values;
		}

		public void setValues(final Map<String, Object> values) {
			this.values = values;
		}

		public Map<String, Object> getCurrentStateReport() {
			return values;
		}

		public Object getCurrentStateReport(final String key) {
			return values.get(key);
		}

		public void setCurrentStateReport(
				final Map<String, Object> currentStateReport) {
			values = currentStateReport;
		}

		public void setCurrentStateReport(final String key, final Object value) {
			values.put(key, value);
		}
	}

	public static class CurrentStateReportResponseDto {
		CurrentStateReportParameters dto;
		Calendar historyTime;
		int singleCurrency;
		CurrentStateReportVO report;

		public CurrentStateReportResponseDto(CurrentStateReportParameters dto,
				Calendar historyTime, int singleCurrency,
				CurrentStateReportVO report) {
			super();
			this.dto = dto;
			this.historyTime = historyTime;
			this.singleCurrency = singleCurrency;
			this.report = report;
		}
	}

	@RequestMapping(value = "admin/reportsCurrentState", method = RequestMethod.POST)
	@ResponseBody
	protected CurrentStateReportResponseDto handleSubmit(
			@RequestBody CurrentStateReportRequestDto form) throws Exception {
                CurrentStateReportResponseDto response = null;
                try{
		final CurrentStateReportParameters params = getDataBinder()
				.readFromString(form.getCurrentStateReport());
		
		Calendar historyTime = null;
		if (params.getTimePointType() == TimePointType.TIME_POINT_HISTORY) {
			params.setInvoices(false);
			params.setLoans(false);
			params.setReferences(false);
			historyTime = params.getTimePoint();
		}
		final CurrentStateReportVO report = currentStateReportService
				.getCurrentStateReport(params);
		int singleCurrency = report.getCurrencies().size();
                 response = new CurrentStateReportResponseDto(params, historyTime, singleCurrency, report);}
                catch(Exception e){
                    e.printStackTrace();
                }
		return response;
	}

//	protected void prepareForm(final ActionContext context) throws Exception {
//		final CurrentStateReportForm form = context.getForm();
//		final CurrentStateReportParameters params = new CurrentStateReportParameters();
//		getDataBinder().writeAsString(form.getCurrentStateReport(), params);
//	}

	private DataBinder<CurrentStateReportParameters> getDataBinder() {
		if (dataBinder == null) {
			final LocalSettings localSettings = settingsService
					.getLocalSettings();
			final BeanBinder<CurrentStateReportParameters> binder = BeanBinder
					.instance(CurrentStateReportParameters.class);
			binder.registerBinder("ads",
					PropertyBinder.instance(Boolean.TYPE, "ads"));
			binder.registerBinder("invoices",
					PropertyBinder.instance(Boolean.TYPE, "invoices"));
			binder.registerBinder("loans",
					PropertyBinder.instance(Boolean.TYPE, "loans"));
			binder.registerBinder("memberAccountInformation", PropertyBinder
					.instance(Boolean.TYPE, "memberAccountInformation"));
			binder.registerBinder("memberGroupInformation", PropertyBinder
					.instance(Boolean.TYPE, "memberGroupInformation"));
			binder.registerBinder("references",
					PropertyBinder.instance(Boolean.TYPE, "references"));
			binder.registerBinder("systemAccountInformation", PropertyBinder
					.instance(Boolean.TYPE, "systemAccountInformation"));
			binder.registerBinder("timePoint", PropertyBinder.instance(
					Calendar.class, "timePoint",
					localSettings.getDateTimeConverter()));
			binder.registerBinder("timePointType", PropertyBinder.instance(
					TimePointType.class, "timePointType"));
			dataBinder = binder;
		}
		return dataBinder;
	}

}
