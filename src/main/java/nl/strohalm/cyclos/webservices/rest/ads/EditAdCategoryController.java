/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.strohalm.cyclos.webservices.rest.ads;

import nl.strohalm.cyclos.entities.ads.AdCategory;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;
import nl.strohalm.cyclos.webservices.rest.GenericResponse;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 *
 * @author Lue Infoservices
 */
@Controller
public class EditAdCategoryController extends BaseRestController {

    public static class AdCategoryParameter {

        private Long id;
        private String name;
        private boolean active;
        private int order;

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

        public boolean isActive() {
            return active;
        }

        public void setActive(boolean active) {
            this.active = active;
        }

        public int getOrder() {
            return order;
        }

        public void setOrder(int order) {
            this.order = order;
        }

    }

    @RequestMapping(value = "admin/editCategory", method = RequestMethod.POST)
    @ResponseBody
    public GenericResponse editAdCategory(@RequestBody AdCategoryParameter params) {
        GenericResponse response = new GenericResponse();
        final AdCategory category = new AdCategory();
        if (params.getId() != null && params.getId() > 0L) {
            category.setId(params.getId());
        }
        category.setName(params.getName());
        category.setOrder(params.getOrder());
        category.setActive(params.isActive());
        final boolean insert = category.getId() == null;
        long id = 0;
        if (insert) {
            // Actually, there might be several categories, one per line
            final String[] names = category.getName().split("\\n");
            final int count = names.length;
            int nextOrder = -1;
            for (String name : names) {
                name = StringUtils.trimToNull(name);
                if (name == null) {
                    continue;
                }
                AdCategory cat = (AdCategory) category.clone();
                cat.setName(name);
                if (nextOrder >= 0) {
                    cat.setOrder(++nextOrder);
                }
                cat = adCategoryService.save(cat);
                if (nextOrder < 0) {
                    nextOrder = cat.getOrder();
                }
                if (count == 1) {
                    id = cat.getId();
                }
            }
            if (count > 1 && category.getParent() != null) {
                id = category.getParent().getId();
            }
        } else {
            id = adCategoryService.save(category).getId();
        }
        response.setMessage(insert ? "adCategory.inserted" : "adCategory.modified");
        response.setStatus(0);
        return response;
    }
}
