/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.strohalm.cyclos.webservices.rest.members;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import nl.strohalm.cyclos.access.AdminMemberPermission;
import nl.strohalm.cyclos.entities.groups.AdminGroup;
import nl.strohalm.cyclos.entities.groups.Group;
import nl.strohalm.cyclos.entities.groups.GroupFilter;
import nl.strohalm.cyclos.entities.groups.GroupFilterQuery;
import nl.strohalm.cyclos.entities.groups.GroupQuery;
import nl.strohalm.cyclos.entities.groups.MemberGroup;
import nl.strohalm.cyclos.entities.members.Element;
import nl.strohalm.cyclos.entities.members.FullTextMemberQuery;
import nl.strohalm.cyclos.entities.members.Member;
import nl.strohalm.cyclos.entities.members.Operator;
import nl.strohalm.cyclos.entities.settings.LocalSettings;
import nl.strohalm.cyclos.utils.Period;
import nl.strohalm.cyclos.utils.access.LoggedUser;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;
import nl.strohalm.cyclos.webservices.rest.GenericResponse;
import org.apache.commons.collections.CollectionUtils;
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
public class SearchMembersController extends BaseRestController {
    
    public static class MemberGroupEntity {
        
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
    
    public static class PossibleGroupEntity {
        
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
    
    public static class SearchMembersResponse extends GenericResponse {
        
        private List<MemberGroupEntity> memberGroups;
        private List<PossibleGroupEntity> possibleGroups;
        List<GroupFilterEntity> groupFilters;
        
        public List<GroupFilterEntity> getGroupFilters() {
            return groupFilters;
        }
        
        public void setGroupFilters(List<GroupFilterEntity> groupFilters) {
            this.groupFilters = groupFilters;
        }
        
        public List<PossibleGroupEntity> getPossibleGroups() {
            return possibleGroups;
        }
        
        public void setPossibleGroups(List<PossibleGroupEntity> possibleGroups) {
            this.possibleGroups = possibleGroups;
        }
        
        public List<MemberGroupEntity> getMemberGroups() {
            return memberGroups;
        }
        
        public void setMemberGroups(List<MemberGroupEntity> memberGroups) {
            this.memberGroups = memberGroups;
        }
        
    }
    
    @RequestMapping(value = "admin/searchMember", method = RequestMethod.GET)
    @ResponseBody
    public SearchMembersResponse prepareForm() throws Exception {
        
        SearchMembersResponse response = new SearchMembersResponse();
        // Retrieve the custom fields that will be used on the search
//        final List<MemberCustomField> fields = customFieldHelper.onlyForMemberSearch(memberCustomFieldService.list());
//        request.setAttribute("customFields", customFieldHelper.buildEntries(fields, memberQuery.getCustomValues()));

        final GroupFilterQuery groupFilterQuery = new GroupFilterQuery();
        if (LoggedUser.isAdministrator()) {
            final AdminGroup adminGroup = LoggedUser.group();
            groupFilterQuery.setAdminGroup(adminGroup);

            // Store the member groups for admins
            final GroupQuery groupQuery = new GroupQuery();
            groupQuery.setNatures(Group.Nature.MEMBER, Group.Nature.BROKER);
            if (!allowRemovedGroups()) {
                groupQuery.setStatus(Group.Status.NORMAL);
            }
            groupQuery.setManagedBy(adminGroup);
//            groupQuery.setGroupFilters(memberQuery.getGroupFilters());
            final List<MemberGroup> groups = (List<MemberGroup>) groupService.search(groupQuery);
            //  request.setAttribute("groups", groups);
            List<MemberGroupEntity> memberGroupList = new ArrayList();
            for (MemberGroup group : groups) {
                MemberGroupEntity groupEntity = new MemberGroupEntity();
                groupEntity.setId(group.getId());
                groupEntity.setName(group.getName());
                memberGroupList.add(groupEntity);
            }
            response.setMemberGroups(memberGroupList);

//            if (CollectionUtils.isEmpty(groups)) {
//                memberQuery.setGroups(groups);
//            }
            if (permissionService.hasPermission(AdminMemberPermission.MEMBERS_REGISTER)) {
                final Collection<MemberGroup> possibleNewGroups = new ArrayList<MemberGroup>();
                for (final MemberGroup memberGroup : groups) {
                    if (Group.Status.NORMAL.equals(memberGroup.getStatus())) {
                        possibleNewGroups.add(memberGroup);
                    }
                }
                // request.setAttribute("possibleNewGroups", possibleNewGroups);
                List<PossibleGroupEntity> possibleGroupList = new ArrayList();
                for (MemberGroup group : possibleNewGroups) {
                    PossibleGroupEntity possibleGroupEntity = new PossibleGroupEntity();
                    possibleGroupEntity.setId(group.getId());
                    possibleGroupEntity.setName(group.getName());
                    possibleGroupList.add(possibleGroupEntity);
                }
                response.setPossibleGroups(possibleGroupList);
            }
        } else {
            MemberGroup memberGroup;
            if (LoggedUser.isMember()) {
                memberGroup = LoggedUser.group();
            } else {
                final Operator operator = LoggedUser.element();
                memberGroup = operator.getMember().getMemberGroup();
            }
            groupFilterQuery.setViewableBy(memberGroup);
        }
        final Collection<GroupFilter> groupFilters = groupFilterService.search(groupFilterQuery);
        if (CollectionUtils.isNotEmpty(groupFilters)) {
            //request.setAttribute("groupFilters", groupFilters);
            List<GroupFilterEntity> groupFilterList = new ArrayList();
            for (GroupFilter filter : groupFilters) {
                GroupFilterEntity entity = new GroupFilterEntity();
                entity.setId(filter.getId());
                entity.setName(filter.getName());
                groupFilterList.add(entity);
            }
            response.setGroupFilters(groupFilterList);
            
        }
//
//        final Member broker = memberQuery.getBroker() == null ? null : (Member) elementService.load(memberQuery.getBroker().getId(), Element.Relationships.USER);
//        memberQuery.setBroker(broker);
//
//        return memberQuery;
        response.setStatus(0);
        return response;
        
    }
    
