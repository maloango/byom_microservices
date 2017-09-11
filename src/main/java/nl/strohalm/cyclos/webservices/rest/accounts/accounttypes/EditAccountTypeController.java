package nl.strohalm.cyclos.webservices.rest.accounts.accounttypes;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import nl.strohalm.cyclos.annotations.Inject;
import nl.strohalm.cyclos.entities.accounts.AccountType;
import nl.strohalm.cyclos.entities.accounts.Currency;
import nl.strohalm.cyclos.entities.accounts.MemberAccountType;
import nl.strohalm.cyclos.entities.accounts.SystemAccountType;
import nl.strohalm.cyclos.entities.settings.LocalSettings;
import nl.strohalm.cyclos.entities.settings.events.LocalSettingsEvent;
import nl.strohalm.cyclos.services.accountfees.AccountFeeService;
import nl.strohalm.cyclos.services.accounts.AccountTypeService;
import nl.strohalm.cyclos.services.accounts.CurrencyService;
import nl.strohalm.cyclos.services.permissions.PermissionService;
import nl.strohalm.cyclos.services.settings.SettingsService;
import nl.strohalm.cyclos.services.transfertypes.PaymentFilterService;
import nl.strohalm.cyclos.services.transfertypes.TransferTypeService;
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
    
    public static class EditAccountTypeRequest{
        
   private Long id;
   private String nature;
   private String name;
   private String description;
   private Long currency;
   private String limitType;
   private BigDecimal creditLimit;
   private BigDecimal upperCreditLimit;

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
 

    @RequestMapping(value = "admin/editAccountType", method = RequestMethod.POST)
    @ResponseBody
    public GenericResponse addAccount(@RequestBody EditAccountTypeRequest request) {
        GenericResponse response = new GenericResponse();
        SystemAccountType systemAccoutType=new SystemAccountType();
        systemAccoutType.setCreditLimit(request.getCreditLimit());
        systemAccoutType.setUpperCreditLimit(request.getUpperCreditLimit());
        systemAccoutType.setDescription(request.getDescription());
        systemAccoutType.setName(request.getName());
        Currency currency=new Currency();
        currency.setId(request.getId());
        systemAccoutType.setCurrency(currency);
        
        
   
        
        //AccountType.LimitType.LIMITED;
     
         //AccountType accountType = resolveAccountType(request);
      final boolean isInsert = systemAccoutType.getId() == null;
      
      systemAccoutType = accountTypeService.save(systemAccoutType);
         if(isInsert){
             response.setMessage("Account inserted !!");
         }
         else
         {
             response.setMessage("Account modified !!");
         }
         response.setStatus(0);
        

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
