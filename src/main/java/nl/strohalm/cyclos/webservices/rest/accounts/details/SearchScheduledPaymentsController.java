package nl.strohalm.cyclos.webservices.rest.accounts.details;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import nl.strohalm.cyclos.controls.ActionContext;
import nl.strohalm.cyclos.entities.accounts.AccountOwner;
import nl.strohalm.cyclos.entities.accounts.AccountType;
import nl.strohalm.cyclos.entities.accounts.MemberAccount;
import nl.strohalm.cyclos.entities.accounts.SystemAccountOwner;
import nl.strohalm.cyclos.entities.accounts.transactions.Payment;
import nl.strohalm.cyclos.entities.accounts.transactions.ScheduledPayment;
import nl.strohalm.cyclos.entities.accounts.transactions.ScheduledPaymentQuery;
import nl.strohalm.cyclos.entities.members.Element;
import nl.strohalm.cyclos.entities.members.Member;
import nl.strohalm.cyclos.services.accounts.MemberAccountTypeQuery;
import nl.strohalm.cyclos.services.accounts.SystemAccountTypeQuery;
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

@Controller
public class SearchScheduledPaymentsController extends BaseRestController {

    public class SearchScheduledPaymentResponse extends GenericResponse {

        private List<AccountTypeEntity> accountTypesList;
      
        private Map<String, ScheduledPaymentQuery.SearchType> searchTypes;
        private Map<String, ScheduledPaymentQuery.StatusGroup> statusGroups;

        public Map<String, ScheduledPaymentQuery.SearchType> getSearchTypes() {
            return searchTypes;
        }

        public void setSearchTypes(Map<String, ScheduledPaymentQuery.SearchType> searchTypes) {
            this.searchTypes = searchTypes;
        }

        public Map<String, ScheduledPaymentQuery.StatusGroup> getStatusGroups() {
            return statusGroups;
        }

        public void setStatusGroups(Map<String, ScheduledPaymentQuery.StatusGroup> statusGroups) {
            this.statusGroups = statusGroups;
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

        Map<String, ScheduledPaymentQuery.StatusGroup> statusGroups = new HashMap();
        statusGroups.put("open", ScheduledPaymentQuery.StatusGroup.OPEN);
        statusGroups.put("closed(entirely paid)", ScheduledPaymentQuery.StatusGroup.CLOSED_WITHOUT_ERRORS);
        statusGroups.put("closed(partialy paid)", ScheduledPaymentQuery.StatusGroup.CLOSED_WITH_ERRORS);

        response.setSearchTypes(searchTypes);
        response.setStatusGroups(statusGroups);
//        response.setOwner(owner);
        response.setStatus(0);
        response.setMessage("Scheduled payment data");
        return response;
    }

}
