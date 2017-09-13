package nl.strohalm.cyclos.webservices.rest.accounts.details;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import nl.strohalm.cyclos.access.AdminMemberPermission;
import nl.strohalm.cyclos.access.AdminSystemPermission;
import nl.strohalm.cyclos.access.BrokerPermission;
import nl.strohalm.cyclos.access.MemberPermission;
import nl.strohalm.cyclos.annotations.Inject;
import nl.strohalm.cyclos.controls.ActionContext;
import nl.strohalm.cyclos.entities.EntityReference;
import nl.strohalm.cyclos.entities.accounts.Account;
import nl.strohalm.cyclos.entities.accounts.AccountOwner;
import nl.strohalm.cyclos.entities.accounts.AccountStatus;
import nl.strohalm.cyclos.entities.accounts.AccountType;
import nl.strohalm.cyclos.entities.accounts.SystemAccountOwner;
import nl.strohalm.cyclos.entities.accounts.SystemAccountType;
import nl.strohalm.cyclos.entities.accounts.transactions.Payment;
import nl.strohalm.cyclos.entities.accounts.transactions.PaymentFilter;
import nl.strohalm.cyclos.entities.accounts.transactions.PaymentFilterQuery;
import nl.strohalm.cyclos.entities.accounts.transactions.Transfer;
import nl.strohalm.cyclos.entities.accounts.transactions.TransferQuery;
import nl.strohalm.cyclos.entities.customization.fields.PaymentCustomField;
import nl.strohalm.cyclos.entities.customization.fields.PaymentCustomFieldValue;
import nl.strohalm.cyclos.entities.groups.AdminGroup;
import nl.strohalm.cyclos.entities.groups.Group;

