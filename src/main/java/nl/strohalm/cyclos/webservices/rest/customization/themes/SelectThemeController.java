package nl.strohalm.cyclos.webservices.rest.customization.themes;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import nl.strohalm.cyclos.annotations.Inject;
import nl.strohalm.cyclos.controls.ActionContext;
import nl.strohalm.cyclos.themes.Theme;
import nl.strohalm.cyclos.themes.ThemeHandler;
import nl.strohalm.cyclos.utils.RequestHelper;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;
@Controller
public class SelectThemeController extends BaseRestController {
	private ThemeHandler themeHandler;

    @Inject
    public void setThemeHandler(final ThemeHandler themeHandler) {
        this.themeHandler = themeHandler;
    }
    
    public static class SelectThemeRequestDTO{
    	 private String            filename;
    	 private Map<String ,Object> values;
    	 public Map<String ,Object> getvalues;

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
    public static class SelectThemeResponseDTO{
    	String message;

		public final String getMessage() {
			return message;
		}

		public final void setMessage(String message) {
			this.message = message;
		}
    }

    @RequestMapping(value = "/admin/selectTheme" , method = RequestMethod.GET)
    @ResponseBody
    protected  SelectThemeResponseDTO ActionForm(@RequestBody SelectThemeRequestDTO form) throws Exception {
        final String selected = form.getFilename();
        SelectThemeResponseDTO response = new SelectThemeResponseDTO();
        
        themeHandler.select(selected);
        response.setMessage("theme.selected");
        return response;
    }

   // @Override
    protected void prepareForm(final ActionContext context) throws Exception {
        final HttpServletRequest request = context.getRequest();
        request.setAttribute("themes", themeHandler.list());
        RequestHelper.storeEnum(request, Theme.Style.class, "styles");
    }

}
