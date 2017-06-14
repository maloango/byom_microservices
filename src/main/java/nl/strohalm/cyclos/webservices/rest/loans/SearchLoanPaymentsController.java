package nl.strohalm.cyclos.webservices.rest.loans;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import nl.strohalm.cyclos.access.AdminSystemPermission;
import nl.strohalm.cyclos.annotations.Inject;
import nl.strohalm.cyclos.controls.ActionContext;
import nl.strohalm.cyclos.controls.loans.SearchLoanPaymentsForm;
import nl.strohalm.cyclos.entities.accounts.MemberAccount;
import nl.strohalm.cyclos.entities.accounts.loans.Loan;
import nl.strohalm.cyclos.entities.accounts.loans.LoanGroup;
import nl.strohalm.cyclos.entities.accounts.loans.LoanGroupQuery;
import nl.strohalm.cyclos.entities.accounts.loans.LoanPayment;
import nl.strohalm.cyclos.entities.accounts.loans.LoanPaymentQuery;
import nl.strohalm.cyclos.entities.accounts.transactions.Payment;
import nl.strohalm.cyclos.entities.accounts.transactions.TransferType;
import nl.strohalm.cyclos.entities.accounts.transactions.TransferTypeQuery;
import nl.strohalm.cyclos.entities.customization.fields.MemberCustomField;
import nl.strohalm.cyclos.entities.customization.fields.MemberCustomFieldValue;
import nl.strohalm.cyclos.entities.customization.fields.PaymentCustomField;
import nl.strohalm.cyclos.entities.customization.fields.PaymentCustomFieldValue;
import nl.strohalm.cyclos.entities.groups.AdminGroup;
import nl.strohalm.cyclos.entities.members.Element;
import nl.strohalm.cyclos.entities.members.Member;
import nl.strohalm.cyclos.entities.settings.LocalSettings;
import nl.strohalm.cyclos.services.customization.MemberCustomFieldService;
import nl.strohalm.cyclos.services.customization.PaymentCustomFieldService;
import nl.strohalm.cyclos.services.elements.ElementService;
import nl.strohalm.cyclos.services.groups.GroupService;
import nl.strohalm.cyclos.services.loangroups.LoanGroupService;
import nl.strohalm.cyclos.services.permissions.PermissionService;
import nl.strohalm.cyclos.services.settings.SettingsService;
import nl.strohalm.cyclos.services.transactions.LoanService;
import nl.strohalm.cyclos.services.transactions.TransactionContext;
import nl.strohalm.cyclos.services.transfertypes.TransferTypeService;
import nl.strohalm.cyclos.utils.CustomFieldHelper;
import nl.strohalm.cyclos.utils.RelationshipHelper;
import nl.strohalm.cyclos.utils.RequestHelper;
import nl.strohalm.cyclos.utils.binding.BeanBinder;
import nl.strohalm.cyclos.utils.binding.BeanCollectionBinder;
import nl.strohalm.cyclos.utils.binding.DataBinder;
import nl.strohalm.cyclos.utils.binding.DataBinderHelper;
import nl.strohalm.cyclos.utils.binding.MapBean;
import nl.strohalm.cyclos.utils.binding.PropertyBinder;
import nl.strohalm.cyclos.utils.binding.SimpleCollectionBinder;
import nl.strohalm.cyclos.utils.conversion.ReferenceConverter;
import nl.strohalm.cyclos.utils.query.QueryParameters;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;

@Controller
public class SearchLoanPaymentsController extends BaseRestController {

