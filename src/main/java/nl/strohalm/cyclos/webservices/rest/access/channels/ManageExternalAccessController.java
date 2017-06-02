package nl.strohalm.cyclos.webservices.rest.access.channels;

import java.util.ArrayList;
import java.util.Collection;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import nl.strohalm.cyclos.access.AdminMemberPermission;
import nl.strohalm.cyclos.access.BrokerPermission;
import nl.strohalm.cyclos.access.MemberPermission;
import nl.strohalm.cyclos.entities.access.Channel;
import nl.strohalm.cyclos.entities.access.User;
import nl.strohalm.cyclos.entities.access.User.TransactionPasswordStatus;
import nl.strohalm.cyclos.entities.groups.MemberGroup;
import nl.strohalm.cyclos.entities.members.Element;
import nl.strohalm.cyclos.entities.members.Member;
import nl.strohalm.cyclos.services.access.AccessService;
import nl.strohalm.cyclos.services.access.ChannelService;
import nl.strohalm.cyclos.services.elements.ElementService;
import nl.strohalm.cyclos.services.groups.GroupService;
import nl.strohalm.cyclos.services.permissions.PermissionService;
import nl.strohalm.cyclos.utils.RelationshipHelper;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;

@Controller
public class ManageExternalAccessController extends BaseRestController {
	
	private ChannelService channelService;
	private ElementService elementService;
	private AccessService accessService;
	private PermissionService permissionService;
	private GroupService groupService;
	
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
	
	public void setChannelService(final ChannelService channelService) {
		this.channelService = channelService;
	}


	public static class ManageExternalAccessRequestDto {
		private long memberId;
		public final long getMemberId() {
			return memberId;
		}
		public final void setMemberId(long memberId) {
			this.memberId = memberId;
		}
		public final Element getElement() {
			return element;
		}
		public final void setElement(Element element) {
			this.element = element;
		}
		public final User getUser() {
			return user;
		}
		public final void setUser(User user) {
			this.user = user;
		}
		private Element element;
		private User user;

	}

	public static class ManageExternalAccessResponseDto {
		private Member member;
		private boolean myAccess;
		private boolean channels;
		private boolean memberCanHavePin;
		private boolean canChangePin;
		public ManageExternalAccessResponseDto(Member member, boolean myAccess,
				boolean channels, boolean memberCanHavePin, boolean canChangePin) {
			super();
			this.canChangePin =canChangePin;
			this.memberCanHavePin =memberCanHavePin;
			this.channels = channels;
			this.canChangePin = canChangePin;
			this.member = member;
		}
		public Member getMember(){
			return member;
		}
			
		public final void setMember(Member member) {
			this.member = member;
		}
		public boolean isMyAccess() {
			return myAccess;
		}
		public final void isMyAccess(boolean myAccess) {
			this.myAccess = myAccess;
		}
		public boolean getChannels(){
			return channels;
		}
		public final void setChannels(boolean channels) {
			this.channels = channels;
		}
		public void setChannel(boolean channels) {
			this.channels = channels;
		}
		public boolean isMemberCanHavePin() {
			return memberCanHavePin;
		}
		public final void setMemberCanHavePin(boolean memberCanHavePin) {
			this.memberCanHavePin = memberCanHavePin;
		}
		
		public boolean isCanChangePin() {
			return canChangePin;
		}

		public void setCanChangePin(boolean canChangePin) {
			this.canChangePin = canChangePin;
		}
		
	}
	@RequestMapping(value = "/admin/manageExternalAccess", method = RequestMethod.POST)
	@ResponseBody
	public ManageExternalAccessResponseDto ManageExternalAccess(
			@RequestBody final ManageExternalAccessRequestDto form,
			HttpServletRequest request) {

		boolean myAccess = false;
		boolean memberCanHavePin = false;

		// Get the member
		Member member;
		final long memberId = form.getMemberId();
		if (memberId > 0) {
			member = elementService.load(memberId, Element.Relationships.USER,
					Member.Relationships.CHANNELS, RelationshipHelper.nested(
							Element.Relationships.GROUP,
							MemberGroup.Relationships.CHANNELS));
			if (form.getElement().equals(member)) {
				myAccess = true;
			}

		} else {
			// Member managing his/her own external access settings
			member = elementService.load(form.getElement().getId(),
					Element.Relationships.USER, Member.Relationships.CHANNELS,
					RelationshipHelper.nested(Element.Relationships.GROUP,
							MemberGroup.Relationships.CHANNELS));
			myAccess = true;
		}

		// Check if the member can have pin
		memberCanHavePin = groupService.usesPin(member.getMemberGroup());

		final MemberGroup memberGroup = member.getMemberGroup();

		// If the pin is blocked, check the permission to unblock it
		if (memberCanHavePin) {
			final boolean pinBlocked = accessService.isPinBlocked(member
					.getMemberUser());
			if (pinBlocked) {
				final boolean canUnblockPin = permissionService
						.permission(member)
						.admin(AdminMemberPermission.ACCESS_UNBLOCK_PIN)
						.broker(BrokerPermission.MEMBER_ACCESS_UNBLOCK_PIN)
						.member(MemberPermission.ACCESS_UNBLOCK_PIN)
						.hasPermission();
				request.setAttribute("canUnblockPin", canUnblockPin);
			}
		}

		// Check if the group of member uses a transaction password
		if (myAccess) {
			final boolean usesTransactionPassword = memberGroup
					.getBasicSettings().getTransactionPassword().isUsed();
			if (usesTransactionPassword) {
				request.setAttribute("usesTransactionPassword",
						usesTransactionPassword);
				final TransactionPasswordStatus transactionPasswordStatus = elementService
						.reloadUser(form.getUser().getId())
						.getTransactionPasswordStatus();
				if (transactionPasswordStatus == TransactionPasswordStatus.BLOCKED) {
					request.setAttribute("transactionPasswordBlocked", true);
				} else if (transactionPasswordStatus.isGenerationAllowed()) {
					request.setAttribute("transactionPasswordPending", true);
				}
			}
		}

		final boolean canChangePin = memberCanHavePin
				&& permissionService.permission(member)
						.admin(AdminMemberPermission.ACCESS_CHANGE_PIN)
						.broker(BrokerPermission.MEMBER_ACCESS_CHANGE_PIN)
						.member().hasPermission();

		// Channels that the group of member have access
		final Channel webChannel = channelService
				.loadByInternalName(Channel.WEB);

		final Collection<Channel> memberGroupChannels = new ArrayList<Channel>(
				memberGroup.getChannels());
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

		// Store member and settings in the request
		ManageExternalAccessResponseDto reponse = new ManageExternalAccessResponseDto(member,myAccess,memberCanHaveSmsChannel,memberCanHavePin,canChangePin);
				
		return reponse;
	}
}