package nl.strohalm.cyclos.webservices.rest.customization.themes;

import org.apache.struts.upload.FormFile;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import nl.strohalm.cyclos.annotations.Inject;
import nl.strohalm.cyclos.themes.ThemeHandler;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;

@Controller
public class ImportThemeController extends BaseRestController {
	private ThemeHandler themeHandler;

	@Inject
	public void setThemeHandler(final ThemeHandler themeHandler) {
		this.themeHandler = themeHandler;
	}

	public static class ImportThemeRequestDTO {
		private FormFile upload;

		public FormFile getUpload() {
			return upload;
		}

		public void setUpload(final FormFile upload) {
			this.upload = upload;
		}
	}

	public static class ImportThemeResponseDTO {

		public final String getMessage() {
			return message;
		}

		String message;

		public final void setMessage(String message) {
			this.message = message;
		}
                public ImportThemeResponseDTO(){
                }

	}

	@RequestMapping(value = "admin/importTheme", method = RequestMethod.POST)
	@ResponseBody
	protected ImportThemeResponseDTO executeAction(@RequestBody ImportThemeRequestDTO form) throws Exception {
		final FormFile upload = form.getUpload();
		String message = null;
		ImportThemeResponseDTO response = new ImportThemeResponseDTO();
		if (upload != null && upload.getFileSize() > 0) {
			try {
				themeHandler.importNew(upload.getFileName(), upload.getInputStream());

				message = "theme.import.successful";
				response.setMessage(message);
				return response;
			} catch (final Exception e) {
				message = "theme.import.error.reading";
				response.setMessage(message);
				return response;
			} finally {
				upload.destroy();
			}
		}
		return response;
	}

}
