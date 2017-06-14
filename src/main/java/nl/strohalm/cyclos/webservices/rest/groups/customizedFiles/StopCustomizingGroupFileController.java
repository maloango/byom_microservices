package nl.strohalm.cyclos.webservices.rest.groups.customizedFiles;

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
public class StopCustomizingGroupFileController extends BaseRestController {
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
    
    public static class StopCustomizingGroupFileRequestDTO{
    	private long              fileId;
        private long              groupId;

        public long getFileId() {
            return fileId;
        }

        public long getGroupId() {
            return groupId;
        }

        public void setFileId(final long fileId) {
            this.fileId = fileId;
        }

        public void setGroupId(final long groupId) {
            this.groupId = groupId;
        }
    }
    public static class StopCustomizingGroupFileResponseDTO{
    	private String message;

		public String getMessage() {
			return message;
		}

		public void setMessage(String message) {
			this.message = message;
		}
		}
    

    @RequestMapping(value = "/member/stopCustomizingGroupFile" , method = RequestMethod.DELETE)
    @ResponseBody
    protected StopCustomizingGroupFileResponseDTO executeAction(@RequestBody StopCustomizingGroupFileRequestDTO form) throws Exception {
        final long id = form.getFileId();
        final long groupId = form.getGroupId();
        if (id <= 0L || groupId <= 0L) {
            throw new ValidationException();
        }
        final CustomizedFile file = customizedFileService.load(id, CustomizedFile.Relationships.GROUP);
        if (file.getGroup() == null || !file.getGroup().getId().equals(groupId)) {
            throw new ValidationException();
        }
        customizedFileService.stopCustomizing(file);
     // Remove the physical file
        final File physicalFile = customizationHelper.customizedFileOf(file);
        customizationHelper.deleteFile(physicalFile);
        StopCustomizingGroupFileResponseDTO response = new StopCustomizingGroupFileResponseDTO();
		response.setMessage("group.customizedFiles.removed");
		return response;

    }

    }


