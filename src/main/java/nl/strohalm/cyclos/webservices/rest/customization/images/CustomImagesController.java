package nl.strohalm.cyclos.webservices.rest.customization.images;

import org.apache.struts.upload.FormFile;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import nl.strohalm.cyclos.annotations.Inject;
import nl.strohalm.cyclos.entities.customization.images.Image;
import nl.strohalm.cyclos.entities.customization.images.Image.Nature;
import nl.strohalm.cyclos.services.customization.ImageService;
import nl.strohalm.cyclos.utils.ImageHelper.ImageType;
import nl.strohalm.cyclos.utils.WebImageHelper;
import nl.strohalm.cyclos.utils.validation.ValidationException;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;
import org.springframework.web.bind.annotation.PathVariable;

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
                public FormFile upload;

        public FormFile getUpload() {
            return upload;
        }

        public void setUpload(FormFile upload) {
            this.upload = upload;
        }

        public Nature getNature() {
            return nature;
        }

        public void setNature(Nature nature) {
            this.nature = nature;
        }
		

		public String getMessage() {
			return message;
		}

		public void setMessage(String message) {
			this.message = message;
		}
                public CustomImagesResponseDto(){
                }
	}

	@RequestMapping(value = "admin/customImages/{nature}", method = RequestMethod.GET)
	@ResponseBody
	protected CustomImagesResponseDto handleSubmit(@PathVariable ("nature") Nature nature) throws Exception {
			
               
		FormFile upload = null;
		CustomImagesResponseDto response = new CustomImagesResponseDto();
		try {
			try {
				nature = Image.Nature.valueOf(response.getNature());
			} catch (final Exception e) {
				throw new ValidationException();
			}
			if (nature != Image.Nature.CUSTOM && nature != Image.Nature.STYLE) {
				throw new ValidationException();
			}

			upload = response.getUpload();
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
