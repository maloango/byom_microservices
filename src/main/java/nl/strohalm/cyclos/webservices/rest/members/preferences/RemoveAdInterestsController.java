/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.strohalm.cyclos.webservices.rest.members.preferences;

import nl.strohalm.cyclos.annotations.Inject;
import nl.strohalm.cyclos.services.elements.AdInterestService;
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
public class RemoveAdInterestsController extends BaseRestController {

    private AdInterestService adInterestService;

    public AdInterestService getAdInterestService() {
        return adInterestService;
    }

    @Inject
    public void setAdInterestService(final AdInterestService adInterestService) {
        this.adInterestService = adInterestService;
    }

    public static class RemoveAdInterest extends GenericResponse {

        private Long[] adInterestsIds;

        public Long[] getAdInterestsIds() {
            return adInterestsIds;
        }

        public void setAdInterestsIds(final Long[] adInterestsIds) {
            this.adInterestsIds = adInterestsIds;
        }
    }

    @RequestMapping(value = "member/removeAdInterest/{adInterestsIds}", method = RequestMethod.GET)
    @ResponseBody
    public GenericResponse removeAdInteres(@PathVariable("adInterestsIds") Long[] adInterestsIds) throws Exception {
        GenericResponse response = new GenericResponse();
        final Long[] ids = adInterestsIds;
        adInterestService.remove(ids);
        response.setStatus(0);
        response.setMessage("!! AdInterest has deleted...");
        return response;

    }

}
