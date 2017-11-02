package nl.strohalm.cyclos.webservices.rest.groups;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import nl.strohalm.cyclos.access.AdminSystemPermission;
import nl.strohalm.cyclos.access.Permission;
import nl.strohalm.cyclos.annotations.Inject;
import nl.strohalm.cyclos.controls.ActionContext;
//import nl.strohalm.cyclos.controls.groups.ListGroupsForm;
import nl.strohalm.cyclos.entities.accounts.MemberAccountType;
import nl.strohalm.cyclos.entities.accounts.transactions.PaymentFilter;
import nl.strohalm.cyclos.entities.groups.AdminGroup;
import nl.strohalm.cyclos.entities.groups.Group;
import nl.strohalm.cyclos.entities.groups.GroupFilter;
import nl.strohalm.cyclos.entities.groups.GroupFilterQuery;
import nl.strohalm.cyclos.entities.groups.GroupQuery;
import nl.strohalm.cyclos.entities.groups.MemberGroup;
import nl.strohalm.cyclos.entities.members.Member;
import nl.strohalm.cyclos.services.groups.GroupFilterService;
import nl.strohalm.cyclos.services.groups.GroupService;
import nl.strohalm.cyclos.services.permissions.PermissionService;
import nl.strohalm.cyclos.utils.access.LoggedUser;
import nl.strohalm.cyclos.utils.binding.BeanBinder;
import nl.strohalm.cyclos.utils.binding.DataBinder;
import nl.strohalm.cyclos.utils.binding.PropertyBinder;
import nl.strohalm.cyclos.utils.query.QueryParameters;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;
import nl.strohalm.cyclos.webservices.rest.GenericResponse;

@Controller
public class ListGroupsController extends BaseRestController {

    public static Map<Group.Nature, Permission> getManageGroupPermissionByNatureMap() {
        final Map<Group.Nature, Permission> permissionByNature = new EnumMap<Group.Nature, Permission>(Group.Nature.class);
        permissionByNature.put(Group.Nature.ADMIN, AdminSystemPermission.GROUPS_MANAGE_ADMIN);
        permissionByNature.put(Group.Nature.BROKER, AdminSystemPermission.GROUPS_MANAGE_BROKER);
        permissionByNature.put(Group.Nature.MEMBER, AdminSystemPermission.GROUPS_MANAGE_MEMBER);

        return permissionByNature;
    }

    public static class GroupsResponse extends GenericResponse {

        private Map<Group.Nature, Permission> permissionByNature;
        private List<Group.Nature> natures;
        private List<GroupFilterEntity> groupFilters;
        private Collection<MemberGroup> memberGroups;
        private List<GroupEntity> groups;

        public List<GroupEntity> getGroups() {
            return groups;
        }

        public void setGroups(List<GroupEntity> groups) {
            this.groups = groups;
        }

        public Collection<MemberGroup> getMemberGroups() {
            return memberGroups;
        }

        public void setMemberGroups(Collection<MemberGroup> memberGroups) {
            this.memberGroups = memberGroups;
        }

        public List<GroupFilterEntity> getGroupFilters() {
            return groupFilters;
        }

        public void setGroupFilters(List<GroupFilterEntity> groupFilters) {
            this.groupFilters = groupFilters;
        }

        public List<Group.Nature> getNatures() {
            return natures;
        }

        public void setNatures(List<Group.Nature> natures) {
            this.natures = natures;
        }

        private boolean manageAnyGroup;

        public boolean isManageAnyGroup() {
            return manageAnyGroup;
        }

        public void setManageAnyGroup(boolean manageAnyGroup) {
            this.manageAnyGroup = manageAnyGroup;
        }

        public Map<Group.Nature, Permission> getPermissionByNature() {
            return permissionByNature;
        }

        public void setPermissionByNature(Map<Group.Nature, Permission> permissionByNature) {
            this.permissionByNature = permissionByNature;
        }

    }

    public static class GroupFilterEntity {

        private Long id;
        private String name;
        private String loginPageName;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getLoginPageName() {
            return loginPageName;
        }

        public void setLoginPageName(String loginPageName) {
            this.loginPageName = loginPageName;
        }

    }

    public static class GroupEntity {

        private Long id;
        private String name;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

    }

    @RequestMapping(value = "admin/listGroups", method = RequestMethod.GET)
    @ResponseBody
    public GroupsResponse listGroups() {
        GroupsResponse response = new GroupsResponse();
        boolean manageAnyGroup = false;
//        final GroupQuery groupQuery = getDataBinder().readFromString(form.getQuery());

        // Put in the request the name of permission used to manage a type of group
        final Map<Group.Nature, Permission> permissionByNature = getManageGroupPermissionByNatureMap();
        response.setPermissionByNature(permissionByNature);

        // Check if the user has permission to manage any group
        for (final Permission permission : permissionByNature.values()) {
            if (permissionService.hasPermission(permission)) {
                manageAnyGroup = true;
                break;
            }
        }

        // List of groups that the administrator can manage
        AdminGroup adminGroup = LoggedUser.group();
        adminGroup = groupService.load(adminGroup.getId(), AdminGroup.Relationships.MANAGES_GROUPS);
        //response.setMemberGroups(adminGroup.getManagesGroups());

        // List of group natures
        response.setNatures(Arrays.asList(Group.Nature.ADMIN, Group.Nature.BROKER, Group.Nature.MEMBER));

        // Search group filters and send to the JSP page
        final GroupFilterQuery groupFilterQuery = new GroupFilterQuery();
        groupFilterQuery.setAdminGroup(adminGroup);
        final Collection<GroupFilter> groupFiltersList = groupFilterService.search(groupFilterQuery);
        List<GroupFilterEntity> groupFilters = new ArrayList();
        for (GroupFilter group : groupFiltersList) {
            GroupFilterEntity groupEntity = new GroupFilterEntity();
            groupEntity.setId(group.getId());
            groupEntity.setName(group.getName());
            groupEntity.setLoginPageName(group.getLoginPageName());
            groupFilters.add(groupEntity);
        }
        System.out.println("------groupFilter: " + groupFiltersList);
        if (CollectionUtils.isNotEmpty(groupFilters)) {
            response.setGroupFilters(groupFilters);
        }

        //list all groups 
        final GroupQuery groupQuery = new GroupQuery();
        final List<? extends Group> groupsList = groupService.search(groupQuery);
        List<GroupEntity> groups = new ArrayList();
        for (Group group : groupsList) {
            GroupEntity entity = new GroupEntity();
            entity.setId(group.getId());
            entity.setName(group.getName());
            groups.add(entity);
        }
        response.setGroups(groups);

        return response;

    }

    public static class GroupParameters {

        private String nature;

        public String getNature() {
            return nature;
        }

        public void setNature(String nature) {
            this.nature = nature;
        }

    }

    @RequestMapping(value = "admin/listGroups", method = RequestMethod.POST)
    @ResponseBody
    public GroupsResponse listGroupsByNature(@RequestBody GroupParameters params) {
        GroupsResponse response = new GroupsResponse();
        final GroupQuery groupQuery = new GroupQuery();
        groupQuery.setNature(Group.Nature.valueOf(params.getNature()));
        final List<? extends Group> groupsList = groupService.search(groupQuery);
        List<GroupEntity> groups = new ArrayList();
        for (Group group : groupsList) {
            GroupEntity entity = new GroupEntity();
            entity.setId(group.getId());
            entity.setName(group.getName());
            groups.add(entity);
        }
        response.setGroups(groups);
        response.setStatus(0);
        return response;
    }
}
