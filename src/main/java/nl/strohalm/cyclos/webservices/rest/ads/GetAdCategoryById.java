/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.strohalm.cyclos.webservices.rest.ads;

import nl.strohalm.cyclos.entities.ads.AdCategory;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;
import nl.strohalm.cyclos.webservices.rest.GenericResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 *
 * @author Lue Infoservices
 */
@Controller
public class GetAdCategoryById extends BaseRestController {
    
    public static class AdCategoryResponse extends GenericResponse {
        
        private AdCategoryEntity adCategory;
        
        public AdCategoryEntity getAdCategory() {
            return adCategory;
        }
        
        public void setAdCategory(AdCategoryEntity adCategory) {
            this.adCategory = adCategory;
        }
        
    }
    
    private static class AdCategoryEntity {
        
        private Long id;
        private String name;
        private boolean categoryStatus;
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

        public boolean isCategoryStatus() {
            return categoryStatus;
        }

        public void setCategoryStatus(boolean categoryStatus) {
            this.categoryStatus = categoryStatus;
        }
        
    
        public int getSubCategory() {
            return subCategory;
        }
        
        public void setSubCategory(int subCategory) {
            this.subCategory = subCategory;
        }
        
    }
    
    @RequestMapping(value = "admin/getAdCategoryById/{id}", method = RequestMethod.GET)
    @ResponseBody
    public AdCategoryResponse getAdCategory(@PathVariable("id") Long id) {
        AdCategoryResponse response = new AdCategoryResponse();
        AdCategory adCategory = adCategoryService.load(id, AdCategory.Relationships.CHILDREN);
        AdCategoryEntity entity = new AdCategoryEntity();
        entity.setId(adCategory.getId());
        entity.setName(adCategory.getName());
        entity.setSubCategory(adCategory.getLevel());
        entity.setCategoryStatus(true);
        response.setAdCategory(entity);
        response.setStatus(0);
        return response;
        
    }
}
