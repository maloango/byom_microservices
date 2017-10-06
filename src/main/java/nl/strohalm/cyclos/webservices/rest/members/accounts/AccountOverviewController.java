/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.strohalm.cyclos.webservices.rest.members.accounts;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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
import nl.strohalm.cyclos.utils.Navigation;
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

        private boolean byBroker;
        private boolean myAccounts;
        private List<? extends Account> accounts;
        private Navigation navigation;
        private Map<Account, BigDecimal> overview;
        private boolean singleAccount;
        private Member member;

        public Member getMember() {
            return member;
        }

        public void setMember(Member member) {
            this.member = member;
        }

        public boolean isSingleAccount() {
            return singleAccount;
        }

        public void setSingleAccount(boolean singleAccount) {
            this.singleAccount = singleAccount;
        }

        public Map<Account, BigDecimal> getOverview() {
            return overview;
        }

        public void setOverview(Map<Account, BigDecimal> overview) {
            this.overview = overview;
        }

        public Navigation getNavigation() {
            return navigation;
        }

        public void setNavigation(Navigation navigation) {
            this.navigation = navigation;
        }

        public boolean isByBroker() {
            return byBroker;
        }

        public void setByBroker(boolean byBroker) {
            this.byBroker = byBroker;
        }

        public boolean isMyAccounts() {
            return myAccounts;
        }

        public void setMyAccounts(boolean myAccounts) {
            this.myAccounts = myAccounts;
        }

        public List<? extends Account> getAccounts() {
            return accounts;
        }

        public void setAccounts(List<? extends Account> accounts) {
            this.accounts = accounts;
        }

    }

    @RequestMapping(value = "member/accountOverview", method = RequestMethod.GET)
    @ResponseBody
    public AccountOverviewResponse accountOverview() throws Exception {
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

        // Get the account types of the owner
        final List<? extends Account> accounts = accountService.getAccounts(owner, RelationshipHelper.nested(Account.Relationships.TYPE, AccountType.Relationships.CURRENCY));

        if (accounts.isEmpty()) {
            // No accounts = error
            response.setMessage("accountOverview.error.noAccounts");
        } else if (accounts.size() == 1) {
            // Single account = redirect to the account details

            // Remove the overview from the navigation
            //  response.getNavigation().removeCurrent();
            // Redirect
            final Account account = accounts.get(0);
            final AccountType accountType = account.getType();
            final Map<String, Object> params = new HashMap<String, Object>();
            params.put("memberId", LoggedUser.user().getId());
            params.put("typeId", accountType.getId());
            params.put("singleAccount", true);
            params.put("fromQuickAccess", "fromQuickAccess");
            //return ActionHelper.redirectWithParams(context.getRequest(), context.findForward("accountDetails"), params);

        }
        //response.setAccounts(accounts);
        // response.setMember(member);
        response.setMyAccounts(myAccounts);
        response.setByBroker(byBroker);
        response.setOverview(resolveAccountOverview(accounts));
        response.setSingleAccount(myAccounts);
        
        response.setStatus(0);
        response.setMessage("!! Account details..");
        return response;

    }

    private Map<Account, BigDecimal> resolveAccountOverview(final List<? extends Account> accounts) {
        final Map<Account, BigDecimal> overview = new LinkedHashMap<Account, BigDecimal>();
        for (final Account account : accounts) {
            try {
                final BigDecimal balance = accountService.getBalance(new AccountDateDTO(account));
                overview.put(account, balance);
            } catch (final EntityNotFoundException e) {
                // Ignore this account
            }
        }
        return overview;
    }

}
