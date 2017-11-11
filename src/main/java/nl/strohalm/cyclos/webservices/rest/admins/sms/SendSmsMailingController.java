/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.strohalm.cyclos.webservices.rest.admins.sms;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import nl.strohalm.cyclos.access.AdminMemberPermission;
import nl.strohalm.cyclos.access.BrokerPermission;
import nl.strohalm.cyclos.access.Permission;
import nl.strohalm.cyclos.entities.groups.AdminGroup;
import nl.strohalm.cyclos.entities.groups.Group;
import nl.strohalm.cyclos.entities.groups.GroupQuery;
import nl.strohalm.cyclos.entities.groups.MemberGroup;
import nl.strohalm.cyclos.entities.members.Element;
import nl.strohalm.cyclos.entities.members.Member;
import nl.strohalm.cyclos.entities.sms.SmsMailing;
import nl.strohalm.cyclos.exceptions.PermissionDeniedException;
import nl.strohalm.cyclos.utils.EntityHelper;
import nl.strohalm.cyclos.utils.access.LoggedUser;
import nl.strohalm.cyclos.utils.binding.BeanBinder;
import nl.strohalm.cyclos.utils.binding.DataBinder;
import nl.strohalm.cyclos.utils.binding.PropertyBinder;
import nl.strohalm.cyclos.utils.binding.SimpleCollectionBinder;
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
public class SendSmsMailingController extends BaseRestController {

    private DataBinder<SmsMailing> dataBinder;

    public static class MailingDataRespone extends GenericResponse {

        private List<GroupEntity> groupList;
        private boolean canSendFree;
        private boolean canSendPaid;
        private boolean free;

        public boolean isFree() {
            return free;
        }

        public void setFree(boolean free) {
            this.free = free;
        }

        public boolean isCanSendFree() {
            return canSendFree;
        }

        public void setCanSendFree(boolean canSendFree) {
            this.canSendFree = canSendFree;
        }

        public boolean isCanSendPaid() {
            return canSendPaid;
        }

        public void setCanSendPaid(boolean canSendPaid) {
            this.canSendPaid = canSendPaid;
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

    @RequestMapping(value = "admin/sendSmsMailings", method = RequestMethod.GET)
    @ResponseBody
    public MailingDataRespone prepareForm() {
        MailingDataRespone response = new MailingDataRespone();

        boolean canSendFree;
        boolean canSendPaid;
        if (LoggedUser.isAdministrator()) {
            canSendFree = permissionService.hasPermission(AdminMemberPermission.SMS_MAILINGS_FREE_SMS_MAILINGS);
            canSendPaid = permissionService.hasPermission(AdminMemberPermission.SMS_MAILINGS_PAID_SMS_MAILINGS);

            final GroupQuery query = new GroupQuery();
            query.setManagedBy((AdminGroup) LoggedUser.group());
            query.setOnlyActive(true);
            List<GroupEntity> groupList = new ArrayList();
            for (Group group : groupService.search(query)) {
                GroupEntity entity = new GroupEntity();
                entity.setId(group.getId());
                entity.setName(group.getName());
                groupList.add(entity);
            }
            response.setGroupList(groupList);
        } else {
            canSendFree = permissionService.hasPermission(BrokerPermission.SMS_MAILINGS_FREE_SMS_MAILINGS);
            canSendPaid = permissionService.hasPermission(BrokerPermission.SMS_MAILINGS_PAID_SMS_MAILINGS);
        }

        response.setCanSendFree(canSendFree);
        response.setCanSendPaid(canSendPaid);
        if (canSendFree && canSendPaid) {
            response.setFree(true);
        }
        response.setStatus(0);
        return response;
    }

    public static class SendSmsParameters {

        private String text;
        private List<Long> groups;
        private Boolean free;
        private Long member;

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }

        public List<Long> getGroups() {
            return groups;
        }

        public void setGroups(List<Long> groups) {
            this.groups = groups;
        }

        public Boolean getFree() {
            return free;
        }

        public void setFree(Boolean free) {
            this.free = free;
        }

        public Long getMember() {
            return member;
        }

        public void setMember(Long member) {
            this.member = member;
        }

    }

    @RequestMapping(value = "admin/sendSmsMailings", method = RequestMethod.POST)
    @ResponseBody
    public GenericResponse sendSms(@RequestBody SendSmsParameters params) {
        GenericResponse response = new GenericResponse();
        Map<String, Object> query = new HashMap();
        query.put("text", params.getText());
        query.put("member", params.getMember());
        query.put("free", params.getFree());
        query.put("groups", params.getGroups());
        final SmsMailing smsMailing = getDataBinder().readFromString(query);

        Permission permission;
        if (LoggedUser.isAdministrator()) {
            permission = smsMailing.isFree() ? AdminMemberPermission.SMS_MAILINGS_FREE_SMS_MAILINGS : AdminMemberPermission.SMS_MAILINGS_PAID_SMS_MAILINGS;
        } else {
            permission = smsMailing.isFree() ? BrokerPermission.SMS_MAILINGS_FREE_SMS_MAILINGS : BrokerPermission.SMS_MAILINGS_PAID_SMS_MAILINGS;
        }

        if (!permissionService.hasPermission(permission)) {
            throw new PermissionDeniedException();
        }
        smsMailingService.send(smsMailing);
        response.setStatus(0);
        return response;

    }

    private DataBinder<SmsMailing> getDataBinder() {
        if (dataBinder == null) {
            final BeanBinder<SmsMailing> binder = BeanBinder.instance(SmsMailing.class);
            binder.registerBinder("free", PropertyBinder.instance(Boolean.TYPE, "free"));
            binder.registerBinder("text", PropertyBinder.instance(String.class, "text"));
            binder.registerBinder("member", PropertyBinder.instance(Member.class, "member"));
            binder.registerBinder("groups", SimpleCollectionBinder.instance(MemberGroup.class, "groups"));
            dataBinder = binder;
        }
        return dataBinder;
    }

}
