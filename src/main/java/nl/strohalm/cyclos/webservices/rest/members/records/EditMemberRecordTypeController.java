package nl.strohalm.cyclos.webservices.rest.members.records;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import nl.strohalm.cyclos.access.AdminSystemPermission;
import nl.strohalm.cyclos.annotations.Inject;
import nl.strohalm.cyclos.controls.ActionContext;
import nl.strohalm.cyclos.controls.members.records.EditMemberRecordTypeForm;
import nl.strohalm.cyclos.entities.groups.Group;
import nl.strohalm.cyclos.entities.groups.GroupQuery;
import nl.strohalm.cyclos.entities.groups.MemberGroup;
import nl.strohalm.cyclos.entities.members.records.MemberRecordType;
import nl.strohalm.cyclos.services.elements.ElementService;
import nl.strohalm.cyclos.services.elements.MemberRecordTypeService;
import nl.strohalm.cyclos.services.groups.GroupService;
import nl.strohalm.cyclos.services.permissions.PermissionService;
import nl.strohalm.cyclos.utils.ActionHelper;
import nl.strohalm.cyclos.utils.RequestHelper;
import nl.strohalm.cyclos.utils.binding.BeanBinder;
import nl.strohalm.cyclos.utils.binding.DataBinder;
import nl.strohalm.cyclos.utils.binding.PropertyBinder;
import nl.strohalm.cyclos.utils.binding.SimpleCollectionBinder;
import nl.strohalm.cyclos.utils.conversion.IdConverter;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;

import org.apache.struts.action.ActionForward;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class EditMemberRecordTypeController extends BaseRestController {
	private MemberRecordTypeService memberRecordTypeService;
	private DataBinder<MemberRecordType> dataBinder;
	private GroupService groupService;
	private ElementService elementService;
	private PermissionService permissionService;

	@Inject
	public void setMemberRecordTypeService(
			final MemberRecordTypeService memberRecordTypeService) {
		this.memberRecordTypeService = memberRecordTypeService;
	}

	public static class EditMemberRecordTypeRequestDto {
		private long memberRecordTypeId;
		protected Map<String, Object> values;

		public Map<String, Object> getValues() {
			return values;
		}

		public void setValues(final Map<String, Object> values) {
			this.values = values;
		}

		public Map<String, Object> getMemberRecordType() {
			return values;
		}

		public Object getMemberRecordType(final String key) {
			return values.get(key);
		}

		public long getMemberRecordTypeId() {
			return memberRecordTypeId;
		}

		public void setMemberRecordType(final Map<String, Object> map) {
			values = map;
		}

		public void setMemberRecordType(final String key, final Object value) {
			values.put(key, value);
		}

		public void setMemberRecordTypeId(final long memberRecordTypeId) {
			this.memberRecordTypeId = memberRecordTypeId;
		}

	}

	public static class EditMemberRecordTypeResponseDto {
		private String message;
		private Long id;

		public EditMemberRecordTypeResponseDto(String message, Long id) {
			super();
			this.message = message;
			this.id = id;
		}

		public String getMessage() {
			return message;
		}

		public void setMessage(String message) {
			this.message = message;
		}
	}

	@RequestMapping(value = "admin/editMemberRecordType", method = RequestMethod.PUT)
	@ResponseBody
	protected EditMemberRecordTypeResponseDto handleSubmit(
			@RequestBody EditMemberRecordTypeRequestDto form) throws Exception {
		EditMemberRecordTypeResponseDto response = null;
                try{
		MemberRecordType memberRecordType = getDataBinder().readFromString(
				form.getMemberRecordType());
		final boolean isInsert = memberRecordType.isTransient();
		memberRecordType = memberRecordTypeService.save(memberRecordType);
		String message = null;
		if (isInsert) {
			message = "memberRecordType.inserted";
		} else {
			message = "memberRecordType.modified";
		}
		Long id = memberRecordType.getId();
		response = new EditMemberRecordTypeResponseDto(
				message, id);}
                catch(Exception e){
                    e.printStackTrace();
                }
		return response;
	}

	protected void prepareForm(final ActionContext context) throws Exception {
		final HttpServletRequest request = context.getRequest();
		final EditMemberRecordTypeForm form = context.getForm();

		final long id = form.getMemberRecordTypeId();
		final boolean isInsert = id <= 0L;
		boolean editable = permissionService
				.hasPermission(AdminSystemPermission.MEMBER_RECORD_TYPES_MANAGE);
		MemberRecordType memberRecordType;
		if (isInsert) {
			memberRecordType = new MemberRecordType();
			editable = true;
		} else {
			memberRecordType = memberRecordTypeService.load(id,
					MemberRecordType.Relationships.FIELDS,
					MemberRecordType.Relationships.GROUPS);
		}
		getDataBinder().writeAsString(form.getMemberRecordType(),
				memberRecordType);
		request.setAttribute("memberRecordType", memberRecordType);
		request.setAttribute("editable", editable);
		request.setAttribute("isInsert", isInsert);

		// Search groups and send to JSP
		final GroupQuery groupQuery = new GroupQuery();
		groupQuery.setNatures(Group.Nature.MEMBER, Group.Nature.BROKER,
				Group.Nature.ADMIN);
		groupQuery.setStatus(Group.Status.NORMAL);
		final List<? extends Group> groups = groupService.search(groupQuery);
		request.setAttribute("groups", groups);

		// Send layouts enum to JSP
		RequestHelper.storeEnum(request, MemberRecordType.Layout.class,
				"layouts");
	}

	protected void validateForm(final ActionContext context) {
		final EditMemberRecordTypeForm form = context.getForm();
		final MemberRecordType memberRecordType = getDataBinder()
				.readFromString(form.getMemberRecordType());
		memberRecordTypeService.validate(memberRecordType);
	}

	private DataBinder<MemberRecordType> getDataBinder() {
		if (dataBinder == null) {
			final BeanBinder<MemberRecordType> binder = BeanBinder
					.instance(MemberRecordType.class);
			binder.registerBinder(
					"id",
					PropertyBinder.instance(Long.class, "id",
							IdConverter.instance()));
			binder.registerBinder("name",
					PropertyBinder.instance(String.class, "name"));
			binder.registerBinder("label",
					PropertyBinder.instance(String.class, "label"));
			binder.registerBinder("description",
					PropertyBinder.instance(String.class, "description"));
			binder.registerBinder("groups", SimpleCollectionBinder.instance(
					MemberGroup.class, "groups"));
			binder.registerBinder("layout", PropertyBinder.instance(
					MemberRecordType.Layout.class, "layout"));
			binder.registerBinder("editable",
					PropertyBinder.instance(Boolean.TYPE, "editable"));
			binder.registerBinder("showMenuItem",
					PropertyBinder.instance(Boolean.TYPE, "showMenuItem"));
			dataBinder = binder;
		}
		return dataBinder;
	}
}
