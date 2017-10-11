/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.strohalm.cyclos.webservices.rest.members.preferences;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import nl.strohalm.cyclos.annotations.Inject;
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
import nl.strohalm.cyclos.utils.access.LoggedUser;
import nl.strohalm.cyclos.utils.binding.BeanBinder;
import nl.strohalm.cyclos.utils.binding.DataBinder;
import nl.strohalm.cyclos.utils.binding.PropertyBinder;
import nl.strohalm.cyclos.utils.conversion.CoercionHelper;
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

    public void setSettingsService(SettingsService settingsService) {
        this.settingsService = settingsService;
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

    public static class EditAdInterestResponse extends GenericResponse {

        private long id;
        private long memberId;
        private Map<String, Object> values = new HashMap<String, Object>();
        private List<GroupFilter> groupFilters;
        private List<Currency> currencies;
        private int singleCurrency;
        private AdInterest adInterest;
        private List<AdCategory> adCategorie;
        private List<Ad.TradeType> tradeTypes = new ArrayList();

        public List<Ad.TradeType> getTradeTypes() {
            return tradeTypes;
        }

        public void setTradeTypes(List<Ad.TradeType> tradeTypes) {
            this.tradeTypes = tradeTypes;
        }
        
        public Map<String, Object> getValues() {
            return values;
        }

        public void setValues(Map<String, Object> values) {
            this.values = values;
        }

        public List<AdCategory> getAdCategorie() {
            return adCategorie;
        }

        public void setAdCategorie(List<AdCategory> adCategorie) {
            this.adCategorie = adCategorie;
        }

        public void setAdInterest(AdInterest adInterest) {
            this.adInterest = adInterest;
        }

        public int getSingleCurrency() {
            return singleCurrency;
        }

        public void setSingleCurrency(final int singleCurrency) {
            this.singleCurrency = singleCurrency;
        }

        public List<GroupFilter> getGroupFilters() {
            return groupFilters;
        }

        public void setGroupFilters(List<GroupFilter> groupFilters) {
            this.groupFilters = groupFilters;
        }

        public List<Currency> getCurrencies() {
            return currencies;
        }

        public void setCurrencies(List<Currency> currencies) {
            this.currencies = currencies;
        }

        public Map<String, Object> getAdInterest() {
            return values;
        }

        public Object getAdInterest(final String key) {
            return values.get(key);
        }

        public long getId() {
            return id;
        }

        public long getMemberId() {
            return memberId;
        }

        public void setAdInterest(final Map<String, Object> map) {
            values = map;
        }

        public void setAdInterest(final String key, final Object value) {
            values.put(key, value);
        }

        public void setId(final long memberId) {
            id = memberId;
        }

        public void setMemberId(final long memberId) {
            this.memberId = memberId;
        }

    }

    @RequestMapping(value = "member/editAdInterest", method = RequestMethod.POST)
    @ResponseBody
    public GenericResponse editAdInterest(@RequestBody EditAdInterestResponse request) throws Exception {
        GenericResponse response = new GenericResponse();
        final AdInterest adInterest = resolveAdInterest(request);
        final boolean isInsert = adInterest.isTransient();
        adInterestService.save(adInterest);
        response.setStatus(0);
        response.setMessage(isInsert ? "adInterest.inserted" : "adInterest.modified");
        return response;

    }

    @RequestMapping(value = "member/editAdInterest", method = RequestMethod.GET)
    @ResponseBody
    public EditAdInterestResponse prepareForm(EditAdInterestResponse request) throws Exception {
        EditAdInterestResponse response = new EditAdInterestResponse();
        // Send ad interest to JSP
        AdInterest adInterest = resolveAdInterest(request);
        final Long id = adInterest.getId();
        if (id != null) {
            adInterest = adInterestService.load(adInterest.getId(), RelationshipHelper.nested(AdInterest.Relationships.MEMBER, Element.Relationships.USER));
        }
        getDataBinder().writeAsString(request.getAdInterest(), adInterest);
        response.setAdInterest(adInterest);

        // Send trade types to JSP
         // RequestHelper.storeEnum(request, Ad.TradeType.class, "tradeTypes");
        List<Ad.TradeType> tradeTypes = new ArrayList();
        tradeTypes.add(Ad.TradeType.OFFER);
        tradeTypes.add(Ad.TradeType.SEARCH);
        response.setTradeTypes(tradeTypes);

        // Send categories to JSP
        response.setAdCategorie(adCategoryService.listLeaf());

        // Send group filters to JSP
        final MemberGroup memberGroup = LoggedUser.group();
        final GroupFilterQuery groupFilterQuery = new GroupFilterQuery();
        groupFilterQuery.setViewableBy(memberGroup);
        final List<GroupFilter> groupFilters = groupFilterService.search(groupFilterQuery);
        if (groupFilters.size() > 0) {
            response.setGroupFilters(groupFilters);
        }

        // Send currencies to JSP
        final List<Currency> currencies = currencyService.listByMemberGroup(memberGroup);
        response.setCurrencies(currencies);
        if (currencies.size() == 1) {
            // Set a single currency variable when there's only one option
            //  response.setSingleCurrency(currencies.get(0));
            response.setSingleCurrency(0);

        } else if (currencies.size() > 1 && adInterest.getCurrency() == null) {
            // When there's multiple currencies, pre select the one of the default account
            final MemberAccountType defaultAccountType = accountTypeService.getDefault(memberGroup, AccountType.Relationships.CURRENCY);
            if (defaultAccountType != null) {
                request.setAdInterest("currency", CoercionHelper.coerce(String.class, defaultAccountType.getCurrency()));
            }
        }
        return response;

    }

    protected void validateForm(final EditAdInterestResponse request) {
        final AdInterest adInterest = resolveAdInterest(request);
        adInterestService.validate(adInterest);
    }

    private AdInterest resolveAdInterest(final EditAdInterestResponse request) {

        final AdInterest adInterest = getDataBinder().readFromString(request.getAdInterest());
        if (adInterest.getOwner() == null && LoggedUser.isMember()) {
            adInterest.setOwner((Member) LoggedUser.element());
        }
        if (adInterest.getType() == null) {
            adInterest.setType(Ad.TradeType.OFFER);
        }
        return adInterest;
    }
}
