package nl.strohalm.cyclos.webservices.rest.members.accounts;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;

import nl.strohalm.cyclos.entities.access.Channel;
import nl.strohalm.cyclos.entities.accounts.AccountOwner;
import nl.strohalm.cyclos.entities.accounts.AccountType;
import nl.strohalm.cyclos.entities.accounts.Currency;
import nl.strohalm.cyclos.entities.accounts.MemberAccountType;
import nl.strohalm.cyclos.entities.accounts.SystemAccountOwner;
import nl.strohalm.cyclos.entities.accounts.transactions.Invoice;
import nl.strohalm.cyclos.entities.accounts.transactions.TransferType;
import nl.strohalm.cyclos.entities.accounts.transactions.TransferTypeQuery;
import nl.strohalm.cyclos.entities.groups.Group;
import nl.strohalm.cyclos.entities.groups.MemberGroup;
import nl.strohalm.cyclos.entities.groups.MemberGroupSettings;
import nl.strohalm.cyclos.entities.members.Element;
import nl.strohalm.cyclos.entities.members.Member;
import nl.strohalm.cyclos.entities.settings.LocalSettings;
import nl.strohalm.cyclos.services.accounts.AccountTypeService;
import nl.strohalm.cyclos.services.accounts.CurrencyService;
import nl.strohalm.cyclos.services.accounts.MemberAccountTypeQuery;
import nl.strohalm.cyclos.services.elements.ElementService;
import nl.strohalm.cyclos.services.settings.SettingsService;
import nl.strohalm.cyclos.services.transactions.InvoiceService;
import nl.strohalm.cyclos.services.transactions.TransactionContext;
import nl.strohalm.cyclos.services.transactions.exceptions.SendingInvoiceWithMultipleTransferTypesWithCustomFields;
import nl.strohalm.cyclos.services.transfertypes.TransferTypeService;
import nl.strohalm.cyclos.utils.TimePeriod;
import nl.strohalm.cyclos.utils.access.LoggedUser;
import nl.strohalm.cyclos.utils.conversion.CoercionHelper;
import nl.strohalm.cyclos.utils.validation.ValidationException;
//import nl.strohalm.cyclos.webservices.payments.SchedulingType;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;
import nl.strohalm.cyclos.webservices.rest.GenericResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class SendInvoiceController extends BaseRestController {
//    private ElementService elementService;
//    private TransferTypeService transferTypeService;
//    private AccountTypeService accountTypeService;
//    private CurrencyService currencyService;
//    private SettingsService settingsService;
//    private InvoiceService invoiceService;
//
//    public void setElementService(ElementService elementService) {
//        this.elementService = elementService;
//    }
//
//    public void setTransferTypeService(TransferTypeService transferTypeService) {
//        this.transferTypeService = transferTypeService;
//    }
//
//    public void setAccountTypeService(AccountTypeService accountTypeService) {
//        this.accountTypeService = accountTypeService;
//    }
//
//    public void setCurrencyService(CurrencyService currencyService) {
//        this.currencyService = currencyService;
//    }
//
//    public void setSettingsService(SettingsService settingsService) {
//        this.settingsService = settingsService;
//    }
//
//    public void setInvoiceService(InvoiceService invoiceService) {
//        this.invoiceService = invoiceService;
//    }
//    
//    
//    
//    public static class SendInvoiceParameters {
//        
//        private Long to;
//        private boolean toSystem;
//        private boolean selectMember;
//        private BigDecimal amount;
//        private Long currencyId;
//        private Long type;
//        private String description;
//        
//        public Long getTo() {
//            return to;
//        }
//        
//        public void setTo(Long to) {
//            this.to = to;
//        }
//        
//        public boolean isToSystem() {
//            return toSystem;
//        }
//        
//        public void setToSystem(boolean toSystem) {
//            this.toSystem = toSystem;
//        }
//        
//        public boolean isSelectMember() {
//            return selectMember;
//        }
//        
//        public void setSelectMember(boolean selectMember) {
//            this.selectMember = selectMember;
//        }
//        
//        public BigDecimal getAmount() {
//            return amount;
//        }
//        
//        public void setAmount(BigDecimal amount) {
//            this.amount = amount;
//        }
//        
//        public Long getCurrencyId() {
//            return currencyId;
//        }
//        
//        public void setCurrencyId(Long currencyId) {
//            this.currencyId = currencyId;
//        }
//
//        public Long getType() {
//            return type;
//        }
//
//        public void setType(Long type) {
//            this.type = type;
//        }
//        
//   
//        
//        public String getDescription() {
//            return description;
//        }
//        
//        public void setDescription(String description) {
//            this.description = description;
//        }
//        
//    }
//    
//    public static class CurrencyEntity {
//        
//        private Long id;
//        private String symbol;
//        
//        public Long getId() {
//            return id;
//        }
//        
//        public void setId(Long id) {
//            this.id = id;
//        }
//        
//        public String getSymbol() {
//            return symbol;
//        }
//        
//        public void setSymbol(String symbol) {
//            this.symbol = symbol;
//        }
//        
//    }
//    
//    public static class TransferTypeEntity {
//        
//        private Long id;
//        private String name;
//        
//        public Long getId() {
//            return id;
//        }
//        
//        public void setId(Long id) {
//            this.id = id;
//        }
//        
//        public String getName() {
//            return name;
//        }
//        
//        public void setName(String name) {
//            this.name = name;
//        }
//        
//    }
//    
//    public static class AccountTypeEntity {
//        
//        private Long id;
//        private String name;
//        
//        public Long getId() {
//            return id;
//        }
//        
//        public void setId(Long id) {
//            this.id = id;
//        }
//        
//        public String getName() {
//            return name;
//        }
//        
//        public void setName(String name) {
//            this.name = name;
//        }
//        
//    }
//    
//    public static class SendInvoiceResponse extends GenericResponse {
//        
//        private List<CurrencyEntity> currencies;
//        private List<TransferTypeEntity> transferTypes;
//        private List<AccountTypeEntity> accountTypeList=new ArrayList();
//        private boolean toSystem;
//        private boolean toMember;
//        private boolean selectMember;
//        private boolean useTransferType;
//        private String currency;
//        private CurrencyEntity singleCurrency;
//        private boolean allowsScheduling;
//        private boolean allowsMultipleScheduling;
//        private Collection<SchedulingType> schedulingTypes;
//        private List<TimePeriod.Field>schedulingFields;
//
//        public List<TimePeriod.Field> getSchedulingFields() {
//            return schedulingFields;
//        }
//
//        public void setSchedulingFields(List<TimePeriod.Field> schedulingFields) {
//            this.schedulingFields = schedulingFields;
//        }
//        
//
//        public Collection<SchedulingType> getSchedulingTypes() {
//            return schedulingTypes;
//        }
//
//        public void setSchedulingTypes(Collection<SchedulingType> schedulingTypes) {
//            this.schedulingTypes = schedulingTypes;
//        }
//        
//
//        public boolean isAllowsScheduling() {
//            return allowsScheduling;
//        }
//
//        public void setAllowsScheduling(boolean allowsScheduling) {
//            this.allowsScheduling = allowsScheduling;
//        }
//
//        public boolean isAllowsMultipleScheduling() {
//            return allowsMultipleScheduling;
//        }
//
//        public void setAllowsMultipleScheduling(boolean allowsMultipleScheduling) {
//            this.allowsMultipleScheduling = allowsMultipleScheduling;
//        }
//        
//        
//        public List<AccountTypeEntity> getAccountTypeList() {
//            return accountTypeList;
//        }
//        
//        public void setAccountTypeList(List<AccountTypeEntity> accountTypeList) {
//            this.accountTypeList = accountTypeList;
//        }
//
//        public CurrencyEntity getSingleCurrency() {
//            return singleCurrency;
//        }
//
//        public void setSingleCurrency(CurrencyEntity singleCurrency) {
//            this.singleCurrency = singleCurrency;
//        }
//        
//      
//        
//        public String getCurrency() {
//            return currency;
//        }
//        
//        public void setCurrency(String currency) {
//            this.currency = currency;
//        }
//        
//        public boolean isUseTransferType() {
//            return useTransferType;
//        }
//        
//        public void setUseTransferType(boolean useTransferType) {
//            this.useTransferType = useTransferType;
//        }
//        
//        public boolean isToSystem() {
//            return toSystem;
//        }
//        
//        public void setToSystem(boolean toSystem) {
//            this.toSystem = toSystem;
//        }
//        
//        public boolean isToMember() {
//            return toMember;
//        }
//        
//        public void setToMember(boolean toMember) {
//            this.toMember = toMember;
//        }
//        
//        public boolean isSelectMember() {
//            return selectMember;
//        }
//        
//        public void setSelectMember(boolean selectMember) {
//            this.selectMember = selectMember;
//        }
//        
//        public List<CurrencyEntity> getCurrencies() {
//            return currencies;
//        }
//        
//        public void setCurrencies(List<CurrencyEntity> currencies) {
//            this.currencies = currencies;
//        }
//        
//        public List<TransferTypeEntity> getTransferTypes() {
//            return transferTypes;
//        }
//        
//        public void setTransferTypes(List<TransferTypeEntity> transferTypes) {
//            this.transferTypes = transferTypes;
//        }
//        
//    }
//    
//    public static class GetInvoiceDataParameters {
//        
//        private boolean toSystem;
//        private boolean selectMember;
//        private Long to;
//        private Long from;
//        
//        public Long getFrom() {
//            return from;
//        }
//        
//        public void setFrom(Long from) {
//            this.from = from;
//        }
//        
//        public boolean isToSystem() {
//            return toSystem;
//        }
//        
//        public void setToSystem(boolean toSystem) {
//            this.toSystem = toSystem;
//        }
//        
//        public boolean isSelectMember() {
//            return selectMember;
//        }
//        
//        public void setSelectMember(boolean selectMember) {
//            this.selectMember = selectMember;
//        }
//        
//        public Long getTo() {
//            return to;
//        }
//        
//        public void setTo(Long to) {
//            this.to = to;
//        }
//        
//    }
//    
//    @RequestMapping(value = "member/sendInvoiceData", method = RequestMethod.POST)
//    @ResponseBody
//    public SendInvoiceResponse displayForm(@RequestBody GetInvoiceDataParameters params) {
//
////            final HttpServletRequest request = context.getRequest();
////        final SendInvoiceForm form = context.getForm();
//        SendInvoiceResponse response = new SendInvoiceResponse();
//        final boolean toSystem = params.isToSystem();
//        final boolean selectMember = params.isSelectMember();
//        
//        AccountOwner to;
//        final Member fromMember = (params.getFrom() == null) ? null : (Member) elementService.load(params.getFrom());
//        final Element loggedElement = LoggedUser.element();
//        if (toSystem) {
//            // System invoice
//            to = SystemAccountOwner.instance();
//        } else {
//            if (selectMember) {
//                // Retrieve the member to send invoice for
//                Member member = null;
//                final Long memberId = params.getTo();
//                if (memberId != null && memberId != loggedElement.getId()) {
//                    final Element element = elementService.load(memberId, Element.Relationships.USER);
//                    if (element instanceof Member) {
//                        member = (Member) element;
//                    }
//                }
//                if (member == null) {
//                    throw new ValidationException();
//                }
//                //request.setAttribute("member", member);
//                to = member;
//            } else {
//                // The member will be selected later
//                to = null;
//            }
//        }
//
//        // If we know who will receive the invoice, get the transfer types or dest account types
//        if (to != null) {
//            if (LoggedUser.isAdministrator() && fromMember == null) {
//                // Only admins may select the transfer type
//                final TransferTypeQuery query = new TransferTypeQuery();
//                query.setChannel(Channel.WEB);
//                query.setContext(TransactionContext.PAYMENT);
//                query.setFromOwner(to);
//                query.setToOwner(LoggedUser.accountOwner());
//                query.setUsePriority(true);
//                //request.setAttribute("transferTypes", transferTypeService.search(query));
//              
//                List<TransferType>transferTypes=transferTypeService.search(query);
//                  System.out.println("-----trnsferType: "+transferTypes);
//                List<TransferTypeEntity> transferList = new ArrayList();
//                for (TransferType tt :transferTypes ) {
//                    TransferTypeEntity transferEntity = new TransferTypeEntity();
//                    transferEntity.setId(tt.getId());
//                    transferEntity.setName(tt.getName());
//                    transferList.add(transferEntity);
//                }
//                response.setTransferTypes(transferList);
//                  System.out.println("-----trnsferType2: "+transferList); 
//                
//            } else {
//                // Members may select the destination account type
//                final MemberAccountTypeQuery query = new MemberAccountTypeQuery();
//                query.setOwner(fromMember == null ? (Member) loggedElement.getAccountOwner() : fromMember);
//                query.setCanPay(to);
//                final List<? extends AccountType> accountTypes = accountTypeService.search(query);
//                if (accountTypes.isEmpty()) {
//                    // return context.sendError("invoice.error.noAccountType");
//                }
//                // request.setAttribute("accountTypes", accountTypes);
//                List<AccountTypeEntity> accountTypeList = new ArrayList();
//                for (AccountType ac : accountTypes) {
//                    AccountTypeEntity accountEntity = new AccountTypeEntity();
//                    accountEntity.setId(ac.getId());
//                    accountEntity.setName(ac.getName());
//                    accountTypeList.add(accountEntity);
//                }
//                response.setAccountTypeList(accountTypeList);
//            }
//        }
//
//        // Resolve the possible currencies
//        final MemberGroup group = getMemberGroup(params);
//        final List<Currency> currencies;
//        if (group != null) {
//            currencies = currencyService.listByMemberGroup(group);
//            final MemberAccountType defaultAccountType = accountTypeService.getDefault(group, AccountType.Relationships.CURRENCY);
//            // Preselect the default currency
//            if (defaultAccountType != null) {
//                response.setCurrency(CoercionHelper.coerce(String.class, defaultAccountType.getCurrency()));
//                
//            }
//        } else {
//            currencies = currencyService.listAll();
//        }
//        //request.setAttribute("currencies", currencies);
//        List<CurrencyEntity> currencyList = new ArrayList();
//        for (Currency currency : currencies) {
//            CurrencyEntity currencyEntity = new CurrencyEntity();
//            currencyEntity.setId(currency.getId());
//            currencyEntity.setSymbol(currency.getSymbol());
//            currencyList.add(currencyEntity);
//        }
//        response.setCurrencies(currencyList);
//        
//        if (currencies.isEmpty()) {
//            // No currencies means no possible payment!!!
//            throw new ValidationException("payment.error.noTransferType");
//        } else if (currencies.size() == 1) {
//            // Special case: There is a single currency. The JSP will use this object
//            Currency singleCurrency = currencies.get(0);
//            CurrencyEntity singleCurrecyEntity = new CurrencyEntity();
//            singleCurrecyEntity.setId(singleCurrency.getId());
//            singleCurrecyEntity.setSymbol(singleCurrency.getSymbol());
//            response.setSingleCurrency(singleCurrecyEntity);
//            
//        }
//
//        response.setToSystem(toSystem);
//        response.setToMember(!toSystem);
//        response.setSelectMember(selectMember);
//       // response("from", fromMember);
//        final boolean useTransferType = LoggedUser.isAdministrator() && fromMember == null;
//         response.setUseTransferType(useTransferType);
//
//        // Check whether scheduled payments may be performed
//        boolean allowsScheduling = false;
//        boolean allowsMultipleScheduling = false;
//        if (LoggedUser.isAdministrator() && fromMember == null) {
//            allowsScheduling = true;
//            allowsMultipleScheduling = true;
//        } else {
//            MemberGroup memberGroup;
//            if (fromMember == null) {
//                memberGroup = ((Member) LoggedUser.accountOwner()).getMemberGroup();
//            } else {
//                memberGroup = fromMember.getMemberGroup();
//            }
//            final MemberGroupSettings memberSettings = memberGroup.getMemberSettings();
//            allowsScheduling = memberSettings.isAllowsScheduledPayments();
//            allowsMultipleScheduling = memberSettings.isAllowsMultipleScheduledPayments();
//        }
//        if (allowsScheduling) {
//             response.setAllowsScheduling(allowsScheduling);
//              response.setAllowsMultipleScheduling(allowsMultipleScheduling);
//              final Collection<SchedulingType> schedulingTypes = EnumSet.of(SchedulingType.IMMEDIATELY, SchedulingType.SINGLE_FUTURE);
//            if (allowsMultipleScheduling) {
//                 schedulingTypes.add(SchedulingType.MULTIPLE_FUTURE);
//                 schedulingTypes.add(SchedulingType.IMMEDIATELY);
//                 schedulingTypes.add(SchedulingType.SINGLE_FUTURE);
//                 
//            }
//            response.setSchedulingTypes(schedulingTypes);
//             response.setSchedulingFields(Arrays.asList(TimePeriod.Field.MONTHS, TimePeriod.Field.WEEKS, TimePeriod.Field.DAYS));
//        }
////        SendInvoiceResponse response = new SendInvoiceResponse();
////        final boolean toSystem = false;
////        final boolean selectMember = true;
////        AccountOwner to = null;
////        final Element loggedElement = LoggedUser.element();
//////        if (toSystem) {
//////            // System invoice
//////            to = SystemAccountOwner.instance();
//////        } else {
//////            if (!selectMember) {
//////                //Retrieve the member to send invoice for
//////                Member member = null;
//////                final Long memberId = IdConverter.instance().valueOf(form.getTo());
//////                if (memberId != null && memberId != loggedElement.getId()) {
//////                    final Element element = elementService.load(memberId, Element.Relationships.USER);
//////                    if (element instanceof Member) {
//////                        member = (Member) element;
//////                    }
//////                }
//////                if (member == null) {
//////                    throw new ValidationException();
//////                }
//////              
//////                to = member;
//////            } else {
//////                // The member will be selected later
//////                to = null;
//////            }
//////        }
////
////        // If we know who will receive the invoice, get the transfer types or dest account types
////        // Only admins may select the transfer type
////        final TransferTypeQuery query = new TransferTypeQuery();
////        query.setChannel(Channel.WEB);
////        query.setContext(TransactionContext.PAYMENT);
////        query.setFromOwner(to);
////        query.setToOwner(LoggedUser.accountOwner());
////        query.setUsePriority(true);
////        List<TransferType> transferType = transferTypeService.search(query);
////        List<TransferTypeEntity> transferList = new ArrayList();
////        for (TransferType tt : transferType) {
////            TransferTypeEntity transferEntity = new TransferTypeEntity();
////            transferEntity.setId(tt.getId());
////            transferEntity.setName(tt.getName());
////            transferList.add(transferEntity);
////        }
////        response.setTransferTypes(transferList);
////
////        // Resolve the possible currencies
////        final MemberGroup group = null;
////        final List<Currency> currencies;
////        if (group != null) {
////            currencies = currencyService.listByMemberGroup(group);
////            final MemberAccountType defaultAccountType = accountTypeService.getDefault(group, AccountType.Relationships.CURRENCY);
////            // Preselect the default currency
////            if (defaultAccountType != null) {
//////                form.setCurrency(CoercionHelper.coerce(String.class, defaultAccountType.getCurrency()));
////            }
////        } else {
////            currencies = currencyService.listAll();
////        }
////        List<CurrencyEntity> currencyList = new ArrayList();
////        for (Currency currency : currencies) {
////            CurrencyEntity currencyEntity = new CurrencyEntity();
////            currencyEntity.setId(currency.getId());
////            currencyEntity.setSymbol(currency.getSymbol());
////            currencyList.add(currencyEntity);
////        }
////        response.setCurrencies(currencyList);
////
//////        if (currencies.isEmpty()) {
//////            // No currencies means no possible payment!!!
//////            throw new ValidationException("payment.error.noTransferType");
//////        } else if (currencies.size() == 1) {
//////            // Special case: There is a single currency. The JSP will use this object
//////            request.setAttribute("singleCurrency", currencies.get(0));
//////        }
////        response.setToSystem(toSystem);
////        response.setToMember(!toSystem);
////        response.setSelectMember(selectMember);
////
////        final boolean useTransferType = LoggedUser.isAdministrator();
////        response.setUseTransferType(useTransferType);
////
//////        // Check whether scheduled payments may be performed
//////        boolean allowsScheduling = false;
//////        boolean allowsMultipleScheduling = false;
//////        if (context.isAdmin() && fromMember == null) {
//////            allowsScheduling = true;
//////            allowsMultipleScheduling = true;
//////        } else {
//////            MemberGroup memberGroup;
//////            if (fromMember == null) {
//////                memberGroup = ((Member) context.getAccountOwner()).getMemberGroup();
//////            } else {
//////                memberGroup = fromMember.getMemberGroup();
//////            }
//////            final MemberGroupSettings memberSettings = memberGroup.getMemberSettings();
//////            allowsScheduling = memberSettings.isAllowsScheduledPayments();
//////            allowsMultipleScheduling = memberSettings.isAllowsMultipleScheduledPayments();
//////        }
//////        if (allowsScheduling) {
//////            request.setAttribute("allowsScheduling", allowsScheduling);
//////            request.setAttribute("allowsMultipleScheduling", allowsMultipleScheduling);
//////            final Collection<SchedulingType> schedulingTypes = EnumSet.of(SchedulingType.IMMEDIATELY, SchedulingType.SINGLE_FUTURE);
//////            if (allowsMultipleScheduling) {
//////                schedulingTypes.add(SchedulingType.MULTIPLE_FUTURE);
//////            }
//////            request.setAttribute("schedulingTypes", schedulingTypes);
//////            request.setAttribute("schedulingFields", Arrays.asList(TimePeriod.Field.MONTHS, TimePeriod.Field.WEEKS, TimePeriod.Field.DAYS));
//////        }
//////
//////        return context.getInputForward();
//        response.setStatus(0);
//        response.setMessage("send invoice data!!");
//        return response;
//
//    }
//    
//    private MemberGroup getMemberGroup(GetInvoiceDataParameters params) {
//        
//        final Long fromId = params.getFrom();
//        final Long toId = params.getTo();
//        Group group = null;
//        if (fromId == null && toId == null) {
//            group = LoggedUser.group();
//        } else if (fromId != null) {
//            final Element element = elementService.load(fromId, Element.Relationships.GROUP);
//            group = element.getGroup();
//        } else {
//            final Element element = elementService.load(toId, Element.Relationships.GROUP);
//            group = element.getGroup();
//        }
//        if (group instanceof MemberGroup) {
//            return (MemberGroup) group;
//        }
//        return null;
//    }
//    
//    @RequestMapping(value = "member/sendInvoice", method = RequestMethod.POST)
//    @ResponseBody
//    public GenericResponse submit(@RequestBody SendInvoiceParameters params) {
//        GenericResponse response = new GenericResponse();
//        
//        final boolean fromProfile = !params.isToSystem() && !params.isSelectMember();
//        final LocalSettings localSettings = settingsService.getLocalSettings();
////        binder.registerBinder("from", PropertyBinder.instance(AccountOwner.class, "from", AccountOwnerConverter.instance()));
////        binder.registerBinder("to", PropertyBinder.instance(AccountOwner.class, "to", AccountOwnerConverter.instance()));
////        binder.registerBinder("transferType", PropertyBinder.instance(TransferType.class, "type", ReferenceConverter.instance(TransferType.class)));
////        binder.registerBinder("destinationAccountType", PropertyBinder.instance(AccountType.class, "destType", ReferenceConverter.instance(AccountType.class)));
////        binder.registerBinder("amount", PropertyBinder.instance(BigDecimal.class, "amount", localSettings.getNumberConverter()));
////        binder.registerBinder("description", PropertyBinder.instance(String.class, "description"));
//        AccountOwner from = LoggedUser.accountOwner();
//        AccountOwner to = (Member) elementService.load(params.getTo());
//        //Currency currency = currencyService.load(params.getCurrencyId());
//        Invoice invoiceMessage = new Invoice();
//        // invoiceMessage.setFrom(from);
//
//        invoiceMessage.setTo(to);
//        invoiceMessage.setAmount(params.getAmount());
//        invoiceMessage.setDescription(params.getDescription());
//        //invoiceMessage.setToMember((Member) elementService.load(params.getTo()));
//        invoiceMessage.setTransferType(transferTypeService.load(params.getType(), TransferType.Relationships.FROM, TransferType.Relationships.TO));
//        
//        try {
//            final Invoice invoice = invoiceService.send(invoiceMessage);
//            response.setMessage("invoice.sent");
//            
//        } catch (final SendingInvoiceWithMultipleTransferTypesWithCustomFields e) {
//            response.setMessage("invoice.error.sendingWithMultipleTransferTypesWithCustomFields");
//        }
//        response.setStatus(0);
//        return response;
//    }
    
}
