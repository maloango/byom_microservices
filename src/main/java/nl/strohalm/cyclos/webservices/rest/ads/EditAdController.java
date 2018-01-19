package nl.strohalm.cyclos.webservices.rest.ads;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import nl.strohalm.cyclos.entities.Relationship;
import nl.strohalm.cyclos.entities.ads.Ad;
import nl.strohalm.cyclos.entities.ads.AdCategory;
import nl.strohalm.cyclos.entities.ads.AdCategoryWithCounterQuery;
import nl.strohalm.cyclos.entities.ads.AdCategoryWithCounterVO;
import nl.strohalm.cyclos.entities.members.Element;
import nl.strohalm.cyclos.utils.RelationshipHelper;
import nl.strohalm.cyclos.utils.validation.ValidationException;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;
import nl.strohalm.cyclos.webservices.rest.GenericResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class EditAdController extends BaseRestController {

    private static final Relationship[] FETCH = {RelationshipHelper.nested(Ad.Relationships.CATEGORY, RelationshipHelper.nested(AdCategory.MAX_LEVEL, AdCategory.Relationships.PARENT)), RelationshipHelper.nested(Ad.Relationships.OWNER, Element.Relationships.USER), RelationshipHelper.nested(Ad.Relationships.OWNER, Element.Relationships.GROUP), Ad.Relationships.CUSTOM_VALUES, Ad.Relationships.IMAGES, Ad.Relationships.CURRENCY};

    public static class EditAdResponse extends GenericResponse {

        private List<AdCategory> categories;
        private   Ad ad ;
        

      

    }

    @RequestMapping(value = "admin/editAd", method = RequestMethod.GET)
    @ResponseBody
    public EditAdResponse prepareForm(@RequestBody Long id) {
        EditAdResponse response = new EditAdResponse();
        Ad ad = null;
        if (id > 0) {
            ad = adService.load(id, FETCH);
        }

        final List<AdCategory> leafCategories = new ArrayList<AdCategory>(adCategoryService.listLeaf());
        final List<AdCategory> categories = new ArrayList<AdCategory>();
        for (final Iterator<AdCategory> iterator = leafCategories.iterator(); iterator.hasNext();) {
            final AdCategory category = iterator.next();
            if (category.isRoot() && category.isLeaf()) {
                iterator.remove();
                categories.add(category);
            }
        }
        // Now, those categories which are both root and leaf are first. Add others
        categories.addAll(leafCategories);
        //request.setAttribute("categories", categories);
        return null;
    }

}
