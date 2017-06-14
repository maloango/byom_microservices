package nl.strohalm.cyclos.webservices.rest.groups;

import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import nl.strohalm.cyclos.access.MemberPermission;
import nl.strohalm.cyclos.access.Permission;
import nl.strohalm.cyclos.annotations.Inject;
import nl.strohalm.cyclos.controls.ActionContext;
import nl.strohalm.cyclos.controls.groups.EditGroupForm;
import nl.strohalm.cyclos.entities.access.Channel;
import nl.strohalm.cyclos.entities.access.TransactionPassword;
import nl.strohalm.cyclos.entities.access.User;
import nl.strohalm.cyclos.entities.accounts.AccountType;
import nl.strohalm.cyclos.entities.accounts.cards.CardType;
import nl.strohalm.cyclos.entities.accounts.transactions.TransferType;
import nl.strohalm.cyclos.entities.accounts.transactions.TransferTypeQuery;
import nl.strohalm.cyclos.entities.customization.files.CustomizedFile;
import nl.strohalm.cyclos.entities.customization.files.CustomizedFileQuery;
import nl.strohalm.cyclos.entities.groups.AdminGroup;
import nl.strohalm.cyclos.entities.groups.BasicGroupSettings;
import nl.strohalm.cyclos.entities.groups.BasicGroupSettings.PasswordPolicy;
import nl.strohalm.cyclos.entities.groups.BrokerGroup;
import nl.strohalm.cyclos.entities.groups.Group;
import nl.strohalm.cyclos.entities.groups.Group.Nature;
import nl.strohalm.cyclos.entities.groups.GroupQuery;
import nl.strohalm.cyclos.entities.groups.MemberGroup;
import nl.strohalm.cyclos.entities.groups.MemberGroupSettings;
import nl.strohalm.cyclos.entities.groups.MemberGroupSettings.EmailValidation;
import nl.strohalm.cyclos.entities.groups.OperatorGroup;
import nl.strohalm.cyclos.entities.groups.SystemGroup;
import nl.strohalm.cyclos.entities.members.Element;
import nl.strohalm.cyclos.entities.members.Member;
import nl.strohalm.cyclos.entities.members.RegistrationAgreement;
import nl.strohalm.cyclos.entities.members.messages.Message;
import nl.strohalm.cyclos.entities.members.messages.Message.Type;
import nl.strohalm.cyclos.entities.settings.LocalSettings;
import nl.strohalm.cyclos.services.access.AccessService;
import nl.strohalm.cyclos.services.access.ChannelService;
import nl.strohalm.cyclos.services.accounts.AccountTypeService;
import nl.strohalm.cyclos.services.accounts.MemberAccountTypeQuery;
import nl.strohalm.cyclos.services.accounts.cards.CardTypeService;
import nl.strohalm.cyclos.services.customization.CustomizedFileService;
import nl.strohalm.cyclos.services.elements.RegistrationAgreementService;
import nl.strohalm.cyclos.services.groups.GroupService;
import nl.strohalm.cyclos.services.permissions.PermissionService;
import nl.strohalm.cyclos.services.settings.SettingsService;
import nl.strohalm.cyclos.services.transactions.TransactionContext;
import nl.strohalm.cyclos.services.transfertypes.TransferTypeService;
import nl.strohalm.cyclos.utils.CustomizationHelper;
import nl.strohalm.cyclos.utils.RelationshipHelper;
import nl.strohalm.cyclos.utils.RequestHelper;
import nl.strohalm.cyclos.utils.TimePeriod;
import nl.strohalm.cyclos.utils.TimePeriod.Field;
import nl.strohalm.cyclos.utils.binding.BeanBinder;
import nl.strohalm.cyclos.utils.binding.DataBinder;
import nl.strohalm.cyclos.utils.binding.DataBinderHelper;
import nl.strohalm.cyclos.utils.binding.MapBean;
import nl.strohalm.cyclos.utils.binding.MapBinder;
import nl.strohalm.cyclos.utils.binding.PropertyBinder;
import nl.strohalm.cyclos.utils.binding.SimpleCollectionBinder;
import nl.strohalm.cyclos.utils.conversion.IdConverter;
import nl.strohalm.cyclos.utils.query.PageHelper;
import nl.strohalm.cyclos.utils.validation.ValidationException;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;
import nl.strohalm.cyclos.webservices.utils.RestUserHelper;

