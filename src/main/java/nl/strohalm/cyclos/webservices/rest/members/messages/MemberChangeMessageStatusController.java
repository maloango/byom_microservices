/*
    This file is part of Cyclos (www.cyclos.org).
    A project of the Social Trade Organisation (www.socialtrade.org).

    Cyclos is free software; you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation; either version 2 of the License, or
    (at your option) any later version.

    Cyclos is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with Cyclos; if not, write to the Free Software
    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA

 */
package nl.strohalm.cyclos.webservices.rest.members.messages;

import nl.strohalm.cyclos.annotations.Inject;
import nl.strohalm.cyclos.services.elements.MessageAction;
import nl.strohalm.cyclos.services.elements.MessageService;
import nl.strohalm.cyclos.utils.conversion.CoercionHelper;
import nl.strohalm.cyclos.utils.validation.ValidationException;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;
import nl.strohalm.cyclos.webservices.rest.GenericResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Action used to change the status for a message list
 *
 * @author luis
 */
@Controller
public class MemberChangeMessageStatusController extends BaseRestController {

    private MessageService messageService;

    @Inject
    public void setMessageService(final MessageService messageService) {
        this.messageService = messageService;
    }

    public static class ChangeMessageStatusResponse extends GenericResponse {

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

    @RequestMapping(value = "member/changeMessageStatus", method = RequestMethod.POST)
    @ResponseBody

    public GenericResponse changeMessageStatus(@RequestBody ChangeMessageStatusResponse request) throws Exception {
        GenericResponse response = new GenericResponse();
        try {

            final MessageAction action = CoercionHelper.coerce(MessageAction.class, request.getAction());
            final Long[] ids = request.getMessageId();
            if (action == null || ids == null || ids.length == 0) {
                throw new ValidationException();
            }

            messageService.performAction(action, ids);

            switch (action) {
                case DELETE:
                case MOVE_TO_TRASH:
                case RESTORE:
                    response.setMessage("message.actionPerformed." + action);
            }
            response = new GenericResponse();
        } catch (Exception e) {
           
        }
        response.setStatus(0);
        response.setMessage("Message status changed..");
        return response;
       
    }
}

