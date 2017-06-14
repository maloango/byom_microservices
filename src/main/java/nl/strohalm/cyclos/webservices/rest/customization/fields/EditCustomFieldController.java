package nl.strohalm.cyclos.webservices.rest.customization.fields;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import nl.strohalm.cyclos.annotations.Inject;
import nl.strohalm.cyclos.entities.accounts.transactions.TransferType;
import nl.strohalm.cyclos.entities.customization.fields.AdCustomField;
import nl.strohalm.cyclos.entities.customization.fields.AdminCustomField;
import nl.strohalm.cyclos.entities.customization.fields.CustomField;
import nl.strohalm.cyclos.entities.customization.fields.LoanGroupCustomField;
import nl.strohalm.cyclos.entities.customization.fields.MemberCustomField;
import nl.strohalm.cyclos.entities.customization.fields.MemberRecordCustomField;
import nl.strohalm.cyclos.entities.customization.fields.OperatorCustomField;
import nl.strohalm.cyclos.entities.customization.fields.PaymentCustomField;
import nl.strohalm.cyclos.entities.customization.fields.Validation;
import nl.strohalm.cyclos.entities.groups.AdminGroup;
import nl.strohalm.cyclos.entities.groups.MemberGroup;
import nl.strohalm.cyclos.entities.members.Element;
import nl.strohalm.cyclos.entities.members.Member;
import nl.strohalm.cyclos.entities.members.records.MemberRecordType;
import nl.strohalm.cyclos.services.customization.AdCustomFieldService;
import nl.strohalm.cyclos.services.customization.AdminCustomFieldService;
import nl.strohalm.cyclos.services.customization.BaseCustomFieldService;
import nl.strohalm.cyclos.services.customization.LoanGroupCustomFieldService;
import nl.strohalm.cyclos.services.customization.MemberCustomFieldService;
import nl.strohalm.cyclos.services.customization.MemberRecordCustomFieldService;
import nl.strohalm.cyclos.services.customization.OperatorCustomFieldService;
import nl.strohalm.cyclos.services.customization.PaymentCustomFieldService;
import nl.strohalm.cyclos.services.elements.MemberRecordTypeService;
import nl.strohalm.cyclos.services.transfertypes.TransferTypeService;
import nl.strohalm.cyclos.utils.binding.BeanBinder;
import nl.strohalm.cyclos.utils.binding.DataBinder;
import nl.strohalm.cyclos.utils.binding.DataBinderHelper;
import nl.strohalm.cyclos.utils.binding.PropertyBinder;
import nl.strohalm.cyclos.utils.binding.SimpleCollectionBinder;
import nl.strohalm.cyclos.utils.conversion.IdConverter;
import nl.strohalm.cyclos.utils.validation.ValidationException;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;

@Controller
public class EditCustomFieldController extends BaseRestController {

	private static DataBinder<? extends CustomField> getBasicDataBinder(
			final CustomField.Nature nature) {

		final BeanBinder<Validation> validationBinder = BeanBinder.instance(
				Validation.class, "validation");
		validationBinder.registerBinder("required",
				PropertyBinder.instance(Boolean.TYPE, "required"));
		validationBinder.registerBinder("unique",
				PropertyBinder.instance(Boolean.TYPE, "unique"));
		validationBinder.registerBinder("lengthConstraint",
				DataBinderHelper.rangeConstraintBinder("lengthConstraint"));
		validationBinder.registerBinder("validatorClass",
				PropertyBinder.instance(String.class, "validatorClass"));

		final BeanBinder<? extends CustomField> binder = BeanBinder
				.instance(nature.getEntityType());
		binder.registerBinder(
				"id",
				PropertyBinder.instance(Long.class, "id",
						IdConverter.instance()));
		binder.registerBinder("internalName",
				PropertyBinder.instance(String.class, "internalName"));
		binder.registerBinder("name",
				PropertyBinder.instance(String.class, "name"));
		binder.registerBinder("pattern",
				PropertyBinder.instance(String.class, "pattern"));
		binder.registerBinder("parent",
				PropertyBinder.instance(CustomField.class, "parent"));
		binder.registerBinder("description",
				PropertyBinder.instance(String.class, "description"));
		binder.registerBinder("type",
				PropertyBinder.instance(CustomField.Type.class, "type"));
		binder.registerBinder("control",
				PropertyBinder.instance(CustomField.Control.class, "control"));
		binder.registerBinder("size",
				PropertyBinder.instance(CustomField.Size.class, "size"));
		binder.registerBinder("allSelectedLabel",
				PropertyBinder.instance(String.class, "allSelectedLabel"));
		binder.registerBinder("validation", validationBinder);
		return binder;
	}

