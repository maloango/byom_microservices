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
public class MemberBulkActionsController extends BaseRestController {

    public static class MemberBulkActionsResponse extends GenericResponse {

        private List<GroupEntity> groups;
        private List<MemberCustomField> fields;
        private Collection<ChannelEntity> channels;

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
    public static class ChannelEntity{
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
        final GroupQuery query = new GroupQuery();
        query.setNatures(Group.Nature.MEMBER, Group.Nature.BROKER);
        query.setStatus(Group.Status.NORMAL);
//        request.setAttribute("possibleNewGroups", groupService.search(query));
        List<? extends Group> groups = groupService.search(query);
        List<GroupEntity> groupList = new ArrayList();
        for (Group group : groups) {
            GroupEntity groupEntity = new GroupEntity();
            groupEntity.setId(group.getId());
            groupEntity.setName(group.getName());
            groupList.add(groupEntity);
        }
        
         final Collection<Channel> channels = channelService.list();
        // The "web" channel can not be customized by the user, so it should not be sent to the JSP page
        final Channel webChannel = channelService.loadByInternalName(Channel.WEB);
        channels.remove(webChannel);
        List<ChannelEntity>channelList=new ArrayList();
        for(Channel channel:channels){
            ChannelEntity channelEntity=new ChannelEntity();
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
}