@Controller
public class EditGroupController extends BaseRestController {
	
	private static Group.Nature getGroupNature(final EditGroupRequestDTO form, final boolean acceptEmpty) {
        try {
           // final EditGroupForm form = context.getForm();
            final String nature = (String) form.getGroup("nature");
            if (acceptEmpty && StringUtils.isBlank(nature)) {
                return null;
            } else {
                return Group.Nature.valueOf(nature);
            }
        } catch (final Exception e) {
            throw new ValidationException();
        }
    }

    private CustomizationHelper                            customizationHelper;
    private AccountTypeService                             accountTypeService;
    private RegistrationAgreementService                   registrationAgreementService;
    private CustomizedFileService                          customizedFileService;
    private TransferTypeService                            transferTypeService;
    private CardTypeService                                cardTypeService;
    private ChannelService                                 channelService;
    private Map<Group.Nature, DataBinder<? extends Group>> dataBinders;
    private GroupService groupService;
    private PermissionService permissionService;
    private AccessService accessService;
    
    private RestUserHelper restUserHelper;
    
    public final GroupService getGroupService() {
		return groupService;
	}

	public final void setGroupService(GroupService groupService) {
		this.groupService = groupService;
	}

	public final PermissionService getPermissionService() {
		return permissionService;
	}

	public final void setPermissionService(PermissionService permissionService) {
		this.permissionService = permissionService;
	}

	private SettingsService settingsService;
    

    public AccountTypeService getAccountTypeService() {
        return accountTypeService;
    }

    public CustomizedFileService getCustomizedFileService() {
        return customizedFileService;
    }

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

    @Inject
    public void setAccountTypeService(final AccountTypeService accountTypeService) {
        this.accountTypeService = accountTypeService;
    }

    @Inject
    public void setCardTypeService(final CardTypeService cardTypeService) {
        this.cardTypeService = cardTypeService;
    }

    @Inject
    public void setChannelService(final ChannelService channelService) {
        this.channelService = channelService;
    }

    @Inject
    public void setCustomizationHelper(final CustomizationHelper customizationHelper) {
        this.customizationHelper = customizationHelper;
    }

    @Inject
    public void setCustomizedFileService(final CustomizedFileService customizedFileService) {
        this.customizedFileService = customizedFileService;
    }

    @Inject
    public void setRegistrationAgreementService(final RegistrationAgreementService registrationAgreementService) {
        this.registrationAgreementService = registrationAgreementService;
    }

    @Inject
    public void setTransferTypeService(final TransferTypeService transferTypeService) {
        this.transferTypeService = transferTypeService;
    }

    public static class EditGroupRequestDTO{
    	private long              groupId;
        private long              baseGroupId;
        private boolean           forceAccept;
        private boolean           useCustomSMSContextClass;
        private Map<String,Object> values;
        

        public final Map<String, Object> getValues() {
			return values;
		}

		public void EditGroupForm() {
            // Since we don't know now what type of group will it be, prepare each group settings

            final MapBean basicSettings = new MapBean("maxPasswordWrongTries", "passwordPolicy", "transactionPassword", "transactionPasswordLength", "maxTransactionPasswordWrongTries");
            basicSettings.set("passwordLength", new MapBean("min", "max"));
            basicSettings.set("deactivationAfterMaxPasswordTries", new MapBean("field", "number"));
            basicSettings.set("passwordExpiresAfter", new MapBean("field", "number"));
            setGroup("basicSettings", basicSettings);

            final MapBean memberSettings = new MapBean("maxAdsPerMember", "maxAdImagesPerMember", "maxImagesPerMember", "sendPasswordByEmail", "viewLoansByGroup", "repayLoanByGroup", "groupAfterExpiration");
            memberSettings.set("emailValidation", Collections.emptySet());
            memberSettings.setType("emailValidation", Set.class);
            memberSettings.set("pinLength", new MapBean("min", "max"));
            memberSettings.set("defaultAdPublicationTime", new MapBean("field", "number"));
            memberSettings.set("maxAdPublicationTime", new MapBean("field", "number"));
            memberSettings.set("maxSchedulingPeriod", new MapBean("field", "number"));
            memberSettings.set("expireMembersAfter", new MapBean("field", "number"));
            memberSettings.set("pinBlockTimeAfterMaxTries", new MapBean("field", "number"));
            memberSettings.set("smsAdditionalChargedPeriod", new MapBean("field", "number"));
            setGroup("memberSettings", memberSettings);

            final MapBean brokerSettings = new MapBean("initialGroupForRegisteredMembers");
            setGroup("brokerSettings", brokerSettings);

            setGroup("transferTypeIds", Collections.emptyList());
            setGroup("maxAmountPerDayByTT", Collections.emptyList());
            setGroup("defaultMailMessages", Collections.emptyList());
            setGroup("smsMessages", Collections.emptyList());
            setGroup("defaultSmsMessages", Collections.emptyList());
            setGroup("channels", Collections.emptyList());
            setGroup("defaultChannels", Collections.emptyList());
            setGroup("possibleInitialGroups", Collections.emptyList());
        }

