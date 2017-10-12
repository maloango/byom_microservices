/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.strohalm.cyclos.webservices.rest.accounts.external;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import nl.strohalm.cyclos.access.AdminSystemPermission;
import nl.strohalm.cyclos.entities.accounts.external.ExternalAccount;
import nl.strohalm.cyclos.entities.accounts.external.ExternalTransferType;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;
import nl.strohalm.cyclos.webservices.rest.GenericResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 *
 * @author Lue Infoservices
 */
@Controller
public class GetExternalTransferTypeByIdController extends BaseRestController {
    
    public static class PaymentTypeResponse extends GenericResponse{
        private Long id;
        private String name;
        private String description;
        private String code;
        private Set<Map.Entry<String, ExternalTransferType.Action>> actionsSet;

        public Set<Map.Entry<String, ExternalTransferType.Action>> getActionsSet() {
            return actionsSet;
        }

        public void setActionsSet(Set<Map.Entry<String, ExternalTransferType.Action>> actionsSet) {
            this.actionsSet = actionsSet;
        }
        

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

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }
        
    }

    @RequestMapping(value = "admin/getExternalTransferTypeById/{transferTypeId}", method = RequestMethod.GET)
    @ResponseBody
    public PaymentTypeResponse getPaymentType(@PathVariable("transferTypeId") Long transferTypeId) {
        PaymentTypeResponse response=new PaymentTypeResponse();
//        final long id = accountId;
//        final boolean isInsert = id <= 0L;
//        boolean editable = permissionService.hasPermission(AdminSystemPermission.EXTERNAL_ACCOUNTS_MANAGE);
//        ExternalAccount externalAccount;
//        if (isInsert) {
//            externalAccount = new ExternalAccount();
//            editable = true;
//        } else {
//            externalAccount = externalAccountService.load(id);
//        }

        final ExternalTransferType externalTransferType = externalTransferTypeService.load(transferTypeId, ExternalTransferType.Relationships.TRANSFER_TYPE);
        response.setId(externalTransferType.getId());
        response.setName(externalTransferType.getName());
        response.setDescription(externalTransferType.getDescription());
        response.setCode(externalTransferType.getCode());
        //action list
        Map<String, ExternalTransferType.Action> actions = new HashMap();
        actions.put("DISCARD_LOAN", ExternalTransferType.Action.DISCARD_LOAN);
        actions.put("IGNORE", ExternalTransferType.Action.IGNORE);
        actions.put("CONCILIATE_PAYMENT", ExternalTransferType.Action.CONCILIATE_PAYMENT);
        actions.put("GENERATE_MEMBER_PAYMENT", ExternalTransferType.Action.GENERATE_MEMBER_PAYMENT);
        actions.put("GENERATE_SYSTEM_PAYMENT", ExternalTransferType.Action.GENERATE_SYSTEM_PAYMENT);

        Set<Map.Entry<String, ExternalTransferType.Action>> actionsSet = actions.entrySet();
        response.setActionsSet(actionsSet);
        response.setStatus(0);
        return response;
    }
}
