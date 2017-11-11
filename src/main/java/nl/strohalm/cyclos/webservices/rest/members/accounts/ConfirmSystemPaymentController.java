package nl.strohalm.cyclos.webservices.rest.members.accounts;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import nl.strohalm.cyclos.entities.access.Channel;
import nl.strohalm.cyclos.entities.accounts.AccountOwner;
import nl.strohalm.cyclos.entities.accounts.AccountType;
import nl.strohalm.cyclos.entities.accounts.Currency;
import nl.strohalm.cyclos.entities.accounts.MemberAccountType;
import nl.strohalm.cyclos.entities.accounts.transactions.Payment;
import nl.strohalm.cyclos.entities.accounts.transactions.TransferType;
import nl.strohalm.cyclos.entities.accounts.transactions.TransferTypeQuery;
import nl.strohalm.cyclos.entities.exceptions.UnexpectedEntityException;
import nl.strohalm.cyclos.entities.members.Element;
import nl.strohalm.cyclos.entities.members.Member;
import nl.strohalm.cyclos.services.accounts.AccountTypeService;
import nl.strohalm.cyclos.services.accounts.CurrencyService;
import nl.strohalm.cyclos.services.elements.ElementService;
import nl.strohalm.cyclos.services.transactions.DoPaymentDTO;
import nl.strohalm.cyclos.services.transactions.PaymentService;
import nl.strohalm.cyclos.services.transactions.TransactionContext;
import nl.strohalm.cyclos.services.transactions.exceptions.AuthorizedPaymentInPastException;
import nl.strohalm.cyclos.services.transactions.exceptions.CreditsException;
import nl.strohalm.cyclos.services.transfertypes.TransferTypeService;
import nl.strohalm.cyclos.utils.access.LoggedUser;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;
import nl.strohalm.cyclos.webservices.rest.GenericResponse;


@Controller
public class ConfirmSystemPaymentController extends BaseRestController {
    private TransferTypeService transferTypeService;
    private CurrencyService currencyService;
    private ElementService elementService;
    private AccountTypeService accountTypeService;
    private PaymentService paymentService;

    public TransferTypeService getTransferTypeService() {
        return transferTypeService;
    }

    public void setTransferTypeService(TransferTypeService transferTypeService) {
        this.transferTypeService = transferTypeService;
    }

    public CurrencyService getCurrencyService() {
        return currencyService;
    }

    public void setCurrencyService(CurrencyService currencyService) {
        this.currencyService = currencyService;
    }

    public ElementService getElementService() {
        return elementService;
    }

    public void setElementService(ElementService elementService) {
        this.elementService = elementService;
    }

    public AccountTypeService getAccountTypeService() {
        return accountTypeService;
    }

    public void setAccountTypeService(AccountTypeService accountTypeService) {
        this.accountTypeService = accountTypeService;
    }

    public PaymentService getPaymentService() {
        return paymentService;
    }

    public void setPaymentService(PaymentService paymentService) {
        this.paymentService = paymentService;
    }
    
    
    public static class DoPaymentParameters {

        private BigDecimal amount;
        private Long currency;
        private Long type;
        private String description;
        
        public BigDecimal getAmount() {
            return amount;
        }
        
        public void setAmount(BigDecimal amount) {
            this.amount = amount;
        }
        
        public Long getCurrency() {
            return currency;
        }
        
        public void setCurrency(Long currency) {
            this.currency = currency;
        }
        
        public Long getType() {
            return type;
        }
        
        public void setType(Long type) {
            this.type = type;
        }
        
        public String getDescription() {
            return description;
        }
        
        public void setDescription(String description) {
            this.description = description;
        }
        
    }
    
