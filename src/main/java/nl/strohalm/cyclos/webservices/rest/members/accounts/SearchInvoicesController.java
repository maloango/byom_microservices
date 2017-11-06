/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.strohalm.cyclos.webservices.rest.members.accounts;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import nl.strohalm.cyclos.annotations.Inject;
import nl.strohalm.cyclos.entities.EntityReference;
import nl.strohalm.cyclos.entities.accounts.AccountOwner;
import nl.strohalm.cyclos.entities.accounts.SystemAccountOwner;
import nl.strohalm.cyclos.entities.accounts.transactions.Invoice;
import nl.strohalm.cyclos.entities.accounts.transactions.InvoiceQuery;
import nl.strohalm.cyclos.entities.accounts.transactions.TransferType;
import nl.strohalm.cyclos.entities.accounts.transactions.TransferTypeQuery;
import nl.strohalm.cyclos.entities.groups.AdminGroup;
import nl.strohalm.cyclos.entities.members.Element;
import nl.strohalm.cyclos.entities.members.Member;
import nl.strohalm.cyclos.entities.members.Operator;
import nl.strohalm.cyclos.entities.settings.LocalSettings;
import nl.strohalm.cyclos.services.elements.ElementService;
import nl.strohalm.cyclos.services.groups.GroupService;
import nl.strohalm.cyclos.services.settings.SettingsService;
import nl.strohalm.cyclos.services.transactions.InvoiceService;
import nl.strohalm.cyclos.services.transactions.TransactionContext;
import nl.strohalm.cyclos.services.transfertypes.TransferTypeService;
import nl.strohalm.cyclos.utils.Period;
import nl.strohalm.cyclos.utils.access.LoggedUser;
import nl.strohalm.cyclos.utils.binding.BeanBinder;
import nl.strohalm.cyclos.utils.binding.DataBinder;
import nl.strohalm.cyclos.utils.binding.DataBinderHelper;
import nl.strohalm.cyclos.utils.binding.PropertyBinder;
import nl.strohalm.cyclos.utils.conversion.AccountOwnerConverter;
import nl.strohalm.cyclos.utils.conversion.ReferenceConverter;
import nl.strohalm.cyclos.utils.validation.ValidationException;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;
import nl.strohalm.cyclos.webservices.rest.GenericResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;


/**
 *
 * @author Lue
 */
@Controller
public class SearchInvoicesController extends BaseRestController {

    private SettingsService settingsService;
    private ElementService elementService;
    private GroupService groupService;

//    public static class Entry {
//        private Invoice invoice;
//        private Member  relatedMember;
//        private String  relatedName;
//
//        public Invoice getInvoice() {
//            return invoice;
//        }
//
//        public Member getRelatedMember() {
//            return relatedMember;
//        }
//
//        public String getRelatedName() {
//            return relatedName;
//        }
//
//        public void setInvoice(final Invoice invoice) {
//            this.invoice = invoice;
//        }
//
//        public void setRelatedMember(final Member relatedMember) {
//            this.relatedMember = relatedMember;
//        }
//
//        public void setRelatedName(final String relatedName) {
//            this.relatedName = relatedName;
//        }
//    }
//
//    public class TransformInvoiceInEntry implements Transformer<Invoice, Entry> {
//
//        private AccountOwner  owner;
//        private LocalSettings localSettings;
//
//        public TransformInvoiceInEntry(final AccountOwner owner) {
//            this.owner = owner;
//            localSettings = settingsService.getLocalSettings();
//        }
//
//        @Override
//        public Entry transform(final Invoice invoice) {
//            final Entry entry = new Entry();
//            entry.setInvoice(invoice);
//            final AccountOwner from = invoice.getFrom();
//            final AccountOwner to = invoice.getTo();
//            final AccountOwner related = owner.equals(from) ? to : from;
//            if (related instanceof Member) {
//                try {
//                    entry.setRelatedMember((Member) elementService.load(((Member) related).getId()));
//                } catch (final PermissionDeniedException e) {
//                    // Ok. The logged member cannot see this member
//                }
//            }
//            if (entry.getRelatedMember() == null) {
//                if (related instanceof Member) {
//                    entry.setRelatedName(((Member) related).getUsername());
//                } else {
//                    entry.setRelatedName(localSettings.getApplicationUsername());
//                }
//            }
//            return entry;
//        }
//
//    }
    private DataBinder<InvoiceQuery> dataBinder;