	private AdCustomFieldService adCustomFieldService;
	private AdminCustomFieldService adminCustomFieldService;
	private LoanGroupCustomFieldService loanGroupCustomFieldService;
	private MemberCustomFieldService memberCustomFieldService;
	private MemberRecordCustomFieldService memberRecordCustomFieldService;
	private OperatorCustomFieldService operatorCustomFieldService;
	private PaymentCustomFieldService paymentCustomFieldService;
	private MemberRecordTypeService memberRecordTypeService;
	private TransferTypeService transferTypeService;
	private Map<CustomField.Nature, DataBinder<? extends CustomField>> dataBinders;

	@SuppressWarnings("unchecked")
	public DataBinder<AdCustomField> getAdCustomFieldBinder() {
		final BeanBinder<AdCustomField> adFieldBinder = (BeanBinder<AdCustomField>) getBasicDataBinder(CustomField.Nature.AD);
		adFieldBinder.registerBinder("showInSearch",
				PropertyBinder.instance(Boolean.TYPE, "showInSearch"));
		adFieldBinder.registerBinder("indexed",
				PropertyBinder.instance(Boolean.TYPE, "indexed"));
		adFieldBinder.registerBinder("visibility", PropertyBinder.instance(
				AdCustomField.Visibility.class, "visibility"));
		return adFieldBinder;
	}

	@SuppressWarnings("unchecked")
	public DataBinder<AdminCustomField> getAdminCustomFieldBinder() {
		final BeanBinder<AdminCustomField> adminFieldBinder = (BeanBinder<AdminCustomField>) getBasicDataBinder(CustomField.Nature.ADMIN);
		adminFieldBinder.registerBinder("groups",
				SimpleCollectionBinder.instance(AdminGroup.class, "groups"));
		return adminFieldBinder;
	}

	public DataBinder<? extends CustomField> getDataBinder(
			final CustomField.Nature nature) {
		if (dataBinders == null) {
			dataBinders = new EnumMap<CustomField.Nature, DataBinder<? extends CustomField>>(
					CustomField.Nature.class);
			dataBinders.put(CustomField.Nature.MEMBER,
					getMemberCustomFieldBinder());
			dataBinders.put(CustomField.Nature.ADMIN,
					getAdminCustomFieldBinder());
			dataBinders.put(CustomField.Nature.OPERATOR,
					getOperatorCustomFieldBinder());
			dataBinders.put(CustomField.Nature.AD, getAdCustomFieldBinder());
			dataBinders.put(CustomField.Nature.PAYMENT,
					getPaymentCustomFieldBinder());
			dataBinders.put(CustomField.Nature.LOAN_GROUP,
					getLoanGroupCustomFieldBinder());
			dataBinders.put(CustomField.Nature.MEMBER_RECORD,
					getMemberRecordCustomFieldBinder());
		}
		return dataBinders.get(nature);
	}

	@SuppressWarnings("unchecked")
	public DataBinder<LoanGroupCustomField> getLoanGroupCustomFieldBinder() {
		final BeanBinder<LoanGroupCustomField> loanGroupFieldBinder = (BeanBinder<LoanGroupCustomField>) getBasicDataBinder(CustomField.Nature.LOAN_GROUP);
		loanGroupFieldBinder.registerBinder("showInSearch",
				PropertyBinder.instance(Boolean.TYPE, "showInSearch"));
		return loanGroupFieldBinder;
	}

	@SuppressWarnings("unchecked")
	public DataBinder<MemberCustomField> getMemberCustomFieldBinder() {
		final BeanBinder<MemberCustomField> memberFieldBinder = (BeanBinder<MemberCustomField>) getBasicDataBinder(CustomField.Nature.MEMBER);
		memberFieldBinder.registerBinder("visibilityAccess", PropertyBinder
				.instance(MemberCustomField.Access.class, "visibilityAccess"));
		memberFieldBinder.registerBinder("updateAccess", PropertyBinder
				.instance(MemberCustomField.Access.class, "updateAccess"));
		memberFieldBinder
				.registerBinder("memberSearchAccess", PropertyBinder.instance(
						MemberCustomField.Access.class, "memberSearchAccess"));
		memberFieldBinder.registerBinder("adSearchAccess", PropertyBinder
				.instance(MemberCustomField.Access.class, "adSearchAccess"));
		memberFieldBinder.registerBinder("indexing", PropertyBinder.instance(
				MemberCustomField.Indexing.class, "indexing"));
		memberFieldBinder.registerBinder("loanSearchAccess", PropertyBinder
				.instance(MemberCustomField.Access.class, "loanSearchAccess"));
		memberFieldBinder.registerBinder("memberCanHide",
				PropertyBinder.instance(Boolean.TYPE, "memberCanHide"));
		memberFieldBinder.registerBinder("showInPrint",
				PropertyBinder.instance(Boolean.TYPE, "showInPrint"));
		memberFieldBinder.registerBinder("groups",
				SimpleCollectionBinder.instance(MemberGroup.class, "groups"));
		return memberFieldBinder;
	}