    @RequestMapping(value = "member/confirmSystemPayment", method = RequestMethod.POST)
    @ResponseBody
    public GenericResponse doPayment(@RequestBody DoPaymentParameters params) {
        
        GenericResponse response = new GenericResponse();
        
        final DoPaymentDTO paymentDTO = new DoPaymentDTO();
        paymentDTO.setAmount(params.getAmount());
        paymentDTO.setTransferType(transferTypeService.load(params.getType(),TransferType.Relationships.FROM,TransferType.Relationships.TO));
        paymentDTO.setDescription(params.getDescription());
        paymentDTO.setCurrency(currencyService.load(params.getCurrency()));
        paymentDTO.setContext(TransactionContext.SELF_PAYMENT);
        paymentDTO.setFrom(LoggedUser.accountOwner());
        paymentDTO.setTo(LoggedUser.accountOwner());
        
        paymentDTO.setChannel(Channel.REST);

        
//        // Validate the transaction password if needed
//        if (shouldValidateTransactionPassword(context, paymentDTO)) {
//            context.checkTransactionPassword(form.getTransactionPassword());
//        }
//        // Perform the actual payment
        Payment payment;
        try {
            payment = paymentService.doPayment(paymentDTO);
            
        } catch (final CreditsException e) {
            //return context.sendError(actionHelper.resolveErrorKey(e), actionHelper.resolveParameters(e));
        } catch (final UnexpectedEntityException e) {
            response.setMessage("payment.error.invalidTransferType");
            return response;
        } catch (final AuthorizedPaymentInPastException e) {
            response.setMessage("payment.error.authorizedInPast");
            return response;
        }
//        // Redirect to the next action
//        final Map<String, Object> params = new HashMap<String, Object>();
//        ActionForward forward;
//        if (payment instanceof Transfer) {
//            params.put("transferId", payment.getId());
//            forward = context.getSuccessForward();
//        } else if (payment instanceof ScheduledPayment) {
//            params.put("paymentId", payment.getId());
//            forward = context.findForward("scheduledPayment");
//        } else {
//            throw new IllegalStateException("Unknown payment type: " + payment);
//        }
//        params.put("selectMember", form.getSelectMember());
//        params.put("from", form.getFrom());
//        return ActionHelper.redirectWithParams(context.getRequest(), forward, params);
//   
        response.setStatus(0);
        response.setMessage("payment successfull !!");
        return response;
    }
    
    public static class ConfirmPaymentResponse extends GenericResponse {
        
        private List<Currency> currencies;
        private List<TransferTypeEntity> allTransferTypes;
        
        public List<TransferTypeEntity> getAllTransferTypes() {
            return allTransferTypes;
        }
        
        public void setAllTransferTypes(List<TransferTypeEntity> allTransferTypes) {
            this.allTransferTypes = allTransferTypes;
        }
        
        public List<Currency> getCurrencies() {
            return currencies;
        }
        
        public void setCurrencies(List<Currency> currencies) {
            this.currencies = currencies;
        }
        
    }
    
    public static class TransferTypeEntity {

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
    
