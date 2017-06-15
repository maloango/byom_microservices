package nl.strohalm.cyclos.webservices.rest.ads.imports;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import nl.strohalm.cyclos.annotations.Inject;
import nl.strohalm.cyclos.entities.ads.imports.AdImport;
import nl.strohalm.cyclos.entities.ads.imports.ImportedAdCategory;
import nl.strohalm.cyclos.services.ads.AdImportService;
import nl.strohalm.cyclos.utils.EntityHelper;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;

@Controller
public class ImportedAdCategoriesController extends BaseRestController {

	private AdImportService adImportService;

	@Inject
	public void setAdImportService(final AdImportService adImportService) {
		this.adImportService = adImportService;
	}

	public static class ImportedAdCategoriesRequestDto {
		public long getImportId() {
			try {
				return Long.parseLong("" + getQuery("adImport"));
			} catch (final Exception e) {
				return 0L;
			}
		}

		protected Map<String, Object> values;

		public Map<String, Object> getValues() {
			return values;
		}

		public void setValues(final Map<String, Object> values) {
			this.values = values;
		}

		public Map<String, Object> getQuery() {
			return values;
		}

		public Object getQuery(final String key) {
			return values.get(key);
		}

		public void setQuery(final Map<String, Object> query) {
			values = query;
		}

		public void setQuery(final String key, final Object value) {
			values.put(key, value);
		}

		public String getStatus() {
			return (String) getQuery("status");
		}

		public void setImportId(final long id) {
			setQuery("adImport", "" + id);
		}

		public void setStatus(final String status) {
			setQuery("status", status);
		}
	}

	public static class ImportedAdCategoriesResponseDto {
		private List<ImportedAdCategory> categories;

		public ImportedAdCategoriesResponseDto(
				List<ImportedAdCategory> categories) {
			super();
			this.categories = categories;
		}

	}

	@RequestMapping(value = "admin/importedAdCategories", method = RequestMethod.POST)
	@ResponseBody
	protected ImportedAdCategoriesResponseDto executeAction(
			@RequestBody ImportedAdCategoriesRequestDto form) throws Exception {
		// final ImportedAdCategoriesForm form = context.getForm();
		final AdImport adImport = EntityHelper.reference(AdImport.class,
				form.getImportId());
		// final HttpServletRequest request = context.getRequest();

		List<ImportedAdCategory> categories = adImportService
				.getNewCategories(adImport);
		ImportedAdCategoriesResponseDto response = new ImportedAdCategoriesResponseDto(
				categories);
		return response;
	}
}
