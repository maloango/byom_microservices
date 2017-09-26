package nl.strohalm.cyclos.webservices.rest.invoices;

import static com.google.common.util.concurrent.Striped.lock;
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

    public static class SearchInvoiceResponse extends GenericResponse {
        
     private Map<Invoice.Status, String> statusList;
        private Map<InvoiceQuery.Direction, String> directions;
        private List<TransferType> transferTypes;
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

        public List<TransferType> getTransferTypes() {
            return transferTypes;
        }

        public void setTransferTypes(List<TransferType> transferTypes) {
            this.transferTypes = transferTypes;
        }

        public Map<InvoiceQuery.Direction, String> getDirections() {
            return directions;
        }

        public void setDirections(Map<InvoiceQuery.Direction, String> directions) {
            this.directions = directions;
        }

        public Map<Invoice.Status, String> getStatusList() {
            return statusList;
        }

        public void setStatusList(Map<Invoice.Status, String> statusList) {
            this.statusList = statusList;
        }

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
       

        final InvoiceQuery query =  getDataBinder().readFromString(form);

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
        response.setMyInvoices(myInvoices);
        response.setMember(member);
        //retrive directions
        response.setByBroker(byBroker);
        response.setTransferTypes(transferTypes);
        Map<Invoice.Status, String> statusList = new HashMap();
        Map<InvoiceQuery.Direction, String> directions = new HashMap();
        directions.put(InvoiceQuery.Direction.INCOMING, "Incoming");
        directions.put(InvoiceQuery.Direction.OUTGOING, "Outgoing");
        response.setDirections(directions);
        //retrive status
        statusList.put(Invoice.Status.OPEN, "Open");
        statusList.put(Invoice.Status.ACCEPTED, "Accepted");
        statusList.put(Invoice.Status.CANCELLED, "Cancelled");
        statusList.put(Invoice.Status.DENIED, "Denied");
        statusList.put(Invoice.Status.EXPIRED, "Expired");
        response.setStatusList(statusList);
       response.setStatus(0);
       response.setMessage("Invoices list");
       return response;
    }
}
