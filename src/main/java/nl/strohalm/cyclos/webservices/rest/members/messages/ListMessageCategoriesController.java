/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.strohalm.cyclos.webservices.rest.members.messages;

import java.util.List;
import nl.strohalm.cyclos.annotations.Inject;
import nl.strohalm.cyclos.entities.members.messages.MessageCategory;
import nl.strohalm.cyclos.entities.members.messages.MessageCategoryQuery;
import nl.strohalm.cyclos.services.elements.MessageCategoryService;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;
import nl.strohalm.cyclos.webservices.rest.GenericResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 *
 * @author Lue
 */
@Controller
public class ListMessageCategoriesController extends BaseRestController {

    private MessageCategoryService messageCategoryService;

    @Inject
    public void setMessageCategoryService(
            final MessageCategoryService messageCategoryService) {
        this.messageCategoryService = messageCategoryService;
    }

    public static class ListMessageCategories extends GenericResponse {

        //private Element fromElement;
        //private Element toElement;
        //private Collection<? extends Group> groups;
        private List<MessageCategory> search;
        private MessageCategory messageCategory;

        public MessageCategory getMessageCategory() {
            return messageCategory;
        }

        public void setMessageCategory(MessageCategory messageCategory) {
            this.messageCategory = messageCategory;
        }
        

        public List<MessageCategory> getSearch() {
            return search;
        }

        public void setSearch(List<MessageCategory> search) {
            this.search = search;
        }

//        public Element getFromElement() {
//            return fromElement;
//        }
//
//        public Collection<? extends Group> getGroups() {
//            return groups;
//        }
//
//        public Element getToElement() {
//            return toElement;
//        }
//
//        public void setFromElement(final Element fromElement) {
//            this.fromElement = fromElement;
//        }
//
//        public void setGroups(final Collection<? extends Group> groups) {
//            this.groups = groups;
//        }
//
//        public void setToElement(final Element toElement) {
//            this.toElement = toElement;
//        }

    }

    @RequestMapping(value = "member/listMessageCategories", method = RequestMethod.GET)
    @ResponseBody
    public ListMessageCategories ListMessage() throws Exception {
        ListMessageCategories response = new ListMessageCategories();
        try {
            final MessageCategoryQuery query = new MessageCategoryQuery();
            List<MessageCategory> search = messageCategoryService.search(query);
            //response.setMessageCategories(messageCategoryService.search(query));
            search.listIterator();
            response.setSearch(search);
        } catch (Exception e) {
            e.printStackTrace();
        }
        response.setStatus(0);
        response.setMessage("List Messsage Searching!!!!!");
        

        return response;

    }
}
