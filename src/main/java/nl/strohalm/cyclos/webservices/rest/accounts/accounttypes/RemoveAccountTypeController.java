package nl.strohalm.cyclos.webservices.rest.accounts.accounttypes;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import nl.strohalm.cyclos.annotations.Inject;
import nl.strohalm.cyclos.services.accounts.AccountTypeService;
import nl.strohalm.cyclos.utils.validation.ValidationException;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;
import nl.strohalm.cyclos.webservices.rest.GenericResponse;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class RemoveAccountTypeController extends BaseRestController {

    private AccountTypeService accountTypeService;

    public AccountTypeService getAccountTypeService() {
        return accountTypeService;
    }

    @Inject
    public void setAccountTypeService(AccountTypeService accountTypeService) {
        this.accountTypeService = accountTypeService;
    }

    @RequestMapping(value = "admin/removeAccountType/{accountTypeId}", method = RequestMethod.GET)
    @ResponseBody
    public GenericResponse executeAction(@PathVariable("accountTypeId") long accountTypeId) {
        GenericResponse response = new GenericResponse();
        if (accountTypeId <= 0) {
            throw new ValidationException();
        }
        try {
            accountTypeService.remove(accountTypeId);
            response.setMessage("accountType.removed");
        } catch (final Exception e) {
            //return context.sendError("accountType.error.removing");
        }
        response.setStatus(0);
        response.setMessage("Account removed !!");
        return response;

    }

}
