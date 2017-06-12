package nl.strohalm.cyclos.webservices.rest.accountfees;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.servlet.http.HttpServletRequest;

import nl.strohalm.cyclos.annotations.Inject;
import nl.strohalm.cyclos.controls.ActionContext;
import nl.strohalm.cyclos.controls.accounts.accountfees.EditAccountFeeForm;
import nl.strohalm.cyclos.entities.accounts.MemberAccountType;
import nl.strohalm.cyclos.entities.accounts.fees.account.AccountFee;
import nl.strohalm.cyclos.entities.accounts.fees.account.AccountFee.ChargeMode;
import nl.strohalm.cyclos.entities.accounts.fees.account.AccountFee.InvoiceMode;
import nl.strohalm.cyclos.entities.accounts.fees.account.AccountFee.PaymentDirection;
import nl.strohalm.cyclos.entities.accounts.fees.account.AccountFee.RunMode;
import nl.strohalm.cyclos.entities.accounts.fees.account.AccountFeeLogQuery;
import nl.strohalm.cyclos.entities.accounts.transactions.TransferType;
import nl.strohalm.cyclos.entities.groups.Group;
import nl.strohalm.cyclos.entities.groups.GroupQuery;
import nl.strohalm.cyclos.entities.groups.MemberGroup;
import nl.strohalm.cyclos.entities.settings.LocalSettings;
import nl.strohalm.cyclos.entities.settings.events.LocalSettingsEvent;
import nl.strohalm.cyclos.services.accountfees.AccountFeeService;
import nl.strohalm.cyclos.services.accounts.AccountTypeService;
import nl.strohalm.cyclos.services.groups.GroupService;
import nl.strohalm.cyclos.services.settings.SettingsService;
import nl.strohalm.cyclos.services.transfertypes.TransferTypeService;
import nl.strohalm.cyclos.utils.ActionHelper;
import nl.strohalm.cyclos.utils.RequestHelper;
import nl.strohalm.cyclos.utils.TimePeriod;
import nl.strohalm.cyclos.utils.WeekDay;
import nl.strohalm.cyclos.utils.binding.BeanBinder;
import nl.strohalm.cyclos.utils.binding.DataBinder;
import nl.strohalm.cyclos.utils.binding.DataBinderHelper;
import nl.strohalm.cyclos.utils.binding.MapBean;
import nl.strohalm.cyclos.utils.binding.PropertyBinder;
import nl.strohalm.cyclos.utils.binding.SimpleCollectionBinder;
import nl.strohalm.cyclos.utils.conversion.IdConverter;
import nl.strohalm.cyclos.utils.conversion.ReferenceConverter;
import nl.strohalm.cyclos.utils.query.PageHelper;
import nl.strohalm.cyclos.utils.validation.ValidationException;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;

