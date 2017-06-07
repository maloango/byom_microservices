package nl.strohalm.cyclos.webservices.rest.members.sms;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import nl.strohalm.cyclos.access.AdminMemberPermission;
import nl.strohalm.cyclos.access.BrokerPermission;
import nl.strohalm.cyclos.annotations.Inject;
import nl.strohalm.cyclos.controls.ActionContext;
import nl.strohalm.cyclos.controls.members.sms.SearchSmsMailingsForm;
import nl.strohalm.cyclos.entities.groups.AdminGroup;
import nl.strohalm.cyclos.entities.groups.GroupQuery;
import nl.strohalm.cyclos.entities.groups.MemberGroup;
import nl.strohalm.cyclos.entities.members.Element;
import nl.strohalm.cyclos.entities.members.Member;
import nl.strohalm.cyclos.entities.settings.LocalSettings;
import nl.strohalm.cyclos.entities.sms.SmsLog;
import nl.strohalm.cyclos.entities.sms.SmsMailing;
import nl.strohalm.cyclos.entities.sms.SmsMailingQuery;
import nl.strohalm.cyclos.services.elements.ElementService;
import nl.strohalm.cyclos.services.groups.GroupService;
import nl.strohalm.cyclos.services.permissions.PermissionService;
import nl.strohalm.cyclos.services.settings.SettingsService;
import nl.strohalm.cyclos.services.sms.SmsMailingService;
import nl.strohalm.cyclos.utils.RelationshipHelper;
import nl.strohalm.cyclos.utils.binding.BeanBinder;
import nl.strohalm.cyclos.utils.binding.DataBinder;
import nl.strohalm.cyclos.utils.binding.DataBinderHelper;
import nl.strohalm.cyclos.utils.binding.PropertyBinder;
import nl.strohalm.cyclos.utils.query.QueryParameters;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class SearchSmsMailingsController extends BaseRestController {
	private DataBinder<SmsMailingQuery> dataBinder;
	private SmsMailingService smsMailingService;
	private GroupService groupService;
	private ElementService elementService;
	private PermissionService permissionService;
	private SettingsService settingsService;

	@Inject
	public void setSmsMailingService(final SmsMailingService smsMailingService) {
		this.smsMailingService = smsMailingService;
	}

	public static class SearchSmsMailingsRequestDto {

	}

	public static class SearchSmsMailingsResponseDto {
		private List<SmsMailing> smsMailings;

		public List<SmsMailing> getSmsMailings() {
			return smsMailings;
		}

		public void setSmsMailings(List<SmsMailing> smsMailings) {
			this.smsMailings = smsMailings;
		}

		public SearchSmsMailingsResponseDto(List<SmsMailing> smsMailings) {
			super();
			this.smsMailings = smsMailings;
		}

	}

	@RequestMapping(value = "/admin/managePasswords", method = RequestMethod.POST)
	@ResponseBody
	protected SearchSmsMailingsResponseDto executeQuery(
			@RequestBody SearchSmsMailingsRequestDto form,
			final QueryParameters queryParameters) {
		final SmsMailingQuery query = (SmsMailingQuery) queryParameters;
		query.fetch(SmsMailing.Relationships.BY,
				SmsMailing.Relationships.GROUPS);
		final List<SmsMailing> smsMailings = smsMailingService.search(query);
		// final HttpServletRequest request = context.getRequest();
		SearchSmsMailingsResponseDto response = new SearchSmsMailingsResponseDto(
				smsMailings);
		return response;
	}

	protected QueryParameters prepareForm(final ActionContext context) {
		final SearchSmsMailingsForm form = context.getForm();
		final HttpServletRequest request = context.getRequest();
		final SmsMailingQuery query = getDataBinder().readFromString(
				form.getQuery());

		boolean viewFree;
		boolean viewPaid;
		boolean canSend;
		if (context.isAdmin()) {
			viewPaid = viewFree = permissionService
					.hasPermission(AdminMemberPermission.SMS_MAILINGS_VIEW); // 2
																				// assignments
			canSend = permissionService
					.hasPermission(AdminMemberPermission.SMS_MAILINGS_FREE_SMS_MAILINGS)
					|| permissionService
							.hasPermission(AdminMemberPermission.SMS_MAILINGS_PAID_SMS_MAILINGS);

			final GroupQuery groupQuery = new GroupQuery();
			groupQuery.setManagedBy((AdminGroup) context.getGroup());
			groupQuery.setOnlyActive(true);
			request.setAttribute("groups", groupService.search(groupQuery));
		} else {
			viewFree = permissionService
					.hasPermission(BrokerPermission.SMS_MAILINGS_FREE_SMS_MAILINGS);
			viewPaid = permissionService
					.hasPermission(BrokerPermission.SMS_MAILINGS_PAID_SMS_MAILINGS);
			canSend = viewFree || viewPaid; // At least one permission (free /
											// paid) the broker has
		}
		// Ensure to fetch the member, so the name / username will be displayed,
		// if one is selected
		if (query.getMember() != null) {
			query.setMember((Member) elementService.load(query.getMember()
					.getId(), Element.Relationships.USER));
		}
		request.setAttribute("viewFree", viewFree);
		request.setAttribute("viewPaid", viewPaid);
		request.setAttribute("canSend", canSend);
		query.fetch(RelationshipHelper.nested(SmsMailing.Relationships.BY,
				Element.Relationships.GROUP));
		return query;
	}

	protected boolean willExecuteQuery(final ActionContext context,
			final QueryParameters queryParameters) throws Exception {
		return true;
	}

	private DataBinder<SmsMailingQuery> getDataBinder() {
		if (dataBinder == null) {
			final LocalSettings settings = settingsService.getLocalSettings();
			final BeanBinder<SmsMailingQuery> binder = BeanBinder
					.instance(SmsMailingQuery.class);
			binder.registerBinder("period",
					DataBinderHelper.periodBinder(settings, "period"));
			binder.registerBinder("pageParameters",
					DataBinderHelper.pageBinder());
			binder.registerBinder("recipient", PropertyBinder.instance(
					SmsMailingQuery.Recipient.class, "recipient"));
			binder.registerBinder("group",
					PropertyBinder.instance(MemberGroup.class, "group"));
			binder.registerBinder("member",
					PropertyBinder.instance(Member.class, "member"));
			dataBinder = binder;
		}
		return dataBinder;
	}

}
