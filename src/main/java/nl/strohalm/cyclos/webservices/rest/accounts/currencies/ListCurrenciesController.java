package nl.strohalm.cyclos.webservices.rest.accounts.currencies;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import nl.strohalm.cyclos.access.AdminSystemPermission;
import nl.strohalm.cyclos.annotations.Inject;
import nl.strohalm.cyclos.entities.accounts.Currency;
import nl.strohalm.cyclos.services.accounts.CurrencyService;
import nl.strohalm.cyclos.services.permissions.PermissionService;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;

@Controller
public class ListCurrenciesController extends BaseRestController {
	private CurrencyService currencyService;
	private PermissionService permissionService;

	@Inject
	public void setCurrencyService(final CurrencyService currencyService) {
		this.currencyService = currencyService;
	}

	public static class ListCurrenciesRequestDTO {

	}

	public static class ListCurrenciesResponseDTO {
		List<Currency> currencies;
		Boolean editable;

		public ListCurrenciesResponseDTO(List<Currency> currencies, Boolean editable) {
			super();
			this.currencies = currencies;
			this.editable = editable;
		}

	}

	@RequestMapping(value = "admin/listCurrencies", method = RequestMethod.GET)
	@ResponseBody
	protected ListCurrenciesResponseDTO executeAction(@RequestBody ListCurrenciesRequestDTO form) throws Exception {
		// final HttpServletRequest request = context.getRequest();
		final List<Currency> currencies = currencyService.listAll();
		Boolean editable = permissionService.hasPermission(AdminSystemPermission.CURRENCIES_MANAGE);
		ListCurrenciesResponseDTO response = new ListCurrenciesResponseDTO(currencies, editable);
		return response;
	}

}
