package nl.strohalm.cyclos.webservices.rest.alerts;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import nl.strohalm.cyclos.access.AdminSystemPermission;
import nl.strohalm.cyclos.annotations.Inject;
import nl.strohalm.cyclos.entities.alerts.Alert;
import nl.strohalm.cyclos.entities.alerts.AlertQuery;
import nl.strohalm.cyclos.entities.groups.AdminGroup;
import nl.strohalm.cyclos.entities.members.Element;
import nl.strohalm.cyclos.entities.members.Member;
import nl.strohalm.cyclos.services.alerts.AlertService;
import nl.strohalm.cyclos.services.elements.ElementService;
import nl.strohalm.cyclos.services.groups.GroupService;
import nl.strohalm.cyclos.services.permissions.PermissionService;
import nl.strohalm.cyclos.utils.Period;
import nl.strohalm.cyclos.utils.access.LoggedUser;
import org.springframework.stereotype.Controller;

import nl.strohalm.cyclos.webservices.rest.BaseRestController;
import nl.strohalm.cyclos.webservices.rest.GenericResponse;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
@Controller
public class SearchAlertsController extends BaseRestController{
	
	private AlertService alertService;
    private ElementService elementService;
    private GroupService groupService;
    private PermissionService permissionService;
    
    
    @Inject
    public AlertService getAlertService() {
        return alertService;
    }
    
    @Inject
    public void setAlertService(AlertService alertService) {
        this.alertService = alertService;
    }
    
    @Inject
    public ElementService getElementService() {
        return elementService;
    }
    
    @Inject
    public void setElementService(ElementService elementService) {
        this.elementService = elementService;
    }
    
    @Inject
    public GroupService getGroupService() {
        return groupService;
    }
    
    @Inject
    public void setGroupService(GroupService groupService) {
        this.groupService = groupService;
    }
    
    @Inject
    public PermissionService getPermissionService() {
        return permissionService;
    }
    
    @Inject
    public void setPermissionService(PermissionService permissionService) {
        this.permissionService = permissionService;
    }
    
    public static class SearchAlertRequest {
        
        private Period period;
        private Alert.Type type;
        private Member member;
        
        public Period getPeriod() {
            return period;
        }
        
        public void setPeriod(Period period) {
            this.period = period;
        }
        
        public Alert.Type getType() {
            return type;
        }
        
        public void setType(Alert.Type type) {
            this.type = type;
        }

        public Member getMember() {
            return member;
        }

        public void setMember(Member member) {
            this.member = member;
        }
        
        
    }
    
    public static class SearchAlertResponse extends GenericResponse {
        
        private List<? extends Alert> alerts;
        private Collection<Alert.Type> types;
        
        public Collection<Alert.Type> getTypes() {
            return types;
        }
        
        public void setTypes(Collection<Alert.Type> types) {
            this.types = types;
        }
        
        public List<? extends Alert> getAlerts() {
            return alerts;
        }
        
        public void setAlerts(List<? extends Alert> alerts) {
            this.alerts = alerts;
        }
        
    }
    
    @RequestMapping(value = "admin/searchAlerts", method = RequestMethod.POST)
    @ResponseBody
    public SearchAlertResponse searchAlerts(@RequestBody SearchAlertRequest searchAlertQuery) {
        SearchAlertResponse response = new SearchAlertResponse();
        final AlertQuery query = new AlertQuery();
        query.setPeriod(searchAlertQuery.getPeriod());
        query.setType(searchAlertQuery.getType());
        query.setMember((Member) elementService.load(LoggedUser.user().getId(), Element.Relationships.USER));
        
        if (LoggedUser.isAdministrator()) {
            AdminGroup adminGroup = LoggedUser.group();
            adminGroup = groupService.load(adminGroup.getId(), AdminGroup.Relationships.MANAGES_GROUPS);
            query.setGroups(adminGroup.getManagesGroups());
        }
        query.setShowRemoved(true);
        final Collection<Alert.Type> types = new ArrayList<Alert.Type>();
        if (permissionService.hasPermission(AdminSystemPermission.ALERTS_VIEW_SYSTEM_ALERTS)) {
            types.add(Alert.Type.SYSTEM);
        }
        if (permissionService.hasPermission(AdminSystemPermission.ALERTS_VIEW_MEMBER_ALERTS)) {
            types.add(Alert.Type.MEMBER);
        }
        final List<? extends Alert> alerts = alertService.search(query);
        response.setAlerts(alerts);
        response.setTypes(types);
        return response;
    }

}
