package nl.strohalm.cyclos.webservices.rest.groups.customizedFiles;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import nl.strohalm.cyclos.access.Permission;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import nl.strohalm.cyclos.annotations.Inject;
import nl.strohalm.cyclos.controls.ActionContext;
import nl.strohalm.cyclos.controls.groups.ListGroupsAction;
import nl.strohalm.cyclos.controls.groups.customizedFiles.EditGroupCustomizedFileForm;
import nl.strohalm.cyclos.entities.customization.files.CustomizedFile;
import nl.strohalm.cyclos.entities.groups.Group;
import nl.strohalm.cyclos.entities.groups.OperatorGroup;
import nl.strohalm.cyclos.services.customization.CustomizedFileService;
import nl.strohalm.cyclos.services.groups.GroupService;
import nl.strohalm.cyclos.utils.CustomizationHelper;
import nl.strohalm.cyclos.utils.binding.BeanBinder;
import nl.strohalm.cyclos.utils.binding.DataBinder;
import nl.strohalm.cyclos.utils.binding.PropertyBinder;
import nl.strohalm.cyclos.utils.conversion.CoercionConverter;
import nl.strohalm.cyclos.utils.conversion.HtmlConverter;
import nl.strohalm.cyclos.utils.conversion.IdConverter;
import nl.strohalm.cyclos.utils.conversion.ReferenceConverter;
import nl.strohalm.cyclos.utils.validation.ValidationException;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;
import org.springframework.web.bind.annotation.PathVariable;
@Controller
public class EditGroupCustomizedFileController extends BaseRestController{
    

	private CustomizedFileService      customizedFileService;
    public final GroupService getGroupService() {
		return groupService;
	}

	public final void setGroupService(GroupService groupService) {
		this.groupService = groupService;
	}

	public final void setMemberDataBinder(DataBinder<CustomizedFile> memberDataBinder) {
		this.memberDataBinder = memberDataBinder;
	}

	private DataBinder<CustomizedFile> memberDataBinder;
    private DataBinder<CustomizedFile> adminDataBinder;
    private CustomizationHelper        customizationHelper;
    private GroupService groupService;

    public DataBinder<CustomizedFile> getAdminDataBinder() {
        if (adminDataBinder == null) {
            final BeanBinder<CustomizedFile> binder = getCommonBinderElements();
            binder.registerBinder("contents", PropertyBinder.instance(String.class, "contents", CoercionConverter.instance(String.class)));
            adminDataBinder = binder;
        }
        return adminDataBinder;
    }

    public CustomizedFileService getCustomizedFileService() {
        return customizedFileService;
    }

    public DataBinder<CustomizedFile> getMemberDataBinder() {
        if (memberDataBinder == null) {
            final BeanBinder<CustomizedFile> binder = getCommonBinderElements();
            binder.registerBinder("contents", PropertyBinder.instance(String.class, "contents", HtmlConverter.instance()));
            memberDataBinder = binder;
        }
        return memberDataBinder;
    }

    @Inject
    public void setCustomizationHelper(final CustomizationHelper customizationHelper) {
        this.customizationHelper = customizationHelper;
    }

    @Inject
    public void setCustomizedFileService(final CustomizedFileService customizedFileService) {
        this.customizedFileService = customizedFileService;
    }
    
    public static class EditGroupCustomizedFileRequestDTO{
    	private long              groupId;
        private long              fileId;
        private Map<String,Object> values;
        public Map<String,Object>getValues;
        private boolean isAdmin;

        public Map<String, Object> getFile() {
            return values;
        }

        public Object getFile(final String key) {
            return values.get(key);
        }

        public long getFileId() {
            return fileId;
        }

        public long getGroupId() {
            return groupId;
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

        public void setGroupId(final long groupId) {
            this.groupId = groupId;
        }

		public boolean isAdmin() {
			return isAdmin;
		}

		public void setAdmin(boolean isAdmin) {
			this.isAdmin = isAdmin;
		}

		
    }
    
    public static class EditGroupCustomizedFileResponseDTO{
    	
    	String message;

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public Map<String, Object> getParam() {
            return param;
        }

        public void setParam(Map<String, Object> param) {
            this.param = param;
        }
        
		Map<String, Object> param;
		public EditGroupCustomizedFileResponseDTO(String message, Map<String, Object> param) {
			super();
			this.message = message;
			this.param = param;
		}
                public EditGroupCustomizedFileResponseDTO(){
                }

    }

