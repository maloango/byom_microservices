/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.strohalm.cyclos.webservices.rest.members.preferences;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import nl.strohalm.cyclos.annotations.Inject;
import nl.strohalm.cyclos.entities.accounts.AccountType;
import nl.strohalm.cyclos.entities.accounts.Currency;
import nl.strohalm.cyclos.entities.accounts.MemberAccountType;
import nl.strohalm.cyclos.entities.ads.Ad;
import nl.strohalm.cyclos.entities.ads.Ad.TradeType;
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
import nl.strohalm.cyclos.utils.access.LoggedUser;
import nl.strohalm.cyclos.utils.binding.BeanBinder;
import nl.strohalm.cyclos.utils.binding.DataBinder;
import nl.strohalm.cyclos.utils.binding.PropertyBinder;
import nl.strohalm.cyclos.utils.conversion.IdConverter;
import nl.strohalm.cyclos.utils.conversion.ReferenceConverter;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;
import nl.strohalm.cyclos.webservices.rest.GenericResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 *
 * @author Lue
 */
@Controller
public class EditAdInterestController extends BaseRestController {

    private AdInterestService adInterestService;
    private AdCategoryService adCategoryService;
    private GroupFilterService groupFilterService;
    private CurrencyService currencyService;
    private AccountTypeService accountTypeService;
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

    public static class EditAdInterestRequest {

        private long id;
        private long memberId;
        private AdCategory category;
        private String title;
        private Member owner;
        private Currency currency;
        private BigDecimal initialPrice;
        private BigDecimal finalPrice;
        private String keywords;
        private String type;

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }
        
       
        
        public long getMemberId() {
            return memberId;
        }

        public void setMemberId(long memberId) {
            this.memberId = memberId;
        }
        
       
        public AdCategory getCategory() {
            return category;
        }

        public void setCategory(AdCategory category) {
            this.category = category;
        }

        public Member getOwner() {
            return owner;
        }

        public void setOwner(Member owner) {
            this.owner = owner;
        }

        public Currency getCurrency() {
            return currency;
        }

        public void setCurrency(Currency currency) {
            this.currency = currency;
        }

        public long getId() {
            return id;
        }

        public void setId(long id) {
            this.id = id;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public BigDecimal getInitialPrice() {
            return initialPrice;
        }

        public void setInitialPrice(BigDecimal initialPrice) {
            this.initialPrice = initialPrice;
        }

        public BigDecimal getFinalPrice() {
            return finalPrice;
        }

        public void setFinalPrice(BigDecimal finalPrice) {
            this.finalPrice = finalPrice;
        }

        public String getKeywords() {
            return keywords;
        }

        public void setKeywords(String keywords) {
            this.keywords = keywords;
        }

    }

    public static class EditAdInterestResponse extends GenericResponse {

        private Member owner;
        private String title;
        private Ad.TradeType type;
        private List<AdCategory> category = new ArrayList<AdCategory>();
        private List<GroupFilter> groupFilters;
        private BigDecimal initialPrice;
        private BigDecimal finalPrice;
        private List<Currency> currencies;
        private String keywords;
        private AdInterest adInterest;
        private List<TradeType> tradeTypes = new ArrayList();
        private int singleCurrency;

        public int getSingleCurrency() {
            return singleCurrency;
        }

        public void setSingleCurrency(int singleCurrency) {
            this.singleCurrency = singleCurrency;
        }

        public List<Currency> getCurrencies() {
            return currencies;
        }

        public void setCurrencies(List<Currency> currencies) {
            this.currencies = currencies;
        }

        public List<GroupFilter> getGroupFilters() {
            return groupFilters;
        }

        public void setGroupFilters(List<GroupFilter> groupFilters) {
            this.groupFilters = groupFilters;
        }

        public List<AdCategory> getCategory() {
            return category;
        }

        public void setCategory(List<AdCategory> category) {
            this.category = category;
        }

        public List<TradeType> getTradeTypes() {
            return tradeTypes;
        }

        public void setTradeTypes(List<TradeType> tradeTypes) {
            this.tradeTypes = tradeTypes;
        }

        public AdInterest getAdInterest() {
            return adInterest;
        }

        public void setAdInterest(AdInterest adInterest) {
            this.adInterest = adInterest;
        }

        public BigDecimal getFinalPrice() {
            return finalPrice;
        }

        public BigDecimal getInitialPrice() {
            return initialPrice;
        }