import org.apache.struts.action.ActionForward;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class EditAccountFeeController extends BaseRestController {
	private AccountFeeService accountFeeService;
	private AccountTypeService accountTypeService;
	private TransferTypeService transferTypeService;
	private SettingsService settingsService;
	private GroupService groupService;
	private DataBinder<AccountFee> dataBinder;
	private ReadWriteLock lock = new ReentrantReadWriteLock(true);

	public AccountFeeService getAccountFeeService() {
		return accountFeeService;
	}

	public AccountTypeService getAccountTypeService() {
		return accountTypeService;
	}

	public DataBinder<AccountFee> getDataBinder() {
		try {
			lock.readLock().lock();
			if (dataBinder == null) {
				final LocalSettings localSettings = settingsService
						.getLocalSettings();
				final BeanBinder<AccountFee> binder = BeanBinder
						.instance(AccountFee.class);
				binder.registerBinder(
						"id",
						PropertyBinder.instance(Long.class, "id",
								IdConverter.instance()));
				binder.registerBinder("accountType", PropertyBinder.instance(
						MemberAccountType.class, "accountType",
						ReferenceConverter.instance(MemberAccountType.class)));
				binder.registerBinder("name",
						PropertyBinder.instance(String.class, "name"));
				binder.registerBinder("description",
						PropertyBinder.instance(String.class, "description"));
				binder.registerBinder("paymentDirection", PropertyBinder
						.instance(PaymentDirection.class, "paymentDirection"));
				binder.registerBinder("chargeMode",
						PropertyBinder.instance(ChargeMode.class, "chargeMode"));
				binder.registerBinder("enabled",
						PropertyBinder.instance(Boolean.TYPE, "enabled"));
				binder.registerBinder("enabledSince", PropertyBinder.instance(
						Calendar.class, "enabledSince",
						localSettings.getRawDateConverter()));
				binder.registerBinder("runMode",
						PropertyBinder.instance(RunMode.class, "runMode"));
				binder.registerBinder("recurrence",
						DataBinderHelper.timePeriodBinder("recurrence"));
				binder.registerBinder("day",
						PropertyBinder.instance(Byte.class, "day"));
				binder.registerBinder("hour",
						PropertyBinder.instance(Byte.class, "hour"));
				binder.registerBinder("invoiceMode", PropertyBinder.instance(
						InvoiceMode.class, "invoiceMode"));
				binder.registerBinder("amount", PropertyBinder.instance(
						BigDecimal.class, "amount",
						localSettings.getNumberConverter()));
				binder.registerBinder("freeBase", PropertyBinder.instance(
						BigDecimal.class, "freeBase",
						localSettings.getNumberConverter()));
				binder.registerBinder("transferType", PropertyBinder.instance(
						TransferType.class, "transferType",
						ReferenceConverter.instance(TransferType.class)));
				binder.registerBinder("groups",
						SimpleCollectionBinder.instance(Group.class, "groups"));
				dataBinder = binder;
			}
			return dataBinder;
		} finally {
			lock.readLock().unlock();
		}
	}

	public TransferTypeService getTransferTypeService() {
		return transferTypeService;
	}

	public void onLocalSettingsUpdate(final LocalSettingsEvent event) {
		try {
			lock.writeLock().lock();
			dataBinder = null;
		} finally {
			lock.writeLock().unlock();
		}
	}

	@Inject
	public void setAccountFeeService(final AccountFeeService accountFeeService) {
		this.accountFeeService = accountFeeService;
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

	public static class EditAccountFeeRequestDto {
		private long accountTypeId;
		private long accountFeeId;
		protected Map<String, Object> values;

		public Map<String, Object> getValues() {
			return values;
		}

		public void setValues(final Map<String, Object> values) {
			this.values = values;
		}

		public Map<String, Object> getAccountFee() {
			return values;
		}

		public Object getAccountFee(final String key) {
			return values.get(key);
		}

		public long getAccountFeeId() {
			return accountFeeId;
		}

		public long getAccountTypeId() {
			return accountTypeId;
		}

		public String getPaymentDirection() {
			return (String) getAccountFee("paymentDirection");
		}

		public void setAccountFee(final Map<String, Object> map) {
			values = map;
		}

		public void setAccountFee(final String key, final Object value) {
			values.put(key, value);
		}

		public void setAccountFeeId(final long transferTypeId) {
			accountFeeId = transferTypeId;
		}

		public void setAccountTypeId(final long accountTypeId) {
			this.accountTypeId = accountTypeId;
		}

		public void setPaymentDirection(final String paymentDirection) {
			setAccountFee("paymentDirection", paymentDirection);
		}

	}

	public static class EditAccountFeeResponseDto {
		private String message;
		private Map<String, Object> params;

		public EditAccountFeeResponseDto(String message,
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

	@RequestMapping(value = "/admin/editAccountFee", method = RequestMethod.POST)
	@ResponseBody
	protected EditAccountFeeResponseDto handleSubmit(
			@RequestBody EditAccountFeeRequestDto form) throws Exception {
		// final EditAccountFeeForm form = context.getForm();
		AccountFee accountFee = getDataBinder().readFromString(
				form.getAccountFee());
		final boolean isInsert = accountFee.getId() == null;
		accountFee = accountFeeService.save(accountFee);
		String message = null;
		if (isInsert)
			message = "accountFee.inserted";
		else
			message = "accountFee.modified";
		final Map<String, Object> params = new HashMap<String, Object>();
		params.put("accountTypeId", form.getAccountTypeId());
		params.put("accountFeeId", accountFee.getId());
		EditAccountFeeResponseDto response = new EditAccountFeeResponseDto(
				message, params);
		return response;
	}

	protected void prepareForm(final ActionContext context) throws Exception {
		final HttpServletRequest request = context.getRequest();
		final EditAccountFeeForm form = context.getForm();
		final long accountTypeId = form.getAccountTypeId();
		MemberAccountType accountType;
		try {
			accountType = (MemberAccountType) accountTypeService
					.load(accountTypeId);
		} catch (final Exception e) {
			throw new ValidationException();
		}

		final long id = form.getAccountFeeId();
		final boolean isInsert = id <= 0L;
		AccountFee accountFee;
		boolean alreadyRan;
		if (isInsert) {
			accountFee = new AccountFee();
			alreadyRan = false;
		} else {
			accountFee = accountFeeService.load(id,
					AccountFee.Relationships.GROUPS,
					AccountFee.Relationships.TRANSFER_TYPE);

			final List<TransferType> transferTypes = transferTypeService
					.getPosibleTTsForAccountFee(accountType,
							accountFee.getPaymentDirection());
			request.setAttribute("transferTypes", transferTypes);

			final AccountFeeLogQuery query = new AccountFeeLogQuery();
			query.setPageForCount();
			query.setAccountFee(accountFee);
			alreadyRan = PageHelper.hasResults(accountFeeService
					.searchLogs(query));
		}

		getDataBinder().writeAsString(form.getAccountFee(), accountFee);

		final GroupQuery groupQuery = new GroupQuery();
		groupQuery.setNatures(Group.Nature.MEMBER, Group.Nature.BROKER);
		groupQuery.setStatus(Group.Status.NORMAL);
		groupQuery.setMemberAccountType(accountType);
		final List<? extends MemberGroup> groups = (List<? extends MemberGroup>) groupService
				.search(groupQuery);

		request.setAttribute("accountFee", accountFee);
		request.setAttribute("isInsert", isInsert);
		request.setAttribute("alreadyRan", alreadyRan);
		request.setAttribute("accountType", accountType);
		request.setAttribute("groups", groups);
		request.setAttribute("recurrenceFields", Arrays.asList(
				TimePeriod.Field.MONTHS, TimePeriod.Field.WEEKS,
				TimePeriod.Field.DAYS));
		RequestHelper.storeEnum(request, AccountFee.ChargeMode.class,
				"chargeModes");
		RequestHelper.storeEnum(request, AccountFee.PaymentDirection.class,
				"paymentDirections");
		RequestHelper.storeEnum(request, AccountFee.RunMode.class, "runModes");
		RequestHelper.storeEnum(request, AccountFee.InvoiceMode.class,
				"invoiceModes");
		RequestHelper.storeEnum(request, WeekDay.class, "weekDays");
	}

	protected void validateForm(final ActionContext context) {
		final EditAccountFeeForm form = context.getForm();
		final AccountFee accountFee = getDataBinder().readFromString(
				form.getAccountFee());
		accountFeeService.validate(accountFee);
	}

}