/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.strohalm.cyclos.webservices.rest.members.bulk;

import java.util.ArrayList;
import java.util.List;
import nl.strohalm.cyclos.entities.groups.Group;
import nl.strohalm.cyclos.entities.groups.GroupQuery;
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
public class MemberBulkChangeGroupController extends BaseRestController {

    public static class MemberBulkChangeResponse extends GenericResponse {

        List<GroupEntity> groups;

        public List<GroupEntity> getGroups() {
            return groups;
        }

        public void setGroups(List<GroupEntity> groups) {
            this.groups = groups;
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

    @RequestMapping(value = "admin/memberBulkChangeGroup", method = RequestMethod.GET)
    @ResponseBody
    public MemberBulkChangeResponse prepareForm() {
        MemberBulkChangeResponse response = new MemberBulkChangeResponse();
        final GroupQuery query = new GroupQuery();
        query.setNatures(Group.Nature.MEMBER, Group.Nature.BROKER);
        query.setStatus(Group.Status.NORMAL);
        List<? extends Group> possibleGroups = groupService.search(query);
        List<GroupEntity> groups = new ArrayList();
        for (Group group : possibleGroups) {
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
