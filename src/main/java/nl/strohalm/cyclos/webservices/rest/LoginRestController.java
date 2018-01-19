package nl.strohalm.cyclos.webservices.rest;

import javax.servlet.http.HttpServletRequest;
import nl.strohalm.cyclos.annotations.Inject;
import nl.strohalm.cyclos.entities.access.Channel;
import nl.strohalm.cyclos.entities.access.MemberUser;
import nl.strohalm.cyclos.entities.access.User;
import nl.strohalm.cyclos.exceptions.AccessDeniedException;
import nl.strohalm.cyclos.exceptions.PermissionDeniedException;
import nl.strohalm.cyclos.services.access.AccessService;
import nl.strohalm.cyclos.services.access.ChannelService;
import nl.strohalm.cyclos.services.access.exceptions.AlreadyConnectedException;
import nl.strohalm.cyclos.services.access.exceptions.BlockedCredentialsException;
import nl.strohalm.cyclos.services.access.exceptions.InactiveMemberException;
import nl.strohalm.cyclos.services.access.exceptions.LoginException;
import nl.strohalm.cyclos.services.access.exceptions.SystemOfflineException;
import nl.strohalm.cyclos.services.elements.ElementService;
import nl.strohalm.cyclos.utils.LoginHelper;
import org.apache.commons.lang.StringUtils;
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
public class LoginRestController extends BaseRestController {

    protected AccessService accessService;
    protected ChannelService channelService;
    protected LoginHelper loginHelper;
    protected ElementService elementService;

    public ChannelService getChannelService() {
        return channelService;
    }

    @Inject
    public void setAccessService(final AccessService accessService) {
        this.accessService = accessService;
    }

    @Inject
    public void setChannelService(final ChannelService channelService) {
        this.channelService = channelService;
    }

    @Inject
    public final void setLoginHelper(final LoginHelper loginHelper) {
        this.loginHelper = loginHelper;
    }

    @Inject
    public final void setElementService(final ElementService elementService) {
        this.elementService = elementService;
    }
    
    public static class VerifyLoginResponse{
        public int status;

        public int getStatus() {
            return status;
        }

        public void setStatus(int status) {
            this.status = status;
        }
        
    }

    @RequestMapping(value = "verifyAdminlogin", method = RequestMethod.POST)
    @ResponseBody
    public VerifyLoginResponse verifyAdminlogin(HttpServletRequest request)
    {
        VerifyLoginResponse resp=new VerifyLoginResponse();
        resp.setStatus(0);
        return resp;
    }
        
    @RequestMapping(value = "verifyMemberlogin", method = RequestMethod.POST)
    @ResponseBody
    public VerifyLoginResponse verifyMemberlogin(HttpServletRequest request)
    {
        VerifyLoginResponse resp=new VerifyLoginResponse();
        resp.setStatus(0);
        return resp;
    }    

    @RequestMapping(value = "login", method = RequestMethod.POST)
    @ResponseBody
    public LoginResponse doLogin(HttpServletRequest request,@RequestBody LoginForm form) {
         // System.out.println("Received request for login "+form.getPrincipal());
        LoginResponse response=new LoginResponse();

        try{
        final String member = StringUtils.trimToNull(form.getMember());
        final String principal = StringUtils.trimToNull(form.getPrincipal());
        final String password = form.getPassword();
        final String errorReturnTo = resolveErrorReturnTo(form);

        try {
            final Class<? extends User> requiredUserType = User.class;
            response = loginHelper.loginRest(requiredUserType, form.getPrincipalType(), member, principal, password, Channel.WEB, request);
            User user=response.getUser();
            response.setUser(null);
            if (user instanceof MemberUser && elementService.shallAcceptAgreement(((MemberUser) user).getMember())) {
            // Should accept a registration agreement first
            response.setForwardPage("acceptRegistrationAgreement");
            
            response.setErrorCode(200);//ok
        } else if (accessService.hasPasswordExpired()) {
            // Should change an expired password
            response.setForwardPage("changeExpiredPassword");
        } else if (StringUtils.isNotEmpty(errorReturnTo)) {
            // When redirecting to the previous page, remove from session to avoid a next unwanted redirection
            response.setForwardPage(errorReturnTo);
        } else {
            if(response.isIsAdmin())
            response.setForwardPage("adminHome");
            else
             response.setForwardPage("memberHome"); 
        }
            return response;
        } catch (final BlockedCredentialsException e) {
            response.setErrorMessage("login.error.blocked");
             response.setErrorCode(400);
            return response;
           // return ActionHelper.sendError(mapping, request, response, "login.error.blocked");
        } catch (final InactiveMemberException e) {
            response.setErrorMessage("login.error.inactive");
             response.setErrorCode(400);
            return response;
          //  return ActionHelper.sendError(mapping, request, response, "login.error.inactive");
        } catch (final AlreadyConnectedException e) {
            response.setErrorMessage("login.error.alreadyConnected");
             response.setErrorCode(400);
            return response;
          //  return ActionHelper.sendError(mapping, request, response, "login.error.alreadyConnected");
        } catch (final AccessDeniedException e) {
            response.setErrorMessage("error.accessDenied");
             response.setErrorCode(400);
            return response;
           // return ActionHelper.sendError(mapping, request, response, "error.accessDenied");
        } catch (final PermissionDeniedException e) {
            response.setErrorMessage("error.accessDenied");
             response.setErrorCode(400);
            return response;
           // return ActionHelper.sendError(mapping, request, response, "error.accessDenied");
        } catch (final SystemOfflineException e) {
            response.setErrorMessage("error.systemOffline");
             response.setErrorCode(400);
            return response;
          //  return ActionHelper.sendError(mapping, request, response, "error.systemOffline");
        } catch (final LoginException e) {
            response.setErrorMessage("login.error");
             response.setErrorCode(400);
            return response;
            //return ActionHelper.sendError(mapping, request, response, "login.error");
        }
        }catch (Exception ex) {
            ex.printStackTrace();
            response.setErrorCode(400);
            response.setErrorMessage(ex.getMessage());
        }
        return response;
    }

    protected String resolveErrorReturnTo(LoginForm form) {
        final String member = StringUtils.trimToNull(form.getMember());
        if (StringUtils.isEmpty(member)) {
            return null;
        } else {
            return "/do/login?operator=true";
        }
    }

}
