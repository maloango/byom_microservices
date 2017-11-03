package nl.strohalm.cyclos.webservices.rest.groups;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import nl.strohalm.cyclos.annotations.Inject;
import nl.strohalm.cyclos.entities.customization.files.CustomizedFile;
import nl.strohalm.cyclos.entities.groups.Group;
import nl.strohalm.cyclos.services.groups.GroupService;
import nl.strohalm.cyclos.utils.validation.ValidationException;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;
import nl.strohalm.cyclos.webservices.rest.GenericResponse;
import org.springframework.web.bind.annotation.PathVariable;


@Controller
public class RemoveGroupController extends BaseRestController {
    
    @RequestMapping(value = "admin/removeGroup/{groupId}", method = RequestMethod.GET)
    @ResponseBody
    protected GenericResponse executeAction(@PathVariable("groupId") Long groupId) throws Exception {
        GenericResponse response = new GenericResponse();
        final long id = groupId;
        if (id <= 0) {
            throw new ValidationException();
        }
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
        response.setStatus(0);
        
        return response;
    }
    
}
