package nl.strohalm.cyclos.webservices.rest.access;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import nl.strohalm.cyclos.access.AdminMemberPermission;
import nl.strohalm.cyclos.access.BrokerPermission;
import nl.strohalm.cyclos.entities.access.AdminUser;
import nl.strohalm.cyclos.entities.access.MemberUser;
import nl.strohalm.cyclos.entities.access.OperatorUser;
import nl.strohalm.cyclos.entities.access.User;
import nl.strohalm.cyclos.entities.groups.BasicGroupSettings;
import nl.strohalm.cyclos.entities.members.Element;
import nl.strohalm.cyclos.entities.members.Operator;
import nl.strohalm.cyclos.services.elements.ElementService;
import nl.strohalm.cyclos.services.permissions.PermissionService;
import nl.strohalm.cyclos.utils.RelationshipHelper;
import nl.strohalm.cyclos.utils.validation.ValidationException;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;

@Controller
public class ManagePasswordsController extends BaseRestController{
	
	private ElementService elementService;
	private PermissionService permissionService;
	
	public static class ManagePasswordsRequestDto{
		 private long    userId;
		 private boolean admin;
		 private boolean member;

		    public long getUserId() {
		        return userId;
		    }

		    public void setUserId(final long memberId) {
		        userId = memberId;
		    }

			public boolean isAdmin() {
				return admin;
			}

			public void setAdmin(boolean admin) {
				this.admin = admin;
			}

			public boolean isMember() {
				return member;
			}

			public void setMember(boolean member) {
				this.member = member;
			}
		    
		    
	}
	
	public static class ManagePasswordsResponseDto{
		
		private boolean ofAdmin;
		private boolean ofOperator;
		private boolean canChangePassword;
		private boolean canResetPassword;
		private boolean canManageTransactionPassword;
		private User user;
		
		
		public ManagePasswordsResponseDto(boolean ofAdmin, boolean ofOperator, boolean canChangePassword,
		boolean canResetPassword, boolean canManageTransactionPassword, User user) {
			super();
			this.ofAdmin = ofAdmin;
			this.ofOperator = ofOperator;
			this.canChangePassword = canChangePassword;
			this.canResetPassword = canResetPassword;
			this.canManageTransactionPassword = canManageTransactionPassword;
			this.user = user;
		}
		public boolean isOfAdmin() {
			return ofAdmin;
		}
		public void setOfAdmin(boolean ofAdmin) {
			this.ofAdmin = ofAdmin;
		}
		public boolean isOfOperator() {
			return ofOperator;
		}
		public void setOfOperator(boolean ofOperator) {
			this.ofOperator = ofOperator;
		}
		public boolean isCanChangePassword() {
			return canChangePassword;
		}
		public void setCanChangePassword(boolean canChangePassword) {
			this.canChangePassword = canChangePassword;
		}
		public boolean isCanResetPassword() {
			return canResetPassword;
		}
		public void setCanResetPassword(boolean canResetPassword) {
			this.canResetPassword = canResetPassword;
		}
		public boolean isCanManageTransactionPassword() {
			return canManageTransactionPassword;
		}
		public void setCanManageTransactionPassword(boolean canManageTransactionPassword) {
			this.canManageTransactionPassword = canManageTransactionPassword;
		}
		public User getUser() {
			return user;
		}
		public void setUser(User user) {
			this.user = user;
		}
	
	}
	
	
	
	public  void setElementService(final ElementService elementService) {
		this.elementService = elementService;
	}

	public  void setPermissionService(final PermissionService permissionService) {
		this.permissionService = permissionService;
	}


	/**
     * Saves a contact, finding the member by id
     */
    @RequestMapping(value = "/admin/managePasswords", method = RequestMethod.POST)
    @ResponseBody
    public ManagePasswordsResponseDto managePasswords(@RequestBody final ManagePasswordsRequestDto form) {
    	
    	 final long userId = form.getUserId();
    User user = null;
    try {
        if (userId > 0L) {
            user = elementService.loadUser(userId, RelationshipHelper.nested(User.Relationships.ELEMENT, Element.Relationships.GROUP));
        }
        if (user == null) {
            throw new Exception();
        }
    } catch (final Exception e) {
        throw new ValidationException();
    }

    Element element = user.getElement();
    if (element instanceof Operator) {
        element = elementService.load(element.getId(), RelationshipHelper.nested(Operator.Relationships.MEMBER, Element.Relationships.GROUP));
    }
    final BasicGroupSettings groupSettings = element.getGroup().getBasicSettings();
    boolean sendPasswordByEmail = false;
    if (user instanceof MemberUser) {
        sendPasswordByEmail = ((MemberUser) user).getMember().getMemberGroup().getMemberSettings().isSendPasswordByEmail();
    }

    boolean canChangePassword = false;
    boolean canResetPassword = false;
    boolean canManageTransactionPassword = false;

    final boolean tpUsed = groupSettings.getTransactionPassword() != null && groupSettings.getTransactionPassword().isUsed();

    // Determine which the actions can be performed
    if (form.isAdmin()) {
        canChangePassword = permissionService.hasPermission(AdminMemberPermission.ACCESS_CHANGE_PASSWORD);
        // Only can reset if send password by mail is enabled
        canResetPassword = sendPasswordByEmail && permissionService.hasPermission(AdminMemberPermission.ACCESS_RESET_PASSWORD);
        // Only can change TP if it is used
        canManageTransactionPassword = tpUsed && permissionService.hasPermission(AdminMemberPermission.ACCESS_TRANSACTION_PASSWORD);
    } else if (form.isMember()) {
        if (user instanceof OperatorUser) {
            // A member can manage it's operators passwords
            canChangePassword = true;
            canManageTransactionPassword = groupSettings.getTransactionPassword() != null && groupSettings.getTransactionPassword().isUsed();
            ;
        } else {
            // A member accessing as a broker
//            if (!(user instanceof MemberUser) || !context.isBrokerOf((Member) element)) {
//                throw new ValidationException();
//            }
            canChangePassword = permissionService.hasPermission(BrokerPermission.MEMBER_ACCESS_CHANGE_PASSWORD);
            // Only can reset if send password by mail is enabled
            canResetPassword = sendPasswordByEmail && permissionService.hasPermission(BrokerPermission.MEMBER_ACCESS_RESET_PASSWORD);
            canManageTransactionPassword = tpUsed && permissionService.hasPermission(BrokerPermission.MEMBER_ACCESS_TRANSACTION_PASSWORD);
        }
    }
    
    ManagePasswordsResponseDto reponse=new ManagePasswordsResponseDto(user instanceof AdminUser,user instanceof OperatorUser,canChangePassword,canResetPassword,canManageTransactionPassword,user);
    
    	
        return reponse;
    }

}
