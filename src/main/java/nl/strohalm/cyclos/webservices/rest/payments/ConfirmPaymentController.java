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

        private String amount;
        private String description;
        private String type;
        private String date;
        private String currency;
        private String from;

        public String getAmount() {
            return amount;
        }

        public void setAmount(String amount) {
            this.amount = amount;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getDate() {
            return date;
        }

        public void setDate(String date) {
            this.date = date;
        }

        public String getCurrency() {
            return currency;
        }

        public void setCurrency(String currency) {
            this.currency = currency;
        }

        public String getFrom() {
            return from;
        }

        public void setFrom(String from) {
            this.from = from;
        }

    }

    public static class ConfirmPaymentResponse extends GenericResponse {

        private Member fromMember;
        private boolean asMember;

        public boolean isAsMember() {
            return asMember;
        }

        public void setAsMember(boolean asMember) {
            this.asMember = asMember;
        }

        public Member getFromMember() {
            return fromMember;
        }

        public void setFromMember(Member fromMember) {
            this.fromMember = fromMember;
        }

        private List<TransferType> allTransferTypes;
        private Currency currency;

        public List<TransferType> getAllTransferTypes() {
            return allTransferTypes;
        }

        public void setAllTransferTypes(List<TransferType> allTransferTypes) {
            this.allTransferTypes = allTransferTypes;
        }

        public Currency getCurrency() {
            return currency;
        }

        public void setCurrency(Currency currency) {
            this.currency = currency;
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
        return query;
    }

    @RequestMapping(value = "admin/confirmPayment/{from}", method = RequestMethod.GET)
    @ResponseBody
    public ConfirmPaymentResponse prepareForm(@PathVariable("from") Long from) {
        ConfirmPaymentResponse response = new ConfirmPaymentResponse();
//        final BasePaymentForm form = context.getForm();
//        final HttpServletRequest request = context.getRequest();

        // Check whether the payment is as a member
        final Long fromId = from;
        final boolean asMember = fromId != null;
        Member fromMember = null;
        if (asMember) {
            final Element element = elementService.load(fromId, Element.Relationships.GROUP, Element.Relationships.USER);
            if (element instanceof Member) {
                fromMember = (Member) element;
                response.setFromMember(fromMember);
            }
        }
        response.setAsMember(asMember);

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
                    iterator.remove();
                } else {
                    transferTypesPerCurrency.put(currency, tts);
                }
            }

            // Check which currency to preselect
            Currency currency = null;
            if (CollectionUtils.isNotEmpty(transferTypesPerCurrency.get(defaultCurrency))) {
                // There are TTs for the default currency: preselect it
                currency = defaultCurrency;
            } else if (!transferTypesPerCurrency.isEmpty()) {
                // Get the first currency with TTs
                currency = transferTypesPerCurrency.keySet().iterator().next();
            }
//            form.setCurrency(CoercionHelper.coerce(String.class, currency));

            // Store the transfer types associated with the preselected currency
            response.setAllTransferTypes(allTransferTypes);
        }

        if (CollectionUtils.isEmpty(currencies)) {
            // No currency with possible transfer type!!!
            throw new ValidationException("payment.error.noTransferType");
        } else if (currencies.size() == 1) {
            response.setCurrency(currencies.iterator().next());
        }

        response.setStatus(0);
        response.setMessage("!!!");
        return response;

    }

//    @RequestMapping(value = "admin/confirmPayment", method = RequestMethod.POST)
//    @ResponseBody
//    public GenericResponse doPayment(@RequestBody PaymentEntity request) {
//        GenericResponse response = new GenericResponse();
//
//        final DoPaymentDTO paymentDTO = new DoPaymentDTO();
//        paymentDTO.setDescription(request.getDescription());
//        paymentDTO.setAmount(request.getAmount());
//        Currency currency = new Currency();
//        currency.setId(request.getCurrencyId());
//        paymentDTO.setCurrency(currency);
//
//        TransferType transferType = new TransferType();
//        transferType.setId(request.getTransferTypeId());
//        paymentDTO.setTransferType(transferType);
//
//        System.out.println("-----currency: ---" + currency);
//        System.out.println("-----Transfertyep: ---" + transferType);
//
//        // Validate the transaction password if needed
////        if (shouldValidateTransactionPassword(context, paymentDTO)) {
////            context.checkTransactionPassword(form.getTransactionPassword());
////        }
//        // Perform the actual payment
//        Payment payment;
//        try {
//            payment = paymentService.doPayment(paymentDTO);
//            System.out.println("---paymentDto---:" + paymentDTO);
////            context.getSession().removeAttribute("payment");
//        } catch (final CreditsException e) {
//            response.setStatus(1);
//        } catch (final UnexpectedEntityException e) {
//            response.setMessage("payment.error.invalidTransferType");
//        } catch (final AuthorizedPaymentInPastException e) {
//            response.setMessage("payment.error.authorizedInPast");
//        }
//        // Redirect to the next action
////        final Map<String, Object> params = new HashMap<String, Object>();
////        ActionForward forward;
////        if (payment instanceof Transfer) {
////            params.put("transferId", payment.getId());
////            forward = context.getSuccessForward();
////        } else if (payment instanceof ScheduledPayment) {
////            params.put("paymentId", payment.getId());
////            forward = context.findForward("scheduledPayment");
////        } else {
////            throw new IllegalStateException("Unknown payment type: " + payment);
////        }
////        params.put("selectMember", form.getSelectMember());
////        params.put("from", form.getFrom());
//        return response;
//
//    }
}
