package nl.strohalm.cyclos.webservices.rest.groups;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import nl.strohalm.cyclos.annotations.Inject;
import nl.strohalm.cyclos.entities.customization.files.CustomizedFile;
import nl.strohalm.cyclos.entities.groups.Group;
import nl.strohalm.cyclos.services.groups.GroupService;
import nl.strohalm.cyclos.utils.CustomizationHelper;
import nl.strohalm.cyclos.utils.validation.ValidationException;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;
@Controller
public class RemoveGroupController extends BaseRestController {
	private CustomizationHelper customizationHelper;
	private GroupService groupService;

    @Inject
    public void setCustomizationHelper(final CustomizationHelper customizationHelper) {
        this.customizationHelper = customizationHelper;
    }
    public static class RemoveGroupRequestDTO{
    	 private long              groupId;

    	    public long getGroupId() {
    	        return groupId;
    	    }

    	    @Inject
    	    public void setGroupId(final long groupId) {
    	        this.groupId = groupId;
    	    }
    }
    
    public static class RemoveGroupResponseDTO{
    	String message;

		public final String getMessage() {
			return message;
		}

		public final void setMessage(String message) {
			this.message = message;
		}
    	
    	
    }

    @RequestMapping(value ="/admin/removeGroup" ,method = RequestMethod.DELETE)
    @ResponseBody
    protected RemoveGroupResponseDTO executeAction(@RequestBody RemoveGroupRequestDTO  form) throws Exception {
        final long id = form.getGroupId();
        if (id <= 0) {
            throw new ValidationException();
        }
        
        RemoveGroupResponseDTO response = new RemoveGroupResponseDTO();
        final Collection<File> toRemove = new ArrayList<File>();
        try {
            final Group group = groupService.load(id, Group.Relationships.CUSTOMIZED_FILES);
            final Collection<CustomizedFile> customizedFiles = group.getCustomizedFiles();
            
            
            // Before removing the group, get the customized files
            for (final CustomizedFile customizedFile : customizedFiles) {
                final File physicalFile = customizationHelper.customizedFileOf(customizedFile);
                toRemove.add(physicalFile);
            }
            groupService.remove(id);
            // Physically remove the customized files
            for (final File file : toRemove) {
                customizationHelper.deleteFile(file);
            }
            response.setMessage("group.removed");
        } catch (final Exception e) {
        	response.setMessage("group.error.removing");
        }
        return response;
    }

}
