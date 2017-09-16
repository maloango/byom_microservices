/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.strohalm.cyclos.webservices.rest.access;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import nl.strohalm.cyclos.entities.access.Session;
import nl.strohalm.cyclos.entities.access.SessionQuery;
import nl.strohalm.cyclos.entities.groups.Group;
import nl.strohalm.cyclos.entities.members.Member;
import nl.strohalm.cyclos.utils.RequestHelper;
import nl.strohalm.cyclos.utils.access.LoggedUser;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;
import nl.strohalm.cyclos.webservices.rest.GenericResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 *
 * @author Lue Infoservices
 */
@Controller
public class ListConnectedUsersController extends BaseRestController{
    
    
    public static class ListConnectedUsersResponse extends GenericResponse{
        
       private List<Session> sessions ;

        public List<Session> getSessions() {
            return sessions;
        }

        public void setSessions(List<Session> sessions) {
            this.sessions = sessions;
        }
       
       
    }
    
    @RequestMapping(value="admin/listConnectedUsers",method=RequestMethod.GET)
    @ResponseBody
    public ListConnectedUsersResponse listUsers()throws Exception{
        ListConnectedUsersResponse response=new ListConnectedUsersResponse();
        final SessionQuery query=new SessionQuery();
       
        query.setMember(LoggedUser.member());
        List<Group.Nature> natures=new ArrayList();
       
       response.setSessions(accessService.searchSessions(query));
       response.setStatus(0);
       return response;
        
    }
    
}
