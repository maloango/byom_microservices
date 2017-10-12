package nl.strohalm.cyclos.webservices.rest.accounts.external;

import java.util.ArrayList;
import java.util.List;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import nl.strohalm.cyclos.access.AdminSystemPermission;
import nl.strohalm.cyclos.annotations.Inject;
import nl.strohalm.cyclos.entities.accounts.external.ExternalAccount;
import nl.strohalm.cyclos.entities.accounts.external.ExternalTransferType;
import nl.strohalm.cyclos.services.accounts.external.ExternalTransferTypeService;
import nl.strohalm.cyclos.services.permissions.PermissionService;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;
import nl.strohalm.cyclos.webservices.rest.GenericResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class ListExternalTransferTypesController extends BaseRestController {

    public static class ListExternalTransferTypeResponse extends GenericResponse {
     private List<ExternalTransferTypeEntity> externalTransferTypes;
     private  boolean editable;

        public boolean isEditable() {
            return editable;
        }

        public void setEditable(boolean editable) {
            this.editable = editable;
        }
     

        public List<ExternalTransferTypeEntity> getExternalTransferTypes() {
            return externalTransferTypes;
        }

        public void setExternalTransferTypes(List<ExternalTransferTypeEntity> externalTransferTypes) {
            this.externalTransferTypes = externalTransferTypes;
        }
     
    }

    public static class ExternalTransferTypeEntity {

        private Long id;
        private String name;
        private ExternalTransferType.Action action;

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

        public ExternalTransferType.Action getAction() {
            return action;
        }

        public void setAction(ExternalTransferType.Action action) {
            this.action = action;
        }

    }

    @RequestMapping(value = "admin/listExternalTransferTypes/{externalAccountId}", method = RequestMethod.GET)
    @ResponseBody
    protected ListExternalTransferTypeResponse executeAction(@PathVariable("externalAccountId") Long externalAccountId)
            throws Exception {
        ListExternalTransferTypeResponse response = new ListExternalTransferTypeResponse();
        final long id = externalAccountId;
        final boolean isInsert = id <= 0L;
        boolean editable = permissionService.hasPermission(AdminSystemPermission.EXTERNAL_ACCOUNTS_MANAGE);
        ExternalAccount externalAccount;
        if (isInsert) {
            externalAccount = new ExternalAccount();
            editable = true;
        } else {
            externalAccount = externalAccountService.load(id);
        }

        final List<ExternalTransferType> externalTransferTypes = externalTransferTypeService.listByAccount(externalAccount);
        List<ExternalTransferTypeEntity> transferTypeList=new ArrayList();
        for(ExternalTransferType tt:externalTransferTypes){
            ExternalTransferTypeEntity transferTypeEntity=new ExternalTransferTypeEntity();
            transferTypeEntity.setId(tt.getId());
            transferTypeEntity.setName(tt.getName());
            transferTypeEntity.setAction(tt.getAction());
            transferTypeList.add(transferTypeEntity);
            
        }
        response.setEditable(editable);
        response.setExternalTransferTypes(transferTypeList);
        response.setStatus(0);
        response.setMessage("External transfer type list!");
        return response;
    }
}
