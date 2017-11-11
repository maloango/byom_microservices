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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 *
 * @author Lue Infoservices
 */
@Controller
public class EditMessageCategoryController extends BaseRestController {

    public static class MessageParameters {

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

    @RequestMapping(value = "admin/editMessageCategory", method = RequestMethod.POST)
    @ResponseBody
    public GenericResponse edit(@RequestBody MessageParameters params) {
        GenericResponse response = new GenericResponse();
        final MessageCategory category = new MessageCategory();
        if (params.getId() != null && params.getId() > 0L) {
            category.setId(params.getId());
        }
        category.setName(params.getName());
        final boolean insert = category.getId() == null;
        messageCategoryService.save(category);
        response.setMessage(insert ? "messageCategory.inserted" : "messageCategory.modified");
        response.setStatus(0);
        return response;
    }
}
