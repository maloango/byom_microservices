package nl.strohalm.cyclos.webservices.rest.members.sms;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import nl.strohalm.cyclos.annotations.Inject;
import nl.strohalm.cyclos.exceptions.PermissionDeniedException;
import nl.strohalm.cyclos.services.infotexts.InfoTextService;
import nl.strohalm.cyclos.utils.validation.ValidationException;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;

@Controller
public class RemoveInfoTextController extends BaseRestController {

	private InfoTextService infoTextService;

	@Inject
	public void setInfoTextService(final InfoTextService infoTextService) {
		this.infoTextService = infoTextService;
	}

	public static class RemoveInfoTextRequestDto {
		private long infoTextId;

		public long getInfoTextId() {
			return infoTextId;
		}

		@Inject
		public void setInfoTextId(final long infoTextId) {
			this.infoTextId = infoTextId;
		}
	}

	public static class RemoveInfoTextResponseDto {
		private String message;

		public String getMessage() {
			return message;
		}

		public void setMessage(String message) {
			this.message = message;
		}
	}

	@RequestMapping(value = "", method = RequestMethod.DELETE)
	@ResponseBody
	protected RemoveInfoTextResponseDto executeAction(
			final RemoveInfoTextRequestDto form) throws Exception {
		// final RemoveInfoTextForm form = context.getForm();
		final long id = form.getInfoTextId();
		if (id <= 0L) {
			throw new ValidationException();
		}
		String message = null;
		RemoveInfoTextResponseDto response = new RemoveInfoTextResponseDto();
		try {
			infoTextService.remove(id);
			message = "infoText.removed";
			response.setMessage(message);
			return response;

		} catch (final Exception e) {
			if (e instanceof PermissionDeniedException) {
				throw e;
			} else {
				message = "infoText.errorRemoving";
				response.setMessage(message);
				return response;
			}
		}
	}
}
