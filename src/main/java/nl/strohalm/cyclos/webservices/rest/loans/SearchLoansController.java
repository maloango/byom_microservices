package nl.strohalm.cyclos.webservices.rest.loans;

import com.sun.corba.se.pept.broker.Broker;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import nl.strohalm.cyclos.access.AdminMemberPermission;
import nl.strohalm.cyclos.access.AdminSystemPermission;
import nl.strohalm.cyclos.annotations.Inject;
import nl.strohalm.cyclos.controls.ActionContext;
//import nl.strohalm.cyclos.controls.loans.SearchLoansForm;
import nl.strohalm.cyclos.entities.accounts.MemberAccount;
import nl.strohalm.cyclos.entities.accounts.loans.Loan;
import nl.strohalm.cyclos.entities.accounts.loans.LoanGroup;
import nl.strohalm.cyclos.entities.accounts.loans.LoanGroupQuery;
import nl.strohalm.cyclos.entities.accounts.loans.LoanQuery;
import nl.strohalm.cyclos.entities.accounts.transactions.Payment;
import nl.strohalm.cyclos.entities.accounts.transactions.Transfer;
import nl.strohalm.cyclos.entities.accounts.transactions.TransferType;
import nl.strohalm.cyclos.entities.accounts.transactions.TransferTypeQuery;
import nl.strohalm.cyclos.entities.customization.fields.MemberCustomField;
import nl.strohalm.cyclos.entities.customization.fields.MemberCustomFieldValue;
import nl.strohalm.cyclos.entities.customization.fields.PaymentCustomField;
import nl.strohalm.cyclos.entities.customization.fields.PaymentCustomFieldValue;
import nl.strohalm.cyclos.entities.groups.AdminGroup;
import nl.strohalm.cyclos.entities.members.Element;
import nl.strohalm.cyclos.entities.members.Member;
import nl.strohalm.cyclos.entities.settings.LocalSettings;
import nl.strohalm.cyclos.services.customization.MemberCustomFieldService;
import nl.strohalm.cyclos.services.customization.PaymentCustomFieldService;
import nl.strohalm.cyclos.services.elements.ElementService;
import nl.strohalm.cyclos.services.groups.GroupService;
import nl.strohalm.cyclos.services.loangroups.LoanGroupService;
import nl.strohalm.cyclos.services.permissions.PermissionService;
import nl.strohalm.cyclos.services.settings.SettingsService;
import nl.strohalm.cyclos.services.transactions.LoanService;
import nl.strohalm.cyclos.services.transactions.TransactionContext;
import nl.strohalm.cyclos.services.transfertypes.TransferTypeService;
import nl.strohalm.cyclos.utils.CustomFieldHelper;
import nl.strohalm.cyclos.utils.Period;
import nl.strohalm.cyclos.utils.RelationshipHelper;
import nl.strohalm.cyclos.utils.RequestHelper;
import nl.strohalm.cyclos.utils.access.LoggedUser;
import nl.strohalm.cyclos.utils.binding.BeanBinder;
import nl.strohalm.cyclos.utils.binding.BeanCollectionBinder;
import nl.strohalm.cyclos.utils.binding.DataBinder;
import nl.strohalm.cyclos.utils.binding.DataBinderHelper;
import nl.strohalm.cyclos.utils.binding.MapBean;
import nl.strohalm.cyclos.utils.binding.PropertyBinder;
import nl.strohalm.cyclos.utils.conversion.ReferenceConverter;
import nl.strohalm.cyclos.utils.query.QueryParameters;
import nl.strohalm.cyclos.utils.validation.ValidationException;
import nl.strohalm.cyclos.webservices.model.TransferTypeVO;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;
import nl.strohalm.cyclos.webservices.rest.GenericResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class SearchLoansController extends BaseRestController {

    public static class SearchLoansResponse extends GenericResponse {

        private Set<LoanQuery.QueryStatus> queryStatus;
        private List<Loan> loans;

        public List<Loan> getLoans() {
            return loans;
        }

        public void setLoans(List<Loan> loans) {
            this.loans = loans;
        }
        

        public Set<LoanQuery.QueryStatus> getQueryStatus() {
            return queryStatus;
        }

        public void setQueryStatus(Set<LoanQuery.QueryStatus> queryStatus) {
            this.queryStatus = queryStatus;
        }

    }

    public static class SearchLoansParameters {

        private String queryStatus;
        private Long transferType;
        private Long member;
        private Long broker;
        private String grantPeriodBegin;
        private String grantPeriodEnd;
        private String expirationPeriodBegin;
        private String expirationPeriodEnd;
        private String paymentPeriodBegin;
        private String paymentPeriodEnd;

        public String getQueryStatus() {
            return queryStatus;
        }

        public void setQueryStatus(String queryStatus) {
            this.queryStatus = queryStatus;
        }

        public Long getTransferType() {
            return transferType;
        }

        public void setTransferType(Long transferType) {
            this.transferType = transferType;
        }

        public Long getMember() {
            return member;
        }

        public void setMember(Long member) {
            this.member = member;
        }

        public Long getBroker() {
            return broker;
        }

        public void setBroker(Long broker) {
            this.broker = broker;
        }

        public String getGrantPeriodBegin() {
            return grantPeriodBegin;
        }

        public void setGrantPeriodBegin(String grantPeriodBegin) {
            this.grantPeriodBegin = grantPeriodBegin;
        }

        public String getGrantPeriodEnd() {
            return grantPeriodEnd;
        }

        public void setGrantPeriodEnd(String grantPeriodEnd) {
            this.grantPeriodEnd = grantPeriodEnd;
        }

        public String getExpirationPeriodBegin() {
            return expirationPeriodBegin;
        }

        public void setExpirationPeriodBegin(String expirationPeriodBegin) {
            this.expirationPeriodBegin = expirationPeriodBegin;
        }

        public String getExpirationPeriodEnd() {
            return expirationPeriodEnd;
        }

        public void setExpirationPeriodEnd(String expirationPeriodEnd) {
            this.expirationPeriodEnd = expirationPeriodEnd;
        }

        public String getPaymentPeriodBegin() {
            return paymentPeriodBegin;
        }

        public void setPaymentPeriodBegin(String paymentPeriodBegin) {
            this.paymentPeriodBegin = paymentPeriodBegin;
        }

        public String getPaymentPeriodEnd() {
            return paymentPeriodEnd;
        }

        public void setPaymentPeriodEnd(String paymentPeriodEnd) {
            this.paymentPeriodEnd = paymentPeriodEnd;
        }

    }

    @RequestMapping(value = "admin/searchLoans", method = RequestMethod.GET)
    @ResponseBody
    public SearchLoansResponse prepareForm() {

        SearchLoansResponse response = new SearchLoansResponse();
        final Set<LoanQuery.QueryStatus> queryStatus = EnumSet.allOf(LoanQuery.QueryStatus.class);
        // When there is no permission to view authorized loans, remove the statuses which are related to authorization
        if (!permissionService.hasPermission(AdminMemberPermission.LOANS_VIEW_AUTHORIZED)) {
            for (final Iterator<LoanQuery.QueryStatus> it = queryStatus.iterator(); it.hasNext();) {
                if (it.next().isAuthorizationRelated()) {
                    it.remove();
                }
            }
        }
        response.setQueryStatus(queryStatus);
        response.setStatus(0);
        response.setMessage("");
        return response;

    }
    

    @RequestMapping(value = "admin/searchLoans", method = RequestMethod.GET)
    @ResponseBody
    public SearchLoansResponse searchLoans(@RequestBody SearchLoansParameters params) {
        SearchLoansResponse response=new SearchLoansResponse();
        final LocalSettings localSettings = settingsService.getLocalSettings();
//        binder.registerBinder("status", PropertyBinder.instance(Loan.Status.class, "status"));
//        binder.registerBinder("queryStatus", PropertyBinder.instance(LoanQuery.QueryStatus.class, "queryStatus"));
//        binder.registerBinder("transferStatus", PropertyBinder.instance(Transfer.Status.class, "transferStatus"));
//        binder.registerBinder("transferType", PropertyBinder.instance(TransferType.class, "transferType"));
//        binder.registerBinder("member", PropertyBinder.instance(Member.class, "member", ReferenceConverter.instance(Member.class)));
//        binder.registerBinder("broker", PropertyBinder.instance(Member.class, "broker", ReferenceConverter.instance(Member.class)));
//        binder.registerBinder("loanGroup", PropertyBinder.instance(LoanGroup.class, "loanGroup", ReferenceConverter.instance(LoanGroup.class)));
//        binder.registerBinder("memberCustomValues", BeanCollectionBinder.instance(memberCustomValueBinder, "memberValues"));
//        binder.registerBinder("loanCustomValues", BeanCollectionBinder.instance(loanCustomValueBinder, "loanValues"));
//        binder.registerBinder("grantPeriod", DataBinderHelper.periodBinder(localSettings, "grantPeriod"));
//        binder.registerBinder("expirationPeriod", DataBinderHelper.periodBinder(localSettings, "expirationPeriod"));
//        binder.registerBinder("paymentPeriod", DataBinderHelper.periodBinder(localSettings, "paymentPeriod"));
//        binder.registerBinder("transactionNumber", PropertyBinder.instance(String.class, "transactionNumber"));
//        binder.registerBinder("pageParameters", DataBinderHelper.pageBinder());
        final LoanQuery query = new LoanQuery();
        query.setQueryStatus(LoanQuery.QueryStatus.valueOf(params.getQueryStatus()));
        query.setTransferType(transferTypeService.load(params.getTransferType(), TransferType.Relationships.FROM, TransferType.Relationships.TO));
        query.setMember((Member) elementService.load(params.getMember(), Element.Relationships.USER));
        query.setBroker((Member) elementService.load(params.getBroker(), Element.Relationships.USER));

        Period grantPeriod = new Period();
        grantPeriod.setBegin(localSettings.getDateConverter().valueOf(params.getGrantPeriodBegin()));
        grantPeriod.setEnd(localSettings.getDateConverter().valueOf(params.getGrantPeriodEnd()));

        Period expirePeriod = new Period();
        expirePeriod.setBegin(localSettings.getDateConverter().valueOf(params.getExpirationPeriodBegin()));
        expirePeriod.setEnd(localSettings.getDateConverter().valueOf(params.getExpirationPeriodEnd()));

        Period paymentPeriod = new Period();
        paymentPeriod.setBegin(localSettings.getDateConverter().valueOf(params.getPaymentPeriodBegin()));
        paymentPeriod.setEnd(localSettings.getDateConverter().valueOf(params.getPaymentPeriodEnd()));
        
        query.setGrantPeriod(grantPeriod);
        query.setExpirationPeriod(expirePeriod);
        query.setPaymentPeriod(paymentPeriod);
        final List<Loan> loans = loanService.search(query);
        response.setLoans(loans);
        response.setStatus(0);
        response.setMessage("List of loans");
        return response;
    }

}
