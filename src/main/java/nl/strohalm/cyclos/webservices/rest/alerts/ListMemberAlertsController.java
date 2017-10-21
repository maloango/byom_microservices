package nl.strohalm.cyclos.webservices.rest.alerts;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import nl.strohalm.cyclos.annotations.Inject;
import nl.strohalm.cyclos.entities.alerts.Alert;
import nl.strohalm.cyclos.entities.alerts.AlertQuery;
import nl.strohalm.cyclos.entities.alerts.MemberAlert;
import nl.strohalm.cyclos.entities.groups.AdminGroup;
import nl.strohalm.cyclos.entities.members.Element;
import nl.strohalm.cyclos.services.alerts.AlertService;
import nl.strohalm.cyclos.services.groups.GroupService;
import nl.strohalm.cyclos.utils.RelationshipHelper;
import nl.strohalm.cyclos.utils.access.LoggedUser;
import nl.strohalm.cyclos.utils.query.PageParameters;
import nl.strohalm.cyclos.utils.query.QueryParameters;
import org.springframework.stereotype.Controller;

import nl.strohalm.cyclos.webservices.rest.BaseRestController;
import nl.strohalm.cyclos.webservices.rest.GenericResponse;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class ListMemberAlertsController extends BaseRestController {

    private static final int MAX_ALERTS = 200;

    private AlertService alertService;
    private GroupService groupService;

    @Inject
    public AlertService getAlertService() {
        return alertService;
    }

    @Inject
    public void setAlertService(AlertService alertService) {
        this.alertService = alertService;
    }

    @Inject
    public GroupService getGroupService() {
        return groupService;
    }

    @Inject
    public void setGroupService(GroupService groupService) {
        this.groupService = groupService;
    }
    
    public static class ListMemberAlertResponse extends GenericResponse{
       private  List<MemberAlertEntity>alerts;
       private boolean isSystem;
       private boolean isMember;

        public List<MemberAlertEntity> getAlerts() {
            return alerts;
        }

        public void setAlerts(List<MemberAlertEntity> alerts) {
            this.alerts = alerts;
        }

    

     

     

        public boolean isIsSystem() {
            return isSystem;
        }

        public void setIsSystem(boolean isSystem) {
            this.isSystem = isSystem;
        }

        public boolean isIsMember() {
            return isMember;
        }

        public void setIsMember(boolean isMember) {
            this.isMember = isMember;
        }
       
        
        
    }
    
   public static class MemberAlertEntity{
      private Long id;
       private String key;
       private Calendar date;
       private String memberName;

        public String getMemberName() {
            return memberName;
        }

        public void setMemberName(String memberName) {
            this.memberName = memberName;
        }
       

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public Calendar getDate() {
            return date;
        }

        public void setDate(Calendar date) {
            this.date = date;
        }
       
   }

    @RequestMapping(value = "admin/listMemberAlerts", method = RequestMethod.GET)
    @ResponseBody
    public ListMemberAlertResponse listSystemAlerts() {
        final AlertQuery query = new AlertQuery();
        ListMemberAlertResponse response=new ListMemberAlertResponse();
        query.setResultType(QueryParameters.ResultType.LIST);
        query.setPageParameters(PageParameters.max(MAX_ALERTS));
        query.setShowRemoved(false);

        final Alert.Type type = getType();
        query.setType(type);
        System.out.println("-----type:---"+type);
         
        if (Alert.Type.MEMBER.equals(type) && LoggedUser.isAdministrator()) {
            System.out.println("------within loop--------------------");
            
            AdminGroup adminGroup = LoggedUser.group();
            adminGroup = groupService.load(adminGroup.getId(), AdminGroup.Relationships.MANAGES_GROUPS);
            query.setGroups(adminGroup.getManagesGroups());

            query.fetch(RelationshipHelper.nested(MemberAlert.Relationships.MEMBER, Element.Relationships.USER));
             System.out.println(query);
          
        }

        final List<? extends Alert> alertsList =  alertService.search(query);
        List<MemberAlertEntity>alerts=new ArrayList();
        for(Alert alert:alertsList){
            MemberAlertEntity memberAlert=new MemberAlertEntity();
            memberAlert.setId(alert.getId());
            memberAlert.setKey(alert.getKey());
            memberAlert.setDate(alert.getDate());
            MemberAlert m=(MemberAlert) alert;
            memberAlert.setMemberName(m.getMember().getName());
            alerts.add(memberAlert);
        }
         System.out.println(alerts.size());
        //request.setAttribute("alerts", alerts);
        response.setAlerts(alerts);
        //request.setAttribute("isSystem", type == Alert.Type.SYSTEM);
        response.setIsSystem(type == Alert.Type.SYSTEM);
        //request.setAttribute("isMember", type == Alert.Type.MEMBER);
        response.setIsMember(type == Alert.Type.MEMBER);
        return response;
    }

    protected Alert.Type getType() {
        return Alert.Type.MEMBER;
    }

}
