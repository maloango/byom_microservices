/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.strohalm.cyclos.webservices.rest.members.accounts;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import nl.strohalm.cyclos.annotations.Inject;
import nl.strohalm.cyclos.entities.accounts.AccountOwner;
import nl.strohalm.cyclos.entities.accounts.AccountType;
import nl.strohalm.cyclos.entities.accounts.MemberAccount;
import nl.strohalm.cyclos.entities.accounts.SystemAccountOwner;
import nl.strohalm.cyclos.entities.accounts.transactions.Payment;
import nl.strohalm.cyclos.entities.accounts.transactions.ScheduledPayment;
import nl.strohalm.cyclos.entities.accounts.transactions.ScheduledPaymentQuery;
import nl.strohalm.cyclos.entities.accounts.transactions.ScheduledPaymentQuery.SearchType;
import nl.strohalm.cyclos.entities.accounts.transactions.ScheduledPaymentQuery.StatusGroup;
import nl.strohalm.cyclos.entities.members.Element;
import nl.strohalm.cyclos.entities.members.Member;
import nl.strohalm.cyclos.entities.settings.LocalSettings;
import nl.strohalm.cyclos.services.accounts.AccountTypeService;
import nl.strohalm.cyclos.services.accounts.MemberAccountTypeQuery;
import nl.strohalm.cyclos.services.accounts.SystemAccountTypeQuery;
import nl.strohalm.cyclos.services.elements.ElementService;
import nl.strohalm.cyclos.services.settings.SettingsService;
import nl.strohalm.cyclos.services.transactions.ScheduledPaymentService;
import nl.strohalm.cyclos.utils.Period;
import nl.strohalm.cyclos.utils.RelationshipHelper;
import nl.strohalm.cyclos.utils.access.LoggedUser;
import nl.strohalm.cyclos.utils.binding.BeanBinder;
import nl.strohalm.cyclos.utils.binding.DataBinder;
import nl.strohalm.cyclos.utils.binding.DataBinderHelper;
import nl.strohalm.cyclos.utils.binding.PropertyBinder;
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
public class SearchScheduledPaymentsController extends BaseRestController {

    private DataBinder<ScheduledPaymentQuery> dataBinder;
    private AccountTypeService accountTypeService;
    private ScheduledPaymentService scheduledPaymentService;
    private ElementService elementService;
    private SettingsService settingsService;

    public ElementService getElementService() {
        return elementService;
    }

    public void setElementService(ElementService elementService) {
        this.elementService = elementService;
    }

    public SettingsService getSettingsService() {
        return settingsService;
    }

    public void setSettingsService(SettingsService settingsService) {
        this.settingsService = settingsService;
    }

    @Inject
    public void setAccountTypeService(final AccountTypeService accountTypeService) {
        this.accountTypeService = accountTypeService;
    }

    @Inject
    public void setScheduledPaymentService(final ScheduledPaymentService scheduledPaymentService) {
        this.scheduledPaymentService = scheduledPaymentService;
    }

    public static class SearchScheduledPaymentsRequest {

        private String begin;
        private String end;
        private Long memberId;
        private AccountOwner owner;
        private AccountType accountType;
        private Member member;
        private Period period;
        private ScheduledPaymentQuery.StatusGroup statusGroup;
        private Collection<Payment.Status> statusList;
        private SearchType searchType;
        private List<ScheduledPayment> payments;

        public List<ScheduledPayment> getPayments() {
            return payments;
        }

        public void setPayments(List<ScheduledPayment> payments) {
            this.payments = payments;
        }

        public AccountOwner getOwner() {
            return owner;
        }

        public void setOwner(AccountOwner owner) {
            this.owner = owner;
        }

        public AccountType getAccountType() {
            return accountType;
        }

        public void setAccountType(AccountType accountType) {
            this.accountType = accountType;
        }

        public Member getMember() {
            return member;
        }

        public void setMember(Member member) {
            this.member = member;
        }

