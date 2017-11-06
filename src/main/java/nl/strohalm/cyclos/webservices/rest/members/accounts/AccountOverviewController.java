/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.strohalm.cyclos.webservices.rest.members.accounts;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import nl.strohalm.cyclos.annotations.Inject;
import nl.strohalm.cyclos.entities.accounts.Account;
import nl.strohalm.cyclos.entities.accounts.AccountLimitLog;
import nl.strohalm.cyclos.entities.accounts.AccountOwner;
import nl.strohalm.cyclos.entities.accounts.AccountType;
import nl.strohalm.cyclos.entities.accounts.SystemAccountOwner;
import nl.strohalm.cyclos.entities.accounts.transactions.Payment;
import nl.strohalm.cyclos.entities.accounts.transactions.PaymentFilter;
import nl.strohalm.cyclos.entities.accounts.transactions.PaymentFilterQuery;
import nl.strohalm.cyclos.entities.accounts.transactions.TransferQuery;
import nl.strohalm.cyclos.entities.exceptions.EntityNotFoundException;
import nl.strohalm.cyclos.entities.members.Element;
import nl.strohalm.cyclos.entities.members.Member;
import nl.strohalm.cyclos.services.accounts.AccountDateDTO;
import nl.strohalm.cyclos.services.accounts.AccountService;
import nl.strohalm.cyclos.services.accounts.AccountTypeService;
import nl.strohalm.cyclos.services.elements.ElementService;
import nl.strohalm.cyclos.services.transfertypes.PaymentFilterService;
import nl.strohalm.cyclos.utils.Navigation;
import nl.strohalm.cyclos.utils.Period;
import nl.strohalm.cyclos.utils.RelationshipHelper;
import nl.strohalm.cyclos.utils.access.LoggedUser;
import nl.strohalm.cyclos.utils.validation.ValidationException;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;
import nl.strohalm.cyclos.webservices.rest.GenericResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 *
 * @author Lue
 */
@Controller
public class AccountOverviewController extends BaseRestController {

    private AccountService accountService;
    private AccountTypeService accountTypeService;
    private ElementService elementService;
    private PaymentFilterService paymentFilterService;

    public void setElementService(ElementService elementService) {
        this.elementService = elementService;
    }

    public void setPaymentFilterService(PaymentFilterService paymentFilterService) {
        this.paymentFilterService = paymentFilterService;
    }

    public AccountService getAccountService() {
        return accountService;
    }

    public AccountTypeService getAccountTypeService() {
        return accountTypeService;
    }

    @Inject
    public void setAccountService(final AccountService accountService) {
        this.accountService = accountService;
    }

    @Inject
    public void setAccountTypeService(final AccountTypeService accountTypeService) {
        this.accountTypeService = accountTypeService;
    }

    public static class AccountOverviewResponse extends GenericResponse {

        private Member member;
        private boolean myAccounts;
        private boolean byBroker;
        private List<AccountDetails> overview;
        private List<PaymentEntity> paymentList = new ArrayList();
        private Long paymentFilter;
        private Long typeId;
        Set<Entry<Payment.Status, String>>statusSet;
        
        public Set<Entry<Payment.Status, String>> getStatusSet() {
            return statusSet;
        }

        public void setStatusSet(Set<Entry<Payment.Status, String>> statusSet) {
            this.statusSet = statusSet;
        }
        
        private String description;

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public Long getTypeId() {
            return typeId;
        }

        public void setTypeId(Long typeId) {
            this.typeId = typeId;
        }

        

        public Long getPaymentFilter() {
            return paymentFilter;
        }

        public void setPaymentFilter(Long paymentFilter) {
            this.paymentFilter = paymentFilter;
        }

        public List<PaymentEntity> getPaymentList() {
            return paymentList;
        }

        public void setPaymentList(List<PaymentEntity> paymentList) {
            this.paymentList = paymentList;
        }

        public Member getMember() {
            return member;
        }

        public void setMember(Member member) {
            this.member = member;
        }

        public boolean isMyAccounts() {
            return myAccounts;
        }

        public void setMyAccounts(boolean myAccounts) {
            this.myAccounts = myAccounts;
        }

        public boolean isByBroker() {
            return byBroker;
        }

        public void setByBroker(boolean byBroker) {
            this.byBroker = byBroker;
        }

        public List<AccountDetails> getOverview() {
            return overview;
        }

        public void setOverview(List<AccountDetails> overview) {
            this.overview = overview;
        }

    }

