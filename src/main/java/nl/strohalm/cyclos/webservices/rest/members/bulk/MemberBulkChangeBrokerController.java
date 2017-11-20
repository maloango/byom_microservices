/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.strohalm.cyclos.webservices.rest.members.bulk;

import java.util.ArrayList;
import java.util.List;
import nl.strohalm.cyclos.entities.groups.Group;
import nl.strohalm.cyclos.entities.groups.GroupFilter;
import nl.strohalm.cyclos.entities.members.Element;
import nl.strohalm.cyclos.entities.members.FullTextMemberQuery;
import nl.strohalm.cyclos.entities.members.Member;
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
public class MemberBulkChangeBrokerController extends BaseRestController {

    public static class MemberBulkChangeBrokerParameters {

        private Long[] groupFilters;
        private Long[] groups;
        private Long broker;
        private Long newBroker;
        private boolean suspendCommission;
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

        public Long getNewBroker() {
            return newBroker;
        }

        public void setNewBroker(Long newBroker) {
            this.newBroker = newBroker;
        }

        public boolean isSuspendCommission() {
            return suspendCommission;
        }

        public void setSuspendCommission(boolean suspendCommission) {
            this.suspendCommission = suspendCommission;
        }

        public String getComments() {
            return comments;
        }

        public void setComments(String comments) {
            this.comments = comments;
        }

    }

    @RequestMapping(value = "admin/memberBulkChangeBroker", method = RequestMethod.POST)
    @ResponseBody
    public GenericResponse submit(@RequestBody MemberBulkChangeBrokerParameters params) {
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
        final Member newBroker = elementService.load(CoercionHelper.coerce(Long.class, params.getNewBroker()));
        final boolean suspendCommission = params.isSuspendCommission();
        final String comments = params.getComments();
        brokeringService.bulkChangeMemberBroker(query, newBroker, suspendCommission, comments);
        response.setMessage("member.bulkActions.brokerChanged");
        response.setStatus(0);
        return response;
    }
}
