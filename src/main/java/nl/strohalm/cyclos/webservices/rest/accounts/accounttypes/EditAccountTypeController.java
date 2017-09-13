package nl.strohalm.cyclos.webservices.rest.accounts.accounttypes;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import javax.servlet.http.HttpServletRequest;
import nl.strohalm.cyclos.access.AdminSystemPermission;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import nl.strohalm.cyclos.annotations.Inject;
import nl.strohalm.cyclos.entities.Relationship;
import nl.strohalm.cyclos.entities.accounts.AccountType;
import nl.strohalm.cyclos.entities.accounts.Currency;
import nl.strohalm.cyclos.entities.accounts.MemberAccountType;
import nl.strohalm.cyclos.entities.accounts.SystemAccountType;
import nl.strohalm.cyclos.entities.accounts.fees.account.AccountFee;
import nl.strohalm.cyclos.entities.accounts.fees.account.AccountFeeQuery;
import nl.strohalm.cyclos.entities.accounts.transactions.PaymentFilter;
import nl.strohalm.cyclos.entities.accounts.transactions.PaymentFilterQuery;
import nl.strohalm.cyclos.entities.accounts.transactions.TransferType;
import nl.strohalm.cyclos.entities.accounts.transactions.TransferTypeQuery;
import nl.strohalm.cyclos.entities.settings.LocalSettings;
import nl.strohalm.cyclos.entities.settings.events.LocalSettingsEvent;
import nl.strohalm.cyclos.services.accountfees.AccountFeeService;
import nl.strohalm.cyclos.services.accounts.AccountTypeService;
import nl.strohalm.cyclos.services.accounts.CurrencyService;
import nl.strohalm.cyclos.services.permissions.PermissionService;
import nl.strohalm.cyclos.services.settings.SettingsService;
import nl.strohalm.cyclos.services.transactions.TransactionContext;
import nl.strohalm.cyclos.services.transfertypes.PaymentFilterService;
import nl.strohalm.cyclos.services.transfertypes.TransferTypeService;
import nl.strohalm.cyclos.utils.RelationshipHelper;
import nl.strohalm.cyclos.utils.RequestHelper;
import nl.strohalm.cyclos.utils.binding.BeanBinder;
import nl.strohalm.cyclos.utils.binding.DataBinder;
import nl.strohalm.cyclos.utils.binding.PropertyBinder;
import nl.strohalm.cyclos.utils.conversion.IdConverter;
import nl.strohalm.cyclos.utils.validation.ValidationException;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;
import nl.strohalm.cyclos.webservices.rest.GenericResponse;
//import static org.apache.commons.httpclient.URI.param;

@Controller
public class EditAccountTypeController extends BaseRestController {

    private CurrencyService currencyService;
    private AccountTypeService accountTypeService;
    private TransferTypeService transferTypeService;
    private AccountFeeService accountFeeService;
    private PaymentFilterService paymentFilterService;
    private PermissionService permissionService;

    public PermissionService getPermissionService() {
        return permissionService;
    }

