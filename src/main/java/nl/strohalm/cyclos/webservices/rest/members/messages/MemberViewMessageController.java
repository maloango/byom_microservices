/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.strohalm.cyclos.webservices.rest.members.messages;

import nl.strohalm.cyclos.annotations.Inject;
import nl.strohalm.cyclos.entities.Relationship;
import nl.strohalm.cyclos.entities.members.Element;
import nl.strohalm.cyclos.entities.members.Member;
import nl.strohalm.cyclos.entities.members.messages.Message;
import nl.strohalm.cyclos.exceptions.PermissionDeniedException;
import nl.strohalm.cyclos.services.elements.MessageService;
import nl.strohalm.cyclos.utils.access.LoggedUser;
import nl.strohalm.cyclos.utils.validation.ValidationException;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;
import nl.strohalm.cyclos.webservices.rest.GenericResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 *
 * @author Lue
 */
@Controller
public class MemberViewMessageController extends BaseRestController{
     private static final Relationship[] FETCH = { Message.Relationships.FROM_MEMBER, Message.Relationships.TO_MEMBER, Message.Relationships.TO_GROUPS, Message.Relationships.CATEGORY };
    private MessageService              messageService;

    @Inject
    public void setMessageService(final MessageService messageService) {
        this.messageService = messageService;
    }
    
    
    public static class ViewMessageRequest{
         private long              messageId;
         private long lastMessageId;

        public long getLastMessageId() {
            return lastMessageId;
        }

        public void setLastMessageId(long lastMessageId) {
            this.lastMessageId = lastMessageId;
        }
          

    public long getMessageId() {
        return messageId;
    }

    public void setMessageId(final long messageId) {
        this.messageId = messageId;
    }
    

    }
    
    public static class ViewMessageResponse extends GenericResponse{
        private boolean canManageMessage;
        private boolean canReplyMessage;
        private boolean message;
      

        public boolean isCanManageMessage() {
            return canManageMessage;
        }

        public void setCanManageMessage(boolean canManageMessage) {
            this.canManageMessage = canManageMessage;
        }

        public boolean isCanReplyMessage() {
            return canReplyMessage;
        }

        public void setCanReplyMessage(boolean canReplyMessage) {
            this.canReplyMessage = canReplyMessage;
        }

        public boolean isMessage() {
            return message;
        }

        public void setMessage(boolean message) {
            this.message = message;
        }
        
        
        
    }

   @RequestMapping(value = "member/viewMessage", method = RequestMethod.GET )
    @ResponseBody
    protected ViewMessageResponse viewMessage(@RequestBody ViewMessageRequest request) throws Exception {
        //final ViewMessageForm form = context.getForm();
        ViewMessageResponse response = new ViewMessageResponse();
        try{
        long id = request.getMessageId();
        if (id <= 0L) {
            //final Long lastMessageId = (Long) request.getSession.getAttribute("lastMessageId");
              final Long lastMessageId = (Long) request.getLastMessageId();
            if (lastMessageId == null) {
                throw new ValidationException();
            } else {
                id = lastMessageId;
            }
        }
        final Message message = messageService.read(id, FETCH);
       // response..setLastMessageId("lastMessageId", message.getId());
       request.setLastMessageId(id);

        // Ensure the message is not being viewed by someone else
        final Member owner = message.getOwner();
       final Element element = LoggedUser.element();
        if ((owner == null && !LoggedUser.isAdministrator()) || (LoggedUser.isMember() && !element.equals(owner))) {
            throw new PermissionDeniedException();
        }

        response.setMessage("message");
        //response.setCanManageMessage(messageService.canManage(message));
        response.setCanManageMessage(messageService.canManage(message));
        if (message.getFromMember() == null) { // the message came from administration
            response.setCanReplyMessage(messageService.canSendToAdmin());
        } else { // the message came from another member
            response.setCanReplyMessage(messageService.canSendToMember(message.getFromMember()));
        }}
        catch(Exception e){
                
                }
        
       response.setStatus(0);
        response.setMessage("View Message.");
        return response;
    }

    
    
}
