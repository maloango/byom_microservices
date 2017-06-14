package nl.strohalm.cyclos.webservices.rest.groups.groupFilters;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import nl.strohalm.cyclos.annotations.Inject;
import nl.strohalm.cyclos.entities.groups.GroupFilter;
import nl.strohalm.cyclos.entities.groups.MemberGroup;
import nl.strohalm.cyclos.services.customization.CustomizedFileService;
import nl.strohalm.cyclos.services.groups.GroupFilterService;
import nl.strohalm.cyclos.utils.binding.BeanBinder;
import nl.strohalm.cyclos.utils.binding.DataBinder;
import nl.strohalm.cyclos.utils.binding.PropertyBinder;
import nl.strohalm.cyclos.utils.binding.SimpleCollectionBinder;
import nl.strohalm.cyclos.utils.conversion.IdConverter;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;

@Controller
public class EditGroupFilterController extends BaseRestController {
	private GroupFilterService groupFilterService;
	private DataBinder<GroupFilter> dataBinder;
	private CustomizedFileService customizedFileService;

	public DataBinder<GroupFilter> getDataBinder() {
		if (dataBinder == null) {
			final BeanBinder<GroupFilter> binder = BeanBinder
					.instance(GroupFilter.class);
			binder.registerBinder(
					"id",
					PropertyBinder.instance(Long.class, "id",
							IdConverter.instance()));
			binder.registerBinder("name",
					PropertyBinder.instance(String.class, "name"));
			binder.registerBinder("rootUrl",
					PropertyBinder.instance(String.class, "rootUrl"));
			binder.registerBinder("loginPageName",
					PropertyBinder.instance(String.class, "loginPageName"));
			binder.registerBinder("containerUrl",
					PropertyBinder.instance(String.class, "containerUrl"));
			binder.registerBinder("description",
					PropertyBinder.instance(String.class, "description"));
			binder.registerBinder("showInProfile",
					PropertyBinder.instance(Boolean.TYPE, "showInProfile"));
			binder.registerBinder("groups", SimpleCollectionBinder.instance(
					MemberGroup.class, "groups"));
			binder.registerBinder("viewableBy", SimpleCollectionBinder
					.instance(MemberGroup.class, "viewableBy"));
			dataBinder = binder;
		}
		return dataBinder;
	}

	@Inject
	public void setCustomizedFileService(
			final CustomizedFileService customizedFileService) {
		this.customizedFileService = customizedFileService;
	}

	@Inject
	public void setGroupFilterService(
			final GroupFilterService groupFilterService) {
		this.groupFilterService = groupFilterService;
	}

	public static class EditGroupFilterRequestDto {
		private long groupFilterId;

		protected Map<String, Object> values;

		public Map<String, Object> getValues() {
			return values;
		}

		public void setValues(final Map<String, Object> values) {
			this.values = values;
		}

		public Map<String, Object> getGroupFilter() {
			return values;
		}

		public Object getGroupFilter(final String key) {
			return values.get(key);
		}

		public long getGroupFilterId() {
			return groupFilterId;
		}

		public void setGroupFilter(final Map<String, Object> map) {
			values = map;
		}

		public void setGroupFilter(final String key, final Object value) {
			values.put(key, value);
		}

		public void setGroupFilterId(final long groupFilterId) {
			this.groupFilterId = groupFilterId;
		}

	}

	public static class EditGroupFilterResponseDto {
		public String message;
		Map<String, Object> params;

		public EditGroupFilterResponseDto(String message,
				Map<String, Object> params) {
			super();
			this.message = message;
			this.params = params;
		}

		public String getMessage() {
			return message;
		}

		public void setMessage(String message) {
			this.message = message;
		}
	}

	@RequestMapping(value = "/admin/editGroupFilter", method = RequestMethod.PUT)
	@ResponseBody
	protected EditGroupFilterResponseDto handleSubmit(
			final EditGroupFilterRequestDto form) throws Exception {
		// final EditGroupFilterForm form = context.getForm();
		GroupFilter groupFilter = getDataBinder().readFromString(
				form.getGroupFilter());
		final boolean isInsert = (groupFilter.getId() == null);
		groupFilter = groupFilterService.save(groupFilter);
		String message = null;
		if (isInsert) {
			message = "groupFilter.inserted";
		} else {
			message = "groupFilter.modified";
		}
		final Map<String, Object> params = new HashMap<String, Object>();
		params.put("groupFilterId", groupFilter.getId());
		EditGroupFilterResponseDto response = new EditGroupFilterResponseDto(
				message, params);
		return response;
	}
}
