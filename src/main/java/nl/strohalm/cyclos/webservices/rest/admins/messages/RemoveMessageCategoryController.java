/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.strohalm.cyclos.webservices.rest.admins.messages;

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
public class RemoveMessageCategoryController extends BaseRestController {

    @RequestMapping(value = "admin/removeMessageCategory/{id}", method = RequestMethod.GET)
    @ResponseBody
    public GenericResponse removeMessage(@PathVariable("id") Long id) {
        GenericResponse response = new GenericResponse();
        int i = messageCategoryService.remove(id);
        if (i > 0) {
            response.setMessage("Message category removed!");
        } else {
            response.setMessage("error while removing!");
        }
        response.setStatus(0);
        return response;
    }
}
