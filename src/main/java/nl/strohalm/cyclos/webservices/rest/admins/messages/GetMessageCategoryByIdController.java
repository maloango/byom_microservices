/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.strohalm.cyclos.webservices.rest.admins.messages;

import nl.strohalm.cyclos.entities.members.messages.MessageCategory;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;
import nl.strohalm.cyclos.webservices.rest.GenericResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 *
 * @author Lue Infoservices
 */
@Controller
public class GetMessageCategoryByIdController extends BaseRestController {

    public static class MessageResponse extends GenericResponse {

        private Message messageCategory;

        public Message getMessageCategory() {
            return messageCategory;
        }

        public void setMessageCategory(Message messageCategory) {
            this.messageCategory = messageCategory;
        }

    }

    public static class Message {

        private Long id;
        private String name;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

    }

    @RequestMapping(value = "admin/getMessageCategoryById/{id}", method = RequestMethod.GET)
    @ResponseBody
    public MessageResponse messageCategory(@PathVariable("id") Long id) {
        MessageResponse response = new MessageResponse();
        MessageCategory category = messageCategoryService.load(id);
        Message mesg = new Message();
        mesg.setId(category.getId());
        mesg.setName(category.getName());
        response.setMessageCategory(mesg);
        response.setStatus(0);
        return response;

    }
}
