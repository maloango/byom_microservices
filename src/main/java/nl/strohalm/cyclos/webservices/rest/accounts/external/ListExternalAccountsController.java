package nl.strohalm.cyclos.webservices.rest.accounts.external;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import nl.strohalm.cyclos.access.AdminSystemPermission;
import nl.strohalm.cyclos.annotations.Inject;
import nl.strohalm.cyclos.entities.accounts.external.ExternalAccount;
import nl.strohalm.cyclos.services.accounts.external.ExternalAccountService;
import nl.strohalm.cyclos.services.permissions.PermissionService;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;
import nl.strohalm.cyclos.webservices.rest.GenericResponse;

@Controller
public class ListExternalAccountsController extends BaseRestController {

    public static class ListExternalAccountsResponse extends GenericResponse {

        private boolean editable;
        private List<ExternalAccount> externalAccounts;

        public boolean isEditable() {
            return editable;
        }

        public void setEditable(boolean editable) {
            this.editable = editable;
        }

        public List<ExternalAccount> getExternalAccounts() {
            return externalAccounts;
        }

        public void setExternalAccounts(List<ExternalAccount> externalAccounts) {
            this.externalAccounts = externalAccounts;
        }

    }

    @RequestMapping(value = "admin/listExternalAccounts", method = RequestMethod.GET)
    @ResponseBody
    protected ListExternalAccountsResponse executeAction()
            throws Exception {
        ListExternalAccountsResponse response = new ListExternalAccountsResponse();
        final List<ExternalAccount> externalAccounts = externalAccountService.search();
        response.setExternalAccounts(externalAccounts);
        response.setEditable(permissionService.hasPermission(AdminSystemPermission.EXTERNAL_ACCOUNTS_MANAGE));
        return response;
    }
}
