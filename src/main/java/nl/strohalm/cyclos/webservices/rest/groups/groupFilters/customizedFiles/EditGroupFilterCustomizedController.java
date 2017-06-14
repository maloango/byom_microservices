package nl.strohalm.cyclos.webservices.rest.groups.groupFilters.customizedFiles;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import nl.strohalm.cyclos.access.AdminSystemPermission;
import nl.strohalm.cyclos.annotations.Inject;
import nl.strohalm.cyclos.controls.ActionContext;
import nl.strohalm.cyclos.controls.groups.groupFilters.customizedFiles.EditGroupFilterCustomizedFileForm;
import nl.strohalm.cyclos.entities.customization.files.CustomizedFile;
import nl.strohalm.cyclos.entities.groups.GroupFilter;
import nl.strohalm.cyclos.services.customization.CustomizedFileService;
import nl.strohalm.cyclos.services.groups.GroupFilterService;
import nl.strohalm.cyclos.services.permissions.PermissionService;
import nl.strohalm.cyclos.utils.CustomizationHelper;
import nl.strohalm.cyclos.utils.binding.BeanBinder;
import nl.strohalm.cyclos.utils.binding.DataBinder;
import nl.strohalm.cyclos.utils.binding.PropertyBinder;
import nl.strohalm.cyclos.utils.conversion.CoercionConverter;
import nl.strohalm.cyclos.utils.conversion.IdConverter;
import nl.strohalm.cyclos.utils.validation.ValidationException;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;
@Controller
public class EditGroupFilterCustomizedController extends BaseRestController{
	 private CustomizedFileService      customizedFileService;
	    private GroupFilterService         groupFilterService;
	    private DataBinder<CustomizedFile> dataBinder;
	    private CustomizationHelper        customizationHelper;
	    private PermissionService permissionService;

	    public DataBinder<CustomizedFile> getDataBinder() {
	        if (dataBinder == null) {
	            final BeanBinder<CustomizedFile> binder = BeanBinder.instance(CustomizedFile.class);
	            binder.registerBinder("id", PropertyBinder.instance(Long.class, "id", IdConverter.instance()));
	            binder.registerBinder("type", PropertyBinder.instance(CustomizedFile.Type.class, "type"));
	            binder.registerBinder("groupFilter", PropertyBinder.instance(GroupFilter.class, "groupFilter"));
	            binder.registerBinder("name", PropertyBinder.instance(String.class, "name"));
	            binder.registerBinder("contents", PropertyBinder.instance(String.class, "contents", CoercionConverter.instance(String.class)));
	            dataBinder = binder;
	        }
	        return dataBinder;
	    }

	    @Inject
	    public void setCustomizationHelper(final CustomizationHelper customizationHelper) {
	        this.customizationHelper = customizationHelper;
	    }

	    @Inject
	    public void setCustomizedFileService(final CustomizedFileService customizedFileService) {
	        this.customizedFileService = customizedFileService;
	    }

	    @Inject
	    public void setGroupFilterService(final GroupFilterService groupFilterService) {
	        this.groupFilterService = groupFilterService;
	    }
	    
	    public static class EditGroupFilterCustomizedRequestDTO{
	    	private long              groupFilterId;
	        private long              fileId;
	        private Map<String,Object>values;
	        public Map<String,Object>getvalues;

	        public Map<String, Object> getFile() {
	            return values;
	        }

	        public Object getFile(final String key) {
	            return values.get(key);
	        }

	        public long getFileId() {
	            return fileId;
	        }

	        public long getGroupFilterId() {
	            return groupFilterId;
	        }

	        public void setFile(final Map<String, Object> file) {
	            values = file;
	        }

	        public void setFile(final String key, final Object value) {
	            values.put(key, value);
	        }

	        public void setFileId(final long fileId) {
	            this.fileId = fileId;
	        }

	        public void setGroupFilterId(final long groupFilterId) {
	            this.groupFilterId = groupFilterId;
	        }

	    	
	    }
	    
	    public static class EditGroupFilterCustomizedResponseDTO{
	    	String message;
	    	Map<String,Object>param;
	    	public EditGroupFilterCustomizedResponseDTO(String message,Map<String,Object>param){
	    		super();
	    		this.message = message;
	    		this.param = param;
	    	}
	    	
	    }

	    @RequestMapping(value = "/member/editGroupCustomizedFile" ,method = RequestMethod.POST)
	    @ResponseBody
	    protected EditGroupFilterCustomizedResponseDTO handleSubmit(@RequestBody EditGroupFilterCustomizedRequestDTO form ) throws Exception {
	        CustomizedFile file = getDataBinder().readFromString(form.getFile());
	        final GroupFilter groupFilter = groupFilterService.load(file.getGroupFilter().getId());
	        // Ensure the file has a group filter
	        if (groupFilter == null) {
	            throw new ValidationException();
	        }

	        final boolean isInsert = file.isTransient();
	        file = customizedFileService.save(file);

	        // Physically update the file
	        final File physicalFile = customizationHelper.customizedFileOf(file);
	        customizationHelper.updateFile(physicalFile, file);

	        //context.sendMessage(isInsert ? "groupFilter.customizedFiles.customized" : "groupFilter.customizedFiles.modified");
	        final Map<String, Object> params = new HashMap<String, Object>();
	        String message = null;
	        if (isInsert) {
	        	message = "groupFilter.customizedFiles.customized";
	        	}
	        	else{
	        		message = "groupFilter.customizedFiles.modified";
	        	}
	        
	        params.put("fileId", file.getId());
	        params.put("groupFilterId", groupFilter.getId());
	        EditGroupFilterCustomizedResponseDTO response = new EditGroupFilterCustomizedResponseDTO(message, params);
	        return response;
	    }

	    //@Override
	    protected void prepareForm(final ActionContext context) throws Exception {
	        final EditGroupFilterCustomizedFileForm form = context.getForm();
	        final HttpServletRequest request = context.getRequest();

	        final boolean editable = permissionService.hasPermission(AdminSystemPermission.GROUP_FILTERS_MANAGE_CUSTOMIZED_FILES);

	        // Retrieve the group filter
	        final long groupFilterId = form.getGroupFilterId();
	        if (groupFilterId <= 0L) {
	            throw new ValidationException();
	        }
	        final GroupFilter groupFilter = groupFilterService.load(groupFilterId);

	        final long id = form.getFileId();
	        final boolean isInsert = id <= 0L;
	        CustomizedFile file;
	        if (isInsert) {
	            file = new CustomizedFile();
	            file.setGroupFilter(groupFilter);
	            // Prepare the possible types
	            request.setAttribute("types", Arrays.asList(CustomizedFile.Type.STATIC_FILE, CustomizedFile.Type.STYLE));
	        } else {
	            // Retrieve the file
	            file = customizedFileService.load(id);
	            if (file.getGroupFilter() == null || !file.getGroupFilter().equals(groupFilter)) {
	                // Wrong group filter passed
	                throw new ValidationException();
	            }
	        }
	        request.setAttribute("file", file);
	        getDataBinder().writeAsString(form.getFile(), file);
	        request.setAttribute("group", groupFilter);
	        request.setAttribute("isInsert", isInsert);
	        request.setAttribute("editable", editable);
	    }

	   // @Override
	    protected void validateForm(final ActionContext context) {
	        final EditGroupFilterCustomizedFileForm form = context.getForm();
	        final CustomizedFile file = getDataBinder().readFromString(form.getFile());
	        customizedFileService.validate(file);
	    }
}
