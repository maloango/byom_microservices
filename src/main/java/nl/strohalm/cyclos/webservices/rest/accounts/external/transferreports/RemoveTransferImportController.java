package nl.strohalm.cyclos.webservices.rest.accounts.external.transferreports;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import nl.strohalm.cyclos.annotations.Inject;
import nl.strohalm.cyclos.entities.exceptions.UnexpectedEntityException;
import nl.strohalm.cyclos.services.accounts.external.ExternalTransferImportService;
import nl.strohalm.cyclos.utils.validation.ValidationException;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;

@Controller
public class RemoveTransferImportController extends BaseRestController {
	private ExternalTransferImportService externalTransferImportService;

	@Inject
	public void setExternalTransferImportService(
			final ExternalTransferImportService externalTransferImportService) {
		this.externalTransferImportService = externalTransferImportService;
	}

	public static class RemoveTransferImportRequestDto {
		private long transferImportId;

		public long getTransferImportId() {
			return transferImportId;
		}

		public void setTransferImportId(final long transferImportId) {
			this.transferImportId = transferImportId;
		}
	}

	public static class RemoveTransferImportResponseDto {
		public String message;

		public String getMessage() {
			return message;
		}

		public void setMessage(String message) {
			this.message = message;
		}
	}

	@RequestMapping(value = "admin/removeTransferImport", method = RequestMethod.DELETE)
	@ResponseBody
	protected RemoveTransferImportResponseDto executeAction(
			@RequestBody RemoveTransferImportRequestDto form) throws Exception {
		//final RemoveTransferImportForm form = context.getForm();
		final long id = form.getTransferImportId();
		if (id <= 0L) {
			throw new ValidationException();
		}
		RemoveTransferImportResponseDto response = new RemoveTransferImportResponseDto();
		try {
			externalTransferImportService.remove(id);
			response.setMessage("externalTransferImport.removed");
		} catch (final UnexpectedEntityException e) {
			response.setMessage("externalTransferImport.error.removing");
		}
		return response;
	}
}
