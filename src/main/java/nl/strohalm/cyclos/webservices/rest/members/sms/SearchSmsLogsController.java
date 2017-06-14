package nl.strohalm.cyclos.webservices.rest.members.sms;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import nl.strohalm.cyclos.annotations.Inject;
import nl.strohalm.cyclos.controls.ActionContext;
import nl.strohalm.cyclos.controls.members.sms.SearchSmsLogsForm;
import nl.strohalm.cyclos.entities.groups.MemberGroup;
import nl.strohalm.cyclos.entities.members.Element;
import nl.strohalm.cyclos.entities.members.Member;
import nl.strohalm.cyclos.entities.settings.LocalSettings;
import nl.strohalm.cyclos.entities.sms.SmsLog;
import nl.strohalm.cyclos.entities.sms.SmsLogQuery;
import nl.strohalm.cyclos.entities.sms.SmsLogStatus;
import nl.strohalm.cyclos.entities.sms.SmsLogType;
import nl.strohalm.cyclos.services.elements.ElementService;
import nl.strohalm.cyclos.services.settings.SettingsService;
import nl.strohalm.cyclos.services.sms.SmsLogService;
import nl.strohalm.cyclos.utils.RelationshipHelper;
import nl.strohalm.cyclos.utils.RequestHelper;
import nl.strohalm.cyclos.utils.binding.BeanBinder;
import nl.strohalm.cyclos.utils.binding.DataBinder;
import nl.strohalm.cyclos.utils.binding.DataBinderHelper;
import nl.strohalm.cyclos.utils.binding.PropertyBinder;
import nl.strohalm.cyclos.utils.query.QueryParameters;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;

@Controller
public class SearchSmsLogsController extends BaseRestController {
	private DataBinder<SmsLogQuery> dataBinder;
	private SmsLogService smsLogService;
	private SettingsService settingsService;
	private ElementService elementService;

	@Inject
	public void setSmsLogService(final SmsLogService smsLogService) {
		this.smsLogService = smsLogService;
	}

	public static class SearchSmsLogsRequestDto {

	}

	public static class SearchSmsLogsResponseDto {
		private List<SmsLog> smsLogs;

		public List<SmsLog> getSmsLogs() {
			return smsLogs;
		}

		public void setSmsLogs(List<SmsLog> smsLogs) {
			this.smsLogs = smsLogs;
		}

		public SearchSmsLogsResponseDto(List<SmsLog> smsLogs) {
			super();
			this.smsLogs = smsLogs;
		}

	}

	@RequestMapping(value = "", method = RequestMethod.GET)
	@ResponseBody
	protected SearchSmsLogsResponseDto executeQuery(
			@RequestBody SearchSmsLogsRequestDto form,
			final QueryParameters queryParameters) {
		final SmsLogQuery query = (SmsLogQuery) queryParameters;
		final List<SmsLog> smsLogs = smsLogService.search(query);
		// final HttpServletRequest request = context.getRequest();

		SearchSmsLogsResponseDto response = new SearchSmsLogsResponseDto(
				smsLogs);
		return response;
	}

	protected QueryParameters prepareForm(final ActionContext context) {
		final HttpServletRequest request = context.getRequest();
		// Resolve member id
		final SearchSmsLogsForm form = context.getForm();
		long memberId = form.getMemberId();
		if (memberId < 1) {
			memberId = context.getElement().getId();
		}
		final boolean mySmsLogs = memberId == context.getElement().getId();

		// Load member
		final Member member = elementService.load(memberId, RelationshipHelper
				.nested(Element.Relationships.GROUP,
						MemberGroup.Relationships.SMS_MESSAGES));
		form.setQuery("member", member.getId());
		request.setAttribute("member", member);
		request.setAttribute("mySmsLogs", mySmsLogs);
		RequestHelper.storeEnum(request, SmsLogStatus.class, "statusList");
		RequestHelper.storeEnum(request, SmsLogType.class, "typesList");

		return getDataBinder().readFromString(form.getQuery());
	}

	private DataBinder<SmsLogQuery> getDataBinder() {
		if (dataBinder == null) {
			final LocalSettings settings = settingsService.getLocalSettings();
			final BeanBinder<SmsLogQuery> binder = BeanBinder
					.instance(SmsLogQuery.class);
			binder.registerBinder("period",
					DataBinderHelper.periodBinder(settings, "period"));
			binder.registerBinder("member",
					PropertyBinder.instance(Member.class, "member"));
			binder.registerBinder("type",
					PropertyBinder.instance(SmsLogType.class, "type"));
			binder.registerBinder("status",
					PropertyBinder.instance(SmsLogStatus.class, "status"));
			binder.registerBinder("pageParameters",
					DataBinderHelper.pageBinder());
			dataBinder = binder;
		}
		return dataBinder;
	}

}