    @Inject
    public void setPermissionService(PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    public CurrencyService getCurrencyService() {
        return currencyService;
    }

    @Inject
    public void setCurrencyService(CurrencyService currencyService) {
        this.currencyService = currencyService;
    }

    public AccountTypeService getAccountTypeService() {
        return accountTypeService;
    }

    @Inject
    public void setAccountTypeService(AccountTypeService accountTypeService) {
        this.accountTypeService = accountTypeService;
    }

    public TransferTypeService getTransferTypeService() {
        return transferTypeService;
    }

    @Inject
    public void setTransferTypeService(TransferTypeService transferTypeService) {
        this.transferTypeService = transferTypeService;
    }

    public AccountFeeService getAccountFeeService() {
        return accountFeeService;
    }

    @Inject
    public void setAccountFeeService(AccountFeeService accountFeeService) {
        this.accountFeeService = accountFeeService;
    }

    public PaymentFilterService getPaymentFilterService() {
        return paymentFilterService;
    }

    @Inject
    public void setPaymentFilterService(PaymentFilterService paymentFilterService) {
        this.paymentFilterService = paymentFilterService;
    }

    public static class EditAccountTypeRequest {

        private Long id;
        private Long accountTypeId;
        private String nature;
        private String name;
        private String description;
        private Long currency;
        private String limitType;
        private BigDecimal creditLimit;
        private BigDecimal upperCreditLimit;

        public Long getAccountTypeId() {
            return accountTypeId;
        }

        public void setAccountTypeId(Long accountTypeId) {
            this.accountTypeId = accountTypeId;
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getNature() {
            return nature;
        }

        public void setNature(String nature) {
            this.nature = nature;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public Long getCurrency() {
            return currency;
        }

        public void setCurrency(Long currency) {
            this.currency = currency;
        }

        public String getLimitType() {
            return limitType;
        }

        public void setLimitType(String limitType) {
            this.limitType = limitType;
        }

        public BigDecimal getCreditLimit() {
            return creditLimit;
        }

        public void setCreditLimit(BigDecimal creditLimit) {
            this.creditLimit = creditLimit;
        }

        public BigDecimal getUpperCreditLimit() {
            return upperCreditLimit;
        }

        public void setUpperCreditLimit(BigDecimal upperCreditLimit) {
            this.upperCreditLimit = upperCreditLimit;
        }

    }

    public static class EditAccountResponse extends GenericResponse {

        private AccountType accountType;
        private List<TransferType> transferTypes;
        private List<AccountFee> accountFees;
        private List<PaymentFilter> paymentFilters;
        private List<Currency> currencies;
        private boolean isInsert;
        private boolean isSystem;
        private boolean editable;

        public List<Currency> getCurrencies() {
            return currencies;
        }

        public void setCurrencies(List<Currency> currencies) {
            this.currencies = currencies;
        }

        public boolean isIsInsert() {
            return isInsert;
        }

        public void setIsInsert(boolean isInsert) {
            this.isInsert = isInsert;
        }

        public boolean isIsSystem() {
            return isSystem;
        }

        public void setIsSystem(boolean isSystem) {
            this.isSystem = isSystem;
        }

        public boolean isEditable() {
            return editable;
        }

        public void setEditable(boolean editable) {
            this.editable = editable;
        }

        public AccountType getAccountType() {
            return accountType;
        }

        public void setAccountType(AccountType accountType) {
            this.accountType = accountType;
        }

        public List<TransferType> getTransferTypes() {
            return transferTypes;
        }

        public void setTransferTypes(List<TransferType> transferTypes) {
            this.transferTypes = transferTypes;
        }

        public List<AccountFee> getAccountFees() {
            return accountFees;
        }

        public void setAccountFees(List<AccountFee> accountFees) {
            this.accountFees = accountFees;
        }

        public List<PaymentFilter> getPaymentFilters() {
            return paymentFilters;
        }

        public void setPaymentFilters(List<PaymentFilter> paymentFilters) {
            this.paymentFilters = paymentFilters;
        }
    }

    @RequestMapping(value = "admin/editAccountType", method = RequestMethod.POST)
    @ResponseBody
    public EditAccountResponse addAccount(@RequestBody EditAccountTypeRequest request) {
        EditAccountResponse response = new EditAccountResponse();
//        SystemAccountType systemAccoutType=new SystemAccountType();
//        systemAccoutType.setCreditLimit(request.getCreditLimit());
//        systemAccoutType.setUpperCreditLimit(request.getUpperCreditLimit());
//        systemAccoutType.setDescription(request.getDescription());
//        systemAccoutType.setName(request.getName());
//        Currency currency=new Currency();
//        currency.setId(request.getId());
//        systemAccoutType.setCurrency(currency);

//        final HttpServletRequest request = context.getRequest();
//        final EditAccountTypeForm form = context.getForm();
        final long id = request.getAccountTypeId();
        final boolean isInsert = id <= 0L;
        final boolean editable = permissionService.hasPermission(AdminSystemPermission.ACCOUNTS_MANAGE);
        boolean isSystem = false;
        if (isInsert) {
//            RequestHelper.storeEnum(request, AccountType.Nature.class, "natures");
//            RequestHelper.storeEnum(request, AccountType.LimitType.class, "limitTypes");
        } else {
            final AccountType accountType = accountTypeService.load(id);
            isSystem = accountType instanceof SystemAccountType;
            response.setAccountType(accountType);
             accountTypeService.save(accountType);
//            getDataBinder(accountType.getNature()).writeAsString(form.getAccountType(), accountType);

            final TransferTypeQuery ttQuery = new TransferTypeQuery();
            ttQuery.fetch(TransferType.Relationships.FROM, TransferType.Relationships.TO);
            ttQuery.setContext(TransactionContext.ANY);
            ttQuery.setFromAccountType(accountType);
            response.setTransferTypes(transferTypeService.search(ttQuery));

            if (!isSystem) {
                final AccountFeeQuery feeQuery = new AccountFeeQuery();
                final Set<Relationship> fetch = new HashSet<Relationship>();
                fetch.add(RelationshipHelper.nested(AccountFee.Relationships.ACCOUNT_TYPE, AccountType.Relationships.CURRENCY));
                feeQuery.setFetch(fetch);
                feeQuery.setAccountType(accountType);
                feeQuery.setReturnDisabled(true);
                response.setAccountFees(accountFeeService.search(feeQuery));
            }

            final PaymentFilterQuery filterQuery = new PaymentFilterQuery();
            filterQuery.setAccountType(accountType);
            response.setPaymentFilters(paymentFilterService.search(filterQuery));

        }
        response.setCurrencies(currencyService.listAll());
        response.setIsInsert(isInsert);
        response.setIsSystem(isSystem);
        response.setEditable(editable);
        response.setStatus(0);
        response.setMessage("account edited!!");

        //AccountType.LimitType.LIMITED;
        //AccountType accountType = resolveAccountType(request);
//      final boolean isInsert = systemAccoutType.getId() == null;
//      
//      systemAccoutType = accountTypeService.save(systemAccoutType);
//         if(isInsert){
//             response.setMessage("Account inserted !!");
//         }
//         else
//         {
//             response.setMessage("Account modified !!");
//         }
//         response.setStatus(0);
//        
//
        return response;
    }

//        private AccountType resolveAccountType(final EditAccountTypeRequest form) {
//        final long id = form.getId();
//        AccountType accountType=null;
//        AccountType.Nature nature;
//        if (id <= 0L) {
//            try {
//                nature = AccountType.Nature.valueOf(form.getNature());
//            } catch (final Exception e) {
//                throw new ValidationException();
//            }
//        } else {
//            accountType = accountTypeService.load(id);
//            
//        }
//        return accountType;
//    }
}


// having prepared form to complete later full implementation 
