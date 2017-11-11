/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.strohalm.cyclos.webservices.rest.admins.messages;

import java.util.ArrayList;
import java.util.List;
import nl.strohalm.cyclos.entities.members.messages.MessageCategory;
import nl.strohalm.cyclos.entities.members.messages.MessageCategoryQuery;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;
import nl.strohalm.cyclos.webservices.rest.GenericResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 *
 * @author Lue Infoservices
 */
@Controller
public class ListMessageCategoriesController extends BaseRestController {
    
    public static class ListMessageCategories extends GenericResponse {
        
        private List<MessageEntity> messageCategory;
        
        public List<MessageEntity> getMessageCategory() {
            return messageCategory;
        }
        
        public void setMessageCategory(List<MessageEntity> messageCategory) {
            this.messageCategory = messageCategory;
        }
        
    }
    
    public static class MessageEntity {
        
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
    
    @RequestMapping(value = "admin/listMessageCategories", method = RequestMethod.GET)
    @ResponseBody
    public ListMessageCategories ListMessage() throws Exception {
        ListMessageCategories response = new ListMessageCategories();
        try {
            final MessageCategoryQuery query = new MessageCategoryQuery();
            List<MessageCategory> search = messageCategoryService.search(query);
            List<MessageEntity> messages = new ArrayList();
            for (MessageCategory mesg : search) {
                MessageEntity entity = new MessageEntity();
                entity.setId(mesg.getId());
                entity.setName(mesg.getName());
                messages.add(entity);
            }
            response.setMessageCategory(messages);
        } catch (Exception e) {
            e.printStackTrace();
        }
        response.setStatus(0);
        //response.setMessage("List Messsage Searching!!!!!");
        
        return response;
        
    }
}
