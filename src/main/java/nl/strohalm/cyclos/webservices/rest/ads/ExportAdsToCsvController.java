package nl.strohalm.cyclos.webservices.rest.ads;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.lucene.analysis.Analyzer;
import org.apache.struts.action.ActionMapping;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import nl.strohalm.cyclos.annotations.Inject;
import nl.strohalm.cyclos.controls.AbstractActionContext;
import nl.strohalm.cyclos.controls.ActionContext;
import nl.strohalm.cyclos.controls.BaseQueryForm;
import nl.strohalm.cyclos.entities.access.User;
import nl.strohalm.cyclos.entities.ads.Ad;
import nl.strohalm.cyclos.entities.ads.Ad.TradeType;
import nl.strohalm.cyclos.entities.ads.AdCategory;
import nl.strohalm.cyclos.entities.ads.FullTextAdQuery;
import nl.strohalm.cyclos.entities.customization.fields.AdCustomField;
import nl.strohalm.cyclos.entities.members.Element;
import nl.strohalm.cyclos.entities.settings.LocalSettings;
import nl.strohalm.cyclos.entities.settings.events.LocalSettingsEvent;
import nl.strohalm.cyclos.services.ads.AdService;
import nl.strohalm.cyclos.services.customization.AdCustomFieldService;
import nl.strohalm.cyclos.services.elements.ElementService;
import nl.strohalm.cyclos.services.settings.SettingsService;
import nl.strohalm.cyclos.utils.MessageHelper;
import nl.strohalm.cyclos.utils.Navigation;
import nl.strohalm.cyclos.utils.RelationshipHelper;
import nl.strohalm.cyclos.utils.binding.DataBinder;
import nl.strohalm.cyclos.utils.binding.MapBean;
import nl.strohalm.cyclos.utils.conversion.CustomFieldConverter;
import nl.strohalm.cyclos.utils.csv.CSVWriter;
import nl.strohalm.cyclos.utils.query.QueryParameters.ResultType;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;
@Controller
public class ExportAdsToCsvController extends BaseRestController{
	
	 private AdService                   adService;
	    private AdCustomFieldService        adCustomFieldService;
	    private SettingsService settingsService;
	    private ElementService elementService;
	    private MessageHelper messageHelper;
	    private DataBinder<FullTextAdQuery> dataBinder;
	    private RelationshipHelper relationshipHelper;
		private BaseQueryForm form;

	    public final SettingsService getSettingsService() {
			return settingsService;
		}

		public final void setSettingsService(SettingsService settingsService) {
			this.settingsService = settingsService;
		}

		public final ElementService getElementService() {
			return elementService;
		}

		public final void setElementService(ElementService elementService) {
			this.elementService = elementService;
		}

		public final MessageHelper getMessageHelper() {
			return messageHelper;
		}

		public final void setMessageHelper(MessageHelper messageHelper) {
			this.messageHelper = messageHelper;
		}

		public final RelationshipHelper getRelationshipHelper() {
			return relationshipHelper;
		}

		public final void setRelationshipHelper(RelationshipHelper relationshipHelper) {
			this.relationshipHelper = relationshipHelper;
		}

		public AdCustomFieldService getAdCustomFieldService() {
	        return adCustomFieldService;
	    }

	    public AdService getAdService() {
	        return adService;
	    }

	    public DataBinder<FullTextAdQuery> getDataBinder() {
	        if (dataBinder == null) {
	            dataBinder = SearchAdsAction.adFullTextQueryDataBinder(settingsService.getLocalSettings());
	        }
	        return dataBinder;
	    }

	    //@Override
	    public void onLocalSettingsUpdate(final LocalSettingsEvent event) {
	        //super.onLocalSettingsUpdate(event);
	        dataBinder = null;
	    }

	    @Inject
	    public void setAdCustomFieldService(final AdCustomFieldService adCustomFieldService) {
	        this.adCustomFieldService = adCustomFieldService;
	    }

	    @Inject
	    public void setAdService(final AdService adService) {
	        this.adService = adService;
	    }
	    public static class ExportAdsToCsvRequestDTO{
	    	 private boolean           advanced;
	    	    private boolean           forceShowFields;
	    	    private boolean           categoryOnly;
	    	    private boolean           lastAds;
	    	    private boolean           alreadySearched;
	    	    private Analyzer          analyzer;
	    	    private Map<String,Object>values;
	    	    public void BaseQueryForm() {
	    	        reset(null, null);
	    	    }

	    	    public Map<String, Object> getQuery() {
	    	        return values;
	    	    }

	    	    public Object getQuery(final String key) {
	    	        return values.get(key);
	    	    }

	    	    //@Override
	    	   /* public void reset(final ActionMapping mapping, final HttpServletRequest request) {
	    	        super.reset(mapping, request);
	    	        setQuery("pageParameters", new MapBean("currentPage"));
	    	    }*/

	    	    public void setQuery(final Map<String, Object> query) {
	    	        values = query;
	    	    }

	    	    public void setQuery(final String key, final Object value) {
	    	        values.put(key, value);
	    	    }

	    	    public Analyzer getAnalyzer() {
	    	        return analyzer;
	    	    }

	    	    public void setAnalyzer(final Analyzer analyzer) {
	    	        this.analyzer = analyzer;
	    	    }

	    	    public void SearchAdsForm() {
	    	        clearForm();
	    	    }

