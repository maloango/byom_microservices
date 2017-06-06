package nl.strohalm.cyclos.webservices.rest.customization.images;

import nl.strohalm.cyclos.annotations.Inject;
import nl.strohalm.cyclos.entities.customization.images.Image;
import nl.strohalm.cyclos.entities.customization.images.Image.Nature;
import nl.strohalm.cyclos.services.customization.ImageService;
import nl.strohalm.cyclos.utils.ActionHelper;
import nl.strohalm.cyclos.utils.ImageHelper.ImageType;
import nl.strohalm.cyclos.utils.WebImageHelper;
import nl.strohalm.cyclos.utils.validation.ValidationException;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;

import org.apache.struts.upload.FormFile;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class CustomImagesController extends BaseRestController {

	private ImageService imageService;
	private WebImageHelper webImageHelper;

	public ImageService getImageService() {
		return imageService;
	}

	@Inject
	public void setImageService(final ImageService imageService) {
		this.imageService = imageService;
	}

	@Inject
	public void setWebImageHelper(final WebImageHelper webImageHelper) {
		this.webImageHelper = webImageHelper;
	}

	public static class CustomImagesRequestDto {
		private String nature;
		private FormFile upload;

		public String getNature() {
			return nature;
		}

		public FormFile getUpload() {
			return upload;
		}

		public void setNature(final String nature) {
			this.nature = nature;
		}

		public void setUpload(final FormFile upload) {
			this.upload = upload;
		}
	}

	public static class CustomImagesResponseDto {
		public String message;
		public Nature nature;
		

		public String getMessage() {
			return message;
		}

		public void setMessage(String message) {
			this.message = message;
		}
	}

	@RequestMapping(value = "/admin/customImages", method = RequestMethod.POST)
	@ResponseBody
	protected CustomImagesResponseDto handleSubmit(
			@RequestBody CustomImagesRequestDto form) throws Exception {
		//final CustomImagesForm form = context.getForm();
		Image.Nature nature = null;
		FormFile upload = null;
		CustomImagesResponseDto response = new CustomImagesResponseDto();
		try {
			try {
				nature = Image.Nature.valueOf(form.getNature());
			} catch (final Exception e) {
				throw new ValidationException();
			}
			if (nature != Image.Nature.CUSTOM && nature != Image.Nature.STYLE) {
				throw new ValidationException();
			}

			upload = form.getUpload();
			final ImageType type = ImageType.getByContentType(upload
					.getContentType());
			final Image image = imageService.save(nature, type,
					upload.getFileName(), upload.getInputStream());

			// Update the physical file
			webImageHelper.update(image);

			response.setMessage("customImage.uploaded");
		} finally {
			try {
				upload.destroy();
			} catch (final Exception e) {
				// Ignore
			}
		}
		return response;
	}

}