    protected boolean allowRemovedGroups() {
        return true;
    }
    
    @RequestMapping(value = "admin/searchMember", method = RequestMethod.POST)
    @ResponseBody
    public SearchMemberResponse handleSubmit(@RequestBody SearchMembersParameters params) {
        SearchMemberResponse response = new SearchMemberResponse();
        LocalSettings settings = settingsService.getLocalSettings();
        FullTextMemberQuery query = new FullTextMemberQuery();
        query.fetch(Member.Relationships.IMAGES);
        if (params.getKeywords() != null) {
            query.setKeywords(params.getKeywords());
        }
        Period period = new Period();
        period.setBegin(settings.getDateConverter().valueOf(params.getBegin()));
        period.setEnd(settings.getDateConverter().valueOf(params.getEnd()));
        query.setActivationPeriod(period);
        List<GroupFilter> groupFilters = new ArrayList();
        if (params.getGroupFilters() != null) {
            for (Long l : params.getGroupFilters()) {
                groupFilters.add(groupFilterService.load(l, GroupFilter.Relationships.GROUPS));
                
            }
        }
        query.setGroupFilters(groupFilters);
        List<Group> groups = new ArrayList();
        if (params.getGroups() != null) {
            for (Long group : params.getGroups()) {
                groups.add(groupService.load(group, Group.Relationships.GROUP_FILTERS));
            }
        }
        query.setGroups(groups);
        if (params.getBroker() != null) {
            query.setBroker((Member) elementService.load(params.getBroker(), Element.Relationships.USER));
        }
        
        final List<? extends Element> list = elementService.fullTextSearch(query);
        System.out.println("-----"+list);
        List<MemberEntity> members = new ArrayList();
        Member member = null;
        for (Element e : list) {
            member = (Member) e;
            MemberEntity entity = new MemberEntity();
            entity.setId(member.getId());
            entity.setName(member.getName());
            entity.setUsername(member.getUsername());
            members.add(entity);
        }
        
        response.setMembers(members);
        response.setStatus(0);
        return response;
        
    }
    
    public static class SearchMembersParameters {
        
        private String keywords;
        private Long[] groupFilters;
        private Long[] groups;
        private Long broker;
        private String begin;
        private String end;
        
        public String getKeywords() {
            return keywords;
        }
        
        public void setKeywords(String keywords) {
            this.keywords = keywords;
        }
        
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
        
        public String getBegin() {
            return begin;
        }
        
        public void setBegin(String begin) {
            this.begin = begin;
        }
        
        public String getEnd() {
            return end;
        }
        
        public void setEnd(String end) {
            this.end = end;
        }
        
    }
    
    public static class SearchMemberResponse extends GenericResponse {
        
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
        private String username;
        
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
        
        public String getUsername() {
            return username;
        }
        
        public void setUsername(String username) {
            this.username = username;
        }
        
    }
    
}
