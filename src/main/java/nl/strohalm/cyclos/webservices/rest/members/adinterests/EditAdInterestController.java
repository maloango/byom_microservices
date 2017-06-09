package nl.strohalm.cyclos.webservices.rest.members.adinterests;

import java.math.BigDecimal;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import nl.strohalm.cyclos.annotations.Inject;
import nl.strohalm.cyclos.controls.ActionContext;
import nl.strohalm.cyclos.controls.members.adinterests.EditAdInterestForm;
import nl.strohalm.cyclos.controls.restapi.BaseRestController;
import nl.strohalm.cyclos.entities.accounts.AccountType;
import nl.strohalm.cyclos.entities.accounts.Currency;
import nl.strohalm.cyclos.entities.accounts.MemberAccountType;
import nl.strohalm.cyclos.entities.ads.Ad;
import nl.strohalm.cyclos.entities.ads.AdCategory;
import nl.strohalm.cyclos.entities.groups.GroupFilter;
import nl.strohalm.cyclos.entities.groups.GroupFilterQuery;
import nl.strohalm.cyclos.entities.groups.MemberGroup;
import nl.strohalm.cyclos.entities.members.Element;
import nl.strohalm.cyclos.entities.members.Member;
import nl.strohalm.cyclos.entities.members.adInterests.AdInterest;
import nl.strohalm.cyclos.entities.settings.LocalSettings;
import nl.strohalm.cyclos.entities.settings.events.LocalSettingsEvent;
import nl.strohalm.cyclos.services.accounts.AccountTypeService;
import nl.strohalm.cyclos.services.accounts.CurrencyService;
import nl.strohalm.cyclos.services.ads.AdCategoryService;
import nl.strohalm.cyclos.services.elements.AdInterestService;
import nl.strohalm.cyclos.services.groups.GroupFilterService;
import nl.strohalm.cyclos.services.settings.SettingsService;
import nl.strohalm.cyclos.utils.RelationshipHelper;
import nl.strohalm.cyclos.utils.RequestHelper;
import nl.strohalm.cyclos.utils.binding.BeanBinder;
import nl.strohalm.cyclos.utils.binding.DataBinder;
import nl.strohalm.cyclos.utils.binding.PropertyBinder;
import nl.strohalm.cyclos.utils.conversion.CoercionHelper;
import nl.strohalm.cyclos.utils.conversion.IdConverter;
import nl.strohalm.cyclos.utils.conversion.ReferenceConverter;
@Controller
public class EditAdInterestController extends BaseRestController{
	private AdInterestService      adInterestService;
    private AdCategoryService      adCategoryService;
    private GroupFilterService     groupFilterService;
    private CurrencyService        currencyService;
    private AccountTypeService     accountTypeService;
    private DataBinder<AdInterest> dataBinder;
    private SettingsService settingsService;

    public DataBinder<AdInterest> getDataBinder() {
        if (dataBinder == null) {
            final LocalSettings settings = settingsService.getLocalSettings();
            final BeanBinder<AdInterest> binder = BeanBinder.instance(AdInterest.class);
            binder.registerBinder("id", PropertyBinder.instance(Long.class, "id", IdConverter.instance()));
            binder.registerBinder("owner", PropertyBinder.instance(Member.class, "owner", ReferenceConverter.instance(Member.class)));
            binder.registerBinder("title", PropertyBinder.instance(String.class, "title"));
            binder.registerBinder("type", PropertyBinder.instance(Ad.TradeType.class, "type"));
            binder.registerBinder("category", PropertyBinder.instance(AdCategory.class, "category", ReferenceConverter.instance(AdCategory.class)));
            binder.registerBinder("member", PropertyBinder.instance(Member.class, "member", ReferenceConverter.instance(Member.class)));
            binder.registerBinder("groupFilter", PropertyBinder.instance(GroupFilter.class, "groupFilter", ReferenceConverter.instance(GroupFilter.class)));
            binder.registerBinder("initialPrice", PropertyBinder.instance(BigDecimal.class, "initialPrice", settings.getNumberConverter()));
            binder.registerBinder("finalPrice", PropertyBinder.instance(BigDecimal.class, "finalPrice", settings.getNumberConverter()));
            binder.registerBinder("currency", PropertyBinder.instance(Currency.class, "currency"));
            binder.registerBinder("keywords", PropertyBinder.instance(String.class, "keywords"));
            dataBinder = binder;
        }
        return dataBinder;
    }

    //@Override
    public void onLocalSettingsUpdate(final LocalSettingsEvent event) {
        dataBinder = null;
    }

    @Inject
    public void setAccountTypeService(final AccountTypeService accountTypeService) {
        this.accountTypeService = accountTypeService;
    }

    @Inject
    public void setAdCategoryService(final AdCategoryService adCategoryService) {
        this.adCategoryService = adCategoryService;
    }

    @Inject
    public void setAdInterestService(final AdInterestService adInterestService) {
        this.adInterestService = adInterestService;
    }

    @Inject
    public void setCurrencyService(final CurrencyService currencyService) {
        this.currencyService = currencyService;
    }

    @Inject
    public void setGroupFilterService(final GroupFilterService groupFilterService) {
        this.groupFilterService = groupFilterService;
    }

    
    public static class EditAdInterestRequestDTO{
    	 private Member            owner;
    	    private String            title;
    	    private Ad.TradeType      type;
    	    private AdCategory        category;
    	    private Member            member;
    	    private GroupFilter       groupFilter;
    	    private BigDecimal        initialPrice;
    	    private BigDecimal        finalPrice;
    	    private Currency          currency;
    	    private String            keywords;