        public String getKeywords() {
            return keywords;
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

        public void setFinalPrice(final BigDecimal finalPrice) {
            this.finalPrice = finalPrice;
        }

        public void setInitialPrice(final BigDecimal initialPrice) {
            this.initialPrice = initialPrice;
        }

        public void setKeywords(final String keywords) {
            this.keywords = keywords;
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

    }

    @RequestMapping(value = "member/editAdInterest", method = RequestMethod.POST)
    @ResponseBody
    public GenericResponse editAd(@RequestBody EditAdInterestRequest request) throws Exception {
        GenericResponse response = new GenericResponse();
        final AdInterest adInterest = resolveAdInterest();
        //final AdInterest adInterest = new AdInterest();
        adInterest.setId(request.getId());
        adInterest.setCategory(request.getCategory());
        adInterest.setCurrency(request.getCurrency());
        adInterest.setFinalPrice(request.getFinalPrice());
        adInterest.setInitialPrice(request.getInitialPrice());
        adInterest.setTitle(request.getTitle());
        adInterest.setOwner(request.getOwner());
        adInterest.setType(Ad.TradeType.valueOf(request.getType()));
        adInterest.setKeywords(request.getKeywords());

        final boolean isInsert = adInterest.isTransient();
        adInterestService.save(adInterest);
        response.setMessage(isInsert ? "adInterest.inserted" : "adInterest.modified");
        response.setStatus(0);

        return response;
    }

    @RequestMapping(value = "member/editAdInterest", method = RequestMethod.GET)
    @ResponseBody
    public EditAdInterestResponse prepareForm() throws Exception {
        // Send ad interest to JSP
        EditAdInterestResponse response = new EditAdInterestResponse();
//        AdInterest adInterest = resolveAdInterest(context);
        AdInterest adInterest = new AdInterest();
        final Long id = adInterest.getId();
        if (id != null) {
            adInterest = adInterestService.load(adInterest.getId(), RelationshipHelper.nested(AdInterest.Relationships.MEMBER, Element.Relationships.USER));
        }
        // getDataBinder().writeAsString(form.getAdInterest(), adInterest);
//        request.setAttribute("adInterest", adInterest);
        //  response.setAdInterest(adInterest);

        // Send trade types to JSP
        //    RequestHelper.storeEnum(request, Ad.TradeType.class, "tradeTypes");
        List<TradeType> tradeTypes = new ArrayList();
        tradeTypes.add(TradeType.OFFER);
        tradeTypes.add(TradeType.SEARCH);
        response.setTradeTypes(tradeTypes);

        // Send categories to JSP
        // response.setAttribute("adCategories", adCategoryService.listLeaf());
        //  response.setCategory(adCategoryService.listLeaf());
        // Send group filters to JSP
        final MemberGroup memberGroup = LoggedUser.group();
        final GroupFilterQuery groupFilterQuery = new GroupFilterQuery();
        groupFilterQuery.setViewableBy(memberGroup);
        final List<GroupFilter> groupFilters = groupFilterService.search(groupFilterQuery);
        if (groupFilters.size() > 0) {
            //  response.setGroupFilters(groupFilters);
        }

        // Send currencies to JSP
        final List<Currency> currencies = currencyService.listByMemberGroup(memberGroup);
        response.setCurrencies(currencies);
        if (currencies.size() == 1) {
            // Set a single currency variable when there's only one option
            response.setSingleCurrency(0);
        } else if (currencies.size() > 1 && adInterest.getCurrency() == null) {
            // When there's multiple currencies, pre select the one of the default account
            final MemberAccountType defaultAccountType = accountTypeService.getDefault(memberGroup, AccountType.Relationships.CURRENCY);
            if (defaultAccountType != null) {
                //form.setAdInterest("currency", CoercionHelper.coerce(String.class, defaultAccountType.getCurrency()));
            }
        }
        response.setFinalPrice(BigDecimal.ONE);
        response.setInitialPrice(BigDecimal.ONE);
        response.setSingleCurrency(0);
        response.setTradeTypes(tradeTypes);
        
        response.setAdInterest(adInterest);
        response.setCurrencies(currencies);
        //   response.setGroupFilters(groupFilters);
        response.setKeywords(response.getKeywords());
        response.setOwner(response.getOwner());

        response.setStatus(0);
        response.setMessage("!! Ad is modified....");
        return response;

    }

    // @Override
    public void validateForm() {
        final AdInterest adInterest = resolveAdInterest();
        adInterestService.validate(adInterest);
    }

    public AdInterest resolveAdInterest() {
        // final EditAdInterestForm form = context.getForm();
        final AdInterest adInterest = getDataBinder().readFromString(LoggedUser.accountOwner());
        System.out.println("....account Owner:" +LoggedUser.accountOwner());
        if (adInterest.getOwner() == null && LoggedUser.isMember()) {
            adInterest.setOwner((Member) LoggedUser.element());
        }
        if (adInterest.getType() == null) {
            adInterest.setType(Ad.TradeType.OFFER);
        }
        return adInterest;
    }
}
