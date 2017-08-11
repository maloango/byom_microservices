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
import org.springframework.web.bind.annotation.PathVariable;
@Controller
public class EditGroupFilterCustomizedFileController extends BaseRestController{
	 private CustomizedFileService      customizedFileService;
	    private GroupFilterService         groupFilterService;
	    public final PermissionService getPermissionService() {
			return permissionService;
		}

		public final void setPermissionService(PermissionService permissionService) {
			this.permissionService = permissionService;
		}

		public final CustomizedFileService getCustomizedFileService() {
			return customizedFileService;
		}

		public final GroupFilterService getGroupFilterService() {
			return groupFilterService;
		}

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
                private boolean hasPermission;
                private boolean isInsert;
                private boolean editable;

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public boolean isHasPermission() {
            return hasPermission;
        }

        public void setHasPermission(boolean hasPermission) {
            this.hasPermission = hasPermission;
        }

        public boolean isIsInsert() {
            return isInsert;
        }

        public void setIsInsert(boolean isInsert) {
            this.isInsert = isInsert;
        }

        public boolean isEditable() {
            return editable;
        }

        public void setEditable(boolean editable) {
            this.editable = editable;
        }

        public Map<String, Object> getParam() {
            return param;
        }

        public void setParam(Map<String, Object> param) {
            this.param = param;
        }
                
	    	Map<String,Object>param;
	    	public EditGroupFilterCustomizedResponseDTO(String message,Map<String,Object>param){
	    		super();
	    		this.message = message;
	    		this.param = param;
	    	}
                public EditGroupFilterCustomizedResponseDTO(){
                }
	    	
	    }

	    @RequestMapping(value = "admin/editGroupFilterCustomizedFile" ,method = RequestMethod.POST)
	    @ResponseBody
	    protected EditGroupFilterCustomizedResponseDTO handleSubmit(@RequestBody EditGroupFilterCustomizedRequestDTO form ) throws Exception {
	        CustomizedFile file = getDataBinder().readFromString(form.getFile());
	        final GroupFilter groupFilter = groupFilterService.load(file.getGroupFilter().getId());
	        EditGroupFilterCustomizedResponseDTO response =null;
                try{
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
	        response = new EditGroupFilterCustomizedResponseDTO(message, params);}
                catch(ValidationException e){
                    e.printStackTrace();
                }
	        return response;
	    }

	    //@Override
            public static class PrepareFormResponseDTO{
                public HashMap<String, Object> response = new HashMap<String, Object>();
                private boolean isInsert;
                private boolean editable;

        public boolean isIsInsert() {
            return isInsert;
        }

        public void setIsInsert(boolean isInsert) {
            this.isInsert = isInsert;
        }

        public boolean isEditable() {
            return editable;
        }

        public void setEditable(boolean editable) {
            this.editable = editable;
        }
                

        public HashMap<String, Object> getResponse() {
            return response;
        }

        public void setResponse(HashMap<String, Object> response) {
            this.response = response;
        }
                public PrepareFormResponseDTO(){
                    
                }
            }
	@RequestMapping(value = "admin/editGroupFilterCustomizedFile/{groupFilterId}/{fileId}", method = RequestMethod.GET)
	@ResponseBody
           
            protected PrepareFormResponseDTO prepareForm(@PathVariable ("groupFilterId") long groupFilterId, @PathVariable("fileId") long fileId) throws Exception {
	        
                PrepareFormResponseDTO preFormResp=new PrepareFormResponseDTO();
                try{
                    
                HashMap<String,Object> response=new HashMap<String,Object>();

	        final boolean editable = permissionService.hasPermission(AdminSystemPermission.GROUP_FILTERS_MANAGE_CUSTOMIZED_FILES);

	        // Retrieve the group filter
	        final long Id = groupFilterId;
	        if (groupFilterId <= 0L) {
	            throw new ValidationException();
	        }
	        final GroupFilter groupFilter = groupFilterService.load(groupFilterId);

	        final long id = fileId;
	        final boolean isInsert = id <= 0L;
	        CustomizedFile file;
	        if (isInsert) {
	            file = new CustomizedFile();
	            file.setGroupFilter(groupFilter);
	            // Prepare the possible types
	            response.put("types", Arrays.asList(CustomizedFile.Type.STATIC_FILE, CustomizedFile.Type.STYLE));
	        } else {
	            // Retrieve the file
	            file = customizedFileService.load(id);
	            if (file.getGroupFilter() == null || !file.getGroupFilter().equals(groupFilter)) {
	                // Wrong group filter passed
	                throw new ValidationException();
	            }
	        }
                getDataBinder().writeAsString(file,file);
	        response.put("file", file);
	        response.put("group", groupFilter);
	        response.put("isInsert", isInsert);
	        response.put("editable", editable);
                
                preFormResp.setResponse(response);
               }
                catch(ValidationException e){
                    e.printStackTrace();
                }
                return preFormResp;
            }
                
	   

	   // @Override
	    protected void validateForm(final ActionContext context) {
	        final EditGroupFilterCustomizedFileForm form = context.getForm();
	        final CustomizedFile file = getDataBinder().readFromString(form.getFile());
	        customizedFileService.validate(file);
	    }
}
