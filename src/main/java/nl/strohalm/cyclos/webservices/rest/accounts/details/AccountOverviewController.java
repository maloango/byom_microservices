package nl.strohalm.cyclos.webservices.rest.accounts.details;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import nl.strohalm.cyclos.annotations.Inject;
import nl.strohalm.cyclos.entities.accounts.Account;
import nl.strohalm.cyclos.entities.accounts.AccountOwner;
import nl.strohalm.cyclos.entities.accounts.AccountType;
import nl.strohalm.cyclos.entities.exceptions.EntityNotFoundException;
import nl.strohalm.cyclos.entities.members.Element;
import nl.strohalm.cyclos.entities.members.Member;
import nl.strohalm.cyclos.services.accounts.AccountDateDTO;
import nl.strohalm.cyclos.services.accounts.AccountService;
import nl.strohalm.cyclos.services.accounts.AccountTypeService;
import nl.strohalm.cyclos.services.elements.ElementService;
import nl.strohalm.cyclos.utils.ActionHelper;
import nl.strohalm.cyclos.utils.RelationshipHelper;
import nl.strohalm.cyclos.utils.access.LoggedUser;
import nl.strohalm.cyclos.utils.validation.ValidationException;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;
import nl.strohalm.cyclos.webservices.rest.GenericResponse;

@Controller
public class AccountOverviewController extends BaseRestController {

    private AccountService accountService;
    private AccountTypeService accountTypeService;
    private ElementService elementService;

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

    public static class AccountOverViewResponse extends GenericResponse {
        
      // private Account account;
        private Member member;
        private boolean myAccounts;
        private boolean byBroker;
        private List<AccountDetails> overview;

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

    @RequestMapping(value = "/admin/accountOverView", method = RequestMethod.GET)
    @ResponseBody
    protected AccountOverViewResponse executeAction() throws Exception {
        AccountOverViewResponse response = new AccountOverViewResponse();
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
                if(!LoggedUser.isBroker()) {
                    throw new ValidationException();
                }
                byBroker = true;
            }
        }

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
        response.setMember(member);
        response.setMyAccounts(myAccounts);
        response.setByBroker(byBroker);
        response.setOverview(resolveAccountOverview(accounts));

        return response;
    }

    private List<AccountDetails> resolveAccountOverview(final List<? extends Account> accounts) {
        final List<AccountDetails> overview = new ArrayList<AccountDetails>();
        for (final Account account : accounts) {
            try {
                //final BigDecimal balance = accountService.getBalance(new AccountDateDTO(account));
               Account ac= accountService.load(new Long(account.getId()));
               AccountDetails ad=new AccountDetails();
               ad.setId(ac.getId());
               ad.setType(ac.getType().getName());
               ad.setCurrency(ac.getType().getCurrency().getSymbol());
               ad.setBalance(accountService.getBalance(new AccountDateDTO(account)));
                overview.add(ad);
            } catch (final EntityNotFoundException e) {
                // Ignore this account
            }
        }
        return overview;
    }
    
    public static class AccountDetails{
        public long id;
        private String type;
        private BigDecimal balance;
        private String currency;

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
}
