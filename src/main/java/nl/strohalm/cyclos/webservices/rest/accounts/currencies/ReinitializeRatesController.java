package nl.strohalm.cyclos.webservices.rest.accounts.currencies;

import java.util.Calendar;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts.action.ActionForward;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import nl.strohalm.cyclos.annotations.Inject;
import nl.strohalm.cyclos.controls.ActionContext;
import nl.strohalm.cyclos.controls.accounts.currencies.ReinitializeRatesForm;
import nl.strohalm.cyclos.entities.accounts.Currency;
import nl.strohalm.cyclos.entities.settings.LocalSettings;
import nl.strohalm.cyclos.entities.settings.events.LocalSettingsEvent;
import nl.strohalm.cyclos.services.accounts.CurrencyService;
import nl.strohalm.cyclos.services.accounts.rates.RateService;
import nl.strohalm.cyclos.services.accounts.rates.RateService.RateType;
import nl.strohalm.cyclos.services.accounts.rates.ReinitializeRatesDTO;
import nl.strohalm.cyclos.services.accounts.rates.WhatRate;
import nl.strohalm.cyclos.services.settings.SettingsService;
import nl.strohalm.cyclos.utils.binding.BeanBinder;
import nl.strohalm.cyclos.utils.binding.DataBinder;
import nl.strohalm.cyclos.utils.binding.PropertyBinder;
import nl.strohalm.cyclos.utils.conversion.IdConverter;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;
import org.springframework.stereotype.Controller;
@Controller
public class ReinitializeRatesController extends BaseRestController{
	private CurrencyService currencyService;
	private RateService rateService;
	private DataBinder<ReinitializeRatesDTO> dataBinder;
	private SettingsService settingsService;

	public final SettingsService getSettingsService() {
		return settingsService;
	}

	public final void setSettingsService(SettingsService settingsService) {
		this.settingsService = settingsService;
	}

	public final CurrencyService getCurrencyService() {
		return currencyService;
	}

	public final RateService getRateService() {
		return rateService;
	}

	public void onLocalSettingsUpdate(final LocalSettingsEvent event) {
		dataBinder = null;
	}

	@Inject
	public void setCurrencyService(final CurrencyService currencyService) {
		this.currencyService = currencyService;
	}

	@Inject
	public void setRateService(final RateService rateService) {
		this.rateService = rateService;
	}

	protected ActionForward handleDisplay(final ActionContext context)
			throws Exception {
		final HttpServletRequest request = context.getRequest();
		final ReinitializeRatesForm form = context.getForm();
		final long id = form.getCurrencyId();
		final Currency currency = currencyService.load(id);
		request.setAttribute("enabledARate", currency.isEnableARate());
		request.setAttribute("enabledDRate", currency.isEnableDRate());
		request.setAttribute("enabledIRate", currency.isEnableIRate());
		request.setAttribute("enableDateA",
				rateService.getEnableDate(currency, RateType.A_RATE));
		request.setAttribute("enableDateD",
				rateService.getEnableDate(currency, RateType.D_RATE));
		request.setAttribute("enableDateI",
				rateService.getEnableDate(currency, RateType.I_RATE));
		request.setAttribute("currency", currency);
		final ReinitializeRatesDTO dto = new ReinitializeRatesDTO();
		dto.setCurrencyId(id);
		dto.setMaintainPastSettings(true);
		getDataBinder().writeAsString(form.getReinitializeRatesDto(), dto);
		return new ActionForward(
				"/pages/accounts/currencies/reinitializeRates.jsp");
	}

	public static class ReinitializeRatesRequestDto {
		protected Map<String, Object> values;
		private long currencyId;
		private boolean doAInit;
		private boolean doDInit;
		private boolean doIInit;
                

		public long getCurrencyId() {
			return currencyId;
		}

		public Map<String, Object> getReinitializeRatesDto() {
			return values;
		}

		public Object getReinitializeRatesDto(final String key) {
			return values.get(key);
		}

		public WhatRate getWhatRate() {
			final WhatRate whatRate = new WhatRate();
			whatRate.setaRate(doAInit);
			whatRate.setdRate(doDInit);
			whatRate.setiRate(doIInit);
			return whatRate;
		}

		public boolean isDoAInit() {
			return doAInit;
		}

		public boolean isDoDInit() {
			return doDInit;
		}

