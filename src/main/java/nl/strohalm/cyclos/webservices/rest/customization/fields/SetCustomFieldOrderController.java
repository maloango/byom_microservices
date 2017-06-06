package nl.strohalm.cyclos.webservices.rest.customization.fields;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts.action.ActionForward;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import nl.strohalm.cyclos.annotations.Inject;
import nl.strohalm.cyclos.controls.ActionContext;
import nl.strohalm.cyclos.controls.customization.fields.SetCustomFieldOrderForm;
import nl.strohalm.cyclos.entities.accounts.transactions.TransferType;
import nl.strohalm.cyclos.entities.customization.fields.CustomField;
import nl.strohalm.cyclos.entities.members.Member;
import nl.strohalm.cyclos.entities.members.records.MemberRecordType;
import nl.strohalm.cyclos.services.customization.AdCustomFieldService;
import nl.strohalm.cyclos.services.customization.AdminCustomFieldService;
import nl.strohalm.cyclos.services.customization.BaseCustomFieldService;
import nl.strohalm.cyclos.services.customization.BaseGlobalCustomFieldService;
import nl.strohalm.cyclos.services.customization.LoanGroupCustomFieldService;
import nl.strohalm.cyclos.services.customization.MemberCustomFieldService;
import nl.strohalm.cyclos.services.customization.MemberRecordCustomFieldService;
import nl.strohalm.cyclos.services.customization.OperatorCustomFieldService;
import nl.strohalm.cyclos.services.customization.PaymentCustomFieldService;
import nl.strohalm.cyclos.services.elements.MemberRecordTypeService;
import nl.strohalm.cyclos.services.transfertypes.TransferTypeService;
import nl.strohalm.cyclos.utils.ActionHelper;
import nl.strohalm.cyclos.utils.validation.ValidationException;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;

@Controller
public class SetCustomFieldOrderController extends BaseRestController {
	private AdCustomFieldService adCustomFieldService;
	private AdminCustomFieldService adminCustomFieldService;
	private LoanGroupCustomFieldService loanGroupCustomFieldService;
	private MemberCustomFieldService memberCustomFieldService;
	private MemberRecordCustomFieldService memberRecordCustomFieldService;
	private OperatorCustomFieldService operatorCustomFieldService;
	private PaymentCustomFieldService paymentCustomFieldService;

	private MemberRecordTypeService memberRecordTypeService;
	private TransferTypeService transferTypeService;

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

	public static class SetCustomFieldOrderRequestDto {
		private String nature;
		private Long[] fieldIds;
		private long memberRecordTypeId;
		private long transferTypeId;

		public Long[] getFieldIds() {
			return fieldIds;
		}

		public long getMemberRecordTypeId() {
			return memberRecordTypeId;
		}

		public String getNature() {
			return nature;
		}

		public long getTransferTypeId() {
			return transferTypeId;
		}

		public void setFieldIds(final Long[] fieldIds) {
			this.fieldIds = fieldIds;
		}

		public void setMemberRecordTypeId(final long memberRecordTypeId) {
			this.memberRecordTypeId = memberRecordTypeId;
		}

		public void setNature(final String nature) {
			this.nature = nature;
		}

		public void setTransferTypeId(final long transferTypeId) {
			this.transferTypeId = transferTypeId;
		}
	}

	public static class SetCustomFieldOrderResponseDto {
		public String message;
		Map<String, Object> params;
		

		public SetCustomFieldOrderResponseDto(String message,
				Map<String, Object> params) {
			super();
			this.message = message;
			this.params = params;
		}

		public Map<String, Object> getParams() {
			return params;
		}

		public void setParams(Map<String, Object> params) {
			this.params = params;
		}

		public String getMessage() {
			return message;
		}

		public void setMessage(String message) {
			this.message = message;
		}
	}

	@RequestMapping(value = "/admin/setCustomFieldOrder", method = RequestMethod.POST)
	@ResponseBody
	protected SetCustomFieldOrderResponseDto handleSubmit(@RequestBody SetCustomFieldOrderRequestDto form)
			throws Exception {
		//final SetCustomFieldOrderForm form = context.getForm();
		CustomField.Nature nature;
		try {
			nature = CustomField.Nature.valueOf(form.getNature());
		} catch (final Exception e) {
			throw new ValidationException();
		}
		resolveService(nature).setOrder(Arrays.asList(form.getFieldIds()));
		final Map<String, Object> params = new HashMap<String, Object>();
		//ActionForward forward;
		switch (nature) {
		case MEMBER_RECORD:
			params.put("memberRecordTypeId", form.getMemberRecordTypeId());
			//forward = context.findForward("editMemberRecordType");
			break;
		case PAYMENT:
			final TransferType transferType = transferTypeService.load(form
					.getTransferTypeId());
			params.put("transferTypeId", transferType.getId());
			params.put("accountTypeId", transferType.getFrom().getId());
			//forward = context.findForward("editTransferType");
			break;
		default:
			params.put("nature", nature);
			//forward = context.getSuccessForward();
			break;
		}
		String message = "customField.orderModified";
		SetCustomFieldOrderResponseDto response = new SetCustomFieldOrderResponseDto(message, params);
		return response;
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
