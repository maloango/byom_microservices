package nl.strohalm.cyclos.webservices.rest.members.records;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import nl.strohalm.cyclos.access.AdminAdminPermission;
import nl.strohalm.cyclos.access.AdminMemberPermission;
import nl.strohalm.cyclos.access.BrokerPermission;
import nl.strohalm.cyclos.annotations.Inject;
import nl.strohalm.cyclos.controls.ActionContext;
//import nl.strohalm.cyclos.controls.members.records.SearchMemberRecordsForm;
import nl.strohalm.cyclos.entities.customization.fields.MemberRecordCustomField;
import nl.strohalm.cyclos.entities.customization.fields.MemberRecordCustomFieldValue;
import nl.strohalm.cyclos.entities.groups.AdminGroup;
import nl.strohalm.cyclos.entities.groups.BrokerGroup;
import nl.strohalm.cyclos.entities.groups.Group;
import nl.strohalm.cyclos.entities.members.Administrator;
import nl.strohalm.cyclos.entities.members.Element;
import nl.strohalm.cyclos.entities.members.Member;
import nl.strohalm.cyclos.entities.members.records.FullTextMemberRecordQuery;
import nl.strohalm.cyclos.entities.members.records.MemberRecord;
import nl.strohalm.cyclos.entities.members.records.MemberRecordType;
import nl.strohalm.cyclos.entities.settings.LocalSettings;
import nl.strohalm.cyclos.services.customization.MemberRecordCustomFieldService;
import nl.strohalm.cyclos.services.elements.ElementService;
import nl.strohalm.cyclos.services.elements.MemberRecordService;
import nl.strohalm.cyclos.services.elements.MemberRecordTypeService;
import nl.strohalm.cyclos.services.groups.GroupService;
import nl.strohalm.cyclos.services.permissions.PermissionService;
import nl.strohalm.cyclos.services.settings.SettingsService;
import nl.strohalm.cyclos.utils.CustomFieldHelper;
import nl.strohalm.cyclos.utils.RequestHelper;
import nl.strohalm.cyclos.utils.binding.BeanBinder;
import nl.strohalm.cyclos.utils.binding.BeanCollectionBinder;
import nl.strohalm.cyclos.utils.binding.DataBinder;
import nl.strohalm.cyclos.utils.binding.DataBinderHelper;
import nl.strohalm.cyclos.utils.binding.PropertyBinder;
import nl.strohalm.cyclos.utils.query.QueryParameters;
import nl.strohalm.cyclos.utils.validation.ValidationException;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class SearchMemberRecordsController extends BaseRestController {
	public static BeanBinder<FullTextMemberRecordQuery> memberRecordQueryDataBinder(
			final LocalSettings localSettings) {
		final BeanBinder<MemberRecordCustomFieldValue> customValuesBinder = BeanBinder
				.instance(MemberRecordCustomFieldValue.class);
		customValuesBinder
				.registerBinder("field", PropertyBinder.instance(
						MemberRecordCustomField.class, "field"));
		customValuesBinder.registerBinder("value",
				PropertyBinder.instance(String.class, "value"));

		final BeanBinder<FullTextMemberRecordQuery> binder = BeanBinder
				.instance(FullTextMemberRecordQuery.class);
		binder.registerBinder("element",
				PropertyBinder.instance(Element.class, "element"));
		binder.registerBinder("broker",
				PropertyBinder.instance(Member.class, "broker"));
		binder.registerBinder("period",
				DataBinderHelper.periodBinder(localSettings, "period"));
		binder.registerBinder("keywords",
				PropertyBinder.instance(String.class, "keywords"));
		binder.registerBinder("customValues", BeanCollectionBinder.instance(
				customValuesBinder, "customValues"));
		binder.registerBinder("pageParameters", DataBinderHelper.pageBinder());
		return binder;
	}

	private DataBinder<FullTextMemberRecordQuery> dataBinder;
	private MemberRecordService memberRecordService;
	private MemberRecordTypeService memberRecordTypeService;
	private MemberRecordCustomFieldService memberRecordCustomFieldService;
	private GroupService groupService;
	private ElementService elementService;
	private PermissionService permissionService;
	private SettingsService settingsService;
	private CustomFieldHelper customFieldHelper;

	public DataBinder<FullTextMemberRecordQuery> getDataBinder() {
		if (dataBinder == null) {
			final LocalSettings localSettings = settingsService
					.getLocalSettings();
			dataBinder = memberRecordQueryDataBinder(localSettings);
		}
		return dataBinder;
	}

	@Inject
	public void setCustomFieldHelper(final CustomFieldHelper customFieldHelper) {
		this.customFieldHelper = customFieldHelper;
	}

	@Inject
	public void setMemberRecordCustomFieldService(
			final MemberRecordCustomFieldService memberRecordCustomFieldService) {
		this.memberRecordCustomFieldService = memberRecordCustomFieldService;
	}

	@Inject
	public void setMemberRecordService(
			final MemberRecordService memberRecordService) {
		this.memberRecordService = memberRecordService;
	}

	@Inject
	public void setMemberRecordTypeService(
			final MemberRecordTypeService memberRecordTypeService) {
		this.memberRecordTypeService = memberRecordTypeService;
	}

	public static class SearchMemberRecordsRequestDto {

	}

	public static class SearchMemberRecordsResponseDto {
		private List<MemberRecord> memberRecords;

		public SearchMemberRecordsResponseDto(List<MemberRecord> memberRecords) {
			super();
			this.memberRecords = memberRecords;
		}
	}

	@RequestMapping(value = "admin/searchMemberRecords", method = RequestMethod.GET)
	@ResponseBody
	protected SearchMemberRecordsResponseDto executeQuery(
			@RequestBody SearchMemberRecordsRequestDto form,
			final QueryParameters queryParameters) {
		SearchMemberRecordsResponseDto response = null;
                try{
		final FullTextMemberRecordQuery query = (FullTextMemberRecordQuery) queryParameters;
		final List<MemberRecord> memberRecords = memberRecordService
				.fullTextSearch(query);
		response = new SearchMemberRecordsResponseDto(memberRecords);}
				
        catch(Exception e){
            e.printStackTrace();
}
		return response;
	}

//	protected QueryParameters prepareForm(final ActionContext context) {
//		final HttpServletRequest request = context.getRequest();
//		final SearchMemberRecordsForm form = context.getForm();
//
//		final long typeId = form.getTypeId();
//		if (typeId <= 0L) {
//			throw new ValidationException();
//		}
//		final MemberRecordType type = memberRecordTypeService.load(typeId);
//		request.setAttribute("type", type);
//
//		final boolean global = form.isGlobal();
//
//		// Fetch the element
//		Element element = null;
//		if (!global) {
//			final long elementId = form.getElementId();
//			if (elementId <= 0L) {
//				throw new ValidationException();
//			}
//			element = elementService.load(elementId,
//					Element.Relationships.GROUP);
//		}
//		request.setAttribute("element", element);
//
//		// Retrieve the query
//		final FullTextMemberRecordQuery query = getDataBinder().readFromString(
//				form.getQuery());
//		query.setType(type);
//		if (global) {
//			if (form.getQueryElementId() > 0L) {
//				final Element queryElement = elementService.load(form
//						.getQueryElementId());
//				query.setElement(queryElement);
//			}
//			if (context.isBroker()) {
//				query.setBroker(context.getMember());
//			} else if (context.isAdmin() && query.getBroker() != null) {
//				final Member broker = elementService.load(query.getBroker()
//						.getId());
//				query.setBroker(broker);
//			}
//		} else {
//			query.setElement(element);
//		}
//		query.fetch(MemberRecord.Relationships.TYPE);
//
//		// Retrieve the custom fields
//		final List<MemberRecordCustomField> customFields = memberRecordCustomFieldService
//				.list(type);
//		final List<MemberRecordCustomField> fieldsForSearch = new ArrayList<MemberRecordCustomField>();
//		final List<MemberRecordCustomField> fieldsOnList = new ArrayList<MemberRecordCustomField>();
//		for (final MemberRecordCustomField field : customFields) {
//			if (field.isShowInSearch()) {
//				fieldsForSearch.add(field);
//			}
//			if (field.isShowInList()) {
//				fieldsOnList.add(field);
//			}
//		}
//		request.setAttribute(
//				"customValues",
//				customFieldHelper.buildEntries(fieldsForSearch,
//						query.getCustomValues()));
//		request.setAttribute("fieldsOnList", fieldsOnList);
//
//		// Check permissions for logged user
//		final Group group = context.getGroup();
//		boolean canCreate = false;
//		boolean canModify = false;
//		boolean canDelete = false;
//		if (context.isAdmin()) {
//			AdminGroup adminGroup = (AdminGroup) group;
//			adminGroup = groupService.load(adminGroup.getId(),
//					AdminGroup.Relationships.CREATE_MEMBER_RECORD_TYPES,
//					AdminGroup.Relationships.MODIFY_MEMBER_RECORD_TYPES,
//					AdminGroup.Relationships.DELETE_MEMBER_RECORD_TYPES,
//					AdminGroup.Relationships.CREATE_ADMIN_RECORD_TYPES,
//					AdminGroup.Relationships.MODIFY_ADMIN_RECORD_TYPES,
//					AdminGroup.Relationships.DELETE_ADMIN_RECORD_TYPES);
//			if (global) {
//				canCreate = (permissionService
//						.hasPermission(AdminMemberPermission.RECORDS_CREATE) && adminGroup
//						.getCreateMemberRecordTypes().contains(type))
//						|| (permissionService
//								.hasPermission(AdminAdminPermission.RECORDS_CREATE) && adminGroup
//								.getCreateAdminRecordTypes().contains(type));
//				canModify = (permissionService
//						.hasPermission(AdminMemberPermission.RECORDS_MODIFY) && adminGroup
//						.getModifyMemberRecordTypes().contains(type))
//						|| (permissionService
//								.hasPermission(AdminAdminPermission.RECORDS_MODIFY) && adminGroup
//								.getModifyAdminRecordTypes().contains(type));
//				canDelete = (permissionService
//						.hasPermission(AdminMemberPermission.RECORDS_DELETE) && adminGroup
//						.getDeleteMemberRecordTypes().contains(type))
//						|| (permissionService
//								.hasPermission(AdminAdminPermission.RECORDS_DELETE) && adminGroup
//								.getDeleteAdminRecordTypes().contains(type));
//			} else if (element instanceof Member) {
//				canCreate = permissionService
//						.hasPermission(AdminMemberPermission.RECORDS_CREATE)
//						&& adminGroup.getCreateMemberRecordTypes().contains(
//								type);
//				canModify = permissionService
//						.hasPermission(AdminMemberPermission.RECORDS_MODIFY)
//						&& adminGroup.getModifyMemberRecordTypes().contains(
//								type);
//				canDelete = permissionService
//						.hasPermission(AdminMemberPermission.RECORDS_DELETE)
//						&& adminGroup.getDeleteMemberRecordTypes().contains(
//								type);
//			} else if (element instanceof Administrator) {
//				canCreate = permissionService
//						.hasPermission(AdminAdminPermission.RECORDS_CREATE)
//						&& adminGroup.getCreateAdminRecordTypes()
//								.contains(type);
//				canModify = permissionService
//						.hasPermission(AdminAdminPermission.RECORDS_MODIFY)
//						&& adminGroup.getModifyAdminRecordTypes()
//								.contains(type);
//				canDelete = permissionService
//						.hasPermission(AdminAdminPermission.RECORDS_DELETE)
//						&& adminGroup.getDeleteAdminRecordTypes()
//								.contains(type);
//			}
//		} else if ((element instanceof Member)
//				&& context.isBrokerOf((Member) element)) {
//			BrokerGroup brokerGroup = (BrokerGroup) group;
//			brokerGroup = groupService
//					.load(brokerGroup.getId(),
//							BrokerGroup.Relationships.BROKER_MEMBER_RECORD_TYPES,
//							BrokerGroup.Relationships.BROKER_CREATE_MEMBER_RECORD_TYPES,
//							BrokerGroup.Relationships.BROKER_MODIFY_MEMBER_RECORD_TYPES,
//							BrokerGroup.Relationships.BROKER_DELETE_MEMBER_RECORD_TYPES);
//			canCreate = permissionService
//					.hasPermission(BrokerPermission.MEMBER_RECORDS_CREATE)
//					&& brokerGroup.getBrokerCreateMemberRecordTypes().contains(
//							type);
//			canModify = permissionService
//					.hasPermission(BrokerPermission.MEMBER_RECORDS_MODIFY)
//					&& brokerGroup.getBrokerModifyMemberRecordTypes().contains(
//							type);
//			canDelete = permissionService
//					.hasPermission(BrokerPermission.MEMBER_RECORDS_DELETE)
//					&& brokerGroup.getBrokerDeleteMemberRecordTypes().contains(
//							type);
//		}
//		request.setAttribute("canCreate", canCreate);
//		request.setAttribute("canModify", canModify);
//		request.setAttribute("canDelete", canDelete);
//
//		return query;
//	}
//
//	protected boolean willExecuteQuery(final ActionContext context,
//			final QueryParameters queryParameters) throws Exception {
//		final SearchMemberRecordsForm form = context.getForm();
//		// For global searches, don't search when coming straight from the menu
//		if (form.isGlobal()) {
//			return !RequestHelper.isFromMenu(context.getRequest());
//		}
//		// For member-specific, always execute
//		return true;
//	}
}