    private InvoiceService invoiceService;
    private TransferTypeService transferTypeService;

    private ReadWriteLock lock = new ReentrantReadWriteLock(true);

    public DataBinder<InvoiceQuery> getDataBinder() {
        try {
            lock.readLock().lock();
            if (dataBinder == null) {
                final LocalSettings localSettings = settingsService.getLocalSettings();
                final BeanBinder<InvoiceQuery> binder = BeanBinder.instance(InvoiceQuery.class);
                binder.registerBinder("owner", PropertyBinder.instance(AccountOwner.class, "owner", AccountOwnerConverter.instance()));
                binder.registerBinder("direction", PropertyBinder.instance(InvoiceQuery.Direction.class, "direction"));
                binder.registerBinder("status", PropertyBinder.instance(Invoice.Status.class, "status"));
                binder.registerBinder("transferType", PropertyBinder.instance(TransferType.class, "transferType", ReferenceConverter.instance(TransferType.class)));
                binder.registerBinder("period", DataBinderHelper.periodBinder(localSettings, "period"));
                binder.registerBinder("relatedOwner", PropertyBinder.instance(AccountOwner.class, "relatedMemberId", AccountOwnerConverter.zeroIsSystemInstance()));
                binder.registerBinder("description", PropertyBinder.instance(String.class, "description"));
                binder.registerBinder("transactionNumber", PropertyBinder.instance(String.class, "transactionNumber"));
                binder.registerBinder("by", PropertyBinder.instance(Element.class, "by", ReferenceConverter.instance(Element.class)));
                binder.registerBinder("pageParameters", DataBinderHelper.pageBinder());
                dataBinder = binder;
            }
            return dataBinder;
        } finally {
            lock.readLock().unlock();
        }
    }

    public InvoiceService getInvoiceService() {
        return invoiceService;
    }

    public TransferTypeService getTransferTypeService() {
        return transferTypeService;
    }

    // @Override
//    public void onLocalSettingsUpdate(final LocalSettingsEvent event) {
//        try {
//            lock.writeLock().lock();
//            super.onLocalSettingsUpdate(event);
//            dataBinder = null;
//        } finally {
//            lock.writeLock().unlock();
//        }
//    }
    @Inject
    public void setInvoiceService(final InvoiceService invoiceService) {
        this.invoiceService = invoiceService;
    }

    @Inject
    public void setTransferTypeService(final TransferTypeService transferTypeService) {
        this.transferTypeService = transferTypeService;
    }

    public static class SearchInvoicesRequest {

        private Long memberId;
        private String status;
        private String direction;
        private String begin;
        private String end;
        private Long relatedMemberId;
        private Long transferType;
        private String description;

        public Long getMemberId() {
            return memberId;
        }

