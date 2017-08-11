package nl.strohalm.cyclos.webservices.rest.accounts.currencies;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import nl.strohalm.cyclos.annotations.Inject;
import nl.strohalm.cyclos.services.accounts.CurrencyService;
import nl.strohalm.cyclos.utils.validation.ValidationException;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class RemoveCurrencyController extends BaseRestController {
	public final CurrencyService getCurrencyService() {
		return currencyService;
	}

	private CurrencyService currencyService;

	@Inject
	public void setCurrencyService(final CurrencyService currencyService) {
		this.currencyService = currencyService;
	}

	public static class RemoveCurrencyRequestDTO {
		private long currencyId;

		public long getCurrencyId() {
			return currencyId;
		}

		public void setCurrencyId(final long currencyId) {
			this.currencyId = currencyId;
		}
	}

	public static class RemoveCurrencyResponseDTO {
		public String message;

		public String getMessage() {
			return message;
		}

		public void setMessage(String message) {
			this.message = message;
		}
                public RemoveCurrencyResponseDTO(){}

	}

	@RequestMapping(value = "admin/removeCurrency/{currencyId}", method = RequestMethod.GET)
	@ResponseBody
	protected RemoveCurrencyResponseDTO executeAction(@PathVariable ("currencyId")long currencyId) throws Exception {
		final long id =currencyId;
		if (id <= 0L) {
			throw new ValidationException();
		}
		RemoveCurrencyResponseDTO response = new RemoveCurrencyResponseDTO();
		try {
			currencyService.remove(id);
			response.setMessage("currency.removed");
		} catch (final Exception e) {
			response.setMessage("currency.error.removing");
		}
               
		return response;
	}
}
