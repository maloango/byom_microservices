package nl.strohalm.cyclos.webservices.rest.accounts.guarantees.types;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import nl.strohalm.cyclos.annotations.Inject;
import nl.strohalm.cyclos.exceptions.PermissionDeniedException;
import nl.strohalm.cyclos.services.accounts.guarantees.GuaranteeTypeService;
import nl.strohalm.cyclos.utils.validation.ValidationException;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class DeleteGuaranteeTypeController extends BaseRestController {
	private GuaranteeTypeService guaranteeTypeService;

	public final GuaranteeTypeService getGuaranteeTypeService() {
		return guaranteeTypeService;
	}

	public static class DeleteGuaranteeTypeRequestDto {
		private long guaranteeTypeId;

		public long getGuaranteeTypeId() {
			return guaranteeTypeId;
		}

		public void setGuaranteeTypeId(final long guaranteeTypeId) {
			this.guaranteeTypeId = guaranteeTypeId;
		}
	}

	public static class DeleteGuaranteeTypeResponseDto {
		public String message;

		public String getMessage() {
			return message;
		}

		public void setMessage(String message) {
			this.message = message;
		}
	}

	@RequestMapping(value = "admin/deleteGuaranteeType{guaranteeTypeId}", method = RequestMethod.GET)
	@ResponseBody
	public DeleteGuaranteeTypeResponseDto executeAction(
			@PathVariable ("guaranteeTypeId") long guaranteeTypeId) throws Exception {
		// 
		final long id =guaranteeTypeId ;
		if (id <= 0L) {
			throw new ValidationException();
		}
		DeleteGuaranteeTypeResponseDto response = new DeleteGuaranteeTypeResponseDto();
		try {
			guaranteeTypeService.remove(id);
			response.setMessage("guaranteeType.removed");
		} catch (final PermissionDeniedException e) {
			throw e;
		} catch (final Exception e) {
			response.setMessage("guaranteeType.error.removing");
		}
		return response;
	}

	@Inject
	public void setGuaranteeTypeService(
			final GuaranteeTypeService guaranteeTypeService) {
		this.guaranteeTypeService = guaranteeTypeService;
	}
}
