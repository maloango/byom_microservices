package nl.strohalm.cyclos.webservices.rest.authorizationlevels;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import nl.strohalm.cyclos.annotations.Inject;
import nl.strohalm.cyclos.controls.ActionContext;
import nl.strohalm.cyclos.controls.accounts.authorizationlevels.EditAuthorizationLevelForm;
import nl.strohalm.cyclos.entities.accounts.transactions.AuthorizationLevel;
import nl.strohalm.cyclos.entities.accounts.transactions.TransferType;
import nl.strohalm.cyclos.entities.groups.AdminGroup;
import nl.strohalm.cyclos.entities.settings.LocalSettings;
import nl.strohalm.cyclos.entities.settings.events.LocalSettingsChangeListener;
import nl.strohalm.cyclos.entities.settings.events.LocalSettingsEvent;
import nl.strohalm.cyclos.services.settings.SettingsService;
import nl.strohalm.cyclos.services.transfertypes.AuthorizationLevelService;
import nl.strohalm.cyclos.services.transfertypes.TransferTypeService;
import nl.strohalm.cyclos.utils.binding.BeanBinder;
import nl.strohalm.cyclos.utils.binding.DataBinder;
import nl.strohalm.cyclos.utils.binding.PropertyBinder;
import nl.strohalm.cyclos.utils.binding.SimpleCollectionBinder;
import nl.strohalm.cyclos.utils.conversion.IdConverter;
import nl.strohalm.cyclos.utils.conversion.ReferenceConverter;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;

@Controller
public class EditAuthorizationLevelController extends BaseRestController
		implements LocalSettingsChangeListener {

	private SettingsService settingsService;
	private AuthorizationLevelService authorizationLevelService;
	private TransferTypeService transferTypeService;
	private DataBinder<AuthorizationLevel> dataBinder;
	private ReadWriteLock lock = new ReentrantReadWriteLock(true);

	public DataBinder<AuthorizationLevel> getDataBinder() {
		try {
			lock.readLock().lock();
			if (dataBinder == null) {
				final LocalSettings localSettings = settingsService
						.getLocalSettings();
				final BeanBinder<AuthorizationLevel> binder = BeanBinder
						.instance(AuthorizationLevel.class);
				binder.registerBinder(
						"id",
						PropertyBinder.instance(Long.class, "id",
								IdConverter.instance()));
				binder.registerBinder("amount", PropertyBinder.instance(
						BigDecimal.class, "amount",
						localSettings.getNumberConverter()));
				binder.registerBinder("level",
						PropertyBinder.instance(Integer.class, "level"));
				binder.registerBinder("authorizer", PropertyBinder.instance(
						AuthorizationLevel.Authorizer.class, "authorizer"));
				binder.registerBinder("transferType", PropertyBinder.instance(
						TransferType.class, "transferType",
						ReferenceConverter.instance(TransferType.class)));
				binder.registerBinder("adminGroups", SimpleCollectionBinder
						.instance(AdminGroup.class, "adminGroups"));
				dataBinder = binder;
			}
			return dataBinder;
		} finally {
			lock.readLock().unlock();
		}
	}

	@Override
	public void onLocalSettingsUpdate(final LocalSettingsEvent event) {
		try {
			lock.writeLock().lock();
			dataBinder = null;
		} finally {
			lock.writeLock().unlock();
		}
	}

	@Inject
	public void setAuthorizationLevelService(
			final AuthorizationLevelService authorizationLevelService) {
		this.authorizationLevelService = authorizationLevelService;
	}

	@Inject
	public void setTransferTypeService(
			final TransferTypeService transferTypeService) {
		this.transferTypeService = transferTypeService;
	}

	public static class EditAuthorizationLevelRequestDto {
		private long authorizationLevelId;
		protected Map<String, Object> values;

		public Map<String, Object> getValues() {
			return values;
		}

		public void setValues(final Map<String, Object> values) {
			this.values = values;
		}

		public Map<String, Object> getAuthorizationLevel() {
			return values;
		}

		public Object getAuthorizationLevel(final String key) {
			return values.get(key);
		}

		public long getAuthorizationLevelId() {
			return authorizationLevelId;
		}

		public void setAuthorizationLevel(final Map<String, Object> map) {
			values = map;
		}

		public void setAuthorizationLevel(final String key, final Object value) {
			values.put(key, value);
		}

		public void setAuthorizationLevelId(final long authorizationLevelId) {
			this.authorizationLevelId = authorizationLevelId;
		}

	}

	public static class EditAuthorizationLevelResponseDto {
		private String message;
		Map<String, Object> params;

		public EditAuthorizationLevelResponseDto(String message,
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
	}

	@RequestMapping(value = "/admin/editAuthorizationLevel", method = RequestMethod.PUT)
	@ResponseBody
	protected EditAuthorizationLevelResponseDto handleSubmit(
			@RequestBody EditAuthorizationLevelRequestDto form)
			throws Exception {
		// final EditAuthorizationLevelForm form = context.getForm();
		AuthorizationLevel authorizationLevel = getDataBinder().readFromString(
				form.getAuthorizationLevel());
		final boolean isInsert = authorizationLevel.isTransient();
		authorizationLevel = authorizationLevelService.save(authorizationLevel);
		String message = null;
		if (isInsert)
			message = "authorizationLevel.inserted";
		else
			message = "authorizationLevel.modified";
		final Map<String, Object> params = new HashMap<String, Object>();
		TransferType transferType = authorizationLevel.getTransferType();
		transferType = transferTypeService.load(transferType.getId(),
				TransferType.Relationships.FROM);
		final Long accountTypeId = transferType.getFrom().getId();
		params.put("transferTypeId", transferType.getId());
		params.put("accountTypeId", accountTypeId);
		EditAuthorizationLevelResponseDto response = new EditAuthorizationLevelResponseDto(
				message, params);
		return response;
	}

	protected void validateForm(final ActionContext context) {
		final EditAuthorizationLevelForm form = context.getForm();
		final AuthorizationLevel authorizationLevel = getDataBinder()
				.readFromString(form.getAuthorizationLevel());
		authorizationLevelService.validate(authorizationLevel);
	}
}