        public long getBaseGroupId() {
            return baseGroupId;
        }

        public Map<String, Object> getGroup() {
            return values;
        }

        public Object getGroup(final String key) {
            return values.get(key);
        }

        public long getGroupId() {
            return groupId;
        }

        public boolean isForceAccept() {
            return forceAccept;
        }

        public boolean isUseCustomSMSContextClass() {
            return useCustomSMSContextClass;
        }

        public void setBaseGroupId(final long baseGroupId) {
            this.baseGroupId = baseGroupId;
        }

        public void setForceAccept(final boolean forceAccept) {
            this.forceAccept = forceAccept;
        }

        public void setGroup(final Map<String, Object> group) {
            values = group;
        }

        public void setGroup(final String key, final Object value) {
            values.put(key, value);
        }

        public void setGroupId(final long groupId) {
            this.groupId = groupId;
        }

        public void setUseCustomSMSContextClass(final boolean useCustomSMSContextClass) {
            this.useCustomSMSContextClass = useCustomSMSContextClass;
        }
    }
    
    public static class EditGroupResponseDTO{
    	private boolean isMemberGroup;
    	public final boolean isMemberGroup() {
			return isMemberGroup;
		}

		public final void setMemberGroup(boolean isMemberGroup) {
			this.isMemberGroup = isMemberGroup;
		}

		public final boolean isBrokerGroup() {
			return isBrokerGroup;
		}

		public final void setBrokerGroup(boolean isBrokerGroup) {
			this.isBrokerGroup = isBrokerGroup;
		}

		public final boolean isOperatorGroup() {
			return isOperatorGroup;
		}

		public final void setOperatorGroup(boolean isOperatorGroup) {
			this.isOperatorGroup = isOperatorGroup;
		}

		public final boolean isUsesPin() {
			return usesPin;
		}

		public final void setUsesPin(boolean usesPin) {
			this.usesPin = usesPin;
		}

	     

		public final String getGroupId() {
			return groupId;
		}

		public final void setGroupId(String groupId) {
			this.groupId = groupId;
		}



		private boolean isBrokerGroup;
    	private boolean isOperatorGroup;
    	private boolean usesPin;
    	public String message;
    	public String groupId;

		public EditGroupResponseDTO(String message,
				String groupId) {
			super();
			this.message = message;
			this.groupId = groupId;
		}

		public String getMessage() {
			return message;
		}

		public void setMessage(String message) {
			this.message = message;
		}
    	
    	
    }
    @RequestMapping(value = "/admin/editGroup", method = RequestMethod.POST)
    @ResponseBody
    protected EditGroupResponseDTO handleSubmit(@RequestBody EditGroupRequestDTO form ) throws Exception {
    	 
    	//Fetch User based on user token who is calling this API
    	User user=null;
    	
    	restUserHelper =new RestUserHelper(user,groupService,accessService);
    	
        Group group = readGroup(form);
        final Group baseGroup = readBaseGroup(form);
        final boolean isInsert = group.getId() == null;

        // Persist the group
        if (isInsert) {
            group = groupService.insert(group, baseGroup);
        } else {
            group = groupService.update(group, form.isForceAccept());
        }

        // Ensure the customized files collection is reloaded (for example, a group could be copied from other, and we need the collection to be
        // up-to-date)
        group = groupService.reload(group.getId(), Group.Relationships.CUSTOMIZED_FILES);

        // Physically update the files
        for (final CustomizedFile file : group.getCustomizedFiles()) {
            final File physicalFile = customizationHelper.customizedFileOf(file);
            customizationHelper.updateFile(physicalFile, file);
        }

       // context.sendMessage(isInsert ? "group.inserted" : "group.modified");
        
        EditGroupResponseDTO response=new EditGroupResponseDTO(isInsert ? "group.inserted" : "group.modified",String.valueOf(group.getId()));
        return response;
        
        //return ActionHelper.redirectWithParam(context.getRequest(), context.getSuccessForward(), "groupId", group.getId());
    }

