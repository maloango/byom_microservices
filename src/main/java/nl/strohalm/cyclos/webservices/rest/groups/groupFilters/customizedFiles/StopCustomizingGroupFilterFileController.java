package nl.strohalm.cyclos.webservices.rest.groups.groupFilters.customizedFiles;

import java.io.File;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import nl.strohalm.cyclos.annotations.Inject;
import nl.strohalm.cyclos.entities.customization.files.CustomizedFile;
import nl.strohalm.cyclos.services.customization.CustomizedFileService;
import nl.strohalm.cyclos.utils.CustomizationHelper;
import nl.strohalm.cyclos.utils.validation.ValidationException;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;
@Controller
public class StopCustomizingGroupFilterFileController extends BaseRestController {
	 private CustomizedFileService customizedFileService;
	    private CustomizationHelper   customizationHelper;

	    public CustomizedFileService getCustomizedFileService() {
	        return customizedFileService;
	    }

	    @Inject
	    public void setCustomizationHelper(final CustomizationHelper customizationHelper) {
	        this.customizationHelper = customizationHelper;
	    }

	    @Inject
	    public void setCustomizedFileService(final CustomizedFileService customizedFileService) {
	        this.customizedFileService = customizedFileService;
	    }
	    
	    public static class StopCustomizingGroupFilterFileRequestDTO{
	    	
	    	private long              fileId;
	        private long              groupFilterId;

	        public long getFileId() {
	            return fileId;
	        }

	        public long getGroupFilterId() {
	            return groupFilterId;
	        }

	        public void setFileId(final long fileId) {
	            this.fileId = fileId;
	        }

	        public void setGroupFilterId(final long groupFilterId) {
	            this.groupFilterId = groupFilterId;
	        }
	    }
	    
	    public static class StopCustomizingGroupFilterFileResponseDTO{
	    	
	    	private String message;

			public String getMessage() {
				return message;
			}

			public void setMessage(String message) {
				this.message = message;
			}
	    }

	    @RequestMapping(value = "admin/stopCustomizingGroupFilterFile" , method = RequestMethod.DELETE)
	    @ResponseBody
	    
	    protected StopCustomizingGroupFilterFileResponseDTO executeAction(@RequestBody StopCustomizingGroupFilterFileRequestDTO form) throws Exception {
	       
	        final long id = form.getFileId();
	        final long groupFilterId = form.getGroupFilterId();
	        if (id <= 0L || groupFilterId <= 0L) {
	            throw new ValidationException();
	        }
	        final CustomizedFile file = customizedFileService.load(id);
	        if (file.getGroupFilter() == null || !file.getGroupFilter().getId().equals(groupFilterId)) {
	            throw new ValidationException();
	        }
	        customizedFileService.stopCustomizing(file);
	        // Remove the physical file
	        final File physicalFile = customizationHelper.customizedFileOf(file);
	        customizationHelper.deleteFile(physicalFile);
	        StopCustomizingGroupFilterFileResponseDTO response = new StopCustomizingGroupFilterFileResponseDTO();
			response.setMessage("groupFilter.customizedFiles.removed");
			return response;

	        //return ActionHelper.redirectWithParam(context.getRequest(), context.getSuccessForward(), "groupFilterId", groupFilterId);
	    }


}