	public static DataBinder<LoanPaymentQuery> loanPaymentQueryDataBinder(
			final LocalSettings localSettings) {
		final BeanBinder<MemberCustomFieldValue> memberCustomValueBinder = BeanBinder
				.instance(MemberCustomFieldValue.class);
		memberCustomValueBinder.registerBinder("field", PropertyBinder
				.instance(MemberCustomField.class, "field",
						ReferenceConverter.instance(MemberCustomField.class)));
		memberCustomValueBinder.registerBinder("value",
				PropertyBinder.instance(String.class, "value"));

		final BeanBinder<PaymentCustomFieldValue> loanCustomValueBinder = BeanBinder
				.instance(PaymentCustomFieldValue.class);
		loanCustomValueBinder.registerBinder("field", PropertyBinder.instance(
				PaymentCustomField.class, "field",
				ReferenceConverter.instance(PaymentCustomField.class)));
		loanCustomValueBinder.registerBinder("value",
				PropertyBinder.instance(String.class, "value"));

		final BeanBinder<LoanPaymentQuery> binder = BeanBinder
				.instance(LoanPaymentQuery.class);
		binder.registerBinder("statusList", SimpleCollectionBinder.instance(
				LoanPayment.Status.class, "statusList"));
		binder.registerBinder("transferType",
				PropertyBinder.instance(TransferType.class, "transferType"));
		binder.registerBinder("member", PropertyBinder.instance(Member.class,
				"member", ReferenceConverter.instance(Member.class)));
		binder.registerBinder("broker", PropertyBinder.instance(Member.class,
				"broker", ReferenceConverter.instance(Member.class)));
		binder.registerBinder("loanGroup", PropertyBinder.instance(
				LoanGroup.class, "loanGroup",
				ReferenceConverter.instance(LoanGroup.class)));
		binder.registerBinder("memberCustomValues", BeanCollectionBinder
				.instance(memberCustomValueBinder, "memberValues"));
		binder.registerBinder("loanCustomValues", BeanCollectionBinder
				.instance(loanCustomValueBinder, "loanValues"));
		binder.registerBinder("expirationPeriod", DataBinderHelper
				.periodBinder(localSettings, "expirationPeriod"));
		binder.registerBinder("repaymentPeriod",
				DataBinderHelper.periodBinder(localSettings, "repaymentPeriod"));
		binder.registerBinder("pageParameters", DataBinderHelper.pageBinder());
		return binder;
	}

	protected PaymentCustomFieldService paymentCustomFieldService;
	protected MemberCustomFieldService memberCustomFieldService;
	protected LoanService loanService;
	protected TransferTypeService transferTypeService;
	protected LoanGroupService loanGroupService;
	private GroupService groupService;
	private ElementService elementService;
	private PermissionService permissionService;
	private SettingsService settingsService;
	private DataBinder<LoanPaymentQuery> dataBinder;

	private CustomFieldHelper customFieldHelper;

	public DataBinder<LoanPaymentQuery> getDataBinder() {
		if (dataBinder == null) {
			final LocalSettings settings = settingsService.getLocalSettings();
			dataBinder = loanPaymentQueryDataBinder(settings);
		}
		return dataBinder;
	}

	@Inject
	public void setCustomFieldHelper(final CustomFieldHelper customFieldHelper) {
		this.customFieldHelper = customFieldHelper;
	}

	@Inject
	public final void setLoanGroupService(
			final LoanGroupService loanGroupService) {
		this.loanGroupService = loanGroupService;
	}

	@Inject
	public final void setLoanService(final LoanService loanService) {
		this.loanService = loanService;
	}

	@Inject
	public void setMemberCustomFieldService(
			final MemberCustomFieldService memberCustomFieldService) {
		this.memberCustomFieldService = memberCustomFieldService;
	}

	@Inject
	public void setPaymentCustomFieldService(
			final PaymentCustomFieldService paymentCustomFieldService) {
		this.paymentCustomFieldService = paymentCustomFieldService;
	}

	@Inject
	public final void setTransferTypeService(
			final TransferTypeService transferTypeService) {
		this.transferTypeService = transferTypeService;
	}

	public static class SearchLoanPaymentsRequestDto {

		private boolean queryAlreadyExecuted;
		protected Map<String, Object> values;

		public Map<String, Object> getValues() {
			return values;
		}

		public void setValues(final Map<String, Object> values) {
			this.values = values;
		}

		public Map<String, Object> getQuery() {
			return values;
		}

		public Object getQuery(final String key) {
			return values.get(key);
		}

		public void setQuery(final Map<String, Object> query) {
			values = query;
		}

		public void setQuery(final String key, final Object value) {
			values.put(key, value);
		}

		public boolean isQueryAlreadyExecuted() {
			return queryAlreadyExecuted;
		}

		public void setQueryAlreadyExecuted(final boolean queryAlreadyExecuted) {
			this.queryAlreadyExecuted = queryAlreadyExecuted;
		}
	}

	public static class SearchLoanPaymentsResponseDto {
		private List<LoanPayment> loanPayments;

		public SearchLoanPaymentsResponseDto(List<LoanPayment> loanPayments) {
			super();
			this.loanPayments = loanPayments;
		}

	}

	@RequestMapping(value = "/admin/managePasswords", method = RequestMethod.POST)
	@ResponseBody
	protected SearchLoanPaymentsResponseDto executeQuery(
			@RequestBody SearchLoanPaymentsRequestDto form,
			final QueryParameters queryParameters) {
		// final SearchLoanPaymentsForm form = context.getForm();
		// final HttpServletRequest request = context.getRequest();
		final LoanPaymentQuery query = (LoanPaymentQuery) queryParameters;
		final List<LoanPayment> loanPayments = loanService.search(query);
		form.setQueryAlreadyExecuted(true);
		SearchLoanPaymentsResponseDto response = new SearchLoanPaymentsResponseDto(
				loanPayments);
		return response;
	}

