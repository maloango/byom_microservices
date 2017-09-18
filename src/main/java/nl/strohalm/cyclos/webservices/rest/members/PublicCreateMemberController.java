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
package nl.strohalm.cyclos.webservices.rest.members;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.servlet.http.HttpServletRequest;

import nl.strohalm.cyclos.annotations.Inject;
//import nl.strohalm.cyclos.controls.elements.CreateElementAction;
import nl.strohalm.cyclos.entities.access.MemberUser;
import nl.strohalm.cyclos.entities.access.User;
import nl.strohalm.cyclos.entities.customization.fields.MemberCustomFieldValue;
import nl.strohalm.cyclos.entities.groups.Group;
import nl.strohalm.cyclos.entities.members.Member;
import nl.strohalm.cyclos.entities.members.RegisteredMember;
import nl.strohalm.cyclos.exceptions.MailSendingException;
import nl.strohalm.cyclos.services.customization.MemberCustomFieldService;
import nl.strohalm.cyclos.services.elements.ElementService;
import nl.strohalm.cyclos.services.elements.WhenSaving;
import nl.strohalm.cyclos.services.elements.exceptions.UsernameAlreadyInUseException;
import nl.strohalm.cyclos.services.groups.GroupService;
import nl.strohalm.cyclos.services.settings.SettingsService;
import nl.strohalm.cyclos.utils.CustomFieldHelper;
import nl.strohalm.cyclos.utils.MessageResolver;
import nl.strohalm.cyclos.utils.binding.DataBinder;
import nl.strohalm.cyclos.utils.validation.ValidationException;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Action for a public member registration
 *
 * @author luis
 */
@Controller
public class PublicCreateMemberController extends BaseRestController {

    private MemberCustomFieldService memberCustomFieldService;
    private DataBinder<Member>       dataBinder;
    private ReadWriteLock            lock = new ReentrantReadWriteLock();

    private CustomFieldHelper        customFieldHelper;
    protected SettingsService settingsService;
    protected ElementService  elementService;
    private MessageResolver    messageResolver;
    private GroupService       groupService;
    
    public void setMessageResolver(final MessageResolver messageResolver) {
        this.messageResolver = messageResolver;
    }

//    public DataBinder<Member> getDataBinder() {
//        try {
//            lock.readLock().lock();
//            if (dataBinder == null) {
//                final LocalSettings localSettings = settingsService.getLocalSettings();
//                final AccessSettings accessSettings = settingsService.getAccessSettings();
//                dataBinder = CreateElementAction.getDataBinder(localSettings, accessSettings, Member.class, MemberUser.class, MemberGroup.class, MemberCustomField.class, MemberCustomFieldValue.class);
//            }
//            return dataBinder;
//        } 
//        finally {
//            lock.readLock().unlock();
//        }
//    }

    public MemberCustomFieldService getMemberCustomFieldService() {
        return memberCustomFieldService;
    }

    @Inject
    public void setGroupService(GroupService groupService) {
        this.groupService = groupService;
    }

    
    
    @Inject
    public void setCustomFieldHelper(final CustomFieldHelper customFieldHelper) {
        this.customFieldHelper = customFieldHelper;
    }

    @Inject
    public void setMemberCustomFieldService(final MemberCustomFieldService memberCustomFieldService) {
        this.memberCustomFieldService = memberCustomFieldService;
    }
    
     @Inject
    public void setSettingsService(final SettingsService settingsService) {
        this.settingsService = settingsService;
    }
     @Inject
    public final void setElementService(final ElementService elementService) {
        this.elementService = elementService;
    }
    
    public static class CreateMemberResponse {

        private int status;
        private String message;

        public int getStatus() {
            return status;
        }

        public void setStatus(int status) {
            this.status = status;
        }

        /**
         * @return the message
         */
        public String getMessage() {
            return message;
        }

        /**
         * @param message the message to set
         */
        public void setMessage(String message) {
            this.message = message;
        }
    }

    public static class CreatePublicMemberRequest {

        private String captcha;
        private String confirmPassword;
        private boolean forceChangePassword;
        private long groupId;
        private boolean manualPassword;
        private String username;
        private String name;
        private String email;
        private String password;
        
        private String customField1;
         private String customField2;
          private String customField3;
           private String customField4;
            private String customField5;
             private String customField6;
              private String customField7;
               private String customField8;
                private String customField9;
                 private String customField10;

        public String getCaptcha() {
            return captcha;
        }

