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

@Controller
public class RemoveGroupFilterController extends BaseRestController {
	private GroupFilterService groupFilterService;
	public final GroupFilterService getGroupFilterService() {
            
		return groupFilterService;
	}

    public CustomizationHelper getCustomizationHelper() {
        return customizationHelper;
    }

	private CustomizationHelper customizationHelper;

	@Inject
	public void setCustomizationHelper(
			final CustomizationHelper customizationHelper) {
		this.customizationHelper = customizationHelper;
	}

	@Inject
	public void setGroupFilterService(
			final GroupFilterService groupFilterService) {
		this.groupFilterService = groupFilterService;
	}

	public static class RemoveGroupFilterRequestDto {
		private long groupFilterId;

		public long getGroupFilterId() {
			return groupFilterId;
		}

		public void setGroupFilterId(final long groupFilterId) {
			this.groupFilterId = groupFilterId;
		}

	}

	public static class RemoveGroupFilterResponseDto {
		private String message;

		public String getMessage() {
			return message;
		}

		public void setMessage(String message) {
			this.message = message;
		}
	}

	@RequestMapping(value = "admin/removeGroupFilter", method = RequestMethod.POST)
	@ResponseBody
	protected RemoveGroupFilterResponseDto executeAction(@RequestBody RemoveGroupFilterRequestDto form) throws Exception {
			
		RemoveGroupFilterResponseDto response = new RemoveGroupFilterResponseDto();
                try{
		final long id = form.getGroupFilterId();
		 GroupFilter groupFilter = groupFilterService.load(id,GroupFilter.Relationships.CUSTOMIZED_FILES);
				
		final Collection<CustomizedFile> customizedFiles = groupFilter
				.getCustomizedFiles();
		groupFilterService.remove(id);

		
		for (final CustomizedFile customizedFile : customizedFiles) {
			final File physicalFile = customizationHelper
					.customizedFileOf(customizedFile);
			customizationHelper.deleteFile(physicalFile);
		}
		response = new RemoveGroupFilterResponseDto();
		response.setMessage("groupFilter.removed");}
                catch(Exception e){
                    e.printStackTrace();
                }
		return response;
	}
}
