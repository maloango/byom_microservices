package nl.strohalm.cyclos.webservices.rest.ads;

import java.util.List;
import javax.servlet.http.HttpServletRequest;
import nl.strohalm.cyclos.entities.ads.AbstractAdQuery;
import nl.strohalm.cyclos.entities.ads.Ad;
import nl.strohalm.cyclos.entities.ads.AdCategory;
import nl.strohalm.cyclos.entities.ads.AdCategoryWithCounterQuery;
import nl.strohalm.cyclos.entities.ads.AdCategoryWithCounterVO;
import nl.strohalm.cyclos.entities.ads.AdQuery;
import nl.strohalm.cyclos.entities.members.Element;
import nl.strohalm.cyclos.utils.RelationshipHelper;
import nl.strohalm.cyclos.utils.RequestHelper;
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

    @RequestMapping(value = "admin/searchAds", method = RequestMethod.GET)
    @ResponseBody
    public AdsResponse executeQuery() {

        AdsResponse response = new AdsResponse();

        // Store the attributes we need for the search
//        RequestHelper.storeEnum(request, Ad.TradeType.class, "tradeTypes");
//        request.setAttribute("lastAdsForTradeType", form.isLastAds());
//
//        if (context.isAdmin()) {
//            request.setAttribute("editable", true);
//            RequestHelper.storeEnum(request, Ad.Status.class, "status");
//        }
        // Store the categories
        final Ad.TradeType tradeType = Ad.TradeType.SEARCH;
        int rootCategoryCount;

        if (settingsService.getLocalSettings().isShowCountersInAdCategories()) {
            final AdCategoryWithCounterQuery counterQuery = new AdCategoryWithCounterQuery();
            counterQuery.setTradeType(tradeType);
            final List<AdCategoryWithCounterVO> categories = adService.getCategoriesWithCounters(counterQuery);
            rootCategoryCount = categories.size();
            //request.setAttribute("categories", categories);
            //request.setAttribute("showCounters", true);
            response.setCategories(categories);
        } else {
            final List<AdCategory> categories = adCategoryService.listRoot();
            rootCategoryCount = categories.size();
            response.setAdCategories(categories);
            //request.setAttribute("categories", categories);

        }

        //   request.setAttribute("splitCategoriesAt", rootCategoryCount / 2);
//        if (form.isLastAds() || form.isCategoryOnly()) {
//            final AdQuery adQuery = new AdQuery();
//            adQuery.setStatus(Ad.Status.ACTIVE);
//            adQuery.setTradeType(tradeType);
//            adQuery.fetch(RelationshipHelper.nested(Ad.Relationships.OWNER, Element.Relationships.USER), Ad.Relationships.CURRENCY, Ad.Relationships.IMAGES);
//            form.clearForm();
//            form.setQuery("tradeType", tradeType.name());
//
//            return adQuery;
//        }
        // Retrieve the query
//        final AbstractAdQuery query = getDataBinder().readFromString(form.getQuery());
//        query.fetch(RelationshipHelper.nested(Ad.Relationships.OWNER, Element.Relationships.USER), Ad.Relationships.CURRENCY, Ad.Relationships.IMAGES, Ad.Relationships.CUSTOM_VALUES, RelationshipHelper.nested(Ad.Relationships.CATEGORY, RelationshipHelper.nested(AdCategory.MAX_LEVEL, AdCategory.Relationships.PARENT)));
//
//        if (!context.isAdmin()) {
//            // Search only active ads
//            query.setStatus(Ad.Status.ACTIVE);
//        }
        // Fetch the selected category recursively
//        final AdCategory category = query.getCategory() == null ? null : adCategoryService.load(query.getCategory().getId(), RelationshipHelper.nested(AdCategory.MAX_LEVEL, AdCategory.Relationships.PARENT));
//        if (category != null) {
//            request.setAttribute("categoryPath", category.getPathFromRoot());
//            request.setAttribute("category", category);
//        }
        response.setStatus(0);
        return response;

    }

}
