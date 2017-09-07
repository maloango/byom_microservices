package nl.strohalm.cyclos.webservices.rest.accounts.currencies;

import java.math.BigDecimal;
import java.util.Calendar;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import nl.strohalm.cyclos.annotations.Inject;
import nl.strohalm.cyclos.entities.accounts.ARateParameters;
import nl.strohalm.cyclos.entities.accounts.Currency;
import nl.strohalm.cyclos.entities.accounts.DRateParameters;
import nl.strohalm.cyclos.entities.accounts.IRateParameters;
import nl.strohalm.cyclos.entities.settings.LocalSettings;
import nl.strohalm.cyclos.services.accounts.CurrencyService;
import nl.strohalm.cyclos.services.accounts.rates.RateService;
import nl.strohalm.cyclos.services.accounts.rates.WhatRate;
import nl.strohalm.cyclos.services.permissions.PermissionService;
import nl.strohalm.cyclos.services.settings.SettingsService;
import nl.strohalm.cyclos.utils.ActionHelper;
import nl.strohalm.cyclos.utils.binding.BeanBinder;
import nl.strohalm.cyclos.utils.binding.DataBinder;
import nl.strohalm.cyclos.utils.binding.PropertyBinder;
import nl.strohalm.cyclos.utils.conversion.IdConverter;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;
import nl.strohalm.cyclos.webservices.rest.GenericResponse;

@Controller
public class EditCurrencyController extends BaseRestController {

    private CurrencyService currencyService;

    @Inject
    public CurrencyService getCurrencyService() {
        return currencyService;
    }

    @Inject
    public void setCurrencyService(CurrencyService currencyService) {
        this.currencyService = currencyService;
    }

    public static class AddCurrencyRequest {

        private Long id;
        private String name;
        private String description;
        private String symbol;
        private String pattern = "#amount#";
        private DRateParameters dRateParameters;
        private ARateParameters aRateParameters;
        private IRateParameters iRateParameters;

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

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getSymbol() {
            return symbol;
        }

        public void setSymbol(String symbol) {
            this.symbol = symbol;
        }

        public String getPattern() {
            return pattern;
        }

        public void setPattern(String pattern) {
            this.pattern = pattern;
        }

        public DRateParameters getdRateParameters() {
            return dRateParameters;
        }

        public void setdRateParameters(DRateParameters dRateParameters) {
            this.dRateParameters = dRateParameters;
        }

        public ARateParameters getaRateParameters() {
            return aRateParameters;
        }

        public void setaRateParameters(ARateParameters aRateParameters) {
            this.aRateParameters = aRateParameters;
        }

        public IRateParameters getiRateParameters() {
            return iRateParameters;
        }

        public void setiRateParameters(IRateParameters iRateParameters) {
            this.iRateParameters = iRateParameters;
        }

    }

    @RequestMapping(value = "admin/addCurrency", method = RequestMethod.POST)
    @ResponseBody
    public GenericResponse addCurrency(@RequestBody AddCurrencyRequest request) {
        GenericResponse response = new GenericResponse();
        Currency currency = new Currency();
        currency.setDescription(request.getDescription());
        currency.setPattern(request.getPattern());
        currency.setName(request.getName());
        currency.setSymbol(request.getSymbol());
        currency.setId(request.getId());

        final boolean isInsert = currency.isTransient();
        final WhatRate whatRate = new WhatRate();
        whatRate.setaRate(currency.isEnableARate());
        whatRate.setdRate(currency.isEnableDRate());
        whatRate.setiRate(currency.isEnableIRate());
        currency = currencyService.save(currency, whatRate);
        if (isInsert) {
            response.setMessage("currency.inserted");
        } else {
            response.setMessage("currency.modified");
        }
        response.setStatus(0);
        return response;
    }

}