    	    public AdCategory getCategory() {
    	        return category;
    	    }

    	    public Currency getCurrency() {
    	        return currency;
    	    }

    	    public BigDecimal getFinalPrice() {
    	        return finalPrice;
    	    }

    	    public GroupFilter getGroupFilter() {
    	        return groupFilter;
    	    }

    	    public BigDecimal getInitialPrice() {
    	        return initialPrice;
    	    }

    	    public String getKeywords() {
    	        return keywords;
    	    }

    	    public Member getMember() {
    	        return member;
    	    }

    	    public Member getOwner() {
    	        return owner;
    	    }

    	    public String getTitle() {
    	        return title;
    	    }

    	    public Ad.TradeType getType() {
    	        return type;
    	    }

    	    public void setCategory(final AdCategory category) {
    	        this.category = category;
    	    }

    	    public void setCurrency(final Currency currency) {
    	        this.currency = currency;
    	    }

    	    public void setFinalPrice(final BigDecimal finalPrice) {
    	        this.finalPrice = finalPrice;
    	    }

    	    public void setGroupFilter(final GroupFilter groupFilter) {
    	        this.groupFilter = groupFilter;
    	    }

    	    public void setInitialPrice(final BigDecimal initialPrice) {
    	        this.initialPrice = initialPrice;
    	    }

    	    public void setKeywords(final String keywords) {
    	        this.keywords = keywords;
    	    }

    	    public void setMember(final Member member) {
    	        this.member = member;
    	    }

    	    public void setOwner(final Member owner) {
    	        this.owner = owner;
    	    }

    	    public void setTitle(final String title) {
    	        this.title = title;
    	    }

    	    public void setType(final Ad.TradeType type) {
    	        this.type = type;
    	    }

    	    @Override
    	    public String toString() {
    	        return title != null ? title : "";
    	    }
    	
    }
    
    public static class EditAdInterestResponseDTO{
    	String message;

		public final String getMessage() {
			return message;
		}

		public final void setMessage(String message) {
			this.message = message;
		}
    	
    }
    
    @RequestMapping(value = "/member/editAdInterest", method = RequestMethod.PUT)
    @ResponseBody
    protected EditAdInterestResponseDTO formAction(@RequestBody EditAdInterestRequestDTO form) throws Exception {
        final AdInterest adInterest = resolveAdInterest(null);
        final boolean isInsert = adInterest.isTransient();
        adInterestService.save(adInterest);
        EditAdInterestResponseDTO response = new EditAdInterestResponseDTO();
        String message = null;
        if (isInsert) {
        	message = "adInterest.inserted";
			
		}
        else{
        	message = "adInterest.modified";
        }
        //context.sendMessage(isInsert ? "adInterest.inserted" : "adInterest.modified");
        return response;
    }
   

    //@Override
    protected void prepareForm(final ActionContext context) throws Exception {
        final HttpServletRequest request = context.getRequest();

        // Send ad interest to JSP
        final EditAdInterestForm form = context.getForm();
        AdInterest adInterest = resolveAdInterest(context);
        final Long id = adInterest.getId();
        if (id != null) {
            adInterest = adInterestService.load(adInterest.getId(), RelationshipHelper.nested(AdInterest.Relationships.MEMBER, Element.Relationships.USER));
        }
        getDataBinder().writeAsString(form.getAdInterest(), adInterest);
        request.setAttribute("adInterest", adInterest);

        // Send trade types to JSP
        RequestHelper.storeEnum(request, Ad.TradeType.class, "tradeTypes");

        // Send categories to JSP
        request.setAttribute("adCategories", adCategoryService.listLeaf());

        // Send group filters to JSP
        final MemberGroup memberGroup = context.getGroup();
        final GroupFilterQuery groupFilterQuery = new GroupFilterQuery();
        groupFilterQuery.setViewableBy(memberGroup);
        final List<GroupFilter> groupFilters = groupFilterService.search(groupFilterQuery);
        if (groupFilters.size() > 0) {
            request.setAttribute("groupFilters", groupFilters);
        }

        // Send currencies to JSP
        final List<Currency> currencies = currencyService.listByMemberGroup(memberGroup);
        request.setAttribute("currencies", currencies);
        if (currencies.size() == 1) {
            // Set a single currency variable when there's only one option
            request.setAttribute("singleCurrency", currencies.get(0));
        } else if (currencies.size() > 1 && adInterest.getCurrency() == null) {
            // When there's multiple currencies, pre select the one of the default account
            final MemberAccountType defaultAccountType = accountTypeService.getDefault(memberGroup, AccountType.Relationships.CURRENCY);
            if (defaultAccountType != null) {
                form.setAdInterest("currency", CoercionHelper.coerce(String.class, defaultAccountType.getCurrency()));
            }
        }

    }

   // @Override
    protected void validateForm(final ActionContext context) {
        final AdInterest adInterest = resolveAdInterest(context);
        adInterestService.validate(adInterest);
    }

    private AdInterest resolveAdInterest(final ActionContext context) {
        final EditAdInterestForm form = context.getForm();
        final AdInterest adInterest = getDataBinder().readFromString(form.getAdInterest());
        if (adInterest.getOwner() == null && context.isMember()) {
            adInterest.setOwner((Member) context.getElement());
        }
        if (adInterest.getType() == null) {
            adInterest.setType(Ad.TradeType.OFFER);
        }
        return adInterest;
    }

}
