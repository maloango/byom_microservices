package nl.strohalm.cyclos.webservices.rest.accounts.details;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import nl.strohalm.cyclos.controls.ActionContext;
import nl.strohalm.cyclos.entities.accounts.Account;
import nl.strohalm.cyclos.entities.accounts.AccountOwner;
import nl.strohalm.cyclos.entities.accounts.AccountType;
import nl.strohalm.cyclos.entities.accounts.MemberAccount;
import nl.strohalm.cyclos.entities.accounts.SystemAccountOwner;
import nl.strohalm.cyclos.entities.accounts.SystemAccountType;
import nl.strohalm.cyclos.entities.accounts.transactions.Payment;
import nl.strohalm.cyclos.entities.accounts.transactions.ScheduledPayment;
import nl.strohalm.cyclos.entities.accounts.transactions.ScheduledPaymentQuery;
import nl.strohalm.cyclos.entities.members.Element;
import nl.strohalm.cyclos.entities.members.Member;
import nl.strohalm.cyclos.entities.settings.LocalSettings;
import nl.strohalm.cyclos.services.accounts.MemberAccountTypeQuery;
import nl.strohalm.cyclos.services.accounts.SystemAccountTypeQuery;
import nl.strohalm.cyclos.utils.Period;
import nl.strohalm.cyclos.utils.RelationshipHelper;
import nl.strohalm.cyclos.utils.RequestHelper;
import nl.strohalm.cyclos.utils.access.LoggedUser;
import nl.strohalm.cyclos.utils.query.QueryParameters;
import org.springframework.stereotype.Controller;

import nl.strohalm.cyclos.webservices.rest.BaseRestController;
import nl.strohalm.cyclos.webservices.rest.GenericResponse;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RequestBody;

@Controller
public class SearchScheduledPaymentsController extends BaseRestController {

    public static class ScheduluedPaymentParameters {

        private Long memberId;
        private String statusGroup;
        private Long member;
        private String begin;
        private String end;
        private Long accountType;
        private String principal;

        public String getPrincipal() {
            return principal;
        }

        public void setPrincipal(String principal) {
            this.principal = principal;
        }

        public Long getAccountType() {
            return accountType;
        }

        public void setAccountType(Long accountType) {
            this.accountType = accountType;
        }

        public Long getMemberId() {
            return memberId;
        }

        public void setMemberId(Long memberId) {
            this.memberId = memberId;
        }

        public String getStatusGroup() {
            return statusGroup;
        }

        public void setStatusGroup(String statusGroup) {
            this.statusGroup = statusGroup;
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

    }

    public class SearchScheduledPaymentResponse extends GenericResponse {

        private List<AccountTypeEntity> accountTypesList;

        Set<Entry<String, ScheduledPaymentQuery.SearchType>> sType;
        Set<Entry<String, ScheduledPaymentQuery.StatusGroup>> sGroups;

        public Set<Entry<String, ScheduledPaymentQuery.SearchType>> getsType() {
            return sType;
        }

        public void setsType(Set<Entry<String, ScheduledPaymentQuery.SearchType>> sType) {
            this.sType = sType;
        }

        public Set<Entry<String, ScheduledPaymentQuery.StatusGroup>> getsGroups() {
            return sGroups;
        }

        public void setsGroups(Set<Entry<String, ScheduledPaymentQuery.StatusGroup>> sGroups) {
            this.sGroups = sGroups;
        }

        public List<AccountTypeEntity> getAccountTypesList() {
            return accountTypesList;
        }

        public void setAccountTypesList(List<AccountTypeEntity> accountTypesList) {
            this.accountTypesList = accountTypesList;
        }

    }

    public static class AccountTypeEntity {

        private Long id;
        private String name;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

    }

    public static class ScheduledPaymentResponse extends GenericResponse {

        private List<ScheduledPayment> payments;

        public List<ScheduledPayment> getPayments() {
            return payments;
        }

        public void setPayments(List<ScheduledPayment> payments) {
            this.payments = payments;
        }

    }