	    	    public void clearForm() {
	    	    	
	    	        getQuery().clear();
	    	        setQuery("since", new MapBean("number", "period"));
	    	        setQuery("adValues", new MapBean(true, "field", "value"));
	    	        setQuery("memberValues", new MapBean(true, "field", "value"));
	    	        setQuery("tradeType", TradeType.OFFER.name());
	    	        setQuery("status", Ad.Status.ACTIVE.name());
	    	        setQuery("groups", Collections.emptyList());
	    	        setQuery("groupFilters", Collections.emptyList());
	    	    }

	    	    public boolean isAdvanced() {
	    	        return advanced;
	    	    }

	    	    public boolean isAlreadySearched() {
	    	        return alreadySearched;
	    	    }

	    	    public boolean isCategoryOnly() {
	    	        return categoryOnly;
	    	    }

	    	    public boolean isForceShowFields() {
	    	        return forceShowFields;
	    	    }

	    	    public boolean isLastAds() {
	    	        return lastAds;
	    	    }

	    	
	    	    public void  reset(final ActionMapping mapping, final HttpServletRequest request) {
	    	       // super.reset(mapping, request);
	    	        advanced = false;
	    	        setQuery("withImagesOnly", "false");
	    	        setCategoryOnly(false);
	    	        setLastAds(false);
	    	        setForceShowFields(false);
	    	        setAdvanced(false);

	    	        // Clear the group filters on each request only if not in a nested path
	    	        if (request != null) {
	    	            final Navigation navigation = Navigation.get(request.getSession());
	    	            if (navigation == null || navigation.size() == 1) {
	    	                setQuery("groupFilters", Collections.emptyList());
	    	            }
	    	        }
	    	    }

	    	    public void setAdvanced(final boolean advanced) {
	    	        this.advanced = advanced;
	    	    }

	    	    public void setAlreadySearched(final boolean alreadySearched) {
	    	        this.alreadySearched = alreadySearched;
	    	    }

	    	    public void setCategoryOnly(final boolean categoryOnly) {
	    	        this.categoryOnly = categoryOnly;
	    	    }

	    	    public void setForceShowFields(final boolean forceShowFields) {
	    	        this.forceShowFields = forceShowFields;
	    	    }

	    	    public void setLastAds(final boolean lastAds) {
	    	        this.lastAds = lastAds;
	    	    }
	    }
	    public static class ExportAdsToCsvResponseDTO{
	    	String message;
	    	List<Ad> fullTextSearch(FullTextAdQuery query) {
				return null;
			}

			public final String getMessage() {
				return message;
			}

			public final void setMessage(String message) {
				this.message = message;
			}
	    }

	    @RequestMapping(value = "/admin/exportAdsToCsv",method = RequestMethod.GET)
	    @ResponseBody
	    protected ExportAdsToCsvResponseDTO executeQuery(@RequestBody ExportAdsToCsvRequestDTO context) {
	        
	        form = null;
			final FullTextAdQuery query = getDataBinder().readFromString(form.getQuery());
	        query.setResultType(ResultType.ITERATOR);
	        query.fetch(RelationshipHelper.nested(Ad.Relationships.CATEGORY, RelationshipHelper.nested(AdCategory.MAX_LEVEL, AdCategory.Relationships.PARENT)), Ad.Relationships.CURRENCY, Ad.Relationships.CUSTOM_VALUES, RelationshipHelper.nested(Ad.Relationships.OWNER, Element.Relationships.USER));
	        return (ExportAdsToCsvResponseDTO) adService.fullTextSearch(query);
	        
	        
	    }

	   // @Override
	    protected String fileName(final ActionContext context) {
	        final User loggedUser = context.getUser();
	        return "ads_" + loggedUser.getUsername() + ".csv";
	    }

	    //@Override
	    @SuppressWarnings({ "unchecked", "rawtypes" })
	    protected CSVWriter resolveCSVWriter(final ActionContext context) {
	        final LocalSettings settings = settingsService.getLocalSettings();
	        final CSVWriter<Ad> csv = CSVWriter.instance(Ad.class, settings);
	        csv.addColumn(context.message("ad.id"), "id");
	        csv.addColumn(context.message("ad.tradeType"), "tradeType", messageHelper.getMessageConverter(getServlet().getServletContext(), "ad.tradeType."));
	        csv.addColumn(context.message("ad.title"), "title");
	        csv.addColumn(context.message("ad.category"), "category.fullName");
	        csv.addColumn(context.message("ad.price"), "price", settings.getNumberConverter());
	        csv.addColumn(context.message("accountType.currency"), "currency.symbol");
	        csv.addColumn(context.message("ad.permanent"), "permanent");
	        csv.addColumn(context.message("ad.publicationPeriod.begin"), "publicationPeriod.begin", settings.getRawDateConverter());
	        csv.addColumn(context.message("ad.publicationPeriod.end"), "publicationPeriod.end", settings.getRawDateConverter());
	        csv.addColumn(context.message("member.username"), "owner.username");
	        csv.addColumn(context.message("ad.owner"), "owner.name");
	        csv.addColumn(context.message("ad.externalPublication"), "externalPublication");
	        csv.addColumn(context.message("ad.description"), "description");
	        final List<AdCustomField> customFields = adCustomFieldService.list();
	        for (final AdCustomField field : customFields) {
	            csv.addColumn(field.getName(), "customValues", new CustomFieldConverter(field, elementService, settings));
	        }
	        return csv;
	    }

		private AbstractActionContext getServlet() {
			return (this.getServlet());
			
		}

}