		public boolean isDoIInit() {
			return doIInit;
		}

		public void setCurrencyId(final long currencyId) {
			this.currencyId = currencyId;
		}

		public void setDoAInit(final boolean doAInit) {
			this.doAInit = doAInit;
		}

		public void setDoDInit(final boolean doDInit) {
			this.doDInit = doDInit;
		}

		public void setDoIInit(final boolean doIInit) {
			this.doIInit = doIInit;
		}

		public void setReinitializeRatesDto(final Map<String, Object> map) {
			values = map;
		}

		public void setReinitializeRatesDto(final String key, final Object value) {
			values.put(key, value);
		}

		public Map<String, Object> getValues() {
			return values;
		}

		public void setValues(final Map<String, Object> values) {
			this.values = values;
		}
	}

	public static class ReinitializeRatesResponseDto {
		private boolean isEnableARate;
                private boolean isEnableDRate;
                private boolean isEnableIRate;
                private boolean enableDateA;
                private boolean enableDateD;
                private boolean enableDateI;
                private Currency currency;

        public boolean isEnableDateD() {
            return enableDateD;
        }

        public void setEnableDateD(boolean enableDateD) {
            this.enableDateD = enableDateD;
        }

        public boolean isEnableDateI() {
            return enableDateI;
        }

        public void setEnableDateI(boolean enableDateI) {
            this.enableDateI = enableDateI;
        }

        public Currency getCurrency() {
            return currency;
        }

        public void setCurrency(Currency currency) {
            this.currency = currency;
        }
                
                

        public boolean isEnableDateA() {
            return enableDateA;
        }

        public void setEnableDateA(boolean enableDateA) {
            this.enableDateA = enableDateA;
        }
                

        

        public boolean isIsEnableARate() {
            return isEnableARate;
        }

        public void setIsEnableARate(boolean isEnableARate) {
            this.isEnableARate = isEnableARate;
        }

        public boolean isIsEnableDRate() {
            return isEnableDRate;
        }

        public void setIsEnableDRate(boolean isEnableDRate) {
            this.isEnableDRate = isEnableDRate;
        }

        public boolean isIsEnableIRate() {
            return isEnableIRate;
        }

        public void setIsEnableIRate(boolean isEnableIRate) {
            this.isEnableIRate = isEnableIRate;
        }
                
                public ReinitializeRatesResponseDto(){
                }
                
                

	}

	@RequestMapping(value = "admin/reinitializeRates", method = RequestMethod.POST)
	@ResponseBody
	protected ReinitializeRatesResponseDto handleSubmit(
			@RequestBody ReinitializeRatesRequestDto form) throws Exception {
                        ReinitializeRatesResponseDto response = new ReinitializeRatesResponseDto();
                        
		try{
		final ReinitializeRatesDTO reinitDto = getDataBinder().readFromString(
				form.getReinitializeRatesDto());
		reinitDto.setWhatRate(form.getWhatRate());
		
		rateService.reinitializeRate(reinitDto);
		long currencyId = reinitDto.getCurrencyId();
		response = new ReinitializeRatesResponseDto();}
                catch(Exception e){
                    e.printStackTrace();
                }
		return response;
	}

	protected void validateForm(final ActionContext context) {
		final ReinitializeRatesForm form = context.getForm();
		final ReinitializeRatesDTO reinitDto = getDataBinder().readFromString(
				form.getReinitializeRatesDto());
		reinitDto.setWhatRate(form.getWhatRate());
		rateService.validate(reinitDto);
	}

	private DataBinder<ReinitializeRatesDTO> getDataBinder() {
		if (dataBinder == null) {
			final LocalSettings localSettings = settingsService
					.getLocalSettings();
			final BeanBinder<ReinitializeRatesDTO> binder = BeanBinder
					.instance(ReinitializeRatesDTO.class);
			binder.registerBinder("currencyId", PropertyBinder.instance(
					Long.class, "currencyId", IdConverter.instance()));
			binder.registerBinder("reinitSince", PropertyBinder.instance(
					Calendar.class, "reinitSince",
					localSettings.getDateConverter()));
			binder.registerBinder("maintainPastSettings", PropertyBinder
					.instance(boolean.class, "maintainPastSettings"));
			dataBinder = binder;
		}
		return dataBinder;
	}

}
