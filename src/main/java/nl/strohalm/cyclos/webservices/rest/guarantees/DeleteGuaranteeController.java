package nl.strohalm.cyclos.webservices.rest.guarantees;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import nl.strohalm.cyclos.annotations.Inject;
import nl.strohalm.cyclos.services.accounts.guarantees.GuaranteeService;
import nl.strohalm.cyclos.utils.validation.ValidationException;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;

@Controller
public class DeleteGuaranteeController extends BaseRestController {
	protected GuaranteeService guaranteeService;

	public static class DeleteGuaranteeRequestDto {
		private Long guaranteeId;

		public Long getGuaranteeId() {
			return guaranteeId;
		}

		public void setGuaranteeId(final Long paymentObligationId) {
			guaranteeId = paymentObligationId;
		}
	}

	public static class DeleteGuaranteeResponseDto {
		public String message;

		public String getMessage() {
			return message;
		}

		public void setMessage(String message) {
			this.message = message;
		}
	}

	@RequestMapping(value = "", method = RequestMethod.DELETE)
	@ResponseBody
	public DeleteGuaranteeResponseDto executeAction(@RequestBody DeleteGuaranteeRequestDto form)
			throws Exception {
		//final DeleteGuaranteeForm form = context.getForm();
		if (form.getGuaranteeId() <= 0) {
			throw new ValidationException();
		}
		guaranteeService.remove(form.getGuaranteeId());
		DeleteGuaranteeResponseDto response = new DeleteGuaranteeResponseDto();
		response.setMessage("guarantee.removed");
		return response;
	}

	@Inject
	public void setGuaranteeService(final GuaranteeService guaranteeService) {
		this.guaranteeService = guaranteeService;
	}
}
