package nl.strohalm.cyclos.webservices.rest.invoices;

import static com.google.common.util.concurrent.Striped.lock;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import javax.servlet.http.HttpServletRequest;

import nl.strohalm.cyclos.annotations.Inject;
import nl.strohalm.cyclos.controls.ActionContext;
import nl.strohalm.cyclos.entities.EntityReference;
import nl.strohalm.cyclos.entities.accounts.AccountOwner;
import nl.strohalm.cyclos.entities.accounts.SystemAccountOwner;
import nl.strohalm.cyclos.entities.accounts.transactions.Invoice;
import nl.strohalm.cyclos.entities.accounts.transactions.InvoiceQuery;
import nl.strohalm.cyclos.entities.accounts.transactions.TransferType;
import nl.strohalm.cyclos.entities.accounts.transactions.TransferTypeQuery;
import nl.strohalm.cyclos.entities.customization.fields.PaymentCustomField;
import nl.strohalm.cyclos.entities.groups.AdminGroup;
import nl.strohalm.cyclos.entities.groups.MemberGroup;
import nl.strohalm.cyclos.entities.members.Element;
import nl.strohalm.cyclos.entities.members.Member;
import nl.strohalm.cyclos.entities.members.Operator;
import nl.strohalm.cyclos.entities.members.OperatorQuery;
import nl.strohalm.cyclos.entities.settings.LocalSettings;
import nl.strohalm.cyclos.entities.settings.events.LocalSettingsEvent;
import nl.strohalm.cyclos.exceptions.PermissionDeniedException;
import nl.strohalm.cyclos.services.elements.ElementService;
import nl.strohalm.cyclos.services.groups.GroupService;
import nl.strohalm.cyclos.services.settings.SettingsService;
import nl.strohalm.cyclos.services.transactions.InvoiceService;
import nl.strohalm.cyclos.services.transactions.TransactionContext;
import nl.strohalm.cyclos.services.transfertypes.TransferTypeService;
import nl.strohalm.cyclos.utils.Period;
import nl.strohalm.cyclos.utils.RequestHelper;
import nl.strohalm.cyclos.utils.TransformedIteratorList;
import nl.strohalm.cyclos.utils.access.LoggedUser;
import nl.strohalm.cyclos.utils.binding.BeanBinder;
import nl.strohalm.cyclos.utils.binding.DataBinder;
import nl.strohalm.cyclos.utils.binding.DataBinderHelper;
import nl.strohalm.cyclos.utils.binding.PropertyBinder;
import nl.strohalm.cyclos.utils.conversion.AccountOwnerConverter;
import nl.strohalm.cyclos.utils.conversion.CoercionHelper;
import nl.strohalm.cyclos.utils.conversion.ReferenceConverter;
import nl.strohalm.cyclos.utils.conversion.Transformer;
import nl.strohalm.cyclos.utils.query.Page;
import nl.strohalm.cyclos.utils.query.PageImpl;
import nl.strohalm.cyclos.utils.query.QueryParameters;
import nl.strohalm.cyclos.utils.validation.ValidationException;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;
import nl.strohalm.cyclos.webservices.rest.GenericResponse;
import static org.apache.commons.httpclient.util.URIUtil.getQuery;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class SearchInvoicesController extends BaseRestController {

    private DataBinder<InvoiceQuery> dataBinder;

    public DataBinder<InvoiceQuery> getDataBinder() {
        try {

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

        }
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

        private List<Invoice> invoices;

        public List<Invoice> getInvoices() {
            return invoices;
        }

        public void setInvoices(List<Invoice> invoices) {
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

    @RequestMapping(value = "admin/searchInvoices", method = RequestMethod.POST)
    @ResponseBody
    protected SearchInvoiceResponsePostResponse executeQuery(@RequestBody SearchInvoicesRequest request) {
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
//        query.setRelatedOwner();
        query.setTransferType(transferTypeService
                .load(request.getTransferType(),
                        TransferType.Relationships.FROM));
        final List<Invoice> invoices = invoiceService.search(query);
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

    @RequestMapping(value = "admin/searchInvoices", method = RequestMethod.GET)
    @ResponseBody
    public SearchInvoiceResponse prepareForm() {
        SearchInvoiceResponse response = new SearchInvoiceResponse();
//        final HttpServletRequest request = context.getRequest();
//        final SearchInvoicesForm form = context.getForm();
        final AccountOwner owner = LoggedUser.accountOwner();
        Map<String, Object> form = new HashMap<String, Object>();
        form.put("memberId", owner instanceof Member ? ((Member) owner).getId().toString() : "0");
        form.put("direction", InvoiceQuery.Direction.INCOMING.name());
        form.put("owner", owner);

        final InvoiceQuery query = getDataBinder().readFromString(form);

        final Element loggedElement = LoggedUser.element();
//         Set the initial parameters
//        if (query.getOwner() == null) {
//          
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
        if (LoggedUser.isMember()) {
            final OperatorQuery oq = new OperatorQuery();
            oq.setMember((Member) LoggedUser.element());
            final List<? extends Element> operators = elementService.search(oq);
//            request.setAttribute("operators", operators);
        }

        // Store the request attributes
//        request.setAttribute("myInvoices", myInvoices);
//        request.setAttribute("member", member);
//        request.setAttribute("byBroker", byBroker);
//        request.setAttribute("transferTypes", transferTypes);
//        RequestHelper.storeEnum(request, InvoiceQuery.Direction.class, "directions");
//        RequestHelper.storeEnum(request, Invoice.Status.class, "status");
//        response.setTransferTypes(transferTypes);
        response.setMyInvoices(myInvoices);
        response.setMember(member);
        //retrive directions
        response.setByBroker(byBroker);
//        response.setTransferTypes(transferTypes);
//        Map<Invoice.Status, String> statusList = new HashMap();
//        Map<InvoiceQuery.Direction, String> directions = new HashMap();
//        directions.put(InvoiceQuery.Direction.INCOMING, "Incoming");
//        directions.put(InvoiceQuery.Direction.OUTGOING, "Outgoing");

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
//        statusList.put(Invoice.Status.OPEN, "Open");
//        statusList.put(Invoice.Status.ACCEPTED, "Accepted");
//        statusList.put(Invoice.Status.CANCELLED, "Cancelled");
//        statusList.put(Invoice.Status.DENIED, "Denied");
//        statusList.put(Invoice.Status.EXPIRED, "Expired");
//        response.setStatusList(statusList);
        response.setStatus(0);
        response.setMessage("Invoices list");
        return response;
    }
}
