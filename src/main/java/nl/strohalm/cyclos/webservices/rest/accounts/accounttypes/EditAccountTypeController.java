package nl.strohalm.cyclos.webservices.rest.accounts.accounttypes;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.servlet.http.HttpServletRequest;

import nl.strohalm.cyclos.access.AdminSystemPermission;
import nl.strohalm.cyclos.annotations.Inject;
import nl.strohalm.cyclos.controls.ActionContext;
import nl.strohalm.cyclos.controls.accounts.accounttypes.EditAccountTypeForm;
import nl.strohalm.cyclos.entities.Relationship;
import nl.strohalm.cyclos.entities.accounts.AccountType;
import nl.strohalm.cyclos.entities.accounts.Currency;
import nl.strohalm.cyclos.entities.accounts.MemberAccountType;
import nl.strohalm.cyclos.entities.accounts.SystemAccountType;
import nl.strohalm.cyclos.entities.accounts.fees.account.AccountFee;
import nl.strohalm.cyclos.entities.accounts.fees.account.AccountFeeQuery;
import nl.strohalm.cyclos.entities.accounts.transactions.PaymentFilterQuery;
import nl.strohalm.cyclos.entities.accounts.transactions.TransferType;
import nl.strohalm.cyclos.entities.accounts.transactions.TransferTypeQuery;
import nl.strohalm.cyclos.entities.settings.LocalSettings;
import nl.strohalm.cyclos.entities.settings.events.LocalSettingsEvent;
import nl.strohalm.cyclos.services.accountfees.AccountFeeService;
import nl.strohalm.cyclos.services.accounts.AccountTypeService;
import nl.strohalm.cyclos.services.accounts.CurrencyService;
import nl.strohalm.cyclos.services.permissions.PermissionService;
import nl.strohalm.cyclos.services.settings.SettingsService;
import nl.strohalm.cyclos.services.transactions.TransactionContext;
import nl.strohalm.cyclos.services.transfertypes.PaymentFilterService;
import nl.strohalm.cyclos.services.transfertypes.TransferTypeService;
import nl.strohalm.cyclos.utils.ActionHelper;
import nl.strohalm.cyclos.utils.RelationshipHelper;
import nl.strohalm.cyclos.utils.RequestHelper;
import nl.strohalm.cyclos.utils.binding.BeanBinder;
import nl.strohalm.cyclos.utils.binding.DataBinder;
import nl.strohalm.cyclos.utils.binding.PropertyBinder;
import nl.strohalm.cyclos.utils.conversion.IdConverter;
import nl.strohalm.cyclos.utils.validation.RequiredError;
import nl.strohalm.cyclos.utils.validation.ValidationException;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;

