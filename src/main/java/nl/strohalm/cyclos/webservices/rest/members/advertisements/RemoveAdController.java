/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.strohalm.cyclos.webservices.rest.members.advertisements;

import nl.strohalm.cyclos.annotations.Inject;
import nl.strohalm.cyclos.services.ads.AdService;
import nl.strohalm.cyclos.utils.access.LoggedUser;
import nl.strohalm.cyclos.utils.validation.ValidationException;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;
import nl.strohalm.cyclos.webservices.rest.GenericResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 *
 * @author Lue
 */
@Controller
public class RemoveAdController extends BaseRestController {

    AdService adService;

    public AdService getAdService() {
        return adService;
    }

    @Inject
    public void setAdService(final AdService adService) {
        this.adService = adService;
    }

    public static class RemoveAdResponse extends GenericResponse {

        private long id;
        private long memberId;
        //private FormFile          picture;
        private String pictureCaption;

        public long getId() {
            return id;
        }

        public void setId(long id) {
            this.id = id;
        }

        public long getMemberId() {
            return memberId;
        }

        public void setMemberId(long memberId) {
            this.memberId = memberId;
        }

        public String getPictureCaption() {
            return pictureCaption;
        }

        public void setPictureCaption(String pictureCaption) {
            this.pictureCaption = pictureCaption;
        }
        

    }

    @RequestMapping(value = "member/removeAdvertisements/{id}", method = RequestMethod.GET)
    @ResponseBody
    public RemoveAdResponse removeAd(@PathVariable ("id") long id) throws Exception {
        RemoveAdResponse response = new RemoveAdResponse();
        if (id <= 0) {
            throw new ValidationException();
        }
        // Remove the advertisement
        adService.remove(id);
        response.setMessage("ad.removed");
        if (LoggedUser.isAdministrator()) {
            if (LoggedUser.user().getId()> 0) {
                return removeAd(id);
            } else {
                return (RemoveAdResponse) adService;
            }
      
    }
        response.setStatus(0);
        return response;
    }
}


