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
package nl.strohalm.cyclos.webservices.rest.access.channels;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import nl.strohalm.cyclos.access.AdminMemberPermission;
import nl.strohalm.cyclos.access.BrokerPermission;
import nl.strohalm.cyclos.access.MemberPermission;
import nl.strohalm.cyclos.annotations.Inject;
import nl.strohalm.cyclos.entities.access.Channel;
import nl.strohalm.cyclos.entities.access.User.TransactionPasswordStatus;
import nl.strohalm.cyclos.entities.customization.fields.MemberCustomField;
import nl.strohalm.cyclos.entities.groups.MemberGroup;
import nl.strohalm.cyclos.entities.members.Element;
import nl.strohalm.cyclos.entities.members.Member;
import nl.strohalm.cyclos.services.access.AccessService;
import nl.strohalm.cyclos.services.access.ChannelService;
import nl.strohalm.cyclos.services.customization.MemberCustomFieldService;
import nl.strohalm.cyclos.services.elements.ElementService;
import nl.strohalm.cyclos.services.groups.GroupService;
import nl.strohalm.cyclos.services.permissions.PermissionService;
import nl.strohalm.cyclos.utils.EntityHelper;
import nl.strohalm.cyclos.utils.RelationshipHelper;
import nl.strohalm.cyclos.webservices.model.FieldVO;
import nl.strohalm.cyclos.webservices.model.MemberVO;
import nl.strohalm.cyclos.webservices.rest.BaseFieldsRestController;
import nl.strohalm.cyclos.webservices.utils.MemberHelper;

/**
 * Action used to prepare the external access screen, where an admin can change
 * the channel access for individual users.
 * 
 * @author Jefferson Magno
 */
@Controller
public class ManageExternalAccessController extends BaseFieldsRestController<MemberCustomField> {

	private ChannelService channelService;
	private ElementService elementService;
	private GroupService groupService;
	private PermissionService permissionService;
	private AccessService accessService;
         private MemberCustomFieldService memberCustomFieldService;
          private MemberHelper                      memberHelper;
        //     private MemberService            memberService;

	public void setAccessService(AccessService accessService) {
		this.accessService = accessService;
	}

	public void setElementService(ElementService elementService) {
		this.elementService = elementService;
	}

	public void setGroupService(GroupService groupService) {
		this.groupService = groupService;
	}

	public void setPermissionService(PermissionService permissionService) {
		this.permissionService = permissionService;
	}

    public MemberCustomFieldService getMemberCustomFieldService() {
        return memberCustomFieldService;
    }

    public void setMemberCustomFieldService(MemberCustomFieldService memberCustomFieldService) {
        this.memberCustomFieldService = memberCustomFieldService;
    }

    public MemberHelper getMemberHelper() {
        return memberHelper;
    }

    public void setMemberHelper(MemberHelper memberHelper) {
        this.memberHelper = memberHelper;
    }
        
    
        

	@Inject
	public void setChannelService(final ChannelService channelService) {
		this.channelService = channelService;
	}

	public static class ManageExternalAccessRequestDto {
		private long memberId;

		public long getMemberId() {
			return memberId;
		}

		public void setMemberId(long memberId) {
			this.memberId = memberId;
		}

	}

	public static class ManageExternalAccessResponseDto {
		private MemberVO  member;
		private boolean myAccess;
		private Channel channels;
		private boolean memberCanHavePin;
		private boolean canChangePin;
		private boolean canManagePreferences;
		private boolean canChangeChannelAccess;
                private boolean canUnblockPin;
                private boolean usesTransactionPassword;
                private boolean transactionPasswordBlocked;
                private boolean transactionPasswordPending;

                
                public ManageExternalAccessResponseDto(){
                    
                }

        public boolean isCanUnblockPin() {
            return canUnblockPin;
        }

        public void setCanUnblockPin(boolean canUnblockPin) {
            this.canUnblockPin = canUnblockPin;
        }

        public boolean isUsesTransactionPassword() {
            return usesTransactionPassword;
        }

        public void setUsesTransactionPassword(boolean usesTransactionPassword) {
            this.usesTransactionPassword = usesTransactionPassword;
        }

        public boolean isTransactionPasswordBlocked() {
            return transactionPasswordBlocked;
        }

        public void setTransactionPasswordBlocked(boolean transactionPasswordBlocked) {
            this.transactionPasswordBlocked = transactionPasswordBlocked;
        }

        public boolean isTransactionPasswordPending() {
            return transactionPasswordPending;
        }

        public void setTransactionPasswordPending(boolean transactionPasswordPending) {
            this.transactionPasswordPending = transactionPasswordPending;
        }
                
                

		public final boolean isCanManagePreferences() {
			return canManagePreferences;
		}

		public final void setCanManagePreferences(boolean canManagePreferences) {
			this.canManagePreferences = canManagePreferences;
		}

		public final boolean isCanChangeChannelAccess() {
			return canChangeChannelAccess;
		}

		public final void setCanChangeChannelAccess(boolean canChangeChannelAccess) {
			this.canChangeChannelAccess = canChangeChannelAccess;
		}

        public MemberVO getMember() {
            return member;
        }

        public void setMember(MemberVO member) {
            this.member = member;
        }

        

		
		public boolean isMyAccess() {
			return myAccess;
		}

		public void setMyAccess(boolean myAccess) {
			this.myAccess = myAccess;
		}

		public Channel getChannels() {
			return channels;
		}

		public void setChannels(Channel channels) {
			this.channels = channels;
		}

		public boolean isMemberCanHavePin() {
			return memberCanHavePin;
		}

		public void setMemberCanHavePin(boolean memberCanHavePin) {
			this.memberCanHavePin = memberCanHavePin;
		}

