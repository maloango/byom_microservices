/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.strohalm.cyclos.webservices.rest.access;

import nl.strohalm.cyclos.annotations.Inject;
import nl.strohalm.cyclos.entities.access.AdminUser;
import nl.strohalm.cyclos.entities.access.User;
import nl.strohalm.cyclos.entities.members.Element;
import nl.strohalm.cyclos.services.access.AccessService;
import nl.strohalm.cyclos.services.access.ChangeLoginPasswordDTO;
import nl.strohalm.cyclos.services.access.exceptions.CredentialsAlreadyUsedException;
import nl.strohalm.cyclos.services.elements.ElementService;
import nl.strohalm.cyclos.utils.ChangePasswordVO;
import nl.strohalm.cyclos.utils.MessageResolver;
import nl.strohalm.cyclos.utils.RelationshipHelper;
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
public class ChangePasswordController extends BaseRestController {

    private ElementService elementService;
    private AccessService accessService;
    private MessageResolver messageResolver;

    @Inject
    public MessageResolver getMessageResolver() {
        return messageResolver;
    }

    @Inject
    public void setMessageResolver(MessageResolver messageResolver) {
        this.messageResolver = messageResolver;
    }

    @Inject
    public void setElementService(ElementService elementService) {
        this.elementService = elementService;
    }

    @Inject
    public void setAccessService(AccessService accessService) {
        this.accessService = accessService;
    }

    public static class ChangePasswordResponse extends GenericResponse {

        private ChangePasswordVO user;
        private int passwordLength;
        private AdminUser ofAdmin;
        private String myPassword;
        private boolean shouldRequestOldPassword;

        public ChangePasswordVO getUser() {
            return user;
        }

        public void setUser(ChangePasswordVO user) {
            this.user = user;
        }

        public int getPasswordLength() {
            return passwordLength;
        }

        public void setPasswordLength(int passwordLength) {
            this.passwordLength = passwordLength;
        }

        public AdminUser getOfAdmin() {
            return ofAdmin;
        }

        public void setOfAdmin(AdminUser ofAdmin) {
            this.ofAdmin = ofAdmin;
        }

        public String getMyPassword() {
            return myPassword;
        }

        public void setMyPassword(String myPassword) {
            this.myPassword = myPassword;
        }

        public boolean isShouldRequestOldPassword() {
            return shouldRequestOldPassword;
        }

        public void setShouldRequestOldPassword(boolean shouldRequestOldPassword) {
            this.shouldRequestOldPassword = shouldRequestOldPassword;
        }
    }

    @RequestMapping(value = "admin/changePassword", method = RequestMethod.POST)
    @ResponseBody
    public GenericResponse changeAdminPassword(@RequestBody ChangePasswordVO request) throws Exception {
        GenericResponse response = new GenericResponse();
        final ChangeLoginPasswordDTO params = new ChangeLoginPasswordDTO();
        params.setNewPassword(request.getNewPassword());
        params.setOldPassword(request.getOldPassword());
        params.setNewPasswordConfirmation(request.getNewPasswordConfirmation());
        User user = elementService.loadUser(request.getUserId(), RelationshipHelper.nested(User.Relationships.ELEMENT, Element.Relationships.GROUP));
        params.setUser(user);
      
        try {
            accessService.changePassword(params);
        } catch (final CredentialsAlreadyUsedException e) {
            e.printStackTrace();

        }
        response.setMessage(messageResolver.message("changePassword.modified", "ChangePassword"));
        response.setStatus(0);
        //response.setMessage("password changed successfully");
        return response;

    }

    // protected User ofUser(ChangeLoginPasswordDTO context) {
//        final long userId = context.getUser().getId();
//        if (userId == 0 || userId < 0) {
//            return context.getUser();
//        }
//        
//    }
//    protected void validateForm(ChangePasswordVO context) {
//
//        final ChangeLoginPasswordDTO params = context;
//        params.setUser(ofUser(context));
//        accessService.validateChangePassword(context);
//    }

}
