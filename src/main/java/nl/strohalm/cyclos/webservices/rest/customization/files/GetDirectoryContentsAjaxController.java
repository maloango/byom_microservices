package nl.strohalm.cyclos.webservices.rest.customization.files;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import nl.strohalm.cyclos.annotations.Inject;
import nl.strohalm.cyclos.controls.BaseAjaxAction.ContentType;
import nl.strohalm.cyclos.controls.restapi.BaseRestController;
import nl.strohalm.cyclos.utils.CustomizationHelper;
import nl.strohalm.cyclos.utils.binding.BeanBinder;
import nl.strohalm.cyclos.utils.binding.BeanCollectionBinder;
import nl.strohalm.cyclos.utils.binding.DataBinder;
import nl.strohalm.cyclos.utils.binding.PropertyBinder;
@Controller
public class GetDirectoryContentsAjaxController extends BaseRestController{
	
	private DataBinder<Collection<File>> fileDirCollectionBinder;
    private CustomizationHelper          customizationHelper;

    @Inject
    public void setCustomizationHelper(final CustomizationHelper customizationHelper) {
        this.customizationHelper = customizationHelper;
    }

    //@Override
    protected ContentType contentType() {
        return ContentType.JSON;
    }
    
    public static class GetDirectoryContentsAjaxRequestDTO {
    	
    	private String            path;
    	
        public String getPath() {
            return path;
        }

        public void setPath(final String path) {
            this.path = path;
        }

	}

	public static class GetDirectoryContentsAjaxResponseDTO {
		String message;
		Map<String, Object> param;
		public GetDirectoryContentsAjaxResponseDTO(String message, Map<String, Object> param) {
			super();
			this.message = message;
			this.param = param;
		}
		
		public void writeJSON(final GetDirectoryContentsAjaxResponseDTO response, final String json) {
	        try {
	           // response.getWriter().print("{\"result\":" + json + "}");
	        } catch (final Exception e) {
	            throw new IllegalStateException("Error writing JSON string", e);
	        }
	    }
		List<File> file;

		public GetDirectoryContentsAjaxResponseDTO(List<File> file) {
			super();
			this.file = file;
		}
		
	}
	

    @RequestMapping(value = "",method = RequestMethod.GET)
    @ResponseBody
    protected GetDirectoryContentsAjaxResponseDTO renderContent(@RequestBody  GetDirectoryContentsAjaxRequestDTO form) throws Exception {
        
        final String path = form.getPath();
        final List<File> filesAndDirs = customizationHelper.getDirectoryContents(path);
        final String json = getFileDirBinder().readAsString(filesAndDirs);
        
        //responseHelper.writeJSON(response, json);
        GetDirectoryContentsAjaxResponseDTO reponse = new GetDirectoryContentsAjaxResponseDTO(filesAndDirs);
       return customizationHelper.writeJSON(response, json);
       //return response;
        
        
        
    }

    private DataBinder<Collection<File>> getFileDirBinder() {
        if (fileDirCollectionBinder == null) {
            final BeanBinder<File> fileDirBinder = BeanBinder.instance(File.class);
            fileDirBinder.registerBinder("name", PropertyBinder.instance(String.class, "name"));
            fileDirBinder.registerBinder("directory", PropertyBinder.instance(Boolean.TYPE, "directory"));
            fileDirCollectionBinder = BeanCollectionBinder.instance(fileDirBinder);
        }
        return fileDirCollectionBinder;
    }

	}

