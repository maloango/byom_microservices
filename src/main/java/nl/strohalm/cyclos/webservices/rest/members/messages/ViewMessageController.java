package nl.strohalm.cyclos.webservices.rest.members.messages;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import nl.strohalm.cyclos.annotations.Inject;
import nl.strohalm.cyclos.entities.Relationship;
import nl.strohalm.cyclos.entities.access.AdminUser;
import nl.strohalm.cyclos.entities.access.MemberUser;
import nl.strohalm.cyclos.entities.access.User;
import nl.strohalm.cyclos.entities.members.Element;
import nl.strohalm.cyclos.entities.members.Member;
import nl.strohalm.cyclos.entities.members.messages.Message;
import nl.strohalm.cyclos.exceptions.PermissionDeniedException;
import nl.strohalm.cyclos.services.elements.MessageService;
import nl.strohalm.cyclos.utils.validation.ValidationException;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;

@Controller
public class ViewMessageController extends BaseRestController {
	private static final Relationship[] FETCH = {
			Message.Relationships.FROM_MEMBER, Message.Relationships.TO_MEMBER,
			Message.Relationships.TO_GROUPS, Message.Relationships.CATEGORY };
	private MessageService messageService;

	@Inject
	public void setMessageService(final MessageService messageService) {
		this.messageService = messageService;
	}

	public static class ViewMessageRequestDto {
		private long messageId;
		private Message message;
		private Long lastMessageId;
		private Element element;
		User user;

		public void setElement(Element element) {
			this.element = element;
		}

		public long getMessageId() {
			return messageId;
		}

		public void setMessageId(long messageId) {
			this.messageId = messageId;
		}

		public Message getMessage() {
			return message;
		}

		public void setMessage(Message message) {
			this.message = message;
		}

		public Long getLastMessageId() {
			return lastMessageId;
		}

		public void setLastMessageId(Long lastMessageId) {
			this.lastMessageId = lastMessageId;
		}

		public boolean isAdmin() {

			return user instanceof AdminUser;
		}

		public boolean isMember() {

			return user instanceof MemberUser;
		}

		public Element getElement() {
			return user.getElement();
		}

	}

	public static class ViewMessageResponseDto {
		private Long lastMessageId;
		private Message message;
		private boolean canReplyMessage;
		private boolean canManageMessage;

		public ViewMessageResponseDto(Long lastMessageId, Message message,
				boolean canReplyMessage, boolean canManageMessage) {
			super();
			this.lastMessageId = lastMessageId;
			this.message = message;
			this.canReplyMessage = canReplyMessage;
			this.canManageMessage = canManageMessage;
		}

	}

	@RequestMapping(value = "operator/viewMessage", method = RequestMethod.GET)
	@ResponseBody
	protected ViewMessageResponseDto executeAction(
			@RequestBody ViewMessageRequestDto form) throws Exception {
            ViewMessageResponseDto response  = null;
            try{
		long id = form.getMessageId();
		if (id <= 0L) {
			final Long lastMessageId = (Long) form.getLastMessageId();
			if (lastMessageId == null) {
				throw new ValidationException();
			} else {
				id = lastMessageId;
			}
		}
		final Message message = messageService.read(id, FETCH);
		Long lastMessageId = message.getId();
		// Ensure the message is not being viewed by someone else
		final Member owner = message.getOwner();

		final Element element = form.getElement();
		if ((owner == null && !form.isAdmin())
				|| (form.isMember() && !element.equals(owner))) {
			throw new PermissionDeniedException();
		}

		boolean canReplyMessage = false;
		boolean canManageMessage = false;
		canManageMessage = messageService.canManage(message);
		
		if (message.getFromMember() == null) { // the message came from
												// administration
			canReplyMessage = messageService.canSendToAdmin();
		} else { // the message came from another member
			canReplyMessage = messageService.canSendToMember(message
					.getFromMember());}
                        response = new ViewMessageResponseDto(
				lastMessageId, message, canReplyMessage, canManageMessage);
                }
                catch(Exception e){
                        e.printStackTrace();
                        }

		
		return response;
	}
}