    @RequestMapping(value = "admin/searchScheduledPayment", method = RequestMethod.GET)
    @ResponseBody
    public SearchScheduledPaymentResponse prepareForm() {
//        final ScheduledPaymentQuery query = getDataBinder().readFromString(form.getQuery());
//        query.fetch(ScheduledPayment.Relationships.TRANSFERS, RelationshipHelper.nested(Payment.Relationships.FROM, MemberAccount.Relationships.MEMBER), RelationshipHelper.nested(Payment.Relationships.TO, MemberAccount.Relationships.MEMBER));
//
//        // Account owner
//        AccountOwner owner = null;
//        if (LoggedUser.user().getId() > 0) {
//            owner = (Member) elementService.load(form.getMemberId());
//            request.setAttribute("memberId", form.getMemberId());
//        } else {
//            // An admin or member or an operator searching his own scheduled payments
//            owner = context.getAccountOwner();
//        }
//        query.setOwner(owner);
        SearchScheduledPaymentResponse response = new SearchScheduledPaymentResponse();
        AccountOwner owner = LoggedUser.accountOwner();

        List<? extends AccountType> accountTypes;
        if (LoggedUser.isAdministrator() && owner instanceof SystemAccountOwner) {
            final SystemAccountTypeQuery satq = new SystemAccountTypeQuery();
            accountTypes = accountTypeService.search(satq);
        } else {
            final MemberAccountTypeQuery matq = new MemberAccountTypeQuery();
            matq.setOwner((Member) owner);
            accountTypes = accountTypeService.search(matq);
        }

        List<AccountTypeEntity> accountTypesList = new ArrayList();
        for (AccountType account : accountTypes) {
            AccountTypeEntity accountEntity = new AccountTypeEntity();
            accountEntity.setId(account.getId());
            accountEntity.setName(account.getName());
            accountTypesList.add(accountEntity);
        }
        response.setAccountTypesList(accountTypesList);
//        if (query.getMember() != null) {
//            final Member member = elementService.load(query.getMember().getId(), Element.Relationships.USER);
//            query.setMember(member);
//        }
//        if (query.getStatusList() == null) {
//            query.setStatusGroup(ScheduledPaymentQuery.StatusGroup.OPEN);
//            form.setQuery("statusGroup", ScheduledPaymentQuery.StatusGroup.OPEN);
//        }
//        RequestHelper.storeEnum(request, ScheduledPaymentQuery.SearchType.class, "searchTypes");
//        RequestHelper.storeEnum(request, ScheduledPaymentQuery.StatusGroup.class, "statusGroups");
        Map<String, ScheduledPaymentQuery.SearchType> searchTypes = new HashMap();
        searchTypes.put("Outgoing", ScheduledPaymentQuery.SearchType.OUTGOING);
        searchTypes.put("Incoming", ScheduledPaymentQuery.SearchType.INCOMING);

        Set<Entry<String, ScheduledPaymentQuery.SearchType>> sType = searchTypes.entrySet();

        Map<String, ScheduledPaymentQuery.StatusGroup> statusGroups = new HashMap();
        statusGroups.put("open", ScheduledPaymentQuery.StatusGroup.OPEN);
        statusGroups.put("closed(entirely paid)", ScheduledPaymentQuery.StatusGroup.CLOSED_WITHOUT_ERRORS);
        statusGroups.put("closed(partialy paid)", ScheduledPaymentQuery.StatusGroup.CLOSED_WITH_ERRORS);

        Set<Entry<String, ScheduledPaymentQuery.StatusGroup>> sGroups = statusGroups.entrySet();

        response.setsType(sType);
        response.setsGroups(sGroups);
//        response.setOwner(owner);
        response.setStatus(0);
        response.setMessage("Scheduled payment data");
        return response;
    }

    @RequestMapping(value = "admin/searchScheduledPayment", method = RequestMethod.POST)
    @ResponseBody
    public ScheduledPaymentResponse searchScheduledPayment(@RequestBody ScheduluedPaymentParameters params) {
        ScheduledPaymentResponse response = new ScheduledPaymentResponse();
        final LocalSettings localSettings = settingsService.getLocalSettings();
//        Map<String, Object> query = new HashMap();
//
//        query.put("accountType", accountTypeService.load(params.getAccountType()));
//        query.put("statusGroup", ScheduledPaymentQuery.StatusGroup.valueOf(params.getStatusGroup()));
        Period period = new Period();
        period.setBegin(localSettings.getDateConverter().valueOf(params.getBegin()));
        period.setEnd(localSettings.getDateConverter().valueOf(params.getEnd()));
//        query.put("period", localSettings);
//        query.put("member", memberService.loadByIdOrPrincipal(params.getMember(), "admin", "1234"));
        final ScheduledPaymentQuery paymentQuery = new ScheduledPaymentQuery();
        if (params.getAccountType() != null && params.getAccountType() > 0L) {
            paymentQuery.setAccountType(accountTypeService.load(params.getAccountType()));
        }
        paymentQuery.setPeriod(period);
        if (params.getStatusGroup() != null) {
            paymentQuery.setStatusGroup(ScheduledPaymentQuery.StatusGroup.valueOf(params.getStatusGroup()));
        }
        else{
           paymentQuery.setStatusGroup(ScheduledPaymentQuery.StatusGroup.OPEN); 
        }
        if (params.getMember() != null && params.getMember() > 0L) {
            paymentQuery.setMember((Member) elementService.load(params.getMember(),Element.Relationships.USER));
        }
        paymentQuery.setSearchType(ScheduledPaymentQuery.SearchType.OUTGOING);
        paymentQuery.setOwner(LoggedUser.accountOwner());

        List<ScheduledPayment> payments = null;
        if (LoggedUser.isAdministrator()) {
//            query.setSearchType(ScheduledPaymentQuery.SearchType.OUTGOING);
        }
        payments = scheduledPaymentService.search(paymentQuery);
        System.out.println("-----" + payments);
        response.setPayments(payments);
        response.setStatus(0);

        return response;

    }

}
