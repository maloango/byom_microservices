package nl.strohalm.cyclos.webservices.rest.customization.translationMessages;

import org.apache.struts.action.ActionForward;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import nl.strohalm.cyclos.annotations.Inject;
import nl.strohalm.cyclos.controls.ActionContext;
import nl.strohalm.cyclos.controls.customization.translationMessages.RemoveTranslationMessagesForm;
import nl.strohalm.cyclos.services.customization.TranslationMessageService;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;

@Controller
public class RemoveTranslationMessagesController extends BaseRestController {
	private TranslationMessageService translationMessageService;

	@Inject
	public void setTranslationMessageService(
			final TranslationMessageService translationMessageService) {
		this.translationMessageService = translationMessageService;
	}

	public static class RemoveTranslationMessagesRequestDto {
		private Long[] messageIds;

		public Long[] getMessageIds() {
			return messageIds;
		}

		public void setMessageIds(final Long[] messageIds) {
			this.messageIds = messageIds;
		}
	}

	public static class RemoveTranslationMessagesResponseDto {
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
	protected RemoveTranslationMessagesResponseDto executeAction(
			final RemoveTranslationMessagesRequestDto form) throws Exception {
		// final RemoveTranslationMessagesForm form = context.getForm();
		translationMessageService.remove(form.getMessageIds());
		String message = "translationMessage.removed";
		RemoveTranslationMessagesResponseDto response = new RemoveTranslationMessagesResponseDto();
		response.setMessage(message);
		return response;
	}
}
