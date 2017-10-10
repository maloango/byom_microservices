package nl.strohalm.cyclos.webservices.rest.accounts.external;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

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

    public static class ExternalAccountHistoryResponse extends GenericResponse {

        private ExternalTransferImport transferImport;
        private ExternalAccount externalAccount;
        private Collection<ExternalTransferType> types;
        private boolean process;
        private boolean check;
        private boolean managePayment;

        public ExternalTransferImport getTransferImport() {
            return transferImport;
        }

        public void setTransferImport(ExternalTransferImport transferImport) {
            this.transferImport = transferImport;
        }

        public ExternalAccount getExternalAccount() {
            return externalAccount;
        }

        public void setExternalAccount(ExternalAccount externalAccount) {
            this.externalAccount = externalAccount;
        }

        public Collection<ExternalTransferType> getTypes() {
            return types;
        }

        public void setTypes(Collection<ExternalTransferType> types) {
            this.types = types;
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
            response.setTransferImport(transferImport);
        } else {
            final Long externalAccountId = accountId;
            if (externalAccountId == null) {
                throw new ValidationException();
            }
            externalAccount = externalAccountService.load(externalAccountId, ExternalAccount.Relationships.TYPES);
        }
//        query.setAccount(externalAccount);
        final Collection<ExternalTransferType> types = externalAccount.getTypes();
        final boolean process = permissionService.hasPermission(AdminSystemPermission.EXTERNAL_ACCOUNTS_PROCESS_PAYMENT);
        final boolean check = permissionService.hasPermission(AdminSystemPermission.EXTERNAL_ACCOUNTS_CHECK_PAYMENT);
        final boolean managePayment = permissionService.hasPermission(AdminSystemPermission.EXTERNAL_ACCOUNTS_MANAGE_PAYMENT);

        response.setProcess(process);
        response.setManagePayment(managePayment);
        response.setCheck(check);
        response.setExternalAccount(externalAccount);
        response.setTypes(types);
//        RequestHelper.storeEnum(request, ExternalTransfer.SummaryStatus.class, "statusList");

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
