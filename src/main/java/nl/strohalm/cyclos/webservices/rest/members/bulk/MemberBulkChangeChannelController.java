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
import nl.strohalm.cyclos.entities.groups.Group;
import nl.strohalm.cyclos.entities.groups.GroupFilter;
import nl.strohalm.cyclos.entities.members.Element;
import nl.strohalm.cyclos.entities.members.FullTextMemberQuery;
import nl.strohalm.cyclos.entities.members.Member;
import nl.strohalm.cyclos.services.elements.BulkMemberActionResultVO;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;
import nl.strohalm.cyclos.webservices.rest.GenericResponse;
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
public class MemberBulkChangeChannelController extends BaseRestController {

    public static class MemberBulkChangeChannelParameters {

        private Long[] groupFilters;
        private Long[] groups;
        private Long broker;
        private Long[] enableIds;
        private Long[] disableIds;

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

        public Long[] getEnableIds() {
            return enableIds;
        }

        public void setEnableIds(Long[] enableIds) {
            this.enableIds = enableIds;
        }

        public Long[] getDisableIds() {
            return disableIds;
        }

        public void setDisableIds(Long[] disableIds) {
            this.disableIds = disableIds;
        }

    }

    @RequestMapping(value = "admin/memberBulkChangeChannel", method = RequestMethod.POST)
    @ResponseBody
    public GenericResponse submit(@RequestBody MemberBulkChangeChannelParameters params) {
        GenericResponse response = new GenericResponse();
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
        List<Channel> enableIds = new ArrayList();
        for (Long l : params.getEnableIds()) {
            enableIds.add(channelService.load(l));
        }
        List<Channel> disableIds = new ArrayList();
        for (Long l : params.getDisableIds()) {
            disableIds.add(channelService.load(l));
        }

        final BulkMemberActionResultVO result = elementService.bulkChangeMemberChannels(query, enableIds, disableIds);
        if (result.getChanged() > 0 && result.getUnchanged() > 0) {
            response.setMessage("member.bulkActions.channelsChanged");
        } else if (result.getChanged() > 0) {
            response.setMessage("member.bulkActions.channelsChangedForAll");
        } else {
            response.setMessage("member.bulkActions.channelsNotChanged");
        }
        response.setStatus(0);
        return response;
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

    public static class MemberBulkChangeChannelResponse extends GenericResponse {

        List<ChannelEntity> channels;

        public List<ChannelEntity> getChannels() {
            return channels;
        }

        public void setChannels(List<ChannelEntity> channels) {
            this.channels = channels;
        }

    }

    @RequestMapping(value = "admin/memberBulkChangeChannel", method = RequestMethod.GET)
    @ResponseBody
    public MemberBulkChangeChannelResponse prepareForm() {
        MemberBulkChangeChannelResponse response = new MemberBulkChangeChannelResponse();
        final Collection<Channel> channels = channelService.list();
        // The "web" channel can not be customized by the user, so it should not be sent to the JSP page
        final Channel webChannel = channelService.loadByInternalName(Channel.WEB);
        channels.remove(webChannel);
        List<ChannelEntity> channelList = new ArrayList();
        for (Channel channel : channels) {
            ChannelEntity entity = new ChannelEntity();
            entity.setId(channel.getId());
            entity.setName(channel.getName());
            channelList.add(entity);
        }
        response.setChannels(channelList);
        response.setStatus(0);
        return response;
    }
}
