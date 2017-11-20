package nl.strohalm.cyclos.webservices.rest.admins;

import java.util.ArrayList;
import java.util.List;
import nl.strohalm.cyclos.access.AdminAdminPermission;
import nl.strohalm.cyclos.entities.groups.Group;
import nl.strohalm.cyclos.entities.groups.GroupQuery;
import nl.strohalm.cyclos.entities.members.Element;
import nl.strohalm.cyclos.entities.members.FullTextMemberQuery;
import nl.strohalm.cyclos.entities.members.Member;
import org.springframework.stereotype.Controller;

import nl.strohalm.cyclos.webservices.rest.BaseRestController;
import nl.strohalm.cyclos.webservices.rest.GenericResponse;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class SearchAdminsController extends BaseRestController {
    
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
    
    public static class SearchAdminsResponse extends GenericResponse {
        
        private List<GroupEntity> groups;
        private List<GroupEntity> possibleNewGroups;
        
        public List<GroupEntity> getGroups() {
            return groups;
        }
        
        public void setGroups(List<GroupEntity> groups) {
            this.groups = groups;
        }
        
        public List<GroupEntity> getPossibleNewGroups() {
            return possibleNewGroups;
        }
        
        public void setPossibleNewGroups(List<GroupEntity> possibleNewGroups) {
            this.possibleNewGroups = possibleNewGroups;
        }
        
    }
    
    @RequestMapping(value = "admin/searchAdmins", method = RequestMethod.GET)
    @ResponseBody
    public SearchAdminsResponse prepareForm() {
        SearchAdminsResponse response = new SearchAdminsResponse();
        // Store the groups
        final GroupQuery groupQuery = new GroupQuery();
        groupQuery.setNatures(Group.Nature.ADMIN);
        List<GroupEntity> groups = new ArrayList();
        for (Group group : groupService.search(groupQuery)) {
            GroupEntity entity = new GroupEntity();
            entity.setId(group.getId());
            entity.setName(group.getName());
            groups.add(entity);
        }
        response.setGroups(groups);

        // Store the possible groups for new admin
        if (permissionService.hasPermission(AdminAdminPermission.ADMINS_REGISTER)) {
            final GroupQuery possibleGroupQuery = new GroupQuery();
            possibleGroupQuery.setNatures(Group.Nature.ADMIN);
            possibleGroupQuery.setStatus(Group.Status.NORMAL);
            
            List<GroupEntity> possibleNewGroups = new ArrayList();
            for (Group group : groupService.search(possibleGroupQuery)) {
                GroupEntity entity = new GroupEntity();
                entity.setId(group.getId());
                entity.setName(group.getName());
                possibleNewGroups.add(entity);
            }
            response.setPossibleNewGroups(possibleNewGroups);
        }
        response.setStatus(0);
        return response;
    }
    
    public static class SearchAdminsParameters {
        
        private String keywords;
        private Long[] groups;
        
        public String getKeywords() {
            return keywords;
        }
        
        public void setKeywords(String keywords) {
            this.keywords = keywords;
        }
        
        public Long[] getGroups() {
            return groups;
        }
        
        public void setGroups(Long[] groups) {
            this.groups = groups;
        }
        
    }
    
    public static class SearchAdminResponse extends GenericResponse {
        
        private List<MemberEntity> members;
        
        public List<MemberEntity> getMembers() {
            return members;
        }
        
        public void setMembers(List<MemberEntity> members) {
            this.members = members;
        }
        
    }
    
    public static class MemberEntity {
        
        private Long id;
        private String name;
        private String userName;
        
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
        
        public String getUserName() {
            return userName;
        }
        
        public void setUserName(String userName) {
            this.userName = userName;
        }
        
    }
    
    @RequestMapping(value = "admin/searchAdmins", method = RequestMethod.POST)
    @ResponseBody
    public SearchAdminResponse Search(@RequestBody SearchAdminsParameters params) {
        SearchAdminResponse response = new SearchAdminResponse();
        FullTextMemberQuery query = new FullTextMemberQuery();
        query.setKeywords(params.getKeywords());
        List<Group> groups = new ArrayList();
        for (Long l : params.getGroups()) {
            groups.add(groupService.load(l, Group.Relationships.GROUP_FILTERS));
        }
        final List<? extends Element> list = elementService.fullTextSearch(query);
        List<MemberEntity> members = new ArrayList();
        Member member = null;
        for (Element e : list) {
            member = (Member) e;
            MemberEntity entity = new MemberEntity();
            entity.setId(member.getId());
            entity.setName(member.getName());
            entity.setUserName(member.getUsername());
            members.add(entity);
        }
        response.setMembers(members);
        response.setStatus(0);
        return response;
    }
}