import nl.strohalm.cyclos.entities.groups.GroupFilter;
import nl.strohalm.cyclos.entities.groups.GroupFilterQuery;
import nl.strohalm.cyclos.entities.groups.GroupQuery;
import nl.strohalm.cyclos.entities.groups.MemberGroup;
import nl.strohalm.cyclos.entities.members.Element;
import nl.strohalm.cyclos.entities.members.Member;
import nl.strohalm.cyclos.entities.members.OperatorQuery;
import nl.strohalm.cyclos.entities.settings.LocalSettings;
import nl.strohalm.cyclos.services.accounts.AccountDTO;
import nl.strohalm.cyclos.services.accounts.AccountService;
import nl.strohalm.cyclos.services.accounts.AccountTypeService;
import nl.strohalm.cyclos.services.accounts.GetTransactionsDTO;
import nl.strohalm.cyclos.services.customization.PaymentCustomFieldService;
import nl.strohalm.cyclos.services.elements.ElementService;
import nl.strohalm.cyclos.services.groups.GroupFilterService;
import nl.strohalm.cyclos.services.groups.GroupService;
import nl.strohalm.cyclos.services.permissions.PermissionService;
import nl.strohalm.cyclos.services.settings.SettingsService;
import nl.strohalm.cyclos.services.transactions.PaymentService;
import nl.strohalm.cyclos.services.transfertypes.PaymentFilterService;
import nl.strohalm.cyclos.utils.Period;
import nl.strohalm.cyclos.utils.PropertyHelper;
import nl.strohalm.cyclos.utils.RequestHelper;
import nl.strohalm.cyclos.utils.TimePeriod;
import nl.strohalm.cyclos.utils.access.LoggedUser;
import nl.strohalm.cyclos.utils.query.QueryParameters;
//import nl.strohalm.cyclos.webservices.payments.PaymentStatus;
import nl.strohalm.cyclos.entities.groups.Group.Status;
import nl.strohalm.cyclos.utils.binding.BeanBinder;
import nl.strohalm.cyclos.utils.binding.BeanCollectionBinder;
import nl.strohalm.cyclos.utils.binding.DataBinder;
import nl.strohalm.cyclos.utils.binding.DataBinderHelper;
import nl.strohalm.cyclos.utils.binding.PropertyBinder;
import nl.strohalm.cyclos.utils.binding.SimpleCollectionBinder;
import nl.strohalm.cyclos.utils.conversion.AccountOwnerConverter;
import nl.strohalm.cyclos.utils.conversion.ReferenceConverter;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;
import nl.strohalm.cyclos.webservices.rest.GenericResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class AccountHistoryController extends BaseRestController {

    protected AccountService accountService;
    protected PaymentFilterService paymentFilterService;
    protected PaymentCustomFieldService paymentCustomFieldService;

    private AccountTypeService accountTypeService;
    private PaymentService paymentService;
    private GroupFilterService groupFilterService;
    private SettingsService settingsService;
    private ElementService elementService;
    private PermissionService permissionService;
    private GroupService groupService;
     private DataBinder<TransferQuery>   dataBinder;

    public GroupService getGroupService() {
        return groupService;
    }

    @Inject
    public void setGroupService(GroupService groupService) {
        this.groupService = groupService;
    }

    public PermissionService getPermissionService() {
        return permissionService;
    }

    @Inject
    public void setPermissionService(PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    public ElementService getElementService() {
        return elementService;
    }

    @Inject
    public void setElementService(ElementService elementService) {
        this.elementService = elementService;
    }

    public SettingsService getSettingsService() {
        return settingsService;
    }

    @Inject
    public void setSettingsService(SettingsService settingsService) {
        this.settingsService = settingsService;
    }

    public AccountService getAccountService() {
        return accountService;
    }

    @Inject
    public void setAccountService(AccountService accountService) {
        this.accountService = accountService;
    }

    public PaymentFilterService getPaymentFilterService() {
        return paymentFilterService;
    }

    @Inject
    public void setPaymentFilterService(PaymentFilterService paymentFilterService) {
        this.paymentFilterService = paymentFilterService;
    }

    public PaymentCustomFieldService getPaymentCustomFieldService() {
        return paymentCustomFieldService;
    }

    @Inject
    public void setPaymentCustomFieldService(PaymentCustomFieldService paymentCustomFieldService) {
        this.paymentCustomFieldService = paymentCustomFieldService;
    }

    public AccountTypeService getAccountTypeService() {
        return accountTypeService;
    }

    @Inject
    public void setAccountTypeService(AccountTypeService accountTypeService) {
        this.accountTypeService = accountTypeService;
    }

    public PaymentService getPaymentService() {
        return paymentService;
    }

    @Inject
    public void setPaymentService(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    public GroupFilterService getGroupFilterService() {
        return groupFilterService;
    }

    @Inject
    public void setGroupFilterService(GroupFilterService groupFilterService) {
        this.groupFilterService = groupFilterService;
    }
    
    /**
     * Returns a databinder for a transferquery
     */
    public static DataBinder<TransferQuery> transferQueryDataBinder(final LocalSettings localSettings) {
        final BeanBinder<PaymentCustomFieldValue> customValueBinder = BeanBinder.instance(PaymentCustomFieldValue.class);
        customValueBinder.registerBinder("field", PropertyBinder.instance(PaymentCustomField.class, "field"));
        customValueBinder.registerBinder("value", PropertyBinder.instance(String.class, "value"));

        final BeanBinder<TransferQuery> binder = BeanBinder.instance(TransferQuery.class);
        binder.registerBinder("owner", PropertyBinder.instance(AccountOwner.class, "owner", AccountOwnerConverter.instance()));
        binder.registerBinder("status", PropertyBinder.instance(Transfer.Status.class, "status"));
        binder.registerBinder("type", PropertyBinder.instance(AccountType.class, "type", ReferenceConverter.instance(AccountType.class)));
        binder.registerBinder("period", DataBinderHelper.rawPeriodBinder(localSettings, "period"));
        binder.registerBinder("paymentFilter", PropertyBinder.instance(PaymentFilter.class, "paymentFilter", ReferenceConverter.instance(PaymentFilter.class)));
        binder.registerBinder("description", PropertyBinder.instance(String.class, "description"));
        binder.registerBinder("transactionNumber", PropertyBinder.instance(String.class, "transactionNumber"));
        binder.registerBinder("member", PropertyBinder.instance(Member.class, "member", ReferenceConverter.instance(Member.class)));
        binder.registerBinder("by", PropertyBinder.instance(Element.class, "by", ReferenceConverter.instance(Element.class)));
        binder.registerBinder("conciliated", PropertyBinder.instance(Boolean.class, "conciliated"));
        binder.registerBinder("groups", SimpleCollectionBinder.instance(MemberGroup.class, "groups"));
        binder.registerBinder("groupFilters", SimpleCollectionBinder.instance(GroupFilter.class, "groupFilters"));
        binder.registerBinder("customValues", BeanCollectionBinder.instance(customValueBinder, "customValues"));
        binder.registerBinder("pageParameters", DataBinderHelper.pageBinder());
        return binder;
    }
    
    public DataBinder<TransferQuery> getDataBinder() {
        if (dataBinder == null) {
            final LocalSettings localSettings = settingsService.getLocalSettings();
            dataBinder = transferQueryDataBinder(localSettings);
        }
        return dataBinder;
    }

    public static class AccountHistoryRequest {

        private boolean advanced;
        private Long memberId;
        private Long typeId;
        private int owner;
        private int type;
        private Long paymentFilter;
        private List<Integer> groups=new ArrayList<Integer>();
        private Long member;
        private String begin;
        private String end;
        private String description;

        public boolean isAdvanced() {
            return advanced;
        }

        public void setAdvanced(boolean advanced) {
            this.advanced = advanced;
        }

        public Long getMemberId() {
            return memberId;
        }

        public void setMemberId(Long memberId) {
            this.memberId = memberId;
        }

        public Long getTypeId() {
            return typeId;
        }

        public void setTypeId(Long typeId) {
            this.typeId = typeId;
        }

        public int getOwner() {
            return owner;
        }

        public void setOwner(int owner) {
            this.owner = owner;
        }

        public int getType() {
            return type;
        }

        public void setType(int type) {
            this.type = type;
        }

        public Long getPaymentFilter() {
            return paymentFilter;
        }

        public void setPaymentFilter(Long paymentFilter) {
            this.paymentFilter = paymentFilter;
        }

        public List<Integer> getGroups() {
            return groups;
        }

        public void setGroups(List<Integer> groups) {
            this.groups = groups;
        }

        public Long getMember() {
            return member;
        }

        public void setMember(Long member) {
            this.member = member;
        }

        public String getBegin() {
            return begin;
        }

        public void setBegin(String begin) {
            this.begin = begin;
        }

        public String getEnd() {
            return end;
        }

        public void setEnd(String end) {
            this.end = end;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

    }

//    public static class AccountHistoryResponse {
//
//        private List<Transfer> transfers;
//        private EnumSet<Status> paymentStatus;
//        private List<? extends Element> operators;
//        private List<? extends Group> memberGroups;
//        private List<GroupFilter> groupFilters;
//        private BigDecimal creditLimit;
//        private AccountOwner owner;
//        private AccountType type;
//        private List<PaymentFilter> paymentFilters;
//        private boolean myAccount;
//        private AccountStatus status;
//        private String unitsPattern;
//        private Account account;
//        private boolean showConciliated;
//        private List<PaymentCustomField> customFieldsForList;
//
//        public List<? extends Group> getMemberGroups() {
//            return memberGroups;
//        }
//
//        public void setMemberGroups(List<? extends Group> memberGroups) {
//            this.memberGroups = memberGroups;
//        }
//
//        public List<PaymentCustomField> getCustomFieldsForList() {
//            return customFieldsForList;
//        }
//
//        public void setCustomFieldsForList(List<PaymentCustomField> customFieldsForList) {
//            this.customFieldsForList = customFieldsForList;
//        }
//
//        public boolean isShowConciliated() {
//            return showConciliated;
//        }
//
//        public void setShowConciliated(boolean showConciliated) {
//            this.showConciliated = showConciliated;
//        }
//
//        public EnumSet<Status> getPaymentStatus() {
//            return paymentStatus;
//        }
//
//        public void setPaymentStatus(EnumSet<Status> paymentStatus) {
//            this.paymentStatus = paymentStatus;
//        }
//
//        public List<? extends Element> getOperators() {
//            return operators;
//        }
//
//        public void setOperators(List<? extends Element> operators) {
//            this.operators = operators;
//        }
//
//        public List<GroupFilter> getGroupFilters() {
//            return groupFilters;
//        }
//
//        public void setGroupFilters(List<GroupFilter> groupFilters) {
//            this.groupFilters = groupFilters;
//        }
//
//        public BigDecimal getCreditLimit() {
//            return creditLimit;
//        }
//
//        public void setCreditLimit(BigDecimal creditLimit) {
//            this.creditLimit = creditLimit;
//        }
//
//        public AccountOwner getOwner() {
//            return owner;
//        }
//
//        public void setOwner(AccountOwner owner) {
//            this.owner = owner;
//        }
//
//        public AccountType getType() {
//            return type;
//        }
//
//        public void setType(AccountType type) {
//            this.type = type;
//        }
//
//        public List<PaymentFilter> getPaymentFilters() {
//            return paymentFilters;
//        }
//
//        public void setPaymentFilters(List<PaymentFilter> paymentFilters) {
//            this.paymentFilters = paymentFilters;
//        }
//
//        public boolean isMyAccount() {
//            return myAccount;
//        }
//
//        public void setMyAccount(boolean myAccount) {
//            this.myAccount = myAccount;
//        }
//
//        public AccountStatus getStatus() {
//            return status;
//        }
//
//        public void setStatus(AccountStatus status) {
//            this.status = status;
//        }
//
//        public String getUnitsPattern() {
//            return unitsPattern;
//        }
//
//        public void setUnitsPattern(String unitsPattern) {
//            this.unitsPattern = unitsPattern;
//        }
//
//        public Account getAccount() {
//            return account;
//        }
//
//        public void setAccount(Account account) {
//            this.account = account;
//        }
//
//        public List<Transfer> getTransfers() {
//            return transfers;
//        }
//
//        public void setTransfers(List<Transfer> transfers) {
//            this.transfers = transfers;
//        }
//
//    }
    
    public static class AdminAccountHistoryResponse extends GenericResponse{
        private List<Transfer> transfers =new ArrayList<Transfer>();

        public List<Transfer> getTransfers() {
            return transfers;
        }

        public void setTransfers(List<Transfer> transfers) {
            this.transfers = transfers;
        }
        
        
    }

    @RequestMapping(value = "admin/accountHistory", method = RequestMethod.POST)
    @ResponseBody
    public GenericResponse searchAccount(@RequestBody AccountHistoryRequest request) {
//        final HttpServletRequest request = context.getRequest();
        Map<String, Object> queryParameter=new HashMap<String, Object>();
        queryParameter.put("owner",request.getOwner());
        queryParameter.put("type",request.getType());
        queryParameter.put("paymentFilter",request.getPaymentFilter());
        queryParameter.put("member",request.getMember());
        queryParameter.put("description",request.getDescription());
        queryParameter.put("period.begin",request.getBegin());
        queryParameter.put("period.end",request.getEnd());
         queryParameter.put("groups",request.getGroups());
        
        
         final TransferQuery query = getDataBinder().readFromString(queryParameter);
         
      //  final TransferQuery query = (TransferQuery) queryParameters;
        AdminAccountHistoryResponse response = new AdminAccountHistoryResponse();
       // final Account account = accountService.getAccount(new AccountDTO(owner, type));
        final List<Transfer> transfers = paymentService.search(query);
        response.setTransfers(transfers);
        response.setStatus(0);
        response.setMessage("tranfer list!!");
        return response;
//        request.setAttribute("accountHistory", Entry.build(permissionService, elementService, account, transfers, fetchMember()));
    }

//    @RequestMapping(value = "admin/accountHistory", method = RequestMethod.GET)
//    @ResponseBody
//    public AccountHistoryResponse prepareForm() {
//        AccountHistoryResponse response = new AccountHistoryResponse();
//
//        final LocalSettings localSettings = settingsService.getLocalSettings();
//
//        // Set the owner and the account type on the first request
//        boolean firstTime = false;
////        if (RequestHelper.isGet(request)) {
////            form.setQuery("owner", form.getMemberId());
////            form.setQuery("type", form.getTypeId());
////            firstTime = true;
////        }
//
//        // Retrieve the query parameters
//        final TransferQuery query = new TransferQuery();
//
//        query.fetch(Payment.Relationships.CUSTOM_VALUES, Payment.Relationships.FROM, Payment.Relationships.TO, Payment.Relationships.TYPE);
//        query.setReverseOrder(true);
//
//        // Fetch the account type, and add relationship externalAccounts
//        final AccountType type = accountTypeService.load(query.getType().getId());
//
//        // Set the default status to PROCESSED
//        if (query.getStatus() == null) {
//            query.setStatus(Transfer.Status.PROCESSED);
//            // form.setQuery("status", query.getStatus().name());
//        }
//        if (firstTime) {
//            if (type instanceof SystemAccountType) {
//                // Ensure the initial period filter will start from the 1st day of the previous month, to avoid potentially huge DB scans
//                final Period lastMonthPeriod = TimePeriod.ONE_MONTH.previousPeriod(Calendar.getInstance());
//                query.setPeriod(Period.begginingAt(lastMonthPeriod.getBegin()));
//                final String formattedDate = localSettings.getDateConverter().toString(lastMonthPeriod.getBegin());
//                //PropertyHelper.set(form.getQuery("period"), "begin", formattedDate);
//            }
//        }
//
//        // Fetch the owner if is a member
//        AccountOwner owner = query.getOwner();
//        if (owner == null) {
//            owner = SystemAccountOwner.instance();
//        } else if (owner instanceof EntityReference) {
//            owner = (AccountOwner) elementService.load(((Member) owner).getId());
//        }
//
//        // Check if authorization status will be shown
//        boolean showStatus = false;
//        if (owner instanceof SystemAccountOwner) {
//            showStatus = permissionService.hasPermission(AdminSystemPermission.ACCOUNTS_AUTHORIZED_INFORMATION);
//        } else if (LoggedUser.isAdministrator()) {
//            showStatus = permissionService.hasPermission(AdminMemberPermission.ACCOUNTS_AUTHORIZED_INFORMATION);
//        } else if (LoggedUser.accountOwner().equals(owner)) {
//            showStatus = permissionService.hasPermission(MemberPermission.ACCOUNT_AUTHORIZED_INFORMATION);
//        } else if (owner instanceof Member && LoggedUser.isBroker()) {
//            showStatus = permissionService.hasPermission(BrokerPermission.ACCOUNTS_AUTHORIZED_INFORMATION);
//        }
//        if (showStatus) {
//            response.setPaymentStatus(EnumSet.of(Transfer.Status.PROCESSED, Transfer.Status.PENDING, Transfer.Status.DENIED, Transfer.Status.CANCELED));
//        }
//
//        // Retrieve the account
//        final Account account = accountService.getAccount(new AccountDTO(owner, type));
//
//        // Fetch the member on filter
//        if (query.getMember() instanceof EntityReference) {
//            query.setMember((Member) elementService.load(query.getMember().getId(), Element.Relationships.USER));
//        }
//
//        // When a member, get it's operators
//        final Member loggedMember = LoggedUser.member();
//        if (loggedMember != null) {
//            final OperatorQuery oq = new OperatorQuery();
//            oq.setMember(loggedMember);
//            final List<? extends Element> operators = elementService.search(oq);
//            response.setOperators(operators);
//        }
//
//        // When a system account, get groups / group filters
//        if (type instanceof SystemAccountType) {
//            final AdminGroup adminGroup = LoggedUser.group();
//
//            final GroupQuery groups = new GroupQuery();
//            groups.setManagedBy(adminGroup);
//            groups.setNatures(Group.Nature.MEMBER, Group.Nature.BROKER);
//            groups.setStatus(Group.Status.NORMAL);
//            response.setMemberGroups(groupService.search(groups));
//
//            final GroupFilterQuery groupFilters = new GroupFilterQuery();
//            groupFilters.setAdminGroup(adminGroup);
//            response.setGroupFilters(groupFilterService.search(groupFilters));
//        }
//
//        // Get the account status
//        final AccountStatus status = accountService.getRatedStatus(account, null);
//
//        // Get the credit limit
//        final BigDecimal min = paymentService.getMinimumPayment();
//        final GetTransactionsDTO params = new GetTransactionsDTO(query.getOwner(), query.getType());
//        final BigDecimal creditLimit = accountService.getCreditLimit(params);
//        // Don't show if zero
//        if (creditLimit != null && creditLimit.abs().compareTo(min) == 1) {
//            response.setCreditLimit(creditLimit.negate());
//        }
//
//        // Retrieve the payment filters
//        final PaymentFilterQuery pfQuery = new PaymentFilterQuery();
//        pfQuery.setAccountType(query.getType());
//        pfQuery.setContext(PaymentFilterQuery.Context.ACCOUNT_HISTORY);
//        pfQuery.setElement(owner instanceof SystemAccountOwner ? LoggedUser.element(): (Member) owner);
//        final List<PaymentFilter> paymentFilters = paymentFilterService.search(pfQuery);
//
//        // Set the required request attributes
//        response.setOwner(owner instanceof SystemAccountOwner ? null : owner);
//        response.setType(type);
//        response.setPaymentFilters(paymentFilters);
//        response.setMyAccount(LoggedUser.accountOwner().equals(owner));
//        response.setStatus(status);
//        response.setUnitsPattern(type.getCurrency().getPattern());
//        response.setAccount(account);
//
//        if (type instanceof SystemAccountType) {
//            final SystemAccountType systemType = (SystemAccountType) type;
//            response.setShowConciliated(!systemType.getExternalAccounts().isEmpty());
//        } else {
//            response.setShowConciliated(Boolean.FALSE);
//        }
//
//        // Get the custom fields
//        final List<PaymentCustomField> customFieldsForSearch = paymentCustomFieldService.listForSearch(account, false);
//        final List<PaymentCustomField> customFieldsForList = paymentCustomFieldService.listForList(account, false);
//        // request.setAttribute("customFieldsForSearch", customFieldHelper.buildEntries(customFieldsForSearch, query.getCustomValues()));
//        response.setCustomFieldsForList(customFieldsForList);
//
//        // Determine where to go back
////        String backTo = null;
////        if (type instanceof SystemAccountType) {
////            if (!form.isSingleAccount()) {
////                // On system accounts, only go back when there's an account overview
////                backTo = context.getPathPrefix() + "/accountOverview";
////            }
////        } else {
////            // A member account, go back to either overview or profile
////            final Member member = (Member) owner;
////            if (form.isSingleAccount()) {
////                backTo = context.getPathPrefix() + "/profile?memberId=" + member.getId();
////            } else {
////                backTo = context.getPathPrefix() + "/accountOverview?memberId=" + member.getId();
////            }
////        }
////        request.setAttribute("backTo", backTo);
//
//        return response;
//    }

}
