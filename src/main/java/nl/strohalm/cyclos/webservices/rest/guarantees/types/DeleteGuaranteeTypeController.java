package nl.strohalm.cyclos.webservices.rest.guarantees.types;

import org.apache.struts.action.ActionForward;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import nl.strohalm.cyclos.annotations.Inject;
import nl.strohalm.cyclos.controls.ActionContext;
import nl.strohalm.cyclos.controls.accounts.guarantees.types.DeleteGuaranteeTypeForm;
import nl.strohalm.cyclos.exceptions.PermissionDeniedException;
import nl.strohalm.cyclos.services.accounts.guarantees.GuaranteeTypeService;
import nl.strohalm.cyclos.utils.validation.ValidationException;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;

@Controller
public class DeleteGuaranteeTypeController extends BaseRestController {
	private GuaranteeTypeService guaranteeTypeService;

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

	@RequestMapping(value = "/admin/removeAccountType", method = RequestMethod.DELETE)
	@ResponseBody
	public DeleteGuaranteeTypeResponseDto executeAction(
			@RequestBody DeleteGuaranteeTypeRequestDto form) throws Exception {
		// final DeleteGuaranteeTypeForm form = context.getForm();
		final long id = form.getGuaranteeTypeId();
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
