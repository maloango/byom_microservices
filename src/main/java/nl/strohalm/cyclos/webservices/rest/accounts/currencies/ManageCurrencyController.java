package nl.strohalm.cyclos.webservices.rest.accounts.currencies;

import java.util.Calendar;

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
import nl.strohalm.cyclos.webservices.rest.BaseRestController;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class ManageCurrencyController extends BaseRestController {
	private CurrencyService currencyService;
	public final CurrencyService getCurrencyService() {
		return currencyService;
	}

	public final RateService getRateService() {
		return rateService;
	}

	private RateService rateService;

	@Inject
	public void setCurrencyService(final CurrencyService currencyService) {
		this.currencyService = currencyService;
	}

	@Inject
	public void setRateService(final RateService rateService) {
		this.rateService = rateService;
	}

	public static class ManageCurrencyRequestDTO {
		private long currencyId;
                

		public long getCurrencyId() {
			return currencyId;
		}

		public void setCurrencyId(final long currencyId) {
			this.currencyId = currencyId;
		}
	}

   

        
	public static class ManageCurrencyResponseDTO {
		private Currency currency;
		private boolean ratesEnabled;
		private Calendar pendingRateInitProgression;

        public Currency getCurrency() {
            return currency;
        }

        public void setCurrency(Currency currency) {
            this.currency = currency;
        }

        public boolean isRatesEnabled() {
            return ratesEnabled;
        }

        public void setRatesEnabled(boolean ratesEnabled) {
            this.ratesEnabled = ratesEnabled;
        }

        public Calendar getPendingRateInitProgression() {
            return pendingRateInitProgression;
        }

        public void setPendingRateInitProgression(Calendar pendingRateInitProgression) {
            this.pendingRateInitProgression = pendingRateInitProgression;
        }

		public ManageCurrencyResponseDTO(Currency currency, boolean ratesEnabled, Calendar pendingRateInitProgression) {
			super();
			this.currency = currency;
			this.ratesEnabled = ratesEnabled;
			this.pendingRateInitProgression = pendingRateInitProgression;
		}

      public ManageCurrencyResponseDTO(){
      }

	}

	@RequestMapping(value = "admin/manageCurrency/{currencyId}", method = RequestMethod.GET)
	@ResponseBody
	protected ManageCurrencyResponseDTO executeAction(@PathVariable ("currencyId")long currencyId) throws Exception {

		ManageCurrencyResponseDTO response = new ManageCurrencyResponseDTO();
			         System.out.println("ManageCurrency is running........");	
                try{
		final long id = currencyId;
		final boolean isInsert = id <= 0L;
		Currency currency;
		if (isInsert) {
			currency = new Currency();
		} else {
			currency = currencyService.load(id);
		}
		// request.setAttribute("currency", currency);
		final boolean ratesEnabled = rateService.isAnyRateEnabled(currency, null);
		final Calendar pendingRateInitProgression = rateService.checkPendingRateInitializations(currency);
		
		response = new ManageCurrencyResponseDTO(currency, ratesEnabled, pendingRateInitProgression);
                }
                catch(Exception e){
                    e.printStackTrace();
                }
		return response;
	}

}
