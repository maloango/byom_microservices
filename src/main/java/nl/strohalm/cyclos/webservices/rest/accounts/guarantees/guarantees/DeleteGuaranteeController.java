package nl.strohalm.cyclos.webservices.rest.accounts.guarantees.guarantees;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import nl.strohalm.cyclos.annotations.Inject;
import nl.strohalm.cyclos.services.accounts.guarantees.GuaranteeService;
import nl.strohalm.cyclos.utils.validation.ValidationException;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class DeleteGuaranteeController extends BaseRestController {
	protected GuaranteeService guaranteeService;

	public final GuaranteeService getGuaranteeService() {
		return guaranteeService;
	}

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
                public DeleteGuaranteeResponseDto(){
                }
	}

	@RequestMapping(value = "admin/deleteGuarantee{guaranteeId}", method = RequestMethod.GET)
	@ResponseBody
	public DeleteGuaranteeResponseDto executeAction(@PathVariable ("guaranteeId")long guaranteeId)
			throws Exception {
		DeleteGuaranteeResponseDto response = new DeleteGuaranteeResponseDto();
                try{
		if (0 <= guaranteeId) {
		} else {
                    throw new ValidationException();
                    }
		guaranteeService.remove(guaranteeId);
             response = new DeleteGuaranteeResponseDto();
		response.setMessage("guarantee.removed");}
                catch(ValidationException e){
                    e.printStackTrace();
                }
		return response;
	}

	@Inject
	public void setGuaranteeService(final GuaranteeService guaranteeService) {
		this.guaranteeService = guaranteeService;
	}
}