		public boolean isCanChangePin() {
			return canChangePin;
		}

		public void setCanChangePin(boolean canChangePin) {
			this.canChangePin = canChangePin;
		}

	}

	@RequestMapping(value = "admin/manageExternalAccess", method = RequestMethod.POST)
	@ResponseBody
	public ManageExternalAccessResponseDto ManageExternalAccess(@RequestBody final ManageExternalAccessRequestDto form) {
 
                ManageExternalAccessResponseDto reponse = new ManageExternalAccessResponseDto();
                try{
		boolean myAccess = false;
		boolean memberCanHavePin = false;

		// Get the member
		Member member;
		final long memberId = form.getMemberId();
		if (memberId > 0) {
			member = elementService.load(memberId, Element.Relationships.USER, Member.Relationships.CHANNELS,
					RelationshipHelper.nested(Element.Relationships.GROUP, MemberGroup.Relationships.CHANNELS));
//			if (form.getElement().equals(member)) {
				myAccess = true;
			//}

		} else {
			// Member managing his/her own external access settings
			member = elementService.load(form.getMemberId(), Element.Relationships.USER,
					Member.Relationships.CHANNELS,
					RelationshipHelper.nested(Element.Relationships.GROUP, MemberGroup.Relationships.CHANNELS));
			myAccess = true;
		}

		// Check if the member can have pin
		memberCanHavePin = groupService.usesPin(member.getMemberGroup());

		final MemberGroup memberGroup = member.getMemberGroup();

		// If the pin is blocked, check the permission to unblock it
		if (memberCanHavePin) {
			final boolean pinBlocked = accessService.isPinBlocked(member.getMemberUser());
			if (pinBlocked) {
				final boolean canUnblockPin = permissionService.permission(member)
						.admin(AdminMemberPermission.ACCESS_UNBLOCK_PIN)
						.broker(BrokerPermission.MEMBER_ACCESS_UNBLOCK_PIN).member(MemberPermission.ACCESS_UNBLOCK_PIN)
						.hasPermission();
				reponse.setCanUnblockPin(canUnblockPin);
                                
			}
		}

		// Check if the group of member uses a transaction password
		if (myAccess) {
			final boolean usesTransactionPassword = memberGroup.getBasicSettings().getTransactionPassword().isUsed();
			if (usesTransactionPassword) {
				reponse.setUsesTransactionPassword(usesTransactionPassword);
				final TransactionPasswordStatus transactionPasswordStatus = elementService
						.reloadUser(form.getMemberId()).getTransactionPasswordStatus();
				if (transactionPasswordStatus == TransactionPasswordStatus.BLOCKED) {
					reponse.setTransactionPasswordBlocked(true);
				} else if (transactionPasswordStatus.isGenerationAllowed()) {
					reponse.setTransactionPasswordPending(true);
				}
			}
		}

		final boolean canChangePin = memberCanHavePin
				&& permissionService.permission(member).admin(AdminMemberPermission.ACCESS_CHANGE_PIN)
						.broker(BrokerPermission.MEMBER_ACCESS_CHANGE_PIN).member().hasPermission();

		// Channels that the group of member have access
		final Channel webChannel = channelService.loadByInternalName(Channel.WEB);

		final Collection<Channel> memberGroupChannels = new ArrayList<Channel>(memberGroup.getChannels());
		// The "web" channel can not be customized by the user, so it should not
		// be sent to the JSP page
		// We need to clone the channels collection because it's associated to
		// the hibernate session
		memberGroupChannels.remove(webChannel);

		// When the SMS channel is in use, it is not added / removed from this
		// page, but from notifications
		final Channel smsChannel = channelService.getSmsChannel();
		boolean memberCanHaveSmsChannel = false;
		if (smsChannel != null) {
			memberCanHaveSmsChannel = memberGroupChannels.remove(smsChannel);
		}

		final boolean hasPermission = permissionService.permission(member)
				.admin(AdminMemberPermission.PREFERENCES_MANAGE_NOTIFICATIONS)
				.member(MemberPermission.PREFERENCES_MANAGE_NOTIFICATIONS)
				.broker(BrokerPermission.PREFERENCES_MANAGE_NOTIFICATIONS).hasPermission();
		final boolean canManagePreferences = memberCanHaveSmsChannel && hasPermission;

		final boolean canChangeChannelAccess = accessService.canChangeChannelsAccess(member);

//		ManageExternalAccessResponseDto reponse = new ManageExternalAccessResponseDto(member, myAccess, smsChannel,
//				memberCanHavePin, canChangePin, canManagePreferences, canChangeChannelAccess);
List<MemberCustomField> fields=    memberCustomFieldService.list();   
MemberGroup group =member.getMemberGroup();
fields = customFieldHelper.onlyVisibleFields(fields, group);
reponse.setMember(memberHelper.toVO(member, fields, false));
                reponse.setMyAccess(myAccess);
                reponse.setChannels(smsChannel);
                reponse.setMemberCanHavePin(memberCanHavePin);
                reponse.setCanChangePin(canChangePin);
                reponse.setCanManagePreferences(canManagePreferences);
                reponse.setCanChangeChannelAccess(canChangeChannelAccess);
                
                }catch(Exception ex){
                    ex.printStackTrace();
                }
                
		return reponse;
	}
        
     private List<FieldVO> list(final Member member) {
        List<MemberCustomField> fields = memberCustomFieldService.list();
        fields = customFieldHelper.onlyVisibleFields(fields, member.getMemberGroup());
        ArrayList<Long> customFieldIds = new ArrayList<Long>(EntityHelper.toIdsAsList(fields));
        return memberCustomFieldService.getFieldVOs(customFieldIds);
    }
}
