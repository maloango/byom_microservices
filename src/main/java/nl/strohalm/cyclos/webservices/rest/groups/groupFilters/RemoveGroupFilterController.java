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

	@RequestMapping(value = "/admin/removeGroupFilter", method = RequestMethod.POST)
	@ResponseBody
	protected RemoveGroupFilterResponseDto executeAction(
			@RequestBody RemoveGroupFilterRequestDto form) throws Exception {
		// final RemoveGroupFilterForm form = context.getForm();

		// Remove the group filter
		final long id = form.getGroupFilterId();
		final GroupFilter groupFilter = groupFilterService.load(id,
				GroupFilter.Relationships.CUSTOMIZED_FILES);
		final Collection<CustomizedFile> customizedFiles = groupFilter
				.getCustomizedFiles();
		groupFilterService.remove(id);

		// Remove the physical customized files for the group
		for (final CustomizedFile customizedFile : customizedFiles) {
			final File physicalFile = customizationHelper
					.customizedFileOf(customizedFile);
			customizationHelper.deleteFile(physicalFile);
		}
		RemoveGroupFilterResponseDto response = new RemoveGroupFilterResponseDto();
		response.setMessage("groupFilter.removed");
		return response;
	}
}
