package nl.strohalm.cyclos.webservices.rest.loans;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import nl.strohalm.cyclos.access.AdminMemberPermission;

import nl.strohalm.cyclos.access.AdminSystemPermission;
import nl.strohalm.cyclos.annotations.Inject;
import nl.strohalm.cyclos.controls.ActionContext;
//import nl.strohalm.cyclos.controls.loans.SearchLoanPaymentsForm;
import nl.strohalm.cyclos.entities.accounts.MemberAccount;
import nl.strohalm.cyclos.entities.accounts.loans.Loan;
import nl.strohalm.cyclos.entities.accounts.loans.LoanGroup;
import nl.strohalm.cyclos.entities.accounts.loans.LoanGroupQuery;
import nl.strohalm.cyclos.entities.accounts.loans.LoanPayment;
import nl.strohalm.cyclos.entities.accounts.loans.LoanPaymentQuery;
import nl.strohalm.cyclos.entities.accounts.loans.LoanQuery;
import nl.strohalm.cyclos.entities.accounts.transactions.Payment;
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
import nl.strohalm.cyclos.utils.binding.BeanBinder;
import nl.strohalm.cyclos.utils.binding.BeanCollectionBinder;
import nl.strohalm.cyclos.utils.binding.DataBinder;
import nl.strohalm.cyclos.utils.binding.DataBinderHelper;
import nl.strohalm.cyclos.utils.binding.MapBean;
import nl.strohalm.cyclos.utils.binding.PropertyBinder;
import nl.strohalm.cyclos.utils.binding.SimpleCollectionBinder;
import nl.strohalm.cyclos.utils.conversion.ReferenceConverter;
import nl.strohalm.cyclos.utils.query.QueryParameters;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;
import nl.strohalm.cyclos.webservices.rest.GenericResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class SearchLoanPaymentsController extends BaseRestController {

    private DataBinder<LoanPaymentQuery> dataBinder;

    public DataBinder<LoanPaymentQuery> getDataBinder() {
        if (dataBinder == null) {
            final LocalSettings settings = settingsService.getLocalSettings();
            dataBinder = loanPaymentQueryDataBinder(settings);
        }
        return dataBinder;
    }

    public static class SearchLoanPaymentResponse extends GenericResponse {

        private Set<LoanQuery.QueryStatus> queryStatus;
        private List<Loan> loans;
        private List<LoanGroupEntity> loanGroups;
        private List<LoanPayment.Status> statusList;

        public List<LoanPayment.Status> getStatusList() {
            return statusList;
        }

        public void setStatusList(List<LoanPayment.Status> statusList) {
            this.statusList = statusList;
        }

        public List<LoanGroupEntity> getLoanGroups() {
            return loanGroups;
        }

        public void setLoanGroups(List<LoanGroupEntity> loanGroups) {
            this.loanGroups = loanGroups;
        }

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

    public static class LoanGroupEntity {

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

    @RequestMapping(value = "admin/searchLoanPayments", method = RequestMethod.GET)
    @ResponseBody
    public SearchLoanPaymentResponse prepareForm() {
        List<LoanGroupEntity> loanGroups = new ArrayList();
        SearchLoanPaymentResponse response = new SearchLoanPaymentResponse();
        final Set<LoanQuery.QueryStatus> queryStatus = EnumSet.allOf(LoanQuery.QueryStatus.class);
        // When there is no permission to view authorized loans, remove the statuses which are related to authorization
        if (!permissionService.hasPermission(AdminMemberPermission.LOANS_VIEW_AUTHORIZED)) {
            for (final Iterator<LoanQuery.QueryStatus> it = queryStatus.iterator(); it.hasNext();) {
                if (it.next().isAuthorizationRelated()) {
                    it.remove();
                }
            }
        }
        if (permissionService.hasPermission(AdminSystemPermission.LOAN_GROUPS_VIEW)) {
            // Retrieve a list of all loan groups
            final LoanGroupQuery lgQuery = new LoanGroupQuery();
            // request.setAttribute("loanGroups", loanGroupService.search(lgQuery));

            for (LoanGroup group : loanGroupService.search(lgQuery)) {
                LoanGroupEntity entity = new LoanGroupEntity();
                entity.setId(group.getId());
                entity.setName(group.getName());
                loanGroups.add(entity);
            }
            response.setLoanGroups(loanGroups);

        } else {
            response.setLoanGroups(loanGroups);
        }

        //RequestHelper.storeEnum(request, LoanPayment.Status.class, "status");
        List<LoanPayment.Status> status = new ArrayList();
        status.add(LoanPayment.Status.OPEN);
        status.add(LoanPayment.Status.DISCARDED);
        status.add(LoanPayment.Status.EXPIRED);
        status.add(LoanPayment.Status.IN_PROCESS);
        status.add(LoanPayment.Status.RECOVERED);
        status.add(LoanPayment.Status.REPAID);
        status.add(LoanPayment.Status.UNRECOVERABLE);
        response.setStatusList(status);

        response.setQueryStatus(queryStatus);
        response.setStatus(0);
        response.setMessage("");
        return response;

    }

    public static class SearchLoanPaymentParameters {

        private List<String> statusList;
        private Long loanGroup;
        private Long transferType;
        private Long member;
        private Long broker;
        private String expirationPeriodBegin;
        private String expirationPeriodEnd;
        private String repaymentPeriodBegin;
        private String repaymentPeriodEnd;

        public List<String> getStatusList() {
            return statusList;
        }

        public void setStatusList(List<String> statusList) {
            this.statusList = statusList;
        }

       

        public Long getLoanGroup() {
            return loanGroup;
        }

        public void setLoanGroup(Long loanGroup) {
            this.loanGroup = loanGroup;
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

        public String getRepaymentPeriodBegin() {
            return repaymentPeriodBegin;
        }

        public void setRepaymentPeriodBegin(String repaymentPeriodBegin) {
            this.repaymentPeriodBegin = repaymentPeriodBegin;
        }

        public String getRepaymentPeriodEnd() {
            return repaymentPeriodEnd;
        }

        public void setRepaymentPeriodEnd(String repaymentPeriodEnd) {
            this.repaymentPeriodEnd = repaymentPeriodEnd;
        }

    }

    public static class SearchLoanData extends GenericResponse {

        List<LoanPaymentEntity> loanPaymentList;

        public List<LoanPaymentEntity> getLoanPaymentList() {
            return loanPaymentList;
        }

        public void setLoanPaymentList(List<LoanPaymentEntity> loanPaymentList) {
            this.loanPaymentList = loanPaymentList;
        }

    }

    public static class LoanPaymentEntity {

        private Long id;
        private BigDecimal amount;
        private LoanPayment.Status loanStatus;
        private BigDecimal repaidAmount = BigDecimal.ZERO;
        private Calendar repaymentDate;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public BigDecimal getAmount() {
            return amount;
        }

        public void setAmount(BigDecimal amount) {
            this.amount = amount;
        }

        public LoanPayment.Status getLoanStatus() {
            return loanStatus;
        }

        public void setLoanStatus(LoanPayment.Status loanStatus) {
            this.loanStatus = loanStatus;
        }

        public BigDecimal getRepaidAmount() {
            return repaidAmount;
        }

        public void setRepaidAmount(BigDecimal repaidAmount) {
            this.repaidAmount = repaidAmount;
        }

        public Calendar getRepaymentDate() {
            return repaymentDate;
        }

        public void setRepaymentDate(Calendar repaymentDate) {
            this.repaymentDate = repaymentDate;
        }

    }

    @RequestMapping(value = "admin/searchLoanPayments", method = RequestMethod.POST)
    @ResponseBody
    public SearchLoanData search(@RequestBody SearchLoanPaymentParameters params) {
        SearchLoanData response = new SearchLoanData();
        final LocalSettings settings = settingsService.getLocalSettings();
        Map<String, Object> queryParam = new HashMap();
        queryParam.put("statusList", params.getStatusList());
        queryParam.put("transferType", params.getTransferType());
        queryParam.put("member", params.getMember());
        queryParam.put("broker", params.getBroker());
        queryParam.put("loanGroup", params.getLoanGroup());
        Period expPeriod = new Period();
        expPeriod.setBegin(settings.getDateConverter().valueOf(params.getExpirationPeriodBegin()));
        expPeriod.setEnd(settings.getDateConverter().valueOf(params.getExpirationPeriodEnd()));
        queryParam.put("expirationPeriod", expPeriod);

        Period paymentPeriod = new Period();
        paymentPeriod.setBegin(settings.getDateConverter().valueOf(params.getRepaymentPeriodBegin()));
        paymentPeriod.setEnd(settings.getDateConverter().valueOf(params.getRepaymentPeriodEnd()));
        queryParam.put("expirationPeriod", paymentPeriod);

        final LoanPaymentQuery query = getDataBinder().readFromString(queryParam);
        final List<LoanPayment> loanPayments = loanService.search(query);
        List<LoanPaymentEntity> loanPaymentList = new ArrayList();
        for (LoanPayment loan : loanPayments) {
            LoanPaymentEntity entity = new LoanPaymentEntity();
            entity.setId(loan.getId());
            entity.setAmount(loan.getAmount());
            entity.setLoanStatus(loan.getStatus());
            entity.setRepaidAmount(loan.getRepaidAmount());
            entity.setRepaymentDate(loan.getRepaymentDate());
            loanPaymentList.add(entity);
        }
        response.setLoanPaymentList(loanPaymentList);
        response.setStatus(0);
        return response;

    }

    public static DataBinder<LoanPaymentQuery> loanPaymentQueryDataBinder(final LocalSettings localSettings) {
        final BeanBinder<MemberCustomFieldValue> memberCustomValueBinder = BeanBinder.instance(MemberCustomFieldValue.class);
        memberCustomValueBinder.registerBinder("field", PropertyBinder.instance(MemberCustomField.class, "field", ReferenceConverter.instance(MemberCustomField.class)));
        memberCustomValueBinder.registerBinder("value", PropertyBinder.instance(String.class, "value"));

        final BeanBinder<PaymentCustomFieldValue> loanCustomValueBinder = BeanBinder.instance(PaymentCustomFieldValue.class);
        loanCustomValueBinder.registerBinder("field", PropertyBinder.instance(PaymentCustomField.class, "field", ReferenceConverter.instance(PaymentCustomField.class)));
        loanCustomValueBinder.registerBinder("value", PropertyBinder.instance(String.class, "value"));

        final BeanBinder<LoanPaymentQuery> binder = BeanBinder.instance(LoanPaymentQuery.class);
        binder.registerBinder("statusList", SimpleCollectionBinder.instance(LoanPayment.Status.class, "statusList"));
        binder.registerBinder("transferType", PropertyBinder.instance(TransferType.class, "transferType"));
        binder.registerBinder("member", PropertyBinder.instance(Member.class, "member", ReferenceConverter.instance(Member.class)));
        binder.registerBinder("broker", PropertyBinder.instance(Member.class, "broker", ReferenceConverter.instance(Member.class)));
        binder.registerBinder("loanGroup", PropertyBinder.instance(LoanGroup.class, "loanGroup", ReferenceConverter.instance(LoanGroup.class)));
        binder.registerBinder("memberCustomValues", BeanCollectionBinder.instance(memberCustomValueBinder, "memberValues"));
        binder.registerBinder("loanCustomValues", BeanCollectionBinder.instance(loanCustomValueBinder, "loanValues"));
        binder.registerBinder("expirationPeriod", DataBinderHelper.periodBinder(localSettings, "expirationPeriod"));
        binder.registerBinder("repaymentPeriod", DataBinderHelper.periodBinder(localSettings, "repaymentPeriod"));
        binder.registerBinder("pageParameters", DataBinderHelper.pageBinder());
        return binder;
    }

}
