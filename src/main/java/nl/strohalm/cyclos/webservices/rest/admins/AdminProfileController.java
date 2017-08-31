/*
    This file is part of Cyclos (www.cyclos.org).
    A project of the Social Trade Organisation (www.socialtrade.org).

    Cyclos is free software; you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation; either version 2 of the License, or
    (at your option) any later version.

    Cyclos is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with Cyclos; if not, write to the Free Software
    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA

 */
package nl.strohalm.cyclos.webservices.rest.admins;

import java.util.Collection;
import java.util.List;
import java.util.Map;


import nl.strohalm.cyclos.access.AdminAdminPermission;
import nl.strohalm.cyclos.access.AdminMemberPermission;
import nl.strohalm.cyclos.annotations.Inject;
import nl.strohalm.cyclos.entities.Relationship;
import nl.strohalm.cyclos.entities.access.AdminUser;
import nl.strohalm.cyclos.entities.access.User;
import nl.strohalm.cyclos.entities.customization.fields.AdminCustomField;
import nl.strohalm.cyclos.entities.members.Administrator;
import nl.strohalm.cyclos.entities.members.Element;
import nl.strohalm.cyclos.entities.members.records.MemberRecordType;
import nl.strohalm.cyclos.services.access.AccessService;
import nl.strohalm.cyclos.services.access.exceptions.NotConnectedException;
import nl.strohalm.cyclos.services.customization.AdminCustomFieldService;
import nl.strohalm.cyclos.services.elements.ElementService;
import nl.strohalm.cyclos.services.elements.MemberRecordService;
import nl.strohalm.cyclos.services.permissions.PermissionService;
import nl.strohalm.cyclos.utils.AdministratorVO;
import nl.strohalm.cyclos.utils.CustomFieldHelper;
import nl.strohalm.cyclos.utils.MessageResolver;
import nl.strohalm.cyclos.utils.RelationshipHelper;
import nl.strohalm.cyclos.utils.access.LoggedUser;
import nl.strohalm.cyclos.utils.validation.ValidationException;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;
import nl.strohalm.cyclos.webservices.rest.GenericResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;


/**
 * Profile action for admins
 * @author luis
 * ProfileAction<Administrator>
 */

@Controller
public class AdminProfileController extends BaseRestController {

    private static final Relationship[] FETCH = { RelationshipHelper.nested(User.Relationships.ELEMENT, Element.Relationships.GROUP), RelationshipHelper.nested(User.Relationships.ELEMENT, Administrator.Relationships.CUSTOM_VALUES) };

    private AdminCustomFieldService     adminCustomFieldService;
    private MemberRecordService         memberRecordService;

    private CustomFieldHelper           customFieldHelper;
    private ElementService   elementService;
    private AccessService    accessService;
    private PermissionService  permissionService;
     private MessageResolver    messageResolver;

    @Inject
    public void setAdminCustomFieldService(final AdminCustomFieldService adminCustomFieldService) {
        this.adminCustomFieldService = adminCustomFieldService;
    }

    @Inject
    public void setCustomFieldHelper(final CustomFieldHelper customFieldHelper) {
        this.customFieldHelper = customFieldHelper;
    }

