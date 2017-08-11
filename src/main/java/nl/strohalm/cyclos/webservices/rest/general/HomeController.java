/*
    This file is part of Cyclos (www.cyclos.org).
    A project of the Social Trade Organisation (www.socialtrade.org).

    Cyclos is free software; you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation; either version 2 of the License, or
    (at your option) any later version.

    Cyclos is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with Cyclos; if not, write to the Free Software
    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA

 */
package nl.strohalm.cyclos.webservices.rest.general;


import nl.strohalm.cyclos.annotations.Inject;
import nl.strohalm.cyclos.controls.BaseAction;
import nl.strohalm.cyclos.entities.access.TransactionPassword;
import nl.strohalm.cyclos.entities.access.User;
import nl.strohalm.cyclos.entities.access.User.TransactionPasswordStatus;
import nl.strohalm.cyclos.entities.groups.Group;
import nl.strohalm.cyclos.entities.groups.OperatorGroup;
import nl.strohalm.cyclos.entities.members.Element;
import nl.strohalm.cyclos.services.application.ApplicationService;
import nl.strohalm.cyclos.services.application.ApplicationStatusVO;
import nl.strohalm.cyclos.services.elements.MemberService;
import nl.strohalm.cyclos.services.elements.MemberStatusVO;
import nl.strohalm.cyclos.services.elements.QuickAccessVO;
import nl.strohalm.cyclos.utils.RelationshipHelper;
import nl.strohalm.cyclos.utils.access.LoggedUser;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;
import nl.strohalm.cyclos.webservices.utils.RestUserHelper;

import org.apache.struts.action.ActionForward;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Get the Personal, General messages and Number of incoming and outgoing invoices of the Member or AlertBean of Application, it depends if Member or
 * Administrator is logged
 * @author acz, luis
 */

@Controller
public class HomeController extends BaseRestController {

    private ApplicationService applicationService;
    private MemberService      memberService;

    @Inject
    public void setApplicationService(final ApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @Inject
    public void setMemberService(final MemberService memberService) {
        this.memberService = memberService;
    }
    
    public static class UserHomeResponse{
        private boolean generateTransactionPassword;
        private ApplicationStatusVO applicationStatus;
        
        private MemberStatusVO memberStatus;
        private QuickAccessVO  quickAccess;
        
        public int status;

        public boolean isGenerateTransactionPassword() {
            return generateTransactionPassword;
        }

        public void setGenerateTransactionPassword(boolean generateTransactionPassword) {
            this.generateTransactionPassword = generateTransactionPassword;
        }

        public ApplicationStatusVO getApplicationStatus() {
            return applicationStatus;
        }

        public void setApplicationStatus(ApplicationStatusVO applicationStatus) {
            this.applicationStatus = applicationStatus;
        }

        public MemberStatusVO getMemberStatus() {
            return memberStatus;
        }

        public void setMemberStatus(MemberStatusVO memberStatus) {
            this.memberStatus = memberStatus;
        }

        public int getStatus() {
            return status;
        }

        public void setStatus(int status) {
            this.status = status;
        }

         
        public QuickAccessVO getQuickAccess() {
            return quickAccess;
        }

        public void setQuickAccess(QuickAccessVO quickAccess) {
            this.quickAccess = quickAccess;
        }
        
        
    }

    @SuppressWarnings("unchecked")
    @RequestMapping(value = "admin/home", method = RequestMethod.GET)
    @ResponseBody
    protected UserHomeResponse getUserHome() throws Exception {

          UserHomeResponse response=new UserHomeResponse();
        // Check whether should allow the transaction password generation
//        if (context.isTransactionPasswordEnabled()) {
//            final User user = context.getUser();
//            Group loggedGroup = context.getGroup();
//            if (loggedGroup instanceof OperatorGroup) {
//                loggedGroup = groupService.load(loggedGroup.getId(), RelationshipHelper.nested(OperatorGroup.Relationships.MEMBER, Element.Relationships.GROUP));
//            }
//            final TransactionPassword transactionPassword = loggedGroup.getBasicSettings().getTransactionPassword();
//            final TransactionPasswordStatus transactionPasswordStatus = user.getTransactionPasswordStatus();
//            request.setAttribute("generateTransactionPassword", transactionPasswordStatus.isGenerationAllowed() && !(transactionPassword == TransactionPassword.MANUAL && transactionPasswordStatus == TransactionPasswordStatus.NEVER_CREATED));
//        }

        if (LoggedUser.isAdministrator()) {
            response.setApplicationStatus(applicationService.getApplicationStatus());
           // request.setAttribute("applicationStatus", applicationService.getApplicationStatus());
        } else {
            // Member / operator data
            response.setMemberStatus(memberService.getStatus());
           // request.setAttribute("status", memberService.getStatus());
           response.setQuickAccess(memberService.getQuickAccess());
          //  request.setAttribute("quickAccess", memberService.getQuickAccess());
        }

        return response;
    }
}
