package nl.strohalm.cyclos.webservices.rest.settings;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import nl.strohalm.cyclos.access.AdminSystemPermission;
import nl.strohalm.cyclos.controls.ActionContext;
import nl.strohalm.cyclos.entities.settings.MessageSettings.MessageSettingsEnum;
import nl.strohalm.cyclos.services.permissions.PermissionService;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;
import nl.strohalm.cyclos.webservices.rest.GenericResponse;

import org.apache.struts.action.ActionForward;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class ListMessageSettingsController extends BaseRestController {
    
    private static final List<String> GENERAL;
    private static final List<String> MEMBER_NOTIFICATIONS;
    private static final List<String> ADMIN_NOTIFICATIONS;
    private PermissionService permissionService;
    
    static {
        final List<String> general = new ArrayList<String>();
        final List<String> member = new ArrayList<String>();
        final List<String> admin = new ArrayList<String>();
        
        for (final MessageSettingsEnum messageSetting : MessageSettingsEnum
                .values()) {
            switch (messageSetting.getCategory()) {
                case GENERAL:
                    general.add(messageSetting.settingName());
                    break;
                case MEMBER:
                    member.add(messageSetting.settingName());
                    break;
                case ADMIN:
                    admin.add(messageSetting.settingName());
                    break;
                default:
                    throw new IllegalAccessError(
                            "Unknown message setting category: "
                            + messageSetting.getCategory());
            }
        }
        
        GENERAL = Collections.unmodifiableList(general);
        MEMBER_NOTIFICATIONS = Collections.unmodifiableList(member);
        ADMIN_NOTIFICATIONS = Collections.unmodifiableList(admin);
    }
    
    public static class ListMessageSettingsRequestDto {
        
    }
    
    public static class ListMessageSettingsResponseDto extends GenericResponse {
        
        private boolean editable;
        private List<String> generalNotifiaction;
        private List<String> memberNotification;
        private List<String> adminNotification;
        
        public boolean isEditable() {
            return editable;
        }
        
        public void setEditable(boolean editable) {
            this.editable = editable;
        }
        
        public List<String> getGeneralNotifiaction() {
            return generalNotifiaction;
        }
        
        public void setGeneralNotifiaction(List<String> generalNotifiaction) {
            this.generalNotifiaction = generalNotifiaction;
        }
        
        public List<String> getMemberNotification() {
            return memberNotification;
        }
        
        public void setMemberNotification(List<String> memberNotification) {
            this.memberNotification = memberNotification; 
        }
        
        public List<String> getAdminNotification() {
            return adminNotification;
        }
        
        public void setAdminNotification(List<String> adminNotification) {
            this.adminNotification = adminNotification;
        }
        
    }
    
    @RequestMapping(value = "admin/listMessageSettings", method = RequestMethod.GET)
    @ResponseBody
    protected ListMessageSettingsResponseDto executeAction()
            throws Exception {
        ListMessageSettingsResponseDto response = new ListMessageSettingsResponseDto();
        //final boolean editable = permissionService.hasPermission(AdminSystemPermission.TRANSLATION_MANAGE_NOTIFICATION);
        //request.setAttribute("editable", editable);
       // response.setEditable(editable);
        //request.setAttribute("general", );
        response.setGeneralNotifiaction(GENERAL);
        // request.setAttribute("memberNotifications", MEMBER_NOTIFICATIONS);
        response.setMemberNotification(MEMBER_NOTIFICATIONS);
        // request.setAttribute("adminNotifications", ADMIN_NOTIFICATIONS);
        response.setAdminNotification(ADMIN_NOTIFICATIONS);
        response.setStatus(0);
        return response;
    }
}
