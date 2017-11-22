package nl.strohalm.cyclos.webservices.rest.groups;

import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import nl.strohalm.cyclos.controls.ActionContext;
import nl.strohalm.cyclos.entities.access.Channel;
import nl.strohalm.cyclos.entities.access.TransactionPassword;
import nl.strohalm.cyclos.entities.accounts.cards.CardType;
import nl.strohalm.cyclos.entities.accounts.transactions.TransferType;
import nl.strohalm.cyclos.entities.customization.files.CustomizedFile;
import nl.strohalm.cyclos.entities.groups.AdminGroup;
import nl.strohalm.cyclos.entities.groups.BasicGroupSettings;
import nl.strohalm.cyclos.entities.groups.BrokerGroup;
import nl.strohalm.cyclos.entities.groups.Group;
import nl.strohalm.cyclos.entities.groups.GroupQuery;
import nl.strohalm.cyclos.entities.groups.MemberGroup;
import nl.strohalm.cyclos.entities.groups.MemberGroupSettings;
import nl.strohalm.cyclos.entities.groups.OperatorGroup;
import nl.strohalm.cyclos.entities.groups.SystemGroup;
import nl.strohalm.cyclos.entities.members.Member;
import nl.strohalm.cyclos.entities.members.RegistrationAgreement;
import nl.strohalm.cyclos.entities.members.messages.Message;
import nl.strohalm.cyclos.entities.settings.LocalSettings;
import nl.strohalm.cyclos.utils.access.LoggedUser;
import nl.strohalm.cyclos.utils.binding.BeanBinder;
import nl.strohalm.cyclos.utils.binding.DataBinder;
import nl.strohalm.cyclos.utils.binding.DataBinderHelper;
import nl.strohalm.cyclos.utils.binding.MapBinder;
import nl.strohalm.cyclos.utils.binding.PropertyBinder;
import nl.strohalm.cyclos.utils.binding.SimpleCollectionBinder;
import nl.strohalm.cyclos.utils.conversion.IdConverter;
import nl.strohalm.cyclos.utils.validation.ValidationException;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;
import nl.strohalm.cyclos.webservices.rest.GenericResponse;
import org.apache.commons.lang.StringUtils;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class EditGroupController extends BaseRestController {

    private Map<Group.Nature, DataBinder<? extends Group>> dataBinders;

    public DataBinder<? extends Group> getDataBinder(final Group.Nature nature) {
        if (dataBinders == null) {
            dataBinders = new EnumMap<Group.Nature, DataBinder<? extends Group>>(Group.Nature.class);

            final BeanBinder<BasicGroupSettings> basicSettingsBinder = BeanBinder.instance(BasicGroupSettings.class, "basicSettings");
            final BeanBinder<MemberGroupSettings> memberSettingsBinder = BeanBinder.instance(MemberGroupSettings.class, "memberSettings");

            final BeanBinder<AdminGroup> adminGroupBinder = BeanBinder.instance(AdminGroup.class);
            initBasic(adminGroupBinder, basicSettingsBinder);
            initSystem(adminGroupBinder);
            dataBinders.put(Group.Nature.ADMIN, adminGroupBinder);

            final BeanBinder<MemberGroup> memberGroupBinder = BeanBinder.instance(MemberGroup.class);
            initBasic(memberGroupBinder, basicSettingsBinder);
            initSystem(memberGroupBinder);
            initMember(memberGroupBinder, memberSettingsBinder);
            dataBinders.put(Group.Nature.MEMBER, memberGroupBinder);

            final BeanBinder<OperatorGroup> operatorGroupBinder = BeanBinder.instance(OperatorGroup.class);
            initBasic(operatorGroupBinder, basicSettingsBinder);
            initOperator(operatorGroupBinder);
            dataBinders.put(Group.Nature.OPERATOR, operatorGroupBinder);

            final BeanBinder<BrokerGroup> brokerGroupBinder = BeanBinder.instance(BrokerGroup.class);
            initBasic(brokerGroupBinder, basicSettingsBinder);
            initSystem(brokerGroupBinder);
            initMember(brokerGroupBinder, memberSettingsBinder);
            initBroker(brokerGroupBinder);
            dataBinders.put(Group.Nature.BROKER, brokerGroupBinder);
        }
        return dataBinders.get(nature);
    }

    public static class GroupResposne extends GenericResponse {

        private List<Group.Nature> natures;
        private List<GroupEntity> groups;

        public List<GroupEntity> getGroups() {
            return groups;
        }

        public void setGroups(List<GroupEntity> groups) {
            this.groups = groups;
        }

        public List<Group.Nature> getNatures() {
            return natures;
        }

        public void setNatures(List<Group.Nature> natures) {
            this.natures = natures;
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

    @RequestMapping(value = "admin/editGroup", method = RequestMethod.GET)
    @ResponseBody
    public GroupResposne prepareForm() {
        GroupResposne response = new GroupResposne();
        // List of groups that the administrator can manage
        AdminGroup adminGroup = LoggedUser.group();
        adminGroup = groupService.load(adminGroup.getId(), AdminGroup.Relationships.MANAGES_GROUPS);
        //response.setMemberGroups(adminGroup.getManagesGroups());

        // List of group natures
        response.setNatures(Arrays.asList(Group.Nature.ADMIN, Group.Nature.BROKER, Group.Nature.MEMBER));
        response.setStatus(0);
        return response;

    }

    @RequestMapping(value = "admin/findGroupByNature/{nature}", method = RequestMethod.GET)
    @ResponseBody
    public GroupResposne prepareForm(@PathVariable("nature") String nature) {
        GroupResposne response = new GroupResposne();
        Group.Nature n = Group.Nature.valueOf(nature);
        final GroupQuery query = new GroupQuery();
        if (n.equals(Group.Nature.ADMIN)) {
            query.setNature(Group.Nature.ADMIN);
        } else if (n.equals(Group.Nature.MEMBER)) {
            query.setNature(Group.Nature.MEMBER);
        } else if (n.equals(Group.Nature.BROKER)) {
            query.setNature(Group.Nature.BROKER);
        }
        final List<? extends Group> groupsList = groupService.search(query);
        List<GroupEntity> groups = new ArrayList();
        for (Group group : groupsList) {
            GroupEntity entity = new GroupEntity();
            entity.setId(group.getId());
            entity.setName(group.getName());
            groups.add(entity);
        }
        response.setGroups(groups);
        response.setStatus(0);
        return response;
    }

    public static class GroupParameters {

        private Long id;
        private Long baseGroupId;
        private String name;
        private String rootUrl;
        private String description;
        private String nature;
        private String status;

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public Long getBaseGroupId() {
            return baseGroupId;
        }

        public void setBaseGroupId(Long baseGroupId) {
            this.baseGroupId = baseGroupId;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getRootUrl() {
            return rootUrl;
        }

        public void setRootUrl(String rootUrl) {
            this.rootUrl = rootUrl;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getNature() {
            return nature;
        }

        public void setNature(String nature) {
            this.nature = nature;
        }

    }

    @RequestMapping(value = "admin/editGroup", method = RequestMethod.POST)
    @ResponseBody
    public GenericResponse handleSubmit(@RequestBody GroupParameters params) {
        GenericResponse response = new GenericResponse();
        Group group = readGroup(params);
        final Group baseGroup = readBaseGroup(params);
        final boolean isInsert = group.getId() == null;
        System.out.println("------isInsert: " + isInsert);
        // Persist the group
        if (isInsert) {
            group = groupService.insert(group, baseGroup);
            response.setMessage("group.inserted");
        } else {
            group = groupService.update(group, false);
            response.setMessage("group.modified");
        }

        // Ensure the customized files collection is reloaded (for example, a group could be copied from other, and we need the collection to be
        // up-to-date)
        group = groupService.reload(group.getId(), Group.Relationships.CUSTOMIZED_FILES);

        // Physically update the files
//        response.setMessage(isInsert ? "group.inserted" : "group.modified");
        response.setStatus(0);
        return response;
    }

    private Group readGroup(GroupParameters params) {

        final long id = params.getId();
        Group.Nature nature;
        String status = params.getStatus();
        final boolean isInsert = (id <= 0L);
        if (isInsert) {
            nature = getGroupNature(params, false);
            // On insert, empty status means normal
//            final String status = (String) params.getStatus();
            if (StringUtils.isEmpty(status)) {
                status = Group.Status.NORMAL.toString();
            }

        } else {
            nature = groupService.load(id).getNature();
        }
        Map<String, Object> map = new HashMap();
        if (params.getId() != null && params.getId() > 0L) {
            map.put("id", params.getId());
        }
        map.put("name", params.getName());
        map.put("rootUrl", params.getRootUrl());
        map.put("description", params.getDescription());
        map.put("nature", Group.Nature.valueOf(params.getNature()));
        if (params.getStatus() != null) {
            map.put("status", Group.Status.valueOf(status));
        }

        final Group group = getDataBinder(nature).readFromString(map);
        // System.out.println("-----group: " + group.getId() + " ," + group.getDescription());
        if (nature == Group.Nature.OPERATOR) {
            // Ensure to set the logged member on operator groups, as this is not read from request
            final Member member = (Member) LoggedUser.element();
            final OperatorGroup operatorGroup = (OperatorGroup) group;
            operatorGroup.setMember(member);
        }
        return group;
    }

    private Group readBaseGroup(final GroupParameters params) {

        final long baseGroupId = params.getBaseGroupId();
        if (baseGroupId < 1) {
            return null;
        }
        return groupService.load(baseGroupId);
    }

    private static Group.Nature getGroupNature(final GroupParameters params, final boolean acceptEmpty) {
        try {

            final String nature = params.getNature();
            if (acceptEmpty && StringUtils.isBlank(nature)) {
                return null;
            } else {
                return Group.Nature.valueOf(nature);
            }
        } catch (final Exception e) {
            throw new ValidationException();
        }
    }

    /// init stuff
    private void initBasic(final BeanBinder<? extends Group> groupBinder, final BeanBinder<? extends BasicGroupSettings> basicSettingsBinder) {
        groupBinder.registerBinder("basicSettings", basicSettingsBinder);

        groupBinder.registerBinder("id", PropertyBinder.instance(Long.class, "id", IdConverter.instance()));
        groupBinder.registerBinder("name", PropertyBinder.instance(String.class, "name"));
        groupBinder.registerBinder("description", PropertyBinder.instance(String.class, "description"));
        groupBinder.registerBinder("status", PropertyBinder.instance(Group.Status.class, "status"));

        basicSettingsBinder.registerBinder("passwordLength", DataBinderHelper.rangeConstraintBinder("passwordLength"));
        basicSettingsBinder.registerBinder("passwordPolicy", PropertyBinder.instance(BasicGroupSettings.PasswordPolicy.class, "passwordPolicy"));
        basicSettingsBinder.registerBinder("maxPasswordWrongTries", PropertyBinder.instance(Integer.TYPE, "maxPasswordWrongTries"));
        basicSettingsBinder.registerBinder("deactivationAfterMaxPasswordTries", DataBinderHelper.timePeriodBinder("deactivationAfterMaxPasswordTries"));
        basicSettingsBinder.registerBinder("passwordExpiresAfter", DataBinderHelper.timePeriodBinder("passwordExpiresAfter"));
        basicSettingsBinder.registerBinder("transactionPassword", PropertyBinder.instance(TransactionPassword.class, "transactionPassword"));
        basicSettingsBinder.registerBinder("transactionPasswordLength", PropertyBinder.instance(Integer.TYPE, "transactionPasswordLength"));
        basicSettingsBinder.registerBinder("maxTransactionPasswordWrongTries", PropertyBinder.instance(Integer.TYPE, "maxTransactionPasswordWrongTries"));
        basicSettingsBinder.registerBinder("hideCurrencyOnPayments", PropertyBinder.instance(Boolean.TYPE, "hideCurrencyOnPayments"));
    }

    private void initBroker(final BeanBinder<? extends BrokerGroup> brokerGroupBinder) {
        brokerGroupBinder.registerBinder("possibleInitialGroups", SimpleCollectionBinder.instance(MemberGroup.class, "possibleInitialGroups"));
    }

    private void initMember(final BeanBinder<? extends MemberGroup> memberGroupBinder, final BeanBinder<? extends MemberGroupSettings> memberSettingsBinder) {
        memberGroupBinder.registerBinder("memberSettings", memberSettingsBinder);
        memberGroupBinder.registerBinder("active", PropertyBinder.instance(Boolean.TYPE, "active"));
        memberGroupBinder.registerBinder("initialGroup", PropertyBinder.instance(Boolean.TYPE, "initialGroup"));
        memberGroupBinder.registerBinder("initialGroupShow", PropertyBinder.instance(String.class, "initialGroupShow"));
        memberGroupBinder.registerBinder("registrationAgreement", PropertyBinder.instance(RegistrationAgreement.class, "registrationAgreement"));
        memberGroupBinder.registerBinder("defaultMailMessages", SimpleCollectionBinder.instance(Message.Type.class, "defaultMailMessages"));
        memberGroupBinder.registerBinder("smsMessages", SimpleCollectionBinder.instance(Message.Type.class, "smsMessages"));
        memberGroupBinder.registerBinder("defaultSmsMessages", SimpleCollectionBinder.instance(Message.Type.class, "defaultSmsMessages"));
        memberGroupBinder.registerBinder("defaultAllowChargingSms", PropertyBinder.instance(Boolean.TYPE, "defaultAllowChargingSms"));
        memberGroupBinder.registerBinder("defaultAcceptFreeMailing", PropertyBinder.instance(Boolean.TYPE, "defaultAcceptFreeMailing"));
        memberGroupBinder.registerBinder("defaultAcceptPaidMailing", PropertyBinder.instance(Boolean.TYPE, "defaultAcceptPaidMailing"));
        memberGroupBinder.registerBinder("channels", SimpleCollectionBinder.instance(Channel.class, "channels"));
        memberGroupBinder.registerBinder("defaultChannels", SimpleCollectionBinder.instance(Channel.class, "defaultChannels"));
        memberGroupBinder.registerBinder("cardType", PropertyBinder.instance(CardType.class, "cardType"));

        final LocalSettings localSettings = settingsService.getLocalSettings();
        memberSettingsBinder.registerBinder("pinLength", DataBinderHelper.rangeConstraintBinder("pinLength"));
        memberSettingsBinder.registerBinder("maxPinWrongTries", PropertyBinder.instance(Integer.TYPE, "maxPinWrongTries"));
        memberSettingsBinder.registerBinder("pinBlockTimeAfterMaxTries", DataBinderHelper.timePeriodBinder("pinBlockTimeAfterMaxTries"));
        memberSettingsBinder.registerBinder("pinLength", DataBinderHelper.rangeConstraintBinder("pinLength"));
        memberSettingsBinder.registerBinder("smsChargeTransferType", PropertyBinder.instance(TransferType.class, "smsChargeTransferType"));
        memberSettingsBinder.registerBinder("smsChargeAmount", PropertyBinder.instance(BigDecimal.class, "smsChargeAmount", localSettings.getNumberConverter()));
        memberSettingsBinder.registerBinder("smsFree", PropertyBinder.instance(Integer.TYPE, "smsFree"));
        memberSettingsBinder.registerBinder("smsShowFreeThreshold", PropertyBinder.instance(Integer.TYPE, "smsShowFreeThreshold"));
        memberSettingsBinder.registerBinder("smsAdditionalCharged", PropertyBinder.instance(Integer.TYPE, "smsAdditionalCharged"));
        memberSettingsBinder.registerBinder("smsAdditionalChargedPeriod", DataBinderHelper.timePeriodBinder("smsAdditionalChargedPeriod"));
        memberSettingsBinder.registerBinder("smsContextClassName", PropertyBinder.instance(String.class, "smsContextClassName"));

        memberSettingsBinder.registerBinder("maxAdsPerMember", PropertyBinder.instance(Integer.TYPE, "maxAdsPerMember"));
        memberSettingsBinder.registerBinder("maxAdImagesPerMember", PropertyBinder.instance(Integer.TYPE, "maxAdImagesPerMember"));
        memberSettingsBinder.registerBinder("defaultAdPublicationTime", DataBinderHelper.timePeriodBinder("defaultAdPublicationTime"));
        memberSettingsBinder.registerBinder("maxAdPublicationTime", DataBinderHelper.timePeriodBinder("maxAdPublicationTime"));
        memberSettingsBinder.registerBinder("enablePermanentAds", PropertyBinder.instance(Boolean.TYPE, "enablePermanentAds"));
        memberSettingsBinder.registerBinder("externalAdPublication", PropertyBinder.instance(MemberGroupSettings.ExternalAdPublication.class, "externalAdPublication"));
        memberSettingsBinder.registerBinder("maxAdDescriptionSize", PropertyBinder.instance(Integer.TYPE, "maxAdDescriptionSize"));
        memberSettingsBinder.registerBinder("sendPasswordByEmail", PropertyBinder.instance(Boolean.TYPE, "sendPasswordByEmail"));
        memberSettingsBinder.registerBinder("emailValidation", SimpleCollectionBinder.instance(MemberGroupSettings.EmailValidation.class, HashSet.class, "emailValidation"));
        memberSettingsBinder.registerBinder("maxImagesPerMember", PropertyBinder.instance(Integer.TYPE, "maxImagesPerMember"));
        memberSettingsBinder.registerBinder("viewLoansByGroup", PropertyBinder.instance(Boolean.TYPE, "viewLoansByGroup"));
        memberSettingsBinder.registerBinder("repayLoanByGroup", PropertyBinder.instance(Boolean.TYPE, "repayLoanByGroup"));
        memberSettingsBinder.registerBinder("maxSchedulingPayments", PropertyBinder.instance(Integer.TYPE, "maxSchedulingPayments"));
        memberSettingsBinder.registerBinder("maxSchedulingPeriod", DataBinderHelper.timePeriodBinder("maxSchedulingPeriod"));
        memberSettingsBinder.registerBinder("showPosWebPaymentDescription", PropertyBinder.instance(Boolean.TYPE, "showPosWebPaymentDescription"));
        memberSettingsBinder.registerBinder("expireMembersAfter", DataBinderHelper.timePeriodBinder("expireMembersAfter"));
        memberSettingsBinder.registerBinder("groupAfterExpiration", PropertyBinder.instance(MemberGroup.class, "groupAfterExpiration"));
    }

    private void initOperator(final BeanBinder<? extends OperatorGroup> operatorGroupBinder) {
        // Bind max amount per day by transfer type map
        final LocalSettings localSettings = settingsService.getLocalSettings();
        final PropertyBinder<TransferType> keyBinder = PropertyBinder.instance(TransferType.class, "transferTypeIds");
        final PropertyBinder<BigDecimal> valueBinder = PropertyBinder.instance(BigDecimal.class, "maxAmountPerDayByTT", localSettings.getNumberConverter());
        final MapBinder<TransferType, BigDecimal> maxAmountBinder = MapBinder.instance(keyBinder, valueBinder);
        operatorGroupBinder.registerBinder("maxAmountPerDayByTransferType", maxAmountBinder);
    }

    private void initSystem(final BeanBinder<? extends SystemGroup> groupBinder) {
        groupBinder.registerBinder("rootUrl", PropertyBinder.instance(String.class, "rootUrl"));
        groupBinder.registerBinder("loginPageName", PropertyBinder.instance(String.class, "loginPageName"));
        groupBinder.registerBinder("containerUrl", PropertyBinder.instance(String.class, "containerUrl"));
    }

}
