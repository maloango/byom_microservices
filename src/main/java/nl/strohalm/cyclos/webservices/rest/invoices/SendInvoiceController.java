package nl.strohalm.cyclos.webservices.rest.invoices;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
//import nl.strohalm.cyclos.controls.invoices.SendInvoiceForm;
//import nl.strohalm.cyclos.controls.payments.SchedulingType;
import nl.strohalm.cyclos.entities.access.Channel;
import nl.strohalm.cyclos.entities.accounts.Account;
import nl.strohalm.cyclos.entities.accounts.AccountOwner;
import nl.strohalm.cyclos.entities.accounts.AccountType;
import nl.strohalm.cyclos.entities.accounts.Currency;
import nl.strohalm.cyclos.entities.accounts.MemberAccountType;
import nl.strohalm.cyclos.entities.accounts.transactions.Invoice;
import nl.strohalm.cyclos.entities.accounts.transactions.TransferType;
import nl.strohalm.cyclos.entities.accounts.transactions.TransferTypeQuery;
import nl.strohalm.cyclos.entities.groups.MemberGroup;
import nl.strohalm.cyclos.entities.members.Element;
import nl.strohalm.cyclos.entities.members.Member;
import nl.strohalm.cyclos.entities.settings.LocalSettings;
import nl.strohalm.cyclos.services.transactions.TransactionContext;
import nl.strohalm.cyclos.services.transactions.exceptions.SendingInvoiceWithMultipleTransferTypesWithCustomFields;
import nl.strohalm.cyclos.services.transfertypes.TransferTypeService;
import nl.strohalm.cyclos.utils.ActionHelper;
import nl.strohalm.cyclos.utils.access.LoggedUser;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;
import nl.strohalm.cyclos.webservices.rest.GenericResponse;
import org.apache.struts.action.ActionForward;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class SendInvoiceController extends BaseRestController {

    public static class SendInvoiceParameters {

        private Long to;
        private boolean toSystem;
        private boolean selectMember;
        private BigDecimal amount;
        private Long currencyId;
        private Long paymentType;
        private String description;

        public Long getTo() {
            return to;
        }

        public void setTo(Long to) {
            this.to = to;
        }

        public boolean isToSystem() {
            return toSystem;
        }

        public void setToSystem(boolean toSystem) {
            this.toSystem = toSystem;
        }

        public boolean isSelectMember() {
            return selectMember;
        }

        public void setSelectMember(boolean selectMember) {
            this.selectMember = selectMember;
        }

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

        public Long getPaymentType() {
            return paymentType;
        }

        public void setPaymentType(Long paymentType) {
            this.paymentType = paymentType;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

    }

    public static class CurrencyEntity {

        private Long id;
        private String symbol;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getSymbol() {
            return symbol;
        }

        public void setSymbol(String symbol) {
            this.symbol = symbol;
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

    public static class SendInvoiceResponse extends GenericResponse {

        private List<CurrencyEntity> currencies;
        private List<TransferTypeEntity> transferTypes;
        private boolean toSystem;
        private boolean toMember;
        private boolean selectMember;
        private boolean useTransferType;

        public boolean isUseTransferType() {
            return useTransferType;
        }

        public void setUseTransferType(boolean useTransferType) {
            this.useTransferType = useTransferType;
        }

        public boolean isToSystem() {
            return toSystem;
        }

        public void setToSystem(boolean toSystem) {
            this.toSystem = toSystem;
        }

        public boolean isToMember() {
            return toMember;
        }

        public void setToMember(boolean toMember) {
            this.toMember = toMember;
        }

        public boolean isSelectMember() {
            return selectMember;
        }

        public void setSelectMember(boolean selectMember) {
            this.selectMember = selectMember;
        }

        public List<CurrencyEntity> getCurrencies() {
            return currencies;
        }

        public void setCurrencies(List<CurrencyEntity> currencies) {
            this.currencies = currencies;
        }

        public List<TransferTypeEntity> getTransferTypes() {
            return transferTypes;
        }

        public void setTransferTypes(List<TransferTypeEntity> transferTypes) {
            this.transferTypes = transferTypes;
        }

    }

    @RequestMapping(value = "admin/sendInvoice", method = RequestMethod.GET)
    @ResponseBody
    public SendInvoiceResponse displayForm() {
        SendInvoiceResponse response = new SendInvoiceResponse();
        final boolean toSystem = false;
        final boolean selectMember = true;
        AccountOwner to = null;
        final Element loggedElement = LoggedUser.element();
//        if (toSystem) {
//            // System invoice
//            to = SystemAccountOwner.instance();
//        } else {
//            if (!selectMember) {
//                //Retrieve the member to send invoice for
//                Member member = null;
//                final Long memberId = IdConverter.instance().valueOf(form.getTo());
//                if (memberId != null && memberId != loggedElement.getId()) {
//                    final Element element = elementService.load(memberId, Element.Relationships.USER);
//                    if (element instanceof Member) {
//                        member = (Member) element;
//                    }
//                }
//                if (member == null) {
//                    throw new ValidationException();
//                }
//              
//                to = member;
//            } else {
//                // The member will be selected later
//                to = null;
//            }
//        }

        // If we know who will receive the invoice, get the transfer types or dest account types
        // Only admins may select the transfer type
        final TransferTypeQuery query = new TransferTypeQuery();
        query.setChannel(Channel.WEB);
        query.setContext(TransactionContext.PAYMENT);
        query.setFromOwner(to);
        query.setToOwner(LoggedUser.accountOwner());
        query.setUsePriority(true);
        List<TransferType> transferType = transferTypeService.search(query);
        List<TransferTypeEntity> transferList = new ArrayList();
        for (TransferType tt : transferType) {
            TransferTypeEntity transferEntity = new TransferTypeEntity();
            transferEntity.setId(tt.getId());
            transferEntity.setName(tt.getName());
            transferList.add(transferEntity);
        }
        response.setTransferTypes(transferList);

        // Resolve the possible currencies
        final MemberGroup group = null;
        final List<Currency> currencies;
        if (group != null) {
            currencies = currencyService.listByMemberGroup(group);
            final MemberAccountType defaultAccountType = accountTypeService.getDefault(group, AccountType.Relationships.CURRENCY);
            // Preselect the default currency
            if (defaultAccountType != null) {
//                form.setCurrency(CoercionHelper.coerce(String.class, defaultAccountType.getCurrency()));
            }
        } else {
            currencies = currencyService.listAll();
        }
        List<CurrencyEntity> currencyList = new ArrayList();
        for (Currency currency : currencies) {
            CurrencyEntity currencyEntity = new CurrencyEntity();
            currencyEntity.setId(currency.getId());
            currencyEntity.setSymbol(currency.getSymbol());
            currencyList.add(currencyEntity);
        }
        response.setCurrencies(currencyList);

//        if (currencies.isEmpty()) {
//            // No currencies means no possible payment!!!
//            throw new ValidationException("payment.error.noTransferType");
//        } else if (currencies.size() == 1) {
//            // Special case: There is a single currency. The JSP will use this object
//            request.setAttribute("singleCurrency", currencies.get(0));
//        }
        response.setToSystem(toSystem);
        response.setToMember(!toSystem);
        response.setSelectMember(selectMember);

        final boolean useTransferType = LoggedUser.isAdministrator();
        response.setUseTransferType(useTransferType);

//        // Check whether scheduled payments may be performed
//        boolean allowsScheduling = false;
//        boolean allowsMultipleScheduling = false;
//        if (context.isAdmin() && fromMember == null) {
//            allowsScheduling = true;
//            allowsMultipleScheduling = true;
//        } else {
//            MemberGroup memberGroup;
//            if (fromMember == null) {
//                memberGroup = ((Member) context.getAccountOwner()).getMemberGroup();
//            } else {
//                memberGroup = fromMember.getMemberGroup();
//            }
//            final MemberGroupSettings memberSettings = memberGroup.getMemberSettings();
//            allowsScheduling = memberSettings.isAllowsScheduledPayments();
//            allowsMultipleScheduling = memberSettings.isAllowsMultipleScheduledPayments();
//        }
//        if (allowsScheduling) {
//            request.setAttribute("allowsScheduling", allowsScheduling);
//            request.setAttribute("allowsMultipleScheduling", allowsMultipleScheduling);
//            final Collection<SchedulingType> schedulingTypes = EnumSet.of(SchedulingType.IMMEDIATELY, SchedulingType.SINGLE_FUTURE);
//            if (allowsMultipleScheduling) {
//                schedulingTypes.add(SchedulingType.MULTIPLE_FUTURE);
//            }
//            request.setAttribute("schedulingTypes", schedulingTypes);
//            request.setAttribute("schedulingFields", Arrays.asList(TimePeriod.Field.MONTHS, TimePeriod.Field.WEEKS, TimePeriod.Field.DAYS));
//        }
//
//        return context.getInputForward();
        response.setStatus(0);
        response.setMessage("send invoice data!!");
        return response;

    }

    @RequestMapping(value = "admin/sendInvoice", method = RequestMethod.POST)
    @ResponseBody
    public GenericResponse submit(@RequestBody SendInvoiceParameters params) {
        GenericResponse response = new GenericResponse();

        final boolean fromProfile = !params.isToSystem() && !params.isSelectMember();
        final LocalSettings localSettings = settingsService.getLocalSettings();
//        binder.registerBinder("from", PropertyBinder.instance(AccountOwner.class, "from", AccountOwnerConverter.instance()));
//        binder.registerBinder("to", PropertyBinder.instance(AccountOwner.class, "to", AccountOwnerConverter.instance()));
//        binder.registerBinder("transferType", PropertyBinder.instance(TransferType.class, "type", ReferenceConverter.instance(TransferType.class)));
//        binder.registerBinder("destinationAccountType", PropertyBinder.instance(AccountType.class, "destType", ReferenceConverter.instance(AccountType.class)));
//        binder.registerBinder("amount", PropertyBinder.instance(BigDecimal.class, "amount", localSettings.getNumberConverter()));
//        binder.registerBinder("description", PropertyBinder.instance(String.class, "description"));
        AccountOwner from = LoggedUser.accountOwner();
        AccountOwner to = (Member) elementService.load(params.getTo());
        Invoice invoiceMessage = new Invoice();
        invoiceMessage.setFrom(from);
        invoiceMessage.setTo(to);
        invoiceMessage.setAmount(params.getAmount());
        invoiceMessage.setDescription(params.getDescription());
        invoiceMessage.setToMember((Member) elementService.load(params.getTo()));
        invoiceMessage.setTransferType(transferTypeService.load(params.getPaymentType(),TransferType.Relationships.FROM, TransferType.Relationships.TO));
        
        try {
            final Invoice invoice = invoiceService.send(invoiceMessage);
            response.setMessage("invoice.sent");

        } catch (final SendingInvoiceWithMultipleTransferTypesWithCustomFields e) {
            response.setMessage("invoice.error.sendingWithMultipleTransferTypesWithCustomFields");
        }
        response.setStatus(0);
        return response;
    }

}
