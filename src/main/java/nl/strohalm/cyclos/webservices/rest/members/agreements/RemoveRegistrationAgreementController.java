package nl.strohalm.cyclos.webservices.rest.members.agreements;

import org.apache.struts.action.ActionForward;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import nl.strohalm.cyclos.annotations.Inject;
import nl.strohalm.cyclos.controls.ActionContext;
import nl.strohalm.cyclos.controls.members.agreements.RemoveRegistrationAgreementForm;
import nl.strohalm.cyclos.services.elements.RegistrationAgreementService;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;

@Controller
public class RemoveRegistrationAgreementController extends BaseRestController {
	private RegistrationAgreementService registrationAgreementService;

	@Inject
	public void setRegistrationAgreementService(
			final RegistrationAgreementService registrationAgreementService) {
		this.registrationAgreementService = registrationAgreementService;
	}

	public static class RemoveRegistrationAgreementRequestDto {

		private long registrationAgreementId;

		public long getRegistrationAgreementId() {
			return registrationAgreementId;
		}

	}

	public static class RemoveRegistrationAgreementResponseDto {
		private String message;

		public String getMessage() {
			return message;
		}

		public void setMessage(String message) {
			this.message = message;
		}
	}

	@RequestMapping(value = "/admin/managePasswords", method = RequestMethod.POST)
	@ResponseBody
	protected RemoveRegistrationAgreementResponseDto executeAction(
			@RequestBody RemoveRegistrationAgreementRequestDto form)
			throws Exception {
		// final RemoveRegistrationAgreementForm form = context.getForm();
		String message = null;
		RemoveRegistrationAgreementResponseDto response = new RemoveRegistrationAgreementResponseDto();
		try {
			registrationAgreementService.remove(form
					.getRegistrationAgreementId());
			message = "registrationAgreement.removed";
		} catch (final Exception e) {
			message = "registrationAgreement.error.removing";
		}
		return response;
	}
}
