package nl.strohalm.cyclos.webservices.rest.settings;

import java.util.Collection;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import nl.strohalm.cyclos.annotations.Inject;
import nl.strohalm.cyclos.controls.ActionContext;
//import nl.strohalm.cyclos.controls.BaseAjaxAction.ContentType;
//import nl.strohalm.cyclos.controls.settings.ManageSettingsForm;
import nl.strohalm.cyclos.entities.settings.Setting;
import nl.strohalm.cyclos.services.settings.SettingsService;
import nl.strohalm.cyclos.utils.ResponseHelper;
import nl.strohalm.cyclos.utils.conversion.CoercionHelper;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;

import org.apache.struts.action.ActionForward;
import org.apache.struts.upload.FormFile;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class ExportSettingsController extends BaseRestController {
	protected ResponseHelper responseHelper;
	private SettingsService settingsService;

	@Inject
	public void setResponseHelper(final ResponseHelper responseHelper) {
		this.responseHelper = responseHelper;
	}

	public static class ExportSettingsRequestDto {
		private FormFile upload;
		private String[] type;

		public String[] getType() {
			return type;
		}

		public FormFile getUpload() {
			return upload;
		}

		public void setType(final String[] type) {
			this.type = type;
		}

		public void setUpload(final FormFile upload) {
			this.upload = upload;
		}
	}

	public static class ExportSettingsResponseDto {
		private String message;

		public String getMessage() {
			return message;
		}

		public void setMessage(String message) {
			this.message = message;
		}
	}

	@RequestMapping(value = "admin/exportSettings", method = RequestMethod.POST)
	@ResponseBody
	protected ExportSettingsResponseDto executeAction(
			final ExportSettingsRequestDto form) throws Exception {
		// final ManageSettingsForm form = context.getForm();
		final Collection<Setting.Type> types = CoercionHelper.coerceCollection(
				Setting.Type.class, List.class, form.getType());

		final String xml = settingsService.exportToXml(types);
		
		return null;
	}

}
