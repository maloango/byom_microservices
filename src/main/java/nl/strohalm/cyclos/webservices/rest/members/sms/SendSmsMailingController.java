package nl.strohalm.cyclos.webservices.rest.members.sms;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import nl.strohalm.cyclos.access.AdminMemberPermission;
import nl.strohalm.cyclos.access.BrokerPermission;
import nl.strohalm.cyclos.access.Permission;
import nl.strohalm.cyclos.annotations.Inject;
import nl.strohalm.cyclos.controls.ActionContext;
//import nl.strohalm.cyclos.controls.members.sms.SendSmsMailingForm;
import nl.strohalm.cyclos.entities.access.AdminUser;
import nl.strohalm.cyclos.entities.access.User;
import nl.strohalm.cyclos.entities.groups.AdminGroup;
import nl.strohalm.cyclos.entities.groups.GroupQuery;
import nl.strohalm.cyclos.entities.groups.MemberGroup;
import nl.strohalm.cyclos.entities.members.Member;
import nl.strohalm.cyclos.entities.sms.SmsMailing;
import nl.strohalm.cyclos.exceptions.PermissionDeniedException;
import nl.strohalm.cyclos.services.elements.ElementService;
import nl.strohalm.cyclos.services.groups.GroupService;
import nl.strohalm.cyclos.services.permissions.PermissionService;
import nl.strohalm.cyclos.services.settings.SettingsService;
import nl.strohalm.cyclos.services.sms.SmsMailingService;
import nl.strohalm.cyclos.utils.binding.BeanBinder;
import nl.strohalm.cyclos.utils.binding.DataBinder;
import nl.strohalm.cyclos.utils.binding.PropertyBinder;
import nl.strohalm.cyclos.utils.binding.SimpleCollectionBinder;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class SendSmsMailingController extends BaseRestController {

	private DataBinder<SmsMailing> dataBinder;
	private SmsMailingService smsMailingService;
	private GroupService groupService;
	private ElementService elementService;
	private PermissionService permissionService;
	private SettingsService settingsService;

	@Inject
	public void setSmsMailingService(final SmsMailingService smsMailingService) {
		this.smsMailingService = smsMailingService;
	}

	public static class SendSmsMailingRequestDto {
		private boolean isSingleMember;
		protected Map<String, Object> values;

		public Map<String, Object> getValues() {
			return values;
		}

		public void setValues(final Map<String, Object> values) {
			this.values = values;
		}

		public Map<String, Object> getSmsMailing() {
			return values;
		}

		public Object getSmsMailing(final String key) {
			return values.get(key);
		}

		public boolean isSingleMember() {
			return isSingleMember;
		}

		public void setSingleMember(final boolean isSingleMember) {
			this.isSingleMember = isSingleMember;
		}

		public void setSmsMailing(final Map<String, Object> values) {
			this.values = values;
		}

		public void setSmsMailing(final String key, final Object value) {
			values.put(key, value);
		}

		User user;

		public boolean isAdmin() {
			return user instanceof AdminUser;
		}

	}

	public static class SendSmsMailingResponseDto {
		private String message;

		public String getMessage() {
			return message;
		}

		public void setMessage(String message) {
			this.message = message;
		}
	}

	@RequestMapping(value = "member/sendSmsMailing", method = RequestMethod.POST)
	@ResponseBody
	protected SendSmsMailingResponseDto formAction(
			@RequestBody SendSmsMailingRequestDto form) throws Exception {
		SendSmsMailingResponseDto response =null;
                try{
		final SmsMailing smsMailing = getDataBinder().readFromString(
				form.getSmsMailing());
		Permission permission;
		String message = null;
		if (form.isAdmin()) {
			permission = smsMailing.isFree() ? AdminMemberPermission.SMS_MAILINGS_FREE_SMS_MAILINGS
					: AdminMemberPermission.SMS_MAILINGS_PAID_SMS_MAILINGS;
		} else {
			permission = smsMailing.isFree() ? BrokerPermission.SMS_MAILINGS_FREE_SMS_MAILINGS
					: BrokerPermission.SMS_MAILINGS_PAID_SMS_MAILINGS;
		}

		if (!permissionService.hasPermission(permission)) {
			throw new PermissionDeniedException();
		}

		smsMailingService.send(smsMailing);

		message = "smsMailing.sent";
		response = new SendSmsMailingResponseDto();
		response.setMessage(message);}
                catch(Exception e){
                    e.printStackTrace();
                }
		return response;
	}

//	protected void prepareForm(final ActionContext context) throws Exception {
//		final HttpServletRequest request = context.getRequest();
//		final SendSmsMailingForm form = context.getForm();
//
//		boolean canSendFree;
//		boolean canSendPaid;
//		if (context.isAdmin()) {
//			canSendFree = permissionService
//					.hasPermission(AdminMemberPermission.SMS_MAILINGS_FREE_SMS_MAILINGS);
//			canSendPaid = permissionService
//					.hasPermission(AdminMemberPermission.SMS_MAILINGS_PAID_SMS_MAILINGS);
//
//			final GroupQuery query = new GroupQuery();
//			query.setManagedBy((AdminGroup) context.getGroup());
//			query.setOnlyActive(true);
//			request.setAttribute("groups", groupService.search(query));
//		} else {
//			canSendFree = permissionService
//					.hasPermission(BrokerPermission.SMS_MAILINGS_FREE_SMS_MAILINGS);
//			canSendPaid = permissionService
//					.hasPermission(BrokerPermission.SMS_MAILINGS_PAID_SMS_MAILINGS);
//		}
//
//		request.setAttribute("canSendFree", canSendFree);
//		request.setAttribute("canSendPaid", canSendPaid);
//		if (canSendFree && canSendPaid) {
//			form.setSmsMailing("free", "true");
//		}
//	}
//
//	protected void validateForm(final ActionContext context) {
//		final SendSmsMailingForm form = context.getForm();
//		final SmsMailing smsMailing = getDataBinder().readFromString(
//				form.getSmsMailing());
//		smsMailingService.validate(smsMailing, form.isSingleMember());
//	}

	private DataBinder<SmsMailing> getDataBinder() {
		if (dataBinder == null) {
			final BeanBinder<SmsMailing> binder = BeanBinder
					.instance(SmsMailing.class);
			binder.registerBinder("free",
					PropertyBinder.instance(Boolean.TYPE, "free"));
			binder.registerBinder("text",
					PropertyBinder.instance(String.class, "text"));
			binder.registerBinder("member",
					PropertyBinder.instance(Member.class, "member"));
			binder.registerBinder("groups", SimpleCollectionBinder.instance(
					MemberGroup.class, "groups"));
			dataBinder = binder;
		}
		return dataBinder;
	}
}