    @Inject
    public void setMemberRecordService(final MemberRecordService memberRecordService) {
        this.memberRecordService = memberRecordService;
    }
    @Inject
    public void setElementService(ElementService elementService) {
        this.elementService = elementService;
    }
    @Inject
    public void setAccessService(AccessService accessService) {
        this.accessService = accessService;
    }
    @Inject
    public void setPermissionService(PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    @Inject
    public void setMessageResolver(MessageResolver messageResolver) {
        this.messageResolver = messageResolver;
    }
    
    
    
    public static class AdminProfileResponse extends GenericResponse{
        
        private Map<MemberRecordType, Integer> countByRecordType;
        private boolean isLoggedIn;
        private AdministratorVO admin;
        private Collection<CustomFieldHelper.Entry> customFields;
        private boolean editable;
        private boolean myProfile;
        private boolean removed;
        private boolean disabledLogin;

        public Map<MemberRecordType, Integer> getCountByRecordType() {
            return countByRecordType;
        }

        public void setCountByRecordType(Map<MemberRecordType, Integer> countByRecordType) {
            this.countByRecordType = countByRecordType;
        }

        public boolean isIsLoggedIn() {
            return isLoggedIn;
        }

        public void setIsLoggedIn(boolean isLoggedIn) {
            this.isLoggedIn = isLoggedIn;
        }

        public AdministratorVO getAdmin() {
            return admin;
        }

        public void setAdmin(AdministratorVO admin) {
            this.admin = admin;
        }

        public Collection<CustomFieldHelper.Entry> getCustomFields() {
            return customFields;
        }

        public void setCustomFields(Collection<CustomFieldHelper.Entry> customFields) {
            this.customFields = customFields;
        }

        public boolean isEditable() {
            return editable;
        }

        public void setEditable(boolean editable) {
            this.editable = editable;
        }

        public boolean isMyProfile() {
            return myProfile;
        }

        public void setMyProfile(boolean myProfile) {
            this.myProfile = myProfile;
        }

        public boolean isRemoved() {
            return removed;
        }

        public void setRemoved(boolean removed) {
            this.removed = removed;
        }

        public boolean isDisabledLogin() {
            return disabledLogin;
        }

        public void setDisabledLogin(boolean disabledLogin) {
            this.disabledLogin = disabledLogin;
        }
        
        
        
    }
 
    

//    @Override
//    @SuppressWarnings("unchecked")
//    protected <CFV extends CustomFieldValue> Class<CFV> getCustomFieldValueClass() {
//        return (Class<CFV>) AdminCustomFieldValue.class;
//    }
//
//    @Override
//    protected Class<Administrator> getElementClass() {
//        return Administrator.class;
//    }
//
//    @Override
//    @SuppressWarnings("unchecked")
//    protected <G extends Group> Class<G> getGroupClass() {
//        return (Class<G>) AdminGroup.class;
//    }
//
//    @Override
//    @SuppressWarnings("unchecked")
//    protected <U extends User> Class<U> getUserClass() {
//        return (Class<U>) AdminUser.class;
//    }

    @RequestMapping(value = "admin/adminProfile", method = RequestMethod.POST)
    @ResponseBody
    public GenericResponse updateadminProfile(@RequestBody AdministratorVO request) throws Exception {
        GenericResponse response =new GenericResponse();
        System.out.println("---------- "+request.getId()+"  "+request.getEmail());
        // Save the administrator
        //Administrator admin = getWriteDataBinder(context).readFromString(form.getAdmin());
         Administrator savedAdmin = elementService.load(request.getId(), Element.Relationships.USER);
       // admin.getUser().setUsername(savedAdmin.getUsername());
         savedAdmin.setCustomValues(null);
         savedAdmin.setEmail(request.getEmail());
         savedAdmin.setName(request.getFullName());
        savedAdmin = elementService.changeProfile(savedAdmin);

      //  context.sendMessage("profile.modified");
      
      response.setMessage(messageResolver.message("profile.modified","AdminProfile"));
       return response;
        //return ActionHelper.redirectWithParam(context.getRequest(), super.handleSubmit(context), "adminId", admin.getId());
    }

//    @Override
//    @SuppressWarnings({ "unchecked", "rawtypes" })
//    protected DataBinder<Administrator> initDataBinderForWrite(final ActionContext context) {
//        final BeanBinder<Administrator> dataBinder = (BeanBinder<Administrator>) super.initDataBinderForWrite(context);
//
//        final BeanBinder<AdminCustomFieldValue> customValueBinder = BeanBinder.instance(AdminCustomFieldValue.class);
//        customValueBinder.registerBinder("field", PropertyBinder.instance(AdminCustomField.class, "field", ReferenceConverter.instance(AdminCustomField.class)));
//        customValueBinder.registerBinder("value", PropertyBinder.instance(String.class, "value", HtmlConverter.instance()));
//
//        // Replace the normal custom field binder for an admin custom field binder, because it has another property - hidden
//        final BeanCollectionBinder collectionBinder = (BeanCollectionBinder) dataBinder.getMappings().get("customValues");
//        collectionBinder.setElementBinder(customValueBinder);
//
//        return dataBinder;
//    }

    @RequestMapping(value = "admin/adminProfile", method = RequestMethod.GET)
    @ResponseBody
    public AdminProfileResponse prepareForm() throws Exception {
        
        AdminProfileResponse response=new AdminProfileResponse();
                
       // final AdminProfileForm form = context.getForm();
       long adminId=LoggedUser.user().getId();
        boolean myProfile = false;
        AdminUser adminUser = null;
        //final HttpServletRequest request = context.getRequest();

        final Element loggedElement = LoggedUser.element();
        // Load the admin
        if (adminId > 0 && adminId != loggedElement.getId()) {
            final User loaded = elementService.loadUser(adminId, FETCH);
            if (loaded instanceof AdminUser) {
                adminUser = (AdminUser) loaded;
                try {
                    response.setIsLoggedIn(accessService.isLoggedIn(adminUser));
                    //request.setAttribute("isLoggedIn", accessService.isLoggedIn(adminUser));
                } catch (final NotConnectedException e) {
                    // Ok - user is not online
                }
            } else {
                throw new ValidationException();
            }
        } else {
            // My profile
            adminUser = elementService.loadUser(loggedElement.getUser().getId(), FETCH);
            myProfile = true;
        }

        // Write the admin to the form
        final Administrator admin = adminUser.getAdministrator();
        
       // getReadDataBinder(context).writeAsString(form.getAdmin(), admin);

        // Retrieve the custom fields
        final List<AdminCustomField> customFields = customFieldHelper.onlyForGroup(adminCustomFieldService.list(), admin.getAdminGroup());

        // Check the permissions
        boolean editable = myProfile;
        if (!myProfile) {
            editable = permissionService.hasPermission(AdminAdminPermission.ADMINS_CHANGE_PROFILE);
            if (permissionService.hasPermission(AdminMemberPermission.RECORDS_VIEW)) {
                response.setCountByRecordType(memberRecordService.countByType(admin));
                //request.setAttribute("countByRecordType", memberRecordService.countByType(admin));
            }
        }

        // Store the request attributes
        response.setAdmin(admin.getAdministratorVo());
       // request.setAttribute("admin", admin);
        response.setDisabledLogin(accessService.isLoginBlocked(admin.getUser()));
      // request.setAttribute("disabledLogin", accessService.isLoginBlocked(admin.getUser()));
       response.setCustomFields(customFieldHelper.buildEntries(customFields, admin.getCustomValues()));
      //   request.setAttribute("customFields", customFieldHelper.buildEntries(customFields, admin.getCustomValues()));
       response.setEditable(editable);
     //request.setAttribute("editable", editable);
     response.setMyProfile(myProfile);
       // request.setAttribute("myProfile", myProfile);
       response.setRemoved(editable);
       // request.setAttribute("removed", admin.getGroup().getStatus() == Group.Status.REMOVED);
       
       response.setStatus(0);
       response.setMessage("Profile retrieved successfully");
       return response;
    }

//    @Override
//    protected void validateForm(final ActionContext context) {
//        final AdminProfileForm form = context.getForm();
//        final Administrator administrator = getWriteDataBinder(context).readFromString(form.getAdmin());
//        elementService.validate(administrator, WhenSaving.PROFILE, false);
//    }
}