    @RequestMapping(value = "member/confirmSystemPayment", method = RequestMethod.GET)
    @ResponseBody
    public ConfirmPaymentResponse prepareForm() {
        ConfirmPaymentResponse response = new ConfirmPaymentResponse();

//         final Long fromId = IdConverter.instance().valueOf(form.getFrom());
//        final boolean asMember = fromId != null;
//        Member fromMember = null;
//        if (asMember) {
//            final Element element = elementService.load(fromId, Element.Relationships.GROUP, Element.Relationships.USER);
//            if (element instanceof Member) {
//                fromMember = (Member) element;
//                
//            }
//        }
        // Get the member in action
        Member member = null;
        if (member == null && LoggedUser.isMember()) {
            member = LoggedUser.element();
        }
        // Resolve the possible currencies
        final List<Currency> currencies = resolveCurrencies(LoggedUser.accountOwner());

        // Resolve the transfer types
        final TransferTypeQuery ttQuery = resolveTransferTypeQuery(LoggedUser.accountOwner());
        final List<TransferType> tts = transferTypeService.search(ttQuery);
        List<TransferTypeEntity> transferList = new ArrayList();
        System.out.println(".... tts:" );
        for (TransferType tt : tts) {
            TransferTypeEntity transferEntity = new TransferTypeEntity();
            transferEntity.setId(tt.getId());
            transferEntity.setName(tt.getName());
            transferList.add(transferEntity);
            
        }
//        if (ttQuery != null) {
//            
//            Currency defaultCurrency = null;
//            if (member != null) {
//                final MemberAccountType defaultAccountType = accountTypeService.getDefault(member.getMemberGroup(), AccountType.Relationships.CURRENCY);
//                if (defaultAccountType != null) {
//                    defaultCurrency = defaultAccountType.getCurrency();
//                }
//            }
//
//            // Check for transfer types for each currency, removing those currencies without transfer types
////            final Map<Currency, List<TransferType>> transferTypesPerCurrency = new LinkedHashMap<Currency, List<TransferType>>();
////            final List<TransferType> allTransferTypes = new ArrayList<TransferType>();
////            
////            for (final Iterator<Currency> iterator = currencies.iterator(); iterator.hasNext();) {
////                final Currency currency = iterator.next();
////                final TransferTypeQuery currentQuery = (TransferTypeQuery) ttQuery.clone();
////                currentQuery.setCurrency(currency);
////                final List<TransferType> tts = transferTypeService.search(currentQuery);
////                allTransferTypes.addAll(tts);
////                if (tts.isEmpty()) {
////                    iterator.remove();
////                } else {
////                    transferTypesPerCurrency.put(currency, tts);
////                }
////            }
////
////            // Check which currency to preselect
////            Currency currency = null;
////            if (CollectionUtils.isNotEmpty(transferTypesPerCurrency.get(defaultCurrency))) {
////                // There are TTs for the default currency: preselect it
////                currency = defaultCurrency;
////            } else if (!transferTypesPerCurrency.isEmpty()) {
////                // Get the first currency with TTs
////                currency = transferTypesPerCurrency.keySet().iterator().next();
////            }
////            //form.setCurrency(CoercionHelper.coerce(String.class, currency));
////
////            // Store the transfer types associated with the preselected currency
////            //request.setAttribute("transferTypes", allTransferTypes);
////            response.setAllTransferTypes(allTransferTypes);
//            
//        }
//        if (CollectionUtils.isEmpty(currencies)) {
//            // No currency with possible transfer type!!!
//            throw new ValidationException("payment.error.noTransferType");
//        } else if (currencies.size() == 1) {
//            //request.setAttribute("singleCurrency", currencies.iterator().next());
//        }
        response.setAllTransferTypes(transferList);
        response.setCurrencies(currencies);
        
        response.setStatus(0);
        return response;
    }
    
    public List<Currency> resolveCurrencies(final AccountOwner accountOwner) {
        
        final List<Currency> currencies;
        final AccountOwner fromOwner = accountOwner;
        if (fromOwner instanceof Member) {
            final Member member = elementService.load(((Member) fromOwner).getId(), Element.Relationships.GROUP);
            currencies = currencyService.listByMember(member);
            final MemberAccountType defaultAccountType = accountTypeService.getDefault(member.getMemberGroup(), AccountType.Relationships.CURRENCY);
            // Preselect the default currency
            if (defaultAccountType != null) {
                //form.setCurrency(CoercionHelper.coerce(String.class, defaultAccountType.getCurrency()));
            }
        } else {
            currencies = currencyService.listAll();
        }
        
        return currencies;
    }
    
    public TransferTypeQuery resolveTransferTypeQuery(final AccountOwner accountOwner) {
        final TransferTypeQuery query = new TransferTypeQuery();
        query.setUsePriority(true);
        query.setContext(TransactionContext.PAYMENT);
        final AccountOwner owner = accountOwner;
        System.out.println("-----accout owner: " + owner);
        query.setFromOwner(owner);
        query.setToOwner(owner);
        
        query.setGroup(LoggedUser.group());
        
        return query;
    }
    
}