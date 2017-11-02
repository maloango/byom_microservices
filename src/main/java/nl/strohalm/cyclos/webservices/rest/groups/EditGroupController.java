package nl.strohalm.cyclos.webservices.rest.groups;

import java.util.ArrayList;
import java.util.Arrays;

import java.util.EnumMap;
import java.util.List;
import nl.strohalm.cyclos.entities.groups.AdminGroup;
import nl.strohalm.cyclos.entities.groups.Group;
import nl.strohalm.cyclos.entities.groups.GroupQuery;
import nl.strohalm.cyclos.utils.access.LoggedUser;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;
import nl.strohalm.cyclos.webservices.rest.GenericResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class EditGroupController extends BaseRestController {

    public static class GroupResposne extends GenericResponse {

        private List<Group.Nature> natures;
        private List<GroupEntity> groups;

        public List<GroupEntity> getGroups() {
            return groups;
        }

        public void setGroups(List<GroupEntity> groups) {
            this.groups = groups;
        }

        public List<Group.Nature> getNatures() {
            return natures;
        }

        public void setNatures(List<Group.Nature> natures) {
            this.natures = natures;
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

    @RequestMapping(value = "admin/editGroup", method = RequestMethod.GET)
    @ResponseBody
    public GroupResposne prepareForm() {
        GroupResposne response = new GroupResposne();
        // List of groups that the administrator can manage
        AdminGroup adminGroup = LoggedUser.group();
        adminGroup = groupService.load(adminGroup.getId(), AdminGroup.Relationships.MANAGES_GROUPS);
        //response.setMemberGroups(adminGroup.getManagesGroups());

        // List of group natures
        response.setNatures(Arrays.asList(Group.Nature.ADMIN, Group.Nature.BROKER, Group.Nature.MEMBER));
        response.setStatus(0);
        return response;

    }

    @RequestMapping(value = "admin/findGroupByNature/{nature}", method = RequestMethod.GET)
    @ResponseBody
    public GroupResposne prepareForm(@PathVariable("nature") String nature) {
        GroupResposne response = new GroupResposne();
        Group.Nature n=Group.Nature.valueOf(nature);
        final GroupQuery query = new GroupQuery();
        if (n.equals(Group.Nature.ADMIN)) {
            query.setNature(Group.Nature.ADMIN);
        } else if (n.equals(Group.Nature.MEMBER)) {
            query.setNature(Group.Nature.MEMBER);
        } else if (n.equals(Group.Nature.BROKER)) {
            query.setNature(Group.Nature.BROKER);
        }
        final List<? extends Group> groupsList = groupService.search(query);
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