        public void setMemberId(Long memberId) {
            this.memberId = memberId;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getDirection() {
            return direction;
        }

        public void setDirection(String direction) {
            this.direction = direction;
        }

        public String getBegin() {
            return begin;
        }

        public void setBegin(String begin) {
            this.begin = begin;
        }

        public String getEnd() {
            return end;
        }

        public void setEnd(String end) {
            this.end = end;
        }

        public Long getRelatedMemberId() {
            return relatedMemberId;
        }

        public void setRelatedMemberId(Long relatedMemberId) {
            this.relatedMemberId = relatedMemberId;
        }

        public Long getTransferType() {
            return transferType;
        }

        public void setTransferType(Long transferType) {
            this.transferType = transferType;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
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

    public static class SearchInvoiceResponsePostResponse extends GenericResponse {

        private List<InvoiceEntity> invoices;

        public List<InvoiceEntity> getInvoices() {
            return invoices;
        }

        public void setInvoices(List<InvoiceEntity> invoices) {
            this.invoices = invoices;
        }

    }

    public static class SearchInvoiceResponse extends GenericResponse {

        private List<Invoice.Status> invoiceList;
        private List<InvoiceQuery.Direction> dirList;
        private List<TransferTypeEntity> transferList;
        private Member member;
        private boolean myInvoices;
        private boolean byBroker;

        public Member getMember() {
            return member;
        }

        public void setMember(Member member) {
            this.member = member;
        }

        public boolean isMyInvoices() {
            return myInvoices;
        }

        public void setMyInvoices(boolean myInvoices) {
            this.myInvoices = myInvoices;
        }

        public boolean isByBroker() {
            return byBroker;
        }

        public void setByBroker(boolean byBroker) {
            this.byBroker = byBroker;
        }

        public List<TransferTypeEntity> getTransferList() {
            return transferList;
        }

        public void setTransferList(List<TransferTypeEntity> transferList) {
            this.transferList = transferList;
        }

        public List<InvoiceQuery.Direction> getDirList() {
            return dirList;
        }

        public void setDirList(List<InvoiceQuery.Direction> dirList) {
            this.dirList = dirList;
        }

        public List<Invoice.Status> getInvoiceList() {
            return invoiceList;
        }

        public void setInvoiceList(List<Invoice.Status> invoiceList) {
            this.invoiceList = invoiceList;
        }

    }

    public static class InvoiceEntity {

        private Long id;
        private Calendar date;
        private String description;
        private BigDecimal amount;
        private String from;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public Calendar getDate() {
            return date;
        }

        public void setDate(Calendar date) {
            this.date = date;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public BigDecimal getAmount() {
            return amount;
        }

        public void setAmount(BigDecimal amount) {
            this.amount = amount;
        }

        public String getFrom() {
            return from;
        }

        public void setFrom(String from) {
            this.from = from;
        }

    }

    @RequestMapping(value = "member/searchInvoices", method = RequestMethod.POST)
    @ResponseBody
    public SearchInvoiceResponsePostResponse executeQuery(@RequestBody SearchInvoicesRequest request) {
        SearchInvoiceResponsePostResponse response = new SearchInvoiceResponsePostResponse();
        final LocalSettings localSettings = settingsService.getLocalSettings();
//        Map<String, Object> queryParameters = new HashMap();
//        queryParameters.put("owner", 0L);
//        queryParameters.put("status", request.getStatus());
//        queryParameters.put("direction", request.getDirection());
//        queryParameters.put("description", request.getDescription());
//        queryParameters.put("begin", request.getBegin());
//        queryParameters.put("end", request.getEnd());
//        queryParameters.put("relatedMemberId", request.getRelatedMemberId());
//        queryParameters.put("transferType", request.getTransferType());

        final InvoiceQuery query = new InvoiceQuery();
        query.setOwner(LoggedUser.accountOwner());
        query.setStatus(Invoice.Status.valueOf(request.getStatus()));
        query.setDescription(request.getDescription());
        query.setDirection(InvoiceQuery.Direction.valueOf(request.getDirection()));
        Period period = new Period();
        period.setBegin(localSettings.getDateConverter().valueOf(request.getBegin()));
        period.setEnd(localSettings.getDateConverter().valueOf(request.getEnd()));
        query.setPeriod(period);
        query.setRelatedOwner((Member) elementService.load(request.getRelatedMemberId()));
        query.setTransferType(transferTypeService
                .load(request.getTransferType(),
                        TransferType.Relationships.FROM, TransferType.Relationships.TO));
        final List<Invoice> invoicesList = invoiceService.search(query);
        List<InvoiceEntity> invoices = new ArrayList();
        for (Invoice invoice : invoicesList) {
            InvoiceEntity invoiceEntity = new InvoiceEntity();
            invoiceEntity.setId(invoice.getId());
            invoiceEntity.setAmount(invoice.getAmount());
            invoiceEntity.setDate(invoice.getDate());
            invoiceEntity.setDescription(invoice.getDescription());
            // invoiceEntity.setFrom(invoice.getFromMember().getUsername());
            invoices.add(invoiceEntity);
        }
        response.setInvoices(invoices);
        response.setStatus(0);
        response.setMessage("Invoce list");

//        final TransformInvoiceInEntry transformer = new TransformInvoiceInEntry(query.getOwner());
//        List<Entry> entries = new TransformedIteratorList<Invoice, Entry>(transformer, invoices);
//        if (invoices instanceof Page<?>) {
//            final Page<Invoice> page = (Page<Invoice>) invoices;
//            entries = new PageImpl<Entry>(queryParameters.getPageParameters(), page.getTotalCount(), new LinkedList<Entry>(entries));
//        }
//        context.getRequest().setAttribute("invoices", entries);
        return response;
    }

    @RequestMapping(value = "member/searchInvoices", method = RequestMethod.GET)
    @ResponseBody
    public SearchInvoiceResponse prepareForm() {
        SearchInvoiceResponse response = new SearchInvoiceResponse();
        final AccountOwner owner = LoggedUser.accountOwner();
        Map<String, Object> form = new HashMap<String, Object>();
        form.put("memberId", owner instanceof Member ? ((Member) owner).getId().toString() : "0");
        form.put("direction", InvoiceQuery.Direction.INCOMING.name());
        form.put("owner", owner);

        final InvoiceQuery query = getDataBinder().readFromString(form);

        final Element loggedElement = LoggedUser.element();
        // Set the initial parameters
//        if (query.getOwner() == null) {
//            final AccountOwner owner = LoggedUser.accountOwner();
//            query.setOwner(owner);
//            form.setQuery("memberId", owner instanceof Member ? ((Member) owner).getId().toString() : "0");
//        }
//        if (query.getDirection() == null) {
//            query.setDirection(InvoiceQuery.Direction.INCOMING);
//            form.setQuery("direction", InvoiceQuery.Direction.INCOMING.name());
//        }
        if (LoggedUser.isAdministrator()) {
            AdminGroup adminGroup = LoggedUser.group();
            adminGroup = groupService.load(adminGroup.getId(), AdminGroup.Relationships.MANAGES_GROUPS);
            query.setGroups(adminGroup.getManagesGroups());
            //form.setAdvanced(true);
        }

        // Retrieve the data we need
        Member member = null;
        boolean myInvoices = false;
        boolean byBroker = false;
        boolean byOperator = false;
        // final AccountOwner owner = query.getOwner();
        if (owner instanceof SystemAccountOwner) {
            if (LoggedUser.isAdministrator()) {
                myInvoices = true;
            } else {
                throw new ValidationException();
            }
        } else {
            member = elementService.load(((Member) owner).getId(), Element.Relationships.USER);
            myInvoices = loggedElement.equals(member);
            byBroker = LoggedUser.isBroker();
            byOperator = LoggedUser.isOperator() && member.equals(((Operator) loggedElement).getMember());
            if (!LoggedUser.isAdministrator() && !myInvoices && !byBroker && !byOperator) {
                throw new ValidationException();
            }
        }

        // Search the possible transfer types
        final TransferTypeQuery ttQuery = new TransferTypeQuery();
        ttQuery.setContext(TransactionContext.PAYMENT);
        ttQuery.setFromOrToOwner(owner);
        if (LoggedUser.isAdministrator()) {
            AdminGroup adminGroup = LoggedUser.group();
            adminGroup = groupService.load(adminGroup.getId(), AdminGroup.Relationships.MANAGES_GROUPS);
            ttQuery.setFromOrToGroups(adminGroup.getManagesGroups());
        }
        final List<TransferType> transferTypes = transferTypeService.search(ttQuery);
        List<TransferTypeEntity> trasferList = new ArrayList();
        for (TransferType transfer : transferTypes) {
            TransferTypeEntity transferEntiry = new TransferTypeEntity();
            transferEntiry.setId(transfer.getId());
            transferEntiry.setName(transfer.getName());
            trasferList.add(transferEntiry);
        }

        response.setTransferList(trasferList);

        // Fetch the query entities
        if (query.getRelatedMember() instanceof EntityReference) {
            query.setRelatedOwner((Member) elementService.load(query.getRelatedMember().getId(), Element.Relationships.USER));
        }
        if (query.getTransferType() instanceof EntityReference) {
            query.setTransferType(transferTypeService.load(query.getTransferType().getId()));
        }

        // Lists the operators when member
//        if (LoggedUser.isMember()) {
//            final OperatorQuery oq = new OperatorQuery();
//            oq.setMember((Member) LoggedUser.element());
//            final List<? extends Element> operators = elementService.search(oq);
//            //request.setAttribute("operators", operators);
//        }

        response.setMyInvoices(myInvoices);
        response.setMember(member);
        response.setByBroker(byBroker);

        List<InvoiceQuery.Direction> dirList = new ArrayList();
        dirList.add(InvoiceQuery.Direction.INCOMING);
        dirList.add(InvoiceQuery.Direction.OUTGOING);
        response.setDirList(dirList);
        //retrive status
        List<Invoice.Status> invoiceList = new ArrayList();
        invoiceList.add(Invoice.Status.OPEN);
        invoiceList.add(Invoice.Status.ACCEPTED);
        invoiceList.add(Invoice.Status.CANCELLED);
        invoiceList.add(Invoice.Status.DENIED);
        invoiceList.add(Invoice.Status.EXPIRED);
        response.setInvoiceList(invoiceList);

        // Store the request attributes
        response.setStatus(0);
        response.setMessage("Invoices list");
       
        return response;
    }


}
