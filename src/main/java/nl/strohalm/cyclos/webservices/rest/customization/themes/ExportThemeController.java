package nl.strohalm.cyclos.webservices.rest.customization.themes;

import java.util.Collections;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import nl.strohalm.cyclos.annotations.Inject;
import nl.strohalm.cyclos.controls.ActionContext;
//import nl.strohalm.cyclos.controls.customization.themes.ExportThemeForm;
import nl.strohalm.cyclos.themes.Theme;
import nl.strohalm.cyclos.themes.ThemeHandler;
import nl.strohalm.cyclos.utils.binding.BeanBinder;
import nl.strohalm.cyclos.utils.binding.DataBinder;
import nl.strohalm.cyclos.utils.binding.PropertyBinder;
import nl.strohalm.cyclos.utils.binding.SimpleCollectionBinder;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;

@Controller
public class ExportThemeController extends BaseRestController{

	private DataBinder<Theme> dataBinder;
    private ThemeHandler      themeHandler;

    public DataBinder<Theme> getDataBinder() {
        if (dataBinder == null) {
            final BeanBinder<Theme> binder = BeanBinder.instance(Theme.class);
            binder.registerBinder("title", PropertyBinder.instance(String.class, "title"));
            binder.registerBinder("filename", PropertyBinder.instance(String.class, "filename"));
            binder.registerBinder("author", PropertyBinder.instance(String.class, "author"));
            binder.registerBinder("version", PropertyBinder.instance(String.class, "version"));
            binder.registerBinder("description", PropertyBinder.instance(String.class, "description"));
            binder.registerBinder("styles", SimpleCollectionBinder.instance(Theme.Style.class, "styles"));
            dataBinder = binder;
        }
        return dataBinder;
    }

    @Inject
    public void setThemeHandler(final ThemeHandler themeHandler) {
        this.themeHandler = themeHandler;
    }
    public static class ExportThemeRequesrDTO{
    	 protected Map<String, Object> values;

 		public Map<String, Object> getValues() {
 			return values;
 		}
    	public void ExportThemeForm() {
            setTheme("styles", Collections.emptyList());
        }

        public Map<String, Object> getTheme() {
            return values;
        }

        public Object getTheme(final String key) {
            return values.get(key);
        }

        public void setTheme(final Map<String, Object> map) {
            values = map;
        }

        public void setTheme(final String key, final Object value) {
            values.put(key, value);
        }

    }
    
    public static class ExportThemeResponseDTO{
    	String message;
		Map<String, Object> param;
		public ExportThemeResponseDTO(String message, Map<String, Object> param) {
			super();
			this.message = message;
			this.param = param;
		}
		
    }

    @RequestMapping(value ="admin/importTheme" , method = RequestMethod.GET)
    @ResponseBody
    protected ExportThemeResponseDTO handleSubmit(@RequestBody ExportThemeRequesrDTO form) throws Exception {
        final Theme theme = getDataBinder().readFromString(form.getTheme());
        String filename = theme.getFilename();
        if (!filename.endsWith(".theme")) {
            filename = filename + ".theme";
        }
        // Set the download headers and content
        ExportThemeResponseDTO response = new ExportThemeResponseDTO(filename, null);
        
          return response;
    }

   // @Override
//    protected void validateForm(final ActionContext context) {
//        final ExportThemeForm form = context.getForm();
//        final Theme theme = getDataBinder().readFromString(form.getTheme());
//        themeHandler.validateForExport(theme);
//    }

}
