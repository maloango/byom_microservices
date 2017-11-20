/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.strohalm.cyclos.webservices.rest.admins;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import nl.strohalm.cyclos.annotations.Inject;
import nl.strohalm.cyclos.entities.access.AdminUser;
import nl.strohalm.cyclos.entities.customization.fields.AdminCustomField;
import nl.strohalm.cyclos.entities.customization.fields.AdminCustomFieldValue;
import nl.strohalm.cyclos.entities.groups.AdminGroup;
import nl.strohalm.cyclos.entities.groups.Group;
import nl.strohalm.cyclos.entities.members.Administrator;
import nl.strohalm.cyclos.entities.members.Element;
import nl.strohalm.cyclos.services.customization.AdminCustomFieldService;
import nl.strohalm.cyclos.services.elements.ElementService;
import nl.strohalm.cyclos.utils.CustomFieldHelper;
import nl.strohalm.cyclos.utils.access.LoggedUser;
import nl.strohalm.cyclos.webservices.rest.GenericResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 *
 * @author Lue Infoservices
 */
@Controller
public class CreateAdminController extends AbstractCreateAdminController<Administrator> {
    private ElementService elementService;
    private AdminCustomFieldService adminCustomFieldService;

    private CustomFieldHelper       customFieldHelper;

    @Inject
    public void setElementService(ElementService elementService) {
        this.elementService = elementService;
    }
    

    public static class CreateAdminParameters {

        private Long group;
        private String username;
        private String name;
        private String email;
        private String password;
        private String confirmPassword;
        private boolean forceChangePassword;

        public boolean isForceChangePassword() {
            return forceChangePassword;
        }

        public void setForceChangePassword(boolean forceChangePassword) {
            this.forceChangePassword = forceChangePassword;
        }

        public Long getGroup() {
            return group;
        }

        public void setGroup(Long group) {
            this.group = group;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getConfirmPassword() {
            return confirmPassword;
        }

        public void setConfirmPassword(String confirmPassword) {
            this.confirmPassword = confirmPassword;
        }

    }

    @RequestMapping(value = "admin/createAdmin", method = RequestMethod.POST)
    @ResponseBody
    public GenericResponse create(@RequestBody CreateAdminParameters params) {
        GenericResponse response = new GenericResponse();
        Map<String, Object> query = new HashMap();
        query.put("group", params.getGroup());
        query.put("email", params.getEmail());
        query.put("name", params.getName());

        Map<String, Object> userMap = new HashMap();
        userMap.put("username", params.getUsername());
        userMap.put("password", params.getPassword());

        query.put("user", userMap);
        final Element element = getDataBinder().readFromString(query);
        Administrator administrator = (Administrator) element;
        administrator.setEmail(params.getEmail());
        administrator.setName(params.getName());
        administrator.setGroup(groupService.load(params.getGroup(),Group.Relationships.ELEMENTS));
          AdminUser adminUser=new AdminUser();
          adminUser.setUsername(params.getUsername());
          adminUser.setPassword(params.getPassword());
          administrator.setUser(adminUser);
      //   final List<AdminCustomField> customFields = customFieldHelper.onlyForGroup(adminCustomFieldService.list(),administrator.getAdminGroup());
          
                 
        System.out.println("----  "+administrator);
        administrator = (Administrator)elementService.register(administrator, params.isForceChangePassword(), LoggedUser.remoteAddress());
        response.setMessage("createAdmin.created");
        response.setStatus(0);
        return response;
    }

    @Override
    @SuppressWarnings("unchecked")
    protected Class<AdminCustomField> getCustomFieldClass() {
        return AdminCustomField.class;
    }

    @Override
    @SuppressWarnings("unchecked")
    protected Class<AdminCustomFieldValue> getCustomFieldValueClass() {
        return AdminCustomFieldValue.class;
    }

    @Override
    protected Class<Administrator> getElementClass() {
        return Administrator.class;
    }

    @Override
    @SuppressWarnings("unchecked")
    protected Class<AdminGroup> getGroupClass() {
        return AdminGroup.class;
    }

    @Override
    @SuppressWarnings("unchecked")
    protected Class<AdminUser> getUserClass() {
        return AdminUser.class;
    }

}
