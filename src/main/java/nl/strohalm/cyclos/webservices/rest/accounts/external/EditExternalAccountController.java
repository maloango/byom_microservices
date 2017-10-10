package nl.strohalm.cyclos.webservices.rest.accounts.external;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import nl.strohalm.cyclos.access.AdminSystemPermission;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import nl.strohalm.cyclos.annotations.Inject;
import nl.strohalm.cyclos.entities.accounts.AccountTypeQuery;
import nl.strohalm.cyclos.entities.accounts.MemberAccountType;
import nl.strohalm.cyclos.entities.accounts.SystemAccountType;
import nl.strohalm.cyclos.entities.accounts.external.ExternalAccount;
import nl.strohalm.cyclos.services.accounts.AccountTypeService;
import nl.strohalm.cyclos.services.accounts.MemberAccountTypeQuery;
import nl.strohalm.cyclos.services.accounts.SystemAccountTypeQuery;
import nl.strohalm.cyclos.services.accounts.external.ExternalAccountService;
import nl.strohalm.cyclos.utils.binding.BeanBinder;
import nl.strohalm.cyclos.utils.binding.DataBinder;
import nl.strohalm.cyclos.utils.binding.PropertyBinder;
import nl.strohalm.cyclos.utils.conversion.IdConverter;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;
import nl.strohalm.cyclos.webservices.rest.GenericResponse;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

@Controller
public class EditExternalAccountController extends BaseRestController {

    public static class SystemAccountEntity {

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

    public static class MemberAccountEntity {

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

    public static class EditExternalAccountResponse extends GenericResponse {

        private List<SystemAccountEntity> listSystemAccount;
        private List<MemberAccountEntity> listMemberAccount;
        private boolean editable;

        public boolean isEditable() {
            return editable;
        }

        public void setEditable(boolean editable) {
            this.editable = editable;
        }

        public List<SystemAccountEntity> getListSystemAccount() {
            return listSystemAccount;
        }

        public void setListSystemAccount(List<SystemAccountEntity> listSystemAccount) {
            this.listSystemAccount = listSystemAccount;
        }

        public List<MemberAccountEntity> getListMemberAccount() {
            return listMemberAccount;
        }

        public void setListMemberAccount(List<MemberAccountEntity> listMemberAccount) {
            this.listMemberAccount = listMemberAccount;
        }

    }

    public static class EditExternalAccountParameters {

        private Long id;
        private String name;
        private String description;
        private Long systemAccountId;
        private Long memberAccountId;

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

        public Long getSystemAccountId() {
            return systemAccountId;
        }

        public void setSystemAccountId(Long systemAccountId) {
            this.systemAccountId = systemAccountId;
        }

        public Long getMemberAccountId() {
            return memberAccountId;
        }

        public void setMemberAccountId(Long memberAccountId) {
            this.memberAccountId = memberAccountId;
        }

    }

    @RequestMapping(value = "admin/editExternalAccount", method = RequestMethod.GET)
    @ResponseBody
    public EditExternalAccountResponse prepareForm() throws Exception {
        EditExternalAccountResponse response = new EditExternalAccountResponse();
        boolean editable = permissionService.hasPermission(AdminSystemPermission.EXTERNAL_ACCOUNTS_MANAGE);
        final AccountTypeQuery querySystem = new SystemAccountTypeQuery();
        final AccountTypeQuery queryMember = new MemberAccountTypeQuery();
        final List<SystemAccountType> accountSystems = (List<SystemAccountType>) accountTypeService.search(querySystem);
        final List<MemberAccountType> accountMembers = (List<MemberAccountType>) accountTypeService.search(queryMember);
        List<SystemAccountEntity> listSystemAccount = new ArrayList();
        List<MemberAccountEntity> listMemberAccount = new ArrayList();
        for (SystemAccountType systemAccount : accountSystems) {
            SystemAccountEntity systemEntity = new SystemAccountEntity();
            systemEntity.setId(systemAccount.getId());
            systemEntity.setName(systemAccount.getName());
            listSystemAccount.add(systemEntity);
        }

        for (MemberAccountType memberAccount : accountMembers) {
            MemberAccountEntity memberEntity = new MemberAccountEntity();
            memberEntity.setId(memberAccount.getId());
            memberEntity.setName(memberAccount.getName());
            listMemberAccount.add(memberEntity);
        }
        response.setListSystemAccount(listSystemAccount);
        response.setListMemberAccount(listMemberAccount);
        response.setEditable(editable);
        response.setStatus(0);
        return response;

    }

    @RequestMapping(value = "admin/editExternalAccount", method = RequestMethod.POST)
    @ResponseBody
    public GenericResponse EditAccount(@RequestBody EditExternalAccountParameters params) {
        GenericResponse response = new GenericResponse();
        ExternalAccount externalAccount = new ExternalAccount();
        if(params.getId()!=null)
        externalAccount.setId(params.getId());
        externalAccount.setName(params.getName());
        externalAccount.setDescription(params.getDescription());
        externalAccount.setSystemAccountType((SystemAccountType) accountTypeService.load(params.getSystemAccountId()));
        externalAccount.setMemberAccountType((MemberAccountType) accountTypeService.load(params.getMemberAccountId()));
        final boolean isInsert = externalAccount.isTransient();
        externalAccount = externalAccountService.save(externalAccount);
        if (isInsert) {
            response.setMessage("externalAccount.inserted");
        } else {
            response.setMessage("externalAccount.modified");
        }
        response.setStatus(0);
        return response;
    }

}
