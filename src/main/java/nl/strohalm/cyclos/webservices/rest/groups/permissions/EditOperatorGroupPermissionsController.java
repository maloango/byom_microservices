package nl.strohalm.cyclos.webservices.rest.groups.permissions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import nl.strohalm.cyclos.access.Module;
import nl.strohalm.cyclos.access.OperatorPermission;
import nl.strohalm.cyclos.access.Permission;
import nl.strohalm.cyclos.annotations.Inject;
import nl.strohalm.cyclos.entities.accounts.MemberAccountType;
import nl.strohalm.cyclos.entities.accounts.guarantees.GuaranteeType;
import nl.strohalm.cyclos.entities.accounts.transactions.TransferType;
import nl.strohalm.cyclos.entities.groups.Group;
import nl.strohalm.cyclos.services.elements.ReferenceService;
import nl.strohalm.cyclos.services.groups.GroupService;
import nl.strohalm.cyclos.services.groups.OperatorGroupPermissionsDTO;
import nl.strohalm.cyclos.utils.access.PermissionCatalogHandler;
import nl.strohalm.cyclos.utils.binding.BeanBinder;
import nl.strohalm.cyclos.utils.binding.DataBinder;
import nl.strohalm.cyclos.utils.binding.PropertyBinder;
import nl.strohalm.cyclos.utils.binding.SimpleCollectionBinder;
import nl.strohalm.cyclos.utils.conversion.PermissionConverter;
import nl.strohalm.cyclos.utils.conversion.ReferenceConverter;
import nl.strohalm.cyclos.utils.validation.ValidationException;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;

@Controller
public class EditOperatorGroupPermissionsController extends BaseRestController {
	private GroupService groupService;

	public GroupService getGroupService() {
		return groupService;
	}

	public void setGroupService(GroupService groupService) {
		this.groupService = groupService;
	}

	public ReferenceService getReferenceService() {
		return referenceService;
	}

	enum OperatorProperty implements PermissionCollectionProperty {
		canViewInformationOf(MemberAccountType.class), conversionSimulationTTs(
				TransferType.class), guaranteeTypes(GuaranteeType.class);

		private Class<?> elementClass;

		private OperatorProperty(final Class<?> elementClass) {
			this.elementClass = elementClass;
		}

		@Override
		public String cssClassName() {
			return null;
		}

		@Override
		public String onChangeListener() {
			return null;
		}
	}

	private ReferenceService referenceService;

	private DataBinder<OperatorGroupPermissionsDTO> dataBinder;

	public DataBinder<OperatorGroupPermissionsDTO> getDataBinder() {
		if (dataBinder == null) {
			final BeanBinder<OperatorGroupPermissionsDTO> binder = BeanBinder
					.instance(OperatorGroupPermissionsDTO.class);
			binder.registerBinder("group", PropertyBinder.instance(Group.class,
					"group", ReferenceConverter.instance(Group.class)));
			binder.registerBinder("operations", SimpleCollectionBinder
					.instance(Permission.class, "operations",
							PermissionConverter.instance()));

			for (final OperatorProperty property : OperatorProperty.values()) {
				binder.registerBinder(property.name(), SimpleCollectionBinder
						.instance(property.elementClass, property.name()));
			}

			dataBinder = binder;
		}
		return dataBinder;
	}

	@Inject
	public void setReferenceService(final ReferenceService referenceService) {
		this.referenceService = referenceService;
	}

	public static class EditOperatorGroupPermissionsRequestDto {
		private long groupId;
		protected Map<String, Object> values;

		public Map<String, Object> getValues() {
			return values;
		}

		public void setValues(final Map<String, Object> values) {
			this.values = values;
		}

		public long getGroupId() {
			return groupId;
		}

		public Map<String, Object> getPermission() {
			return values;
		}

		public Object getPermission(final String key) {
			return values.get(key);
		}

		public void setGroupId(final long groupId) {
			this.groupId = groupId;
		}

		public void setPermission(final Map<String, Object> map) {
			values = map;
		}

		public void setPermission(final String key, final Object value) {
			values.put(key, value);
		}
	}

	public static class EditOperatorGroupPermissionsResponseDto {
		private String message;
		private Long id;

		public EditOperatorGroupPermissionsResponseDto(String message, Long id) {
			super();
			this.message = message;
			this.id = id;
		}

		public Long getId() {
			return id;
		}

		public void setId(Long id) {
			this.id = id;
		}

		public String getMessage() {
			return message;
		}

		public void setMessage(String message) {
			this.message = message;
		}
	}

	@RequestMapping(value = "/member/editGroupPermissions", method = RequestMethod.POST)
	@ResponseBody
	protected EditOperatorGroupPermissionsResponseDto handleSubmit(
			@RequestBody EditOperatorGroupPermissionsRequestDto form)
			throws Exception {
		// final EditGroupPermissionsForm form = context.getForm();
		final long id = form.getGroupId();
		if (id <= 0L) {
			throw new ValidationException();
		}
		final OperatorGroupPermissionsDTO dto = getDataBinder().readFromString(
				form.getPermission());
		groupService.setPermissions(dto);

		String message = "permission.modified";
		EditOperatorGroupPermissionsResponseDto response = new EditOperatorGroupPermissionsResponseDto(
				message, id);
		return response;
	}

	private void addNotAllowedPermission(
			final Map<Module, List<Permission>> notAllowedPermissionsMap,
			final OperatorPermission opPerm) {
		List<Permission> notAllowedPermissions = notAllowedPermissionsMap
				.get(opPerm.getModule());
		if (notAllowedPermissions == null) {
			notAllowedPermissions = new ArrayList<Permission>();
			notAllowedPermissionsMap.put(opPerm.getModule(),
					notAllowedPermissions);
		}

		notAllowedPermissions.add(opPerm);
	}

	private void addToMap(final Map<Permission, MultiValuesPermissionVO> map,
			final Permission permission,
			final PermissionCollectionProperty property,
			final PermissionCatalogHandler permissionCatalogHandler) {
		if (map.containsKey(permission)) {
			throw new IllegalArgumentException(
					"Permission already added to the multivalues permissions map: "
							+ permission);
		}
		map.put(permission, new MultiValuesPermissionVO(property,
				permissionCatalogHandler.currentValues(permission),
				permissionCatalogHandler.possibleValues(permission)));
	}

	private Map<Permission, MultiValuesPermissionVO> createMultiValuesPermissionsMap(
			final PermissionCatalogHandler permissionCatalogHandler,
			final Group group) {
		final Map<Permission, MultiValuesPermissionVO> map = new HashMap<Permission, MultiValuesPermissionVO>();

		addToMap(map, OperatorPermission.ACCOUNT_ACCOUNT_INFORMATION,
				OperatorProperty.canViewInformationOf, permissionCatalogHandler);
		addToMap(map, OperatorPermission.GUARANTEES_ISSUE_GUARANTEES,
				OperatorProperty.guaranteeTypes, permissionCatalogHandler);

		return map;
	}
}