    @RequestMapping(value = "/member/accountOverView", method = RequestMethod.GET)
    @ResponseBody
    protected AccountOverviewResponse executeAction() throws Exception {
        AccountOverviewResponse response = new AccountOverviewResponse();
        final long memberId = LoggedUser.user().getId();
        AccountOwner owner;
        Member member = null;
        boolean myAccounts = false;
        boolean byBroker = false;

        final Element loggedElement = LoggedUser.element();
        // Resolve the account owner we will use
        if (memberId <= 0L || memberId == loggedElement.getId()) {
            owner = loggedElement.getAccountOwner();
            myAccounts = true;
            if (LoggedUser.isMember()) {
                member = LoggedUser.element();
            }
        } else {
            final Element element = elementService.load(memberId, Element.Relationships.USER);
            if (!(element instanceof Member)) {
                throw new ValidationException();
            }
            member = (Member) element;
            owner = member;
            if (LoggedUser.isMember()) {
                if (!LoggedUser.isBroker()) {
                    throw new ValidationException();
                }
                byBroker = true;
            }
        }
        // Get the paymentList member
        final PaymentFilterQuery pfQuery = new PaymentFilterQuery();
        //  pfQuery.setAccountType(query.getType());
        // pfQuery.setContext(PaymentFilterQuery.Context.ACCOUNT_HISTORY);
        pfQuery.setElement(owner instanceof SystemAccountOwner ? LoggedUser.element() : (Member) owner);
        final List<PaymentFilter> paymentFilters = paymentFilterService.search(pfQuery);
        List<PaymentEntity> payList = new ArrayList();
        for (PaymentFilter list : paymentFilters) {
            PaymentEntity paymentDetails = new PaymentEntity();
            paymentDetails.setId(list.getId());
            paymentDetails.setName(list.getName());
            payList.add(paymentDetails);

        }

        // get status type 
        Map<Payment.Status, String> status1 = new HashMap();
        status1.put(Payment.Status.DENIED, "DENIED");
        status1.put(Payment.Status.CANCELED, "CANCELED");
        status1.put(Payment.Status.PENDING, "PENDING");
        status1.put(Payment.Status.PROCESSED, "PROCESSED");
        Set<Entry<Payment.Status, String>>statusSet=status1.entrySet();
        response.setStatusSet(statusSet);

        // Get the account types of the owner
        final List<? extends Account> accounts = accountService.getAccounts(owner, RelationshipHelper.nested(Account.Relationships.TYPE, AccountType.Relationships.CURRENCY));

        if (accounts.isEmpty()) {
            // No accounts = error
            response.setMessage("accountOverview.error.noAccounts");
        } else if (accounts.size() == 1) {
            // Single account = redirect to the account details

            // Remove the overview from the navigation
            //LoggedUser.getNavigation().removeCurrent();
            // Redirect
            final Account account = accounts.get(0);
            final AccountType accountType = account.getType();
            final Map<String, Object> params = new HashMap<String, Object>();
            params.put("memberId", memberId);
            params.put("typeId", accountType.getId());
            params.put("singleAccount", true);
            params.put("fromQuickAccess", LoggedUser.AccessType.SYSTEM);
            //return ActionHelper.redirectWithParams(context.getRequest(), context.findForward("accountDetails"), params);
        }

        response.setStatus(0);
        response.setMessage("account overview list!!");
        // response.setMember(member);
        response.setMyAccounts(myAccounts);
        response.setByBroker(byBroker);
        response.setOverview(resolveAccountOverview(accounts));
        response.setPaymentList(payList);

        return response;
    }

    private List<AccountDetails> resolveAccountOverview(final List<? extends Account> accounts) {
        final List<AccountDetails> overview = new ArrayList<AccountDetails>();
        for (final Account account : accounts) {
            try {
                final BigDecimal balance = accountService.getBalance(new AccountDateDTO(account));

                //  overview.put(account, balance);
                Account ac = accountService.load(new Long(account.getId()));
                AccountDetails ad = new AccountDetails();
                ad.setId(ac.getId());
                ad.setType(ac.getType().getName());
                ad.setCurrency(ac.getType().getCurrency().getSymbol());
                ad.setBalance(ac.getCreditLimit());
                ad.setOwnerName(ac.getOwnerName());
                ad.setCreationDate(ac.getCreationDate());
                ad.setLastClosingDate(ac.getLastClosingDate());
                ad.setDescription(ad.getDescription());
                ad.setLimitLogs(ad.getLimitLogs());

                overview.add(ad);
            } catch (final EntityNotFoundException e) {
                // Ignore this account
            }
        }
        return overview;
    }

    public static class AccountDetails {

        public long id;
        private String type;
        private BigDecimal balance;
        private String currency;
        private String description;
        private Calendar creationDate;
        private Calendar lastClosingDate;
        private Collection<AccountLimitLog> limitLogs;

        public Collection<AccountLimitLog> getLimitLogs() {
            return limitLogs;
        }

        public void setLimitLogs(Collection<AccountLimitLog> limitLogs) {
            this.limitLogs = limitLogs;
        }

        public Calendar getCreationDate() {
            return creationDate;
        }

        public void setCreationDate(Calendar creationDate) {
            this.creationDate = creationDate;
        }

        public Calendar getLastClosingDate() {
            return lastClosingDate;
        }

        public void setLastClosingDate(Calendar lastClosingDate) {
            this.lastClosingDate = lastClosingDate;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        private String ownerName;

        public String getOwnerName() {
            return ownerName;
        }

        public void setOwnerName(String ownerName) {
            this.ownerName = ownerName;
        }

        public long getId() {
            return id;
        }

        public void setId(long id) {
            this.id = id;
        }

        public BigDecimal getBalance() {
            return balance;
        }

        public void setBalance(BigDecimal balance) {
            this.balance = balance;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getCurrency() {
            return currency;
        }

        public void setCurrency(String currency) {
            this.currency = currency;
        }

    }

    public static class PaymentEntity {

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

}
