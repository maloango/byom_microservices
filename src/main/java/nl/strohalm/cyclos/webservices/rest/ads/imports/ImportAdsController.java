package nl.strohalm.cyclos.webservices.rest.ads.imports;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts.action.ActionForward;
import org.apache.struts.upload.FormFile;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import nl.strohalm.cyclos.annotations.Inject;
import nl.strohalm.cyclos.controls.ActionContext;
import nl.strohalm.cyclos.controls.ads.imports.ImportAdsForm;
import nl.strohalm.cyclos.entities.accounts.Currency;
import nl.strohalm.cyclos.entities.ads.imports.AdImport;
import nl.strohalm.cyclos.services.accounts.CurrencyService;
import nl.strohalm.cyclos.services.ads.AdImportService;
import nl.strohalm.cyclos.utils.ActionHelper;
import nl.strohalm.cyclos.utils.binding.BeanBinder;
import nl.strohalm.cyclos.utils.binding.DataBinder;
import nl.strohalm.cyclos.utils.binding.PropertyBinder;
import nl.strohalm.cyclos.utils.csv.UnknownColumnException;
import nl.strohalm.cyclos.utils.validation.RequiredError;
import nl.strohalm.cyclos.utils.validation.ValidationException;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;

@Controller
public class ImportAdsController extends BaseRestController {
	private AdImportService adImportService;
	private CurrencyService currencyService;
	private DataBinder<AdImport> dataBinder;

	public DataBinder<AdImport> getDataBinder() {
		if (dataBinder == null) {
			final BeanBinder<AdImport> binder = BeanBinder
					.instance(AdImport.class);
			binder.registerBinder("currency",
					PropertyBinder.instance(Currency.class, "currency"));
			dataBinder = binder;
		}
		return dataBinder;
	}

	@Inject
	public void setAdImportService(final AdImportService adImportService) {
		this.adImportService = adImportService;
	}

	@Inject
	public void setCurrencyService(final CurrencyService currencyService) {
		this.currencyService = currencyService;
	}

	public static class ImportAdsRequestDto {
		private FormFile upload;
		protected Map<String, Object> values;

		public Map<String, Object> getValues() {
			return values;
		}

		public void setValues(final Map<String, Object> values) {
			this.values = values;
		}

		public Map<String, Object> getImport() {
			return values;
		}

		public Object getImport(final String property) {
			return values.get(property);
		}

		public FormFile getUpload() {
			return upload;
		}

		public void setImport(final Map<String, Object> values) {
			this.values = values;
		}

		public void setImport(final String property, final Object value) {
			values.put(property, value);
		}

		public void setUpload(final FormFile upload) {
			this.upload = upload;
		}

	}

	public static class ImportAdsResponseDto {
		private String message;
		long importId;
		
		public long getImportId() {
			return importId;
		}

		public void setImportId(long importId) {
			this.importId = importId;
		}

		public String getMessage() {
			return message;
		}

		public void setMessage(String message) {
			this.message = message;
		}
	}

	@RequestMapping(value = "", method = RequestMethod.GET)
	@ResponseBody
	protected ImportAdsResponseDto handleSubmit(
			@RequestBody ImportAdsRequestDto form) throws Exception {
		//final ImportAdsForm form = context.getForm();
		final FormFile upload = form.getUpload();
		if (upload == null || upload.getFileSize() == 0) {
			throw new ValidationException("upload", "adImport.file",
					new RequiredError());
		}
		AdImport adImport = getDataBinder().readFromString(form.getImport());
		ImportAdsResponseDto response = new ImportAdsResponseDto();
		try {
			adImport = adImportService.importAds(adImport,
					upload.getInputStream());
			long importId =adImport.getId();
			response.setImportId(importId);
			
		} catch (final UnknownColumnException e) {
			String err = "general.error.csv.unknownColumn";
			response.setMessage(err);
		} finally {
			upload.destroy();
		}
		return response;
	}

	protected void prepareForm(final ActionContext context) throws Exception {
		final HttpServletRequest request = context.getRequest();
		final List<Currency> currencies = currencyService.listAll();
		if (currencies.size() == 1) {
			request.setAttribute("singleCurrency", currencies.get(0));
		}
		request.setAttribute("currencies", currencies);
	}

	protected void validateForm(final ActionContext context) {
		final ImportAdsForm form = context.getForm();
		final AdImport adImport = getDataBinder().readFromString(
				form.getImport());
		adImportService.validate(adImport);
	}

}
