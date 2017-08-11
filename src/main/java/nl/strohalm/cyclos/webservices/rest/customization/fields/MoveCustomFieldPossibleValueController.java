package nl.strohalm.cyclos.webservices.rest.customization.fields;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import nl.strohalm.cyclos.annotations.Inject;
import nl.strohalm.cyclos.entities.accounts.transactions.TransferType;
import nl.strohalm.cyclos.entities.customization.fields.CustomField;
import nl.strohalm.cyclos.entities.customization.fields.CustomFieldPossibleValue;
import nl.strohalm.cyclos.entities.customization.fields.MemberRecordCustomField;
import nl.strohalm.cyclos.entities.customization.fields.PaymentCustomField;
import nl.strohalm.cyclos.entities.members.records.MemberRecordType;
import nl.strohalm.cyclos.services.customization.AdCustomFieldService;
import nl.strohalm.cyclos.services.customization.AdminCustomFieldService;
import nl.strohalm.cyclos.services.customization.BaseCustomFieldService;
import nl.strohalm.cyclos.services.customization.LoanGroupCustomFieldService;
import nl.strohalm.cyclos.services.customization.MemberCustomFieldService;
import nl.strohalm.cyclos.services.customization.MemberRecordCustomFieldService;
import nl.strohalm.cyclos.services.customization.OperatorCustomFieldService;
import nl.strohalm.cyclos.services.customization.PaymentCustomFieldService;
import nl.strohalm.cyclos.utils.validation.ValidationException;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;

@Controller
public class MoveCustomFieldPossibleValueController extends BaseRestController {
	private AdCustomFieldService adCustomFieldService;
	private AdminCustomFieldService adminCustomFieldService;
	private LoanGroupCustomFieldService loanGroupCustomFieldService;
	private MemberCustomFieldService memberCustomFieldService;
	public final AdCustomFieldService getAdCustomFieldService() {
		return adCustomFieldService;
	}

	public final AdminCustomFieldService getAdminCustomFieldService() {
		return adminCustomFieldService;
	}

	public final LoanGroupCustomFieldService getLoanGroupCustomFieldService() {
		return loanGroupCustomFieldService;
	}

	public final MemberCustomFieldService getMemberCustomFieldService() {
		return memberCustomFieldService;
	}

	public final MemberRecordCustomFieldService getMemberRecordCustomFieldService() {
		return memberRecordCustomFieldService;
	}

	public final OperatorCustomFieldService getOperatorCustomFieldService() {
		return operatorCustomFieldService;
	}

	public final PaymentCustomFieldService getPaymentCustomFieldService() {
		return paymentCustomFieldService;
	}

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

	public static class MoveCustomFieldPossibleValueRequestDto {

		private long oldValueId;
		private long newValueId;
		private String nature;

		public String getNature() {
			return nature;
		}

		public long getNewValueId() {
			return newValueId;
		}

		public long getOldValueId() {
			return oldValueId;
		}

		public void setNature(final String nature) {
			this.nature = nature;
		}

		public void setNewValueId(final long newValueId) {
			this.newValueId = newValueId;
		}

		public void setOldValueId(final long oldValueId) {
			this.oldValueId = oldValueId;
		}

	}

	public static class MoveCustomFieldPossibleValueResponseDto {
		private String message;
		private int affected;
		private String oldValue;
		private String newValue;

		public String getMessage() {
			return message;
		}

		public void setMessage(String message) {
			this.message = message;
		}

		public int getAffected() {
			return affected;
		}

		public void setAffected(int affected) {
			this.affected = affected;
		}

		public String getValue() {
			return oldValue;
		}

		public void setValue(String value) {
			this.oldValue = value;
		}

		public String getNewValue() {
			return newValue;
		}

		public void setNewValue(String newValue) {
			this.newValue = newValue;
		}

		public MoveCustomFieldPossibleValueResponseDto(String message,
				int affected, String value, String newValue) {
			super();
			this.message = message;
			this.affected = affected;
			this.oldValue = value;
			this.newValue = newValue;
		}

	}

	@RequestMapping(value = "admin/moveCustomFieldPossibleValue", method = RequestMethod.POST)
	@ResponseBody
	protected MoveCustomFieldPossibleValueResponseDto executeAction(
			@RequestBody final MoveCustomFieldPossibleValueRequestDto form)
			throws Exception {
		MoveCustomFieldPossibleValueResponseDto response = null;
                try{
		final BaseCustomFieldService<CustomField> service = resolveService(getNature(form));
		final CustomFieldPossibleValue oldValue = service
				.loadPossibleValue(form.getOldValueId());
		final CustomFieldPossibleValue newValue = service
				.loadPossibleValue(form.getNewValueId());

		final CustomField field = oldValue.getField();
		final int affected = service.replacePossibleValues(oldValue, newValue);
		final Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("fieldId", field.getId());
		parameters.put("nature", field.getNature());
		switch (field.getNature()) {
		case PAYMENT:
			final TransferType transferType = ((PaymentCustomField) field)
					.getTransferType();
			parameters.put("transferTypeId", transferType.getId());
			parameters.put("accountTypeId", transferType.getFrom().getId());
			break;
		case MEMBER_RECORD:
			final MemberRecordType memberRecordType = ((MemberRecordCustomField) field)
					.getMemberRecordType();
			parameters.put("memberRecordTypeId", memberRecordType.getId());
			break;
		}
		response = new MoveCustomFieldPossibleValueResponseDto(
				"customField.valuesMoved", affected, oldValue.getValue(),
				newValue.getValue());}
                catch(Exception e){
                    e.printStackTrace();
                }
		return response;
	}

	private CustomField.Nature getNature(
			final MoveCustomFieldPossibleValueRequestDto form) {
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
