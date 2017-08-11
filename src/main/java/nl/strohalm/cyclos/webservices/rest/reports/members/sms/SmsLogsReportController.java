package nl.strohalm.cyclos.webservices.rest.reports.members.sms;

import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import nl.strohalm.cyclos.annotations.Inject;
import nl.strohalm.cyclos.controls.ActionContext;
import nl.strohalm.cyclos.controls.reports.members.sms.SmsLogsReportForm;
import nl.strohalm.cyclos.entities.groups.AdminGroup;
import nl.strohalm.cyclos.entities.groups.MemberGroup;
import nl.strohalm.cyclos.entities.members.Administrator;
import nl.strohalm.cyclos.entities.members.Element;
import nl.strohalm.cyclos.entities.members.Member;
import nl.strohalm.cyclos.entities.members.messages.Message;
import nl.strohalm.cyclos.entities.settings.LocalSettings;
import nl.strohalm.cyclos.entities.sms.SmsLog;
import nl.strohalm.cyclos.entities.sms.SmsLogReportQuery;
import nl.strohalm.cyclos.entities.sms.SmsLogReportVO;
import nl.strohalm.cyclos.entities.sms.SmsLogStatus;
import nl.strohalm.cyclos.entities.sms.SmsLogType;
import nl.strohalm.cyclos.entities.sms.SmsMailing;
import nl.strohalm.cyclos.entities.sms.SmsMailingType;
import nl.strohalm.cyclos.entities.sms.SmsType;
import nl.strohalm.cyclos.services.elements.ElementService;
import nl.strohalm.cyclos.services.groups.GroupService;
import nl.strohalm.cyclos.services.permissions.PermissionService;
import nl.strohalm.cyclos.services.settings.SettingsService;
import nl.strohalm.cyclos.services.sms.SmsLogService;
import nl.strohalm.cyclos.utils.EntityHelper;
import nl.strohalm.cyclos.utils.RelationshipHelper;
import nl.strohalm.cyclos.utils.RequestHelper;
import nl.strohalm.cyclos.utils.binding.BeanBinder;
import nl.strohalm.cyclos.utils.binding.DataBinder;
import nl.strohalm.cyclos.utils.binding.DataBinderHelper;
import nl.strohalm.cyclos.utils.binding.PropertyBinder;
import nl.strohalm.cyclos.utils.binding.SimpleCollectionBinder;
import nl.strohalm.cyclos.utils.query.QueryParameters;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class SmsLogsReportController extends BaseRestController {

	public static DataBinder<SmsLogReportQuery> getSmsLogReportQueryBinder(
			final LocalSettings settings) {
		final BeanBinder<SmsLogReportQuery> binder = BeanBinder
				.instance(SmsLogReportQuery.class);
		binder.registerBinder("period",
				DataBinderHelper.periodBinder(settings, "period"));
		binder.registerBinder("memberGroups", SimpleCollectionBinder.instance(
				MemberGroup.class, "memberGroups"));
		binder.registerBinder("member",
				PropertyBinder.instance(Member.class, "member"));
		binder.registerBinder("status",
				PropertyBinder.instance(SmsLogStatus.class, "status"));
		binder.registerBinder("type",
				PropertyBinder.instance(SmsLogType.class, "type"));
		binder.registerBinder("mailingTypes", SimpleCollectionBinder.instance(
				SmsMailingType.class, "mailingTypes"));
		binder.registerBinder("messageTypes", SimpleCollectionBinder.instance(
				Message.Type.class, "messageTypes"));
		binder.registerBinder("smsTypes",
				SimpleCollectionBinder.instance(SmsType.class, "smsTypes"));
		binder.registerBinder("pageParameters", DataBinderHelper.pageBinder());
		return binder;
	}

	private DataBinder<SmsLogReportQuery> dataBinder;
	private SmsLogService smsLogService;

	@Inject
	public void setSmsLogService(final SmsLogService smsLogService) {
		this.smsLogService = smsLogService;
	}

	private GroupService groupService;
	private ElementService elementService;
	private SettingsService settingsService;

	public static class SmsLogsReportRequestDto {

	}

	public static class SmsLogsReportResponseDto {
		Map<SmsLogType, Map<SmsLogStatus, Integer>> totals;
		Map<SmsLogType, Integer> totalsByType;
		Map<SmsLogStatus, Integer> totalsByStatus;
		int total;
		List<SmsLog> smsLogs;

		public SmsLogsReportResponseDto(
				Map<SmsLogType, Map<SmsLogStatus, Integer>> totals,
				Map<SmsLogType, Integer> totalsByType,
				Map<SmsLogStatus, Integer> totalsByStatus, int total,
				List<SmsLog> smsLogs) {
			super();
			this.totals = totals;
			this.totalsByType = totalsByType;
			this.totalsByStatus = totalsByStatus;
			this.total = total;
			this.smsLogs = smsLogs;
		}

	}

	@RequestMapping(value = "admin/membersSmsLogsReport", method = RequestMethod.POST)
	@ResponseBody
	protected SmsLogsReportResponseDto executeQuery(
			@RequestBody SmsLogsReportRequestDto context,
			final QueryParameters queryParameters) {
            SmsLogsReportResponseDto response =null;
            try{
		final SmsLogReportQuery query = (SmsLogReportQuery) queryParameters
				.clone();
		final SmsLogReportVO report = smsLogService.getSmsLogReport(query);
		Map<SmsLogType, Map<SmsLogStatus, Integer>> totals = report.getTotals();
		Map<SmsLogType, Integer> totalsByType = report.getTotalsByType();
		Map<SmsLogStatus, Integer> totalsByStatus = report.getTotalsByStatus();
		int total = report.getTotal();
		List<SmsLog> smsLogs = report.getLogs();
		 response = new SmsLogsReportResponseDto(
				totals, totalsByType, totalsByStatus, total, smsLogs);}
            catch(Exception e){
                e.printStackTrace();
            }
		return response;
	}

	protected QueryParameters prepareForm(final ActionContext context) {
		final HttpServletRequest request = context.getRequest();
		final Administrator admin = elementService.load(context.getElement()
				.getId(), RelationshipHelper.nested(
				Element.Relationships.GROUP,
				AdminGroup.Relationships.MANAGES_GROUPS));
		request.setAttribute("memberGroups", admin.getAdminGroup()
				.getManagesGroups());
		RequestHelper.storeEnum(request, SmsLogType.class, "typesList");
		RequestHelper.storeEnum(request, SmsLogStatus.class, "statusList");
		RequestHelper.storeEnum(request, SmsMailingType.class, "mailingTypes");
		request.setAttribute("smsTypes", smsLogService.getSmsTypes());

		// Filter the message types which can never be delivered by sms
		final EnumSet<Message.Type> messageTypes = EnumSet
				.allOf(Message.Type.class);
		messageTypes.remove(Message.Type.FROM_MEMBER);
		messageTypes.remove(Message.Type.FROM_ADMIN_TO_GROUP);
		messageTypes.remove(Message.Type.FROM_ADMIN_TO_MEMBER);
		request.setAttribute("messagesTypes", messageTypes);

		final SmsLogsReportForm form = context.getForm();
		final SmsLogReportQuery query = getDataBinder().readFromString(
				form.getQuery());
		query.setReturnTotals(true);
		query.fetch(
				RelationshipHelper.nested(SmsLog.Relationships.TARGET_MEMBER,
						Element.Relationships.USER), RelationshipHelper.nested(
						SmsLog.Relationships.SMS_MAILING,
						SmsMailing.Relationships.BY));
		if (query.getMember() != null) {
			query.setMember((Member) elementService.load(query.getMember()
					.getId(), Element.Relationships.USER));
		}
		final Collection<MemberGroup> grps = groupService.load(EntityHelper
				.toIdsAsList(query.getMemberGroups()));
		query.setMemberGroups(grps);
		query.setSmsTypes(smsLogService.loadSmsTypes(EntityHelper
				.toIdsAsList(query.getSmsTypes())));
		return query;
	}

	private DataBinder<SmsLogReportQuery> getDataBinder() {
		if (dataBinder == null) {
			final LocalSettings settings = settingsService.getLocalSettings();
			dataBinder = getSmsLogReportQueryBinder(settings);
		}
		return dataBinder;
	}

}
