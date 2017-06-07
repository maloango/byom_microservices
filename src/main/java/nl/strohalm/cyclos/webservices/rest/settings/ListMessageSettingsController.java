package nl.strohalm.cyclos.webservices.rest.settings;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import nl.strohalm.cyclos.access.AdminSystemPermission;
import nl.strohalm.cyclos.controls.ActionContext;
import nl.strohalm.cyclos.entities.settings.MessageSettings.MessageSettingsEnum;
import nl.strohalm.cyclos.services.permissions.PermissionService;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;

import org.apache.struts.action.ActionForward;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class ListMessageSettingsController extends BaseRestController {
	private static final List<String> GENERAL;
	private static final List<String> MEMBER_NOTIFICATIONS;
	private static final List<String> ADMIN_NOTIFICATIONS;
	private PermissionService permissionService;
	static {
		final List<String> general = new ArrayList<String>();
		final List<String> member = new ArrayList<String>();
		final List<String> admin = new ArrayList<String>();

		for (final MessageSettingsEnum messageSetting : MessageSettingsEnum
				.values()) {
			switch (messageSetting.getCategory()) {
			case GENERAL:
				general.add(messageSetting.settingName());
				break;
			case MEMBER:
				member.add(messageSetting.settingName());
				break;
			case ADMIN:
				admin.add(messageSetting.settingName());
				break;
			default:
				throw new IllegalAccessError(
						"Unknown message setting category: "
								+ messageSetting.getCategory());
			}
		}

		GENERAL = Collections.unmodifiableList(general);
		MEMBER_NOTIFICATIONS = Collections.unmodifiableList(member);
		ADMIN_NOTIFICATIONS = Collections.unmodifiableList(admin);
	}

	public static class ListMessageSettingsRequestDto {

	}

	public static class ListMessageSettingsResponseDto {

		private boolean editable;
		private List<String> GENERAL;
		private List<String> MEMBER_NOTIFICATIONS;
		private List<String> ADMIN_NOTIFICATIONS;

		public ListMessageSettingsResponseDto(boolean editable,
				List<String> gENERAL, List<String> mEMBER_NOTIFICATIONS,
				List<String> aDMIN_NOTIFICATIONS) {
			super();
			this.editable = editable;
			GENERAL = gENERAL;
			MEMBER_NOTIFICATIONS = mEMBER_NOTIFICATIONS;
			ADMIN_NOTIFICATIONS = aDMIN_NOTIFICATIONS;
		}

	}

	@RequestMapping(value = "", method = RequestMethod.GET)
	@ResponseBody
	protected ListMessageSettingsResponseDto executeAction(
			@RequestBody ListMessageSettingsRequestDto context)
			throws Exception {
		// final HttpServletRequest request = context.getRequest();
		final boolean editable = permissionService
				.hasPermission(AdminSystemPermission.TRANSLATION_MANAGE_NOTIFICATION);
		ListMessageSettingsResponseDto response = new ListMessageSettingsResponseDto(
				editable, GENERAL, MEMBER_NOTIFICATIONS, ADMIN_NOTIFICATIONS);

		return response;
	}
}
