/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.strohalm.cyclos.webservices.rest.members.pending;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import nl.strohalm.cyclos.entities.accounts.transactions.TransferQuery;
import nl.strohalm.cyclos.entities.customization.fields.MemberCustomField;
import nl.strohalm.cyclos.entities.customization.fields.MemberCustomFieldValue;
import nl.strohalm.cyclos.entities.groups.AdminGroup;
import nl.strohalm.cyclos.entities.groups.BrokerGroup;
import nl.strohalm.cyclos.entities.groups.Group;
import nl.strohalm.cyclos.entities.groups.MemberGroup;
import nl.strohalm.cyclos.entities.members.Element;
import nl.strohalm.cyclos.entities.members.Member;
import nl.strohalm.cyclos.entities.members.PendingMember;
import nl.strohalm.cyclos.entities.members.PendingMemberQuery;
import nl.strohalm.cyclos.entities.settings.LocalSettings;
import nl.strohalm.cyclos.utils.Period;
import nl.strohalm.cyclos.utils.access.LoggedUser;
import nl.strohalm.cyclos.utils.binding.BeanBinder;
import nl.strohalm.cyclos.utils.binding.BeanCollectionBinder;
import nl.strohalm.cyclos.utils.binding.DataBinder;
import nl.strohalm.cyclos.utils.binding.DataBinderHelper;
import nl.strohalm.cyclos.utils.binding.PropertyBinder;
import nl.strohalm.cyclos.utils.binding.SimpleCollectionBinder;
import nl.strohalm.cyclos.utils.validation.ValidationException;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;
import nl.strohalm.cyclos.webservices.rest.GenericResponse;
import static nl.strohalm.cyclos.webservices.rest.accounts.details.AccountHistoryController.transferQueryDataBinder;
import org.apache.commons.beanutils.BeanComparator;
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
public class SearchPendingMemberController extends BaseRestController {

    private ReadWriteLock lock = new ReentrantReadWriteLock(true);
    private DataBinder<PendingMemberQuery> dataBinder;

    public static BeanBinder<PendingMemberQuery> createDataBinder(final LocalSettings settings) {
        final BeanBinder<PendingMemberQuery> binder = BeanBinder.instance(PendingMemberQuery.class);

        final BeanBinder<MemberCustomFieldValue> customValueBinder = BeanBinder.instance(MemberCustomFieldValue.class);
        customValueBinder.registerBinder("field", PropertyBinder.instance(MemberCustomField.class, "field"));
        customValueBinder.registerBinder("value", PropertyBinder.instance(String.class, "value"));

        binder.registerBinder("name", PropertyBinder.instance(String.class, "name"));
        binder.registerBinder("broker", PropertyBinder.instance(Member.class, "broker"));
        binder.registerBinder("creationPeriod", DataBinderHelper.periodBinder(settings, "creationPeriod"));
        binder.registerBinder("groups", SimpleCollectionBinder.instance(MemberGroup.class, "groups"));
        binder.registerBinder("customValues", BeanCollectionBinder.instance(customValueBinder, "customValues"));
        binder.registerBinder("pageParameters", DataBinderHelper.pageBinder());
        return binder;
    }

    private DataBinder<PendingMemberQuery> getDataBinder() {
        try {
            lock.readLock().lock();
            if (dataBinder == null) {
                final LocalSettings settings = settingsService.getLocalSettings();
                dataBinder = createDataBinder(settings);
            }
            return dataBinder;
        } finally {
            lock.readLock().unlock();
        }
    }

    public static class PendingMembersResponse extends GenericResponse {

        private List<PendingMemberEntity> groups;
       private List<PendingMemberListEntity>pendingMembersList;

        public List<PendingMemberListEntity> getPendingMembersList() {
            return pendingMembersList;
        }

        public void setPendingMembersList(List<PendingMemberListEntity> pendingMembersList) {
            this.pendingMembersList = pendingMembersList;
        }

      

        public List<PendingMemberEntity> getGroups() {
            return groups;
        }

        public void setGroups(List<PendingMemberEntity> groups) {
            this.groups = groups;
        }

    }

    public static class PendingMemberEntity {

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
    public static class PendingMemberListEntity{
        private Long id;
        private String name;
        private String userName;
        private Calendar creationDate;

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

        public Calendar getCreationDate() {
            return creationDate;
        }

