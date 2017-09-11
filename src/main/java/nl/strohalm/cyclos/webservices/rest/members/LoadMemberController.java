/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.strohalm.cyclos.webservices.rest.members;

import nl.strohalm.cyclos.annotations.Inject;
import nl.strohalm.cyclos.entities.access.PrincipalType;
import nl.strohalm.cyclos.entities.exceptions.EntityNotFoundException;
import nl.strohalm.cyclos.entities.members.Element;
import nl.strohalm.cyclos.entities.members.Member;
import nl.strohalm.cyclos.services.access.ChannelService;
import nl.strohalm.cyclos.services.elements.ElementService;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;
import nl.strohalm.cyclos.webservices.rest.GenericResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 *
 * @author Lue
 */
@Controller
public class LoadMemberController extends BaseRestController{
    private ChannelService channelService;
    private ElementService elementService;

    public ElementService getElementService() {
        return elementService;
    }

    @Inject
    public void setElementService(ElementService elementService) {
        this.elementService = elementService;
    }

    @Inject
    public void setChannelService(final ChannelService channelService) {
        this.channelService = channelService;
    }

//    @Override
//    protected ContentType contentType() {
//        return ContentType.JSON;
//    }
    public static class LoadMemberRequest {

        private String username;

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

    }

    public static class LoadMemberResponse extends GenericResponse {

        private   Member member;

        public Member getMember() {
            return member;
        }

        public void setMember(Member member) {
            this.member = member;
        }
        

    }

    @RequestMapping(value = "member/loadMember", method = RequestMethod.POST)
    @ResponseBody
    protected LoadMemberResponse executeAction(@RequestBody LoadMemberRequest request) throws Exception {
        LoadMemberResponse response=new LoadMemberResponse();
        final PrincipalType principalType = channelService.resolvePrincipalType(request.getUsername(), "USER");
        final String principal = "USER";
        Member member;
        try {
            member = elementService.loadByPrincipal(principalType, principal, Element.Relationships.USER);
        } catch (final EntityNotFoundException e) {
            member = null;
        }
        response.setStatus(0);
        response.setMessage("member retrived!!");
        
        return response;
       
    }
}
