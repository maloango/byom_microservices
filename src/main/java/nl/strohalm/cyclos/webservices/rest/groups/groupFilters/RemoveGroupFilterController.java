package nl.strohalm.cyclos.webservices.rest.groups.groupFilters;

import java.io.File;
import java.util.Collection;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import nl.strohalm.cyclos.annotations.Inject;
import nl.strohalm.cyclos.entities.customization.files.CustomizedFile;
import nl.strohalm.cyclos.entities.groups.GroupFilter;
import nl.strohalm.cyclos.services.groups.GroupFilterService;
import nl.strohalm.cyclos.utils.CustomizationHelper;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;
import nl.strohalm.cyclos.webservices.rest.GenericResponse;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class RemoveGroupFilterController extends BaseRestController {
    
    @RequestMapping(value = "admin/removeGroupFilter/{groupFilterId}", method = RequestMethod.GET)
    @ResponseBody
    protected GenericResponse executeAction(@PathVariable("groupFilterId") Long groupFilterId) throws Exception {
        GenericResponse response = new GenericResponse();
        final GroupFilter groupFilter = groupFilterService.load(groupFilterId, GroupFilter.Relationships.CUSTOMIZED_FILES);
        final Collection<CustomizedFile> customizedFiles = groupFilter.getCustomizedFiles();
        groupFilterService.remove(groupFilterId);

        // Remove the physical customized files for the group
        for (final CustomizedFile customizedFile : customizedFiles) {
            final File physicalFile = customizationHelper.customizedFileOf(customizedFile);
            customizationHelper.deleteFile(physicalFile);
        }
        
        response.setMessage("groupFilter.removed");
        response.setStatus(0);
        return response;
    }
}