    @RequestMapping(value = "member/editGroupCustomizedFile" , method = RequestMethod.POST)
    @ResponseBody
    protected EditGroupCustomizedFileResponseDTO handleSubmit(@RequestBody EditGroupCustomizedFileRequestDTO form) throws Exception {
        DataBinder<CustomizedFile> dataBinder;
        EditGroupCustomizedFileResponseDTO response = null;
        try{
        if (form.isAdmin()) {
            dataBinder = getAdminDataBinder();
        } else {
            dataBinder = getMemberDataBinder();
        }
        CustomizedFile file = dataBinder.readFromString(form.getFile());
        Group group = file.getGroup();
        // Ensure the file has a group
        if (group == null) {
            throw new ValidationException();
        } else {
            group = groupService.load(group.getId());
            file.setGroup(group);
        }

        final boolean isInsert = file.isTransient();
        if (group instanceof OperatorGroup) {
            file.setType(CustomizedFile.Type.STATIC_FILE);
        }
        file = customizedFileService.save(file);

        // Physically update the file
        final File physicalFile = customizationHelper.customizedFileOf(file);
        customizationHelper.updateFile(physicalFile, file);
        final Map<String, Object> params = new HashMap<String, Object>();
        String message = null;
        if (isInsert) {
        	message = "customizedFiles.customized";
        }
        	else{
        		message = "customizedFiles.modified";
        	}
        
        params.put("fileId", file.getId());
        params.put("groupId", group.getId());
       response = new EditGroupCustomizedFileResponseDTO(message, params);}
        catch(ValidationException e){
            e.printStackTrace();
            
        }
            return response;
    }

        
   public static class PrepareFormResponseDto{
       String message;
       private boolean group;
       private boolean isInsert;
       private boolean editable;
       public HashMap<String,Object> response=new HashMap<String,Object>();

        public HashMap<String, Object> getResponse() {
            return response;
        }

        public void setResponse(HashMap<String, Object> response) {
            this.response = response;
        }
       

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public boolean isGroup() {
            return group;
        }

        public void setGroup(boolean group) {
            this.group = group;
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
        public PrepareFormResponseDto(){
        }

   }
   @RequestMapping(value = "member/editGroupCustomizedFile/{groupId}", method = RequestMethod.GET)
   @ResponseBody
    
    public PrepareFormResponseDto prepareForm(@PathVariable ("groupId") long groupId,@PathVariable("isAdmin") int isAdmin) throws Exception {
      PrepareFormResponseDto preFormResp = new PrepareFormResponseDto();
      try{
      HashMap<String,Object> response=new HashMap<String,Object>();

        boolean editable = false;

        // Retrieve the group
        final Map<Group.Nature, Permission> permissionByNature = ListGroupsAction.getManageGroupPermissionByNatureMap();
        final long Id = groupId;
        if (groupId <= 0L) {
            throw new ValidationException();
        }
        final Group group = groupService.load(groupId);

        final long id = groupId;
        final boolean isInsert = id <= 0L;
        CustomizedFile file;
        if (isInsert) {
            file = new CustomizedFile();
            file.setGroup(group);
            // Prepare the possible types
            response.put("types", Arrays.asList(CustomizedFile.Type.STATIC_FILE, CustomizedFile.Type.STYLE));
            editable = true;
        } else {
            // Retrieve the file
            file = customizedFileService.load(id);
            if (file.getGroup() == null || !file.getGroup().equals(group)) {
                // Wrong group passed
                throw new ValidationException();
            }
            editable = customizedFileService.canViewOrManageInGroup(group);

        }
        response.put("file", file);
        DataBinder<CustomizedFile> dataBinder;
        if (isAdmin==1) {
            dataBinder = getAdminDataBinder();
        } else {
            dataBinder = getMemberDataBinder();
        }
      
      dataBinder.writeAsString(group, group);
          Object put = response.put("group", group);
        response.put("isInsert", isInsert);
        response.put("editable", editable);
        
         preFormResp.setResponse(response);
       }
       catch(ValidationException e){
               e.printStackTrace();
               }
       
        
        return preFormResp;
    }

   
    protected void validateForm(final ActionContext context) {
        final EditGroupCustomizedFileForm form = context.getForm();
        DataBinder<CustomizedFile> dataBinder;
        if (context.isAdmin()) {
            dataBinder = getAdminDataBinder();
        } else {
            dataBinder = getMemberDataBinder();
        }
        final CustomizedFile file = dataBinder.readFromString(form.getFile());
        if (context.isMember()) {
            file.setType(CustomizedFile.Type.STATIC_FILE);
        }
        customizedFileService.validate(file);
    }

    private BeanBinder<CustomizedFile> getCommonBinderElements() {
        final BeanBinder<CustomizedFile> binder = BeanBinder.instance(CustomizedFile.class);
        binder.registerBinder("id", PropertyBinder.instance(Long.class, "id", IdConverter.instance()));
        binder.registerBinder("type", PropertyBinder.instance(CustomizedFile.Type.class, "type"));
        binder.registerBinder("group", PropertyBinder.instance(Group.class, "group", ReferenceConverter.instance(Group.class)));
        binder.registerBinder("name", PropertyBinder.instance(String.class, "name"));

        return binder;
    }
}
