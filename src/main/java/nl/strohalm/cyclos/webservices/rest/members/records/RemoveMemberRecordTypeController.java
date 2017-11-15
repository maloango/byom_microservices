/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.strohalm.cyclos.webservices.rest.members.records;

import nl.strohalm.cyclos.exceptions.PermissionDeniedException;
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
public class RemoveMemberRecordTypeController extends BaseRestController {

    @RequestMapping(value = "admin/removeMemberRecordType/{id}", method = RequestMethod.GET)
    @ResponseBody
    public GenericResponse remove(@PathVariable("id") Long id) {
        GenericResponse response = new GenericResponse();
        if (id <= 0) {
            throw new ValidationException();
        }
        try {
            memberRecordTypeService.remove(id);
            response.setMessage("memberRecordType.removed");
        } catch (final PermissionDeniedException e) {
            throw e;
        } catch (final Exception e) {
            response.setMessage("memberRecordType.error.removing");
        }
        response.setStatus(0);
        return response;
    }
}
