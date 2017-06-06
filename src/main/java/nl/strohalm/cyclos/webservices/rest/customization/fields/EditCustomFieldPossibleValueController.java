package nl.strohalm.cyclos.webservices.rest.customization.fields;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.struts.action.ActionForward;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import nl.strohalm.cyclos.annotations.Inject;
import nl.strohalm.cyclos.controls.ActionContext;
import nl.strohalm.cyclos.controls.customization.fields.EditCustomFieldPossibleValueForm;
import nl.strohalm.cyclos.entities.accounts.transactions.TransferType;
import nl.strohalm.cyclos.entities.customization.fields.CustomField;
import nl.strohalm.cyclos.entities.customization.fields.CustomFieldPossibleValue;
import nl.strohalm.cyclos.entities.customization.fields.MemberRecordCustomField;
import nl.strohalm.cyclos.entities.customization.fields.PaymentCustomField;
import nl.strohalm.cyclos.entities.customization.fields.CustomField.Nature;
import nl.strohalm.cyclos.entities.exceptions.DaoException;
import nl.strohalm.cyclos.services.customization.AdCustomFieldService;
import nl.strohalm.cyclos.services.customization.AdminCustomFieldService;
import nl.strohalm.cyclos.services.customization.BaseCustomFieldService;
import nl.strohalm.cyclos.services.customization.LoanGroupCustomFieldService;
import nl.strohalm.cyclos.services.customization.MemberCustomFieldService;
import nl.strohalm.cyclos.services.customization.MemberRecordCustomFieldService;
import nl.strohalm.cyclos.services.customization.OperatorCustomFieldService;
import nl.strohalm.cyclos.services.customization.PaymentCustomFieldService;
import nl.strohalm.cyclos.utils.ActionHelper;
import nl.strohalm.cyclos.utils.binding.BeanBinder;
import nl.strohalm.cyclos.utils.binding.DataBinder;
import nl.strohalm.cyclos.utils.binding.PropertyBinder;
import nl.strohalm.cyclos.utils.conversion.IdConverter;
import nl.strohalm.cyclos.utils.validation.RequiredError;
import nl.strohalm.cyclos.utils.validation.ValidationException;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;

@Controller
public class EditCustomFieldPossibleValueController extends BaseRestController {
	private AdCustomFieldService adCustomFieldService;
	private AdminCustomFieldService adminCustomFieldService;
	private LoanGroupCustomFieldService loanGroupCustomFieldService;
	private MemberCustomFieldService memberCustomFieldService;
	private MemberRecordCustomFieldService memberRecordCustomFieldService;
	private OperatorCustomFieldService operatorCustomFieldService;
	private PaymentCustomFieldService paymentCustomFieldService;

	private DataBinder<CustomFieldPossibleValue> dataBinder;