        public Period getPeriod() {
            return period;
        }

        public void setPeriod(Period period) {
            this.period = period;
        }

        public ScheduledPaymentQuery.StatusGroup getStatusGroup() {
            return statusGroup;
        }

        public void setStatusGroup(ScheduledPaymentQuery.StatusGroup statusGroup) {
            this.statusGroup = statusGroup;
        }

        public Collection<Payment.Status> getStatusList() {
            return statusList;
        }

        public void setStatusList(Collection<Payment.Status> statusList) {
            this.statusList = statusList;
        }

        public SearchType getSearchType() {
            return searchType;
        }

        public void setSearchType(SearchType searchType) {
            this.searchType = searchType;
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

        public Long getMemberId() {
            return memberId;
        }

        public void setMemberId(Long memberId) {
            this.memberId = memberId;
        }

    }

    public static class SearchScheduledPaymentsResponse extends GenericResponse {

        private List<ScheduledPayment> payments;
        private long memberId;
        private long adminId;
        private List<? extends AccountType> accountTypes;
        private AccountOwner owner;
        private List<StatusGroup> group = new ArrayList();
        private List<SearchType> searchType= new ArrayList();

        public List<SearchType> getSearchType() {
            return searchType;
        }

        public void setSearchType(List<SearchType> searchType) {
            this.searchType = searchType;
        }
        

        public List<StatusGroup> getGroup() {
            return group;
        }

        public void setGroup(List<StatusGroup> group) {
            this.group = group;
        }
        

        public AccountOwner getOwner() {
            return owner;
        }

        public void setOwner(AccountOwner owner) {
            this.owner = owner;
        }
        

        public List<? extends AccountType> getAccountTypes() {
            return accountTypes;
        }

        public void setAccountTypes(List<? extends AccountType> accountTypes) {
            this.accountTypes = accountTypes;
        }
        

        public long getMemberId() {
            return memberId;
        }

        public void setMemberId(long memberId) {
            this.memberId = memberId;
        }

        public long getAdminId() {
            return adminId;
        }

        public void setAdminId(long adminId) {
            this.adminId = adminId;
        }
        

        public List<ScheduledPayment> getPayments() {
            return payments;
        }

        public void setPayments(List<ScheduledPayment> payments) {
            this.payments = payments;
        }

    }

    @RequestMapping(value = "member/searchScheduledPayments", method = RequestMethod.POST)
    @ResponseBody
    public GenericResponse executeQuery(@RequestBody SearchScheduledPaymentsRequest request) {
        // final ScheduledPaymentQuery query = (ScheduledPaymentQuery) queryParameters;
        SearchScheduledPaymentsResponse response = new SearchScheduledPaymentsResponse();
        Map<String, Object> queryParameter = new HashMap<String, Object>();
        queryParameter.put("owner", request.getOwner());
        queryParameter.put("accountType", request.getAccountType());
        queryParameter.put("Period", request.getPeriod());
        queryParameter.put("searchType", request.getSearchType());
        queryParameter.put("statusList", request.getStatusList());
        queryParameter.put("memberId", request.getMemberId());
        queryParameter.put("statusGroup", request.getStatusGroup());
        queryParameter.put("member", request.getMember());
        queryParameter.put("begin", request.getBegin());
        queryParameter.put("end", request.getEnd());

        final ScheduledPaymentQuery query = getDataBinder().readFromString(queryParameter);
        List<ScheduledPayment> payments = null;
        if (LoggedUser.isAdministrator()) {
            query.setSearchType(SearchType.OUTGOING);
        }
        payments = scheduledPaymentService.search(query);
        //context.getRequest().setAttribute("payments", payments);
        response.setPayments(payments);
       
        response.setStatus(0);
        response.setMessage("!! SearchScheduledPayments list...");
        return response;

    }

