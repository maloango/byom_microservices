/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.strohalm.cyclos.webservices.rest.accounts.accounttypes;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import nl.strohalm.cyclos.entities.accounts.Account;
import nl.strohalm.cyclos.entities.accounts.AccountLimitLog;
import nl.strohalm.cyclos.entities.accounts.AccountType;
import nl.strohalm.cyclos.entities.accounts.AccountTypeQuery;
import nl.strohalm.cyclos.entities.accounts.Currency;
import nl.strohalm.cyclos.services.accounts.AccountDTO;
import nl.strohalm.cyclos.services.accounts.MemberAccountTypeQuery;
import nl.strohalm.cyclos.services.accounts.SystemAccountTypeQuery;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;
import nl.strohalm.cyclos.webservices.rest.GenericResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 *
 * @author Lue Infoservices
 */
@Controller
public class GetAccountTypeByIdController extends BaseRestController {

    public static class GetAccountResponse extends GenericResponse {

        private String name;
        private String descrption;
        private Currency currency;
        private AccountType.LimitType limitType;
        private AccountType.Nature nature;
        private BigDecimal lowerCreditLimit;
        private BigDecimal upperCreditLimit;

        public AccountType.Nature getNature() {
            return nature;
        }

        public void setNature(AccountType.Nature nature) {
            this.nature = nature;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDescrption() {
            return descrption;
        }

        public void setDescrption(String descrption) {
            this.descrption = descrption;
        }

        public Currency getCurrency() {
            return currency;
        }

        public void setCurrency(Currency currency) {
            this.currency = currency;
        }

        public AccountType.LimitType getLimitType() {
            return limitType;
        }

        public void setLimitType(AccountType.LimitType limitType) {
            this.limitType = limitType;
        }

        public BigDecimal getLowerCreditLimit() {
            return lowerCreditLimit;
        }

        public void setLowerCreditLimit(BigDecimal lowerCreditLimit) {
            this.lowerCreditLimit = lowerCreditLimit;
        }

        public BigDecimal getUpperCreditLimit() {
            return upperCreditLimit;
        }

        public void setUpperCreditLimit(BigDecimal upperCreditLimit) {
            this.upperCreditLimit = upperCreditLimit;
        }

    }

    @RequestMapping(value = "admin/getAccountTypeById/{accountTypeId}", method = RequestMethod.GET)
    @ResponseBody
    protected GetAccountResponse executeAction(@PathVariable("accountTypeId") Long accountTypeId) throws Exception {

        GetAccountResponse response = new GetAccountResponse();
        final AccountTypeQuery accountQuery = new SystemAccountTypeQuery();
        accountQuery.fetch(AccountType.Relationships.CURRENCY);
        //accountTypes.addAll(accountTypeService.search(memberQuery));
//        Account ac = accountService.getAccount(params, Account.Relationships.TYPE);
        response.setName(accountTypeService.load(accountTypeId).getName());
        response.setDescrption(accountTypeService.load(accountTypeId).getDescription());
        response.setLimitType(accountTypeService.load(accountTypeId).getLimitType());
        response.setCurrency(accountTypeService.load(accountTypeId).getCurrency());
        response.setNature(accountTypeService.load(accountTypeId).getNature());
        Account account=accountService.load(accountTypeId,Account.Relationships.TYPE);
        //response.getLowerCreditLimit(account.getCreditLimit());
        response.setUpperCreditLimit(account.getUpperCreditLimit());
        
        response.setStatus(0);
        response.setMessage("Account type list!!");
        //response.setAccountType(accountTypes);
        // response = new ListAccountTypesResponseDTO(accountTypes)

        return response;
    }
}
