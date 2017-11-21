/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.strohalm.cyclos.webservices.rest.members.bulk;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import nl.strohalm.cyclos.entities.access.Channel;
import nl.strohalm.cyclos.entities.customization.fields.MemberCustomField;
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
import nl.strohalm.cyclos.webservices.rest.members.SearchMembersController;
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
public class MemberBulkActionsController extends BaseRestController {

    public static class MemberBulkActionsResponse extends GenericResponse {

        private List<GroupEntity> groups;
        private List<MemberCustomField> fields;
        private Collection<ChannelEntity> channels;
        List<GroupFilterEntity> groupFilters;

        public List<GroupFilterEntity> getGroupFilters() {
            return groupFilters;
        }

        public void setGroupFilters(List<GroupFilterEntity> groupFilters) {
            this.groupFilters = groupFilters;
        }

        public Collection<ChannelEntity> getChannels() {
            return channels;
        }

        public void setChannels(Collection<ChannelEntity> channels) {
            this.channels = channels;
        }

        public List<MemberCustomField> getFields() {
            return fields;
        }

        public void setFields(List<MemberCustomField> fields) {
            this.fields = fields;
        }

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

    public static class ChannelEntity {

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

    @RequestMapping(value = "admin/memberBulkActions", method = RequestMethod.GET)
    @ResponseBody
    public MemberBulkActionsResponse prepareForm() {
        MemberBulkActionsResponse response = new MemberBulkActionsResponse();
        final List<MemberCustomField> fields = memberCustomFieldService.list();
        //response.setFields(fields);
//        request.setAttribute("customFields", customFieldHelper.buildEntries(fields, memberQuery.getCustomValues()));

        final GroupFilterQuery groupFilterQuery = new GroupFilterQuery();
        List<GroupEntity> groupList = new ArrayList();
        if (LoggedUser.isAdministrator()) {
            final AdminGroup adminGroup = LoggedUser.group();
            groupFilterQuery.setAdminGroup(adminGroup);
            final GroupQuery query = new GroupQuery();
            query.setNatures(Group.Nature.MEMBER, Group.Nature.BROKER);
            query.setStatus(Group.Status.NORMAL);
//        request.setAttribute("possibleNewGroups", groupService.search(query));
            List<? extends Group> groups = groupService.search(query);

            for (Group group : groups) {
                GroupEntity groupEntity = new GroupEntity();
                groupEntity.setId(group.getId());
                groupEntity.setName(group.getName());
                groupList.add(groupEntity);
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
        final Collection<Channel> channels = channelService.list();
        // The "web" channel can not be customized by the user, so it should not be sent to the JSP page
        final Channel webChannel = channelService.loadByInternalName(Channel.WEB);
        channels.remove(webChannel);
        List<ChannelEntity> channelList = new ArrayList();
        for (Channel channel : channels) {
            ChannelEntity channelEntity = new ChannelEntity();
            channelEntity.setId(channel.getId());
            channelEntity.setName(channel.getName());
            channelList.add(channelEntity);
        }
        response.setChannels(channelList);
        response.setGroups(groupList);
        response.setStatus(0);
        response.setMessage("prepare data for member bulk actions");
        return response;
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

    @RequestMapping(value = "admin/memberBulkActions", method = RequestMethod.POST)
    @ResponseBody
    public SearchMemberResponse handleSubmit(@RequestBody SearchMembersParameters params) {
        SearchMemberResponse response = new SearchMemberResponse();
        LocalSettings settings = settingsService.getLocalSettings();
        FullTextMemberQuery query = new FullTextMemberQuery();
        query.fetch(Member.Relationships.IMAGES);
      
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
        if (params.getBroker()!= null) {
            query.setBroker((Member) elementService.load(params.getBroker(), Element.Relationships.USER));
        }

        final List<? extends Element> list = elementService.fullTextSearch(query);
        System.out.println("-----" + list);
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

        private Long[] groupFilters;
        private Long[] groups;
        private Long broker;

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
