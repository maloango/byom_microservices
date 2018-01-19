package nl.strohalm.cyclos.webservices.rest.ads;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import nl.strohalm.cyclos.entities.accounts.Currency;
import nl.strohalm.cyclos.entities.ads.AbstractAdQuery;
import nl.strohalm.cyclos.entities.ads.Ad;
import nl.strohalm.cyclos.entities.ads.AdCategory;
import nl.strohalm.cyclos.entities.ads.AdCategoryWithCounterQuery;
import nl.strohalm.cyclos.entities.ads.AdCategoryWithCounterVO;
import nl.strohalm.cyclos.entities.ads.AdQuery;
import nl.strohalm.cyclos.entities.ads.FullTextAdQuery;
import nl.strohalm.cyclos.entities.groups.AdminGroup;
import nl.strohalm.cyclos.entities.groups.Group;
import nl.strohalm.cyclos.entities.groups.GroupFilter;
import nl.strohalm.cyclos.entities.groups.GroupFilterQuery;
import nl.strohalm.cyclos.entities.groups.MemberGroup;
import nl.strohalm.cyclos.entities.members.Element;
import nl.strohalm.cyclos.entities.members.Member;
import nl.strohalm.cyclos.utils.RelationshipHelper;
import nl.strohalm.cyclos.utils.RequestHelper;
import nl.strohalm.cyclos.utils.TimePeriod;
import nl.strohalm.cyclos.utils.access.LoggedUser;
import nl.strohalm.cyclos.utils.conversion.CoercionHelper;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;
import nl.strohalm.cyclos.webservices.rest.GenericResponse;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class SearchAdsController extends BaseRestController {

    public static class AdsResponse extends GenericResponse {

        private List<AdCategoryWithCounterVO> categories;
        private List<AdCategory> AdCategories;
        private List<CurrencyEntity> currencyList;
        private List<TimePeriod.Field> sincePeriod;
        private List<MemberGroupEntity> memberGroupList;
        private List<GroupFilterEntity> groupFilterList;
        private List<Ad.Status> adStatus;

        public List<Ad.Status> getAdStatus() {
            return adStatus;
        }

        public void setAdStatus(List<Ad.Status> adStatus) {
            this.adStatus = adStatus;
        }

        public List<GroupFilterEntity> getGroupFilterList() {
            return groupFilterList;
        }

        public void setGroupFilterList(List<GroupFilterEntity> groupFilterList) {
            this.groupFilterList = groupFilterList;
        }

        public List<MemberGroupEntity> getMemberGroupList() {
            return memberGroupList;
        }

        public void setMemberGroupList(List<MemberGroupEntity> memberGroupList) {
            this.memberGroupList = memberGroupList;
        }

        public List<TimePeriod.Field> getSincePeriod() {
            return sincePeriod;
        }

        public void setSincePeriod(List<TimePeriod.Field> sincePeriod) {
            this.sincePeriod = sincePeriod;
        }

        public List<CurrencyEntity> getCurrencyList() {
            return currencyList;
        }

        public void setCurrencyList(List<CurrencyEntity> currencyList) {
            this.currencyList = currencyList;
        }

        public List<AdCategory> getAdCategories() {
            return AdCategories;
        }

        public void setAdCategories(List<AdCategory> AdCategories) {
            this.AdCategories = AdCategories;
        }

        public List<AdCategoryWithCounterVO> getCategories() {
            return categories;
        }

        public void setCategories(List<AdCategoryWithCounterVO> categories) {
            this.categories = categories;
        }

    }

    public static class CurrencyEntity {

        private Long id;
        private String symbol;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getSymbol() {
            return symbol;
        }

        public void setSymbol(String symbol) {
            this.symbol = symbol;
        }

    }

    public static class MemberGroupEntity {

        private Long id;
        private String name;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

    }

    public static class GroupFilterEntity {

        private Long id;
        private String name;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

    }

    @RequestMapping(value = "admin/searchAds", method = RequestMethod.GET)
    @ResponseBody
    public AdsResponse executeQuery() {

        AdsResponse response = new AdsResponse();

        // Store the categories
        final Ad.TradeType tradeType = Ad.TradeType.OFFER;
        int rootCategoryCount;

        if (settingsService.getLocalSettings().isShowCountersInAdCategories()) {
            final AdCategoryWithCounterQuery counterQuery = new AdCategoryWithCounterQuery();
            counterQuery.setTradeType(tradeType);
            final List<AdCategoryWithCounterVO> categories = adService.getCategoriesWithCounters(counterQuery);
            rootCategoryCount = categories.size();
            //request.setAttribute("categories", categories);
            //request.setAttribute("showCounters", true);
            response.setCategories(categories);
            System.out.println("-----adcat: " + categories);
        } else {
            final List<AdCategory> categories = adCategoryService.listRoot();
            System.out.println("add----" + categories.get(0).getChildren());
            rootCategoryCount = categories.size();
            response.setAdCategories(categories);
            //request.setAttribute("categories", categories);

        }

        // Retrieve the currencies
        List<Currency> currencies;
        List<CurrencyEntity> currencyList = new ArrayList();
        if (LoggedUser.isAdministrator()) {
            currencies = currencyService.listAll();
            for (Currency currency : currencies) {
                CurrencyEntity entity = new CurrencyEntity();
                entity.setId(currency.getId());
                entity.setSymbol(currency.getSymbol());
                currencyList.add(entity);
            }
        } else {
            final Member member = (Member) LoggedUser.accountOwner();
            currencies = currencyService.listByMemberGroup(member.getMemberGroup());
        }
        if (currencies.size() > 1) {
            response.setCurrencyList(currencyList);
        }

        // Retrieve the periods for "published since"
        response.setSincePeriod(Arrays.asList(TimePeriod.Field.DAYS, TimePeriod.Field.WEEKS, TimePeriod.Field.MONTHS));

        // Admins can search by groups
        final GroupFilterQuery groupFilterQuery = new GroupFilterQuery();
        AdminGroup adminGroup = LoggedUser.group();
        adminGroup = groupService.load(adminGroup.getId(), AdminGroup.Relationships.MANAGES_GROUPS);
        final Collection<MemberGroup> memberGroups = adminGroup.getManagesGroups();
        List<MemberGroupEntity> memberGroupList = new ArrayList();
        for (MemberGroup group : memberGroups) {
            MemberGroupEntity entity = new MemberGroupEntity();
            entity.setId(group.getId());
            entity.setName(group.getName());
            memberGroupList.add(entity);
        }
        if (CollectionUtils.isNotEmpty(memberGroups)) {
            response.setMemberGroupList(memberGroupList);
        }
        groupFilterQuery.setAdminGroup(adminGroup);
        final Collection<GroupFilter> groupFilters = groupFilterService.search(groupFilterQuery);
        List<GroupFilterEntity> groupFilterList = new ArrayList();
        for (GroupFilter filter : groupFilters) {
            GroupFilterEntity entity = new GroupFilterEntity();
            entity.setId(filter.getId());
            entity.setName(filter.getName());
            groupFilterList.add(entity);
        }
        response.setGroupFilterList(groupFilterList);
        //set status
        response.setAdStatus(Arrays.asList(Ad.Status.ACTIVE, Ad.Status.EXPIRED, Ad.Status.PERMANENT, Ad.Status.SCHEDULED));
        response.setStatus(0);
        return response;

    }

    public static class SearchAdsParameters {

        private String tradeType;
        private String keywords;
        private boolean withImagesOnly;
        private String adStatus;
        private List<Long> groupFilters;
        private List<Long> groups;
        private BigDecimal initialPrice;
        private BigDecimal finalPrice;
        private Long currency;
        private int since_number;
        private String since_field;
        private Long category;

        public Long getCategory() {
            return category;
        }

        public void setCategory(Long category) {
            this.category = category;
        }

        public String getTradeType() {
            return tradeType;
        }

        public void setTradeType(String tradeType) {
            this.tradeType = tradeType;
        }

        public String getKeywords() {
            return keywords;
        }

        public void setKeywords(String keywords) {
            this.keywords = keywords;
        }

        public boolean isWithImagesOnly() {
            return withImagesOnly;
        }

        public void setWithImagesOnly(boolean withImagesOnly) {
            this.withImagesOnly = withImagesOnly;
        }

        public String getAdStatus() {
            return adStatus;
        }

        public void setAdStatus(String adStatus) {
            this.adStatus = adStatus;
        }

        public List<Long> getGroupFilters() {
            return groupFilters;
        }

        public void setGroupFilters(List<Long> groupFilters) {
            this.groupFilters = groupFilters;
        }

        public List<Long> getGroups() {
            return groups;
        }

        public void setGroups(List<Long> groups) {
            this.groups = groups;
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

        public Long getCurrency() {
            return currency;
        }

        public void setCurrency(Long currency) {
            this.currency = currency;
        }

        public int getSince_number() {
            return since_number;
        }

        public void setSince_number(int since_number) {
            this.since_number = since_number;
        }

        public String getSince_field() {
            return since_field;
        }

        public void setSince_field(String since_field) {
            this.since_field = since_field;
        }

    }

    public static class SearchAdsResponse extends GenericResponse {

        private List<AdEntity> adList;

        public List<AdEntity> getAdList() {
            return adList;
        }

        public void setAdList(List<AdEntity> adList) {
            this.adList = adList;
        }

    }

    public static class AdEntity {

        private String description;
        private String currencySymbol;
        private String title;
        private BigDecimal price;
        private String image;
        private Long id;
        private String publishedBy;

        public String getPublishedBy() {
            return publishedBy;
        }

        public void setPublishedBy(String publishedBy) {
            this.publishedBy = publishedBy;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getCurrencySymbol() {
            return currencySymbol;
        }

        public void setCurrencySymbol(String currencySymbol) {
            this.currencySymbol = currencySymbol;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public BigDecimal getPrice() {
            return price;
        }

        public void setPrice(BigDecimal price) {
            this.price = price;
        }

        public String getImage() {
            return image;
        }

        public void setImage(String image) {
            this.image = image;
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }
    }

    @RequestMapping(value = "admin/searchAds", method = RequestMethod.POST)
    @ResponseBody
    public SearchAdsResponse searchAds(@RequestBody SearchAdsParameters params) {
        SearchAdsResponse response = new SearchAdsResponse();
        List<Ad> ads = null;

        final FullTextAdQuery query = new FullTextAdQuery();
        if (params.getCategory() != null && params.getCategory()>0L) {
            query.setCategory(adCategoryService.load(params.getCategory()));
        }
        if (params.getKeywords() != null) {
            query.setKeywords(params.getKeywords());
        }
        if (params.getCurrency() != null && params.getCurrency() > 0L) {
            query.setCurrency(currencyService.load(params.getCurrency()));
        }
        if (params.getInitialPrice() != null) {
            query.setInitialPrice(params.getInitialPrice());
        }
        if (params.getFinalPrice() != null) {
            query.setFinalPrice(params.getFinalPrice());
        }
        if (params.isWithImagesOnly() != false) {
            query.setWithImagesOnly(params.isWithImagesOnly());
        }
        if (params.getAdStatus() != null) {
            query.setStatus(Ad.Status.valueOf(params.getAdStatus()));
        }
        if (params.getTradeType() != null) {
            query.setTradeType(Ad.TradeType.valueOf(params.getTradeType()));
        }
        if (params.getGroupFilters() != null) {
            List<GroupFilter> groupFilters = new ArrayList();
            for (Long groupId : params.getGroupFilters()) {
                groupFilters.add(groupFilterService.load(groupId, GroupFilter.Relationships.GROUPS));
            }
            query.setGroupFilters(groupFilters);
        }
        if (params.getGroups() != null) {
            List<MemberGroup> groups = new ArrayList();
            for (Long id : params.getGroups()) {
                groups.add((MemberGroup) groupService.load(id, Group.Relationships.GROUP_FILTERS));
            }
            query.setGroups(groups);
        }
        if (params.getSince_field() != null) {
            TimePeriod tp = new TimePeriod();
            tp.setNumber(params.getSince_number());
            tp.setField(TimePeriod.Field.valueOf(params.getSince_field()));
            query.setSince(tp);
        }

        ads = adService.fullTextSearch(query);
        List<AdEntity> adList = new ArrayList();
        for (Ad ad : ads) {
            AdEntity entity = new AdEntity();
            entity.setId(ad.getId());
            entity.setTitle(ad.getTitle());
            entity.setDescription(ad.getDescription());
            //entity.setImage(ad.getImages());
            entity.setCurrencySymbol(ad.getCurrency().getSymbol());
            entity.setPrice(ad.getPrice());
            entity.setPublishedBy(ad.getOwner().getName());
            adList.add(entity);

        }
        response.setStatus(0);
        response.setAdList(adList);
        return response;
    }

}