  //  @Override
    @SuppressWarnings({ "unchecked", "rawtypes" })
    protected void prepareForm(final ActionContext context) throws Exception {
        final HttpServletRequest request = context.getRequest();
        final EditGroupForm form = context.getForm();
        final long id = form.getGroupId();
        boolean editable = false;
        boolean canManageFiles = false;

        final Map<Group.Nature, Permission> permissionByNature = ListGroupsAction.getManageGroupPermissionByNatureMap();

        final boolean isInsert = id <= 0L;
        if (isInsert) {
            // Prepare for insert
            List<Group.Nature> natures = new ArrayList<Group.Nature>();
            if (context.isAdmin()) {
                // Put in the request the name of permission used to manage a type of group
                request.setAttribute("permissionByNature", permissionByNature);

                for (final Group.Nature nature : permissionByNature.keySet()) {
                    final Permission permission = permissionByNature.get(nature);
                    if (permissionService.hasPermission(permission)) {
                        natures.add(nature);
                    }
                }
            } else {
                // It's a member inserting an operator group
                final GroupQuery groupQuery = new GroupQuery();
                groupQuery.setMember((Member) context.getElement());
                final List<OperatorGroup> baseGroups = (List<OperatorGroup>) groupService.search(groupQuery);
                request.setAttribute("baseGroups", baseGroups);
                request.setAttribute("isOperatorGroup", true);
                natures = Collections.singletonList(Group.Nature.OPERATOR);
            }
            request.setAttribute("natures", natures);
            RequestHelper.storeEnum(request, Group.Status.class, "status");
            editable = true;
        } else {
            // Prepare for modify
            Group group = groupService.load(id, Group.Relationships.CUSTOMIZED_FILES, MemberGroup.Relationships.CHANNELS);
            final boolean isMemberGroup = MemberGroup.class.isAssignableFrom(group.getNature().getGroupClass());
            final boolean isBrokerGroup = BrokerGroup.class.isAssignableFrom(group.getNature().getGroupClass());
            final boolean isOperatorGroup = OperatorGroup.class.isAssignableFrom(group.getNature().getGroupClass());
            if (group.getStatus().isEnabled()) {
                request.setAttribute("deactivationTimePeriodFields", Arrays.asList(Field.SECONDS, Field.MINUTES, Field.HOURS, Field.DAYS));
                request.setAttribute("passwordExpiresAfterFields", Arrays.asList(Field.DAYS, Field.WEEKS, Field.MONTHS, Field.YEARS));
                RequestHelper.storeEnum(request, TransactionPassword.class, "transactionPasswords");
                if (isMemberGroup) {
                    final MemberGroup memberGroup = (MemberGroup) group;

                    // Check if the group has access to a channel that uses pin
                    final boolean usesPin = groupService.usesPin(memberGroup);
                    request.setAttribute("usesPin", usesPin);

                    // Retrieve the registration agreements
                    final List<RegistrationAgreement> registrationAgreements = registrationAgreementService.listAll();
                    request.setAttribute("registrationAgreements", registrationAgreements);

                    // Retrieve the associated accounts
                    request.setAttribute("timePeriodFields", Arrays.asList(Field.DAYS, Field.WEEKS, Field.MONTHS, Field.YEARS));
                    final MemberAccountTypeQuery atQuery = new MemberAccountTypeQuery();
                    atQuery.setRelatedToGroup(memberGroup);
                    request.setAttribute("accountTypes", accountTypeService.search(atQuery));

                    // Sort the message types using their messages
                    final List<Type> messageTypes = Arrays.asList(Message.Type.values());
                    final Comparator cmp = new Comparator() {
                        @Override
                        public int compare(final Object o1, final Object o2) {
                            final String msg1 = context.message("message.type." + o1);
                            final String msg2 = context.message("message.type." + o2);
                            return msg1.compareTo(msg2);
                        }
                    };
                    Collections.sort(messageTypes, cmp);
                    request.setAttribute("messageTypes", messageTypes);

                    // we create a wrapper ArrayList because the list must implement the remove method
                    final List<Type> smsMessageTypes = new ArrayList<Type>(Arrays.asList(Message.Type.values()));
                    CollectionUtils.filter(smsMessageTypes, new Predicate() {
                        @Override
                        public boolean evaluate(final Object object) {
                            final Message.Type type = (Message.Type) object;
                            switch (type) {
                                case FROM_MEMBER:
                                case FROM_ADMIN_TO_MEMBER:
                                case FROM_ADMIN_TO_GROUP:
                                    return false;
                                default:
                                    return true;
                            }
                        }
                    });
                    Collections.sort(smsMessageTypes, cmp);
                    request.setAttribute("smsMessageTypes", smsMessageTypes);

                    // Store the possible groups for expiration
                    final GroupQuery query = new GroupQuery();
                    query.setNatures(Group.Nature.MEMBER, Group.Nature.BROKER);
                    final List<? extends Group> groups = groupService.search(query);
                    groups.remove(group);
                    request.setAttribute("possibleExpirationGroups", groups);
                    request.setAttribute("expirationTimeFields", Arrays.asList(TimePeriod.Field.DAYS, TimePeriod.Field.WEEKS, TimePeriod.Field.MONTHS, TimePeriod.Field.YEARS));

                    // Store transfer types for SMS charge
                    final TransferTypeQuery ttQuery = new TransferTypeQuery();
                    ttQuery.setFromGroups(Collections.singletonList(memberGroup));
                    ttQuery.setToNature(AccountType.Nature.SYSTEM);
                    final List<TransferType> smsChargeTransferTypes = transferTypeService.search(ttQuery);
                    request.setAttribute("smsChargeTransferTypes", smsChargeTransferTypes);

                    request.setAttribute("smsAdditionalChargedPeriodFields", Arrays.asList(Field.DAYS, Field.WEEKS, Field.MONTHS));

                    // Retrieve the card types
                    request.setAttribute("cardTypes", cardTypeService.listAll());
                }
                if (isBrokerGroup) {
                    // Retrieve the possible groups for registered members by broker
                    final GroupQuery query = new GroupQuery();
                    query.setNatures(Group.Nature.MEMBER, Group.Nature.BROKER);
                    query.setStatus(Group.Status.NORMAL);
                    request.setAttribute("memberGroups", groupService.search(query));
                }
                if (isMemberGroup || isBrokerGroup) {
                    RequestHelper.storeEnum(request, MemberGroupSettings.ExternalAdPublication.class, "externalAdPublications");
                }
                if (isOperatorGroup) {
                    // Load the associated transaction types for the max amount per day
                    group = groupService.load(group.getId(), RelationshipHelper.nested(OperatorGroup.Relationships.MEMBER, Element.Relationships.GROUP), OperatorGroup.Relationships.MAX_AMOUNT_PER_DAY_BY_TRANSFER_TYPE, OperatorGroup.Relationships.MAX_AMOUNT_PER_DAY_BY_TRANSFER_TYPE);
                    final OperatorGroup operatorGroup = (OperatorGroup) group;
                    request.setAttribute("transferTypes", operatorGroup.getMember().getGroup().getTransferTypes());
                }

                // Retrieve the associated customized files
                final CustomizedFileQuery cfQuery = new CustomizedFileQuery();
                cfQuery.setGroup(group);
                request.setAttribute("customizedFiles", customizedFileService.search(cfQuery));

                // Check whether the login page name will be shown
                request.setAttribute("showLoginPageName", customizationHelper.isAnyFileRelatedToLoginPage(group.getCustomizedFiles()));
            }
            getDataBinder(group.getNature()).writeAsString(form.getGroup(), group);
            request.setAttribute("group", group);
            request.setAttribute("isMemberGroup", isMemberGroup);
            request.setAttribute("isBrokerGroup", isBrokerGroup);
            request.setAttribute("isOperatorGroup", isOperatorGroup);

            if (isMemberGroup) {
                // Show scheduling options when there's a schedulable transfer type
                final TransferTypeQuery ttQuery = new TransferTypeQuery();
                ttQuery.setPageForCount();
                ttQuery.setContext(TransactionContext.PAYMENT);
                ttQuery.setGroup(group);
                ttQuery.setSchedulable(true);
                request.setAttribute("showScheduling", PageHelper.getTotalCount(transferTypeService.search(ttQuery)) > 0);

                // Channels that the group of member have access
                final Collection<Channel> channels = channelService.list();
                // The "web" channel can not be customized by the user, so it should not be sent to the JSP page
                final Channel webChannel = channelService.loadByInternalName(Channel.WEB);
                channels.remove(webChannel);
                request.setAttribute("channels", channels);

                RequestHelper.storeEnum(request, EmailValidation.class, "emailValidations");
            }

            if (context.isAdmin()) {
                AdminGroup adminGroup = context.getGroup();
                adminGroup = groupService.load(adminGroup.getId(), AdminGroup.Relationships.MANAGES_GROUPS);
                if (permissionService.hasPermission(permissionByNature.get(group.getNature())) && (Group.Nature.ADMIN.equals(group.getNature()) || adminGroup.getManagesGroups().contains(group))) {
                    editable = true;
                }
            } else {
                // It's a member updating an operator group
                editable = permissionService.hasPermission(MemberPermission.OPERATORS_MANAGE);
            }
            canManageFiles = customizedFileService.canViewOrManageInGroup(group);
        }

        request.setAttribute("isInsert", isInsert);
        request.setAttribute("editable", editable);
        request.setAttribute("canManageFiles", canManageFiles);
        RequestHelper.storeEnum(request, PasswordPolicy.class, "passwordPolicies");
    }

