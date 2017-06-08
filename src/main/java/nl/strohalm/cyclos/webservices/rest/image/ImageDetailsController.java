package nl.strohalm.cyclos.webservices.rest.image;

import java.io.PrintWriter;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForward;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import nl.strohalm.cyclos.controls.ActionContext;
import nl.strohalm.cyclos.controls.image.ImageDetailsForm;
import nl.strohalm.cyclos.controls.restapi.BaseRestController;
import nl.strohalm.cyclos.entities.Entity;
import nl.strohalm.cyclos.entities.customization.images.Image;
import nl.strohalm.cyclos.entities.customization.images.ImageCaptionDTO;
import nl.strohalm.cyclos.entities.customization.images.ImageDetailsDTO;
import nl.strohalm.cyclos.entities.customization.images.OwneredImage;
import nl.strohalm.cyclos.services.customization.ImageService;
import nl.strohalm.cyclos.utils.EntityHelper;
import nl.strohalm.cyclos.utils.binding.BeanBinder;
import nl.strohalm.cyclos.utils.binding.BeanCollectionBinder;
import nl.strohalm.cyclos.utils.binding.DataBinder;
import nl.strohalm.cyclos.utils.binding.MapBean;
import nl.strohalm.cyclos.utils.binding.PropertyBinder;
import nl.strohalm.cyclos.utils.validation.ValidationException;
@Controller
public class ImageDetailsController extends BaseRestController{
	private ImageService imageService;

	 public final ImageService getImageService() {
		return imageService;
	}

	public final void setImageService(ImageService imageService) {
		this.imageService = imageService;
	}
	
	public static class ImageDetailsRequestDTO{
		private Map<String,Object> values;
		public Map<String,Object>getvalues;
		private Image.Nature          nature;
	    private Entity                owner;
	    private List<ImageCaptionDTO> details;

	    public List<ImageCaptionDTO> getDetails() {
	        return details;
	    }

	    public Entity getImageOwner() {
	        return EntityHelper.reference(nature.getOwnerType(), owner.getId());
	    }

	    public Image.Nature getNature() {
	        return nature;
	    }

	    public Entity getOwner() {
	        return owner;
	    }

	    public void setDetails(final List<ImageCaptionDTO> details) {
	        this.details = details;
	    }

	    public void setNature(final Image.Nature nature) {
	        this.nature = nature;
	    }

	    public void setOwner(final Entity owner) {
	        this.owner = owner;
	    }
		public void ImageDetailsForm() {
	        setImages("details", new MapBean(true, "id", "caption"));
	    }

	    public Map<String, Object> getImages() {
	        return values;
	    }

	    public Object getImages(final String key) {
	        return values.get(key);
	    }

	    public void setImages(final Map<String, Object> map) {
	        values = map;
	    }

	    public void setImages(final String key, final Object value) {
	        values.put(key, value);
	    }
	}
	
	public static class ImageDetailsResponseDTO{
		
		
	}
	

	@RequestMapping(value = " ", method = RequestMethod.GET)
	@ResponseBody
	    protected ImageDetailsResponseDTO handleSubmit(final ActionContext context) throws Exception {
	        final ImageDetailsForm form = context.getForm();
	        final ImageDetailsDTO details = getDataBinder().readFromString(form.getImages());
	        if (details.getNature() == null || details.getOwner() == null) {
	            throw new ValidationException();
	        }

	        boolean success;
	        try {
	            imageService.saveDetails(details);
	            success = true;
	        } catch (final Exception e) {
	            success = false;
	        }

	        // Return a JavaScript message to the opener window
	        final HttpServletResponse response = context.getResponse();
	        response.setContentType("text/html");
	        final PrintWriter out = response.getWriter();
	        out.print("<html><script>");
	        if (success) {
	            out.print("window.opener.imageContainer.handleImageDetailsSuccess(");
	            out.print(getDetailCollectionBinder().readAsString(details.getDetails()));
	            out.print(");\n");
	        } else {
	            out.print("window.opener.imageContainer.handleImageDetailsError();\n");
	            out.print("history.back();");
	        }
	        out.print("</script></html>");
	        return null;
	    }

	   // @Override
	    protected void prepareForm(final ActionContext context) throws Exception {
	        final ImageDetailsForm form = context.getForm();
	        final ImageDetailsDTO details = getDataBinder().readFromString(form.getImages());
	        Entity owner;
	        try {
	            owner = details.getImageOwner();
	        } catch (final Exception e) {
	            owner = null;
	        }
	        if (owner == null) {
	            throw new ValidationException();
	        }
	        final List<? extends OwneredImage> images = imageService.listByOwner(owner);
	        context.getRequest().setAttribute("images", images);
	    }

	    private DataBinder<ImageDetailsDTO> getDataBinder() {
	        if (dataBinder == null) {
	            final BeanBinder<ImageDetailsDTO> binder = BeanBinder.instance(ImageDetailsDTO.class);
	            binder.registerBinder("nature", PropertyBinder.instance(Image.Nature.class, "nature"));
	            binder.registerBinder("owner", PropertyBinder.instance(Entity.class, "owner"));
	            binder.registerBinder("details", BeanCollectionBinder.instance(getDetailBinder(), "details"));

	            dataBinder = binder;
	        }
	        return dataBinder;
	    }

	    private BeanBinder<ImageCaptionDTO> getDetailBinder() {
	        final BeanBinder<ImageCaptionDTO> detailBinder = BeanBinder.instance(ImageCaptionDTO.class);
	        detailBinder.registerBinder("id", PropertyBinder.instance(Long.class, "id"));
	        detailBinder.registerBinder("caption", PropertyBinder.instance(String.class, "caption"));
	        return detailBinder;
	    }
	
	
}
