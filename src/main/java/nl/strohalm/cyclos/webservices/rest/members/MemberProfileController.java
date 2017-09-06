/*
    This file is part of Cyclos (www.cyclos.org).
    A project of the Social Trade Organisation (www.socialtrade.org).

    Cyclos is free software; you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation; either version 2 of the License, or
    (at your option) any later version.

    Cyclos is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with Cyclos; if not, write to the Free Software
    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA

 */
package nl.strohalm.cyclos.webservices.rest.members;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


import nl.strohalm.cyclos.access.AdminMemberPermission;
import nl.strohalm.cyclos.access.BrokerPermission;
import nl.strohalm.cyclos.access.MemberPermission;
import nl.strohalm.cyclos.annotations.Inject;
import nl.strohalm.cyclos.entities.Relationship;
import nl.strohalm.cyclos.entities.access.Channel;
import nl.strohalm.cyclos.entities.access.MemberUser;
import nl.strohalm.cyclos.entities.access.User;
import nl.strohalm.cyclos.entities.customization.fields.MemberCustomField;
import nl.strohalm.cyclos.entities.customization.fields.MemberCustomField.Access;
import nl.strohalm.cyclos.entities.groups.AdminGroup;
import nl.strohalm.cyclos.entities.groups.Group;
import nl.strohalm.cyclos.entities.groups.GroupFilter;
import nl.strohalm.cyclos.entities.groups.GroupFilterQuery;
import nl.strohalm.cyclos.entities.groups.MemberGroup;
import nl.strohalm.cyclos.entities.members.Administrator;
import nl.strohalm.cyclos.entities.members.Element;
import nl.strohalm.cyclos.entities.members.Member;
import nl.strohalm.cyclos.entities.members.Operator;
import nl.strohalm.cyclos.entities.members.PendingEmailChange;
import nl.strohalm.cyclos.entities.members.Reference.Nature;
import nl.strohalm.cyclos.entities.members.records.MemberRecordType;
import nl.strohalm.cyclos.entities.settings.AccessSettings.UsernameGeneration;
import nl.strohalm.cyclos.exceptions.MailSendingException;
import nl.strohalm.cyclos.exceptions.PermissionDeniedException;
import nl.strohalm.cyclos.services.access.AccessService;
import nl.strohalm.cyclos.services.access.exceptions.NotConnectedException;
import nl.strohalm.cyclos.services.accounts.AccountService;
import nl.strohalm.cyclos.services.customization.MemberCustomFieldService;
import nl.strohalm.cyclos.services.elements.ElementService;
import nl.strohalm.cyclos.services.elements.MemberRecordService;
import nl.strohalm.cyclos.services.elements.ReferenceService;
import nl.strohalm.cyclos.services.groups.GroupFilterService;
import nl.strohalm.cyclos.services.groups.GroupService;
import nl.strohalm.cyclos.services.permissions.PermissionService;
import nl.strohalm.cyclos.services.settings.SettingsService;
import nl.strohalm.cyclos.utils.CustomFieldHelper;
import nl.strohalm.cyclos.utils.MessageResolver;
import nl.strohalm.cyclos.utils.RelationshipHelper;
import nl.strohalm.cyclos.utils.access.LoggedUser;
import nl.strohalm.cyclos.utils.binding.MapBean;
import nl.strohalm.cyclos.utils.validation.ValidationException;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Profile action for members
 *
 * @author luis
 * @author Jefferson Magno
 */
@Controller
public class MemberProfileController extends BaseRestController {

    private static final Relationship[] FETCH = {
        RelationshipHelper.nested(User.Relationships.ELEMENT, Element.Relationships.GROUP),
        RelationshipHelper.nested(User.Relationships.ELEMENT, Member.Relationships.BROKER),
        RelationshipHelper.nested(User.Relationships.ELEMENT, Member.Relationships.CUSTOM_VALUES)
    };

    private AccountService accountService;
    private MemberCustomFieldService memberCustomFieldService;
    private GroupFilterService groupFilterService;
    private MemberRecordService memberRecordService;
    private ReferenceService referenceService;
    private ElementService elementService;
    private AccessService accessService;
    private PermissionService permissionService;
    private GroupService groupService;
    private SettingsService settingsService;
    private MessageResolver messageResolver;

