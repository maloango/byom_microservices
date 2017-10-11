package nl.strohalm.cyclos.webservices.rest.accounts.external;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import nl.strohalm.cyclos.access.AdminSystemPermission;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import nl.strohalm.cyclos.annotations.Inject;
import nl.strohalm.cyclos.entities.accounts.external.ExternalAccount;
import nl.strohalm.cyclos.entities.accounts.external.ExternalTransferType;
import nl.strohalm.cyclos.entities.accounts.external.ExternalTransferType.Action;
import nl.strohalm.cyclos.entities.accounts.transactions.TransferType;
import nl.strohalm.cyclos.entities.accounts.transactions.TransferTypeQuery;
import nl.strohalm.cyclos.services.accounts.external.ExternalAccountService;
import nl.strohalm.cyclos.services.accounts.external.ExternalTransferTypeService;
import nl.strohalm.cyclos.services.permissions.PermissionService;
import nl.strohalm.cyclos.services.transactions.TransactionContext;
import nl.strohalm.cyclos.services.transfertypes.TransferTypeService;
import nl.strohalm.cyclos.utils.binding.BeanBinder;
import nl.strohalm.cyclos.utils.binding.DataBinder;
import nl.strohalm.cyclos.utils.binding.PropertyBinder;
import nl.strohalm.cyclos.utils.conversion.IdConverter;
import nl.strohalm.cyclos.utils.validation.ValidationException;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;
import nl.strohalm.cyclos.webservices.rest.GenericResponse;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class EditExternalTransferTypeController extends BaseRestController {

    public static class EditExternalTransferResponse extends GenericResponse {

        private Set<Entry<String, ExternalTransferType.Action>> actionsSet;
        private List<TransferType> toMemberTransferTypes;
        private List<TransferType> toSystemTransferTypes;
        private ExternalAccount externalAccount;

        public ExternalAccount getExternalAccount() {
            return externalAccount;
        }

        public void setExternalAccount(ExternalAccount externalAccount) {
            this.externalAccount = externalAccount;
        }

        public List<TransferType> getToMemberTransferTypes() {
            return toMemberTransferTypes;
        }

        public void setToMemberTransferTypes(List<TransferType> toMemberTransferTypes) {
            this.toMemberTransferTypes = toMemberTransferTypes;
        }

        public List<TransferType> getToSystemTransferTypes() {
            return toSystemTransferTypes;
        }

        public void setToSystemTransferTypes(List<TransferType> toSystemTransferTypes) {
            this.toSystemTransferTypes = toSystemTransferTypes;
        }

        private boolean editable;

        public boolean isEditable() {
            return editable;
        }

        public void setEditable(boolean editable) {
            this.editable = editable;
        }

        public Set<Entry<String, Action>> getActionsSet() {
            return actionsSet;
        }

        public void setActionsSet(Set<Entry<String, Action>> actionsSet) {
            this.actionsSet = actionsSet;
        }

    }

    @RequestMapping(value = "admin/editExternalTransferType/{accountId}", method = RequestMethod.GET)
    @ResponseBody
    protected EditExternalTransferResponse prepareForm(@PathVariable("accountId") Long accountId)
            throws Exception {

//        final boolean isInsert = id <= 0L;
        EditExternalTransferResponse response = new EditExternalTransferResponse();
        boolean editable = permissionService.hasPermission(AdminSystemPermission.EXTERNAL_ACCOUNTS_MANAGE);
        ExternalTransferType externalTransferType;
        ExternalAccount externalAccount;

        externalTransferType = new ExternalTransferType();
        final long account = accountId;
        if (account <= 0L) {
            throw new ValidationException();
        }
        externalAccount = externalAccountService.load(account);
        externalTransferType.setAccount(externalAccount);
        editable = true;
//        } else {
//            externalTransferType = externalTransferTypeService.load(id, ExternalTransferType.Relationships.ACCOUNT);
//            externalAccount = externalTransferType.getAccount();

//        RequestHelper.storeEnum(request, ExternalTransferType.Action.class, "actions");
        Map<String, ExternalTransferType.Action> actions = new HashMap();
        actions.put("DISCARD_LOAN", ExternalTransferType.Action.DISCARD_LOAN);
        actions.put("IGNORE", ExternalTransferType.Action.IGNORE);
        actions.put("CONCILIATE_PAYMENT",ExternalTransferType.Action.CONCILIATE_PAYMENT);
        actions.put("GENERATE_MEMBER_PAYMENT",ExternalTransferType.Action.GENERATE_MEMBER_PAYMENT );
        actions.put("GENERATE_SYSTEM_PAYMENT", ExternalTransferType.Action.GENERATE_SYSTEM_PAYMENT);
        
        Set<Entry<String, ExternalTransferType.Action>> actionsSet = actions.entrySet();
        response.setEditable(editable);
        response.setActionsSet(actionsSet);
        final TransferTypeQuery toMemberQuery = new TransferTypeQuery();
        toMemberQuery.setContext(TransactionContext.AUTOMATIC);
        toMemberQuery.setFromAccountType(externalAccount.getSystemAccountType());
        toMemberQuery.setToAccountType(externalAccount.getMemberAccountType());
        final List<TransferType> toMemberTransferTypes = transferTypeService.search(toMemberQuery);
//        response.setToMemberTransferTypes(toMemberTransferTypes);

        final TransferTypeQuery toSystemQuery = new TransferTypeQuery();
        toSystemQuery.setContext(TransactionContext.AUTOMATIC);
        toSystemQuery.setFromAccountType(externalAccount.getMemberAccountType());
        toSystemQuery.setToAccountType(externalAccount.getSystemAccountType());
        final List<TransferType> toSystemTransferTypes = transferTypeService.search(toSystemQuery);
//        response.setToSystemTransferTypes(toSystemTransferTypes);

//        response.setExternalAccount(externalAccount);
        response.setStatus(0);
        response.setMessage("edit transfer type");
        return response;
    }

}