	public DataBinder<CustomFieldPossibleValue> getDataBinder() {
		if (dataBinder == null) {
			final BeanBinder<CustomFieldPossibleValue> binder = BeanBinder
					.instance(CustomFieldPossibleValue.class);
			binder.registerBinder(
					"id",
					PropertyBinder.instance(Long.class, "id",
							IdConverter.instance()));
			binder.registerBinder("field",
					PropertyBinder.instance(CustomField.class, "field"));
			binder.registerBinder("parent", PropertyBinder.instance(
					CustomFieldPossibleValue.class, "parent"));
			binder.registerBinder("value",
					PropertyBinder.instance(String.class, "value"));
			binder.registerBinder("enabled",
					PropertyBinder.instance(Boolean.TYPE, "enabled"));
			binder.registerBinder("defaultValue",
					PropertyBinder.instance(Boolean.TYPE, "defaultValue"));
			dataBinder = binder;
		}
		return dataBinder;
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
	public void setOperatorCustomFieldService(
			final OperatorCustomFieldService operatorCustomFieldService) {
		this.operatorCustomFieldService = operatorCustomFieldService;
	}

	@Inject
	public void setPaymentCustomFieldService(
			final PaymentCustomFieldService paymentCustomFieldService) {
		this.paymentCustomFieldService = paymentCustomFieldService;
	}

	public static class EditCustomFieldPossibleValueRequestDto {
		private String multipleValues;
		private String nature;
		protected Map<String, Object> values;

		public Map<String, Object> getValues() {
			return values;
		}

		public void setValues(final Map<String, Object> values) {
			this.values = values;
		}

		public String getMultipleValues() {
			return multipleValues;
		}

		public String getNature() {
			return nature;
		}

		public Map<String, Object> getPossibleValue() {
			return values;
		}

		public Object getPossibleValue(final String key) {
			return values.get(key);
		}

		public void setMultipleValues(final String multipleValues) {
			this.multipleValues = multipleValues;
		}

		public void setNature(final String nature) {
			this.nature = nature;
		}

		public void setPossibleValue(final Map<String, Object> value) {
			values = value;
		}

		public void setPossibleValue(final String key, final Object value) {
			values.put(key, value);
		}
	}

	public static class EditCustomFieldPossibleValueResponseDto {
		private String message;
		private Map<String, Object> map;

		public EditCustomFieldPossibleValueResponseDto(String message,
				Map<String, Object> map) {
			super();
			this.message = message;
			this.map = map;
		}

		public Map<String, Object> getMap() {
			return map;
		}

		public void setMap(Map<String, Object> map) {
			this.map = map;
		}

		public String getMessage() {
			return message;
		}

		public void setMessage(String message) {
			this.message = message;
		}
	}

	@RequestMapping(value = "/admin/editCustomFieldPossibleValue", method = RequestMethod.PUT)
	@ResponseBody
	protected EditCustomFieldPossibleValueResponseDto handleSubmit(
			@RequestBody EditCustomFieldPossibleValueRequestDto form)
			throws Exception {
		// final EditCustomFieldPossibleValueForm form = context.getForm();
		final Collection<CustomFieldPossibleValue> resolveAllValues = resolveAllValues(form);
		Boolean isInsert = null;
		CustomField field = null;
		CustomFieldPossibleValue parentValue = null;
		Nature nature = getNature(form);
		final BaseCustomFieldService<CustomField> service = resolveService(nature);
		final Map<String, Object> params = new HashMap<String, Object>();
		String message=null;
		EditCustomFieldPossibleValueResponseDto response = new EditCustomFieldPossibleValueResponseDto(
				message, params);
		try {
			for (final CustomFieldPossibleValue possibleValue : resolveAllValues) {
				if (isInsert == null) {
					isInsert = possibleValue.getId() == null;
					field = service.load(possibleValue.getField().getId());
					parentValue = possibleValue.getParent();
				}
				service.save(possibleValue);
			}
			if (isInsert) {
				response.setMessage("customField.possibleValue.inserted");
			} else {
				response.setMessage("customField.possibleValue.modified");
			}

			params.put("nature", nature);
			params.put("fieldId", field.getId());

			switch (field.getNature()) {
			case MEMBER_RECORD:
				final MemberRecordCustomField memberRecordField = (MemberRecordCustomField) field;
				final Long memberRecordTypeId = memberRecordField
						.getMemberRecordType().getId();
				params.put("memberRecordTypeId", memberRecordTypeId);
				break;
			case PAYMENT:
				final PaymentCustomField paymentField = (PaymentCustomField) field;
				final TransferType transferType = paymentField
						.getTransferType();
				params.put("transferTypeId", transferType.getId());
				params.put("accountTypeId", transferType.getFrom().getId());
				break;
			}

			if (parentValue != null) {
				params.put("parentValueId", parentValue.getId());
			}
			return response;
		} catch (final DaoException e) {
			response.setMessage("customField.possibleValue.error.saving");
			return response;
		}
	}

	private CustomField.Nature getNature(
			final EditCustomFieldPossibleValueRequestDto form) {
		CustomField.Nature nature;
		try {
			nature = CustomField.Nature.valueOf(form.getNature());
		} catch (final Exception e) {
			throw new ValidationException();
		}
		return nature;
	}

	private Collection<CustomFieldPossibleValue> resolveAllValues(
			EditCustomFieldPossibleValueRequestDto form) {
		// final EditCustomFieldPossibleValueForm form = context.getForm();
		final CustomFieldPossibleValue possibleValue = getDataBinder()
				.readFromString(form.getPossibleValue());
		if (possibleValue.isTransient()) {
			// When inserting, multiple values may be created, one per line
			final String[] lines = StringUtils.split(form.getMultipleValues(),
					'\n');
			final Collection<CustomFieldPossibleValue> possibleValues = new ArrayList<CustomFieldPossibleValue>();
			for (String value : lines) {
				value = StringUtils.trimToNull(value);
				if (value == null) {
					continue;
				}
				// Get each possible value
				final CustomFieldPossibleValue current = (CustomFieldPossibleValue) possibleValue
						.clone();
				current.setValue(value);
				possibleValues.add(current);
			}
			return possibleValues;
		} else {
			return Collections.singleton(possibleValue);
		}
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
