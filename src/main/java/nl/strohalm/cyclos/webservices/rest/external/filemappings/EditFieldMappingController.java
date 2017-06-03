package nl.strohalm.cyclos.webservices.rest.external.filemappings;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import nl.strohalm.cyclos.access.AdminSystemPermission;
import nl.strohalm.cyclos.annotations.Inject;
import nl.strohalm.cyclos.controls.ActionContext;
import nl.strohalm.cyclos.controls.accounts.external.filemappings.EditFieldMappingForm;
import nl.strohalm.cyclos.entities.accounts.MemberAccountType;
import nl.strohalm.cyclos.entities.accounts.external.ExternalAccount;
import nl.strohalm.cyclos.entities.accounts.external.filemapping.FieldMapping;
import nl.strohalm.cyclos.entities.accounts.external.filemapping.FieldMapping.Field;
import nl.strohalm.cyclos.entities.accounts.external.filemapping.FileMapping;
import nl.strohalm.cyclos.entities.accounts.external.filemapping.FileMappingWithFields;
import nl.strohalm.cyclos.entities.customization.fields.MemberCustomField;
import nl.strohalm.cyclos.entities.groups.GroupQuery;
import nl.strohalm.cyclos.entities.groups.MemberGroup;
import nl.strohalm.cyclos.services.accounts.external.filemapping.FieldMappingService;
import nl.strohalm.cyclos.services.accounts.external.filemapping.FileMappingService;
import nl.strohalm.cyclos.services.customization.MemberCustomFieldService;
import nl.strohalm.cyclos.services.permissions.PermissionService;
import nl.strohalm.cyclos.utils.ActionHelper;
import nl.strohalm.cyclos.utils.CustomFieldHelper;
import nl.strohalm.cyclos.utils.RelationshipHelper;
import nl.strohalm.cyclos.utils.RequestHelper;
import nl.strohalm.cyclos.utils.binding.BeanBinder;
import nl.strohalm.cyclos.utils.binding.DataBinder;
import nl.strohalm.cyclos.utils.binding.PropertyBinder;
import nl.strohalm.cyclos.utils.conversion.IdConverter;
import nl.strohalm.cyclos.utils.conversion.ReferenceConverter;
import nl.strohalm.cyclos.utils.validation.ValidationException;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;
import nl.strohalm.cyclos.webservices.rest.access.ResetAndSendPasswordController.ResetAndSendPasswordRequestDto;

import org.apache.struts.action.ActionForward;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;

@Controller
public class EditFieldMappingController extends BaseRestController {
	private MemberCustomFieldService memberCustomFieldService;
	private FieldMappingService fieldMappingService;
	private FileMappingService fileMappingService;
	private DataBinder<FieldMapping> dataBinder;
	private PermissionService permissionService;

	private CustomFieldHelper customFieldHelper;

	public DataBinder<FieldMapping> getDataBinder() {
		if (dataBinder == null) {
			final BeanBinder<FieldMapping> fieldMappingBinder = BeanBinder
					.instance(FieldMapping.class);
			fieldMappingBinder.registerBinder(
					"id",
					PropertyBinder.instance(Long.class, "id",
							IdConverter.instance()));
			fieldMappingBinder.registerBinder("fileMapping", PropertyBinder
					.instance(FileMapping.class, "fileMapping",
							ReferenceConverter.instance(FileMapping.class)));
			fieldMappingBinder.registerBinder("order",
					PropertyBinder.instance(Integer.TYPE, "order"));
			fieldMappingBinder.registerBinder("name",
					PropertyBinder.instance(String.class, "name"));
			fieldMappingBinder.registerBinder("field",
					PropertyBinder.instance(FieldMapping.Field.class, "field"));
			fieldMappingBinder.registerBinder("memberField", PropertyBinder
					.instance(MemberCustomField.class, "memberField",
							ReferenceConverter
									.instance(MemberCustomField.class)));
			dataBinder = fieldMappingBinder;
		}
		return dataBinder;
	}

	@Inject
	public void setCustomFieldHelper(final CustomFieldHelper customFieldHelper) {
		this.customFieldHelper = customFieldHelper;
	}

	@Inject
	public void setFieldMappingService(
			final FieldMappingService fieldMappingService) {
		this.fieldMappingService = fieldMappingService;
	}

	@Inject
	public void setFileMappingService(
			final FileMappingService fileMappingService) {
		this.fileMappingService = fileMappingService;
	}

	@Inject
	public void setMemberCustomFieldService(
			final MemberCustomFieldService memberCustomFieldService) {
		this.memberCustomFieldService = memberCustomFieldService;
	}

	public static class EditFieldMappingRequestDto {

	}