    private CustomFieldHelper customFieldHelper;

    public ElementService getElementService() {
        return elementService;
    }

    public void setElementService(ElementService elementService) {
        this.elementService = elementService;
    }

    public AccessService getAccessService() {
        return accessService;
    }

    public void setAccessService(AccessService accessService) {
        this.accessService = accessService;
    }

    public PermissionService getPermissionService() {
        return permissionService;
    }

    public void setPermissionService(PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    public GroupService getGroupService() {
        return groupService;
    }

    public void setGroupService(GroupService groupService) {
        this.groupService = groupService;
    }

    public SettingsService getSettingsService() {
        return settingsService;
    }

    public void setSettingsService(SettingsService settingsService) {
        this.settingsService = settingsService;
    }

    @Inject
    public void setAccountService(final AccountService accountService) {
        this.accountService = accountService;
    }

    @Inject
    public void setCustomFieldHelper(final CustomFieldHelper customFieldHelper) {
        this.customFieldHelper = customFieldHelper;
    }

    @Inject
    public void setGroupFilterService(final GroupFilterService groupFilterService) {
        this.groupFilterService = groupFilterService;
    }

    
    @Inject
    public void setMemberCustomFieldService(final MemberCustomFieldService memberCustomFieldService) {
        this.memberCustomFieldService = memberCustomFieldService;
    }

    @Inject
    public void setMemberRecordService(final MemberRecordService memberRecordService) {
        this.memberRecordService = memberRecordService;
    }

    @Inject
    public void setReferenceService(final ReferenceService referenceService) {
        this.referenceService = referenceService;
    }
    @Inject
    public void setMessageResolver(MessageResolver messageResolver) {
        this.messageResolver = messageResolver;
    }
    
    

//    @Override
//    @SuppressWarnings("unchecked")
//    protected <CFV extends CustomFieldValue> Class<CFV> getCustomFieldValueClass() {
//        return (Class<CFV>) MemberCustomFieldValue.class;
//    }
    //@Override
    protected Class<Member> getElementClass() {
        return Member.class;
    }

    //@Override
    @SuppressWarnings("unchecked")
    protected <G extends Group> Class<G> getGroupClass() {
        return (Class<G>) MemberGroup.class;
    }

    //@Override
    @SuppressWarnings("unchecked")
    protected <U extends User> Class<U> getUserClass() {
        return (Class<U>) MemberUser.class;
    }

    public static class MemberProfileControllerRequestDTO {

        private long memberId;

        public MemberProfileControllerRequestDTO() {
            setMember("user", new MapBean("id", "username"));
            setMember("group", new MapBean("id", "name"));
            setMember("broker", new MapBean("id", "name"));
            setMember("customValues", new MapBean(true, "field", "value", "hidden"));
        }

        public Map<String, Object> getMember(Map<String, Object> values) {
            return values;
        }

//    public Object getMember(final String key,final Object value) {
//       return values;
//    }
        public long getMemberId() {
            return memberId;
        }

        public void setMember(final Map<String, Object> map) {

        }

        public final void setMember(final String key, final Object value) {
            //values.put(key, value);
        }

        public void setMemberId(final long memberId) {
            this.memberId = memberId;
        }

    }

    public static class MemberProfileControllerResponseDTO {

        private Member member;
        private boolean hasAccounts;
        private boolean removed;
        private boolean disabledLogin;
        private boolean editableFields;
        private boolean canChangeName;
        private boolean canChangeEmail;
        private boolean canChangeUsername;
        private boolean pendingEmailChange;
        private boolean editable;
        private boolean byBroker;
        private boolean myProfile;
        private boolean profileOfOtherMember;
        private boolean profileOfBrokered;
        private boolean operatorCanViewReports;
        private boolean hasCardType;
        String message;
        private boolean isLoggedIn;
        private boolean memberCanAccessExternalChannels;
        private boolean hasTransactionFeedbacks;
        private String groupFilters;
        private Map<MemberRecordType, Integer>  countByRecordType;
        private Collection<CustomFieldHelper.Entry> customFields;

        public Collection<CustomFieldHelper.Entry> getCustomFields() {
            return customFields;
        }

        public void setCustomFields(Collection<CustomFieldHelper.Entry> customFields) {
            this.customFields = customFields;
        }
        

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
        

        public Member getMember() {
            return member;
        }

        public void setMember(Member member) {
            this.member = member;
        }

        public boolean isHasAccounts() {
            return hasAccounts;
        }

        public void setHasAccounts(boolean hasAccounts) {
            this.hasAccounts = hasAccounts;
        }

        public boolean isRemoved() {
            return removed;
        }

        public void setRemoved(boolean removed) {
            this.removed = removed;
        }

        public boolean isDisabledLogin() {
            return disabledLogin;
        }

        public void setDisabledLogin(boolean disabledLogin) {
            this.disabledLogin = disabledLogin;
        }
        public boolean isEditableFields() {
            return editableFields;
        }

        public void setEditableFields(boolean editableFields) {
            this.editableFields = editableFields;
        }

        public boolean isCanChangeName() {
            return canChangeName;
        }

        public void setCanChangeName(boolean canChangeName) {
            this.canChangeName = canChangeName;
        }

        public boolean isCanChangeEmail() {
            return canChangeEmail;
        }

        public void setCanChangeEmail(boolean canChangeEmail) {
            this.canChangeEmail = canChangeEmail;
        }

        public boolean isCanChangeUsername() {
            return canChangeUsername;
        }

        public void setCanChangeUsername(boolean canChangeUsername) {
            this.canChangeUsername = canChangeUsername;
        }

        public boolean isPendingEmailChange() {
            return pendingEmailChange;
        }

        public void setPendingEmailChange(boolean pendingEmailChange) {
            this.pendingEmailChange = pendingEmailChange;
        }

        public boolean isEditable() {
            return editable;
        }

        public void setEditable(boolean editable) {
            this.editable = editable;
        }

        public boolean isByBroker() {
            return byBroker;
        }

        public void setByBroker(boolean byBroker) {
            this.byBroker = byBroker;
        }

        public boolean isMyProfile() {
            return myProfile;
        }

        public void setMyProfile(boolean myProfile) {
            this.myProfile = myProfile;
        }

        public boolean isProfileOfOtherMember() {
            return profileOfOtherMember;
        }

        public void setProfileOfOtherMember(boolean profileOfOtherMember) {
            this.profileOfOtherMember = profileOfOtherMember;
        }

        public boolean isProfileOfBrokered() {
            return profileOfBrokered;
        }

        public void setProfileOfBrokered(boolean profileOfBrokered) {
            this.profileOfBrokered = profileOfBrokered;
        }

        public boolean isOperatorCanViewReports() {
            return operatorCanViewReports;
        }

        public void setOperatorCanViewReports(boolean operatorCanViewReports) {
            this.operatorCanViewReports = operatorCanViewReports;
        }

        public boolean isHasCardType() {
            return hasCardType;
        }

        public void setHasCardType(boolean hasCardType) {
            this.hasCardType = hasCardType;
        }
        
        public  MemberProfileControllerResponseDTO(){
        }

        public boolean isIsLoggedIn() {
            return isLoggedIn;
        }

        public void setIsLoggedIn(boolean isLoggedIn) {
            this.isLoggedIn = isLoggedIn;
        }

        public boolean isMemberCanAccessExternalChannels() {
            return memberCanAccessExternalChannels;
        }

        public void setMemberCanAccessExternalChannels(boolean memberCanAccessExternalChannels) {
            this.memberCanAccessExternalChannels = memberCanAccessExternalChannels;
        }

        public boolean isHasTransactionFeedbacks() {
            return hasTransactionFeedbacks;
        }

        public void setHasTransactionFeedbacks(boolean hasTransactionFeedbacks) {
            this.hasTransactionFeedbacks = hasTransactionFeedbacks;
        }

        public String getGroupFilters() {
            return groupFilters;
        }

        public void setGroupFilters(String groupFilters) {
            this.groupFilters = groupFilters;
        }

        public Map<MemberRecordType, Integer> getCountByRecordType() {
            return countByRecordType;
        }

        public void setCountByRecordType(Map<MemberRecordType, Integer> countByRecordType) {
            this.countByRecordType = countByRecordType;
        }
        public static class MemberDataResponse{
            private String memberLogin;
            private String fullName;
            private String eMail;
            private String birthday;
            private String gender;
            private String address;
            private String postalCode;
            private String city;
            private String area;
            private String phone;
            private String mobilePhone;
            private String fax;
            private String url;

            public String getMemberLogin() {
                return memberLogin;
            }

            public void setMemberLogin(String memberLogin) {
                this.memberLogin = memberLogin;
            }

            public String getFullName() {
                return fullName;
            }

            public void setFullName(String fullName) {
                this.fullName = fullName;
            }

            public String geteMail() {
                return eMail;
            }

            public void seteMail(String eMail) {
                this.eMail = eMail;
            }

            public String getBirthday() {
                return birthday;
            }

            public void setBirthday(String birthday) {
                this.birthday = birthday;
            }

            public String getGender() {
                return gender;
            }

            public void setGender(String gender) {
                this.gender = gender;
            }

            public String getAddress() {
                return address;
            }

            public void setAddress(String address) {
                this.address = address;
            }

            public String getPostalCode() {
                return postalCode;
            }

            public void setPostalCode(String postalCode) {
                this.postalCode = postalCode;
            }

            public String getCity() {
                return city;
            }

            public void setCity(String city) {
                this.city = city;
            }

            public String getArea() {
                return area;
            }

            public void setArea(String area) {
                this.area = area;
            }

            public String getPhone() {
                return phone;
            }

            public void setPhone(String phone) {
                this.phone = phone;
            }

            public String getMobilePhone() {
                return mobilePhone;
            }

            public void setMobilePhone(String mobilePhone) {
                this.mobilePhone = mobilePhone;
            }

            public String getFax() {
                return fax;
            }

            public void setFax(String fax) {
                this.fax = fax;
            }

            public String getUrl() {
                return url;
            }

            public void setUrl(String url) {
                this.url = url;
            }
            
            
            
            
        }
    }
   
        
  
  
    @RequestMapping(value = "member/memberProfile", method = RequestMethod.GET)
    @ResponseBody
    @SuppressWarnings("unchecked")
    public MemberProfileControllerResponseDTO memberProfile() throws Exception {
        MemberProfileControllerResponseDTO response = new MemberProfileControllerResponseDTO();
        // final MemberProfileForm form = context.getForm();
        final boolean profileOfBrokered = false;
        boolean myProfile = false;
        boolean profileOfOtherMember = false;
        boolean operatorCanViewReports = false;
        MemberUser memberUser = null;
        //final HttpServletRequest request = context.getRequest();

        final Element loggedElement = LoggedUser.element();
        long memberId=LoggedUser.group().getId();
        // Load the user
        //if (memberId > 0 && form.getMemberId() != loggedElement.getId()) {
        if (memberId > 0 && memberId != loggedElement.getId()) {
            final User loaded = elementService.loadUser(memberId, FETCH);
            if (loaded instanceof MemberUser) {
                memberUser = (MemberUser) loaded;
                profileOfOtherMember = true;
            }
            if (LoggedUser.isAdministrator()) {
                try {
                    response.setIsLoggedIn(accessService.isLoggedIn(memberUser));
                } catch (final NotConnectedException e) {
                    // Ok - user is not online
                }
            }
            if (LoggedUser.isOperator()) {
                final Operator operator = LoggedUser.element();
                if (!memberUser.getMember().equals(operator.getMember())) {
                    // Operator viewing other member's profile
                    operatorCanViewReports = permissionService.hasPermission(MemberPermission.REPORTS_VIEW);
                }
            }
        }
        if (memberUser == null && LoggedUser.isMember()) {
            memberUser = elementService.loadUser(LoggedUser.user().getId(), FETCH);
            myProfile = true;
        }
        if (memberUser == null) {
            throw new ValidationException();
        }

        // Check whether the logged member can see this profile
        final Member member = memberUser.getMember();
        if (!loggedElement.equals(member)) {
            if (loggedElement instanceof Administrator) {
                // An admin must manage the member's group
                final AdminGroup group = groupService.load(LoggedUser.group().getId(), AdminGroup.Relationships.MANAGES_GROUPS);
                if (!group.getManagesGroups().contains(member.getGroup())) {
                    throw new PermissionDeniedException();
                }
            } else {
                // A member must be able to view the member's profile...
                final MemberGroup group = groupService.load(((Member) LoggedUser.accountOwner()).getGroup().getId(), MemberGroup.Relationships.CAN_VIEW_PROFILE_OF_GROUPS);
                if (!group.getCanViewProfileOfGroups().contains(member.getGroup())) {
                    // ... but when he's the broker, show anyway
                    if (!LoggedUser.isBroker()) {
                        throw new PermissionDeniedException();
                    }
                }
            }
        }
        // Check if the member can access external channels
        boolean memberCanAccessExternalChannels = false;
        final MemberGroup group = groupService.load(member.getMemberGroup().getId(), MemberGroup.Relationships.CHANNELS);
        for (final Channel current : group.getChannels()) {
            if (!Channel.WEB.equals(current.getInternalName())) {
                memberCanAccessExternalChannels = true;
            }
        }
        response.setMemberCanAccessExternalChannels( memberCanAccessExternalChannels);

        // Check whether the given member has transaction feedbacks
        final Collection<Nature> referenceNatures = referenceService.getNaturesByGroup(member.getMemberGroup());
        final boolean hasTransactionFeedbacks = referenceNatures.contains(Nature.TRANSACTION);
         response.setHasTransactionFeedbacks(hasTransactionFeedbacks);

        // Check if the member belongs to a group managed by the admin
        if (LoggedUser.isAdministrator()) {
            AdminGroup adminGroup = LoggedUser.group();
            adminGroup = groupService.load(adminGroup.getId(), AdminGroup.Relationships.MANAGES_GROUPS);
            if (!adminGroup.getManagesGroups().contains(member.getGroup())) {
                throw new PermissionDeniedException();
            }
        }

        // getReadDataBinder(context).writeAsString(form.getMember(), member);
        // Retrieve the group filters
        if (LoggedUser.isMember()) {
            final GroupFilterQuery groupFilterQuery = new GroupFilterQuery();
            groupFilterQuery.setGroup(memberUser.getMember().getMemberGroup());
            final Collection<GroupFilter> groupFilters = groupFilterService.search(groupFilterQuery);
            if (groupFilters.size() > 0) {
                final StringBuilder groupFiltersStr = new StringBuilder();
                for (final GroupFilter groupFilter : groupFilters) {
                    if (groupFilter.isShowInProfile()) {
                        if (!"".equals(groupFiltersStr.toString())) {
                            groupFiltersStr.append(", ");
                        }
                        groupFiltersStr.append(groupFilter.getName());
                    }
                }
                if (!"".equals(groupFiltersStr.toString())) {
                    response.setGroupFilters(groupFiltersStr.toString());
                }
            }
        }

//        // Retrieve the images
//        final List<MemberImage> images = (List<MemberImage>) imageService.listByOwner(member);
//        final MemberGroupSettings groupSettings = member.getMemberGroup().getMemberSettings();
//        final boolean maxImages = groupSettings == null ? true : images.size() >= groupSettings.getMaxImagesPerMember();

        // Check the permissions
        final boolean usernameGenerated = settingsService.getAccessSettings().getUsernameGeneration() != UsernameGeneration.NONE;
        boolean editable = myProfile;
        boolean byBroker = false;
        boolean canChangeName = false;
        boolean canChangeUsername = false;
        boolean canChangeEmail = false;
        final boolean removed = member.getGroup().getStatus() == Group.Status.REMOVED;
        if (!myProfile) {
            boolean canViewRecords = false;
            if (LoggedUser.isAdministrator()) {
                // Check if the member has remarks
                editable = permissionService.hasPermission(AdminMemberPermission.MEMBERS_CHANGE_PROFILE);
                canViewRecords = permissionService.hasPermission(AdminMemberPermission.RECORDS_VIEW);
                canChangeName = editable && permissionService.hasPermission(AdminMemberPermission.MEMBERS_CHANGE_NAME);
                canChangeEmail = editable && permissionService.hasPermission(AdminMemberPermission.MEMBERS_CHANGE_EMAIL);
                canChangeUsername = !usernameGenerated && editable && permissionService.hasPermission(AdminMemberPermission.MEMBERS_CHANGE_USERNAME);
            } else {
                // Check if the member is by broker
                byBroker = LoggedUser.isBroker();
                if (byBroker) {
                    editable = permissionService.hasPermission(BrokerPermission.MEMBERS_CHANGE_PROFILE);
                    canViewRecords = permissionService.hasPermission(BrokerPermission.MEMBER_RECORDS_VIEW);
                    canChangeName = editable && permissionService.hasPermission(BrokerPermission.MEMBERS_CHANGE_NAME);
                    canChangeEmail = editable && permissionService.hasPermission(BrokerPermission.MEMBERS_CHANGE_EMAIL);
                    canChangeUsername = !usernameGenerated && editable && permissionService.hasPermission(BrokerPermission.MEMBERS_CHANGE_USERNAME);
                }
            }
            if (canViewRecords) {
             
                 response.setCountByRecordType( memberRecordService.countByType(member));
            }
        } else {
            canChangeName = permissionService.hasPermission(MemberPermission.PROFILE_CHANGE_NAME);
            canChangeEmail = permissionService.hasPermission(MemberPermission.PROFILE_CHANGE_EMAIL);
            canChangeUsername = !usernameGenerated && permissionService.hasPermission(MemberPermission.PROFILE_CHANGE_USERNAME);
        }

        final Group loggedGroup = LoggedUser.group();
        // Retrieve the custom fields
        final List<MemberCustomField> allFields = memberCustomFieldService.list();
        List<MemberCustomField> customFields;
        if (removed) {
            // Removed members are view-only, and will display the values for all fields the member had a value
            customFields = allFields;
        } else {
            customFields = customFieldHelper.onlyForGroup(allFields, member.getMemberGroup());
        }
        // This map will store, for each field, if it is editable or not
        final Map<MemberCustomField, Boolean> editableFields = new HashMap<MemberCustomField, Boolean>();
        for (final Iterator<MemberCustomField> it = customFields.iterator(); it.hasNext();) {
            final MemberCustomField field = it.next();
            // Check if the field is visible
            final Access visibility = field.getVisibilityAccess();
            if (visibility != null && !visibility.granted(loggedGroup, myProfile, byBroker, false, false)) {
                it.remove();
            }
            // Check if the field can be updated
            final Access update = field.getUpdateAccess();
            editableFields.put(field, update != null && update.granted(loggedGroup, myProfile, byBroker, false, false));
        }

        // Check if logged user belongs to a group with card type associated - for members only
        boolean hasCardType = false;
        if (member.getMemberGroup().getCardType() != null) {
            hasCardType = true;
        }
        
     

//        PendingEmailChange pendingEmailChange = null;
//        if (editable) {
//            pendingEmailChange = elementService.getPendingEmailChange(member);
//        }
        

        // Store the request attributes
        response.setMember(member);
        response.setRemoved(editable);
        response.setHasAccounts(accountService.hasAccounts(member));
        response.setDisabledLogin(accessService.isLoginBlocked(member.getUser()));
       
        response.setCustomFields(customFieldHelper.buildEntries(customFields,member.getCustomValues()));
        response.setEditableFields(editable);
        response.setCanChangeName(canChangeName);
        response.setCanChangeEmail(canChangeEmail);
        response.setCanChangeUsername(canChangeUsername);
        response.setPendingEmailChange(canChangeEmail);
        response.setByBroker(byBroker);
        response.setMyProfile(myProfile);
        response.setProfileOfOtherMember(profileOfOtherMember);
        response.setProfileOfBrokered(profileOfBrokered);
        response.setOperatorCanViewReports(operatorCanViewReports);
        response.setHasCardType(hasCardType);
        

//        if (editable) {
//            return context.getInputForward();
//        } else {
//            return context.findForward("view");
//        }
               
       
       return response;
    }

    @RequestMapping(value = "member/memberProfile", method = RequestMethod.POST)
    @ResponseBody
    public MemberProfileControllerResponseDTO handleSubmit() throws Exception {
       // final MemberProfileForm form = context.getForm();
        // Save the member
        MemberProfileControllerResponseDTO response = new MemberProfileControllerResponseDTO();
       // long memberId=LoggedUser.user().getId();
        Member member = LoggedUser.member();

        // Load the member's broker
        Member currentMember;
        try {
            currentMember = elementService.load(member.getId(), Member.Relationships.BROKER);
        } catch (final ClassCastException e) {
            throw new ValidationException();
        }
        final Member broker = currentMember.getBroker();
        member.setBroker(broker);

        if (member.isTransient()) {
            throw new ValidationException();
        }

        // Save the member, checking if a pending e-mail change has been created
        final boolean hadPendingEmailChange = elementService.getPendingEmailChange(member) != null;
        try {
            member = elementService.changeProfile(member);
        } catch (final MailSendingException e) {
            response.setMessage(messageResolver.message("profile.error.changeEmailValidationFailed","Email"));
        }
        final PendingEmailChange pendingEmailChange = elementService.getPendingEmailChange(member);

        // Save the uploaded image
//        final FormFile upload = form.getPicture();
//        if (upload != null && upload.getFileSize() > 0) {
//            try {
//                imageService.save(member, form.getPictureCaption(), ImageType.getByContentType(upload.getContentType()), upload.getFileName(), upload.getInputStream());
//            } finally {
//                upload.destroy();
//            }
//        }

        if (!hadPendingEmailChange && pendingEmailChange != null) {
            //Context.sendMessage("profile.modified.emailPending", pendingEmailChange.getNewEmail());
            response.setMessage(messageResolver.message("profile.modified","AdminProfile"));
        } else {
            response.setMessage("profile.modified");
        }
        return response;

        //return ActionHelper.redirectWithParam(context.getRequest(), super.handleSubmit(context), "memberId", member.getId());
    }

//    @Override
//    protected DataBinder<Member> initDataBinderForRead(final ActionContext context) {
//        final BeanBinder<Member> dataBinder = (BeanBinder<Member>) super.initDataBinderForRead(context);
//        dataBinder.registerBinder("hideEmail", PropertyBinder.instance(Boolean.TYPE, "hideEmail"));
//        return dataBinder;
//    }
//    @Override
//    @SuppressWarnings({ "unchecked", "rawtypes" })
//    protected DataBinder<Member> initDataBinderForWrite(final ActionContext context) {
//        final BeanBinder<Member> dataBinder = (BeanBinder<Member>) super.initDataBinderForWrite(context);
//        dataBinder.registerBinder("hideEmail", PropertyBinder.instance(Boolean.TYPE, "hideEmail"));
//
//        final BeanBinder<? extends User> userBinder = BeanBinder.instance(getUserClass(), "user");
//        userBinder.registerBinder("username", PropertyBinder.instance(String.class, "username"));
//        dataBinder.registerBinder("user", userBinder);
//
//        // Add another custom field value attribute: hidden
//        final BeanCollectionBinder collectionBinder = (BeanCollectionBinder) dataBinder.getMappings().get("customValues");
//        final BeanBinder elementBinder = (BeanBinder) collectionBinder.getElementBinder();
//        elementBinder.registerBinder("hidden", PropertyBinder.instance(Boolean.TYPE, "hidden"));
//
//        return dataBinder;
//    }
//    @Override
//    protected void validateForm(final ActionContext context) {
//        final Member member = resolveMember(context);
//        elementService.validate(member, WhenSaving.PROFILE, false);
//    }
//
//    private Member resolveMember(final ActionContext context) {
//        final MemberProfileForm form = context.getForm();
//        return getWriteDataBinder(context).readFromString(form.getMember());
//    }
}