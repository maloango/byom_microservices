package nl.strohalm.cyclos.webservices.rest.invoices;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import nl.strohalm.cyclos.annotations.Inject;
import nl.strohalm.cyclos.controls.ActionContext;
//import nl.strohalm.cyclos.controls.invoices.SendInvoiceForm;
//import nl.strohalm.cyclos.controls.payments.SchedulingType;
import nl.strohalm.cyclos.entities.access.Channel;
import nl.strohalm.cyclos.entities.access.MemberUser;
import nl.strohalm.cyclos.entities.access.OperatorUser;
import nl.strohalm.cyclos.entities.access.User;
import nl.strohalm.cyclos.entities.accounts.AccountOwner;
import nl.strohalm.cyclos.entities.accounts.AccountType;
import nl.strohalm.cyclos.entities.accounts.Currency;
import nl.strohalm.cyclos.entities.accounts.MemberAccountType;
import nl.strohalm.cyclos.entities.accounts.SystemAccountOwner;
import nl.strohalm.cyclos.entities.accounts.transactions.Invoice;
import nl.strohalm.cyclos.entities.accounts.transactions.InvoicePayment;
import nl.strohalm.cyclos.entities.accounts.transactions.TransferType;
import nl.strohalm.cyclos.entities.accounts.transactions.TransferTypeQuery;
import nl.strohalm.cyclos.entities.customization.fields.CustomFieldValue;
import nl.strohalm.cyclos.entities.customization.fields.PaymentCustomField;
import nl.strohalm.cyclos.entities.customization.fields.PaymentCustomFieldValue;
import nl.strohalm.cyclos.entities.groups.Group;
import nl.strohalm.cyclos.entities.groups.MemberGroup;
import nl.strohalm.cyclos.entities.groups.MemberGroupSettings;
import nl.strohalm.cyclos.entities.members.Element;
import nl.strohalm.cyclos.entities.members.Member;
import nl.strohalm.cyclos.entities.settings.LocalSettings;
import nl.strohalm.cyclos.services.accounts.AccountTypeService;
import nl.strohalm.cyclos.services.accounts.CurrencyService;
import nl.strohalm.cyclos.services.accounts.MemberAccountTypeQuery;
import nl.strohalm.cyclos.services.elements.ElementService;
import nl.strohalm.cyclos.services.settings.SettingsService;
import nl.strohalm.cyclos.services.transactions.InvoiceService;
import nl.strohalm.cyclos.services.transactions.TransactionContext;
import nl.strohalm.cyclos.services.transactions.exceptions.SendingInvoiceWithMultipleTransferTypesWithCustomFields;
import nl.strohalm.cyclos.services.transfertypes.TransferTypeService;
import nl.strohalm.cyclos.utils.TimePeriod;
import nl.strohalm.cyclos.utils.binding.BeanBinder;
import nl.strohalm.cyclos.utils.binding.BeanCollectionBinder;
import nl.strohalm.cyclos.utils.binding.DataBinder;
import nl.strohalm.cyclos.utils.binding.MapBean;
import nl.strohalm.cyclos.utils.binding.PropertyBinder;
import nl.strohalm.cyclos.utils.conversion.AccountOwnerConverter;
import nl.strohalm.cyclos.utils.conversion.CoercionHelper;
import nl.strohalm.cyclos.utils.conversion.HtmlConverter;
import nl.strohalm.cyclos.utils.conversion.IdConverter;
import nl.strohalm.cyclos.utils.conversion.ReferenceConverter;
import nl.strohalm.cyclos.utils.validation.ValidationException;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;

