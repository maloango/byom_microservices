package nl.strohalm.cyclos.webservices.rest.customization.files;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import nl.strohalm.cyclos.annotations.Inject;
import nl.strohalm.cyclos.entities.customization.files.CustomizedFile;
import nl.strohalm.cyclos.services.customization.CustomizedFileService;
import nl.strohalm.cyclos.utils.CustomizationHelper;
import nl.strohalm.cyclos.utils.validation.ValidationException;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;
import org.springframework.web.bind.annotation.PathVariable;
@Controller
public class StopCustomizingFileController extends BaseRestController {
	
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
                public StopCustomizingResponseDTO(){
                }
    	
    }

    @RequestMapping(value ="admin/stopCustomizingFile/{fileId}",method = RequestMethod.GET)
    @ResponseBody
    protected StopCustomizingResponseDTO executeAction(@PathVariable ("fileId") long fileId) throws Exception {
        StopCustomizingResponseDTO response =null;
        try{
        final long id = fileId;
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
        response = new StopCustomizingResponseDTO(originalContents);

        response.setMessage("customizedFile.removed");}
        catch(IOException e){
            e.printStackTrace();
        }   catch (ValidationException e) {
            e.printStackTrace();
            }
       
        return response;
    }

}
