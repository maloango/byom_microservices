/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.strohalm.cyclos.webservices.rest.members.accounts;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import nl.strohalm.cyclos.access.AdminMemberPermission;
import nl.strohalm.cyclos.access.AdminSystemPermission;
import nl.strohalm.cyclos.annotations.Inject;
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
import nl.strohalm.cyclos.utils.access.LoggedUser;
import nl.strohalm.cyclos.utils.binding.BeanBinder;
import nl.strohalm.cyclos.utils.binding.BeanCollectionBinder;
import nl.strohalm.cyclos.utils.binding.DataBinder;
import nl.strohalm.cyclos.utils.binding.DataBinderHelper;
import nl.strohalm.cyclos.utils.binding.PropertyBinder;
import nl.strohalm.cyclos.utils.conversion.ReferenceConverter;
import nl.strohalm.cyclos.utils.validation.ValidationException;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;
import nl.strohalm.cyclos.webservices.rest.GenericResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 *
 * @author Lue
 */
@Controller
public class SearchLoansController extends BaseRestController {

    public static DataBinder<LoanQuery> loanQueryDataBinder(final LocalSettings localSettings) {
        final BeanBinder<MemberCustomFieldValue> memberCustomValueBinder = BeanBinder.instance(MemberCustomFieldValue.class);
        memberCustomValueBinder.registerBinder("field", PropertyBinder.instance(MemberCustomField.class, "field", ReferenceConverter.instance(MemberCustomField.class)));
        memberCustomValueBinder.registerBinder("value", PropertyBinder.instance(String.class, "value"));

        final BeanBinder<PaymentCustomFieldValue> loanCustomValueBinder = BeanBinder.instance(PaymentCustomFieldValue.class);
        loanCustomValueBinder.registerBinder("field", PropertyBinder.instance(PaymentCustomField.class, "field", ReferenceConverter.instance(PaymentCustomField.class)));
        loanCustomValueBinder.registerBinder("value", PropertyBinder.instance(String.class, "value"));

        final BeanBinder<LoanQuery> binder = BeanBinder.instance(LoanQuery.class);
        binder.registerBinder("status", PropertyBinder.instance(Loan.Status.class, "status"));
        binder.registerBinder("queryStatus", PropertyBinder.instance(LoanQuery.QueryStatus.class, "queryStatus"));
        binder.registerBinder("transferStatus", PropertyBinder.instance(Transfer.Status.class, "transferStatus"));
        binder.registerBinder("transferType", PropertyBinder.instance(TransferType.class, "transferType"));
        binder.registerBinder("member", PropertyBinder.instance(Member.class, "member", ReferenceConverter.instance(Member.class)));
        binder.registerBinder("broker", PropertyBinder.instance(Member.class, "broker", ReferenceConverter.instance(Member.class)));
        binder.registerBinder("loanGroup", PropertyBinder.instance(LoanGroup.class, "loanGroup", ReferenceConverter.instance(LoanGroup.class)));
        binder.registerBinder("memberCustomValues", BeanCollectionBinder.instance(memberCustomValueBinder, "memberValues"));
        binder.registerBinder("loanCustomValues", BeanCollectionBinder.instance(loanCustomValueBinder, "loanValues"));
        binder.registerBinder("grantPeriod", DataBinderHelper.periodBinder(localSettings, "grantPeriod"));
        binder.registerBinder("expirationPeriod", DataBinderHelper.periodBinder(localSettings, "expirationPeriod"));
        binder.registerBinder("paymentPeriod", DataBinderHelper.periodBinder(localSettings, "paymentPeriod"));
        binder.registerBinder("transactionNumber", PropertyBinder.instance(String.class, "transactionNumber"));
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
    private PermissionService permissionService;
    private SettingsService settingsService;
    private ElementService elementService;
    private GroupService groupService;

    public void setPermissionService(PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    public void setSettingsService(SettingsService settingsService) {
        this.settingsService = settingsService;
    }

    public void setElementService(ElementService elementService) {
        this.elementService = elementService;
    }

    public void setGroupService(GroupService groupService) {
        this.groupService = groupService;
    }

    public DataBinder<LoanQuery> getDataBinder() {
        if (dataBinder == null) {
            final LocalSettings localSettings = settingsService.getLocalSettings();
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

//    public void onLocalSettingsUpdate(final LocalSettingsEvent event) {
//        super.onLocalSettingsUpdate(event);
//        dataBinder = null;
//    }
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
    public void setMemberCustomFieldService(final MemberCustomFieldService memberCustomFieldService) {
        this.memberCustomFieldService = memberCustomFieldService;
    }

    @Inject
    public void setPaymentCustomFieldService(final PaymentCustomFieldService paymentCustomFieldService) {
        this.paymentCustomFieldService = paymentCustomFieldService;
    }

    @Inject
    public void setTransferTypeService(final TransferTypeService transferTypeService) {
        this.transferTypeService = transferTypeService;
    }

    /**
     * If true, iterate over the loans to teste if any mas multiple payments
     */
    protected boolean computeMultiPayment() {
        return true;
    }

    public static class SearchLoansResponse extends GenericResponse {

        private long loanGroupId;
        private long memberId;
        private boolean queryAlreadyExecuted;
        private List<Loan> loans;
        private boolean isMultiPayment;
        private boolean fullQuery;
        private boolean byBroker;
        private boolean myLoans;
        private String queryStatus1;
        private LoanGroup loanGroup;
        private List<TransferType> transferTypes;
        private List<MemberCustomField> memberFields;
        private List<PaymentCustomField> allFields;
        private List<PaymentCustomField> customFieldsForList;
        private Collection<PaymentCustomFieldValue> loanCustomValues;
        private List<PaymentCustomField> customFieldsForSearch;
        private List<LoanGroup> loan;
        private Set<LoanQuery.QueryStatus> queryStatus;
        private List<Loan.Status> status2;
        private  List<Loan.Status> status3 = new ArrayList();
        

        public List<Loan.Status> getStatus3() {
            return status3;
        }

        public void setStatus3(List<Loan.Status> status3) {
            this.status3 = status3;
        }
        

        public List<Loan.Status> getStatus2() {
            return status2;
        }

        public void setStatus2(List<Loan.Status> status2) {
            this.status2 = status2;
        }

        public Set<LoanQuery.QueryStatus> getQueryStatus() {
            return queryStatus;
        }

        public void setQueryStatus(Set<LoanQuery.QueryStatus> queryStatus) {
            this.queryStatus = queryStatus;
        }

        public List<LoanGroup> getLoan() {
            return loan;
        }

        public void setLoan(List<LoanGroup> loan) {
            this.loan = loan;
        }

        public List<PaymentCustomField> getCustomFieldsForSearch() {
            return customFieldsForSearch;
        }

        public void setCustomFieldsForSearch(List<PaymentCustomField> customFieldsForSearch) {
            this.customFieldsForSearch = customFieldsForSearch;
        }

        public Collection<PaymentCustomFieldValue> getLoanCustomValues() {
            return loanCustomValues;
        }

        public void setLoanCustomValues(Collection<PaymentCustomFieldValue> loanCustomValues) {
            this.loanCustomValues = loanCustomValues;
        }

        public List<PaymentCustomField> getCustomFieldsForList() {
            return customFieldsForList;
        }

        public void setCustomFieldsForList(List<PaymentCustomField> customFieldsForList) {
            this.customFieldsForList = customFieldsForList;
        }

        public List<PaymentCustomField> getAllFields() {
            return allFields;
        }

        public void setAllFields(List<PaymentCustomField> allFields) {
            this.allFields = allFields;
        }

        public List<MemberCustomField> getMemberFields() {
            return memberFields;
        }

        public void setMemberFields(List<MemberCustomField> memberFields) {
            this.memberFields = memberFields;
        }

        public List<TransferType> getTransferTypes() {
            return transferTypes;
        }

        public void setTransferTypes(List<TransferType> transferTypes) {
            this.transferTypes = transferTypes;
        }

        public LoanGroup getLoanGroup() {
            return loanGroup;
        }

        public void setLoanGroup(LoanGroup loanGroup) {
            this.loanGroup = loanGroup;
        }

        public String getQueryStatus1() {
            return queryStatus1;
        }

        public void setQueryStatus1(String queryStatus1) {
            this.queryStatus1 = queryStatus1;
        }

        public boolean isByBroker() {
            return byBroker;
        }

        public void setByBroker(boolean byBroker) {
            this.byBroker = byBroker;
        }

        public boolean isMyLoans() {
            return myLoans;
        }

        public void setMyLoans(boolean myLoans) {
            this.myLoans = myLoans;
        }

        public boolean isFullQuery() {
            return fullQuery;
        }

        public void setFullQuery(boolean fullQuery) {
            this.fullQuery = fullQuery;
        }

        public boolean isIsMultiPayment() {
            return isMultiPayment;
        }

        public void setIsMultiPayment(boolean isMultiPayment) {
            this.isMultiPayment = isMultiPayment;
        }

        public List<Loan> getLoans() {
            return loans;
        }

        public void setLoans(List<Loan> loans) {
            this.loans = loans;
        }

        public long getLoanGroupId() {
            return loanGroupId;
        }

        public void setLoanGroupId(long loanGroupId) {
            this.loanGroupId = loanGroupId;
        }

        public long getMemberId() {
            return memberId;
        }

        public void setMemberId(long memberId) {
            this.memberId = memberId;
        }

        public boolean isQueryAlreadyExecuted() {
            return queryAlreadyExecuted;
        }

        public void setQueryAlreadyExecuted(boolean queryAlreadyExecuted) {
            this.queryAlreadyExecuted = queryAlreadyExecuted;
        }

    }

    @RequestMapping(value = "member/searchLoans", method = RequestMethod.POST)
    @ResponseBody
    public SearchLoansResponse executeQuery(@RequestBody SearchLoansResponse request) {
        SearchLoansResponse response = new SearchLoansResponse();
        final LoanQuery query = new LoanQuery();
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
        response.setLoans(loans);
        response.setIsMultiPayment(isMultiPayment);
        response.setQueryAlreadyExecuted(true);
        
        response.setStatus(0);
        response.setMessage("!! List of Loans....");
        return response;
    }

    @RequestMapping(value = "member/searchLoans", method = RequestMethod.GET)
    @ResponseBody
    public SearchLoansResponse prepareForm() {
        SearchLoansResponse response = new SearchLoansResponse();
        final long memberId = LoggedUser.user().getId();
        final long loanGroupId = LoggedUser.user().getId();
        final boolean fullQuery = LoggedUser.isAdministrator() && memberId == 0L && loanGroupId == 0L;
        boolean myLoans = false;
        boolean byBroker = false;
        response.setFullQuery(fullQuery);

//        if (RequestHelper.isGet(request) && !response.isQueryAlreadyExecuted()) {
//            if (fullQuery) {
//                response.setQueryStatus1(LoanQuery.QueryStatus.OPEN.name());
//            } else {
//                response.setMessage(Loan.Status.OPEN.name());
//            }
//        }
     //  final LoanQuery query = getDataBinder().readFromString(form.getQuery());
        LoanQuery query = new LoanQuery();
        query.fetch(Loan.Relationships.PAYMENTS, RelationshipHelper.nested(Loan.Relationships.TRANSFER, Payment.Relationships.TO, MemberAccount.Relationships.MEMBER, Element.Relationships.USER));

        // Retrieve the member if needed
        Member member = null;
        if (LoggedUser.isAdministrator()) {
            if (memberId > 0) {
                final Element element = elementService.load(memberId, Element.Relationships.USER);
                if (!(element instanceof Member)) {
                    throw new ValidationException();
                }
                member = (Member) element;
            } else {
                member = query.getMember();
            }

            AdminGroup adminGroup = LoggedUser.group();
            adminGroup = groupService.load(adminGroup.getId(), AdminGroup.Relationships.MANAGES_GROUPS);
            query.setGroups(adminGroup.getManagesGroups());
        } else {
            final Member loggedMember = (Member) LoggedUser.accountOwner();
            if (memberId == 0L || memberId == loggedMember.getId()) {
                member = loggedMember;
                myLoans = true;
            } else {
                final Element element = elementService.load(memberId, Element.Relationships.USER);
                if (!(element instanceof Member)) {
                    throw new ValidationException();
                }
                member = (Member) element;
                if (!LoggedUser.isBroker()) {
                    byBroker = true;
                    throw new ValidationException();

                }
            }
            query.setMember(member);

            response.setByBroker(byBroker);
            response.setMyLoans(myLoans);

            // Store the status
            if (LoggedUser.isAdministrator()) {
                // Admins see all statuses
              //  RequestHelper.storeEnum(request, Loan.Status.class, "status");
                List<Loan.Status> status3 = new ArrayList();
                status3.add(Loan.Status.OPEN);
                status3.add(Loan.Status.CLOSED);
                status3.add(Loan.Status.PENDING_AUTHORIZATION);
                status3.add(Loan.Status.AUTHORIZATION_DENIED);
                response.setStatus3(status3);
                        
            } else {
                //  response.setAttribute("status", EnumSet.of(Loan.Status.OPEN, Loan.Status.CLOSED));
                List<Loan.Status> status2 = new ArrayList();

                status2.add(Loan.Status.OPEN);
                status2.add(Loan.Status.CLOSED);
                response.setStatus2(status2);
            }
            final Set<LoanQuery.QueryStatus> queryStatus = EnumSet.allOf(LoanQuery.QueryStatus.class);
            // When there is no permission to view authorized loans, remove the statuses which are related to authorization
            if (!permissionService.hasPermission(AdminMemberPermission.LOANS_VIEW_AUTHORIZED)) {
                for (final Iterator<LoanQuery.QueryStatus> it = queryStatus.iterator(); it.hasNext();) {
                    if (it.next().isAuthorizationRelated()) {
                        it.remove();
                    }
                }
            }
            //response.setAttribute("queryStatus", queryStatus);
            response.setQueryStatus(queryStatus);

            // Retrieve the loan group if needed
//            LoanGroup loanGroup = null;
//            if (loanGroupId > 0L) {
//                loanGroup = loanGroupService.load(loanGroupId);
//                query.setLoanGroup(loanGroup);
//            }
//            response.setLoanGroup(loanGroup);

            if (fullQuery) {
                // Retrieve a list of all transfer types that are loans
                final TransferTypeQuery ttQuery = new TransferTypeQuery();
                ttQuery.setContext(TransactionContext.LOAN);
                if (LoggedUser.isAdministrator()) {
                    AdminGroup adminGroup = LoggedUser.group();
                    adminGroup = groupService.load(adminGroup.getId(), AdminGroup.Relationships.MANAGES_GROUPS);
                    ttQuery.setToGroups(adminGroup.getManagesGroups());
                }
                final List<TransferType> transferTypes = transferTypeService.search(ttQuery);
                if (transferTypes.size() == 1) {
                    // When there is a single transfer type, set it, so that the custom fields will be shown
                    query.setTransferType(transferTypes.iterator().next());
                    // response.setAttribute("singleTransferType", query.getTransferType());

                }
                response.setTransferTypes(transferTypes);

                // Get the member custom fields
                final List<MemberCustomField> memberFields = customFieldHelper.onlyForLoanSearch(memberCustomFieldService.list());
              //  request.setAttribute("memberFieldValues", customFieldHelper.buildEntries(memberFields, query.getMemberCustomValues()));
                response.setMemberFields(memberFields);
                // Get the payment custom fields
                final TransferType transferType = query.getTransferType();
              //  response.setQuery("loanValues", new MapBean(true, "field", "value"));
                if (transferType == null) {
                    // If setting no transfer type, don't filter by custom fields also
                    query.setLoanCustomValues(null);
                } else {
                    // Get the custom fields for search and for list
                    final List<PaymentCustomField> allFields = paymentCustomFieldService.list(transferType, true);
                    response.setAllFields(allFields);
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
                    response.setCustomFieldsForList(customFieldsForList);

                    // Ensure the query has no custom values which are not visible
                    final Collection<PaymentCustomFieldValue> loanCustomValues = query.getLoanCustomValues();
                    if (loanCustomValues != null) {
                        final Iterator<PaymentCustomFieldValue> iterator = loanCustomValues.iterator();
                        while (iterator.hasNext()) {
                            final PaymentCustomFieldValue fieldValue = iterator.next();
                            if (!customFieldsForSearch.contains(fieldValue.getField())) {
                                iterator.remove();
                            } else {
                            }
                        }
                    }

                    //response.setCustomFieldsForSearch("loanFieldValues", customFieldHelper.buildEntries(customFieldsForSearch, loanCustomValues));
                    response.setCustomFieldsForSearch(customFieldsForSearch);
                    response.setLoanCustomValues(loanCustomValues);
                }

//                if (permissionService.hasPermission(AdminSystemPermission.LOAN_GROUPS_VIEW)) {
//                    // Retrieve a list of all loan groups
//                    final LoanGroupQuery lgQuery = new LoanGroupQuery();
//                    response.setLoan(loanGroupService.search(lgQuery));
//                } else {
//                    // response.setLoanGroups("loanGroups", Collections.emptyList());
//                }

                if (query.getMember() != null) {
                    query.setMember((Member) elementService.load(query.getMember().getId(), Element.Relationships.USER));
                }
                if (query.getBroker() != null) {
                    query.setBroker((Member) elementService.load(query.getBroker().getId(), Element.Relationships.USER));
                }
            }

            response.setStatus(0);
            response.setMessage("!! Searching Loans....");

        }

        // @Override
//        protected boolean willExecuteQuery(final ActionContext context, final QueryParameters queryParameters) throws Exception {
//        
//            final SearchLoansForm form = context.getForm();
//            if (form.isQueryAlreadyExecuted()) {
//                return true;
//            }
//
//            final HttpServletRequest request = context.getRequest();
//            final boolean fullQuery = (Boolean) request.getAttribute("fullQuery");
//            return !fullQuery || RequestHelper.isPost(request);
//        }
        return response;
    }
}
