package nl.strohalm.cyclos.webservices.rest.customization.files;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.apache.struts.action.ActionForward;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import nl.strohalm.cyclos.annotations.Inject;
import nl.strohalm.cyclos.controls.ActionContext;
import nl.strohalm.cyclos.controls.customization.files.StopCustomizingFileForm;
import nl.strohalm.cyclos.controls.restapi.BaseRestController;
import nl.strohalm.cyclos.entities.customization.files.CustomizedFile;
import nl.strohalm.cyclos.services.customization.CustomizedFileService;
import nl.strohalm.cyclos.utils.ActionHelper;
import nl.strohalm.cyclos.utils.CustomizationHelper;
import nl.strohalm.cyclos.utils.validation.ValidationException;
@Controller
public class StopCustomizingController extends BaseRestController {
	
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
    
    public static class StopCustomizingRequestDTO{
    	private long              fileId;

        public long getFileId() {
            return fileId;
        }

        public void setFileId(final long fileId) {
            this.fileId = fileId;
        }
    	
    	
    }
    
    public static class StopCustomizingResponseDTO{
    	
    	String message;

		public StopCustomizingResponseDTO(String message) {
			super();
			this.message = message;
		}

		public final String getMessage() {
			return message;
		}

		public final void setMessage(String message) {
			this.message = message;
		}
    	
    }

    @RequestMapping(value ="/admin/stopCustomizingFile",method = RequestMethod.DELETE)
    @ResponseBody
    protected StopCustomizingResponseDTO executeAction(@RequestBody StopCustomizingRequestDTO form ) throws Exception {
        //final StopCustomizingFileForm form = context.getForm();
        final long id = form.getFileId();
        if (id <= 0L) {
            throw new ValidationException();
        }

        final CustomizedFile file = customizedFileService.load(id);

        String originalContents = null;
        if (file.isConflict()) {
            originalContents = file.getNewContents();
        } else {
            originalContents = file.getOriginalContents();
        }
        customizedFileService.stopCustomizing(file);

        final CustomizedFile.Type type = file.getType();
        final File customized = customizationHelper.customizedFileOf(type, file.getName());
        final File original = customizationHelper.originalFileOf(type, file.getName());
        switch (type) {
            case APPLICATION_PAGE:
                customizationHelper.updateFile(original, System.currentTimeMillis(), originalContents);
                break;
            case STYLE:
                // For style sheet files, we must copy the original back.
                customized.getParentFile().mkdirs();
                customizationHelper.updateFile(customized, System.currentTimeMillis(), FileUtils.readFileToString(original));
                break;
            default:
                // Remove the physical file
                customizationHelper.deleteFile(customized);
                break;
        }
        StopCustomizingResponseDTO response = new StopCustomizingResponseDTO(originalContents);

        response.setMessage("customizedFile.removed");
       // return ActionHelper.redirectWithParam(context.getRequest(), context.getSuccessForward(), "type", file.getType());
        return response;
    }

}
