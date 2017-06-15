package nl.strohalm.cyclos.webservices.rest.image;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import nl.strohalm.cyclos.annotations.Inject;
import nl.strohalm.cyclos.controls.BaseAjaxAction.ContentType;
import nl.strohalm.cyclos.entities.ads.Ad;
import nl.strohalm.cyclos.entities.customization.images.AdImage;
import nl.strohalm.cyclos.entities.customization.images.Image;
import nl.strohalm.cyclos.entities.customization.images.MemberImage;
import nl.strohalm.cyclos.services.customization.ImageService;
import nl.strohalm.cyclos.servlets.ImageByIdServlet;
import nl.strohalm.cyclos.utils.RelationshipHelper;
import nl.strohalm.cyclos.utils.customizedfile.CustomizedFileHandler;
import nl.strohalm.cyclos.utils.validation.ValidationException;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;
@Controller
public class RemoveImageController extends BaseRestController {
	private ImageService          imageService;
    private CustomizedFileHandler customizedFileHandler;

    @Inject
    public void setCustomizedFileHandler(final CustomizedFileHandler customizedFileHandler) {
        this.customizedFileHandler = customizedFileHandler;
    }

    @Inject
    public void setImageService(final ImageService imageService) {
        this.imageService = imageService;
    }

    //@Override
    protected ContentType contentType() {
        return ContentType.TEXT;
    }
    
    public static class RemoveImageRequestDTO{
    	private long              id;

        public long getId() {
            return id;
        }

        public void setId(final long id) {
            this.id = id;
        }
    	
    }
    
    public static class RemoveImageResponseDTO{
    	String message;

		public final String getMessage() {
			return message;
		}

		public final void setMessage(String message) {
			this.message = message;
		}
    	
    }

    @RequestMapping(value="admin/removeImage",method=RequestMethod.DELETE)
    @ResponseBody
    protected RemoveImageResponseDTO renderContent(@RequestBody RemoveImageRequestDTO form) throws Exception {
        final Long id = form.getId();
        if (id <= 0) {
            throw new ValidationException();
        }
        final Image image = imageService.load(id, MemberImage.Relationships.MEMBER, RelationshipHelper.nested(AdImage.Relationships.AD, Ad.Relationships.OWNER));

        // Call the correct method
        switch (image.getNature()) {
            case AD:
                imageService.remove(id);
                break;
            case MEMBER:
                imageService.remove(id);
                break;
            default:
                throw new ValidationException();
        }
        
        // Remove from cache (will do nothing if cache is not used)
        customizedFileHandler.delete(ImageByIdServlet.IMAGES_CACHE_PATH + "/" + id);
        customizedFileHandler.delete(ImageByIdServlet.THUMBNAILS_CACHE_PATH + "/" + id);
        RemoveImageResponseDTO reponse = new RemoveImageResponseDTO();
        return reponse;
    }


}