	public static class EditFieldMappingResponseDto {

	}

	protected EditFieldMappingResponseDto handleSubmit(@RequestBody ResetAndSendPasswordRequestDto form)
			throws Exception {
		//final EditFieldMappingForm form = context.getForm();
		FieldMapping fieldMapping = getDataBinder().readFromString(
				form.getFieldMapping());
		final boolean isInsert = fieldMapping.isTransient();
		fieldMapping = fieldMappingService.save(fieldMapping);
		context.sendMessage(isInsert ? "fieldMapping.inserted"
				: "fieldMapping.modified");
		final Long externalAccountId = fieldMapping.getFileMapping()
				.getAccount().getId();
		return ActionHelper.redirectWithParam(context.getRequest(),
				context.getSuccessForward(), "externalAccountId",
				externalAccountId);
	}

	protected void prepareForm(final ActionContext context) throws Exception {
		final HttpServletRequest request = context.getRequest();
		final EditFieldMappingForm form = context.getForm();

		final Long fileMappingId = form.getFileMappingId();
		if (fileMappingId <= 0) {
			throw new ValidationException();
		}
		final FileMapping fileMapping = fileMappingService.load(fileMappingId,
				FileMappingWithFields.Relationships.FIELDS,
				RelationshipHelper.nested(
						FileMapping.Relationships.EXTERNAL_ACCOUNT,
						ExternalAccount.Relationships.MEMBER_ACCOUNT_TYPE));
		final MemberAccountType memberAccountType = fileMapping.getAccount()
				.getMemberAccountType();

		final long fieldMappingId = form.getFieldMappingId();
		final boolean isInsert = (fieldMappingId <= 0);
		FieldMapping fieldMapping = null;
		if (isInsert) {
			fieldMapping = new FieldMapping();
			fieldMapping.setFileMapping(fileMapping);
		} else {
			fieldMapping = fieldMappingService.load(fieldMappingId);
		}
		getDataBinder().writeAsString(form.getFieldMapping(), fieldMapping);
		request.setAttribute("fieldMapping", fieldMapping);
		request.setAttribute("editable", permissionService
				.hasPermission(AdminSystemPermission.EXTERNAL_ACCOUNTS_MANAGE));
		request.setAttribute("isInsert", isInsert);
		RequestHelper.storeEnum(request,
				FileMappingWithFields.NumberFormat.class, "numberFormats");

		final Set<FieldMapping.Field> fields = EnumSet
				.allOf(FieldMapping.Field.class);
		final Set<Field> memberIdentificationFields = EnumSet.of(
				Field.MEMBER_ID, Field.MEMBER_USERNAME,
				Field.MEMBER_CUSTOM_FIELD);
		if (fileMapping instanceof FileMappingWithFields) {
			final FileMappingWithFields fileWithFields = (FileMappingWithFields) fileMapping;
			for (final FieldMapping current : fileWithFields.getFields()) {
				final Field field = current.getField();
				// The only field which may be duplicated is IGNORED
				if (field == Field.IGNORED) {
					continue;
				}
				// The fields that identify the member may appear only once
				if (memberIdentificationFields.contains(field)
						&& !memberIdentificationFields.contains(fieldMapping
								.getField())) {
					fields.removeAll(memberIdentificationFields);
				} else {
					fields.remove(field);
				}
			}
			if (!isInsert) {
				fields.add(fieldMapping.getField()); // The field that was
														// previously selected
														// should always be
														// present
			}
		}
		request.setAttribute("fields", fields);
		// Fetch the custom fields when they can be used
		if (fields.contains(Field.MEMBER_CUSTOM_FIELD)) {
			final List<MemberCustomField> memberCustomFields = getMemberCustomFields(memberAccountType);
			request.setAttribute("memberFields", memberCustomFields);
		}
	}

	protected void validateForm(final ActionContext context) {
		final EditFieldMappingForm form = context.getForm();
		final FieldMapping fieldMapping = getDataBinder().readFromString(
				form.getFieldMapping());
		fieldMappingService.validate(fieldMapping);
	}

	@SuppressWarnings("unchecked")
	private List<MemberCustomField> getMemberCustomFields(
			final MemberAccountType memberAccountType) {
		final GroupQuery groupQuery = new GroupQuery();
		groupQuery.setMemberAccountType(memberAccountType);
		final List<MemberGroup> memberGroups = (List<MemberGroup>) groupService
				.search(groupQuery);

		List<MemberCustomField> memberFields = memberCustomFieldService.list();
		memberFields = customFieldHelper.onlyForGroups(memberFields,
				memberGroups);
		return memberFields;
	}

}