import org.apache.commons.lang.StringUtils;
import org.apache.struts.action.ActionForward;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class EditAccountTypeController extends BaseRestController {
	private CurrencyService currencyService;
	private AccountTypeService accountTypeService;
	private TransferTypeService transferTypeService;
	private AccountFeeService accountFeeService;
	private PaymentFilterService paymentFilterService;
	private Map<AccountType.Nature, DataBinder<? extends AccountType>> dataBinders;
	private ReadWriteLock lock = new ReentrantReadWriteLock(true);
	private SettingsService settingsService;
	private PermissionService permissionService;

	public AccountFeeService getAccountFeeService() {
		return accountFeeService;
	}

	public AccountTypeService getAccountTypeService() {
		return accountTypeService;
	}

	public DataBinder<? extends AccountType> getDataBinder(final AccountType.Nature nature) {
		try {
			lock.readLock().lock();
			if (dataBinders == null) {
				final HashMap<AccountType.Nature, DataBinder<? extends AccountType>> temp = new HashMap<AccountType.Nature, DataBinder<? extends AccountType>>();
				final LocalSettings localSettings = settingsService.getLocalSettings();

				final BeanBinder<SystemAccountType> systemBinder = BeanBinder.instance(SystemAccountType.class);
				initBasic(systemBinder);
				systemBinder.registerBinder("creditLimit",
						PropertyBinder.instance(BigDecimal.class, "creditLimit", localSettings.getNumberConverter()));
				systemBinder.registerBinder("upperCreditLimit", PropertyBinder.instance(BigDecimal.class,
						"upperCreditLimit", localSettings.getNumberConverter()));
				temp.put(AccountType.Nature.SYSTEM, systemBinder);

				final BeanBinder<MemberAccountType> memberBinder = BeanBinder.instance(MemberAccountType.class);
				initBasic(memberBinder);
				temp.put(AccountType.Nature.MEMBER, memberBinder);
				dataBinders = temp;
			}
			return dataBinders.get(nature);
		} finally {
			lock.readLock().unlock();
		}
	}

	public PaymentFilterService getPaymentFilterService() {
		return paymentFilterService;
	}

	public TransferTypeService getTransferTypeService() {
		return transferTypeService;
	}

	public void onLocalSettingsUpdate(final LocalSettingsEvent event) {
		try {
			lock.writeLock().lock();
			dataBinders = null;
		} finally {
			lock.writeLock().unlock();
		}
	}

	@Inject
	public void setAccountFeeService(final AccountFeeService accountFeeService) {
		this.accountFeeService = accountFeeService;
	}

	@Inject
	public void setAccountTypeService(final AccountTypeService accountTypeService) {
		this.accountTypeService = accountTypeService;
	}

	@Inject
	public void setCurrencyService(final CurrencyService currencyService) {
		this.currencyService = currencyService;
	}

	@Inject
	public void setPaymentFilterService(final PaymentFilterService paymentFilterService) {
		this.paymentFilterService = paymentFilterService;
	}

	@Inject
	public void setTransferTypeService(final TransferTypeService transferTypeService) {
		this.transferTypeService = transferTypeService;
	}

	public static class EditAccountTypeRequestDTO {
		private long accountTypeId;
		protected Map<String, Object> values;

		public Map<String, Object> getValues() {
			return values;
		}

		public void setValues(final Map<String, Object> values) {
			this.values = values;
		}

		public Map<String, Object> getAccountType() {
			return values;
		}

		public Object getAccountType(final String key) {
			return values.get(key);
		}

		public long getAccountTypeId() {
			return accountTypeId;
		}

		public void setAccountType(final Map<String, Object> map) {
			values = map;
		}

		public void setAccountType(final String key, final Object value) {
			values.put(key, value);
		}

		public void setAccountTypeId(final long accountTypeId) {
			this.accountTypeId = accountTypeId;
		}
	}

	public static class EditAccountTypeResponseDTO {
		String message;
		Map<String, Object> param;

		public EditAccountTypeResponseDTO(String message, Map<String, Object> param) {
			super();
			this.message = message;
			this.param = param;
		}

	}

	@RequestMapping(value = "/admin/editAccountType", method = RequestMethod.POST)
	@ResponseBody
	protected EditAccountTypeResponseDTO handleSubmit(@RequestBody EditAccountTypeRequestDTO form) throws Exception {
		// final EditAccountTypeForm form = context.getForm();
		AccountType accountType = resolveAccountType(form);
		final boolean isInsert = accountType.getId() == null;
		accountType = accountTypeService.save(accountType);
		String message = null;
		if (isInsert) {
			message = "accountType.inserted";
		} else {
			message = "accountType.modified";
		}
		Map<String, Object> param = new HashMap<String, Object>();
		param.put("accountTypeId", accountType.getId());
		EditAccountTypeResponseDTO response = new EditAccountTypeResponseDTO(message, param);
		return response;
	}

	private void initBasic(final BeanBinder<? extends AccountType> binder) {
		binder.registerBinder("id", PropertyBinder.instance(Long.class, "id", IdConverter.instance()));
		binder.registerBinder("name", PropertyBinder.instance(String.class, "name"));
		binder.registerBinder("description", PropertyBinder.instance(String.class, "description"));
		binder.registerBinder("currency", PropertyBinder.instance(Currency.class, "currency"));
	}

	private AccountType resolveAccountType(final EditAccountTypeRequestDTO form) {
		final long id = form.getAccountTypeId();
		AccountType.Nature nature;
		if (id <= 0L) {
			try {
				nature = AccountType.Nature.valueOf(form.getAccountType("nature").toString());
			} catch (final Exception e) {
				throw new ValidationException();
			}
		} else {
			nature = accountTypeService.load(id).getNature();
		}
		return getDataBinder(nature).readFromString(form.getAccountType());
	}
}