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

        private List<InterestEntity> interestList;

        public List<InterestEntity> getInterestList() {
            return interestList;
        }

        public void setInterestList(List<InterestEntity> interestList) {
            this.interestList = interestList;
        }

        private List<AdInterest> adInterests = new ArrayList<AdInterest>();

        public List<AdInterest> getAdInterests() {
            return adInterests;
        }

        public void setAdInterests(List<AdInterest> adInterests) {
            this.adInterests = adInterests;
        }

    }

    public static class InterestEntity {

        private Long id;
        private String owner;

        public String getOwner() {
            return owner;
        }

        public void setOwner(String owner) {
            this.owner = owner;
        }

      
        public long getId() {
            return id;
        }

        public void setId(long id) {
            this.id = id;
        }

    }

    @RequestMapping(value = "member/listadInterest", method = RequestMethod.GET)
    @ResponseBody
    public ListAdInterestsResponse listAd() throws Exception {

        ListAdInterestsResponse response = new ListAdInterestsResponse();
        final Member owner = LoggedUser.element();
        final AdInterestQuery query = new AdInterestQuery();
        query.setOwner(owner);
        // query.setOwner(LoggedUser.member());
        final List<AdInterest> adInterests = adInterestService.search(query);
        System.out.println(".... adinterest" + adInterestService.search(query));

        List<InterestEntity> interestList = new ArrayList();
        for (AdInterest ad : adInterests) {
            InterestEntity entity = new InterestEntity();
            entity.setId(ad.getId());
            entity.setOwner(ad.getMember().getName());
           

            interestList.add(entity);
        }
        response.setInterestList(interestList);

        response.setStatus(0);
        response.setMessage("!! List of AdInterest...");

        return response;
    }

}
