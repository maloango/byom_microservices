package nl.strohalm.cyclos.webservices.rest.accounts.accounttypes;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
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
import nl.strohalm.cyclos.services.accounts.AccountService;
import nl.strohalm.cyclos.services.accounts.AccountTypeService;
import nl.strohalm.cyclos.services.accounts.CurrencyService;
import nl.strohalm.cyclos.services.permissions.PermissionService;
import nl.strohalm.cyclos.services.settings.SettingsService;
import nl.strohalm.cyclos.services.transactions.TransactionContext;
import nl.strohalm.cyclos.services.transfertypes.PaymentFilterService;
import nl.strohalm.cyclos.services.transfertypes.TransferTypeService;
import nl.strohalm.cyclos.utils.ActionHelper;
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
    private Map<AccountType.Nature, DataBinder<? extends AccountType>> dataBinders;
    private ReadWriteLock lock = new ReentrantReadWriteLock(true);

    public DataBinder<? extends AccountType> getDataBinder(final AccountType.Nature nature) {
        try {
            lock.readLock().lock();
            if (dataBinders == null) {
                final HashMap<AccountType.Nature, DataBinder<? extends AccountType>> temp = new HashMap<AccountType.Nature, DataBinder<? extends AccountType>>();
                final LocalSettings localSettings = settingsService.getLocalSettings();

                final BeanBinder<SystemAccountType> systemBinder = BeanBinder.instance(SystemAccountType.class);
                initBasic(systemBinder);
                systemBinder.registerBinder("creditLimit", PropertyBinder.instance(BigDecimal.class, "creditLimit", localSettings.getNumberConverter()));
                systemBinder.registerBinder("upperCreditLimit", PropertyBinder.instance(BigDecimal.class, "upperCreditLimit", localSettings.getNumberConverter()));
                temp.put(AccountType.Nature.SYSTEM, systemBinder);

                final BeanBinder<MemberAccountType> memberBinder = BeanBinder.instance(MemberAccountType.class);
                initBasic(memberBinder);
                temp.put(AccountType.Nature.MEMBER, memberBinder);
                dataBinders = temp;
            }
            return dataBinders.get(nature);
        } finally {
            lock.readLock().unlock();
        }
    }

    private void initBasic(final BeanBinder<? extends AccountType> binder) {
        binder.registerBinder("id", PropertyBinder.instance(Long.class, "id", IdConverter.instance()));
        binder.registerBinder("name", PropertyBinder.instance(String.class, "name"));
        binder.registerBinder("description", PropertyBinder.instance(String.class, "description"));
        binder.registerBinder("currency", PropertyBinder.instance(Currency.class, "currency"));
    }

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
        private Long currencyId;
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

        public Long getCurrencyId() {
            return currencyId;
        }

        public void setCurrencyId(Long currencyId) {
            this.currencyId = currencyId;
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

        private List<Currency> currencies;
        private Set<Entry<String, AccountType.Nature>> set;
        private boolean isInsert;
        private boolean isSystem;
        private boolean editable;

        public Set<Entry<String, AccountType.Nature>> getSet() {
            return set;
        }

        public void setSet(Set<Entry<String, AccountType.Nature>> set) {
            this.set = set;
        }

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

    }

    @RequestMapping(value = "admin/editAccountType", method = RequestMethod.POST)
    @ResponseBody
    public GenericResponse EditAccount(@RequestBody EditAccountTypeRequest request) throws Exception {
        GenericResponse response = new GenericResponse();
        try {
            AccountType accountType = null;

            final boolean isInsert = request.getAccountTypeId() == null;
            Currency currency = new Currency();
            currency.setId(request.getCurrencyId());
            System.out.println("------id: "+request.getAccountTypeId());
            AccountType.Nature nature ;
            if (request.getAccountTypeId() <= 0L) {
                try {
                    nature = AccountType.Nature.valueOf(request.getNature().toString());
                } catch (final Exception e) {
                    throw new ValidationException();
                }
            } else {
                nature = accountTypeService.load(request.getAccountTypeId()).getNature();
            }
            
            System.out.print("nature------: "+nature);
            if (nature.equals(AccountType.Nature.SYSTEM)) {
                SystemAccountType systemAccount = new SystemAccountType();
                systemAccount.setId(request.getAccountTypeId());
                systemAccount.setName(request.getName());
                systemAccount.setDescription(request.getDescription());
                systemAccount.setCurrency(currency);
                systemAccount.setUpperCreditLimit(request.getUpperCreditLimit());
                systemAccount.setCreditLimit(request.getCreditLimit());
                accountType = systemAccount;

            } else if (nature.equals(AccountType.Nature.MEMBER)) {
                MemberAccountType memberAccount = new MemberAccountType();
                memberAccount.setId(request.getAccountTypeId());
                memberAccount.setName(request.getName());
                memberAccount.setDescription(request.getDescription());
                memberAccount.setCurrency(currency);
                // memberAccount.setUpperCreditLimit(request.getUpperCreditLimit());
                //memberAccount.setCreditLimit(request.getCreditLimit());
                accountType = memberAccount;
            }
            accountType = accountTypeService.save(accountType);
           
            response.setMessage(isInsert ? "accountType.inserted" : "accountType.modified");
            response.setStatus(0);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return response;
    }

    @RequestMapping(value = "admin/editAccountType", method = RequestMethod.GET)
    @ResponseBody
    public EditAccountResponse prepareForm() {
        EditAccountResponse response = new EditAccountResponse();
        Map<String, AccountType.Nature> nature = new HashMap<String, AccountType.Nature>();
        nature.put("Member", AccountType.Nature.MEMBER);
        nature.put("System", AccountType.Nature.SYSTEM);
        Set<Entry<String, AccountType.Nature>> type = nature.entrySet();
        final boolean editable = permissionService.hasPermission(AdminSystemPermission.ACCOUNTS_MANAGE);
        boolean isSystem = false;
        response.setSet(type);
        response.setCurrencies(currencyService.listAll());

        response.setIsInsert(false);
        response.setIsSystem(isSystem);
        response.setEditable(editable);
        return response;
    }

    private AccountType resolveAccountType(final EditAccountTypeRequest form) {
        final long id = form.getAccountTypeId();
        AccountType.Nature nature;
        if (id <= 0L) {
            try {
                nature = AccountType.Nature.valueOf(form.getNature().toString());
            } catch (final Exception e) {
                throw new ValidationException();
            }
        } else {
            nature = accountTypeService.load(id).getNature();
        }
        return getDataBinder(nature).readFromString(form.getNature());
    }
}



// having prepared form to complete later full implementation 
