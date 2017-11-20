/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.strohalm.cyclos.webservices.rest.members.bulk;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import nl.strohalm.cyclos.entities.groups.Group;
import nl.strohalm.cyclos.entities.groups.GroupFilter;
import nl.strohalm.cyclos.entities.groups.GroupQuery;
import nl.strohalm.cyclos.entities.groups.MemberGroup;
import nl.strohalm.cyclos.entities.members.Element;
import nl.strohalm.cyclos.entities.members.FullTextMemberQuery;
import nl.strohalm.cyclos.entities.members.Member;
import nl.strohalm.cyclos.services.elements.BulkMemberActionResultVO;
import nl.strohalm.cyclos.utils.binding.MapBean;
import nl.strohalm.cyclos.utils.conversion.CoercionHelper;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;
import nl.strohalm.cyclos.webservices.rest.GenericResponse;
import org.apache.commons.lang.StringUtils;
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
    
    public static class MemberBulkChangeParameters {
        
        private Long[] groupFilters;
        private Long[] groups;
        private Long broker;
        private Long newGroup;
        private String comments;
        
        public Long[] getGroupFilters() {
            return groupFilters;
        }
        
        public void setGroupFilters(Long[] groupFilters) {
            this.groupFilters = groupFilters;
        }
        
        public Long[] getGroups() {
            return groups;
        }
        
        public void setGroups(Long[] groups) {
            this.groups = groups;
        }
        
        public Long getBroker() {
            return broker;
        }
        
        public void setBroker(Long broker) {
            this.broker = broker;
        }
        
        public Long getNewGroup() {
            return newGroup;
        }
        
        public void setNewGroup(Long newGroup) {
            this.newGroup = newGroup;
        }
        
        public String getComments() {
            return comments;
        }
        
        public void setComments(String comments) {
            this.comments = comments;
        }
        
    }
    
    @RequestMapping(value = "admin/memberBulkChangeGroup", method = RequestMethod.POST)
    @ResponseBody
    public GenericResponse submit(@RequestBody MemberBulkChangeParameters params) {
        GenericResponse response = new GenericResponse();
        final MapBean bean = new MapBean();
        final FullTextMemberQuery query = new FullTextMemberQuery();
        List<GroupFilter> groupFilters = new ArrayList();
        for (Long l : params.getGroupFilters()) {
            groupFilters.add(groupFilterService.load(l, GroupFilter.Relationships.GROUPS));
            
        }
        query.setGroupFilters(groupFilters);
        List<Group> groups = new ArrayList();
        for (Long group : params.getGroups()) {
            groups.add(groupService.load(group, Group.Relationships.GROUP_FILTERS));
        }
        query.setGroups(groups);
        query.setBroker((Member) elementService.load(params.getBroker(), Element.Relationships.USER));
        final MemberGroup newGroup = groupService.load(CoercionHelper.coerce(Long.class, params.getNewGroup()));
        final String comments = params.getComments();
        final BulkMemberActionResultVO results = elementService.bulkChangeMemberGroup(query, newGroup, comments);
        response.setMessage("member.bulkActions.groupChanged");
        response.setStatus(0);
        return response;
    }
    
}
