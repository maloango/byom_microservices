package nl.strohalm.cyclos.webservices.rest.ads;

import java.util.Collection;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import nl.strohalm.cyclos.access.AdminMemberPermission;
import nl.strohalm.cyclos.access.BrokerPermission;
import nl.strohalm.cyclos.access.MemberPermission;
import nl.strohalm.cyclos.access.OperatorPermission;
import nl.strohalm.cyclos.annotations.Inject;
import nl.strohalm.cyclos.entities.Entity;
import nl.strohalm.cyclos.entities.access.AdminUser;
import nl.strohalm.cyclos.entities.access.MemberUser;
import nl.strohalm.cyclos.entities.access.OperatorUser;
import nl.strohalm.cyclos.entities.access.User;
import nl.strohalm.cyclos.entities.ads.Ad;
import nl.strohalm.cyclos.entities.ads.AdQuery;
import nl.strohalm.cyclos.entities.customization.images.AdImage;
import nl.strohalm.cyclos.entities.members.Element;
import nl.strohalm.cyclos.entities.members.Member;
import nl.strohalm.cyclos.entities.members.Operator;
import nl.strohalm.cyclos.services.ads.AdService;
import nl.strohalm.cyclos.services.elements.ElementService;
import nl.strohalm.cyclos.services.groups.GroupService;
import nl.strohalm.cyclos.services.permissions.PermissionService;
import nl.strohalm.cyclos.services.settings.SettingsService;
import nl.strohalm.cyclos.utils.RelationshipHelper;
import nl.strohalm.cyclos.utils.validation.ValidationException;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class MemberAdsController extends BaseRestController {
	private AdService adService;
	private GroupService groupService;
	public final GroupService getGroupService() {
		return groupService;
	}

	public final void setGroupService(GroupService groupService) {
		this.groupService = groupService;
	}

	public final ElementService getElementService() {
		return elementService;
	}

	public final void setElementService(ElementService elementService) {
		this.elementService = elementService;
	}

	public static final PermissionService getPermissionService() {
		return permissionService;
	}

	public static final void setPermissionService(PermissionService permissionService) {
		MemberAdsController.permissionService = permissionService;
	}

	public final SettingsService getSettingsService() {
		return settingsService;
	}

	public final void setSettingsService(SettingsService settingsService) {
		this.settingsService = settingsService;
	}

	private ElementService elementService;
	private static PermissionService permissionService;
	private SettingsService settingsService;

	public AdService getAdService() {
		return adService;
	}

	@Inject
	public void setAdService(final AdService adService) {
		this.adService = adService;
	}

	public static class MemberAdsRequestDto {
		private long memberId;
		private boolean readOnly;

		public long getMemberId() {
			return memberId;
		}

		public boolean isReadOnly() {
			return readOnly;
		}

		public void setMemberId(final long memberId) {
			this.memberId = memberId;
		}

		public void setReadOnly(final boolean readOnly) {
			this.readOnly = readOnly;
		}

		User user;

		public Entity getElement() {
			return user.getElement();
		}

		public boolean isOperator() {
			return user instanceof OperatorUser;
		}

		public boolean isMember() {
			return user instanceof MemberUser;
		}

		public boolean isBrokerOf(Member member) {
			if (member == null || !isBroker() || member.equals(getElement())) {
				return false;
			}

			return permissionService.manages(member);
		}

		public boolean isBroker() {
			if (!isMember()) {
				return false;
			}
			final Member member = (Member) getElement();
			return member.getMemberGroup().isBroker();
		}

		public boolean isAdmin() {
			return user instanceof AdminUser;
		}
	}

	public static class MemberAdsResponseDto {
		
		Member member;
		boolean hasImages;
		boolean myAds;
		boolean editable;
		List<Ad> ads;
		boolean brokerViewingAsMember;
		boolean maxAds;

        public MemberAdsResponseDto(Member member, boolean hasImages, boolean myAds, boolean editable, List<Ad> ads, boolean brokerViewingAsMember, boolean maxAds) {
            this.member = member;
            this.hasImages = hasImages;
            this.myAds = myAds;
            this.editable = editable;
            this.ads = ads;
            this.brokerViewingAsMember = brokerViewingAsMember;
            this.maxAds = maxAds;
        }
                
        

        public Member getMember() {
            return member;
        }

        public void setMember(Member member) {
            this.member = member;
        }

        public boolean isHasImages() {
            return hasImages;
        }

        public void setHasImages(boolean hasImages) {
            this.hasImages = hasImages;
        }

        public boolean isMyAds() {
            return myAds;
        }

        public void setMyAds(boolean myAds) {
            this.myAds = myAds;
        }

        public boolean isEditable() {
            return editable;
        }

        public void setEditable(boolean editable) {
            this.editable = editable;
        }

        public List<Ad> getAds() {
            return ads;
        }

        public void setAds(List<Ad> ads) {
            this.ads = ads;
        }

        public boolean isBrokerViewingAsMember() {
            return brokerViewingAsMember;
        }

        public void setBrokerViewingAsMember(boolean brokerViewingAsMember) {
            this.brokerViewingAsMember = brokerViewingAsMember;
        }

        public boolean isMaxAds() {
            return maxAds;
        }

        public void setMaxAds(boolean maxAds) {
            this.maxAds = maxAds;
        }


                public MemberAdsResponseDto(){
                }
                        

	}

	@RequestMapping(value = "admin/memberAds/{memberId}", method = RequestMethod.POST)
	@ResponseBody
	protected MemberAdsRequestDto executeAction(@PathVariable ("memberId") long memberId) throws Exception {
			
		
		Member member;
		boolean myAds = false;
		boolean editable = false;
		MemberAdsRequestDto response= new MemberAdsRequestDto();
                try{
		final boolean brokerViewingAsMember = response.isReadOnly();

		
		if (response.getMemberId() <= 0
				|| response.getMemberId() == response.getElement().getId()
				|| (response.isOperator() && response.getMemberId() == ((Operator) response
						.getElement()).getMember().getId())) {
			if (response.isMember()) {
				member = (Member) response.getElement();
				editable = permissionService
						.hasPermission(MemberPermission.ADS_PUBLISH);
			} else if (response.isOperator()) {
				member = ((Operator) response.getElement()).getMember();
				editable = permissionService
						.hasPermission(OperatorPermission.ADS_PUBLISH);
			} else {
				throw new ValidationException();
			}
			myAds = true;
		} else {
			final Element element = elementService.load(response.getMemberId(),
					Element.Relationships.USER);

			if (!(element instanceof Member)) {
				throw new ValidationException();
			}

			member = (Member) element;
			if (response.isMember()) {
				editable = !brokerViewingAsMember
						&& response.isBrokerOf(member)
						&& permissionService
								.hasPermission(BrokerPermission.ADS_MANAGE);
			} else if (response.isAdmin()) {
				editable = permissionService
						.hasPermission(AdminMemberPermission.ADS_MANAGE);
			}
		}

		final AdQuery query = new AdQuery();
		query.fetch(RelationshipHelper.nested(Ad.Relationships.OWNER,
				Element.Relationships.USER), Ad.Relationships.CURRENCY);
		query.setMyAds(myAds);
		query.setOwner(member);

		// Member viewing another member's ads
		if (!response.isAdmin() && !myAds && !response.isBrokerOf(member)) {
			query.setStatus(Ad.Status.ACTIVE);
		}

		if (brokerViewingAsMember) {
			query.setStatus(Ad.Status.ACTIVE);
		}

		final List<Ad> ads = adService.search(query);

		// Check if any ad has images
		boolean hasImages = false;
		for (final Ad ad : ads) {
			final Collection<AdImage> images = ad.getImages();
			if (images != null && !images.isEmpty()) {
				hasImages = true;
				break;
			}
		}

		// Check for maxAds
		member = elementService.load(member.getId(),
				Element.Relationships.GROUP);
		final int adCount = ads.size();
		final int maxAdsPerMember = member.getMemberGroup().getMemberSettings()
				.getMaxAdsPerMember();
		boolean maxAds = adCount >= maxAdsPerMember;
		
                    response = new MemberAdsRequestDto();
}
                catch(Exception e){
                    e.printStackTrace();
                }
                
				
		return response;
	}
}
