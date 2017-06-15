package nl.strohalm.cyclos.webservices.rest.ads.imports;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import nl.strohalm.cyclos.annotations.Inject;
import nl.strohalm.cyclos.entities.ads.imports.AdImport;
import nl.strohalm.cyclos.services.accounts.CurrencyService;
import nl.strohalm.cyclos.services.ads.AdImportService;
import nl.strohalm.cyclos.utils.validation.ValidationException;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;

@Controller
public class ImportedAdsSummaryController extends BaseRestController {
	private AdImportService adImportService;
	private CurrencyService currencyService;

	@Inject
	public void setAdImportService(final AdImportService adImportService) {
		this.adImportService = adImportService;
	}

	@Inject
	public void setCurrencyService(final CurrencyService currencyService) {
		this.currencyService = currencyService;
	}

	public static class ImportedAdsSummaryRequestDto {
		private long importId;

		public long getImportId() {
			return importId;
		}

		public void setImportId(final long importId) {
			this.importId = importId;
		}
	}

	public static class ImportedAdsSummaryResponseDto {
		private String message;

		public String getMessage() {
			return message;
		}

		public void setMessage(String message) {
			this.message = message;
		}
	}

	@RequestMapping(value = "admin/importedAdsSummary", method = RequestMethod.GET)
	@ResponseBody
	protected ImportedAdsSummaryResponseDto formAction(
			@RequestBody ImportedAdsSummaryRequestDto context) throws Exception {
		final AdImport adImport = getImport(context);
		adImportService.processImport(adImport);
		String message = "adImport.processed";
		ImportedAdsSummaryResponseDto response = new ImportedAdsSummaryResponseDto();
		response.setMessage(message);
		return response;
	}

/*	protected void prepareForm(final ImportedAdsSummaryRequestDto context) throws Exception {
		final AdImport adImport = getImport(context);
		final HttpServletRequest request = context.getRequest();
		request.setAttribute("adImport", adImport);
		request.setAttribute("summary", adImportService.getSummary(adImport));

		// We need to know if there's a single currency. In this case, the
		// currency won't be shown
		final List<Currency> currencies = currencyService.listAll();
		if (currencies.size() == 1) {
			request.setAttribute("singleCurrency", currencies.get(0));
		}
	}
*/
	private AdImport getImport(final ImportedAdsSummaryRequestDto form) {
		// final ImportedAdsSummaryForm form = context.getForm();
		try {
			return adImportService.load(form.getImportId(),
					AdImport.Relationships.CURRENCY);
		} catch (final Exception e) {
			throw new ValidationException();
		}
	}
}
