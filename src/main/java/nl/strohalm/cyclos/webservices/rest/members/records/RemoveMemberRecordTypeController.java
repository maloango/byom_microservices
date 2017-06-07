package nl.strohalm.cyclos.webservices.rest.members.records;

import org.apache.struts.action.ActionForward;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import nl.strohalm.cyclos.annotations.Inject;
import nl.strohalm.cyclos.controls.ActionContext;
import nl.strohalm.cyclos.controls.members.records.RemoveMemberRecordTypeForm;
import nl.strohalm.cyclos.exceptions.PermissionDeniedException;
import nl.strohalm.cyclos.services.elements.MemberRecordTypeService;
import nl.strohalm.cyclos.utils.validation.ValidationException;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;

@Controller
public class RemoveMemberRecordTypeController extends BaseRestController {
	private MemberRecordTypeService memberRecordTypeService;

	@Inject
	public void setMemberRecordTypeService(
			final MemberRecordTypeService memberRecordTypeService) {
		this.memberRecordTypeService = memberRecordTypeService;
	}

	public static class RemoveMemberRecordTypeRequestDto {
		private long memberRecordTypeId;

		public long getMemberRecordTypeId() {
			return memberRecordTypeId;
		}

		public void setMemberRecordTypeId(final long memberRecordTypeId) {
			this.memberRecordTypeId = memberRecordTypeId;
		}
	}

	public static class RemoveMemberRecordTypeResponseDto {
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
	protected RemoveMemberRecordTypeResponseDto executeAction(
			@RequestBody RemoveMemberRecordTypeRequestDto form)
			throws Exception {
		// final RemoveMemberRecordTypeForm form = context.getForm();
		final long id = form.getMemberRecordTypeId();
		if (id <= 0) {
			throw new ValidationException();
		}
		RemoveMemberRecordTypeResponseDto response = new RemoveMemberRecordTypeResponseDto();
		String message = null;
		try {
			memberRecordTypeService.remove(id);
			message = "memberRecordType.removed";
			response.setMessage(message);
			return response;
		} catch (final PermissionDeniedException e) {
			throw e;
		} catch (final Exception e) {
			message = "memberRecordType.error.removing";
			response.setMessage(message);
		}
		return response;
	}
}
