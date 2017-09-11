/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.strohalm.cyclos.webservices.rest.members.messages;

import nl.strohalm.cyclos.annotations.Inject;
import nl.strohalm.cyclos.entities.exceptions.DaoException;
import nl.strohalm.cyclos.services.elements.MessageCategoryService;
import nl.strohalm.cyclos.utils.validation.ValidationException;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;
import nl.strohalm.cyclos.webservices.rest.GenericResponse;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 *
 * @author Lue
 */
@Controller
public class RemoveMessageCategoryController extends BaseRestController {

    private MessageCategoryService messageCategoryService;

    public MessageCategoryService getMessageCategoryService() {
        return messageCategoryService;
    }

    @Inject
    public void setMessageCategoryService(final MessageCategoryService messageCategoryService) {
        this.messageCategoryService = messageCategoryService;
    }

    public static class RemoveMessageResponse extends GenericResponse {

        private long messageCategoryId;

        public long getMessageCategoryId() {
            return messageCategoryId;
        }

        public void setMessageCategoryId(final long messageCategoryId) {
            this.messageCategoryId = messageCategoryId;
        }
    }

    @RequestMapping(value = "member/removeMessage/{messageCategoryId}", method = RequestMethod.GET)
    @ResponseBody
    public RemoveMessageResponse removeMessage(@PathVariable("messageCategoryId") long messageCategoryId) throws Exception {
        //final RemoveMessageCategoryForm form = context.getForm();
        RemoveMessageResponse response = new RemoveMessageResponse();
       // final long id = LoggedUser.user().getId();
        if (messageCategoryId <= 0L) {
            throw new ValidationException();
        }

        try {
            messageCategoryService.remove(messageCategoryId);
            response.setMessage("messageCategory.removed");
        } catch (final DaoException e) {
            response.setMessage("messageCategory.error.removing");
        } catch (final DataIntegrityViolationException e) {
            response.setMessage("messageCategory.error.removing");
        }
        response.setStatus(0);
        response.setMessage("Message removed...");

        return response;

    }
}
