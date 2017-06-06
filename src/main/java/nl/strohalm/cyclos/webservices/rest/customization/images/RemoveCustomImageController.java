package nl.strohalm.cyclos.webservices.rest.customization.images;

import javax.servlet.http.HttpServletRequest;

import nl.strohalm.cyclos.annotations.Inject;
import nl.strohalm.cyclos.controls.customization.images.RemoveCustomImageForm;
import nl.strohalm.cyclos.entities.customization.images.Image;
import nl.strohalm.cyclos.entities.customization.images.Image.Nature;
import nl.strohalm.cyclos.services.customization.ImageService;
import nl.strohalm.cyclos.utils.ActionHelper;
import nl.strohalm.cyclos.utils.StringHelper;
import nl.strohalm.cyclos.utils.WebImageHelper;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class RemoveCustomImageController extends BaseRestController {
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

	public static class RemoveCustomImageRequestDto {
		private long imageId;

		public long getImageId() {
			return imageId;
		}

		public void setImageId(final long imageId) {
			this.imageId = imageId;
		}
	}

	public static class RemoveCustomImageResponseDto {
		public String message;

		public String getMessage() {
			return message;
		}

		public void setMessage(String message) {
			this.message = message;
		}
	}

	@RequestMapping(value = "/admin/removeCustomImage", method = RequestMethod.POST)
	@ResponseBody
	protected RemoveCustomImageResponseDto executeAction(
			@RequestBody RemoveCustomImageRequestDto form) throws Exception {
		// final RemoveCustomImageForm form = context.getForm();
		final long id = form.getImageId();
		final Image image = imageService.load(id);
		imageService.remove(image.getId());
		RemoveCustomImageResponseDto response = new RemoveCustomImageResponseDto();
		response.setMessage("customImage.removed");

		// Remove the local file if a custom or style image
		if (image.getNature() != Nature.SYSTEM) {
			webImageHelper.remove(image);
		}

		// final HttpServletRequest request = context.getRequest();
		/*
		 * return ActionHelper.redirectWithParam(request,
		 * context.getSuccessForward(), "nature",
		 * StringHelper.removeMarkupTags(request.getParameter("nature")));
		 */
		return response;
	}
}
