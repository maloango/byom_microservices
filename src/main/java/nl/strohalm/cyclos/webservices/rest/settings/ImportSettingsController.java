package nl.strohalm.cyclos.webservices.rest.settings;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import nl.strohalm.cyclos.controls.ActionContext;
import nl.strohalm.cyclos.controls.settings.ManageSettingsForm;
import nl.strohalm.cyclos.entities.settings.LocalSettings;
import nl.strohalm.cyclos.entities.settings.Setting;
import nl.strohalm.cyclos.entities.settings.Setting.Type;
import nl.strohalm.cyclos.exceptions.PermissionDeniedException;
import nl.strohalm.cyclos.services.settings.SettingsService;
import nl.strohalm.cyclos.services.settings.exceptions.SelectedSettingTypeNotInFileException;
import nl.strohalm.cyclos.utils.conversion.CoercionHelper;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.struts.action.ActionForward;
import org.apache.struts.upload.FormFile;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@SuppressWarnings("unchecked")
public class ImportSettingsController extends BaseRestController {
	private SettingsService settingsService;

	public static class ImportSettingsRequestDto {
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

/*	public static class ImportSettingsResponseDto {
		private String message;

		public String getMessage() {
			return message;
		}

		public void setMessage(String message) {
			this.message = message;
		}
	}

	@RequestMapping(value = "/admin/managePasswords", method = RequestMethod.POST)
	@ResponseBody
	protected ImportSettingsResponseDto executeAction(@RequestBody ImportSettingsRequestDto form)
			throws Exception {
		//final ManageSettingsForm form = context.getForm();
		final LocalSettings localSettings = settingsService.getLocalSettings();
		final FormFile upload = form.getUpload();
		final Collection<Setting.Type> types = CoercionHelper.coerceCollection(
				Setting.Type.class, List.class, form.getType());
		try {
			final List<String> lines = IOUtils.readLines(
					upload.getInputStream(), localSettings.getCharset());
			final String xml = StringUtils.join(lines.iterator(), '\n');
			settingsService.importFromXml(xml, types);
			context.sendMessage("settings.imported");
		} catch (final PermissionDeniedException e) {
			// Rethrow when permission denied
			throw e;
		} catch (final SelectedSettingTypeNotInFileException e) {
			final List<Type> notImportedTypes = e.getNotImportedTypes();
			final List<String> names = new ArrayList<String>();
			for (final Type type : notImportedTypes) {
				names.add(context.message("settings.type." + type.name()));
			}
			context.sendMessage("settings.error.selectedSettingTypeNotInFile",
					StringUtils.join(names.iterator(), "\n"));
		} catch (final Exception e) {
			context.sendMessage("settings.error.importing");
		} finally {
			upload.destroy();
		}
		if (types.contains(Setting.Type.MAIL_TRANSLATION)
				|| types.contains(Setting.Type.MESSAGE)) {
			return context.findForward("manageTranslationMessages");
		} else {
			return context.getSuccessForward();
		}
	}

*/
}