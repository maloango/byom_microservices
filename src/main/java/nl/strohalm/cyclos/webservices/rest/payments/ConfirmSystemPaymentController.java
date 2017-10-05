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

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class ConfirmSystemPaymentController extends BaseRestController {

    private static final String NO_POSSIBLE_TRANSFER_TYPES_ERROR = "NO_POSSIBLE_TRANSFER_TYPES";
    private static final String BLOCKED_TRANSACTION_PASSWORD_ERROR = "BLOCKED_TRANSACTION_PASSWORD";
    private static final String INVALID_TRANSACTION_PASSWORD_ERROR = "INVALID_TRANSACTION_PASSWORD";
    private static final String MISSING_TRANSACTION_PASSWORD_ERROR = "MISSING_TRANSACTION_PASSWORD";
    private static final String INACTIVE_TRANSACTION_PASSWORD_ERROR = "INACTIVE_TRANSACTION_PASSWORD";
    private static final String INVALID_AMOUNT_ERROR = "INVALID_AMOUNT";

    private static final String MAX_AMOUNT_PER_DAY_EXCEEDED = "MAX_AMOUNT_PER_DAY_EXCEEDED";
    private static final String NOT_ENOUGH_CREDITS = "NOT_ENOUGH_FUNDS";
    private static final String UPPER_CREDIT_LIMIT_REACHED = "UPPER_CREDIT_LIMIT_REACHED";
    protected static final String TRANSFER_MINIMUM_PAYMENT = "TRANSFER_MINIMUM_PAYMENT";
    protected static final String UNKNOWN_PAYMENT_ERROR = "UNKNOWN_PAYMENT_ERROR";

    private AccountsRestController accountsRestController;
    private PaymentCustomFieldService paymentCustomFieldService;
    private CustomFieldHelper customFieldHelper;
    private TransactionFeeService transactionFeeService;
    private AccessRestController accessRestController;

    public AccessRestController getAccessRestController() {
        return accessRestController;
    }

    @Inject
    public void setAccessRestController(AccessRestController accessRestController) {
        this.accessRestController = accessRestController;
    }

    public TransactionFeeService getTransactionFeeService() {
        return transactionFeeService;
    }

    @Inject
    public void setTransactionFeeService(TransactionFeeService transactionFeeService) {
        this.transactionFeeService = transactionFeeService;
    }

    public CustomFieldHelper getCustomFieldHelper() {
        return customFieldHelper;
    }

    @Inject
    public void setCustomFieldHelper(CustomFieldHelper customFieldHelper) {
        this.customFieldHelper = customFieldHelper;
    }

    public PaymentCustomFieldService getPaymentCustomFieldService() {
        return paymentCustomFieldService;
    }

    @Inject
    public void setPaymentCustomFieldService(PaymentCustomFieldService paymentCustomFieldService) {
        this.paymentCustomFieldService = paymentCustomFieldService;
    }

    public AccountsRestController getAccountsRestController() {
        return accountsRestController;
    }

    @Inject
    public void setAccountsRestController(AccountsRestController accountsRestController) {
        this.accountsRestController = accountsRestController;
    }
    
    
      public static class ConfirmPaymentResult {
        private Long    id;
        private boolean pending;

        public Long getId() {
            return id;
        }

        public boolean isPending() {
            return pending;
        }

        public void setId(final Long id) {
            this.id = id;
        }

        public void setPending(final boolean pending) {
            this.pending = pending;
        }
    }

    public static class DoPaymentResult {

        private boolean wouldRequireAuthorization;
        private MemberVO from;
        private MemberVO to;
        private BigDecimal finalAmount;
        private String formattedFinalAmount;
        private List<TransactionFeeVO> fees;
        private TransferTypeVO transferType;
        private Map<String, String> customValues;

        public Map<String, String> getCustomValues() {
            return customValues;
        }

        public List<TransactionFeeVO> getFees() {
            return fees;
        }

        public BigDecimal getFinalAmount() {
            return finalAmount;
        }

        public String getFormattedFinalAmount() {
            return formattedFinalAmount;
        }

        public MemberVO getFrom() {
            return from;
        }

        public MemberVO getTo() {
            return to;
        }

        public TransferTypeVO getTransferType() {
            return transferType;
        }

        public boolean isWouldRequireAuthorization() {
            return wouldRequireAuthorization;
        }

        public void setCustomValues(final Map<String, String> customValues) {
            this.customValues = customValues;
        }

        public void setFees(final List<TransactionFeeVO> fees) {
            this.fees = fees;
        }

        public void setFinalAmount(final BigDecimal finalAmount) {
            this.finalAmount = finalAmount;
        }

        public void setFormattedFinalAmount(final String formattedFinalAmount) {
            this.formattedFinalAmount = formattedFinalAmount;
        }

        public void setFrom(final MemberVO from) {
            this.from = from;
        }

        public void setTo(final MemberVO to) {
            this.to = to;
        }

        public void setTransferType(final TransferTypeVO transferType) {
            this.transferType = transferType;
        }

        public void setWouldRequireAuthorization(final boolean wouldRequireAuthorization) {
            this.wouldRequireAuthorization = wouldRequireAuthorization;
        }

    }

    public static class DoPaymentParameters {

        private Long currencyId;
        private String currencySymbol;
        private Long transferTypeId;
        private BigDecimal amount;
        private String description;
        private String transactionPassword;
        private List<FieldValueVO> customValues;

        public BigDecimal getAmount() {
            return amount;
        }

        public Long getCurrencyId() {
            return currencyId;
        }

        public String getCurrencySymbol() {
            return currencySymbol;
        }

        public List<FieldValueVO> getCustomValues() {
            return customValues;
        }

        public String getDescription() {
            return description;
        }

        public String getTransactionPassword() {
            return transactionPassword;
        }

        public Long getTransferTypeId() {
            return transferTypeId;
        }

        public void setAmount(final BigDecimal amount) {
            this.amount = amount;
        }

        public void setCurrencyId(final Long currencyId) {
            this.currencyId = currencyId;
        }

        public void setCurrencySymbol(final String currencySymbol) {
            this.currencySymbol = currencySymbol;
        }

        public void setCustomValues(final List<FieldValueVO> customValues) {
            this.customValues = customValues;
        }

        public void setDescription(final String description) {
            this.description = description;
        }

        public void setTransactionPassword(final String transactionPassword) {
            this.transactionPassword = transactionPassword;
        }

        public void setTransferTypeId(final Long transferTypeId) {
            this.transferTypeId = transferTypeId;
        }

        @Override
        public String toString() {
            return "DoPaymentParameters [currencyId=" + currencyId + ", currencySymbol=" + currencySymbol + ", transferTypeId=" + transferTypeId + ", amount=" + amount + ", description=" + description + ", transactionPassword=" + transactionPassword + ", customValues=" + customValues + "]";
        }

    }

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

        private List<EntityVO> transferTypes;
        private List<Currency> currencies;

        public List<Currency> getCurrencies() {
            return currencies;
        }

        public void setCurrencies(List<Currency> currencies) {
            this.currencies = currencies;
        }

        public List<EntityVO> getTransferTypes() {
            return transferTypes;
        }

        public void setTransferTypes(List<EntityVO> transferTypes) {
            this.transferTypes = transferTypes;
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

    @RequestMapping(value = "admin/confirmSystemPayment", method = RequestMethod.GET)
    @ResponseBody
    public ConfirmPaymentResponse prepareForm() {
        ConfirmPaymentResponse response = new ConfirmPaymentResponse();
//        Currency currency = new Currency();
//        currency.setId(CurrencyId);
        final Long memberId = LoggedUser.user().getId();

        // Resolve the possible currencies
        final List<Currency> currencies = currencyService.listAll();
        final TransferTypeQuery query = new TransferTypeQuery();
        query.setUsePriority(true);
        query.setContext(TransactionContext.SELF_PAYMENT);
        final AccountOwner owner = LoggedUser.accountOwner();
        query.setFromOwner(owner);
        query.setToOwner(owner);

        query.setGroup(LoggedUser.group());

        // Check for transfer types for each currency, removing those currencies without transfer types
        final Map<Currency, List<TransferType>> transferTypesPerCurrency = new LinkedHashMap<Currency, List<TransferType>>();
        final List<TransferType> allTransferTypes = new ArrayList<TransferType>();

        for (final Iterator<Currency> iterator = currencies.iterator(); iterator.hasNext();) {
            final Currency currency = iterator.next();
            final TransferTypeQuery currentQuery = (TransferTypeQuery) query.clone();
            currentQuery.setCurrency(currency);
            final List<TransferType> tts = transferTypeService.search(currentQuery);
            allTransferTypes.addAll(tts);
            if (tts.isEmpty()) {
                iterator.remove();
            } else {
                transferTypesPerCurrency.put(currency, tts);

            }
        }

        System.out.println("-------------  " + allTransferTypes.toString());
        List<EntityVO> ttVo = new ArrayList<EntityVO>();
        for (TransferType allTransferType : allTransferTypes) {
            ttVo.add(allTransferType.readOnlyView());
        }

        response.setTransferTypes(ttVo);
        response.setCurrencies(currencies);

        response.setStatus(0);
        response.setMessage("Transfer type list");
        return response;

    }

    @RequestMapping(value = "admin/confirmSystemPayment", method = RequestMethod.POST)
    @ResponseBody
    public ConfirmPaymentResult doPayment(@RequestBody DoPaymentParameters params) {
        if (params == null) {
            throw new ValidationException();
        }

        Member loggedMember = LoggedUser.member();

        BigDecimal amount = params.getAmount();
        if (amount == null) {
            throw new IllegalArgumentException(INVALID_AMOUNT_ERROR);
        }
        AccountOwner toOwner = SystemAccountOwner.instance();
        TransferType transferType = resolveTransferType(params, toOwner);

        // Calculate the installments
        // List<ScheduledPaymentDTO> installments = buildInstallments(params, amount, transferType);
        // Check the transaction password, if needed
        if (accessRestController.isRequireTransactionPassword()) {
            String transactionPassword = params.getTransactionPassword();
            User.TransactionPasswordStatus status = loggedMember.getUser().getTransactionPasswordStatus();
            if (status == null || status == User.TransactionPasswordStatus.PENDING || status == User.TransactionPasswordStatus.NEVER_CREATED) {
                throw new IllegalArgumentException(INACTIVE_TRANSACTION_PASSWORD_ERROR);
            }
            if (StringUtils.isEmpty(transactionPassword)) {
                throw new IllegalArgumentException(MISSING_TRANSACTION_PASSWORD_ERROR);
            }
            try {
                accessService.checkTransactionPassword(transactionPassword);
            } catch (InvalidCredentialsException e) {
                throw new IllegalArgumentException(INVALID_TRANSACTION_PASSWORD_ERROR);
            } catch (BlockedCredentialsException e) {
                throw new IllegalArgumentException(BLOCKED_TRANSACTION_PASSWORD_ERROR);
            }
        }

        // Create the DoPaymentDTO
        DoPaymentDTO dto = buildDoPaymentDTO(params, toOwner, transferType);

        // Perform the payment
        Payment payment = paymentService.doPayment(dto);

        // Create the result
       ConfirmPaymentResult result = new ConfirmPaymentResult();
        result.setId(payment.getId());
        result.setPending(payment.getProcessDate() == null);
        return result;
    }

    private TransferType resolveTransferType(final DoPaymentParameters params, final AccountOwner toOwner) {
        Member loggedMember = LoggedUser.member();
        Currency currency = accountsRestController.loadCurrencyByIdOrSymbol(params.getCurrencyId(), params.getCurrencySymbol());
        final TransferTypeQuery query = new TransferTypeQuery();
        query.setResultType(QueryParameters.ResultType.LIST);
        if (loggedMember == null) {
            query.setContext(LoggedUser.accountOwner().equals(toOwner) ? TransactionContext.SELF_PAYMENT : TransactionContext.PAYMENT);
            query.setFromOwner(LoggedUser.accountOwner());
        } else {
            query.setContext(loggedMember.equals(toOwner) ? TransactionContext.SELF_PAYMENT : TransactionContext.PAYMENT);
            query.setFromOwner(loggedMember);
        }
        query.setChannel(Channel.REST);

        query.setToOwner(toOwner);
        query.setCurrency(currency);
        List<TransferType> possibleTransferTypes = transferTypeService.search(query);
        if (possibleTransferTypes.isEmpty()) {
            throw new IllegalArgumentException(NO_POSSIBLE_TRANSFER_TYPES_ERROR);
        }

        // Resolve the transfer type
        Long transferTypeId = params.getTransferTypeId();
        TransferType transferType = null;
        if (transferTypeId != null) {
            for (TransferType tt : possibleTransferTypes) {
                if (tt.getId().equals(transferTypeId)) {
                    transferType = tt;
                    break;
                }
            }
        } else {
            // When there are multiple transfer types, prefer the first one from the default account
            if (possibleTransferTypes.size() > 1) {
                MemberAccountType defaultType = accountTypeService.getDefault(loggedMember.getMemberGroup());
                for (TransferType current : possibleTransferTypes) {
                    if (current.getFrom().equals(defaultType)) {
                        transferType = current;
                        break;
                    }
                }
            }
            // No TT found so far. Get the first one
            if (transferType == null) {
                transferType = possibleTransferTypes.isEmpty() ? null : possibleTransferTypes.get(0);
            }
        }
        if (transferType == null) {
            throw new EntityNotFoundException(TransferType.class);
        }
        return transferType;
    }

    private DoPaymentDTO buildDoPaymentDTO(final DoPaymentParameters params, final AccountOwner toOwner, final TransferType transferType) {
        DoPaymentDTO dto = new DoPaymentDTO();
        dto.setContext(TransactionContext.PAYMENT);
        dto.setChannel(Channel.REST);
        dto.setAmount(params.getAmount());
        dto.setCurrency(null);
        dto.setTo(toOwner);
        dto.setTransferType(transferType);
        dto.setDescription(params.getDescription());
        // dto.setPayments(installments);
        List<PaymentCustomField> allowedFields = paymentCustomFieldService.list(transferType, true);
        final Collection<PaymentCustomFieldValue> customValues = customFieldHelper.toValueCollection(allowedFields, params.getCustomValues());
        dto.setCustomValues(customValues);
        return dto;
    }
}
