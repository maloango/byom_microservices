package nl.strohalm.cyclos.webservices.rest.accounts.external;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import nl.strohalm.cyclos.annotations.Inject;
import nl.strohalm.cyclos.entities.accounts.external.ExternalTransferType;
import nl.strohalm.cyclos.exceptions.PermissionDeniedException;
import nl.strohalm.cyclos.services.accounts.external.ExternalTransferTypeService;
import nl.strohalm.cyclos.utils.validation.ValidationException;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;
import nl.strohalm.cyclos.webservices.rest.GenericResponse;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class RemoveExternalTransferTypeController extends BaseRestController {

    @RequestMapping(value = "admin/removeExternalTransferType/{ExternalTransferTypeId}", method = RequestMethod.GET)
    @ResponseBody
    protected GenericResponse executeAction(@PathVariable("ExternalTransferTypeId") long ExternalTransferTypeId)
            throws Exception {
        // final RemoveExternalTransferTypeForm form = context.getForm();
        GenericResponse response = new GenericResponse();
        final long id = ExternalTransferTypeId;
        if (id <= 0L) {
            throw new ValidationException();
        }
        final ExternalTransferType transferType = externalTransferTypeService.load(id);
        try {
            externalTransferTypeService.remove(id);
            response.setMessage("externalTransferType.removed");
        } catch (final PermissionDeniedException e) {
            throw e;
        } catch (final Exception e) {
            response.setMessage("externalTransferType.error.removing");
        }

        return response;

    }
}
