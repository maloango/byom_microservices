/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.strohalm.cyclos.webservices.rest.members.preferences;

import java.util.ArrayList;
import java.util.List;
import nl.strohalm.cyclos.annotations.Inject;
import nl.strohalm.cyclos.entities.members.Member;
import nl.strohalm.cyclos.entities.members.adInterests.AdInterest;
import nl.strohalm.cyclos.entities.members.adInterests.AdInterestQuery;
import nl.strohalm.cyclos.services.elements.AdInterestService;
import nl.strohalm.cyclos.utils.access.LoggedUser;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;
import nl.strohalm.cyclos.webservices.rest.GenericResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 *
 * @author Lue
 */
@Controller
public class ListAdInterestsController extends BaseRestController {

    private AdInterestService adInterestService;

    public AdInterestService getAdInterestService() {
        return adInterestService;
    }

    @Inject
    public void setAdInterestService(final AdInterestService adInterestService) {
        this.adInterestService = adInterestService;
    }

    public static class ListAdInterestsResponse extends GenericResponse {

       
        private Member owner;
        private List<AdInterest> adInterests = new ArrayList<AdInterest>();

        public List<AdInterest> getAdInterests() {
            return adInterests;
        }

        public void setAdInterests(List<AdInterest> adInterests) {
            this.adInterests = adInterests;
        }
        
        public Member getOwner() {
            return owner;
        }

        public void setOwner(Member owner) {
            this.owner = owner;
        }

    }

    @RequestMapping(value = "member/listadInterest", method = RequestMethod.GET)
    @ResponseBody
    public ListAdInterestsResponse listAd() throws Exception {
        
        ListAdInterestsResponse response = new ListAdInterestsResponse();
        final Member owner = LoggedUser.element();
        final AdInterestQuery query = new AdInterestQuery();
        query.setOwner(owner);
        final List<AdInterest> adInterests = adInterestService.search(query);
        response.setAdInterests(adInterests);
        response.setStatus(0);
        response.setMessage("!! List of AdInterest...");
        return response;
    }

}