        public void setCaptcha(String captcha) {
            this.captcha = captcha;
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

        public long getGroupId() {
            return groupId;
        }

        public void setGroupId(long groupId) {
            this.groupId = groupId;
        }

        public boolean isManualPassword() {
            return manualPassword;
        }

        public void setManualPassword(boolean manualPassword) {
            this.manualPassword = manualPassword;
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

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

      

        public String getCustomField1() {
            return customField1;
        }

        public void setCustomField1(String customField1) {
            this.customField1 = customField1;
        }

        public String getCustomField2() {
            return customField2;
        }

        public void setCustomField2(String customField2) {
            this.customField2 = customField2;
        }

        public String getCustomField3() {
            return customField3;
        }

        public void setCustomField3(String customField3) {
            this.customField3 = customField3;
        }

        public String getCustomField4() {
            return customField4;
        }

        public void setCustomField4(String customField4) {
            this.customField4 = customField4;
        }

        public String getCustomField5() {
            return customField5;
        }

        public void setCustomField5(String customField5) {
            this.customField5 = customField5;
        }

        public String getCustomField6() {
            return customField6;
        }

        public void setCustomField6(String customField6) {
            this.customField6 = customField6;
        }

        public String getCustomField7() {
            return customField7;
        }

        public void setCustomField7(String customField7) {
            this.customField7 = customField7;
        }

        public String getCustomField8() {
            return customField8;
        }

        public void setCustomField8(String customField8) {
            this.customField8 = customField8;
        }

        public String getCustomField9() {
            return customField9;
        }

        public void setCustomField9(String customField9) {
            this.customField9 = customField9;
        }

        public String getCustomField10() {
            return customField10;
        }

        public void setCustomField10(String customField10) {
            this.customField10 = customField10;
        }
        
     

    }

    @RequestMapping(value = "publicCreateMember", method = RequestMethod.POST)
    @ResponseBody
    public CreateMemberResponse publicCreateMember(HttpServletRequest request,@RequestBody CreatePublicMemberRequest form) {
        System.out.println("Request came for member creation");
        CreateMemberResponse response = null;
      
        try{
        
        // Check the captcha challenge
       
        // Save the member
        final Member member = new Member(); //getDataBinder().readFromString(form.getValues());
         member.setName(form.getName());
         member.setEmail(form.getEmail());
         
       MemberUser memberUser=new MemberUser();
       memberUser.setPassword(form.getPassword());
       memberUser.setUsername(form.getUsername());
       member.setUser(memberUser);
       //ReferenceConverter.instance(Group.class).
      
       member.setGroup(groupService.load(form.getGroupId(),Group.Relationships.PERMISSIONS));
       
       List<MemberCustomFieldValue> customFieldValue=new ArrayList<MemberCustomFieldValue>();
       
       MemberCustomFieldValue customField1=new MemberCustomFieldValue();
       customField1.setHidden(false);
       customField1.setValue(form.getCustomField1());
       customField1.setField(memberCustomFieldService.load(1L));
       customFieldValue.add(customField1);
       
        MemberCustomFieldValue customField2=new MemberCustomFieldValue();
       customField2.setHidden(false);
       customField2.setValue(form.getCustomField2());
       customField2.setField(memberCustomFieldService.load(2L));
       customFieldValue.add(customField2);
       
        MemberCustomFieldValue customField3=new MemberCustomFieldValue();
       customField3.setHidden(false);
       customField3.setValue(form.getCustomField3());
       customField3.setField(memberCustomFieldService.load(3L));
       customFieldValue.add(customField3);
       
        MemberCustomFieldValue customField4=new MemberCustomFieldValue();
       customField4.setHidden(false);
       customField4.setValue(form.getCustomField4());
       customField4.setField(memberCustomFieldService.load(4L));
       customFieldValue.add(customField4);
       
        MemberCustomFieldValue customField5=new MemberCustomFieldValue();
       customField5.setHidden(false);
       customField5.setValue(form.getCustomField5());
       customField5.setField(memberCustomFieldService.load(5L));
       customFieldValue.add(customField5);
       
       
        MemberCustomFieldValue customField6=new MemberCustomFieldValue();
       customField6.setHidden(false);
       customField6.setValue(form.getCustomField6());
       customField6.setField(memberCustomFieldService.load(6L));
       customFieldValue.add(customField6);
       
        MemberCustomFieldValue customField7=new MemberCustomFieldValue();
       customField7.setHidden(false);
       customField7.setValue(form.getCustomField7());
       customField7.setField(memberCustomFieldService.load(7L));
       customFieldValue.add(customField7);
       
        MemberCustomFieldValue customField8=new MemberCustomFieldValue();
       customField8.setHidden(false);
       customField8.setValue(form.getCustomField8());
       customField8.setField(memberCustomFieldService.load(8L));
       customFieldValue.add(customField8);
       
        MemberCustomFieldValue customField9=new MemberCustomFieldValue();
       customField9.setHidden(false);
       customField1.setValue(form.getCustomField9());
       customField9.setField(memberCustomFieldService.load(9L));
       customFieldValue.add(customField9);
       
        MemberCustomFieldValue customField10=new MemberCustomFieldValue();
       customField10.setHidden(false);
       customField10.setValue(form.getCustomField10());
       customField10.setField(memberCustomFieldService.load(10L));
       customFieldValue.add(customField10);
        
            System.out.println("Validating Form ----------");
        response=validateForm(member, form.isManualPassword(), form.getConfirmPassword());
        if(response==null){
            response=new CreateMemberResponse();
        }else{
            return response;
        }
        
        RegisteredMember registeredMember;
        try {
             System.out.println("Registering Member ----------");
            registeredMember = (RegisteredMember) elementService.register(member, false, request.getRemoteAddr());
             if(registeredMember==null){
                 response.setStatus(200);
                 response.setMessage("Sending Email error");
             }
        } catch (final UsernameAlreadyInUseException e) {
            e.printStackTrace();
//            final ActionForward actionForward = ActionHelper.sendError(mapping, request, response, "createMember.public.alreadyExists");
//            session.setAttribute("forceBack", "forceBack");
//            return actionForward;
            response.setStatus(200);
            response.setMessage(messageResolver.message("createMember.public.alreadyExists", member.getUsername()));
           return response;
        } catch (MailSendingException e) {
            e.printStackTrace();
           // return ActionHelper.sendError(mapping, request, response, "createMember.public.errorSendingMail");
             response.setStatus(200);
             System.out.println("Setting response message");
            response.setMessage(messageResolver.message("createMember.public.errorSendingMail", member.getEmail()));
            System.out.println("Returnning response");
            return response;
        }

        // We will send the flow to the error page not to showing an error, but the created message
        String message;
        if (registeredMember instanceof Member) {
            if (((Member) registeredMember).isActive()) {
                final User user = ((Member) registeredMember).getUser();
                if (user.getPassword() != null && user.getPasswordDate() == null) {
                    // Member is active and password is sent by mail
                    message = "createMember.public.awaitingPassword";
                } else {
                    // Member is ready
                    message = "createMember.public.validated";
                }
            } else {
                // Member in inactive group. Message is awaiting activation by admin
                message = "createMember.public.awaitingActivation";
            }
        } else {
            // Typed e-mail has to be validated
            message = "createMember.public.awaitingMailValidation";
        }
      //  session.removeAttribute("forceBack");
      //  ActionHelper.sendError(mapping, request, response, message, registeredMember.getUsername());
         response.setMessage(messageResolver.message(message, registeredMember.getUsername()));
        return response;
        }catch (Exception ex) {
            ex.printStackTrace();
            response.setStatus(400);
            response.setMessage("Internal Server Error");
            return response;
        }
    }


    protected CreateMemberResponse validateForm(final Member member,boolean manualPassword,String confirmPassword) throws ValidationException {
      //  final CreateMemberForm form = (CreateMemberForm) actionForm;

        // Form validation
      //  final Member member = getDataBinder().readFromString(form.getMember());
         CreateMemberResponse response=null;
      
      
        try {
            elementService.validate(member, WhenSaving.PUBLIC, manualPassword);
           
        } catch (final ValidationException e) {
            response=new CreateMemberResponse();
           response.setMessage(e.getMessage());
           response.setStatus(500);
           return response;
        }

//        final String captcha = form.getCaptcha();
//        if (StringUtils.isEmpty(captcha) || !CaptchaServlet.checkChallenge(request, captcha)) {
//            exc.addPropertyError("captcha", new ValidationError("createMember.captcha.invalid"));
//        }

        String password;
        try {
            password = StringUtils.trimToNull(member.getUser().getPassword());
        } catch (final Exception e) {
            password = null;
        }

       // final String confirmPassword = StringUtils.trimToNull(form.getConfirmPassword());
        if (password != null && (confirmPassword == null || !ObjectUtils.equals(confirmPassword, member.getUser().getPassword()))) {
//            exc.addGeneralError(new PasswordsDontMatchError());
             response=new CreateMemberResponse();
           response.setMessage(messageResolver.message("errors.passwords",confirmPassword));
           response.setStatus(500);
           return response;
        }

//        exc.throwIfHasErrors();
return response;
    }
}
