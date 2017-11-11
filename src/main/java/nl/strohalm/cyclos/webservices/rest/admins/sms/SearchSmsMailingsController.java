/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.strohalm.cyclos.webservices.rest.admins.sms;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import nl.strohalm.cyclos.access.AdminMemberPermission;
import nl.strohalm.cyclos.access.BrokerPermission;
import nl.strohalm.cyclos.entities.groups.AdminGroup;
import nl.strohalm.cyclos.entities.groups.Group;
import nl.strohalm.cyclos.entities.groups.GroupQuery;
import nl.strohalm.cyclos.entities.groups.MemberGroup;
import nl.strohalm.cyclos.entities.members.Element;
import nl.strohalm.cyclos.entities.members.Member;
import nl.strohalm.cyclos.entities.settings.LocalSettings;
import nl.strohalm.cyclos.entities.sms.SmsMailing;
import nl.strohalm.cyclos.entities.sms.SmsMailingQuery;
import nl.strohalm.cyclos.utils.Period;
import nl.strohalm.cyclos.utils.access.LoggedUser;
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
public class SearchSmsMailingsController extends BaseRestController {

    public static class MailingDataRespone extends GenericResponse {

        private List<GroupEntity> groupList;
        private boolean viewFree;
        private boolean viewPaid;
        private boolean canSend;

        public boolean isViewFree() {
            return viewFree;
        }

        public void setViewFree(boolean viewFree) {
            this.viewFree = viewFree;
        }

        public boolean isViewPaid() {
            return viewPaid;
        }

        public void setViewPaid(boolean viewPaid) {
            this.viewPaid = viewPaid;
        }

        public boolean isCanSend() {
            return canSend;
        }

        public void setCanSend(boolean canSend) {
            this.canSend = canSend;
        }

        public List<GroupEntity> getGroupList() {
            return groupList;
        }

        public void setGroupList(List<GroupEntity> groupList) {
            this.groupList = groupList;
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

    @RequestMapping(value = "admin/searchSmsMailings", method = RequestMethod.GET)
    @ResponseBody
    public MailingDataRespone prepareForm() {
        MailingDataRespone response = new MailingDataRespone();
        boolean viewFree;
        boolean viewPaid;
        boolean canSend;
        if (LoggedUser.isAdministrator()) {
            viewPaid = viewFree = permissionService.hasPermission(AdminMemberPermission.SMS_MAILINGS_VIEW); // 2 assignments
            canSend = permissionService.hasPermission(AdminMemberPermission.SMS_MAILINGS_FREE_SMS_MAILINGS) || permissionService.hasPermission(AdminMemberPermission.SMS_MAILINGS_PAID_SMS_MAILINGS);

            final GroupQuery groupQuery = new GroupQuery();
            groupQuery.setManagedBy((AdminGroup) LoggedUser.group());
            groupQuery.setOnlyActive(true);
            List<GroupEntity> groupList = new ArrayList();
            for (Group group : groupService.search(groupQuery)) {
                GroupEntity entity = new GroupEntity();
                entity.setId(group.getId());
                entity.setName(group.getName());
                groupList.add(entity);
            }
            response.setGroupList(groupList);
        } else {
            viewFree = permissionService.hasPermission(BrokerPermission.SMS_MAILINGS_FREE_SMS_MAILINGS);
            viewPaid = permissionService.hasPermission(BrokerPermission.SMS_MAILINGS_PAID_SMS_MAILINGS);
            canSend = viewFree || viewPaid; // At least one permission (free / paid) the broker has
        }
        // Ensure to fetch the member, so the name / username will be displayed, if one is selected
//        if (query.getMember() != null) {
//            query.setMember((Member) elementService.load(query.getMember().getId(), Element.Relationships.USER));
//        }
        response.setViewFree(viewFree);
        response.setViewPaid(viewPaid);
        response.setCanSend(canSend);
        response.setStatus(0);
        return response;
    }

    public static class SmsMailParameters {

        private String recipient;
        private Long member;
        private Long group;
        private String begin;
        private String end;

        public String getRecipient() {
            return recipient;
        }

        public void setRecipient(String recipient) {
            this.recipient = recipient;
        }

        public Long getMember() {
            return member;
        }

        public void setMember(Long member) {
            this.member = member;
        }

        public Long getGroup() {
            return group;
        }

        public void setGroup(Long group) {
            this.group = group;
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

    public static class SearchMailingsResposne extends GenericResponse {

        private List<SearchMailingEntity> mailingList;

        public List<SearchMailingEntity> getMailingList() {
            return mailingList;
        }

        public void setMailingList(List<SearchMailingEntity> mailingList) {
            this.mailingList = mailingList;
        }

    }

    public static class SearchMailingEntity {

        private Calendar date;
        private String by;
        private String text;
        private int sentSms;
        private boolean free;
        private Collection<String> groups;

        public Calendar getDate() {
            return date;
        }

        public void setDate(Calendar date) {
            this.date = date;
        }

        public String getBy() {
            return by;
        }

        public void setBy(String by) {
            this.by = by;
        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }

        public int getSentSms() {
            return sentSms;
        }

        public void setSentSms(int sentSms) {
            this.sentSms = sentSms;
        }

        public boolean isFree() {
            return free;
        }

        public void setFree(boolean free) {
            this.free = free;
        }

        public Collection<String> getGroups() {
            return groups;
        }

        public void setGroups(Collection<String> groups) {
            this.groups = groups;
        }
    }

    @RequestMapping(value = "admin/searchSmsMailings", method = RequestMethod.POST)
    @ResponseBody
    public SearchMailingsResposne searchMail(@RequestBody SmsMailParameters params) {
        SearchMailingsResposne response = new SearchMailingsResposne();
        final LocalSettings settings = settingsService.getLocalSettings();
        final SmsMailingQuery query = new SmsMailingQuery();

        if (params.getRecipient() != null) {
            query.setRecipient(SmsMailingQuery.Recipient.valueOf(params.getRecipient()));
        }
        query.setMember((Member) elementService.load(params.getMember(), Element.Relationships.USER));
        query.setGroup((MemberGroup) groupService.load(params.getGroup(), Group.Relationships.PERMISSIONS));
        
        Period period = new Period();
        period.setBegin(settings.getDateConverter().valueOf(params.getBegin()));
        period.setEnd(settings.getDateConverter().valueOf(params.getEnd()));
        query.setPeriod(period);
        query.fetch(SmsMailing.Relationships.BY, SmsMailing.Relationships.GROUPS);
        final List<SmsMailing> smsMailings = smsMailingService.search(query);
        List<SearchMailingEntity> miailingList = new ArrayList();
       
        for (SmsMailing sms : smsMailings) {
            List<String> groups = new ArrayList();
            SearchMailingEntity smsEntity = new SearchMailingEntity();
            smsEntity.setDate(sms.getDate());
            smsEntity.setBy(sms.getBy().getName());
            smsEntity.setFree(sms.isFree());
            smsEntity.setSentSms(sms.getSentSms());
            smsEntity.setText(sms.getText());
            for (MemberGroup group : sms.getGroups()) {
                groups.add(group.getName());
            }
            smsEntity.setGroups(groups);
            miailingList.add(smsEntity);

        }
        response.setMailingList(miailingList);
        response.setStatus(0);
        return response;
    }
}
