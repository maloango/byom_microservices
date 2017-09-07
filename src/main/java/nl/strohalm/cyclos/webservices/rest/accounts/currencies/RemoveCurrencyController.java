package nl.strohalm.cyclos.webservices.rest.accounts.currencies;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import nl.strohalm.cyclos.annotations.Inject;
import nl.strohalm.cyclos.services.accounts.CurrencyService;
import nl.strohalm.cyclos.utils.validation.ValidationException;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;
import nl.strohalm.cyclos.webservices.rest.GenericResponse;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class RemoveCurrencyController extends BaseRestController {

    private CurrencyService currencyService;

    @Inject
    public final CurrencyService getCurrencyService() {
        return currencyService;
    }

    @Inject
    public void setCurrencyService(final CurrencyService currencyService) {
        this.currencyService = currencyService;
    }

    @RequestMapping(value = "admin/removeCurrency/{currencyId}", method = RequestMethod.GET)
    @ResponseBody
    protected GenericResponse executeAction(@PathVariable("currencyId") long currencyId) throws Exception {
        GenericResponse response = new GenericResponse();
        final long id = currencyId;
        if (id <= 0L) {
            throw new ValidationException();
        }

        try {
            currencyService.remove(id);
            response.setMessage("currency.removed");
        } catch (final Exception e) {
            response.setMessage("currency.error.removing");
        }

        return response;
    }
}
