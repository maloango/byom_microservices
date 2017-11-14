/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.strohalm.cyclos.webservices.rest.settings;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import nl.strohalm.cyclos.entities.access.Channel;
import nl.strohalm.cyclos.entities.accounts.transactions.TransferType;
import nl.strohalm.cyclos.entities.groups.AdminGroup;
import nl.strohalm.cyclos.entities.groups.Group;
import nl.strohalm.cyclos.entities.groups.GroupQuery;
import nl.strohalm.cyclos.entities.groups.MemberGroup;
import nl.strohalm.cyclos.entities.members.Member;
import nl.strohalm.cyclos.entities.services.ServiceClient;
import nl.strohalm.cyclos.entities.services.ServiceOperation;
import nl.strohalm.cyclos.utils.access.LoggedUser;
import nl.strohalm.cyclos.utils.binding.BeanBinder;
import nl.strohalm.cyclos.utils.binding.DataBinder;
import nl.strohalm.cyclos.utils.binding.PropertyBinder;
import nl.strohalm.cyclos.utils.binding.SimpleCollectionBinder;
import nl.strohalm.cyclos.utils.conversion.IdConverter;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;
import nl.strohalm.cyclos.webservices.rest.GenericResponse;
import org.apache.cxf.endpoint.Client;
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
public class EditServiceClientController extends BaseRestController {

    private DataBinder<ServiceClient> dataBinder;

    public static class ServiceClientResposne extends GenericResponse {

        private List<MemberGroupEntity> memberGroups;
        private List<ChannelEntity> channelList;
        private ServiceOperation serviceOperation;
        private List<ServiceOperation> serviceOperations;

        public List<ServiceOperation> getServiceOperations() {
            return serviceOperations;
        }

        public void setServiceOperations(List<ServiceOperation> serviceOperations) {
            this.serviceOperations = serviceOperations;
        }

        public ServiceOperation getServiceOperation() {
            return serviceOperation;
        }

        public void setServiceOperation(ServiceOperation serviceOperation) {
            this.serviceOperation = serviceOperation;
        }

        public List<ChannelEntity> getChannelList() {
            return channelList;
        }

        public void setChannelList(List<ChannelEntity> channelList) {
            this.channelList = channelList;
        }

        public List<MemberGroupEntity> getMemberGroups() {
            return memberGroups;
        }

        public void setMemberGroups(List<MemberGroupEntity> memberGroups) {
            this.memberGroups = memberGroups;
        }

    }

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

    public static class ChannelEntity {

        private Long id;
        private String displayName;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getDisplayName() {
            return displayName;
        }

        public void setDisplayName(String displayName) {
            this.displayName = displayName;
        }

    }

    @RequestMapping(value = "admin/editServiceClient", method = RequestMethod.GET)
    @ResponseBody
    public ServiceClientResposne prepareForm() {
        ServiceClientResposne response = new ServiceClientResposne();
        ServiceClient client;
//        if (id <= 0) {
        client = new ServiceClient();
//        } else {
//            client = serviceClientService.load(id, ServiceClient.Relationships.MEMBER, ServiceClient.Relationships.PERMISSIONS);
//            final List<TransferType> doPaymentTypes = serviceClientService.listPossibleDoPaymentTypes(client);
//            final List<TransferType> receivePaymentTypes = serviceClientService.listPossibleReceivePaymentTypes(client);
//            request.setAttribute("doPaymentTypes", doPaymentTypes);
//            request.setAttribute("receivePaymentTypes", receivePaymentTypes);
//            request.setAttribute("chargebackPaymentTypes", client.getMember() == null ? doPaymentTypes : receivePaymentTypes);
//        }
        final GroupQuery groupQuery = new GroupQuery();
        groupQuery.setManagedBy((AdminGroup) LoggedUser.group());
        groupQuery.setNatures(Group.Nature.BROKER, Group.Nature.MEMBER);
        final List<MemberGroup> memberGroups = (List<MemberGroup>) groupService.search(groupQuery);
        List<MemberGroupEntity> memberGroupList = new ArrayList();
        for (MemberGroup group : memberGroups) {
            MemberGroupEntity entity = new MemberGroupEntity();
            entity.setId(group.getId());
            entity.setName(group.getName());
            memberGroupList.add(entity);
        }
        response.setMemberGroups(memberGroupList);

        List<ChannelEntity> channelList = new ArrayList();
        for (Channel channel : serviceClientService.listPossibleChannels()) {
            ChannelEntity entity = new ChannelEntity();
            entity.setId(channel.getId());
            entity.setDisplayName(channel.getDisplayName());
            channelList.add(entity);
        }
        response.setChannelList(channelList);
        List<ServiceOperation> serviceOperations = Arrays.asList(ServiceOperation.values());
        response.setServiceOperations(serviceOperations);
        System.out.println("------service: " + serviceOperations);
        response.setStatus(0);
        return response;

    }

