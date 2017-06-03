package nl.strohalm.cyclos.webservices.rest.external;

import org.apache.struts.action.ActionForward;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import nl.strohalm.cyclos.annotations.Inject;
import nl.strohalm.cyclos.controls.ActionContext;
import nl.strohalm.cyclos.controls.accounts.external.RemoveExternalTransferTypeForm;
import nl.strohalm.cyclos.entities.accounts.external.ExternalTransferType;
import nl.strohalm.cyclos.exceptions.PermissionDeniedException;
import nl.strohalm.cyclos.services.accounts.external.ExternalTransferTypeService;
import nl.strohalm.cyclos.utils.ActionHelper;
import nl.strohalm.cyclos.utils.validation.ValidationException;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;

@Controller
public class RemoveExternalTransferTypeController extends BaseRestController {

	private ExternalTransferTypeService externalTransferTypeService;

	@Inject
	public void setExternalTransferTypeService(
			final ExternalTransferTypeService externalTransferTypeService) {
		this.externalTransferTypeService = externalTransferTypeService;
	}

	public static class RemoveExternalTransferTypeRequestDto {
		private long externalTransferTypeId;

		public long getExternalTransferTypeId() {
			return externalTransferTypeId;
		}

		public void setExternalTransferTypeId(final long externalTransferType) {
			externalTransferTypeId = externalTransferType;
		}
	}

	public static class RemoveExternalTransferTypeResponseDto {
		public String message;

		public String getMessage() {
			return message;
		}

		public void setMessage(String message) {
			this.message = message;
		}
	}

	@RequestMapping(value = "/admin/removeAccountType", method = RequestMethod.DELETE)
	@ResponseBody
	protected RemoveExternalTransferTypeResponseDto executeAction(
			@RequestBody RemoveExternalTransferTypeRequestDto form)
			throws Exception {
		// final RemoveExternalTransferTypeForm form = context.getForm();
		final long id = form.getExternalTransferTypeId();
		if (id <= 0L) {
			throw new ValidationException();
		}
		final ExternalTransferType transferType = externalTransferTypeService
				.load(id);
		RemoveExternalTransferTypeResponseDto response = new RemoveExternalTransferTypeResponseDto();
		try {
			externalTransferTypeService.remove(id);
			response.setMessage("externalTransferType.removed");
		} catch (final PermissionDeniedException e) {
			throw e;
		} catch (final Exception e) {
			response.setMessage("externalTransferType.error.removing");
		}
		return response;

	}
}
