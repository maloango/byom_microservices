package nl.strohalm.cyclos.webservices.rest.members.records;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import nl.strohalm.cyclos.access.AdminSystemPermission;
import nl.strohalm.cyclos.annotations.Inject;
import nl.strohalm.cyclos.entities.members.records.MemberRecordType;
import nl.strohalm.cyclos.entities.members.records.MemberRecordTypeQuery;
import nl.strohalm.cyclos.services.elements.MemberRecordTypeService;
import nl.strohalm.cyclos.services.permissions.PermissionService;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;

@Controller
public class ListMemberRecordTypesController extends BaseRestController {

	private MemberRecordTypeService memberRecordTypeService;
	private PermissionService permissionService;

	@Inject
	public void setMemberRecordTypeService(
			final MemberRecordTypeService memberRecordTypeService) {
		this.memberRecordTypeService = memberRecordTypeService;
	}

	public static class ListMemberRecordTypesRequestDto {

	}

	public static class ListMemberRecordTypesResponseDto {

		private List<MemberRecordType> memberRecordTypes;
		private boolean editable;

		public ListMemberRecordTypesResponseDto(
				List<MemberRecordType> memberRecordTypes, boolean editable) {
			super();
			this.memberRecordTypes = memberRecordTypes;
			this.editable = editable;
		}

	}

	@RequestMapping(value = "/admin/managePasswords", method = RequestMethod.POST)
	@ResponseBody
	protected ListMemberRecordTypesResponseDto executeAction(
			@RequestBody ListMemberRecordTypesRequestDto form) throws Exception {
		// final HttpServletRequest request = context.getRequest();
		final List<MemberRecordType> memberRecordTypes = memberRecordTypeService
				.search(new MemberRecordTypeQuery());

		boolean editable = permissionService
				.hasPermission(AdminSystemPermission.MEMBER_RECORD_TYPES_MANAGE);
		ListMemberRecordTypesResponseDto response = new ListMemberRecordTypesResponseDto(
				memberRecordTypes, editable);
		return response;
	}
}