	protected QueryParameters prepareForm(final ActionContext context) {
		final HttpServletRequest request = context.getRequest();
		final SearchLoanPaymentsForm form = context.getForm();
		final LoanPaymentQuery query = getDataBinder().readFromString(
				form.getQuery());
		query.fetch(RelationshipHelper.nested(LoanPayment.Relationships.LOAN,
				Loan.Relationships.TRANSFER,
				Payment.Relationships.CUSTOM_VALUES), RelationshipHelper
				.nested(LoanPayment.Relationships.LOAN,
						Loan.Relationships.TRANSFER, Payment.Relationships.TO,
						MemberAccount.Relationships.MEMBER,
						Element.Relationships.USER));

		// Just search loan payments of members in groups managed by admin group
		AdminGroup adminGroup = context.getGroup();
		adminGroup = groupService.load(adminGroup.getId(),
				AdminGroup.Relationships.MANAGES_GROUPS);
		query.setGroups(adminGroup.getManagesGroups());

		// Retrieve a list of all transfer types that are loans
		final TransferTypeQuery ttQuery = new TransferTypeQuery();
		ttQuery.setContext(TransactionContext.LOAN);
		ttQuery.setToGroups(adminGroup.getManagesGroups());
		final List<TransferType> transferTypes = transferTypeService
				.search(ttQuery);
		if (transferTypes.size() == 1) {
			// When there is a single transfer type, set it, so that the custom
			// fields will be shown
			query.setTransferType(transferTypes.iterator().next());
			request.setAttribute("singleTransferType", query.getTransferType());
		}
		request.setAttribute("transferTypes", transferTypes);

		// Get the member custom fields
		final List<MemberCustomField> memberFields = customFieldHelper
				.onlyForLoanSearch(memberCustomFieldService.list());
		request.setAttribute(
				"memberFieldValues",
				customFieldHelper.buildEntries(memberFields,
						query.getMemberCustomValues()));

		// Get the payment custom fields
		final TransferType transferType = query.getTransferType();
		form.setQuery("loanValues", new MapBean(true, "field", "value"));
		if (transferType == null) {
			// If setting no transfer type, don't filter by custom fields also
			query.setLoanCustomValues(null);
		} else {
			// Get the custom fields for search and for list
			final List<PaymentCustomField> allFields = paymentCustomFieldService
					.list(transferType, true);
			request.setAttribute("allFields", allFields);
			final List<PaymentCustomField> customFieldsForSearch = new ArrayList<PaymentCustomField>();
			final List<PaymentCustomField> customFieldsForList = new ArrayList<PaymentCustomField>();
			for (final PaymentCustomField customField : allFields) {
				if (customField.getSearchAccess() != PaymentCustomField.Access.NONE) {
					customFieldsForSearch.add(customField);
				}
				if (customField.getListAccess() != PaymentCustomField.Access.NONE) {
					customFieldsForList.add(customField);
				}
			}
			request.setAttribute("customFieldsForList", customFieldsForList);

			// Ensure the query has no custom values which are not visible
			final Collection<PaymentCustomFieldValue> loanCustomValues = query
					.getLoanCustomValues();
			if (loanCustomValues != null) {
				final Iterator<PaymentCustomFieldValue> iterator = loanCustomValues
						.iterator();
				while (iterator.hasNext()) {
					final PaymentCustomFieldValue fieldValue = iterator.next();
					if (!customFieldsForSearch.contains(fieldValue.getField())) {
						iterator.remove();
					}
				}
			}

			request.setAttribute("loanFieldValues", customFieldHelper
					.buildEntries(customFieldsForSearch, loanCustomValues));
		}

		RequestHelper.storeEnum(request, LoanPayment.Status.class, "status");

		if (permissionService
				.hasPermission(AdminSystemPermission.LOAN_GROUPS_VIEW)) {
			// Retrieve a list of all loan groups
			final LoanGroupQuery lgQuery = new LoanGroupQuery();
			request.setAttribute("loanGroups", loanGroupService.search(lgQuery));
		} else {
			request.setAttribute("loanGroups", Collections.emptyList());
		}

		if (query.getMember() != null) {
			query.setMember((Member) elementService.load(query.getMember()
					.getId(), Element.Relationships.USER));
		}
		if (query.getBroker() != null) {
			query.setBroker((Member) elementService.load(query.getBroker()
					.getId(), Element.Relationships.USER));
		}

		return query;
	}

}
