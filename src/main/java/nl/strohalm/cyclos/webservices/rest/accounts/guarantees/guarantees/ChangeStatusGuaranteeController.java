package nl.strohalm.cyclos.webservices.rest.accounts.guarantees.guarantees;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import nl.strohalm.cyclos.annotations.Inject;
import nl.strohalm.cyclos.entities.accounts.guarantees.Guarantee;
import nl.strohalm.cyclos.services.accounts.guarantees.GuaranteeService;
import nl.strohalm.cyclos.services.accounts.guarantees.exceptions.GuaranteeStatusChangeException;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;

@Controller
public class ChangeStatusGuaranteeController extends BaseRestController {
	
	protected GuaranteeService guaranteeService;

	public final GuaranteeService getGuaranteeService() {
		return guaranteeService;
	}

	public static class ChangeStatusGuaranteeRequestDto {
		private Long guaranteeId;

		public Long getGuaranteeId() {
			return guaranteeId;
		}

		public void setGuaranteeId(final Long guaranteeId) {
			this.guaranteeId = guaranteeId;
		}
	}

	public static class ChangeStatusGuaranteeResponseDto {
		private String message;
		Long guaranteeId;

		public ChangeStatusGuaranteeResponseDto(String message, Long guaranteeId) {
			super();
			this.message = message;
			this.guaranteeId = guaranteeId;
		}

		public String getMessage() {
			return message;
		}

		public void setMessage(String message) {
			this.message = message;
		}
                public ChangeStatusGuaranteeResponseDto(){
                }
	}

	@RequestMapping(value = "", method = RequestMethod.POST)
	@ResponseBody
	public ChangeStatusGuaranteeResponseDto executeAction(
			@RequestBody ChangeStatusGuaranteeRequestDto form) throws Exception {
		ChangeStatusGuaranteeResponseDto response = null;
                try{
		String message = null;
		Long guaranteeId = null;
		try {
			changeStatus(form);
		} catch (final GuaranteeStatusChangeException e) {
			message = "guarantee.error.changeStatus " + " guarantee.status."
					+ e.getNewstatus();
		}
		guaranteeId = form.getGuaranteeId();
                 response = new ChangeStatusGuaranteeResponseDto(
				message, guaranteeId);}
                catch(Exception e){
                    e.printStackTrace();
                }
                 
		return response;

	}

	@Inject
	public void setGuaranteeService(final GuaranteeService guaranteeService) {
		this.guaranteeService = guaranteeService;
	}

	protected void changeStatus(final ChangeStatusGuaranteeRequestDto form) {
		// final ChangeStatusGuaranteeForm form = context.getForm();
		guaranteeService.changeStatus(form.getGuaranteeId(), getNewStatus());
	}

	protected Guarantee.Status getNewStatus() {
		return null;
	}
}
