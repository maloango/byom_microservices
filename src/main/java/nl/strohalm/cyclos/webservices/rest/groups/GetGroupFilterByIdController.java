/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.strohalm.cyclos.webservices.rest.groups;

import java.util.List;
import nl.strohalm.cyclos.entities.groups.GroupFilter;
import nl.strohalm.cyclos.entities.groups.GroupFilterQuery;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;
import nl.strohalm.cyclos.webservices.rest.GenericResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 *
 * @author Lue Infoservices
 */
@Controller
public class GetGroupFilterByIdController extends BaseRestController {

    public static class GetGroupResponse extends GenericResponse {

        private GroupFilterEntity groupFilter;

        public GroupFilterEntity getGroupFilter() {
            return groupFilter;
        }

        public void setGroupFilter(GroupFilterEntity groupFilter) {
            this.groupFilter = groupFilter;
        }

    }

    public static class GroupFilterEntity {

        private Long id;
        private String name;
        private String description;
        private String loginPageName;
        private String rootUrl;
        private String containerUrl;
        private boolean showInProfile;
//    private List<MemberGroup>    groups;
//    private Collection<MemberGroup>    viewableBy;

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

    @RequestMapping(value = "admin/getGroupFilterById/{id}", method = RequestMethod.GET)
    @ResponseBody
    public GetGroupResponse getGroup(@PathVariable("id") Long id) {
        GetGroupResponse response = new GetGroupResponse();
        GroupFilter groupFilter = groupFilterService.load(id, GroupFilter.Relationships.GROUPS);
        GroupFilterEntity entity = new GroupFilterEntity();
        entity.setId(groupFilter.getId());
        entity.setContainerUrl(groupFilter.getContainerUrl());
        entity.setDescription(groupFilter.getDescription());
        entity.setLoginPageName(groupFilter.getLoginPageName());
        entity.setName(groupFilter.getName());
        entity.setRootUrl(groupFilter.getRootUrl());
        entity.setShowInProfile(groupFilter.isShowInProfile());

        response.setGroupFilter(entity);
        response.setStatus(0);
        return response;
    }
}
