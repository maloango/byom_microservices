package nl.strohalm.cyclos.webservices.rest.accounts.currencies;

import java.math.BigDecimal;
import java.util.Calendar;

import javax.servlet.http.HttpServletRequest;

import nl.strohalm.cyclos.access.AdminSystemPermission;
import nl.strohalm.cyclos.annotations.Inject;
import nl.strohalm.cyclos.controls.ActionContext;
import nl.strohalm.cyclos.controls.accounts.currencies.EditCurrencyForm;
import nl.strohalm.cyclos.entities.accounts.ARateParameters;
import nl.strohalm.cyclos.entities.accounts.Currency;
import nl.strohalm.cyclos.entities.accounts.DRateParameters;
import nl.strohalm.cyclos.entities.accounts.IRateParameters;
import nl.strohalm.cyclos.entities.settings.LocalSettings;
import nl.strohalm.cyclos.entities.settings.events.LocalSettingsChangeListener;
import nl.strohalm.cyclos.entities.settings.events.LocalSettingsEvent;
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
import nl.strohalm.cyclos.webservices.rest.accounts.accounttypes.RemoveAccountTypeController.RemoveAccountTypeRequestDto;
import nl.strohalm.cyclos.webservices.rest.accounts.accounttypes.RemoveAccountTypeController.RemoveAccountTypeResponseDto;

import org.apache.struts.action.ActionForward;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class EditCurrencyController extends BaseRestController implements
		LocalSettingsChangeListener {
	private CurrencyService currencyService;
	private RateService rateService;
	private DataBinder<Currency> dataBinder;
	private PermissionService permissionService;
	private SettingsService settingsService;

	@Override
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

	public static class EditCurrencyRequestDto {
		private long currencyId;
		private boolean enableARate;
		private boolean enableDRate;
		private boolean enableIRate;

		public long getCurrencyId() {
			return currencyId;
		}

		public void setCurrencyId(long currencyId) {
			this.currencyId = currencyId;
		}

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

	public static class EditCurrencyResponseDto {
		public String message;

		public String getMessage() {
			return message;
		}

		public void setMessage(String message) {
			this.message = message;
		}
	}

	@RequestMapping(value = "/admin/editCurrency", method = RequestMethod.POST)
	@ResponseBody
	protected EditCurrencyResponseDto handleSubmit(
			@RequestBody EditCurrencyForm form) throws Exception {
		Currency currency = getDataBinder().readFromString(form.getCurrency());
		final boolean isInsert = currency.isTransient();
		final WhatRate whatRate = new WhatRate();
		whatRate.setaRate(form.isEnableARate());
		whatRate.setdRate(form.isEnableDRate());
		whatRate.setiRate(form.isEnableIRate());
		currency = currencyService.save(currency, whatRate);
		EditCurrencyResponseDto response = new EditCurrencyResponseDto();
		if (isInsert) {
			response.setMessage("currency.inserted");
		} else {
			response.setMessage("currency.modified");
		}
		return response;
	}

	private DataBinder<Currency> getDataBinder() {
		if (dataBinder == null) {
			final LocalSettings localSettings = settingsService
					.getLocalSettings();

			final BeanBinder<Currency> binder = BeanBinder
					.instance(Currency.class);
			binder.registerBinder(
					"id",
					PropertyBinder.instance(Long.class, "id",
							IdConverter.instance()));
			binder.registerBinder("name",
					PropertyBinder.instance(String.class, "name"));
			binder.registerBinder("symbol",
					PropertyBinder.instance(String.class, "symbol"));
			binder.registerBinder("pattern",
					PropertyBinder.instance(String.class, "pattern"));
			binder.registerBinder("description",
					PropertyBinder.instance(String.class, "description"));

			final BeanBinder<ARateParameters> aRate = BeanBinder.instance(
					ARateParameters.class, "aRateParameters");
			aRate.registerBinder(
					"id",
					PropertyBinder.instance(Long.class, "id",
							IdConverter.instance()));
			aRate.registerBinder("initValue", PropertyBinder.instance(
					BigDecimal.class, "initValue",
					localSettings.getNumberConverter()));
			aRate.registerBinder("initDate", PropertyBinder.instance(
					Calendar.class, "initDate",
					localSettings.getDateTimeConverter()));
			aRate.registerBinder("creationValue", PropertyBinder.instance(
					BigDecimal.class, "creationValue",
					localSettings.getNumberConverter()));
			binder.registerBinder("aRateParameters", aRate);

			final BeanBinder<DRateParameters> dRate = BeanBinder.instance(
					DRateParameters.class, "dRateParameters");
			dRate.registerBinder(
					"id",
					PropertyBinder.instance(Long.class, "id",
							IdConverter.instance()));
			dRate.registerBinder("interest", PropertyBinder.instance(
					BigDecimal.class, "interest",
					localSettings.getHighPrecisionConverter()));
			dRate.registerBinder("baseMalus", PropertyBinder.instance(
					BigDecimal.class, "baseMalus",
					localSettings.getHighPrecisionConverter()));
			dRate.registerBinder("minimalD", PropertyBinder.instance(
					BigDecimal.class, "minimalD",
					localSettings.getNumberConverter()));
			dRate.registerBinder("initValue", PropertyBinder.instance(
					BigDecimal.class, "initValue",
					localSettings.getNumberConverter()));
			dRate.registerBinder("initDate", PropertyBinder.instance(
					Calendar.class, "initDate",
					localSettings.getDateTimeConverter()));
			dRate.registerBinder("creationValue", PropertyBinder.instance(
					BigDecimal.class, "creationValue",
					localSettings.getNumberConverter()));
			binder.registerBinder("dRateParameters", dRate);

			final BeanBinder<IRateParameters> iRate = BeanBinder.instance(
					IRateParameters.class, "iRateParameters");
			iRate.registerBinder(
					"id",
					PropertyBinder.instance(Long.class, "id",
							IdConverter.instance()));
			binder.registerBinder("iRateParameters", iRate);

			dataBinder = binder;
		}
		return dataBinder;
	}

}
