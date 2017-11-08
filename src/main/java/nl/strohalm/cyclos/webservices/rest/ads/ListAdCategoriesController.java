/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.strohalm.cyclos.webservices.rest.ads;

import java.util.ArrayList;
import java.util.List;
import nl.strohalm.cyclos.access.AdminSystemPermission;
import nl.strohalm.cyclos.entities.ads.AdCategory;
import nl.strohalm.cyclos.entities.ads.AdCategoryQuery;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;
import nl.strohalm.cyclos.webservices.rest.GenericResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 *
 * @author Lue Infoservices
 */
@Controller
public class ListAdCategoriesController extends BaseRestController {

    public static class ListAdCategoryRespnose extends GenericResponse {

        List<CategoryEntity> categoryList;
        private boolean editable;

        public boolean isEditable() {
            return editable;
        }

        public void setEditable(boolean editable) {
            this.editable = editable;
        }

        public List<CategoryEntity> getCategoryList() {
            return categoryList;
        }

        public void setCategoryList(List<CategoryEntity> categoryList) {
            this.categoryList = categoryList;
        }

    }

    public static class CategoryEntity {

        private Long id;
        private String name;
        private String status;
        private int subCategory;

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

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public int getSubCategory() {
            return subCategory;
        }

        public void setSubCategory(int subCategory) {
            this.subCategory = subCategory;
        }

   

    }

    @RequestMapping(value = "admin/listAdCategories", method = RequestMethod.GET)
    @ResponseBody
    public ListAdCategoryRespnose listCategory() {
        ListAdCategoryRespnose response = new ListAdCategoryRespnose();
        final AdCategoryQuery query = new AdCategoryQuery();
        query.setReturnDisabled(true);
        query.setParent(null);
        query.fetch(AdCategory.Relationships.CHILDREN);
        List<AdCategory> adCategories = adCategoryService.search(query);
        System.out.println("-----ad: "+adCategories);
        List<CategoryEntity> categoryList = new ArrayList();
        for (AdCategory ad : adCategories) {
            CategoryEntity entity = new CategoryEntity();
            entity.setId(ad.getId());
            entity.setName(ad.getName());
            entity.setStatus("Active");
            entity.setSubCategory(ad.getLevel());
            categoryList.add(entity);
        }
        response.setCategoryList(categoryList);
        response.setEditable(permissionService.hasPermission(AdminSystemPermission.AD_CATEGORIES_MANAGE));
        response.setStatus(0);
        return response;
    }
}
