package nl.strohalm.cyclos.webservices.rest.accounts.external;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import nl.strohalm.cyclos.annotations.Inject;
import nl.strohalm.cyclos.exceptions.PermissionDeniedException;
import nl.strohalm.cyclos.services.accounts.external.ExternalAccountService;
import nl.strohalm.cyclos.utils.validation.ValidationException;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;
import nl.strohalm.cyclos.webservices.rest.GenericResponse;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class RemoveExternalAccountController extends BaseRestController {

    @RequestMapping(value = "admin/removeExternalAccount/{accountId}", method = RequestMethod.GET)
    @ResponseBody
    protected GenericResponse executeAction(@PathVariable("accountId") Long accountId) throws Exception {
        GenericResponse response = new GenericResponse();
        try {
            externalAccountService.remove(accountId);
            response.setMessage("externalAccount.removed");
        } catch (final PermissionDeniedException e) {
            throw e;
        } catch (final Exception e) {
            response.setMessage("externalAccount.error.removing");
        }
        return response;
    }

}
