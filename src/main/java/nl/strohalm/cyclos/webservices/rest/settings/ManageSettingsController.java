package nl.strohalm.cyclos.webservices.rest.settings;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import nl.strohalm.cyclos.entities.settings.Setting;
import nl.strohalm.cyclos.entities.settings.Setting.Type;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;

@Controller
public class ManageSettingsController extends BaseRestController {
	public static enum Action {
		IMPORT, EXPORT;
	}

	public static class ManageSettingsRequestDto {

	}

	public static class ManageSettingsResponseDto {
		private Setting.Type[] settingTypes;

		public ManageSettingsResponseDto(Type[] settingTypes) {
			super();
			this.settingTypes = settingTypes;
		}

	}

	@RequestMapping(value = "/admin/managePasswords", method = RequestMethod.POST)
	@ResponseBody
	protected ManageSettingsResponseDto executeAction(
			@RequestBody ManageSettingsRequestDto form) throws Exception {
		final Setting.Type[] settingTypes = { Setting.Type.ACCESS,
				Setting.Type.ALERT, Setting.Type.LOCAL, Setting.Type.LOG,
				Setting.Type.MAIL };
		// final HttpServletRequest request = context.getRequest();
		ManageSettingsResponseDto response = new ManageSettingsResponseDto(
				settingTypes);
		// RequestHelper.storeEnum(request, Action.class, "actions");
		return response;
	}
}
