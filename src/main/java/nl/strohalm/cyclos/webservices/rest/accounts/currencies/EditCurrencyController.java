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

    public static class AddCurrencyRequest extends Currency {

        private boolean enableARate;
        private boolean enableDRate;
        private boolean enableIRate;

        public boolean isEnableARate() {
            return enableARate;
        }

        public void setEnableARate(boolean enableARate) {
            this.enableARate = enableARate;
        }

        public boolean isEnableDRate() {
            return enableDRate;
        }

        public void setEnableDRate(boolean enableDRate) {
            this.enableDRate = enableDRate;
        }

        public boolean isEnableIRate() {
            return enableIRate;
        }

        public void setEnableIRate(boolean enableIRate) {
            this.enableIRate = enableIRate;
        }

    }

  @RequestMapping(value = "admin/addCurrency", method = RequestMethod.POST)
    @ResponseBody
 public GenericResponse addCurrency(@RequestBody AddCurrencyRequest currency) {
       GenericResponse response = new GenericResponse();
      final boolean isInsert = currency.isTransient();
      final WhatRate whatRate = new WhatRate();
      whatRate.setaRate(currency.isEnableARate());
      whatRate.setdRate(currency.isEnableDRate());
      whatRate.setiRate(currency.isEnableIRate());
      currency = (AddCurrencyRequest) currencyService.save(currency, whatRate);
      if (isInsert) {
          response.setMessage("currency.inserted");
      } else {
          response.setMessage("currency.modified");
      }
      response.setStatus(0);
       return response;
  }

}
