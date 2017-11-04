package nl.strohalm.cyclos.webservices.rest.groups.groupFilters;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import nl.strohalm.cyclos.entities.groups.Group;
import nl.strohalm.cyclos.entities.groups.GroupFilter;
import nl.strohalm.cyclos.entities.groups.GroupQuery;
import nl.strohalm.cyclos.entities.groups.MemberGroup;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;
import nl.strohalm.cyclos.webservices.rest.GenericResponse;
import org.springframework.web.bind.annotation.RequestBody;

@Controller
public class EditGroupFilterController extends BaseRestController {

    public static class GroupFilterResponse extends GenericResponse {

        private List<GroupFilterEntity> groupList;
        private List<GroupFilterEntity> viewableByList;
        private boolean canManageCustomizedFiles;

        public boolean isCanManageCustomizedFiles() {
            return canManageCustomizedFiles;
        }

        public void setCanManageCustomizedFiles(boolean canManageCustomizedFiles) {
            this.canManageCustomizedFiles = canManageCustomizedFiles;
        }

        public List<GroupFilterEntity> getGroupList() {
            return groupList;
        }

        public void setGroupList(List<GroupFilterEntity> groupList) {
            this.groupList = groupList;
        }

        public List<GroupFilterEntity> getViewableByList() {
            return viewableByList;
        }

        public void setViewableByList(List<GroupFilterEntity> viewableByList) {
            this.viewableByList = viewableByList;
        }

    }

    public static class GroupFilterEntity {

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

    @RequestMapping(value = "admin/editGroupFilter", method = RequestMethod.GET)
    @ResponseBody
    public GroupFilterResponse prepareForm() throws Exception {
        GroupFilterResponse response = new GroupFilterResponse();
//        final long id = form.getGroupFilterId();
//        final boolean isInsert = (id <= 0L);
//        if (!isInsert) {
//            final GroupFilter groupFilter = groupFilterService.load(id, GroupFilter.Relationships.GROUPS, GroupFilter.Relationships.VIEWABLE_BY, GroupFilter.Relationships.CUSTOMIZED_FILES);
//            request.setAttribute("groupFilter", groupFilter);
//            getDataBinder().writeAsString(form.getGroupFilter(), groupFilter);
//
//            // Retrieve the associated customized files
//            final CustomizedFileQuery cfQuery = new CustomizedFileQuery();
//            cfQuery.setGroupFilter(groupFilter);
//            request.setAttribute("customizedFiles", customizedFileService.search(cfQuery));
//        }
        // Get the groups that can belong to this group filter
        final GroupQuery query = new GroupQuery();
        query.setNatures(Group.Nature.MEMBER, Group.Nature.BROKER);
        final Collection<MemberGroup> groups = (Collection<MemberGroup>) groupService.search(query);
        List<GroupFilterEntity> groupList = new ArrayList();
        for (MemberGroup memberGroup : groups) {
            GroupFilterEntity entity = new GroupFilterEntity();
            entity.setId(memberGroup.getId());
            entity.setName(memberGroup.getName());
            groupList.add(entity);
        }
        // Get the groups that can view this group filter
        final Collection<MemberGroup> viewableBy = groups;
        List<GroupFilterEntity> viewableList = new ArrayList();
        for (MemberGroup memberGroup : groups) {
            GroupFilterEntity entity = new GroupFilterEntity();
            entity.setId(memberGroup.getId());
            entity.setName(memberGroup.getName());
            viewableList.add(entity);
        }

        response.setGroupList(groupList);
        response.setViewableByList(viewableList);
        response.setCanManageCustomizedFiles(customizedFileService.canViewOrManageInGroupFilters());
        response.setStatus(0);
        response.setMessage("");
        return response;

    }

    public static class GroupFilterParameters {

        private Long id;
        private String name;
        private String rootUrl;
        private String loginPageName;
        private String containerUrl;
        private String description;
        private boolean showInProfile;
        private List<Long> groups;
        private List<Long> viewableBy;

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

        public String getRootUrl() {
            return rootUrl;
        }

        public void setRootUrl(String rootUrl) {
            this.rootUrl = rootUrl;
        }

        public String getLoginPageName() {
            return loginPageName;
        }

        public void setLoginPageName(String loginPageName) {
            this.loginPageName = loginPageName;
        }

        public String getContainerUrl() {
            return containerUrl;
        }

        public void setContainerUrl(String containerUrl) {
            this.containerUrl = containerUrl;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public boolean isShowInProfile() {
            return showInProfile;
        }

        public void setShowInProfile(boolean showInProfile) {
            this.showInProfile = showInProfile;
        }

        public List<Long> getGroups() {
            return groups;
        }

        public void setGroups(List<Long> groups) {
            this.groups = groups;
        }

        public List<Long> getViewableBy() {
            return viewableBy;
        }

        public void setViewableBy(List<Long> viewableBy) {
            this.viewableBy = viewableBy;
        }

    }

    @RequestMapping(value = "admin/editGroupFilter", method = RequestMethod.POST)
    @ResponseBody
    public GenericResponse editGroupFilter(@RequestBody GroupFilterParameters params) {
        GenericResponse response = new GenericResponse();
        GroupFilter groupFilter = new GroupFilter();
        if (params.getId() != null && params.getId() > 0L) {
            groupFilter.setId(params.getId());
        }
        groupFilter.setName(params.getName());
        groupFilter.setContainerUrl(params.getContainerUrl());
        groupFilter.setDescription(params.getDescription());
        groupFilter.setLoginPageName(params.getLoginPageName());
        groupFilter.setRootUrl(params.getRootUrl());
       // groupFilter.setGroups((Collection<MemberGroup>)groupService.load(params.getGroups(),Group.Relationships.ELEMENTS)));
       // groupFilter.setViewableBy((Collection<MemberGroup>)groupService.load(params.getViewableBy(), MemberGroup.Relationships.CAN_VIEW_GROUP_FILTERS));
        final boolean isInsert = (groupFilter.getId() == null);
        groupFilter = groupFilterService.save(groupFilter);
        response.setMessage(isInsert ? "groupFilter.inserted" : "groupFilter.modified");
        response.setStatus(0);
        return response;

    }

}
