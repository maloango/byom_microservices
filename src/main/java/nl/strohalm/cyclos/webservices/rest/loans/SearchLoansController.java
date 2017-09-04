package nl.strohalm.cyclos.webservices.rest.loans;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import nl.strohalm.cyclos.access.AdminMemberPermission;
import nl.strohalm.cyclos.access.AdminSystemPermission;
import nl.strohalm.cyclos.annotations.Inject;
import nl.strohalm.cyclos.controls.ActionContext;
//import nl.strohalm.cyclos.controls.loans.SearchLoansForm;
import nl.strohalm.cyclos.entities.accounts.MemberAccount;
import nl.strohalm.cyclos.entities.accounts.loans.Loan;
import nl.strohalm.cyclos.entities.accounts.loans.LoanGroup;
import nl.strohalm.cyclos.entities.accounts.loans.LoanGroupQuery;
import nl.strohalm.cyclos.entities.accounts.loans.LoanQuery;
import nl.strohalm.cyclos.entities.accounts.transactions.Payment;
import nl.strohalm.cyclos.entities.accounts.transactions.Transfer;
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
import nl.strohalm.cyclos.utils.conversion.ReferenceConverter;
import nl.strohalm.cyclos.utils.query.QueryParameters;
import nl.strohalm.cyclos.utils.validation.ValidationException;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class SearchLoansController extends BaseRestController {
	private GroupService groupService;
	private ElementService elementService;
	private PermissionService permissionService;
	private SettingsService settingsService;

	public static DataBinder<LoanQuery> loanQueryDataBinder(
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

		final BeanBinder<LoanQuery> binder = BeanBinder
				.instance(LoanQuery.class);
		binder.registerBinder("status",
				PropertyBinder.instance(Loan.Status.class, "status"));
		binder.registerBinder("queryStatus", PropertyBinder.instance(
				LoanQuery.QueryStatus.class, "queryStatus"));
		binder.registerBinder("transferStatus", PropertyBinder.instance(
				Transfer.Status.class, "transferStatus"));
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
		binder.registerBinder("grantPeriod",
				DataBinderHelper.periodBinder(localSettings, "grantPeriod"));
		binder.registerBinder("expirationPeriod", DataBinderHelper
				.periodBinder(localSettings, "expirationPeriod"));
		binder.registerBinder("paymentPeriod",
				DataBinderHelper.periodBinder(localSettings, "paymentPeriod"));
		binder.registerBinder("transactionNumber",
				PropertyBinder.instance(String.class, "transactionNumber"));
		binder.registerBinder("pageParameters", DataBinderHelper.pageBinder());
		return binder;
	}

	private PaymentCustomFieldService paymentCustomFieldService;
	private MemberCustomFieldService memberCustomFieldService;
	private DataBinder<LoanQuery> dataBinder;
	private LoanGroupService loanGroupService;
	private LoanService loanService;
	private TransferTypeService transferTypeService;

	private CustomFieldHelper customFieldHelper;

	public DataBinder<LoanQuery> getDataBinder() {
		if (dataBinder == null) {
			final LocalSettings localSettings = settingsService
					.getLocalSettings();
			dataBinder = loanQueryDataBinder(localSettings);
		}
		return dataBinder;
	}

	public LoanGroupService getLoanGroupService() {
		return loanGroupService;
	}

	public LoanService getLoanService() {
		return loanService;
	}

	public PaymentCustomFieldService getPaymentCustomFieldService() {
		return paymentCustomFieldService;
	}

	public TransferTypeService getTransferTypeService() {
		return transferTypeService;
	}

	@Inject
	public void setCustomFieldHelper(final CustomFieldHelper customFieldHelper) {
		this.customFieldHelper = customFieldHelper;
	}

	@Inject
	public void setLoanGroupService(final LoanGroupService loanGroupService) {
		this.loanGroupService = loanGroupService;
	}

	@Inject
	public void setLoanService(final LoanService loanService) {
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
	public void setTransferTypeService(
			final TransferTypeService transferTypeService) {
		this.transferTypeService = transferTypeService;
	}

	/**
	 * If true, iterate over the loans to teste if any mas multiple payments
	 */
	protected boolean computeMultiPayment() {
		return true;
	}

	public static class SearchLoansRequestDto {
		private long loanGroupId;
		private long memberId;
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

		public long getLoanGroupId() {
			return loanGroupId;
		}

		public long getMemberId() {
			return memberId;
		}

		public boolean isQueryAlreadyExecuted() {
			return queryAlreadyExecuted;
		}

		public void setLoanGroupId(final long loanGroupId) {
			this.loanGroupId = loanGroupId;
		}

		public void setMemberId(final long memberId) {
			this.memberId = memberId;
		}

		public void setQueryAlreadyExecuted(final boolean queryAlreadyExecuted) {
			this.queryAlreadyExecuted = queryAlreadyExecuted;
		}
	}

	public static class SearchLoansResponseDto {
		List<Loan> loans;
		boolean isMultiPayment;

        public List<Loan> getLoans() {
            return loans;
        }

        public void setLoans(List<Loan> loans) {
            this.loans = loans;
        }

        public boolean isIsMultiPayment() {
            return isMultiPayment;
        }

        public void setIsMultiPayment(boolean isMultiPayment) {
            this.isMultiPayment = isMultiPayment;
        }
                
		public SearchLoansResponseDto(List<Loan> loans, boolean isMultiPayment) {
			super();
			this.loans = loans;
			this.isMultiPayment = isMultiPayment;
		}

	}

	@RequestMapping(value = "member/searchLoans", method = RequestMethod.GET)
	@ResponseBody
	protected SearchLoansResponseDto executeQuery(
			@RequestBody SearchLoansRequestDto form,
			final QueryParameters queryParameters) {
		SearchLoansResponseDto response = null;
                try{
		final LoanQuery query = (LoanQuery) queryParameters;
		final List<Loan> loans = loanService.search(query);
		boolean isMultiPayment = false;
		if (computeMultiPayment()) {
			for (final Loan loan : loans) {
				if (loan.getParameters().getType() != Loan.Type.SINGLE_PAYMENT) {
					isMultiPayment = true;
					break;
				}
			}
		}
		form.setQueryAlreadyExecuted(true);
		response = new SearchLoansResponseDto(loans,
				isMultiPayment);}
                catch(Exception e){
                    e.printStackTrace();
                }
		return response;
	}

//	protected QueryParameters prepareForm(final ActionContext context) {
//		final HttpServletRequest request = context.getRequest();
//		final SearchLoansForm form = context.getForm();
//
//		final long memberId = form.getMemberId();
//		final long loanGroupId = form.getLoanGroupId();
//		final boolean fullQuery = context.isAdmin() && memberId == 0L
//				&& loanGroupId == 0L;
//		boolean myLoans = false;
//		boolean byBroker = false;
//		request.setAttribute("fullQuery", fullQuery);
//
//		if (RequestHelper.isGet(request) && !form.isQueryAlreadyExecuted()) {
//			if (fullQuery) {
//				form.setQuery("queryStatus", LoanQuery.QueryStatus.OPEN.name());
//			} else {
//				form.setQuery("status", Loan.Status.OPEN.name());
//			}
//		}
//
//		final LoanQuery query = getDataBinder().readFromString(form.getQuery());
//		query.fetch(Loan.Relationships.PAYMENTS, RelationshipHelper.nested(
//				Loan.Relationships.TRANSFER, Payment.Relationships.TO,
//				MemberAccount.Relationships.MEMBER, Element.Relationships.USER));
//
//		// Retrieve the member if needed
//		Member member = null;
//		if (context.isAdmin()) {
//			if (memberId > 0) {
//				final Element element = elementService.load(memberId,
//						Element.Relationships.USER);
//				if (!(element instanceof Member)) {
//					throw new ValidationException();
//				}
//				member = (Member) element;
//			} else {
//				member = query.getMember();
//			}
//
//			AdminGroup adminGroup = context.getGroup();
//			adminGroup = groupService.load(adminGroup.getId(),
//					AdminGroup.Relationships.MANAGES_GROUPS);
//			query.setGroups(adminGroup.getManagesGroups());
//		} else {
//			final Member loggedMember = (Member) context.getAccountOwner();
//			if (memberId == 0L || memberId == loggedMember.getId()) {
//				member = loggedMember;
//				myLoans = true;
//			} else {
//				final Element element = elementService.load(memberId,
//						Element.Relationships.USER);
//				if (!(element instanceof Member)) {
//					throw new ValidationException();
//				}
//				member = (Member) element;
//				if (!context.isBrokerOf(member)) {
//					throw new ValidationException();
//				}
//				byBroker = true;
//			}
//		}
//		query.setMember(member);
//		request.setAttribute("member", member);
//		request.setAttribute("myLoans", myLoans);
//		request.setAttribute("byBroker", byBroker);
//
//		// Store the status
//		if (context.isAdmin()) {
//			// Admins see all statuses
//			RequestHelper.storeEnum(request, Loan.Status.class, "status");
//		} else {
//			request.setAttribute("status",
//					EnumSet.of(Loan.Status.OPEN, Loan.Status.CLOSED));
//		}
//		final Set<LoanQuery.QueryStatus> queryStatus = EnumSet
//				.allOf(LoanQuery.QueryStatus.class);
//		// When there is no permission to view authorized loans, remove the
//		// statuses which are related to authorization
//		if (!permissionService
//				.hasPermission(AdminMemberPermission.LOANS_VIEW_AUTHORIZED)) {
//			for (final Iterator<LoanQuery.QueryStatus> it = queryStatus
//					.iterator(); it.hasNext();) {
//				if (it.next().isAuthorizationRelated()) {
//					it.remove();
//				}
//			}
//		}
//		request.setAttribute("queryStatus", queryStatus);
//
//		// Retrieve the loan group if needed
//		LoanGroup loanGroup = null;
//		if (loanGroupId > 0L) {
//			loanGroup = loanGroupService.load(loanGroupId);
//			query.setLoanGroup(loanGroup);
//		}
//		request.setAttribute("loanGroup", loanGroup);
//
//		if (fullQuery) {
//			// Retrieve a list of all transfer types that are loans
//			final TransferTypeQuery ttQuery = new TransferTypeQuery();
//			ttQuery.setContext(TransactionContext.LOAN);
//			if (context.isAdmin()) {
//				AdminGroup adminGroup = context.getGroup();
//				adminGroup = groupService.load(adminGroup.getId(),
//						AdminGroup.Relationships.MANAGES_GROUPS);
//				ttQuery.setToGroups(adminGroup.getManagesGroups());
//			}
//			final List<TransferType> transferTypes = transferTypeService
//					.search(ttQuery);
//			if (transferTypes.size() == 1) {
//				// When there is a single transfer type, set it, so that the
//				// custom fields will be shown
//				query.setTransferType(transferTypes.iterator().next());
//				request.setAttribute("singleTransferType",
//						query.getTransferType());
//			}
//			request.setAttribute("transferTypes", transferTypes);
//
//			// Get the member custom fields
//			final List<MemberCustomField> memberFields = customFieldHelper
//					.onlyForLoanSearch(memberCustomFieldService.list());
//			request.setAttribute(
//					"memberFieldValues",
//					customFieldHelper.buildEntries(memberFields,
//							query.getMemberCustomValues()));
//
//			// Get the payment custom fields
//			final TransferType transferType = query.getTransferType();
//			form.setQuery("loanValues", new MapBean(true, "field", "value"));
//			if (transferType == null) {
//				// If setting no transfer type, don't filter by custom fields
//				// also
//				query.setLoanCustomValues(null);
//			} else {
//				// Get the custom fields for search and for list
//				final List<PaymentCustomField> allFields = paymentCustomFieldService
//						.list(transferType, true);
//				request.setAttribute("allFields", allFields);
//				final List<PaymentCustomField> customFieldsForSearch = new ArrayList<PaymentCustomField>();
//				final List<PaymentCustomField> customFieldsForList = new ArrayList<PaymentCustomField>();
//				for (final PaymentCustomField customField : allFields) {
//					if (customField.getSearchAccess() != PaymentCustomField.Access.NONE) {
//						customFieldsForSearch.add(customField);
//					}
//					if (customField.getListAccess() != PaymentCustomField.Access.NONE) {
//						customFieldsForList.add(customField);
//					}
//				}
//				request.setAttribute("customFieldsForList", customFieldsForList);
//
//				// Ensure the query has no custom values which are not visible
//				final Collection<PaymentCustomFieldValue> loanCustomValues = query
//						.getLoanCustomValues();
//				if (loanCustomValues != null) {
//					final Iterator<PaymentCustomFieldValue> iterator = loanCustomValues
//							.iterator();
//					while (iterator.hasNext()) {
//						final PaymentCustomFieldValue fieldValue = iterator
//								.next();
//						if (!customFieldsForSearch.contains(fieldValue
//								.getField())) {
//							iterator.remove();
//						}
//					}
//				}
//
//				request.setAttribute("loanFieldValues", customFieldHelper
//						.buildEntries(customFieldsForSearch, loanCustomValues));
//			}
//
//			if (permissionService
//					.hasPermission(AdminSystemPermission.LOAN_GROUPS_VIEW)) {
//				// Retrieve a list of all loan groups
//				final LoanGroupQuery lgQuery = new LoanGroupQuery();
//				request.setAttribute("loanGroups",
//						loanGroupService.search(lgQuery));
//			} else {
//				request.setAttribute("loanGroups", Collections.emptyList());
//			}
//
//			if (query.getMember() != null) {
//				query.setMember((Member) elementService.load(query.getMember()
//						.getId(), Element.Relationships.USER));
//			}
//			if (query.getBroker() != null) {
//				query.setBroker((Member) elementService.load(query.getBroker()
//						.getId(), Element.Relationships.USER));
//			}
//		}
//
//		return query;
//	}
//
//	protected boolean willExecuteQuery(final ActionContext context,
//			final QueryParameters queryParameters) throws Exception {
//		final SearchLoansForm form = context.getForm();
//		if (form.isQueryAlreadyExecuted()) {
//			return true;
//		}
//
//		final HttpServletRequest request = context.getRequest();
//		final boolean fullQuery = (Boolean) request.getAttribute("fullQuery");
//		return !fullQuery || RequestHelper.isPost(request);
//	}

}
