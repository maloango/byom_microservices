/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.strohalm.cyclos.webservices.rest.access;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import nl.strohalm.cyclos.access.AdminAdminPermission;
import nl.strohalm.cyclos.access.AdminMemberPermission;
import nl.strohalm.cyclos.entities.access.Session;
import nl.strohalm.cyclos.entities.access.SessionQuery;
import nl.strohalm.cyclos.entities.accounts.loans.LoanQuery;
import nl.strohalm.cyclos.entities.groups.Group;
import nl.strohalm.cyclos.entities.members.Member;
import nl.strohalm.cyclos.utils.RequestHelper;
import nl.strohalm.cyclos.utils.access.LoggedUser;
import nl.strohalm.cyclos.utils.binding.DataBinder;
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
public class ListConnectedUsersController extends BaseRestController {

    public static class ListConnectedUsersResponse extends GenericResponse {

        private Set<Group.Nature> groups;
        private boolean canDisconnectAdmin;
        private boolean canDisconnectMember;
        private List<ConnectedUsersEntity> sessions;

        public List<ConnectedUsersEntity> getSessions() {
            return sessions;
        }

        public void setSessions(List<ConnectedUsersEntity> sessions) {
            this.sessions = sessions;
        }

        public Set<Group.Nature> getGroups() {
            return groups;
        }

        public void setGroups(Set<Group.Nature> groups) {
            this.groups = groups;
        }

        public boolean isCanDisconnectAdmin() {
            return canDisconnectAdmin;
        }

        public void setCanDisconnectAdmin(boolean canDisconnectAdmin) {
            this.canDisconnectAdmin = canDisconnectAdmin;
        }

        public boolean isCanDisconnectMember() {
            return canDisconnectMember;
        }

        public void setCanDisconnectMember(boolean canDisconnectMember) {
            this.canDisconnectMember = canDisconnectMember;
        }

    }

    public static class ConnectedUsersParameters {

        private String[] natures;

        public String[] getNatures() {
            return natures;
        }

        public void setNatures(String[] natures) {
            this.natures = natures;
        }

    }

    public static class ConnectedUsersEntity {

        private String identifier;
        private Calendar creationDate;
        private Calendar expirationDate;
        private String user;
        private String name;
        private String remoteAddress;
        private boolean posWeb;
        private Long id;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
        

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getIdentifier() {
            return identifier;
        }

        public void setIdentifier(String identifier) {
            this.identifier = identifier;
        }

        public Calendar getCreationDate() {
            return creationDate;
        }

        public void setCreationDate(Calendar creationDate) {
            this.creationDate = creationDate;
        }

        public Calendar getExpirationDate() {
            return expirationDate;
        }

        public void setExpirationDate(Calendar expirationDate) {
            this.expirationDate = expirationDate;
        }

        public String getUser() {
            return user;
        }

        public void setUser(String user) {
            this.user = user;
        }

        public String getRemoteAddress() {
            return remoteAddress;
        }

        public void setRemoteAddress(String remoteAddress) {
            this.remoteAddress = remoteAddress;
        }

        public boolean isPosWeb() {
            return posWeb;
        }

        public void setPosWeb(boolean posWeb) {
            this.posWeb = posWeb;
        }

    }

    @RequestMapping(value = "admin/listConnectedUsers", method = RequestMethod.GET)
    @ResponseBody
    public ListConnectedUsersResponse listUsers() throws Exception {
        ListConnectedUsersResponse response = new ListConnectedUsersResponse();
        Set<Group.Nature> groups = null;
        if (LoggedUser.isAdministrator()) {

            groups = EnumSet.allOf(Group.Nature.class);
        }
        response.setCanDisconnectAdmin(permissionService.hasPermission(AdminAdminPermission.ACCESS_DISCONNECT));
        response.setCanDisconnectMember(permissionService.hasPermission(AdminMemberPermission.ACCESS_DISCONNECT));
        response.setGroups(groups);
        response.setStatus(0);
        return response;

    }

    @RequestMapping(value = "admin/listConnectedUsers", method = RequestMethod.POST)
    @ResponseBody
    public ListConnectedUsersResponse searchConnectedUsers(@RequestBody ConnectedUsersParameters params) {
        ListConnectedUsersResponse response = new ListConnectedUsersResponse();
        final SessionQuery query = new SessionQuery();
        List<Group.Nature> groupNatures = new ArrayList();
        for (String groups : params.getNatures()) {
            groupNatures.add(Group.Nature.valueOf(groups));
        }
        query.setNatures(groupNatures);
        final List<Session> sessionList = accessService.searchSessions(query);
        List<ConnectedUsersEntity> sessions = new ArrayList();
        for (Session sess : sessionList) {
            ConnectedUsersEntity conectedUserEntiy = new ConnectedUsersEntity();
            conectedUserEntiy.setCreationDate(sess.getCreationDate());
            conectedUserEntiy.setExpirationDate(sess.getExpirationDate());
            conectedUserEntiy.setId(sess.getId());
            conectedUserEntiy.setIdentifier(sess.getIdentifier());
            conectedUserEntiy.setPosWeb(sess.isPosWeb());
            conectedUserEntiy.setRemoteAddress(sess.getRemoteAddress());
            conectedUserEntiy.setUser(sess.getUser().getUsername());
            conectedUserEntiy.setName(sess.getUser().getName());
            sessions.add(conectedUserEntiy);
        }
        response.setSessions(sessions);
        response.setStatus(0);
        response.setMessage("");
        return response;
    }

}