	@SuppressWarnings("unchecked")
	public DataBinder<MemberRecordCustomField> getMemberRecordCustomFieldBinder() {
		final BeanBinder<MemberRecordCustomField> memberRecordFieldBinder = (BeanBinder<MemberRecordCustomField>) getBasicDataBinder(CustomField.Nature.MEMBER_RECORD);
		memberRecordFieldBinder.registerBinder("memberRecordType",
				PropertyBinder.instance(MemberRecordType.class,
						"memberRecordType"));
		memberRecordFieldBinder.registerBinder("showInSearch",
				PropertyBinder.instance(Boolean.TYPE, "showInSearch"));
		memberRecordFieldBinder.registerBinder("showInList",
				PropertyBinder.instance(Boolean.TYPE, "showInList"));
		memberRecordFieldBinder
				.registerBinder("brokerAccess", PropertyBinder.instance(
						MemberRecordCustomField.Access.class, "brokerAccess"));
		return memberRecordFieldBinder;
	}

	@SuppressWarnings("unchecked")
	public DataBinder<OperatorCustomField> getOperatorCustomFieldBinder() {
		final BeanBinder<OperatorCustomField> operatorFieldBinder = (BeanBinder<OperatorCustomField>) getBasicDataBinder(CustomField.Nature.OPERATOR);
		operatorFieldBinder.registerBinder("member",
				PropertyBinder.instance(Member.class, "member"));
		operatorFieldBinder.registerBinder("visibility", PropertyBinder
				.instance(OperatorCustomField.Visibility.class, "visibility"));
		return operatorFieldBinder;
	}

	@SuppressWarnings("unchecked")
	public DataBinder<PaymentCustomField> getPaymentCustomFieldBinder() {
		final BeanBinder<PaymentCustomField> paymentFieldBinder = (BeanBinder<PaymentCustomField>) getBasicDataBinder(CustomField.Nature.PAYMENT);
		paymentFieldBinder.registerBinder("enabled",
				PropertyBinder.instance(Boolean.TYPE, "enabled"));
		paymentFieldBinder.registerBinder("transferType",
				PropertyBinder.instance(TransferType.class, "transferType"));
		paymentFieldBinder.registerBinder("searchAccess", PropertyBinder
				.instance(PaymentCustomField.Access.class, "searchAccess"));
		paymentFieldBinder.registerBinder("listAccess", PropertyBinder
				.instance(PaymentCustomField.Access.class, "listAccess"));
		return paymentFieldBinder;
	}

	@Inject
	public void setAdCustomFieldService(
			final AdCustomFieldService adCustomFieldService) {
		this.adCustomFieldService = adCustomFieldService;
	}

	@Inject
	public void setAdminCustomFieldService(
			final AdminCustomFieldService adminCustomFieldService) {
		this.adminCustomFieldService = adminCustomFieldService;
	}

	@Inject
	public void setLoanGroupCustomFieldService(
			final LoanGroupCustomFieldService loanGroupCustomFieldService) {
		this.loanGroupCustomFieldService = loanGroupCustomFieldService;
	}

	@Inject
	public void setMemberCustomFieldService(
			final MemberCustomFieldService memberCustomFieldService) {
		this.memberCustomFieldService = memberCustomFieldService;
	}

	@Inject
	public void setMemberRecordCustomFieldService(
			final MemberRecordCustomFieldService memberRecordCustomFieldService) {
		this.memberRecordCustomFieldService = memberRecordCustomFieldService;
	}

	@Inject
	public void setMemberRecordTypeService(
			final MemberRecordTypeService memberRecordTypeService) {
		this.memberRecordTypeService = memberRecordTypeService;
	}

	@Inject
	public void setOperatorCustomFieldService(
			final OperatorCustomFieldService operatorCustomFieldService) {
		this.operatorCustomFieldService = operatorCustomFieldService;
	}

	@Inject
	public void setPaymentCustomFieldService(
			final PaymentCustomFieldService paymentCustomFieldService) {
		this.paymentCustomFieldService = paymentCustomFieldService;
	}

