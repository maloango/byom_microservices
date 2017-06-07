package nl.strohalm.cyclos.webservices.rest.customization.themes;

import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import nl.strohalm.cyclos.annotations.Inject;
import nl.strohalm.cyclos.controls.restapi.BaseRestController;
import nl.strohalm.cyclos.themes.ThemeHandler;

@Controller
public class RemoveThemeController extends BaseRestController {

	private ThemeHandler themeHandler;

    @Inject
    public void setThemeHandler(final ThemeHandler themeHandler) {
        this.themeHandler = themeHandler;
    }
    
    public static class RemoveRequestDTO{
    	private String            filename;
    	protected Map<String, Object> values;

 		public Map<String, Object> getValues() {
 			return values;
 		}

        public String getFilename() {
            return filename;
        }

        public Map<String, Object> getTheme() {
            return values;
        }

        public Object getTheme(final String key) {
            return values.get(key);
        }

        public void setFilename(final String filename) {
            this.filename = filename;
        }

        public void setTheme(final Map<String, Object> map) {
            values = map;
        }

        public void setTheme(final String key, final Object value) {
            values.put(key, value);
        }
    }
    
    public static class RemoveResponseDTO{
    	String message;

		public final String getMessage() {
			return message;
		}

		public final void setMessage(String message) {
			this.message = message;
		}
    	
    	
    }

    @RequestMapping(value = "/admin/removeTheme", method = RequestMethod.DELETE)
    @ResponseBody
    protected RemoveResponseDTO executeAction(@RequestBody RemoveRequestDTO form) throws Exception {
        final String filename = form.getFilename();
        RemoveResponseDTO response = new RemoveResponseDTO();
        try {
            themeHandler.remove(filename);
            response.setMessage("theme.removed");
        } catch (final Exception e) {
            response.setMessage("theme.remove.error");
        }
        return response;
    }

}
