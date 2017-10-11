/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.strohalm.cyclos.webservices.rest.members.advertisements;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import jdk.internal.org.objectweb.asm.TypeReference;
import nl.strohalm.cyclos.access.AdminMemberPermission;
import nl.strohalm.cyclos.access.BrokerPermission;
import nl.strohalm.cyclos.access.MemberPermission;
import nl.strohalm.cyclos.access.OperatorPermission;
import nl.strohalm.cyclos.annotations.Inject;
import nl.strohalm.cyclos.entities.ads.Ad;
import nl.strohalm.cyclos.entities.ads.AdQuery;
import nl.strohalm.cyclos.entities.customization.images.AdImage;
import nl.strohalm.cyclos.entities.members.Element;
import nl.strohalm.cyclos.entities.members.Member;
import nl.strohalm.cyclos.entities.members.Operator;
import nl.strohalm.cyclos.services.ads.AdService;
import nl.strohalm.cyclos.services.elements.ElementService;
import nl.strohalm.cyclos.services.permissions.PermissionService;
import nl.strohalm.cyclos.utils.RelationshipHelper;
import nl.strohalm.cyclos.utils.access.LoggedUser;
import nl.strohalm.cyclos.utils.validation.ValidationException;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;
import nl.strohalm.cyclos.webservices.rest.GenericResponse;
import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 *
 * @author Lue
 */
@Controller
@JsonAutoDetect
public class MemberAdsController extends BaseRestController {

    private AdService adService;
    private PermissionService permissionService;
    private ElementService elementService;

    public void setPermissionService(PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    public void setElementService(ElementService elementService) {
        this.elementService = elementService;
    }

    public AdService getAdService() {
        return adService;
    }

    @Inject
    public void setAdService(final AdService adService) {
        this.adService = adService;
    }

    public static class MemberAdsResponse extends GenericResponse {

        private long memberId;
        private boolean readOnly;

        private List<AdsEntity> ads = new ArrayList<AdsEntity>();

        private boolean editable;
        private boolean maxAds;
        private boolean myAds;
        private int adCount;
        private Collection<AdImage> images;
        private boolean hasImages;

        public boolean isHasImages() {
            return hasImages;
        }

        public void setHasImages(boolean hasImages) {
            this.hasImages = hasImages;
        }

        public Collection<AdImage> getImages() {
            return images;
        }

        public void setImages(Collection<AdImage> images) {
            this.images = images;
        }

        public List<AdsEntity> getAds() {
            return ads;
        }

        public void setAds(List<AdsEntity> ads) {
            this.ads = ads;
        }

        public boolean isEditable() {
            return editable;
        }

        public void setEditable(boolean editable) {
            this.editable = editable;
        }

        public boolean isMaxAds() {
            return maxAds;
        }

        public void setMaxAds(boolean maxAds) {
            this.maxAds = maxAds;
        }

        public boolean isMyAds() {
            return myAds;
        }

        public void setMyAds(boolean myAds) {
            this.myAds = myAds;
        }

        public int getAdCount() {
            return adCount;
        }

        public void setAdCount(int adCount) {
            this.adCount = adCount;
        }

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

        public MemberAdsResponse() {
        }
    }

    @RequestMapping(value = "member/advertisements", method = RequestMethod.GET)
    @ResponseBody
    public MemberAdsResponse memberAds() throws Exception {
        MemberAdsResponse response = new MemberAdsResponse();
        Member member;
        boolean myAds = false;
        boolean editable = false;
        // Read only means that the broker is viewing member ads as a common member
        final boolean brokerViewingAsMember = response.isReadOnly();

        // if the memberId parameter is zero or is equals to the logged user or is equals to the logged operator's member
        if (LoggedUser.user().getId() <= 0 || LoggedUser.user().getId() == LoggedUser.element().getId()
                || (LoggedUser.isOperator() && LoggedUser.user().getId() == ((Operator) LoggedUser.element()).getMember().getId())) {
            if (LoggedUser.isMember()) {
                member = LoggedUser.element();
                editable = permissionService.hasPermission(MemberPermission.ADS_PUBLISH);
            } else if (LoggedUser.isOperator()) {
                member = ((Operator) LoggedUser.element()).getMember();
                editable = permissionService.hasPermission(OperatorPermission.ADS_PUBLISH);
            } else {
                throw new ValidationException();
            }
            myAds = true;
        } else {
            final Element element = elementService.load(LoggedUser.user().getId(), Element.Relationships.USER);

            if (!(element instanceof Member)) {
                throw new ValidationException();
            }

            member = (Member) element;
            if (LoggedUser.isMember()) {
                editable = !brokerViewingAsMember && LoggedUser.isBroker() && permissionService.hasPermission(BrokerPermission.ADS_MANAGE);
            } else if (LoggedUser.isAdministrator()) {
                editable = permissionService.hasPermission(AdminMemberPermission.ADS_MANAGE);
            }
        }

        final AdQuery query = new AdQuery();
        query.fetch(RelationshipHelper.nested(Ad.Relationships.OWNER, Element.Relationships.USER), Ad.Relationships.CURRENCY);
        query.setMyAds(myAds);
        query.setOwner(member);

        // Member viewing another member's ads
        if (!LoggedUser.isAdministrator() && !myAds && !LoggedUser.isBroker()) {
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
        member = elementService.load(member.getId(), Element.Relationships.GROUP);
        final int adCount = ads.size();
        final int maxAdsPerMember = member.getMemberGroup().getMemberSettings().getMaxAdsPerMember();
        //  System.out.println("-----size: " + ads.size());
        List<AdsEntity> addEntityList = new ArrayList();

        for (Ad ad : ads) {
            AdsEntity adEntity = new AdsEntity();
            adEntity.setDescription(ad.getDescription());
            adEntity.setPrice(ad.getPrice());
            adEntity.setStatus(ad.getStatus().name());
            adEntity.setType(ad.getTradeType().name());
            addEntityList.add(adEntity);
        }
        response.setAds(addEntityList);
        response.setAdCount(adCount);
        response.setEditable(editable);
        response.setHasImages(hasImages);
        response.setMyAds(myAds);
        response.setMaxAds(adCount >= maxAdsPerMember);
        response.setStatus(0);
        response.setMessage("!! List of Ads");

        return response;
    }

    public static class AdsEntity {

        private String description;
        private BigDecimal price;
        private String type;
        private String status;

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public BigDecimal getPrice() {
            return price;
        }

        public void setPrice(BigDecimal price) {
            this.price = price;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

    }

}