	@Inject
	public void setTransferTypeService(
			final TransferTypeService transferTypeService) {
		this.transferTypeService = transferTypeService;
	}

	public static class EditCustomFieldRequestDto {
		private Element element;

		public Element getElement() {
			return element;
		}

		public void setElement(Element element) {
			this.element = element;
		}

		private long fieldId;
		private long parentValueId;
		private String nature;
		protected Map<String, Object> values;

		public Map<String, Object> getValues() {
			return values;
		}

		public void setValues(final Map<String, Object> values) {
			this.values = values;
		}

		public Map<String, Object> getField() {
			return values;
		}

		public Object getField(final String name) {
			return values.get(name);
		}

		public long getFieldId() {
			return fieldId;
		}

		public long getMemberRecordTypeId() {
			try {
				return (Long) getField("memberRecordType");
			} catch (final Exception e) {
				return 0;
			}
		}

		public String getNature() {
			return nature;
		}

		public long getParentValueId() {
			return parentValueId;
		}

		public long getTransferTypeId() {
			try {
				return (Long) getField("transferType");
			} catch (final Exception e) {
				return 0;
			}
		}

		public void setField(final Map<String, Object> field) {
			values = field;
		}

		public void setField(final String name, final Object value) {
			values.put(name, value);
		}

		public void setFieldId(final long fieldId) {
			this.fieldId = fieldId;
		}

		public void setMemberRecordTypeId(final long memberRecordTypeId) {
			setField("memberRecordType", memberRecordTypeId);
		}

		public void setNature(final String nature) {
			this.nature = nature;
		}

		public void setParentValueId(final long parentValueId) {
			this.parentValueId = parentValueId;
		}

		public void setTransferTypeId(final long transferTypeId) {
			setField("transferType", transferTypeId);
		}
	}

	public static class EditCustomFieldResponseDto {
		public String message;

		public String getMessage() {
			return message;
		}

		public void setMessage(String message) {
			this.message = message;
		}
	}

	@RequestMapping(value = "/admin/editCustomField", method = RequestMethod.POST)
	@ResponseBody
	protected EditCustomFieldResponseDto handleSubmit(
			@RequestBody EditCustomFieldRequestDto form) throws Exception {
		// final EditCustomFieldForm form = context.getForm();
		final CustomField.Nature nature = getNature(form);
		CustomField field = getDataBinder(nature).readFromString(
				form.getField());
		final boolean isInsert = field.getId() == null;
		if (isInsert && field instanceof OperatorCustomField) {
			((OperatorCustomField) field).setMember((Member) form.getElement());
		}
		field = resolveService(nature).save(field);

		// Forward with correct parameters
		final Map<String, Object> params = new HashMap<String, Object>();
		params.put("fieldId", field.getId());
		params.put("nature", field.getNature().name());
		switch (nature) {
		case MEMBER_RECORD:
			params.put("memberRecordTypeId", form.getField("memberRecordType"));
			break;
		case PAYMENT:
			final PaymentCustomField paymentField = (PaymentCustomField) field;
			params.put("accountTypeId", paymentField.getTransferType()
					.getFrom().getId());
			params.put("transferTypeId", paymentField.getTransferType().getId());
			break;
		}
		EditCustomFieldResponseDto response = new EditCustomFieldResponseDto();
		if (isInsert) {
			response.setMessage("customField.inserted");
		} else {
			response.setMessage("customField.modified");
		}
		return response;
	}

	private CustomField.Nature getNature(final EditCustomFieldRequestDto form) {
		CustomField.Nature nature;
		try {
			nature = CustomField.Nature.valueOf(form.getNature());
		} catch (final Exception e) {
			throw new ValidationException();
		}
		return nature;
	}

	@SuppressWarnings("unchecked")
	private <CF extends CustomField> BaseCustomFieldService<CF> resolveService(
			final CustomField.Nature nature) {
		switch (nature) {
		case AD:
			return (BaseCustomFieldService<CF>) adCustomFieldService;
		case ADMIN:
			return (BaseCustomFieldService<CF>) adminCustomFieldService;
		case LOAN_GROUP:
			return (BaseCustomFieldService<CF>) loanGroupCustomFieldService;
		case MEMBER:
			return (BaseCustomFieldService<CF>) memberCustomFieldService;
		case MEMBER_RECORD:
			return (BaseCustomFieldService<CF>) memberRecordCustomFieldService;
		case OPERATOR:
			return (BaseCustomFieldService<CF>) operatorCustomFieldService;
		case PAYMENT:
			return (BaseCustomFieldService<CF>) paymentCustomFieldService;
		}
		return null;
	}

}
