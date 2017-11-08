/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.strohalm.cyclos.webservices.rest.ads;

import nl.strohalm.cyclos.entities.ads.AdCategory;
import nl.strohalm.cyclos.entities.exceptions.DaoException;
import nl.strohalm.cyclos.utils.ActionHelper;
import nl.strohalm.cyclos.utils.validation.ValidationException;
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
public class RemoveAdCategoryController extends BaseRestController {

    @RequestMapping(value = "admin/removeAdCategory/{id}", method = RequestMethod.GET)
    @ResponseBody
    public GenericResponse removeAdCategory(@PathVariable("id") Long id) {
        GenericResponse response = new GenericResponse();
        if (id <= 0) {
            throw new ValidationException();
        }
        try {
            final AdCategory adCategory = adCategoryService.load(id, AdCategory.Relationships.PARENT);
            final AdCategory parent = adCategory.getParent();
            adCategoryService.remove(id);
            response.setMessage("adCategory.removed");

        } catch (final DaoException e) {
            // return context.sendError("adCategory.error.removing");
        }
        response.setStatus(0);
        return response;

    }
}
