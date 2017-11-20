/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.strohalm.cyclos.webservices.rest.members;

import nl.strohalm.cyclos.access.AdminMemberPermission;
import nl.strohalm.cyclos.access.BrokerPermission;
import nl.strohalm.cyclos.controls.ActionContext;
import nl.strohalm.cyclos.entities.access.MemberUser;
import nl.strohalm.cyclos.entities.access.User;
import nl.strohalm.cyclos.entities.groups.Group;
import nl.strohalm.cyclos.entities.members.Element;
import nl.strohalm.cyclos.entities.members.Member;
import nl.strohalm.cyclos.entities.members.RegisteredMember;
import nl.strohalm.cyclos.exceptions.MailSendingException;
import nl.strohalm.cyclos.utils.access.LoggedUser;
import nl.strohalm.cyclos.utils.transaction.CurrentTransactionData;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;
import nl.strohalm.cyclos.webservices.rest.GenericResponse;
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
public class CreateMemberController extends BaseRestController {

    public static class CreateMemberParameters {

        private Long groupId;
        private String username;
        private String name;
        private String email;
        private boolean hideEmail;
        private boolean manualPassword;
        private String password;
        private String confirmPassword;
        private boolean forceChangePassword;

        public Long getGroupId() {
            return groupId;
        }

        public void setGroupId(Long groupId) {
            this.groupId = groupId;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public boolean isHideEmail() {
            return hideEmail;
        }

        public void setHideEmail(boolean hideEmail) {
            this.hideEmail = hideEmail;
        }

        public boolean isManualPassword() {
            return manualPassword;
        }

        public void setManualPassword(boolean manualPassword) {
            this.manualPassword = manualPassword;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getConfirmPassword() {
            return confirmPassword;
        }

        public void setConfirmPassword(String confirmPassword) {
            this.confirmPassword = confirmPassword;
        }

        public boolean isForceChangePassword() {
            return forceChangePassword;
        }

        public void setForceChangePassword(boolean forceChangePassword) {
            this.forceChangePassword = forceChangePassword;
        }

    }

    @RequestMapping(value = "admin/createMember", method = RequestMethod.POST)
    @ResponseBody
    public GenericResponse create(@RequestBody CreateMemberParameters params) {
        GenericResponse response = new GenericResponse();
        final Member member = new Member();
        member.setName(params.getName());
        member.setEmail(params.getEmail());
        member.setGroup(groupService.load(params.getGroupId(), Group.Relationships.ELEMENTS));
        member.setHideEmail(params.isHideEmail());
        MemberUser memberUser = new MemberUser();
        memberUser.setUsername(params.getUsername());
        memberUser.setPassword(params.getPassword());
        member.setUser(memberUser);
      ensureBrokerIsSet(member);
//
       final boolean sendPasswordByEmail = member.getMemberGroup().getMemberSettings().isSendPasswordByEmail();
   final boolean canChangePassword = permissionService.hasPermission(LoggedUser.isAdministrator() ? AdminMemberPermission.ACCESS_CHANGE_PASSWORD : BrokerPermission.MEMBER_ACCESS_CHANGE_PASSWORD);
    final boolean allowSetPassword = !sendPasswordByEmail || canChangePassword;

        // When password cannot be set, ensure it's null
        if (!allowSetPassword) {
            final User user = member.getUser();
            if (user != null) {
                user.setPassword(null);
            }
        }
//
        // When password is not sent by e-mail and can't set a definitive password, ensure the force change is set
        if (!sendPasswordByEmail && !canChangePassword) {
           // form.setForceChangePassword(true);
        }

        RegisteredMember registeredMember;
        String successKey = "createMember.created";
        try {
            registeredMember = (RegisteredMember) elementService.register(member, params.isForceChangePassword(),LoggedUser.remoteAddress());
        } catch (final MailSendingException e) {
            response.setMessage("createMember.error.mailSending");
        }
//
        boolean sendMessage = false;

        // Check if there's a mail exception
        if (CurrentTransactionData.hasMailError()) {
            successKey = "createMember.created.mailError";
            sendMessage = true;
        }
        response.setMessage(successKey);
        response.setStatus(0);
        return response;
       
    }
    
     private void ensureBrokerIsSet( final Element element) {
        if (LoggedUser.isBroker()) {
            final Member member = (Member) element;
            member.setBroker((Member) LoggedUser.element());
        }
    }
}
