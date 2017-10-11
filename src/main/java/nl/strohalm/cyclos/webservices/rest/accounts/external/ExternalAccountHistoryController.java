package nl.strohalm.cyclos.webservices.rest.accounts.external;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import nl.strohalm.cyclos.access.AdminSystemPermission;

import org.springframework.stereotype.Controller;

import nl.strohalm.cyclos.annotations.Inject;
import nl.strohalm.cyclos.controls.ActionContext;
import nl.strohalm.cyclos.entities.accounts.external.ExternalAccount;
import nl.strohalm.cyclos.entities.accounts.external.ExternalTransfer;
import nl.strohalm.cyclos.entities.accounts.external.ExternalTransfer.SummaryStatus;
import nl.strohalm.cyclos.entities.accounts.external.ExternalTransferImport;
import nl.strohalm.cyclos.entities.accounts.external.ExternalTransferQuery;
import nl.strohalm.cyclos.entities.accounts.external.ExternalTransferType;
import nl.strohalm.cyclos.entities.members.Member;
import nl.strohalm.cyclos.entities.settings.LocalSettings;
import nl.strohalm.cyclos.entities.settings.events.LocalSettingsChangeListener;
import nl.strohalm.cyclos.entities.settings.events.LocalSettingsEvent;
import nl.strohalm.cyclos.services.accounts.external.ExternalAccountService;
import nl.strohalm.cyclos.services.accounts.external.ExternalTransferImportService;
import nl.strohalm.cyclos.services.accounts.external.ExternalTransferService;
import nl.strohalm.cyclos.services.permissions.PermissionService;
import nl.strohalm.cyclos.services.settings.SettingsService;
import nl.strohalm.cyclos.services.transactions.NegativeAllowedTransactionSummaryVO;
import nl.strohalm.cyclos.services.transactions.TransactionSummaryVO;
import nl.strohalm.cyclos.utils.BigDecimalHelper;
import nl.strohalm.cyclos.utils.RelationshipHelper;
import nl.strohalm.cyclos.utils.binding.BeanBinder;
import nl.strohalm.cyclos.utils.binding.DataBinder;
import nl.strohalm.cyclos.utils.binding.DataBinderHelper;
import nl.strohalm.cyclos.utils.binding.PropertyBinder;
import nl.strohalm.cyclos.utils.conversion.ReferenceConverter;
import nl.strohalm.cyclos.utils.query.QueryParameters;
import nl.strohalm.cyclos.utils.validation.ValidationException;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;
import nl.strohalm.cyclos.webservices.rest.GenericResponse;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class ExternalAccountHistoryController extends BaseRestController {
    
    public static class ExternalTransferTypeEntity {

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
    
    public static class ExternalAccountEntity {

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
    
    public static class ExternalAccountHistoryResponse extends GenericResponse {
        
        private List<ExternalTransferTypeEntity> externalAccounts;
        private ExternalAccountEntity externalAccount;
        private Set<Entry<String,ExternalTransfer.SummaryStatus>>statusSet;
        private boolean process;
        private boolean check;
        private boolean managePayment;

        public Set<Entry<String, SummaryStatus>> getStatusSet() {
            return statusSet;
        }

        public void setStatusSet(Set<Entry<String, SummaryStatus>> statusSet) {
            this.statusSet = statusSet;
        }
        
      
        
        public ExternalAccountEntity getExternalAccount() {
            return externalAccount;
        }
        
        public void setExternalAccount(ExternalAccountEntity externalAccount) {
            this.externalAccount = externalAccount;
        }
        
        public List<ExternalTransferTypeEntity> getExternalAccounts() {
            return externalAccounts;
        }
        
        public void setExternalAccounts(List<ExternalTransferTypeEntity> externalAccounts) {
            this.externalAccounts = externalAccounts;
        }
        
        public boolean isProcess() {
            return process;
        }
        
        public void setProcess(boolean process) {
            this.process = process;
        }
        
        public boolean isCheck() {
            return check;
        }
        
        public void setCheck(boolean check) {
            this.check = check;
        }
        
        public boolean isManagePayment() {
            return managePayment;
        }
        
        public void setManagePayment(boolean managePayment) {
            this.managePayment = managePayment;
        }
        
    }
    
    @RequestMapping(value = "admin/externalAccountHistory/{accountId}/{importId}", method = RequestMethod.GET)
    @ResponseBody
    public ExternalAccountHistoryResponse executeQuery(@PathVariable("accountId") Long accountId, @PathVariable("importId") Long importId) {
        // Retrieve the query parameters
        ExternalAccountHistoryResponse response = new ExternalAccountHistoryResponse();
        ExternalAccount externalAccount = null;
        final Long transferImportId = importId;
        if (transferImportId != null && transferImportId > 0) {
            final ExternalTransferImport transferImport = externalTransferImportService.load(transferImportId, RelationshipHelper.nested(ExternalTransferImport.Relationships.ACCOUNT, ExternalAccount.Relationships.TYPES));
//            query.setTransferImport(transferImport);
            externalAccount = transferImport.getAccount();
            //response.setTransferImport(transferImport);
        } else {
            final Long externalAccountId = accountId;
            if (externalAccountId == null) {
                throw new ValidationException();
            }
            externalAccount = externalAccountService.load(externalAccountId, ExternalAccount.Relationships.TYPES);
        }
        ExternalAccountEntity externalAccountEntiy = new ExternalAccountEntity();
        externalAccountEntiy.setId(externalAccount.getId());
        externalAccountEntiy.setName(externalAccount.getName());
        response.setExternalAccount(externalAccountEntiy);
//      query.setAccount(externalAccount);
        final Collection<ExternalTransferType> types = externalAccount.getTypes();
        final boolean process = permissionService.hasPermission(AdminSystemPermission.EXTERNAL_ACCOUNTS_PROCESS_PAYMENT);
        final boolean check = permissionService.hasPermission(AdminSystemPermission.EXTERNAL_ACCOUNTS_CHECK_PAYMENT);
        final boolean managePayment = permissionService.hasPermission(AdminSystemPermission.EXTERNAL_ACCOUNTS_MANAGE_PAYMENT);
        List<ExternalTransferTypeEntity> externalTransferTypeList = new ArrayList();
        for (ExternalTransferType tt : types) {
            ExternalTransferTypeEntity externalTransferTypeEntity = new ExternalTransferTypeEntity();
            externalTransferTypeEntity.setId(tt.getId());
            externalTransferTypeEntity.setName(tt.getName());
            externalTransferTypeList.add(externalTransferTypeEntity);
        }
        response.setProcess(process);
        response.setManagePayment(managePayment);
        response.setCheck(check);
        response.setExternalAccounts(externalTransferTypeList);
        // response.setTypes(types);
//    RequestHelper.storeEnum(request, ExternalTransfer.SummaryStatus.class, "statusList");
        Map<String, ExternalTransfer.SummaryStatus> statusList = new HashMap();
        Set<Entry<String,ExternalTransfer.SummaryStatus>>statusSet=statusList.entrySet();

        statusList.put("total", SummaryStatus.TOTAL);
        statusList.put("checked", SummaryStatus.CHECKED);
        statusList.put("complete_pending", SummaryStatus.COMPLETE_PENDING);
        statusList.put("incomplete_pending", SummaryStatus.INCOMPLETE_PENDING);
        statusList.put("processed", SummaryStatus.PROCESSED);
        response.setStatusSet(statusSet);

//        final List<ExternalTransferAction> listPossibleActions = new ArrayList<ExternalTransferAction>();
//        if (check) {
//            listPossibleActions.add(ExternalTransferAction.MARK_AS_CHECKED);
//            listPossibleActions.add(ExternalTransferAction.MARK_AS_UNCHECKED);
//        }
//        if (managePayment) {
//            listPossibleActions.add(ExternalTransferAction.DELETE);
//        }
//        request.setAttribute("possibleActions", listPossibleActions);
//
//        getDataBinder().writeAsString(form.getQuery(), query);
        response.setStatus(0);
        response.setMessage("External account ");
        return response;
    }
    
}
