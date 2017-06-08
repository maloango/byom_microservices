package nl.strohalm.cyclos.webservices.rest.customization.images;

import nl.strohalm.cyclos.annotations.Inject;
import nl.strohalm.cyclos.controls.customization.images.SystemImagesForm;
import nl.strohalm.cyclos.entities.customization.images.Image;
import nl.strohalm.cyclos.entities.customization.images.SystemImage;
import nl.strohalm.cyclos.services.customization.ImageService;
import nl.strohalm.cyclos.utils.ImageHelper.ImageType;
import nl.strohalm.cyclos.utils.WebImageHelper;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;

import org.apache.commons.lang.StringUtils;
import org.apache.struts.upload.FormFile;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class SystemImagesController extends BaseRestController {
	public static class SystemImageVO implements Comparable<SystemImageVO> {
		private final String label;
		private final SystemImage image;

		public SystemImageVO(final String label, final SystemImage image) {
			this.label = label;
			this.image = image;
		}

		@Override
		public int compareTo(final SystemImageVO other) {
			if (other == null || other.label == null) {
				return 1;
			}
			return label.compareTo(other.label);
		}

		public SystemImage getImage() {
			return image;
		}

		public String getLabel() {
			return label;
		}
	}

	private ImageService imageService;
	private WebImageHelper webImageHelper;

	@Inject
	public void setImageService(final ImageService imageService) {
		this.imageService = imageService;
	}

	@Inject
	public void setWebImageHelper(final WebImageHelper webImageHelper) {
		this.webImageHelper = webImageHelper;
	}

	public static class SystemImagesRequestDto {
		private FormFile upload;
		private String name;

		public String getName() {
			return name;
		}

		public FormFile getUpload() {
			return upload;
		}

		public void setName(final String name) {
			this.name = name;
		}

		public void setUpload(final FormFile upload) {
			this.upload = upload;
		}
	}

	public static class SystemImagesResponseDto {
		public String message;

		public String getMessage() {
			return message;
		}

		public void setMessage(String message) {
			this.message = message;
		}
	}

	@RequestMapping(value = "/admin/systemImages", method = RequestMethod.PUT)
	@ResponseBody
	protected SystemImagesResponseDto formAction(
			@RequestBody SystemImagesRequestDto form) throws Exception {
		// final SystemImagesForm form = context.getForm();
		FormFile upload = null;
		SystemImagesResponseDto response = new SystemImagesResponseDto();
		try {
			upload = form.getUpload();
			final ImageType type = ImageType.getByContentType(upload
					.getContentType());
			final String name = StringUtils.trimToEmpty(form.getName());
			final Image image = imageService.save(Image.Nature.SYSTEM, type,
					name, upload.getInputStream());

			// Update context elements / cache
			webImageHelper.update(image);

			response.setMessage("customImage.uploaded");
			return response;
		} finally {
			try {
				upload.destroy();
			} catch (final Exception e) {
				// Ignore
			}
		}
	}

}