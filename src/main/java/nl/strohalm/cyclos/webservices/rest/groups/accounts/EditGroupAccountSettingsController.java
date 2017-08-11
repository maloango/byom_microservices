package nl.strohalm.cyclos.webservices.rest.groups.accounts;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import nl.strohalm.cyclos.annotations.Inject;
import nl.strohalm.cyclos.entities.accounts.MemberAccountType;
import nl.strohalm.cyclos.entities.accounts.MemberGroupAccountSettings;
import nl.strohalm.cyclos.entities.accounts.transactions.TransferType;
import nl.strohalm.cyclos.entities.groups.MemberGroup;
import nl.strohalm.cyclos.entities.settings.LocalSettings;
import nl.strohalm.cyclos.entities.settings.events.LocalSettingsChangeListener;
import nl.strohalm.cyclos.entities.settings.events.LocalSettingsEvent;
import nl.strohalm.cyclos.services.accounts.AccountTypeService;
import nl.strohalm.cyclos.services.groups.GroupService;
import nl.strohalm.cyclos.services.settings.SettingsService;
import nl.strohalm.cyclos.services.transfertypes.TransferTypeService;
import nl.strohalm.cyclos.utils.binding.BeanBinder;
import nl.strohalm.cyclos.utils.binding.DataBinder;
import nl.strohalm.cyclos.utils.binding.PropertyBinder;
import nl.strohalm.cyclos.utils.conversion.IdConverter;
import nl.strohalm.cyclos.utils.conversion.ReferenceConverter;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;

@Controller
public class EditGroupAccountSettingsController extends BaseRestController
		implements LocalSettingsChangeListener {
	private AccountTypeService accountTypeService;
	private TransferTypeService transferTypeService;
	private DataBinder<MemberGroupAccountSettings> dataBinder;
	private SettingsService settingsService;
	private GroupService groupService;
	

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

	public AccountTypeService getAccountTypeService() {
		return accountTypeService;
	}

	public DataBinder<MemberGroupAccountSettings> getDataBinder() {
		if (dataBinder == null) {

			final LocalSettings localSettings = settingsService
					.getLocalSettings();

			final BeanBinder<MemberGroupAccountSettings> binder = BeanBinder
					.instance(MemberGroupAccountSettings.class);
			binder.registerBinder(
					"id",
					PropertyBinder.instance(Long.class, "id",
							IdConverter.instance()));
			binder.registerBinder("group", PropertyBinder.instance(
					MemberGroup.class, "group",
					ReferenceConverter.instance(MemberGroup.class)));
			binder.registerBinder("accountType", PropertyBinder.instance(
					MemberAccountType.class, "accountType",
					ReferenceConverter.instance(MemberAccountType.class)));
			binder.registerBinder("default",
					PropertyBinder.instance(Boolean.TYPE, "default"));
			binder.registerBinder("transactionPasswordRequired", PropertyBinder
					.instance(Boolean.TYPE, "transactionPasswordRequired"));
			binder.registerBinder("defaultCreditLimit", PropertyBinder
					.instance(BigDecimal.class, "defaultCreditLimit",
							localSettings.getNumberConverter()
									.negativeToAbsolute()));
			binder.registerBinder("defaultUpperCreditLimit", PropertyBinder
					.instance(BigDecimal.class, "defaultUpperCreditLimit",
							localSettings.getNumberConverter()));
			binder.registerBinder("initialCredit", PropertyBinder.instance(
					BigDecimal.class, "initialCredit",
					localSettings.getNumberConverter()));
			binder.registerBinder("initialCreditTransferType", PropertyBinder
					.instance(TransferType.class, "initialCreditTransferType",
							ReferenceConverter.instance(TransferType.class)));
			binder.registerBinder("lowUnits", PropertyBinder.instance(
					BigDecimal.class, "lowUnits",
					localSettings.getNumberConverter()));
			binder.registerBinder("lowUnitsMessage",
					PropertyBinder.instance(String.class, "lowUnitsMessage"));
			binder.registerBinder("hideWhenNoCreditLimit", PropertyBinder
					.instance(Boolean.TYPE, "hideWhenNoCreditLimit"));

			dataBinder = binder;
		}
		return dataBinder;
	}

	public TransferTypeService getTransferTypeService() {
		return transferTypeService;
	}

	@Override
	public void onLocalSettingsUpdate(final LocalSettingsEvent event) {
		dataBinder = null;
	}

	@Inject
	public void setAccountTypeService(
			final AccountTypeService accountTypeService) {
		this.accountTypeService = accountTypeService;
	}

	@Inject
	public void setTransferTypeService(
			final TransferTypeService transferTypeService) {
		this.transferTypeService = transferTypeService;
	}

	public static class EditGroupAccountSettingsRequestDto {
		private long groupId;
		private long accountTypeId;
		private boolean updateAccountLimits;
		protected Map<String, Object> values;

		public Map<String, Object> getValues() {
			return values;
		}

		public void setValues(final Map<String, Object> values) {
			this.values = values;
		}

		public long getAccountTypeId() {
			return accountTypeId;
		}

		public long getGroupId() {
			return groupId;
		}

		public Map<String, Object> getSetting() {
			return values;
		}

		public Object getSetting(final String key) {
			return values.get(key);
		}

		public boolean isUpdateAccountLimits() {
			return updateAccountLimits;
		}

		public void setAccountTypeId(final long accountTypeId) {
			this.accountTypeId = accountTypeId;
		}

		public void setGroupId(final long groupId) {
			this.groupId = groupId;
		}

		public void setSetting(final Map<String, Object> map) {
			values = map;
		}

		public void setSetting(final String key, final Object value) {
			values.put(key, value);
		}

		public void setUpdateAccountLimits(final boolean updateAccountLimits) {
			this.updateAccountLimits = updateAccountLimits;
		}

	}

	public static class EditGroupAccountSettingsResponseDto {
		public String message;
		Map<String, Object> params;

		public EditGroupAccountSettingsResponseDto(String message,
				Map<String, Object> params) {
			super();
			this.message = message;
			this.params = params;
		}

		public String getMessage() {
			return message;
		}

		public void setMessage(String message) {
			this.message = message;
		}
                public EditGroupAccountSettingsResponseDto(){
                }
	}

	@RequestMapping(value = "admin/editGroupAccountSettings", method = RequestMethod.POST)
	@ResponseBody
	protected EditGroupAccountSettingsResponseDto handleSubmit(
			@RequestBody EditGroupAccountSettingsRequestDto form)
			throws Exception {
		EditGroupAccountSettingsResponseDto response = null;
                try{
		MemberGroupAccountSettings groupAccountSettings = getDataBinder()
				.readFromString(form.getSetting());
		final boolean isInsert = groupAccountSettings.getId() == null;
		String message = null;
		if (isInsert) {
			groupAccountSettings = groupService
					.insertAccountSettings(groupAccountSettings);
			message = "group.account.inserted";
		} else {
			groupAccountSettings = groupService.updateAccountSettings(
					groupAccountSettings, form.isUpdateAccountLimits());
			message = "group.account.modified";
		}
		final Map<String, Object> params = new HashMap<String, Object>();
		params.put("groupId", groupAccountSettings.getGroup().getId());
		params.put("accountTypeId", groupAccountSettings.getAccountType()
				.getId());
		response = new EditGroupAccountSettingsResponseDto(
				message, params);}
                catch(Exception e){
                    e.printStackTrace();
                }

		return response;
	}

}
