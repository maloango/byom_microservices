package nl.strohalm.cyclos.webservices.rest.members.adinterests;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import nl.strohalm.cyclos.entities.accounts.Currency;
import nl.strohalm.cyclos.entities.ads.Ad;
import nl.strohalm.cyclos.entities.ads.AdCategory;
import nl.strohalm.cyclos.entities.groups.GroupFilter;
import nl.strohalm.cyclos.entities.members.Member;
import nl.strohalm.cyclos.entities.members.adInterests.AdInterest;
import nl.strohalm.cyclos.entities.members.adInterests.AdInterestQuery;
import nl.strohalm.cyclos.services.elements.AdInterestService;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;
@Controller
public class ListAdInterestsController extends BaseRestController{
	
	
	public static class ListAdInterestsRequestDTO{
		private Member            owner;
	    private String            title;
	    private Ad.TradeType      type;
	    private AdCategory        category;
	    private Member            member;
	    private GroupFilter       groupFilter;
	    private BigDecimal        initialPrice;
	    private BigDecimal        finalPrice;
	    private Currency          currency;
	    private String            keywords;
	    private AdInterestService adInterestService;

	    public AdCategory getCategory() {
	        return category;
	    }

	    public Currency getCurrency() {
	        return currency;
	    }

	    public BigDecimal getFinalPrice() {
	        return finalPrice;
	    }

	    public GroupFilter getGroupFilter() {
	        return groupFilter;
	    }

	    public BigDecimal getInitialPrice() {
	        return initialPrice;
	    }

	    public String getKeywords() {
	        return keywords;
	    }

	    public Member getMember() {
	        return member;
	    }

	    public Member getOwner() {
	        return owner;
	    }

	    public String getTitle() {
	        return title;
	    }

	    public Ad.TradeType getType() {
	        return type;
	    }

	    public void setCategory(final AdCategory category) {
	        this.category = category;
	    }

	    public void setCurrency(final Currency currency) {
	        this.currency = currency;
	    }

	    public void setFinalPrice(final BigDecimal finalPrice) {
	        this.finalPrice = finalPrice;
	    }

	    public void setGroupFilter(final GroupFilter groupFilter) {
	        this.groupFilter = groupFilter;
	    }

	    public void setInitialPrice(final BigDecimal initialPrice) {
	        this.initialPrice = initialPrice;
	    }

	    public void setKeywords(final String keywords) {
	        this.keywords = keywords;
	    }

	    public void setMember(final Member member) {
	        this.member = member;
	    }

	    public void setOwner(final Member owner) {
	        this.owner = owner;
	    }

	    public void setTitle(final String title) {
	        this.title = title;
	    }

	    public void setType(final Ad.TradeType type) {
	        this.type = type;
	    }

	    @Override
	    public String toString() {
	        return title != null ? title : "";
	    }

		
	
	public static class ListAdInterestsResponseDTO{
		List<AdInterest> adInterests;

		public final List<AdInterest> getAdInterests() {
			return adInterests;
		}

		public final void setAdInterests(List<AdInterest> adInterests) {
			this.adInterests = adInterests;
		}

		public ListAdInterestsResponseDTO(List<AdInterest> adInterests) {
			super();
			this.adInterests = adInterests;
		}
	}
	
	@RequestMapping(value = "member/listAdInterests", method = RequestMethod.GET)
	@ResponseBody
	    protected ListAdInterestsResponseDTO executeAction(@RequestBody ListAdInterestsRequestDTO form) throws Exception {
	        //final Member owner = context.getElement();
	        final AdInterestQuery query = new AdInterestQuery();
	        query.setOwner(owner);
	        final List<AdInterest> adInterests = adInterestService.search(query);
	        ListAdInterestsResponseDTO response = new ListAdInterestsResponseDTO(adInterests);
	        return response;
	
	       
	    }
	}
}




