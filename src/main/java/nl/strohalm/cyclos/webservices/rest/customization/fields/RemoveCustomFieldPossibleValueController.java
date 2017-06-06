package nl.strohalm.cyclos.webservices.rest.customization.fields;

import java.util.HashMap;
import java.util.Map;

import org.apache.struts.action.ActionForward;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import nl.strohalm.cyclos.annotations.Inject;
import nl.strohalm.cyclos.controls.ActionContext;
import nl.strohalm.cyclos.controls.customization.fields.RemoveCustomFieldPossibleValueForm;
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
import nl.strohalm.cyclos.utils.validation.ValidationException;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;

@Controller
public class RemoveCustomFieldPossibleValueController extends
		BaseRestController {
	private AdCustomFieldService adCustomFieldService;
	private AdminCustomFieldService adminCustomFieldService;
	private LoanGroupCustomFieldService loanGroupCustomFieldService;
	private MemberCustomFieldService memberCustomFieldService;
	private MemberRecordCustomFieldService memberRecordCustomFieldService;
	private OperatorCustomFieldService operatorCustomFieldService;
	private PaymentCustomFieldService paymentCustomFieldService;

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

	public static class RemoveCustomFieldPossibleValueRequestDto {
		private long fieldId;
		private long possibleValueId;

		// private String nature;

		public long getFieldId() {
			return fieldId;
		}

		/*
		 * public String getNature() { return nature; } public void
		 * setNature(final String nature) { this.nature = nature; }
		 */

		public long getPossibleValueId() {
			return possibleValueId;
		}

		public void setFieldId(final long fieldId) {
			this.fieldId = fieldId;
		}

		public void setPossibleValueId(final long possibleValueId) {
			this.possibleValueId = possibleValueId;
		}

		CustomField.Nature nature;

		public CustomField.Nature getNature() {
			return nature;
		}

		public void setNature(CustomField.Nature nature) {
			this.nature = nature;
		}
	}

	public static class RemoveCustomFieldPossibleValueResponseDto {
		public String key;
		public Nature nature;
		public Map<String, Object> params;

		public RemoveCustomFieldPossibleValueResponseDto(String key,
				Nature nature, Map<String, Object> params) {
			super();
			this.key = key;
			this.nature = nature;
			this.params = params;
		}

		public String getKey() {
			return key;
		}

		public void setKey(String key) {
			this.key = key;
		}

		public Nature getNature() {
			return nature;
		}

		public void setNature(Nature nature) {
			this.nature = nature;
		}

		public Map<String, Object> getParams() {
			return params;
		}

		public void setParams(Map<String, Object> params) {
			this.params = params;
		}

	}

	@RequestMapping(value = "/admin/removeCustomFieldPossibleValue", method = RequestMethod.POST)
	@ResponseBody
	protected RemoveCustomFieldPossibleValueResponseDto executeAction(
			@RequestBody RemoveCustomFieldPossibleValueRequestDto form)
			throws Exception {
		// final RemoveCustomFieldPossibleValueForm form = context.getForm();
		final long id = form.getPossibleValueId();
		if (id <= 0) {
			throw new ValidationException();
		}
		final Nature nature = getNature(form);
		final Map<String, Object> params = new HashMap<String, Object>();
		String key;
		try {
			final BaseCustomFieldService<CustomField> service = resolveService(nature);
			final CustomFieldPossibleValue possibleValue = service
					.loadPossibleValue(id);
			final CustomField customField = possibleValue.getField();
			switch (customField.getNature()) {
			case PAYMENT:
				final PaymentCustomField paymentField = (PaymentCustomField) customField;
				params.put("transferTypeId", paymentField.getTransferType()
						.getId());
				break;
			case MEMBER_RECORD:
				final MemberRecordCustomField memberRecordField = (MemberRecordCustomField) customField;
				params.put("memberRecordTypeId", memberRecordField
						.getMemberRecordType().getId());
			default:
				service.removePossibleValue(id);
				break;
			}
			key = "customField.possibleValue.removed";
		} catch (final DaoException e) {
			key = "customField.possibleValue.error.removing";
		}
		params.put("fieldId", form.getFieldId());
		params.put("nature", nature);

		RemoveCustomFieldPossibleValueResponseDto response = new RemoveCustomFieldPossibleValueResponseDto(
				key, nature, params);
		return response;

	}

	private CustomField.Nature getNature(
			final RemoveCustomFieldPossibleValueRequestDto form) {
		CustomField.Nature nature;
		try {
			nature = form.getNature();
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