    //@Override
    protected void validateForm(EditGroupRequestDTO form ) {
        final Group group = readGroup(form);
        // Check if it's a insert operation
        groupService.validate(group);
        if (group instanceof MemberGroup) {
           // final EditGroupForm form = context.getForm();
            final MemberGroup memberGroup = (MemberGroup) group;
            // Ensure the smsContext is present when the checkbox for using a custom context is checked
            if (form.isUseCustomSMSContextClass() && StringUtils.isEmpty(memberGroup.getMemberSettings().getSmsContextClassName())) {
               // throw new ValidationException("errors.required", context.message("group.settings.smsContextClassName"));
            	 throw new ValidationException("errors.required","group.settings.smsContextClassName");
            }
        }
    }

    private void initBasic(final BeanBinder<? extends Group> groupBinder, final BeanBinder<? extends BasicGroupSettings> basicSettingsBinder) {
        groupBinder.registerBinder("basicSettings", basicSettingsBinder);

        groupBinder.registerBinder("id", PropertyBinder.instance(Long.class, "id", IdConverter.instance()));
        groupBinder.registerBinder("name", PropertyBinder.instance(String.class, "name"));
        groupBinder.registerBinder("description", PropertyBinder.instance(String.class, "description"));
        groupBinder.registerBinder("status", PropertyBinder.instance(Group.Status.class, "status"));

        basicSettingsBinder.registerBinder("passwordLength", DataBinderHelper.rangeConstraintBinder("passwordLength"));
        basicSettingsBinder.registerBinder("passwordPolicy", PropertyBinder.instance(PasswordPolicy.class, "passwordPolicy"));
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
        memberSettingsBinder.registerBinder("emailValidation", SimpleCollectionBinder.instance(EmailValidation.class, HashSet.class, "emailValidation"));
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

    private Group readBaseGroup(EditGroupRequestDTO form) {
       // final EditGroupForm form = context.getForm();
        final long baseGroupId = form.getBaseGroupId();
        if (baseGroupId < 1) {
            return null;
        }
        return groupService.load(baseGroupId);
    }

    private Group readGroup(final EditGroupRequestDTO form) {
       // final EditGroupForm form = context.getForm();
        final long id = form.getGroupId();
        Group.Nature nature;
        final boolean isInsert = (id <= 0L);
        if (isInsert) {
            nature = getGroupNature(form, false);
            // On insert, empty status means normal
            final String status = (String) form.getGroup("status");
            if (StringUtils.isEmpty(status)) {
                form.setGroup("status", Group.Status.NORMAL.name());
            }
        } else {
            nature = groupService.load(id).getNature();
        }
        final Group group = getDataBinder(nature).readFromString(form.getGroup());
        if (nature == Nature.OPERATOR) {
            // Ensure to set the logged member on operator groups, as this is not read from request
            final Member member = (Member) restUserHelper.getElement();
            final OperatorGroup operatorGroup = (OperatorGroup) group;
            operatorGroup.setMember(member);
        }
        return group;
    }
}
