package nl.strohalm.cyclos.webservices.rest.ads.imports;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import nl.strohalm.cyclos.annotations.Inject;
import nl.strohalm.cyclos.controls.ActionContext;
//import nl.strohalm.cyclos.controls.ads.imports.ImportedAdsDetailsForm;
import nl.strohalm.cyclos.entities.ads.imports.AdImport;
import nl.strohalm.cyclos.entities.ads.imports.ImportedAd;
import nl.strohalm.cyclos.entities.ads.imports.ImportedAdQuery;
import nl.strohalm.cyclos.services.ads.AdImportService;
import nl.strohalm.cyclos.utils.binding.BeanBinder;
import nl.strohalm.cyclos.utils.binding.DataBinder;
import nl.strohalm.cyclos.utils.binding.DataBinderHelper;
import nl.strohalm.cyclos.utils.binding.PropertyBinder;
import nl.strohalm.cyclos.utils.query.QueryParameters;
import nl.strohalm.cyclos.utils.validation.ValidationException;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;

@Controller
public class ImportedAdsDetailsController extends BaseRestController {
	private AdImportService adImportService;
	public final AdImportService getAdImportService() {
		return adImportService;
	}

	private DataBinder<ImportedAdQuery> dataBinder;

	public DataBinder<ImportedAdQuery> getDataBinder() {
		if (dataBinder == null) {
			final BeanBinder<ImportedAdQuery> binder = BeanBinder
					.instance(ImportedAdQuery.class);
			binder.registerBinder("adImport",
					PropertyBinder.instance(AdImport.class, "adImport"));
			binder.registerBinder("status", PropertyBinder.instance(
					ImportedAdQuery.Status.class, "status"));
			binder.registerBinder("lineNumber",
					PropertyBinder.instance(Integer.class, "lineNumber"));
			binder.registerBinder("pageParameters",
					DataBinderHelper.pageBinder());
			return binder;
		}
		return dataBinder;
	}

	@Inject
	public void setAdImportService(final AdImportService adImportService) {
		this.adImportService = adImportService;
	}

	public static class ImportedAdsDetailsRequestDto {

	}

	public static class ImportedAdsDetailsResponseDto {
		private List<ImportedAd> ads;

		public ImportedAdsDetailsResponseDto(List<ImportedAd> ads) {
			super();
			this.ads = ads;
		}

	}

	@RequestMapping(value = "admin/importedAdsDetails", method = RequestMethod.GET)
	@ResponseBody
	protected ImportedAdsDetailsResponseDto executeQuery(
			final ImportedAdsDetailsRequestDto context,
			final QueryParameters queryParameters) {
		ImportedAdsDetailsResponseDto response = null;
                try{
		final ImportedAdQuery query = (ImportedAdQuery) queryParameters;
		final List<ImportedAd> ads = adImportService.searchImportedAds(query);
		response = new ImportedAdsDetailsResponseDto(ads);}
                catch(Exception e){
                    e.printStackTrace();
                        }
				
		return response;
	}

//	protected QueryParameters prepareForm(final ActionContext context) {
//		final HttpServletRequest request = context.getRequest();
//		final ImportedAdsDetailsForm form = context.getForm();
//		final ImportedAdQuery query = getDataBinder().readFromString(
//				form.getQuery());
//		final AdImport adImport = adImportService.load(query.getAdImport()
//				.getId(), AdImport.Relationships.CURRENCY);
//		if (adImport == null || query.getStatus() == null) {
//			throw new ValidationException();
//		}
//		query.setAdImport(adImport);
//		request.setAttribute("lowercaseStatus", query.getStatus().name()
//				.toLowerCase());
//		return query;
//	}

    /**
     *
     * @param context
     * @param queryParameters
     * @return
     * @throws Exception
     */
    protected boolean willExecuteQuery(final ActionContext context,
			final QueryParameters queryParameters) throws Exception {
		return true;
	}
}
