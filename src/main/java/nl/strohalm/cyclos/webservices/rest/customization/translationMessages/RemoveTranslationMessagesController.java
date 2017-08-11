package nl.strohalm.cyclos.webservices.rest.customization.translationMessages;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import nl.strohalm.cyclos.annotations.Inject;
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

	public final TranslationMessageService getTranslationMessageService() {
		return translationMessageService;
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

	@RequestMapping(value = "admin/removeTranslationMessages", method = RequestMethod.POST)
	@ResponseBody
	protected RemoveTranslationMessagesResponseDto executeAction(
			final RemoveTranslationMessagesRequestDto form) throws Exception {
		RemoveTranslationMessagesResponseDto response =null;
                try{
		translationMessageService.remove(form.getMessageIds());
		String message = "translationMessage.removed";
                
		response = new RemoveTranslationMessagesResponseDto();
                response.setMessage(message);}
		catch(Exception e){
                    e.printStackTrace();
                            
}
               
		return response;
	}
}
