package nl.strohalm.cyclos.webservices.rest.payments;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;

import org.apache.struts.action.ActionForward;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import nl.strohalm.cyclos.annotations.Inject;
import nl.strohalm.cyclos.controls.ActionContext;
import nl.strohalm.cyclos.entities.access.Channel;
import nl.strohalm.cyclos.entities.access.User;
import nl.strohalm.cyclos.entities.accounts.AccountOwner;
import nl.strohalm.cyclos.entities.accounts.AccountType;
import nl.strohalm.cyclos.entities.accounts.Currency;
import nl.strohalm.cyclos.entities.accounts.MemberAccountType;
import nl.strohalm.cyclos.entities.accounts.SystemAccountOwner;
import nl.strohalm.cyclos.entities.accounts.transactions.Payment;
import nl.strohalm.cyclos.entities.accounts.transactions.ScheduledPayment;
import nl.strohalm.cyclos.entities.accounts.transactions.Transfer;
import nl.strohalm.cyclos.entities.accounts.transactions.TransferType;
import nl.strohalm.cyclos.entities.accounts.transactions.TransferTypeQuery;
import nl.strohalm.cyclos.entities.customization.fields.PaymentCustomField;
import nl.strohalm.cyclos.entities.customization.fields.PaymentCustomFieldValue;
import nl.strohalm.cyclos.entities.exceptions.EntityNotFoundException;
import nl.strohalm.cyclos.entities.exceptions.UnexpectedEntityException;
import nl.strohalm.cyclos.entities.members.Element;
import nl.strohalm.cyclos.entities.members.Member;
import nl.strohalm.cyclos.entities.settings.LocalSettings;
import nl.strohalm.cyclos.services.access.AccessService;
import nl.strohalm.cyclos.services.access.exceptions.BlockedCredentialsException;
import nl.strohalm.cyclos.services.access.exceptions.InvalidCredentialsException;
import nl.strohalm.cyclos.services.customization.PaymentCustomFieldService;
import nl.strohalm.cyclos.services.elements.ElementService;
import nl.strohalm.cyclos.services.transactions.DoPaymentDTO;
import nl.strohalm.cyclos.services.transactions.PaymentService;
import nl.strohalm.cyclos.services.transactions.TransactionContext;
import nl.strohalm.cyclos.services.transactions.exceptions.AuthorizedPaymentInPastException;
import nl.strohalm.cyclos.services.transactions.exceptions.CreditsException;
import nl.strohalm.cyclos.services.transfertypes.TransactionFeePreviewDTO;
import nl.strohalm.cyclos.services.transfertypes.TransactionFeeService;
import nl.strohalm.cyclos.services.transfertypes.TransferTypeService;
import nl.strohalm.cyclos.utils.ActionHelper;
import nl.strohalm.cyclos.utils.CustomFieldHelper;
import nl.strohalm.cyclos.utils.EntityVO;
import nl.strohalm.cyclos.utils.RelationshipHelper;
import nl.strohalm.cyclos.utils.access.LoggedUser;
import nl.strohalm.cyclos.utils.conversion.CoercionHelper;
import nl.strohalm.cyclos.utils.conversion.IdConverter;
import nl.strohalm.cyclos.utils.query.QueryParameters;
import nl.strohalm.cyclos.utils.validation.ValidationException;
import nl.strohalm.cyclos.webservices.model.FieldValueVO;
import nl.strohalm.cyclos.webservices.model.MemberVO;
import nl.strohalm.cyclos.webservices.model.TransactionFeeVO;
import nl.strohalm.cyclos.webservices.model.TransferTypeVO;
import nl.strohalm.cyclos.webservices.rest.AccessRestController;
import nl.strohalm.cyclos.webservices.rest.AccountsRestController;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;
import nl.strohalm.cyclos.webservices.rest.GenericResponse;
import nl.strohalm.cyclos.webservices.rest.PaymentsRestController;
import nl.strohalm.cyclos.webservices.rest.PaymentsRestController.DoPaymentParameters;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class ConfirmSystemPaymentController extends BaseRestController {
    
    @RequestMapping(value = "admin/confirmSystemPayment", method = RequestMethod.POST)
    @ResponseBody
    public void doPayment(@RequestBody DoPaymentParameters params) {

//        GenericResponse response =new GenericResponse();
//           final ConfirmPaymentForm form = context.getForm();
//        final DoPaymentDTO paymentDTO = validatePayment(context);
//        // Validate the transaction password if needed
//        if (shouldValidateTransactionPassword(context, paymentDTO)) {
//            context.checkTransactionPassword(form.getTransactionPassword());
//        }
//        // Perform the actual payment
//        Payment payment;
//        try {
//            payment = paymentService.doPayment(paymentDTO);
//            context.getSession().removeAttribute("payment");
//        } catch (final CreditsException e) {
//            return context.sendError(actionHelper.resolveErrorKey(e), actionHelper.resolveParameters(e));
//        } catch (final UnexpectedEntityException e) {
//            return context.sendError("payment.error.invalidTransferType");
//        } catch (final AuthorizedPaymentInPastException e) {
//            return context.sendError("payment.error.authorizedInPast");
//        }
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
//        return response;
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
    
    public static class TransferTypeEntity{
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
    
    @RequestMapping(value = "admin/confirmSystemPayment", method = RequestMethod.GET)
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
        List<TransferTypeEntity>transferList=new ArrayList();
        for(TransferType tt:tts){
            TransferTypeEntity transferEntity=new TransferTypeEntity();
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
        query.setContext(TransactionContext.SELF_PAYMENT);
        final AccountOwner owner = accountOwner;
        System.out.println("-----accout owner: "+owner);
        query.setFromOwner(owner);
        query.setToOwner(owner);
        
        query.setGroup(LoggedUser.group());
        
        return query;
    }
    
}