    @RequestMapping(value = "admin/editServiceClient", method = RequestMethod.POST)
    @ResponseBody
    public GenericResponse submit(@RequestBody ServiceClientParameters params) {
        GenericResponse response = new GenericResponse();
        ServiceClient clientTemp = null;
        Map<String, Object> query = new HashMap();
        if (params.getId() != null && params.getId() > 0L) {
            clientTemp = serviceClientService.load(params.getId(), ServiceClient.Relationships.MEMBER, ServiceClient.Relationships.PERMISSIONS);
            final List<TransferType> doPaymentTypes = serviceClientService.listPossibleDoPaymentTypes(clientTemp);
            final List<TransferType> receivePaymentTypes = serviceClientService.listPossibleReceivePaymentTypes(clientTemp);
            query.put("id", params.getId());
            query.put("doPaymentTypes", doPaymentTypes);
            query.put("receivePaymentTypes", receivePaymentTypes);
            query.put("chargebackPaymentTypes", clientTemp.getMember() == null ? doPaymentTypes : receivePaymentTypes);
        }
        query.put("name", params.getName());
        query.put("hostname", params.getHostname());
        query.put("username", params.getUsername());
        query.put("password", params.getPassword());
       // query.put("credentialsRequired", params.getCredentialsRequired());
        //query.put("ignoreRegistrationValidations", params.getIgnoreRegistrationValidations());
        query.put("member", params.getMember());
        query.put("channel", params.getChannel());
        query.put("permissions", params.getPermissions());
      //  query.put("manageGroups", params.getManageGroups());

        final ServiceClient client = getDataBinder().readFromString(query);
        final boolean isInsert = client.isTransient();
        serviceClientService.save(client);
        response.setMessage(isInsert ? "serviceClient.inserted" : "serviceClient.modified");
        response.setStatus(0);
        return response;

    }

    private static class ServiceClientParameters {

        private Long id;
        private String name;
        private String hostname;
        private String username;
        private String password;
        private Boolean credentialsRequired;
        private Long member;
        private Long channel;
        private Set<String> permissions;
        private Set<Long> doPaymentTypes;
        private Set<Long> receivePaymentTypes;
        private Set<Long> chargebackPaymentTypes;
        private Set<Long> manageGroups;
        private Boolean ignoreRegistrationValidations;

        public Boolean getIgnoreRegistrationValidations() {
            return ignoreRegistrationValidations;
        }

        public void setIgnoreRegistrationValidations(Boolean ignoreRegistrationValidations) {
            this.ignoreRegistrationValidations = ignoreRegistrationValidations;
        }

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

        public String getHostname() {
            return hostname;
        }

        public void setHostname(String hostname) {
            this.hostname = hostname;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public Boolean getCredentialsRequired() {
            return credentialsRequired;
        }

        public void setCredentialsRequired(Boolean credentialsRequired) {
            this.credentialsRequired = credentialsRequired;
        }

        public Long getMember() {
            return member;
        }

        public void setMember(Long member) {
            this.member = member;
        }

        public Long getChannel() {
            return channel;
        }

        public void setChannel(Long channel) {
            this.channel = channel;
        }

        public Set<String> getPermissions() {
            return permissions;
        }

        public void setPermissions(Set<String> permissions) {
            this.permissions = permissions;
        }

        public Set<Long> getDoPaymentTypes() {
            return doPaymentTypes;
        }

        public void setDoPaymentTypes(Set<Long> doPaymentTypes) {
            this.doPaymentTypes = doPaymentTypes;
        }

        public Set<Long> getReceivePaymentTypes() {
            return receivePaymentTypes;
        }

        public void setReceivePaymentTypes(Set<Long> receivePaymentTypes) {
            this.receivePaymentTypes = receivePaymentTypes;
        }

        public Set<Long> getChargebackPaymentTypes() {
            return chargebackPaymentTypes;
        }

        public void setChargebackPaymentTypes(Set<Long> chargebackPaymentTypes) {
            this.chargebackPaymentTypes = chargebackPaymentTypes;
        }

        public Set<Long> getManageGroups() {
            return manageGroups;
        }

        public void setManageGroups(Set<Long> manageGroups) {
            this.manageGroups = manageGroups;
        }

    }

    private DataBinder<ServiceClient> getDataBinder() {
        if (dataBinder == null) {
            final BeanBinder<ServiceClient> binder = BeanBinder.instance(ServiceClient.class);
            binder.registerBinder("id", PropertyBinder.instance(Long.class, "id", IdConverter.instance()));
            binder.registerBinder("name", PropertyBinder.instance(String.class, "name"));
            binder.registerBinder("hostname", PropertyBinder.instance(String.class, "hostname"));
            binder.registerBinder("username", PropertyBinder.instance(String.class, "username"));
            binder.registerBinder("password", PropertyBinder.instance(String.class, "password"));
            binder.registerBinder("credentialsRequired", PropertyBinder.instance(Boolean.TYPE, "credentialsRequired"));
            binder.registerBinder("ignoreRegistrationValidations", PropertyBinder.instance(Boolean.TYPE, "ignoreRegistrationValidations"));
            binder.registerBinder("member", PropertyBinder.instance(Member.class, "member"));
            binder.registerBinder("channel", PropertyBinder.instance(Channel.class, "channel"));
            binder.registerBinder("permissions", SimpleCollectionBinder.instance(ServiceOperation.class, Set.class, "permissions"));
            binder.registerBinder("doPaymentTypes", SimpleCollectionBinder.instance(TransferType.class, Set.class, "doPaymentTypes"));
            binder.registerBinder("receivePaymentTypes", SimpleCollectionBinder.instance(TransferType.class, Set.class, "receivePaymentTypes"));
            binder.registerBinder("chargebackPaymentTypes", SimpleCollectionBinder.instance(TransferType.class, Set.class, "chargebackPaymentTypes"));
            binder.registerBinder("manageGroups", SimpleCollectionBinder.instance(MemberGroup.class, Set.class, "manageGroups"));
            dataBinder = binder;
        }
        return dataBinder;
    }
}
