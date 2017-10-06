/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.strohalm.cyclos.webservices.rest.members.reports;

import nl.strohalm.cyclos.annotations.Inject;
import nl.strohalm.cyclos.entities.members.Element;
import nl.strohalm.cyclos.entities.members.Member;
import nl.strohalm.cyclos.services.elements.ActivitiesVO;
import nl.strohalm.cyclos.services.elements.ElementService;
import nl.strohalm.cyclos.services.elements.MemberService;
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
public class ActivitiesController extends BaseRestController {

    private MemberService memberService;
    private ElementService elementService;

    public void setElementService(ElementService elementService) {
        this.elementService = elementService;
    }
    
    public MemberService getMemberService() {
        return memberService;
    }

    @Inject
    public void setMemberService(final MemberService memberService) {
        this.memberService = memberService;
    }

    public static class ActivitiesResponse extends GenericResponse {

        private long memberId;
        private boolean myActivities;
        private ActivitiesVO activities;
        private Member member;

        public Member getMember() {
            return member;
        }

        public void setMember(Member member) {
            this.member = member;
        }
        

        public ActivitiesVO getActivities() {
            return activities;
        }

        public void setActivities(ActivitiesVO activities) {
            this.activities = activities;
        }
        
        public boolean isMyActivities() {
            return myActivities;
        }

        public void setMyActivities(boolean myActivities) {
            this.myActivities = myActivities;
        }
        
        public long getMemberId() {
            return memberId;
        }

        public void setMemberId(final long memberId) {
            this.memberId = memberId;
        }
    }

    @RequestMapping(value = "member/report/{memberId}", method = RequestMethod.GET)
    @ResponseBody
    public ActivitiesResponse reports(@PathVariable("memberId") long memberId) throws Exception {
       ActivitiesResponse response = new ActivitiesResponse();

        boolean myActivities = false;

        Member member;
        if (memberId<= 0 || LoggedUser.element().getId().equals(memberId)) {
            member = LoggedUser.member();
            myActivities = true;
        } else {
            final Element element = elementService.load(memberId, Element.Relationships.USER, Element.Relationships.GROUP);
            if (!(element instanceof Member)) {
                throw new ValidationException();
            }
            member = (Member) element;
        }

        // Get the activities
        final ActivitiesVO activities = memberService.getActivities(member);

        
        response.setActivities(activities);
        response.setMyActivities(myActivities);
        response.setStatus(0);
        response.setMessage("!!Display reports");

      return response;
    }

}