import org.apache.struts.action.ActionForward;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class SendInvoiceController extends BaseRestController {

	private AccountTypeService accountTypeService;
	private DataBinder<Invoice> dataBinder;
	private InvoiceService invoiceService;
	private TransferTypeService transferTypeService;
	private CurrencyService currencyService;
	private ElementService elementService;
	private SettingsService settingsService;

	public final ElementService getElementService() {
		return elementService;
	}

	public final void setElementService(ElementService elementService) {
		this.elementService = elementService;
	}

	public final SettingsService getSettingsService() {
		return settingsService;
	}

	public final void setSettingsService(SettingsService settingsService) {
		this.settingsService = settingsService;
	}

	public final CurrencyService getCurrencyService() {
		return currencyService;
	}

	public AccountTypeService getAccountTypeService() {
		return accountTypeService;
	}

	public DataBinder<Invoice> getDataBinder() {
		if (dataBinder == null) {
			final BeanBinder<? extends CustomFieldValue> customValueBinder = BeanBinder
					.instance(PaymentCustomFieldValue.class);
			customValueBinder.registerBinder("field",
					PropertyBinder.instance(PaymentCustomField.class, "field"));
			customValueBinder.registerBinder("value", PropertyBinder.instance(
					String.class, "value", HtmlConverter.instance()));

			final LocalSettings localSettings = settingsService
					.getLocalSettings();
			final BeanBinder<Invoice> binder = BeanBinder
					.instance(Invoice.class);
			binder.registerBinder("from", PropertyBinder.instance(
					AccountOwner.class, "from",
					AccountOwnerConverter.instance()));
			binder.registerBinder("to", PropertyBinder.instance(
					AccountOwner.class, "to", AccountOwnerConverter.instance()));
			binder.registerBinder("transferType", PropertyBinder.instance(
					TransferType.class, "type",
					ReferenceConverter.instance(TransferType.class)));
			binder.registerBinder("destinationAccountType", PropertyBinder
					.instance(AccountType.class, "destType",
							ReferenceConverter.instance(AccountType.class)));
			binder.registerBinder("amount", PropertyBinder.instance(
					BigDecimal.class, "amount",
					localSettings.getNumberConverter()));
			binder.registerBinder("description",
					PropertyBinder.instance(String.class, "description"));
			binder.registerBinder("customValues", BeanCollectionBinder
					.instance(customValueBinder, "customValues"));

			final BeanBinder<InvoicePayment> scheduledPayments = BeanBinder
					.instance(InvoicePayment.class);
			scheduledPayments.registerBinder("date",
					PropertyBinder.instance(Calendar.class, "date",
							localSettings.getRawDateConverter()));
			scheduledPayments.registerBinder("amount", PropertyBinder.instance(
					BigDecimal.class, "amount",
					localSettings.getNumberConverter()));
			binder.registerBinder("payments", BeanCollectionBinder.instance(
					scheduledPayments, "payments"));

			dataBinder = binder;
		}
		return dataBinder;
	}

	public InvoiceService getInvoiceService() {
		return invoiceService;
	}

	public TransferTypeService getTransferTypeService() {
		return transferTypeService;
	}

	@Inject
	public void setAccountTypeService(
			final AccountTypeService accountTypeService) {
		this.accountTypeService = accountTypeService;
	}

	@Inject
	public void setCurrencyService(final CurrencyService currencyService) {
		this.currencyService = currencyService;
	}

	@Inject
	public void setInvoiceService(final InvoiceService invoiceService) {
		this.invoiceService = invoiceService;
	}

	@Inject
	public void setTransferTypeService(
			final TransferTypeService transferTypeService) {
		this.transferTypeService = transferTypeService;
	}

//	protected ActionForward handleDisplay(final ActionContext context)
//			throws Exception {
//		final HttpServletRequest request = context.getRequest();
//		final SendInvoiceForm form = context.getForm();
//		final boolean toSystem = form.isToSystem();
//		final boolean selectMember = form.isSelectMember();
//
//		AccountOwner to;
//		final Member fromMember = (form.getFrom() == null) ? null
//				: (Member) elementService.load(Long.valueOf(form.getFrom()));
//		final Element loggedElement = context.getElement();
//		if (toSystem) {
//			// System invoice
//			to = SystemAccountOwner.instance();
//		} else {
//			if (!selectMember) {
//				// Retrieve the member to send invoice for
//				Member member = null;
//				final Long memberId = IdConverter.instance().valueOf(
//						form.getTo());
//				if (memberId != null && memberId != loggedElement.getId()) {
//					final Element element = elementService.load(memberId,
//							Element.Relationships.USER);
//					if (element instanceof Member) {
//						member = (Member) element;
//					}
//				}
//				if (member == null) {
//					throw new ValidationException();
//				}
//				request.setAttribute("member", member);
//				to = member;
//			} else {
//				// The member will be selected later
//				to = null;
//			}
//		}
//
//		// If we know who will receive the invoice, get the transfer types or
//		// dest account types
//		if (to != null) {
//			if (context.isAdmin() && fromMember == null) {
//				// Only admins may select the transfer type
//				final TransferTypeQuery query = new TransferTypeQuery();
//				query.setChannel(Channel.WEB);
//				query.setContext(TransactionContext.PAYMENT);
//				query.setFromOwner(to);
//				query.setToOwner(context.getAccountOwner());
//				query.setUsePriority(true);
//				request.setAttribute("transferTypes",
//						transferTypeService.search(query));
//			} else {
//				// Members may select the destination account type
//				final MemberAccountTypeQuery query = new MemberAccountTypeQuery();
//				query.setOwner(fromMember == null ? (Member) loggedElement
//						.getAccountOwner() : fromMember);
//				query.setCanPay(to);
//				final List<? extends AccountType> accountTypes = accountTypeService
//						.search(query);
//				if (accountTypes.isEmpty()) {
//					return context.sendError("invoice.error.noAccountType");
//				}
//				request.setAttribute("accountTypes", accountTypes);
//			}
//		}
//
//		// Resolve the possible currencies
//		final MemberGroup group = getMemberGroup(context);
//		final List<Currency> currencies;
//		if (group != null) {
//			currencies = currencyService.listByMemberGroup(group);
//			final MemberAccountType defaultAccountType = accountTypeService
//					.getDefault(group, AccountType.Relationships.CURRENCY);
//			// Preselect the default currency
//			if (defaultAccountType != null) {
//				form.setCurrency(CoercionHelper.coerce(String.class,
//						defaultAccountType.getCurrency()));
//			}
//		} else {
//			currencies = currencyService.listAll();
//		}
//		request.setAttribute("currencies", currencies);
//
//		if (currencies.isEmpty()) {
//			// No currencies means no possible payment!!!
//			throw new ValidationException("payment.error.noTransferType");
//		} else if (currencies.size() == 1) {
//			// Special case: There is a single currency. The JSP will use this
//			// object
//			request.setAttribute("singleCurrency", currencies.get(0));
//		}
//
//		request.setAttribute("toSystem", toSystem);
//		request.setAttribute("toMember", !toSystem);
//		request.setAttribute("selectMember", selectMember);
//		request.setAttribute("from", fromMember);
//
//		final boolean useTransferType = context.isAdmin() && fromMember == null;
//		request.setAttribute("useTransferType", useTransferType);
//
//		// Check whether scheduled payments may be performed
//		boolean allowsScheduling = false;
//		boolean allowsMultipleScheduling = false;
//		if (context.isAdmin() && fromMember == null) {
//			allowsScheduling = true;
//			allowsMultipleScheduling = true;
//		} else {
//			MemberGroup memberGroup;
//			if (fromMember == null) {
//				memberGroup = ((Member) context.getAccountOwner())
//						.getMemberGroup();
//			} else {
//				memberGroup = fromMember.getMemberGroup();
//			}
//			final MemberGroupSettings memberSettings = memberGroup
//					.getMemberSettings();
//			allowsScheduling = memberSettings.isAllowsScheduledPayments();
//			allowsMultipleScheduling = memberSettings
//					.isAllowsMultipleScheduledPayments();
//		}
//		if (allowsScheduling) {
//			request.setAttribute("allowsScheduling", allowsScheduling);
//			request.setAttribute("allowsMultipleScheduling",
//					allowsMultipleScheduling);
//			final Collection<SchedulingType> schedulingTypes = EnumSet.of(
//					SchedulingType.IMMEDIATELY, SchedulingType.SINGLE_FUTURE);
//			if (allowsMultipleScheduling) {
//				schedulingTypes.add(SchedulingType.MULTIPLE_FUTURE);
//			}
//			request.setAttribute("schedulingTypes", schedulingTypes);
//			request.setAttribute("schedulingFields", Arrays.asList(
//					TimePeriod.Field.MONTHS, TimePeriod.Field.WEEKS,
//					TimePeriod.Field.DAYS));
//		}
//
//		return context.getInputForward();
//	}

	public static class SendInvoiceRequestDto {
		private String destType;

		public String getDestType() {
			return destType;
		}

		public void setDestType(final String destinationAccountType) {
			destType = destinationAccountType;
		}

		private boolean selectMember;
		private String to;
		private boolean toSystem;
		private Object payments = new MapBean(true, "date", "amount");
		private String from;
		private String amount;
		private String description;
		private String type;
		private String date;
		private String currency;
		// private String from;
		private MapBean customValues = new MapBean(true, "field", "value");

		public String getAmount() {
			return amount;
		}

		public String getCurrency() {
			return currency;
		}

		public MapBean getCustomValues() {
			return customValues;
		}

		public String getDate() {
			return date;
		}

		public String getDescription() {
			return description;
		}

		public String getFrom() {
			return from;
		}

		public String getType() {
			return type;
		}

		public void setAmount(final String amount) {
			this.amount = amount;
		}

		public void setCurrency(final String currency) {
			this.currency = currency;
		}

		public void setCustomValues(final MapBean customValues) {
			this.customValues = customValues;
		}

		public void setDate(final String date) {
			this.date = date;
		}

		public void setDescription(final String description) {
			this.description = description;
		}

		public void setFrom(final String from) {
			this.from = from;
		}

		public void setType(final String type) {
			this.type = type;
		}

		public Object getPayments() {
			return payments;
		}

		public String getTo() {
			return to;
		}

		public boolean isSelectMember() {
			return selectMember;
		}

		public boolean isToSystem() {
			return toSystem;
		}

		public void setPayments(final Object payments) {
			this.payments = payments;
		}

		public void setSelectMember(final boolean selectMember) {
			this.selectMember = selectMember;
		}

		public void setTo(final String to) {
			this.to = to;
		}

		public void setToSystem(final boolean toSystem) {
			this.toSystem = toSystem;
		}

		public Member getMember() {
			final AccountOwner owner = getAccountOwner();
			return (Member) (owner instanceof Member ? owner : null);
		}

		User user;

		public boolean isMember() {
			return user instanceof MemberUser;
		}

		public boolean isOperator() {
			return user instanceof OperatorUser;
		}

		public AccountOwner getAccountOwner() {
			try {
				final Element element = getElement();
				return element.getAccountOwner();
			} catch (final NullPointerException e) {
				return null;
			}
		}

		@SuppressWarnings("unchecked")
		public <E extends Element> E getElement() {
			return (E) user.getElement();
		}

	}

	public static class SendInvoiceResponseDto {
		private String message;
		Map<String, Object> params;

		public SendInvoiceResponseDto(String message, Map<String, Object> params) {
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

	@RequestMapping(value = "admin/sendInvoice", method = RequestMethod.POST)
	@ResponseBody
	protected SendInvoiceResponseDto handleSubmit(
			@RequestBody SendInvoiceRequestDto form) throws Exception {
		
		final boolean fromProfile = !form.isToSystem()
				&& !form.isSelectMember();
		String message = null;
		Map<String, Object> params = null;
		SendInvoiceResponseDto response = new SendInvoiceResponseDto(message,
				params);
		try {
			final Invoice invoice = invoiceService.send(resolveInvoice(form));
			message = "invoice.sent";
			ActionForward forward = null;
			params = new HashMap<String, Object>();
			if (fromProfile) {
				// forward = context.findForward("profile");
				params.put("memberId", invoice.getToMember().getId());
			} else {
				// forward = context.findForward("newInvoice");
			}
			final Member fromMember = invoice.getFromMember();
			if (fromMember != null && !fromMember.equals(form.getMember())) {
				// From another member
				params.put("from", form.getFrom());
			} else if (fromProfile) {
				params.put("to", form.getTo());
			}
			if (form.isToSystem()) {
				params.put("toSystem", true);
			} else if (form.isSelectMember()) {
				params.put("selectMember", true);
			}
			return response;
		} catch (final SendingInvoiceWithMultipleTransferTypesWithCustomFields e) {
			message = "invoice.error.sendingWithMultipleTransferTypesWithCustomFields";
                
			return response;
		}
	}

//	private MemberGroup getMemberGroup(final ActionContext context) {
//		final SendInvoiceForm form = context.getForm();
//		final Long fromId = IdConverter.instance().valueOf(form.getFrom());
//		final Long toId = IdConverter.instance().valueOf(form.getTo());
//		Group group = null;
//		if (fromId == null && toId == null) {
//			group = context.getGroup();
//		} else if (fromId != null) {
//			final Element element = elementService.load(fromId,
//					Element.Relationships.GROUP);
//			group = element.getGroup();
//		} else {
//			final Element element = elementService.load(toId,
//					Element.Relationships.GROUP);
//			group = element.getGroup();
//		}
//		if (group instanceof MemberGroup) {
//			return (MemberGroup) group;
//		}
//		return null;
//	}

	private Invoice resolveInvoice(final SendInvoiceRequestDto form) {
		// final SendInvoiceForm form = context.getForm();
		final Invoice invoice = getDataBinder().readFromString(form);
		if ((form.isMember() && invoice.getFromMember() == null)
				|| form.isOperator()) {
			invoice.setFrom(form.getAccountOwner());
		}
		return invoice;
	}

}