    @RequestMapping(value = "member/searchScheduledPayments", method = RequestMethod.GET)
    @ResponseBody
    public SearchScheduledPaymentsResponse prepareForm() {
       SearchScheduledPaymentsResponse response = new SearchScheduledPaymentsResponse();
       Map<String, Object> form = new HashMap<String, Object>();
        final ScheduledPaymentQuery query = getDataBinder().readFromString(form);
        query.fetch(ScheduledPayment.Relationships.TRANSFERS, RelationshipHelper.nested(Payment.Relationships.FROM, MemberAccount.Relationships.MEMBER), RelationshipHelper.nested(Payment.Relationships.TO, MemberAccount.Relationships.MEMBER));

        // Account owner
        AccountOwner owner = null;
        if (LoggedUser.user().getId()> 0) {
            owner = (Member) elementService.load(LoggedUser.user().getId());
           // response.setAttribute("memberId", form.getMemberId());
           response.setMemberId(0);
        } else {
            // An admin or member or an operator searching his own scheduled payments
            owner = LoggedUser.accountOwner();
        }
        query.setOwner(owner);

        List<? extends AccountType> accountTypes;
        if (LoggedUser.isAdministrator() && owner instanceof SystemAccountOwner) {
            final SystemAccountTypeQuery satq = new SystemAccountTypeQuery();
            accountTypes = accountTypeService.search(satq);
        } else {
            final MemberAccountTypeQuery matq = new MemberAccountTypeQuery();
            matq.setOwner((Member) owner);
            accountTypes = accountTypeService.search(matq);
        }
       // response.setAccountTypes(accountTypes);

        if (query.getMember() != null) {
            final Member member = elementService.load(query.getMember().getId(), Element.Relationships.USER);
            query.setMember(member);
        }
        if (query.getStatusList() == null) {
            query.setStatusGroup(ScheduledPaymentQuery.StatusGroup.OPEN);
           // response.setStatusGroup("statusGroup",ScheduledPaymentQuery.StatusGroup.OPEN);
        }

       // RequestHelper.storeEnum(request, ScheduledPaymentQuery.SearchType.class, "searchTypes");
      //  RequestHelper.storeEnum(request, ScheduledPaymentQuery.StatusGroup.class, "statusGroups");
        List<StatusGroup> group = new ArrayList();
        group.add(StatusGroup.CLOSED_WITHOUT_ERRORS);
        group.add(StatusGroup.CLOSED_WITH_ERRORS);
        group.add(StatusGroup.OPEN);
        
        List<SearchType> searchType= new ArrayList();
        searchType.add(SearchType.INCOMING);
        searchType.add(SearchType.OUTGOING);
        response.setSearchType(searchType);
        
        response.setGroup(group);
      //  response.setOwner(owner);
        response.setStatus(0);
        response.setMessage("!! SearchScheduledPayments .....");
        return response;
    }

//    protected boolean willExecuteQuery(final ActionContext context, final QueryParameters queryParameters) throws Exception {
//        return true;
//    }
    private DataBinder<ScheduledPaymentQuery> getDataBinder() {
        if (dataBinder == null) {
            final BeanBinder<ScheduledPaymentQuery> binder = BeanBinder.instance(ScheduledPaymentQuery.class);
            final LocalSettings localSettings = settingsService.getLocalSettings();
            binder.registerBinder("accountType", PropertyBinder.instance(AccountType.class, "accountType"));
            binder.registerBinder("searchType", PropertyBinder.instance(ScheduledPaymentQuery.SearchType.class, "searchType"));
            binder.registerBinder("statusGroup", PropertyBinder.instance(ScheduledPaymentQuery.StatusGroup.class, "statusGroup"));
            binder.registerBinder("period", DataBinderHelper.periodBinder(localSettings, "period"));
            binder.registerBinder("member", PropertyBinder.instance(Member.class, "member"));
            binder.registerBinder("pageParameters", DataBinderHelper.pageBinder());
            dataBinder = binder;
        }
        return dataBinder;
    }

}
