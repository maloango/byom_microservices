package nl.strohalm.cyclos.webservices.rest.currencies;

import java.util.Calendar;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import nl.strohalm.cyclos.annotations.Inject;
import nl.strohalm.cyclos.entities.accounts.Currency;
import nl.strohalm.cyclos.services.accounts.CurrencyService;
import nl.strohalm.cyclos.services.accounts.rates.RateService;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;

@Controller
public class ManageCurrencyController extends BaseRestController {
	private CurrencyService currencyService;
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
		public Map<String, Object> message;

		public Map<String, Object> getMessage() {
			return message;
		}

		public void setMessage(Map<String, Object> message) {
			this.message = message;
		}

		public void setMessage(String string, boolean ratesEnabled) {
			// TODO Auto-generated method stub

		}

		public void setMessage(String string, Currency currency) {
			// TODO Auto-generated method stub

		}

		public void setAttribute(String string,
				Calendar pendingRateInitProgression) {
			// TODO Auto-generated method stub

		}

	}

	@RequestMapping(value = "", method = RequestMethod.PUT)
	@ResponseBody
	protected ManageCurrencyResponseDTO executeAction(
			@RequestBody ManageCurrencyRequestDTO form) throws Exception {

		// final ManageCurrencyForm manageForm = form.getForm();
		final long id = form.getCurrencyId();
		final boolean isInsert = id <= 0L;
		Currency currency;
		if (isInsert) {
			currency = new Currency();
		} else {
			currency = currencyService.load(id);
		}
		ManageCurrencyResponseDTO response = new ManageCurrencyResponseDTO();
		response.setMessage("currency", currency);
		final boolean ratesEnabled = rateService.isAnyRateEnabled(currency,
				null);
		response.setMessage("ratesEnabled", ratesEnabled);
		// get the progress on any pending rate initialization
		final Calendar pendingRateInitProgression = rateService
				.checkPendingRateInitializations(currency);
		response.setAttribute("pendingRateInit", pendingRateInitProgression);
		return response;
	}

}
