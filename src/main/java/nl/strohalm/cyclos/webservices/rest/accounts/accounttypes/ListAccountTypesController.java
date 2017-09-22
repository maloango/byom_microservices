package nl.strohalm.cyclos.webservices.rest.accounts.accounttypes;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import nl.strohalm.cyclos.annotations.Inject;
import nl.strohalm.cyclos.entities.accounts.AccountType;
import nl.strohalm.cyclos.entities.accounts.Currency;
import nl.strohalm.cyclos.services.accounts.AccountTypeService;
import nl.strohalm.cyclos.services.accounts.MemberAccountTypeQuery;
import nl.strohalm.cyclos.services.accounts.SystemAccountTypeQuery;
import nl.strohalm.cyclos.webservices.model.AccountTypeVO;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;
import nl.strohalm.cyclos.webservices.rest.GenericResponse;
import nl.strohalm.cyclos.webservices.utils.AccountHelper;

@Controller
public class ListAccountTypesController extends BaseRestController {

    private AccountTypeService accountTypeService;
    private AccountHelper accountHelper;

    public AccountTypeService getAccountTypeService() {
        return accountTypeService;
    }

    @Inject
    public void setAccountTypeService(final AccountTypeService accountTypeService) {
        this.accountTypeService = accountTypeService;
    }

    public AccountHelper getAccountHelper() {
        return accountHelper;
    }

    public void setAccountHelper(AccountHelper accountHelper) {
        this.accountHelper = accountHelper;
    }

    public static class ListAccountTypesResponseDTO extends GenericResponse {

        List<AccountTypeVO> accountTypes;

        public List<AccountTypeVO> getAccountTypes() {
            return accountTypes;
        }

        public void setAccountTypes(List<AccountTypeVO> accountTypes) {
            this.accountTypes = accountTypes;
        }

    }

    public static class AccountTypeVO {
        private Long id;
        private String name;
        private String descrption;
        private Currency currency;
        private AccountType.LimitType limitType;
        private AccountType.Nature nature;
        private BigDecimal lowerCreditLimit;
        private BigDecimal upperCreditLimit;

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

        public AccountType.Nature getNature() {
            return nature;
        }

        public void setNature(AccountType.Nature nature) {
            this.nature = nature;
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

    @RequestMapping(value = "admin/listAccountTypes", method = RequestMethod.GET)
    @ResponseBody
    protected ListAccountTypesResponseDTO executeAction() throws Exception {

        ListAccountTypesResponseDTO response = new ListAccountTypesResponseDTO();
        try {
            final List<AccountTypeVO> accountTypes = new ArrayList<AccountTypeVO>();
            List<AccountType> allAccountType=new ArrayList();
            // Get the system accounts
            final SystemAccountTypeQuery systemQuery = new SystemAccountTypeQuery();
            systemQuery.fetch(AccountType.Relationships.CURRENCY);
            allAccountType.addAll(accountTypeService.search(systemQuery));
             final MemberAccountTypeQuery memberQuery = new MemberAccountTypeQuery();
            memberQuery.fetch(AccountType.Relationships.CURRENCY);
             allAccountType.addAll(accountTypeService.search(memberQuery));
            for (AccountType ac : allAccountType) {
                AccountTypeVO accountTypevo = new AccountTypeVO();
                accountTypevo.setName(ac.getName());
                accountTypevo.setDescrption(ac.getDescription());
                accountTypevo.setLimitType(ac.getLimitType());
                accountTypevo.setCurrency(ac.getCurrency());
                accountTypevo.setNature(ac.getNature());
                accountTypevo.setId(ac.getId());
                accountTypes.add(accountTypevo);
            }
            response.setAccountTypes(accountTypes);
            response.setStatus(0);
            response.setMessage("Account type list!!");
            //response.setAccountType(accountTypes);
            // response = new ListAccountTypesResponseDTO(accountTypes)

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return response;
    }

}
