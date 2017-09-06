package nl.strohalm.cyclos.webservices.rest.accounts.currencies;

import java.util.Calendar;
import java.util.List;
import nl.strohalm.cyclos.access.AdminSystemPermission;
import nl.strohalm.cyclos.access.Permission;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import nl.strohalm.cyclos.annotations.Inject;
import nl.strohalm.cyclos.entities.accounts.Currency;
import nl.strohalm.cyclos.entities.accounts.transactions.SysOutTransferListener;
import nl.strohalm.cyclos.services.accounts.CurrencyService;
import nl.strohalm.cyclos.services.accounts.rates.RateService;
import nl.strohalm.cyclos.services.permissions.PermissionService;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;
import nl.strohalm.cyclos.webservices.rest.GenericResponse;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class ManageCurrencyController extends BaseRestController {

    private RateService rateService;
    private CurrencyService currencyService;

    @Inject
    public CurrencyService getCurrencyService() {
        return currencyService;
    }

    @Inject
    public void setCurrencyService(CurrencyService currencyService) {
        this.currencyService = currencyService;
    }

    @Inject
    public RateService getRateService() {
        return rateService;
    }

    @Inject
    public void setRateService(RateService rateService) {
        this.rateService = rateService;
    }

    public static class ManageCurrencyResponse extends GenericResponse {

        private Currency currency;
        private Calendar pendingRateInitProgression;
        private boolean ratesEnabled;

        public boolean isRatesEnabled() {
            return ratesEnabled;
        }

        public void setRatesEnabled(boolean ratesEnabled) {
            this.ratesEnabled = ratesEnabled;
        }

        public Currency getCurrency() {
            return currency;
        }

        public void setCurrency(Currency currency) {
            this.currency = currency;
        }

        public Calendar getPendingRateInitProgression() {
            return pendingRateInitProgression;
        }

        public void setPendingRateInitProgression(Calendar pendingRateInitProgression) {
            this.pendingRateInitProgression = pendingRateInitProgression;
        }
    }

    @RequestMapping(value = "admin/manageCurrency/{currencyId}", method = RequestMethod.GET)
    @ResponseBody
    protected ManageCurrencyResponse addCurrency(@PathVariable("currencyId") long currencyId) throws Exception {
        ManageCurrencyResponse response = new ManageCurrencyResponse();
        final boolean isInsert = currencyId <= 0L;
        Currency currency;
        if (isInsert) {
            currency = new Currency();
        } else {
            currency = currencyService.load(currencyId);
        }
        response.setCurrency(currency);
        final boolean ratesEnabled = rateService.isAnyRateEnabled(currency, null);
        response.setRatesEnabled(ratesEnabled);
        // get the progress on any pending rate initialization
        final Calendar pendingRateInitProgression = rateService.checkPendingRateInitializations(currency);
        response.setPendingRateInitProgression(pendingRateInitProgression);
        response.setStatus(0);
        response.setMessage("currency added !!");
        return response;
    }

}
