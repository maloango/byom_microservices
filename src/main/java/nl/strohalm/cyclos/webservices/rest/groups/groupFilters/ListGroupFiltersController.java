package nl.strohalm.cyclos.webservices.rest.groups.groupFilters;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import nl.strohalm.cyclos.annotations.Inject;
import nl.strohalm.cyclos.entities.customization.files.CustomizedFile;
import nl.strohalm.cyclos.entities.groups.GroupFilter;
import nl.strohalm.cyclos.entities.groups.GroupFilterQuery;
import nl.strohalm.cyclos.entities.groups.MemberGroup;
import nl.strohalm.cyclos.services.groups.GroupFilterService;
import nl.strohalm.cyclos.utils.query.QueryParameters;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;
import nl.strohalm.cyclos.webservices.rest.GenericResponse;

@Controller
public class ListGroupFiltersController extends BaseRestController {

    public static class ListGroupResponse extends GenericResponse {

        List<GroupFilterEntity> groupFilterList;

        public List<GroupFilterEntity> getGroupFilterList() {
            return groupFilterList;
        }

        public void setGroupFilterList(List<GroupFilterEntity> groupFilterList) {
            this.groupFilterList = groupFilterList;
        }

    }

    public static class GroupFilterEntity {

        private String name;
        private String description;
        private String loginPageName;
        private String rootUrl;
        private String containerUrl;
        private boolean showInProfile;
        private Long id;

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

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getLoginPageName() {
            return loginPageName;
        }

        public void setLoginPageName(String loginPageName) {
            this.loginPageName = loginPageName;
        }

        public String getRootUrl() {
            return rootUrl;
        }

        public void setRootUrl(String rootUrl) {
            this.rootUrl = rootUrl;
        }

        public String getContainerUrl() {
            return containerUrl;
        }

        public void setContainerUrl(String containerUrl) {
            this.containerUrl = containerUrl;
        }

        public boolean isShowInProfile() {
            return showInProfile;
        }

        public void setShowInProfile(boolean showInProfile) {
            this.showInProfile = showInProfile;
        }
    }

    @RequestMapping(value = "admin/listGroupFilters", method = RequestMethod.GET)
    @ResponseBody
    public ListGroupResponse executeAction() throws Exception {

        ListGroupResponse response = new ListGroupResponse();
        GroupFilterQuery query = new GroupFilterQuery();
        final List<GroupFilter> groupFilters = groupFilterService.search(query);
        List<GroupFilterEntity> groupFilterList = new ArrayList();
        System.out.println("---------groupFilter: " + groupFilters);
        for (GroupFilter group : groupFilters) {
            GroupFilterEntity entity = new GroupFilterEntity();
            entity.setId(group.getId());
            entity.setName(group.getName());
            entity.setContainerUrl(group.getContainerUrl());
            entity.setDescription(group.getDescription());
            entity.setRootUrl(group.getRootUrl());
            entity.setShowInProfile(group.isShowInProfile());
            entity.setLoginPageName(group.getLoginPageName());
            groupFilterList.add(entity);

        }
        response.setGroupFilterList(groupFilterList);
        response.setStatus(0);
        response.setMessage("");
        return response;
    }
}
