package nl.strohalm.cyclos.webservices.rest.members.preferences;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import nl.strohalm.cyclos.annotations.Inject;
import nl.strohalm.cyclos.entities.access.Channel;
import nl.strohalm.cyclos.entities.access.ChannelPrincipal;
import nl.strohalm.cyclos.entities.accounts.transactions.TransferType;
import nl.strohalm.cyclos.entities.groups.MemberGroup;
import nl.strohalm.cyclos.entities.members.Element;
import nl.strohalm.cyclos.entities.members.Member;
import nl.strohalm.cyclos.entities.members.messages.Message;
import nl.strohalm.cyclos.entities.members.preferences.NotificationPreference;
import nl.strohalm.cyclos.entities.settings.LocalSettings;
import nl.strohalm.cyclos.entities.sms.MemberSmsStatus;
import nl.strohalm.cyclos.services.access.AccessService;
import nl.strohalm.cyclos.services.access.ChannelService;
import nl.strohalm.cyclos.services.elements.ElementService;
import nl.strohalm.cyclos.services.elements.MemberService;
import nl.strohalm.cyclos.services.preferences.PreferenceService;
import nl.strohalm.cyclos.services.settings.SettingsService;
import nl.strohalm.cyclos.services.sms.ISmsContext;
import nl.strohalm.cyclos.utils.RelationshipHelper;
import nl.strohalm.cyclos.utils.access.LoggedUser;
import nl.strohalm.cyclos.utils.conversion.CoercionHelper;
import org.springframework.stereotype.Controller;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;
import nl.strohalm.cyclos.webservices.rest.GenericResponse;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class NotificationPreferenceController extends BaseRestController {

    private PreferenceService preferenceService;
    private ChannelService channelService;
    private MemberService memberService;
    private AccessService accessService;
    private ElementService elementService;
    private SettingsService settingsService;

    public AccessService getAccessService() {
        return accessService;
    }

    public void setAccessService(AccessService accessService) {
        this.accessService = accessService;
    }

    public ElementService getElementService() {
        return elementService;
    }

    public void setElementService(ElementService elementService) {
        this.elementService = elementService;
    }

    public SettingsService getSettingsService() {
        return settingsService;
    }

    public void setSettingsService(SettingsService settingsService) {
        this.settingsService = settingsService;
    }

    @Inject
    public void setChannelService(final ChannelService channelService) {
        this.channelService = channelService;
    }

    @Inject
    public void setMemberService(final MemberService memberService) {
        this.memberService = memberService;
    }

    @Inject
    public void setPreferenceService(final PreferenceService preferenceService) {
        this.preferenceService = preferenceService;
    }

    public static class NotificationPreferenceResponse extends GenericResponse {

        private boolean enableSmsOperations;
        private boolean allowChargingSms;
        private boolean acceptFreeMailing;
        private boolean acceptPaidMailing;
        private long memberId;

        private Map<String, Object> values=new HashMap<String,Object>();

        public NotificationPreferenceResponse() {
        }

        public Map<String, Object> getValues() {
            return values;
        }

        public void setValues(Map<String, Object> values) {
            this.values = values;
        }

        public long getMemberId() {
            return memberId;
        }

        public Map<String, Object> getNotificationPreference() {
            return values;
        }

        public Object getNotificationPreference(final String key) {
            return values.get(key);
        }

        public boolean isAcceptFreeMailing() {
            return acceptFreeMailing;
        }

        public boolean isAcceptPaidMailing() {
            return acceptPaidMailing;
        }

        public boolean isAllowChargingSms() {
            return allowChargingSms;
        }

        public boolean isEnableSmsOperations() {
            return enableSmsOperations;
        }

        public void setAcceptFreeMailing(final boolean acceptFreeMailing) {
            this.acceptFreeMailing = acceptFreeMailing;
        }

        public void setAcceptPaidMailing(final boolean acceptPaidMailing) {
            this.acceptPaidMailing = acceptPaidMailing;
        }

        public void setAllowChargingSms(final boolean allowChargingSms) {
            this.allowChargingSms = allowChargingSms;
        }

        public void setEnableSmsOperations(final boolean enableSmsOperations) {
            this.enableSmsOperations = enableSmsOperations;
        }

        public void setMemberId(final long memberId) {
            this.memberId = memberId;
        }

        public void setNotificationPreference(final Map<String, Object> map) {
            values = map;
        }

        public void setNotificationPreference(final String key, final Object value) {
            values.put(key, value);
        }
        private Map<Message.Type, NotificationPreference> map;
        private Collection<NotificationPreference> list;
        private List<Message.Type> usedTypes;
        private Collection<ChannelPrincipal> principals;
        private Collection<MemberGroup> groups;
        final List<String> channels = new ArrayList<String>();
        // private Map<String, Object> notificationPreference;
        private boolean isEmail;
        private boolean isMessage;
        private boolean isSms;
        private boolean smsEnabled;
        private boolean hasSmsMessages;
        private boolean smsEnabledTypes;
        private Member member;
        private boolean hasEmail;
        private List<Message.Type> types;
        private MemberSmsStatus smsStatus;
        private int MaxFreeSms;
        private int AdditionalChargedSms;
        private String additionalChargeCurrency;
        private String additionalChargePeriod;
        private boolean canChangeChannelsAccess;
        private boolean hasAccessToSmsChannel;
        private boolean showFreeSms;

        public boolean isCanChangeChannelsAccess() {
            return canChangeChannelsAccess;
        }

        public void setCanChangeChannelsAccess(boolean canChangeChannelsAccess) {
            this.canChangeChannelsAccess = canChangeChannelsAccess;
        }

        public boolean isHasAccessToSmsChannel() {
            return hasAccessToSmsChannel;
        }

        public void setHasAccessToSmsChannel(boolean hasAccessToSmsChannel) {
            this.hasAccessToSmsChannel = hasAccessToSmsChannel;
        }

        public boolean isShowFreeSms() {
            return showFreeSms;
        }

        public void setShowFreeSms(boolean showFreeSms) {
            this.showFreeSms = showFreeSms;
        }

        public int getMaxFreeSms() {
            return MaxFreeSms;
        }

        public void setMaxFreeSms(int MaxFreeSms) {
            this.MaxFreeSms = MaxFreeSms;
        }

        public int getAdditionalChargedSms() {
            return AdditionalChargedSms;
        }

        public void setAdditionalChargedSms(int AdditionalChargedSms) {
            this.AdditionalChargedSms = AdditionalChargedSms;
        }

        public String getAdditionalChargeCurrency() {
            return additionalChargeCurrency;
        }

        public void setAdditionalChargeCurrency(String additionalChargeCurrency) {
            this.additionalChargeCurrency = additionalChargeCurrency;
        }

        public String getAdditionalChargePeriod() {
            return additionalChargePeriod;
        }

        public void setAdditionalChargePeriod(String additionalChargePeriod) {
            this.additionalChargePeriod = additionalChargePeriod;
        }

        public MemberSmsStatus getSmsStatus() {
            return smsStatus;
        }

        public void setSmsStatus(MemberSmsStatus smsStatus) {
            this.smsStatus = smsStatus;
        }

        public List<Message.Type> getTypes() {
            return types;
        }

        public void setTypes(List<Message.Type> types) {
            this.types = types;
        }

        public boolean isHasEmail() {
            return hasEmail;
        }

        public void setHasEmail(boolean hasEmail) {
            this.hasEmail = hasEmail;
        }

        public Member getMember() {
            return member;
        }

        public void setMember(Member member) {
            this.member = member;
        }

        public boolean isIsEmail() {
            return isEmail;
        }

        public void setIsEmail(boolean isEmail) {
            this.isEmail = isEmail;
        }

        public boolean isIsMessage() {
            return isMessage;
        }

        public void setIsMessage(boolean isMessage) {
            this.isMessage = isMessage;
        }

        public boolean isIsSms() {
            return isSms;
        }

        public void setIsSms(boolean isSms) {
            this.isSms = isSms;
        }

        public boolean isSmsEnabled() {
            return smsEnabled;
        }

        public void setSmsEnabled(boolean smsEnabled) {
            this.smsEnabled = smsEnabled;
        }

        public boolean isHasSmsMessages() {
            return hasSmsMessages;
        }

        public void setHasSmsMessages(boolean hasSmsMessages) {
            this.hasSmsMessages = hasSmsMessages;
        }

        public boolean isSmsEnabledTypes() {
            return smsEnabledTypes;
        }

        public void setSmsEnabledTypes(boolean smsEnabledTypes) {
            this.smsEnabledTypes = smsEnabledTypes;
        }

        public Map<Message.Type, NotificationPreference> getMap() {
            return map;
        }

        public void setMap(Map<Message.Type, NotificationPreference> map) {
            this.map = map;
        }

        public Collection<NotificationPreference> getList() {
            return list;
        }

        public void setList(Collection<NotificationPreference> list) {
            this.list = list;
        }

        public List<Message.Type> getUsedTypes() {
            return usedTypes;
        }

        public void setUsedTypes(List<Message.Type> usedTypes) {
            this.usedTypes = usedTypes;
        }

        public Collection<ChannelPrincipal> getPrincipals() {
            return principals;
        }

        public void setPrincipals(Collection<ChannelPrincipal> principals) {
            this.principals = principals;
        }

        public Collection<MemberGroup> getGroups() {
            return groups;
        }

        public void setGroups(Collection<MemberGroup> groups) {
            this.groups = groups;
        }

    }

    @RequestMapping(value = "member/notificationPreference", method = RequestMethod.POST)
    @ResponseBody
    public GenericResponse handleSubmit(@RequestBody NotificationPreferenceResponse request) throws Exception {
        GenericResponse response = new GenericResponse();
        long memberId = request.getMemberId();

        if (memberId < 1) {
            memberId = LoggedUser.element().getId();
        }

        // Load member and member group
        final Member member = elementService.load(memberId, RelationshipHelper.nested(Element.Relationships.GROUP, MemberGroup.Relationships.SMS_MESSAGES), Member.Relationships.CHANNELS);

        // Load member notification preferences
        Collection<NotificationPreference> list = preferenceService.load(member);
        if (list == null) {
            list = new ArrayList<NotificationPreference>();
        }

        // Store notification preferences by type
        final Map<Message.Type, NotificationPreference> map = new HashMap<Message.Type, NotificationPreference>();
        for (final NotificationPreference preference : list) {
            map.put(preference.getType(), preference);
        }

        // Check if the member have e-mail
        final boolean hasEmail = StringUtils.isNotEmpty(member.getEmail());

        final LocalSettings localSettings = settingsService.getLocalSettings();
        final boolean smsEnabled = localSettings.isSmsEnabled();
        boolean hasNotificationsBySms = false;
        // Save the notification preferences
        final List<Message.Type> usedTypes = preferenceService.listNotificationTypes(member);
        for (final Message.Type type : Message.Type.values()) {
            // final String isEmailStr = (String) form.getNotificationPreference(type.name() + "_email");
            final String isEmailStr = (String) request.getNotificationPreference(type.name() + "_email");
            final String isMessageStr = (String) request.getNotificationPreference(type.name() + "_message");
            final String isSmsStr = (String) request.getNotificationPreference(type.name() + "_sms");

            boolean isEmail = false;
            boolean isMessage = false;
            boolean isSms = false;

            // If not use type, use both as false
            if (usedTypes.contains(type)) {
                isEmail = hasEmail ? CoercionHelper.coerce(Boolean.TYPE, isEmailStr) : false;
                isMessage = CoercionHelper.coerce(Boolean.TYPE, isMessageStr);
                if (type == Message.Type.FROM_ADMIN_TO_MEMBER || type == Message.Type.FROM_ADMIN_TO_GROUP) {
                    isMessage = true;
                }
                if (smsEnabled) {
                    isSms = CoercionHelper.coerce(Boolean.TYPE, isSmsStr);
                }
            }
            hasNotificationsBySms |= isSms;
            NotificationPreference preference = map.get(type);
            if (preference == null && (isEmail || isMessage || isSms)) {
                // Insert new notification preference
                preference = new NotificationPreference();
                preference.setType(type);
                preference.setEmail(isEmail);
                preference.setMessage(isMessage);
                preference.setSms(isSms);
                map.put(type, preference);
            } else if (preference != null) {
                // Update an existing notification preference
                preference.setEmail(isEmail);
                preference.setMessage(isMessage);
                preference.setSms(isSms);
            }
        }
        preferenceService.save(member, map.values());

        if (smsEnabled) {
            // Store the sms operations channel
            final Channel smsChannel = channelService.getSmsChannel();

            if (accessService.canChangeChannelsAccess(member) && smsChannel != null) {
                final Set<Channel> channels = new HashSet<Channel>(accessService.getChannelsEnabledForMember(member));
                if (request.isEnableSmsOperations()) {
                    channels.add(smsChannel);
                } else {
                    channels.remove(smsChannel);
                }
                accessService.changeChannelsAccess(member, channels, false);
            }
            // The other flags come from the member sms status
            preferenceService.saveSmsStatusPreferences(member, request.isAcceptFreeMailing(), request.isAcceptPaidMailing(), request.isAllowChargingSms(), hasNotificationsBySms);
        }

        response.setMessage("notificationPreferences.modified");
        response.setStatus(0);
        return response;
    }

    @RequestMapping(value = "member/notificationPreference", method = RequestMethod.GET)
    @ResponseBody
    public NotificationPreferenceResponse prepareForm() throws Exception {
        NotificationPreferenceResponse response = new NotificationPreferenceResponse();
        long memberId = LoggedUser.user().getId();

        if (memberId < 1) {
            memberId = LoggedUser.element().getId();
        }

        // Load member and member group
        final Member member = elementService.load(memberId, RelationshipHelper.nested(Element.Relationships.GROUP, MemberGroup.Relationships.SMS_MESSAGES), Member.Relationships.CHANNELS);
       // response.setMember(member);

        final LocalSettings localSettings = settingsService.getLocalSettings();

        // Check which messages types can be sent by sms (group setting)
        final Collection<Message.Type> smsMessages = member.getMemberGroup().getSmsMessages();
        final boolean hasSmsMessages = CollectionUtils.isNotEmpty(smsMessages);
        final boolean smsEnabled = localSettings.isSmsEnabled();
        response.setSmsEnabled(smsEnabled);
        response.setHasSmsMessages(hasSmsMessages);
        response.setSmsEnabledTypes(smsEnabled);

        // Load member notification preferences
        Collection<NotificationPreference> list = preferenceService.load(member);
        if (list == null) {
            list = new ArrayList<NotificationPreference>();
        }

        // Store notification preferences by type
        final Map<Message.Type, NotificationPreference> map = new HashMap<Message.Type, NotificationPreference>();
        for (final NotificationPreference preference : list) {
            map.put(preference.getType(), preference);
        }

        // Check if the member have e-mail
        final boolean hasEmail = StringUtils.isNotEmpty(member.getEmail()) ? true : false;
        response.setHasEmail(hasEmail);

        final List<Message.Type> types = preferenceService.listNotificationTypes(member);
        for (final Message.Type type : types) {
            final NotificationPreference preference = map.get(type);
            final String typeMessage = type.name() + "_message";
            final String typeEmail = type.name() + "_email";
            final String typeSms = type.name() + "_sms";
            System.out.println(typeMessage);
            final boolean isEmail = preference != null ? preference.isEmail() : false;
            boolean isMessage = preference != null ? preference.isMessage() : false;
            if (type == Message.Type.FROM_ADMIN_TO_MEMBER || type == Message.Type.FROM_ADMIN_TO_GROUP) {
                isMessage = true;
            }
            final boolean isSms = preference != null ? preference.isSms() : false;
            System.out.println(isMessage);
            response.setNotificationPreference(typeMessage, isMessage);
            response.setNotificationPreference(typeEmail, isEmail);
            response.setNotificationPreference(typeSms, isSms);
        }
        response.setTypes(types);

        if (smsEnabled) {
            final ISmsContext smsContext = memberService.getSmsContext(member);
            response.setMaxFreeSms(smsContext.getFreeSms(member));
            response.setAdditionalChargedSms(smsContext.getAdditionalChargedSms(member));
            // response.setAdditionalChargeAmount(smsContext.getAdditionalChargeAmount(member));
            final TransferType tt = member.getMemberGroup().getMemberSettings().getSmsChargeTransferType();
            response.setAdditionalChargeCurrency(tt == null ? null : tt.getCurrency().toString());
            response.setAdditionalChargePeriod(smsContext.getAdditionalChargedPeriod(member).toString());

            final MemberSmsStatus smsStatus = preferenceService.getMemberSmsStatus(member);
            response.setShowFreeSms(smsContext.showFreeSms(smsStatus));
            final Channel smsChannel = channelService.getSmsChannel();
            if (smsChannel != null) {
                final boolean hasAccessToSmsChannel = member.getMemberGroup().getChannels().contains(smsChannel);
                response.setEnableSmsOperations(accessService.isChannelEnabledForMember(smsChannel.getInternalName(), member));
                response.setHasAccessToSmsChannel(hasAccessToSmsChannel);
                if (hasAccessToSmsChannel) {
                    //   response.setCanChangeChannelAccess(accessService.canChangeChannelsAccess(member));
                    response.setCanChangeChannelsAccess(accessService.canChangeChannelsAccess(member));
                }
            }
            response.setAllowChargingSms(smsStatus.isAllowChargingSms());
            response.setAcceptFreeMailing(smsStatus.isAcceptFreeMailing());
            response.setAcceptPaidMailing(smsStatus.isAcceptPaidMailing());
            response.setSmsStatus(smsStatus);
            response.setAcceptFreeMailing(hasEmail);
            response.setAcceptPaidMailing(hasEmail);
            response.setAdditionalChargedSms(0);
            response.setAllowChargingSms(hasEmail);
            response.setCanChangeChannelsAccess(hasSmsMessages);
            response.setEnableSmsOperations(hasSmsMessages);
            response.setHasAccessToSmsChannel(hasSmsMessages);
            response.setHasEmail(hasEmail);
            response.setHasSmsMessages(hasSmsMessages);
            response.setIsEmail(hasEmail);
            response.setIsMessage(hasSmsMessages);
            response.setIsSms(hasEmail);
            response.setList(list);
            response.setMap(map);
            response.setMaxFreeSms(0);
            response.setMember(member);
            response.setMemberId(memberId);
            response.setShowFreeSms(hasEmail);
            response.setTypes(types);
            response.setUsedTypes(types);
            response.setStatus(0);

        }
        return response;
    }

}
