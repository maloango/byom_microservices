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
import nl.strohalm.cyclos.entities.accounts.transactions.TransferType;
import nl.strohalm.cyclos.entities.customization.fields.CustomField;
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
import nl.strohalm.cyclos.services.transfertypes.TransferTypeService;
import nl.strohalm.cyclos.utils.validation.ValidationException;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;

@Controller
public class RemoveCustomFieldController extends BaseRestController {
	private TransferTypeService transferTypeService;
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

	@Inject
	public void setTransferTypeService(
			final TransferTypeService transferTypeService) {
		this.transferTypeService = transferTypeService;
	}

	public static class RemoveCustomFieldRequestDto {
		// private String nature;
		private long fieldId;
		private long memberRecordTypeId;
		private long transferTypeId;

		public long getFieldId() {
			return fieldId;
		}

		public long getMemberRecordTypeId() {
			return memberRecordTypeId;
		}

		/*
		 * public String getNature() { return nature; } public void
		 * setNature(final String nature) { this.nature = nature; }
		 */

		public long getTransferTypeId() {
			return transferTypeId;
		}

		public void setFieldId(final long fieldId) {
			this.fieldId = fieldId;
		}

		public void setMemberRecordTypeId(final long memberRecordTypeId) {
			this.memberRecordTypeId = memberRecordTypeId;
		}

		public void setTransferTypeId(final long transferTypeId) {
			this.transferTypeId = transferTypeId;
		}

		CustomField.Nature nature;

		public CustomField.Nature getNature() {
			return nature;
		}

		public void setNature(CustomField.Nature nature) {
			this.nature = nature;
		}

	}

	public static class RemoveCustomFieldResponseDto {
		public String message;

		public String getMessage() {
			return message;
		}

		public void setMessage(String message) {
			this.message = message;
		}
	}

	@RequestMapping(value = "/admin/removeCustomField", method = RequestMethod.DELETE)
	@ResponseBody
	protected RemoveCustomFieldResponseDto executeAction(
			@RequestBody RemoveCustomFieldRequestDto form) throws Exception {
		// final RemoveCustomFieldForm form = context.getForm();
		final long id = form.getFieldId();
		if (id <= 0) {
			throw new ValidationException();
		}
		final Nature nature = getNature(form);
		ActionForward forward;
		final Map<String, Object> params = new HashMap<String, Object>();
		params.put("nature", nature);
		RemoveCustomFieldResponseDto response = new RemoveCustomFieldResponseDto();
		try {
			final BaseCustomFieldService<CustomField> service = resolveService(nature);
			service.remove(id);
			switch (nature) {
			case PAYMENT:
				final TransferType transferType = transferTypeService.load(form
						.getTransferTypeId());
				// forward = context.findForward("editTransferType");
				params.put("transferTypeId", transferType.getId());
				params.put("accountTypeId", transferType.getFrom().getId());
				break;
			case MEMBER_RECORD:
				// forward = context.findForward("editMemberRecordType");
				params.put("memberRecordTypeId", form.getMemberRecordTypeId());
				break;
			default:
				// forward = context.getSuccessForward();
				break;
			}
			response.setMessage("customField.removed");
			return response;
		} catch (final DaoException e) {
			response.setMessage("customField.error.removing");
			return response;
		}
	}

	private CustomField.Nature getNature(final RemoveCustomFieldRequestDto form) {
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
