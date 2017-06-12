package nl.strohalm.cyclos.webservices.rest.groups.groupFilters;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts.action.ActionForward;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import nl.strohalm.cyclos.annotations.Inject;
import nl.strohalm.cyclos.controls.ActionContext;
import nl.strohalm.cyclos.entities.groups.GroupFilter;
import nl.strohalm.cyclos.entities.groups.GroupFilterQuery;
import nl.strohalm.cyclos.services.groups.GroupFilterService;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;

@Controller
public class ListGroupFiltersController extends BaseRestController {
	private GroupFilterService groupFilterService;

	@Inject
	public void setGroupFilterService(
			final GroupFilterService groupFilterService) {
		this.groupFilterService = groupFilterService;
	}

	public static class ListGroupFiltersRequestDto {

	}

	public static class ListGroupFiltersResponseDto {
		private List<GroupFilter> groupFilters;

		public ListGroupFiltersResponseDto(List<GroupFilter> groupFilters) {
			super();
			this.groupFilters = groupFilters;
		}

	}

	@RequestMapping(value = "/admin/managePasswords", method = RequestMethod.GET)
	@ResponseBody
	protected ListGroupFiltersResponseDto executeAction(
			@RequestBody ListGroupFiltersRequestDto context) throws Exception {
		// final HttpServletRequest request = context.getRequest();

		final List<GroupFilter> groupFilters = groupFilterService
				.search(new GroupFilterQuery());
		// request.setAttribute("groupFilters", groupFilters);
		ListGroupFiltersResponseDto response = new ListGroupFiltersResponseDto(
				groupFilters);
		return response;
	}
}
