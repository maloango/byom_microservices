package nl.strohalm.cyclos.webservices.rest.members.messages;

import org.apache.struts.action.ActionForward;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import nl.strohalm.cyclos.annotations.Inject;
import nl.strohalm.cyclos.controls.ActionContext;
import nl.strohalm.cyclos.controls.members.messages.ChangeMessageStatusForm;
import nl.strohalm.cyclos.services.elements.MessageAction;
import nl.strohalm.cyclos.services.elements.MessageService;
import nl.strohalm.cyclos.utils.conversion.CoercionHelper;
import nl.strohalm.cyclos.utils.validation.ValidationException;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;

@Controller
public class ChangeMessageStatusController extends BaseRestController {
	private MessageService messageService;

	@Inject
	public void setMessageService(final MessageService messageService) {
		this.messageService = messageService;
	}

	public static class ChangeMessageStatusRequestDto {
		private Long[] messageId;
		private String action;

		public String getAction() {
			return action;
		}

		public Long[] getMessageId() {
			return messageId;
		}

		public void setAction(final String action) {
			this.action = action;
		}

		public void setMessageId(final Long[] messageId) {
			this.messageId = messageId;
		}
	}

	public static class ChangeMessageStatusResponseDto {
		private String message;

		public String getMessage() {
			return message;
		}

		public void setMessage(String message) {
			this.message = message;
		}
	}

	@RequestMapping(value = "admin/changeMessageStatus", method = RequestMethod.PUT)
	@ResponseBody
	protected ChangeMessageStatusResponseDto executeAction(
			@RequestBody ChangeMessageStatusRequestDto form) throws Exception {
		ChangeMessageStatusResponseDto response = null;
                try{

		final MessageAction action = CoercionHelper.coerce(MessageAction.class,
				form.getAction());
		final Long[] ids = form.getMessageId();
		if (action == null || ids == null || ids.length == 0) {
			throw new ValidationException();
		}

		messageService.performAction(action, ids);
		//response = new ChangeMessageStatusResponseDto();
		switch (action) {
		case DELETE:
		case MOVE_TO_TRASH:
		case RESTORE:
			response.setMessage("message.actionPerformed." + action);
		}
                response = new ChangeMessageStatusResponseDto();}
                catch(Exception e){
                    e.printStackTrace();
                }

		return response;
	}

}
