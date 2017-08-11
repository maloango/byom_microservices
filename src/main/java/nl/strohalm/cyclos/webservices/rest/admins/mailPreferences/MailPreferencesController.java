package nl.strohalm.cyclos.webservices.rest.admins.mailPreferences;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import nl.strohalm.cyclos.access.AdminSystemPermission;
import nl.strohalm.cyclos.annotations.Inject;
import nl.strohalm.cyclos.controls.ActionContext;
import nl.strohalm.cyclos.controls.admins.mailPreferences.MailPreferencesForm;
import nl.strohalm.cyclos.entities.accounts.guarantees.GuaranteeType;
import nl.strohalm.cyclos.entities.accounts.guarantees.GuaranteeTypeQuery;
import nl.strohalm.cyclos.entities.accounts.transactions.TransferType;
import nl.strohalm.cyclos.entities.alerts.MemberAlert;
import nl.strohalm.cyclos.entities.alerts.SystemAlert;
import nl.strohalm.cyclos.entities.exceptions.EntityNotFoundException;
import nl.strohalm.cyclos.entities.groups.AdminGroup;
import nl.strohalm.cyclos.entities.groups.MemberGroup;
import nl.strohalm.cyclos.entities.groups.SystemGroup;
import nl.strohalm.cyclos.entities.members.Administrator;
import nl.strohalm.cyclos.entities.members.messages.MessageCategory;
import nl.strohalm.cyclos.entities.members.preferences.AdminNotificationPreference;
import nl.strohalm.cyclos.services.accounts.guarantees.GuaranteeTypeService;
import nl.strohalm.cyclos.services.groups.GroupService;
import nl.strohalm.cyclos.services.permissions.PermissionService;
import nl.strohalm.cyclos.services.preferences.PreferenceService;
import nl.strohalm.cyclos.services.transfertypes.TransferTypeService;
import nl.strohalm.cyclos.utils.RequestHelper;
import nl.strohalm.cyclos.utils.binding.BeanBinder;
import nl.strohalm.cyclos.utils.binding.DataBinder;
import nl.strohalm.cyclos.utils.binding.PropertyBinder;
import nl.strohalm.cyclos.utils.binding.SimpleCollectionBinder;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class MailPreferencesController extends BaseRestController {
	private GuaranteeTypeService guaranteeTypeService;
	private PreferenceService preferenceService;
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

	public final GuaranteeTypeService getGuaranteeTypeService() {
		return guaranteeTypeService;
	}

	public final PreferenceService getPreferenceService() {
		return preferenceService;
	}

	public final TransferTypeService getTransferTypeService() {
		return transferTypeService;
	}

	private TransferTypeService transferTypeService;
	private DataBinder<AdminNotificationPreference> dataBinder;
	private GroupService groupService;
	private PermissionService permissionService;

	@Inject
	public void setGuaranteeTypeService(
			final GuaranteeTypeService guaranteeTypeService) {
		this.guaranteeTypeService = guaranteeTypeService;
	}

	@Inject
	public void setPreferenceService(final PreferenceService preferenceService) {
		this.preferenceService = preferenceService;
	}

	@Inject
	public void setTransferTypeService(
			final TransferTypeService transferTypeService) {
		this.transferTypeService = transferTypeService;
	}

	public static final class MailPreferencesRequestDto {
    private boolean                 applicationErrors;
    private boolean                 systemInvoices;
    private Administrator           admin;
    private Set<TransferType>       transferTypes;
    private Set<TransferType>       newPendingPayments;
    private Set<GuaranteeType>      guaranteeTypes;
    private Set<MessageCategory>    messageCategories;
    private Set<SystemAlert.Alerts> systemAlerts;
    private Set<MemberAlert.Alerts> memberAlerts;
    private Set<MemberGroup>        newMembers;

    public Administrator getAdmin() {
        return admin;
    }

    public Set<GuaranteeType> getGuaranteeTypes() {
        return guaranteeTypes;
    }

    public Set<MemberAlert.Alerts> getMemberAlerts() {
        return memberAlerts;
    }

    public Set<MessageCategory> getMessageCategories() {
        return messageCategories;
    }

    public Set<MemberGroup> getNewMembers() {
        return newMembers;
    }

    public Set<TransferType> getNewPendingPayments() {
        return newPendingPayments;
    }

    public Set<SystemAlert.Alerts> getSystemAlerts() {
        return systemAlerts;
    }

    public Set<TransferType> getTransferTypes() {
        return transferTypes;
    }

    public boolean isApplicationErrors() {
        return applicationErrors;
    }

    public boolean isSystemInvoices() {
        return systemInvoices;
    }

    public void setAdmin(final Administrator admin) {
        this.admin = admin;
    }

    public void setApplicationErrors(final boolean applicationErrors) {
        this.applicationErrors = applicationErrors;
    }

    public void setGuaranteeTypes(final Set<GuaranteeType> guaranteeTypes) {
        this.guaranteeTypes = guaranteeTypes;
    }

    public void setMemberAlerts(final Set<MemberAlert.Alerts> memberAlerts) {
        this.memberAlerts = memberAlerts;
    }

    public void setMessageCategories(final Set<MessageCategory> messageCategories) {
        this.messageCategories = messageCategories;
    }

    public void setNewMembers(final Set<MemberGroup> newMembers) {
        this.newMembers = newMembers;
    }

    public void setNewPendingPayments(final Set<TransferType> newPendingPayments) {
        this.newPendingPayments = newPendingPayments;
    }

    public void setSystemAlerts(final Set<SystemAlert.Alerts> systemAlerts) {
        this.systemAlerts = systemAlerts;
    }

    public void setSystemInvoices(final boolean systemInvoices) {
        this.systemInvoices = systemInvoices;
    }

    public void setTransferTypes(final Set<TransferType> transferTypes) {
        this.transferTypes = transferTypes;
    }

  
        public MailPreferencesRequestDto() {
        setAdminNotificationPreference("transferTypes", Collections.emptyList());
        setAdminNotificationPreference("newPendingPayments", Collections.emptyList());
        setAdminNotificationPreference("guaranteeTypes", Collections.emptyList());
        setAdminNotificationPreference("messageCategories", Collections.emptyList());
        setAdminNotificationPreference("newMembers", Collections.emptyList());
        setAdminNotificationPreference("memberAlerts", Collections.emptyList());
        setAdminNotificationPreference("systemAlerts", Collections.emptyList());
        
    }

    public Map<String, Object> getAdminNotificationPreference() {
        return values;
    }

    public Object getAdminNotificationPreference(final String key) {
        return values.get(key);
    }

    public void setAdminNotificationPreference(final Map<String, Object> map) {
        values = map;
    }

    public void setAdminNotificationPreference(final String key, final Object value) {
        values.put(key, value);
    }
		protected Map<String, Object> values;

		public Map<String, Object> getValues() {
			return values;
		}

		public void setValues(final Map<String, Object> values) {
			this.values = values;
		}

		
	}

	public static class MailPreferencesResponseDto {
		public String message;

		public String getMessage() {
			return message;
		}

		public void setMessage(String message) {
			this.message = message;
		}
                public MailPreferencesResponseDto(){
                }
               
	}

	@RequestMapping(value = "admin/mailPreferences/{systemInvoices}", method = RequestMethod.GET)
	@ResponseBody
	protected MailPreferencesResponseDto formAction(@PathVariable ("systemInvoices") boolean systemInvoices, PreferenceService notificationPreference) throws Exception {
		MailPreferencesResponseDto response =null;
                try{
		//AdminNotificationPreference notificationPreference = getDataBinder()
			//	.readFromString(form.getAdminNotificationPreference());
		notificationPreference = (PreferenceService) preferenceService.save((AdminNotificationPreference) notificationPreference);
		response = new MailPreferencesResponseDto();
		response.setMessage("mailPreferences.saved");}
                catch(Exception e){
                    e.printStackTrace();
                }
            return response;
	}

	protected void prepareForm(final ActionContext context) throws Exception {
		final HttpServletRequest request = context.getRequest();
		final MailPreferencesForm form = context.getForm();

		final Administrator admin = context.getElement();
		AdminGroup group = admin.getAdminGroup();
		group = groupService.load(group.getId(),
				AdminGroup.Relationships.VIEW_INFORMATION_OF,
				SystemGroup.Relationships.MESSAGE_CATEGORIES);

		final List<MemberGroup> memberGroups = new ArrayList<MemberGroup>(
				permissionService.getManagedMemberGroups());
		for (final Iterator<MemberGroup> it = memberGroups.iterator(); it
				.hasNext();) {
			if (it.next().isRemoved()) {
				it.remove();
			}
		}
		Collections.sort(memberGroups);
		final List<TransferType> transferTypes = transferTypeService
				.getPaymentAndSelfPaymentTTs();
		final List<TransferType> newPendingPayments = transferTypeService
				.getAuthorizableTTs();

		List<GuaranteeType> guaranteeTypes = Collections.emptyList();
		if (permissionService
				.hasPermission(AdminSystemPermission.GUARANTEE_TYPES_VIEW)) {
			final GuaranteeTypeQuery guaranteeTypeQuery = new GuaranteeTypeQuery();
			guaranteeTypeQuery.setEnabled(true);
			guaranteeTypes = guaranteeTypeService.search(guaranteeTypeQuery);
		}

		final List<MessageCategory> messageCategories = new ArrayList<MessageCategory>(
				group.getMessageCategories());
		Collections.sort(messageCategories);

		AdminNotificationPreference notificationPreference = null;
		try {
			notificationPreference = preferenceService
					.load(admin,
							AdminNotificationPreference.Relationships.TRANSFER_TYPES,
							AdminNotificationPreference.Relationships.MESSAGE_CATEGORIES,
							AdminNotificationPreference.Relationships.MEMBER_ALERTS,
							AdminNotificationPreference.Relationships.SYSTEM_ALERTS);
			form.setAdminNotificationPreference("applicationErrors",
					notificationPreference.isApplicationErrors());
			form.setAdminNotificationPreference("systemInvoices",
					notificationPreference.isSystemInvoices());
			request.setAttribute("selectedTransferTypes",
					notificationPreference.getTransferTypes());
			request.setAttribute("selectedNewPendingPayments",
					notificationPreference.getNewPendingPayments());
			request.setAttribute("selectedGuaranteeTypes",
					notificationPreference.getGuaranteeTypes());
			request.setAttribute("selectedMessageCategories",
					notificationPreference.getMessageCategories());
			request.setAttribute("selectedNewMembers",
					notificationPreference.getNewMembers());
			request.setAttribute("selectedSystemAlerts",
					notificationPreference.getSystemAlerts());
			request.setAttribute("selectedMemberAlerts",
					notificationPreference.getMemberAlerts());
		} catch (final EntityNotFoundException e) {
			// Ignore - no current preference
		}

		RequestHelper.storeEnum(request, MemberAlert.Alerts.class,
				"memberAlerts");
		RequestHelper.storeEnum(request, SystemAlert.Alerts.class,
				"systemAlerts");

		request.setAttribute("transferTypes", transferTypes);
		request.setAttribute("newPendingPayments", newPendingPayments);
		request.setAttribute("guaranteeTypes", guaranteeTypes);
		request.setAttribute("messageCategories", messageCategories);
		request.setAttribute("memberGroups", memberGroups);
		request.setAttribute("notificationPreference", notificationPreference);
	}

	private DataBinder<AdminNotificationPreference> getDataBinder() {
		if (dataBinder == null) {
			final BeanBinder<AdminNotificationPreference> binder = BeanBinder
					.instance(AdminNotificationPreference.class);
			binder.registerBinder("transferTypes", SimpleCollectionBinder
					.instance(TransferType.class, Set.class, "transferTypes"));
			binder.registerBinder("newPendingPayments", SimpleCollectionBinder
					.instance(TransferType.class, Set.class,
							"newPendingPayments"));
			binder.registerBinder("messageCategories", SimpleCollectionBinder
					.instance(MessageCategory.class, Set.class,
							"messageCategories"));
			binder.registerBinder("guaranteeTypes", SimpleCollectionBinder
					.instance(GuaranteeType.class, Set.class, "guaranteeTypes"));
			binder.registerBinder("newMembers", SimpleCollectionBinder
					.instance(MemberGroup.class, Set.class, "newMembers"));
			binder.registerBinder("systemAlerts", SimpleCollectionBinder
					.instance(SystemAlert.Alerts.class, Set.class,
							"systemAlerts"));
			binder.registerBinder("memberAlerts", SimpleCollectionBinder
					.instance(MemberAlert.Alerts.class, Set.class,
							"memberAlerts"));
			binder.registerBinder("applicationErrors",
					PropertyBinder.instance(Boolean.TYPE, "applicationErrors"));
			binder.registerBinder("systemInvoices",
					PropertyBinder.instance(Boolean.TYPE, "systemInvoices"));
			dataBinder = binder;
		}
		return dataBinder;
	}
}
