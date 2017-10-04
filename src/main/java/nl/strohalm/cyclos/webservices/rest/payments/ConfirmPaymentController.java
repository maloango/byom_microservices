package nl.strohalm.cyclos.webservices.rest.payments;

import java.math.BigDecimal;
import java.util.ArrayList;
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
import nl.strohalm.cyclos.entities.accounts.AccountOwner;
import nl.strohalm.cyclos.entities.accounts.AccountType;
import nl.strohalm.cyclos.entities.accounts.Currency;
import nl.strohalm.cyclos.entities.accounts.MemberAccountType;
import nl.strohalm.cyclos.entities.accounts.transactions.Payment;
import nl.strohalm.cyclos.entities.accounts.transactions.ScheduledPayment;
import nl.strohalm.cyclos.entities.accounts.transactions.Transfer;
import nl.strohalm.cyclos.entities.accounts.transactions.TransferType;
import nl.strohalm.cyclos.entities.accounts.transactions.TransferTypeQuery;
import nl.strohalm.cyclos.entities.exceptions.UnexpectedEntityException;
import nl.strohalm.cyclos.entities.members.Element;
import nl.strohalm.cyclos.entities.members.Member;
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
import nl.strohalm.cyclos.services.transfertypes.TransactionFeeService;
import nl.strohalm.cyclos.services.transfertypes.TransferTypeService;
import nl.strohalm.cyclos.utils.ActionHelper;
import nl.strohalm.cyclos.utils.CustomFieldHelper;
import nl.strohalm.cyclos.utils.access.LoggedUser;
import nl.strohalm.cyclos.utils.conversion.CoercionHelper;
import nl.strohalm.cyclos.utils.conversion.IdConverter;
import nl.strohalm.cyclos.utils.validation.ValidationException;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;
import nl.strohalm.cyclos.webservices.rest.GenericResponse;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class ConfirmPaymentController extends BaseRestController {
    

    public static class PaymentEntity {

        private BigDecimal amount;
        private String description;
        private Long transferTypeId;
      
        private Long currencyId;
        private String from;

        public BigDecimal getAmount() {
            return amount;
        }

        public void setAmount(BigDecimal amount) {
            this.amount = amount;
        }

        public Long getCurrencyId() {
            return currencyId;
        }

        public void setCurrencyId(Long currencyId) {
            this.currencyId = currencyId;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public Long getTransferTypeId() {
            return transferTypeId;
        }

        public void setTransferTypeId(Long transferTypeId) {
            this.transferTypeId = transferTypeId;
        }

    

        public String getFrom() {
            return from;
        }

        public void setFrom(String from) {
            this.from = from;
        }

    }

    public static class ConfirmPaymentResponse extends GenericResponse {

        private List<TransferTypeEntity> transferTypes;
        private CurrencyEntity singleCurrency;
        private String preselectCurrencySymbol;

        public CurrencyEntity getSingleCurrency() {
            return singleCurrency;
        }

        public void setSingleCurrency(CurrencyEntity singleCurrency) {
            this.singleCurrency = singleCurrency;
        }

        public String getPreselectCurrencySymbol() {
            return preselectCurrencySymbol;
        }

        public void setPreselectCurrencySymbol(String preselectCurrencySymbol) {
            this.preselectCurrencySymbol = preselectCurrencySymbol;
        }

        public List<TransferTypeEntity> getTransferTypes() {
            return transferTypes;
        }

        public void setTransferTypes(List<TransferTypeEntity> transferTypes) {
            this.transferTypes = transferTypes;
        }

    }

    public static class TransferTypeEntity {

        private String name;
        private Long id;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

    }

    public static class CurrencyEntity {

        private String symbol;
        private Long id;

        public String getSymbol() {
            return symbol;
        }

        public void setSymbol(String symbol) {
            this.symbol = symbol;
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

    }

    public List<Currency> resolveCurrencies() {

        final List<Currency> currencies;
        final AccountOwner fromOwner = LoggedUser.accountOwner();
        if (fromOwner instanceof Member) {
            final Member member = elementService.load(((Member) fromOwner).getId(), Element.Relationships.GROUP);
            currencies = currencyService.listByMember(member);
            final MemberAccountType defaultAccountType = accountTypeService.getDefault(member.getMemberGroup(), AccountType.Relationships.CURRENCY);
            // Preselect the default currency
            if (defaultAccountType != null) {
//                form.setCurrency(CoercionHelper.coerce(String.class, defaultAccountType.getCurrency()));
            }
        } else {
            currencies = currencyService.listAll();
        }
//        request.setAttribute("currencies", currencies);

        if (currencies.isEmpty()) {
            // No currencies means no possible payment!!!
            throw new ValidationException("payment.error.noTransferType");
        } else if (currencies.size() == 1) {
            // Special case: There is a single currency. The JSP will use this object
//            request.setAttribute("singleCurrency", currencies.get(0));
        }
        System.out.println("-----currency:-------" + currencies);
        return currencies;
    }

    protected TransferTypeQuery resolveTransferTypeQuery() {

        final Long memberId = LoggedUser.user().getId();

        final TransferTypeQuery query = new TransferTypeQuery();
        query.setUsePriority(true);
        query.setContext(TransactionContext.SELF_PAYMENT);
        final AccountOwner owner = LoggedUser.accountOwner();
        query.setFromOwner(owner);
        query.setToOwner(owner);
        if (memberId != null) {
            query.setBy(LoggedUser.element());
        } else {
            query.setGroup(LoggedUser.group());
        }
        System.out.println("-----query:-------" + query);
        return query;
    }

    @RequestMapping(value = "admin/confirmPayment", method = RequestMethod.GET)
    @ResponseBody
    public ConfirmPaymentResponse prepareForm() {
        ConfirmPaymentResponse response = new ConfirmPaymentResponse();

        // Check whether the payment is as a member
        final Long fromId = LoggedUser.user().getId();
        final boolean asMember = fromId != null;
        Member fromMember = null;
        if (asMember) {
            final Element element = elementService.load(fromId, Element.Relationships.GROUP, Element.Relationships.USER);
            if (element instanceof Member) {
                fromMember = (Member) element;
//                request.setAttribute("member", fromMember);
            }
        }
//        request.setAttribute("asMember", asMember);

        // Get the member in action
        Member member = fromMember;
        if (member == null && LoggedUser.isMember()) {
            member = LoggedUser.element();
        }

        // Resolve the possible currencies
        final List<Currency> currencies = resolveCurrencies();

        // Resolve the transfer types
        final TransferTypeQuery ttQuery = resolveTransferTypeQuery();
        if (ttQuery != null) {

            Currency defaultCurrency = null;
            if (member != null) {
                final MemberAccountType defaultAccountType = accountTypeService.getDefault(member.getMemberGroup(), AccountType.Relationships.CURRENCY);
                if (defaultAccountType != null) {
                    defaultCurrency = defaultAccountType.getCurrency();
                }
            }

            // Check for transfer types for each currency, removing those currencies without transfer types
            final Map<Currency, List<TransferType>> transferTypesPerCurrency = new LinkedHashMap<Currency, List<TransferType>>();
            final List<TransferType> allTransferTypes = new ArrayList<TransferType>();

            for (final Iterator<Currency> iterator = currencies.iterator(); iterator.hasNext();) {
                final Currency currency = iterator.next();
                final TransferTypeQuery currentQuery = (TransferTypeQuery) ttQuery.clone();
                currentQuery.setCurrency(currency);
                final List<TransferType> tts = transferTypeService.search(currentQuery);
                allTransferTypes.addAll(tts);
                if (tts.isEmpty()) {
                    //iterator.remove();
                } else {
                    transferTypesPerCurrency.put(currency, tts);
                }
            }
            System.out.println("---currencies size------------:" + currencies.size());
            // Check which currency to preselect
            Currency currency = null;
            if (CollectionUtils.isNotEmpty(transferTypesPerCurrency.get(defaultCurrency))) {
                // There are TTs for the default currency: preselect it
                currency = defaultCurrency;
            } else if (!transferTypesPerCurrency.isEmpty()) {
                // Get the first currency with TTs
                currency = transferTypesPerCurrency.keySet().iterator().next();
            }
//           response.setPreselectCurrencySymbol(currency.getSymbol());

            // Store the transfer types associated with the preselected currency
//            request.setAttribute("transferTypes", allTransferTypes);
            List<TransferTypeEntity> transferList = new ArrayList();
            for (TransferType transferType : allTransferTypes) {
                TransferTypeEntity transferEntity = new TransferTypeEntity();
                transferEntity.setId(transferType.getId());
                transferEntity.setName(transferType.getName());
                transferList.add(transferEntity);
            }

            response.setTransferTypes(transferList);
        }

        if (CollectionUtils.isEmpty(currencies)) {
            // No currency with possible transfer type!!!
            throw new ValidationException("payment.error.noTransferType");
        } else if (currencies.size() == 1) {
            CurrencyEntity currencyEntity = new CurrencyEntity();
            currencyEntity.setId(currencies.iterator().next().getId());
            currencyEntity.setSymbol(currencies.iterator().next().getSymbol());
            response.setSingleCurrency(currencyEntity);
//            request.setAttribute("singleCurrency", currencies.iterator().next());
        }
        response.setStatus(0);
        response.setMessage("!!!");
        return response;

    }

    @RequestMapping(value = "admin/confirmPayment", method = RequestMethod.POST)
    @ResponseBody
    public GenericResponse doPayment(@RequestBody PaymentEntity request) {
        GenericResponse response = new GenericResponse();

        final DoPaymentDTO paymentDTO = new DoPaymentDTO();
        paymentDTO.setDescription(request.getDescription());
        paymentDTO.setAmount(request.getAmount());
        Currency currency = new Currency();
        currency.setId(request.getCurrencyId());
        paymentDTO.setCurrency(currency);

        TransferType transferType = new TransferType();
        //transferType.setId(transferTypeService.load(request.getTransferTypeId(),TransferType.Relationships.));
        
        paymentDTO.setTransferType(transferType);

        System.out.println("-----currency: ---" + currency);
        System.out.println("-----Transfertyep: ---" + transferType);

        // Validate the transaction password if needed
//        if (shouldValidateTransactionPassword(context, paymentDTO)) {
//            context.checkTransactionPassword(form.getTransactionPassword());
//        }
        // Perform the actual payment
        Payment payment;
        try {
            payment = paymentService.doPayment(paymentDTO);
            System.out.println("---paymentDto---:" + paymentDTO);
//            context.getSession().removeAttribute("payment");
        } catch (final CreditsException e) {
            response.setStatus(1);
        } catch (final UnexpectedEntityException e) {
            response.setMessage("payment.error.invalidTransferType");
        } catch (final AuthorizedPaymentInPastException e) {
            response.setMessage("payment.error.authorizedInPast");
        }
        // Redirect to the next action
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
        return response;

    }
}