        public void setCreationDate(Calendar creationDate) {
            this.creationDate = creationDate;
        }
        
        
    }

    @RequestMapping(value = "admin/searchPendingMembers", method = RequestMethod.GET)
    @ResponseBody
    public PendingMembersResponse prepareForm() {

        PendingMembersResponse response = new PendingMembersResponse();
        // Retrieve the custom fields that will be used on the search
        //final List<MemberCustomField> fields = customFieldHelper.onlyForMemberSearch(memberCustomFieldService.list());
        // request.setAttribute("customFields", customFieldHelper.buildEntries(fields, query.getCustomValues()));

        // Get the allowed groups
        Collection<MemberGroup> allowedGroups;
        if (LoggedUser.isAdministrator()) {
            final AdminGroup group = groupService.reload(LoggedUser.group().getId(), AdminGroup.Relationships.MANAGES_GROUPS);
            allowedGroups = group.getManagesGroups();
        } else if (LoggedUser.isBroker()) {
            final BrokerGroup group = groupService.reload(LoggedUser.group().getId(), BrokerGroup.Relationships.POSSIBLE_INITIAL_GROUPS);
            allowedGroups = group.getPossibleInitialGroups();
        } else {
            throw new ValidationException();
        }
        final List<MemberGroup> groupsList = new ArrayList<MemberGroup>(allowedGroups);
        Collections.sort(groupsList, new BeanComparator("name"));
        List<PendingMemberEntity> groups = new ArrayList();
        for (MemberGroup group : groupsList) {
            PendingMemberEntity pEntity = new PendingMemberEntity();
            pEntity.setId(group.getId());
            pEntity.setName(group.getName());
            groups.add(pEntity);
        }
        response.setGroups(groups);
        response.setStatus(0);
        response.setMessage("");
        return response;
    }

    public static class PendingMembersParameters {

        private String name;
        private Long broker;
        private String creationPeriodBegin;
        private String creationPeriodEnd;
        private List<Long> groups;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Long getBroker() {
            return broker;
        }

        public void setBroker(Long broker) {
            this.broker = broker;
        }

        public String getCreationPeriodBegin() {
            return creationPeriodBegin;
        }

        public void setCreationPeriodBegin(String creationPeriodBegin) {
            this.creationPeriodBegin = creationPeriodBegin;
        }

        public String getCreationPeriodEnd() {
            return creationPeriodEnd;
        }

        public void setCreationPeriodEnd(String creationPeriodEnd) {
            this.creationPeriodEnd = creationPeriodEnd;
        }

        public List<Long> getGroups() {
            return groups;
        }

        public void setGroups(List<Long> groups) {
            this.groups = groups;
        }

    }

    @RequestMapping(value = "admin/searchPendingMembers", method = RequestMethod.POST)
    @ResponseBody
    public PendingMembersResponse searchPendingMembers(@RequestBody PendingMembersParameters params) {
        final LocalSettings localSettings = settingsService.getLocalSettings();
        PendingMembersResponse response = new PendingMembersResponse();
        Map<String, Object> queryParameter = new HashMap<String, Object>();
        queryParameter.put("name", params.getName());
        if(params.getBroker()!=null && params.getBroker()<0L)
        queryParameter.put("broker", (Member) elementService.load(params.getBroker(), Element.Relationships.USER));
        Period creationPeriod = new Period();
        creationPeriod.setBegin(localSettings.getDateConverter().valueOf(params.getCreationPeriodBegin()));
        creationPeriod.setEnd(localSettings.getDateConverter().valueOf(params.getCreationPeriodEnd()));
        queryParameter.put("creationPeriod", creationPeriod);
        queryParameter.put("groups", params.getGroups());

        final PendingMemberQuery query = getDataBinder().readFromString(queryParameter);
        List<PendingMember> pendingMembers = elementService.search(query);
        List<PendingMemberListEntity>pendingMembersList=new ArrayList();
        for(PendingMember member:pendingMembers){
            PendingMemberListEntity entity=new PendingMemberListEntity();
            entity.setId(member.getId());
            entity.setName(member.getName());
            entity.setUserName(member.getUsername());
            entity.setCreationDate(member.getCreationDate());
            pendingMembersList.add(entity);
            
        }
        response.setPendingMembersList(pendingMembersList);
        response.setStatus(0);
        response.setMessage("");
        return response;

    }

}
