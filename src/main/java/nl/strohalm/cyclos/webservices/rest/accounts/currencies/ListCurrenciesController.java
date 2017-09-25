package nl.strohalm.cyclos.webservices.rest.accounts.currencies;

import java.math.BigDecimal;
import java.util.List;
import nl.strohalm.cyclos.access.AdminSystemPermission;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import nl.strohalm.cyclos.annotations.Inject;
import nl.strohalm.cyclos.entities.accounts.ARateParameters;
import nl.strohalm.cyclos.entities.accounts.Currency;
import nl.strohalm.cyclos.entities.accounts.DRateParameters;
import nl.strohalm.cyclos.entities.accounts.IRateParameters;
import nl.strohalm.cyclos.services.accounts.CurrencyService;
import nl.strohalm.cyclos.services.permissions.PermissionService;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;
import nl.strohalm.cyclos.webservices.rest.GenericResponse;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class ListCurrenciesController extends BaseRestController {

    private CurrencyService currencyService;
    private PermissionService permissionService;

    public final PermissionService getPermissionService() {
        return permissionService;
    }

    public final void setPermissionService(PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    public final CurrencyService getCurrencyService() {
        return currencyService;
    }

    @Inject
    public void setCurrencyService(final CurrencyService currencyService) {
        this.currencyService = currencyService;
    }

    public static class ListCurrenciesResponse extends GenericResponse{

        private List<Currency> currencies;
        private boolean editable;

        public List<Currency> getCurrencies() {
            return currencies;
        }

        public void setCurrencies(List<Currency> currencies) {
            this.currencies = currencies;
        }

        public boolean isEditable() {
            return editable;
        }

        public void setEditable(boolean editable) {
            this.editable = editable;
        }

    }

    @RequestMapping(value = "admin/listCurrencies", method = RequestMethod.GET)
    @ResponseBody
    protected ListCurrenciesResponse executeAction() throws Exception {
        ListCurrenciesResponse response = new ListCurrenciesResponse();
        final List<Currency> currencies = currencyService.listAll();
       
        response.setCurrencies(currencies);
        response.setEditable(permissionService.hasPermission(AdminSystemPermission.CURRENCIES_MANAGE));
        response.setStatus(0);
        response.setMessage("list of currency!!");
        return response;
    }

}
