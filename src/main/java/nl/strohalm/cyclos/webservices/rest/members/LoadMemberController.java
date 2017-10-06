/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.strohalm.cyclos.webservices.rest.members;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import nl.strohalm.cyclos.annotations.Inject;
import nl.strohalm.cyclos.entities.groups.MemberGroup;
import nl.strohalm.cyclos.entities.members.Element;
import nl.strohalm.cyclos.entities.members.Member;
import nl.strohalm.cyclos.entities.members.MemberQuery;
import nl.strohalm.cyclos.entities.settings.LocalSettings;
import nl.strohalm.cyclos.services.elements.BrokerQuery;
import nl.strohalm.cyclos.services.elements.ElementService;
import nl.strohalm.cyclos.services.groups.GroupService;
import nl.strohalm.cyclos.services.settings.SettingsService;
import nl.strohalm.cyclos.utils.access.LoggedUser;
import nl.strohalm.cyclos.utils.binding.BeanBinder;
import nl.strohalm.cyclos.utils.binding.BeanCollectionBinder;
import nl.strohalm.cyclos.utils.binding.DataBinder;
import nl.strohalm.cyclos.utils.binding.DataBinderHelper;
import nl.strohalm.cyclos.utils.binding.PropertyBinder;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;
import nl.strohalm.cyclos.webservices.rest.GenericResponse;
import org.apache.commons.lang.ArrayUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 *
 * @author Lue
 */
@Controller
public class LoadMemberController extends BaseRestController {

    private ElementService elementService;
    protected GroupService groupService;
    private SettingsService settingsService;

    public SettingsService getSettingsService() {
        return settingsService;
    }

    @Inject
    public void setSettingsService(SettingsService settingsService) {
        this.settingsService = settingsService;
    }

    private DataBinder<?> dataBinder;
    private DataBinder<?> dataBinderWithMaxScheduledPayments;

    public DataBinder<?> getDataBinder() {
        if (dataBinder == null) {
            dataBinder = BeanCollectionBinder.instance(DataBinderHelper.simpleElementBinder());
        }
        return dataBinder;
    }

    public DataBinder<?> getDataBinderWithMaxScheduledPayments() {
        if (dataBinderWithMaxScheduledPayments == null) {
            final BeanBinder<Map<String, Object>> elementBinder = DataBinderHelper.simpleElementBinder();
            elementBinder.registerBinder("maxScheduledPayments", PropertyBinder.instance(int.class, "group.memberSettings.maxSchedulingPayments"));
            dataBinderWithMaxScheduledPayments = BeanCollectionBinder.instance(elementBinder);
        }
        return dataBinderWithMaxScheduledPayments;
    }

    public ElementService getElementService() {
        return elementService;
    }

    @Inject
    public void setElementService(ElementService elementService) {
        this.elementService = elementService;
    }

    @Inject
    public void setGroupService(GroupService groupService) {
        this.groupService = groupService;
    }

//    @Override
//    protected ContentType contentType() {
//        return ContentType.JSON;
//    }
    public static class LoadMemberRequest {

        private String userName;
        private String name;
        private boolean brokers;
        private boolean viewableGroup;
        private boolean enabled = true;
        private boolean maxScheduledPayments;
        private Long exclude;
        private Long[] groupIds;

        public String getUserName() {
            return userName;
        }

        public void setUserName(String userName) {
            this.userName = userName;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public boolean isBrokers() {
            return brokers;
        }

        public void setBrokers(boolean brokers) {
            this.brokers = brokers;
        }

        public boolean isViewableGroup() {
            return viewableGroup;
        }

        public void setViewableGroup(boolean viewableGroup) {
            this.viewableGroup = viewableGroup;
        }

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public boolean isMaxScheduledPayments() {
            return maxScheduledPayments;
        }

        public void setMaxScheduledPayments(boolean maxScheduledPayments) {
            this.maxScheduledPayments = maxScheduledPayments;
        }

        public Long getExclude() {
            return exclude;
        }

        public void setExclude(Long exclude) {
            this.exclude = exclude;
        }

        public Long[] getGroupIds() {
            return groupIds;
        }

        public void setGroupIds(Long[] groupIds) {
            this.groupIds = groupIds;
        }

    }

    public static class LoadMemberResponse extends GenericResponse {

        private List<MemberEntity> members;

        public List<MemberEntity> getMembers() {
            return members;
        }

        public void setMembers(List<MemberEntity> members) {
            this.members = members;
        }

    }

    @RequestMapping(value = "member/loadMember", method = RequestMethod.POST)
    @ResponseBody
    public LoadMemberResponse executeAction(@RequestBody LoadMemberRequest form) throws Exception {
        LoadMemberResponse response = new LoadMemberResponse();
        final LocalSettings localSettings = settingsService.getLocalSettings();

        final MemberQuery memberQuery = form.isBrokers() ? new BrokerQuery() : new MemberQuery();
        //memberQuery.setViewableGroup(form.getViewableGroup());
        if (form.isEnabled()) {
            memberQuery.setEnabled(form.isEnabled());
        }
        memberQuery.setExcludeRemoved(true);
        memberQuery.limitResults(localSettings.getMaxAjaxResults());
        //memberQuery.setName(form.getName());
        memberQuery.setUsername(form.getUserName());
        // Search only brokered users
        if (LoggedUser.isBroker()) {
            if (form.isBrokers()) {
                final Member broker = (Member) LoggedUser.element();
                memberQuery.setBroker(broker);
            }
        }
//        Element exclude;
//        if (form.getExclude() != null) {
//            // When specifying a member to exclude from search, apply it...
//            exclude = EntityHelper.reference(Element.class, form.getExclude());
//        } else {
//            // ... otherwise, exclude the logged member himself
//            if (LoggedUser.isOperator()) {
//                exclude = (Element) LoggedUser.accountOwner();
//            } else {
//                exclude = LoggedUser.element();
//            }
//        }
//        memberQuery.setExcludeElements(Collections.singleton(exclude));
        if (form.isMaxScheduledPayments()) {
            memberQuery.fetch(Element.Relationships.GROUP);
        }
        System.out.println("----- " + memberQuery);
//        final Collection<MemberGroup> groups = resolveGroups(form.getGroupIds());
//        memberQuery.setGroups(groups);
        final List<? extends Element> members = elementService.search(memberQuery);
        final String json = (form.isMaxScheduledPayments() ? getDataBinderWithMaxScheduledPayments() : getDataBinder()).readAsString(members);
        //responseHelper.writeJSON(context.getResponse(), json);
        List<MemberEntity> memberList = new ArrayList();
        for (Element member : members) {
            MemberEntity memberEntity = new MemberEntity();
            memberEntity.setId(member.getId());
            memberEntity.setName(member.getName());
            memberEntity.setUserName(member.getUsername());
            memberList.add(memberEntity);
        }
        response.setStatus(0);
        response.setMembers(memberList);
        response.setMessage("Member found!!");

        return response;

    }

    private Collection<MemberGroup> resolveGroups(Long[] groupIds) {
        // Ensure that only normal groups (not removed) are used
        Collection<MemberGroup> groups = null;
        if (ArrayUtils.isNotEmpty(groupIds)) {
            groups = new HashSet<MemberGroup>();
            for (final Long id : groupIds) {
                if (id > 0) {
                    groups.add((MemberGroup) groupService.load(id));
                }
            }
        }
        return groups;
    }

    public static class MemberEntity {

        private long id;
        private String name;
        private String userName;

        public long getId() {
            return id;
        }

        public void setId(long id) {
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

    }
}
