package nl.strohalm.cyclos.webservices.rest.members.activities;

import javax.servlet.http.HttpServletRequest;

import nl.strohalm.cyclos.annotations.Inject;
import nl.strohalm.cyclos.controls.ActionContext;
//import nl.strohalm.cyclos.controls.members.activities.ActivitiesForm;
import nl.strohalm.cyclos.entities.ads.Ad;
import nl.strohalm.cyclos.entities.members.Element;
import nl.strohalm.cyclos.entities.members.Member;
import nl.strohalm.cyclos.entities.members.Reference;
import nl.strohalm.cyclos.services.elements.ActivitiesVO;
import nl.strohalm.cyclos.services.elements.ElementService;
import nl.strohalm.cyclos.services.elements.MemberService;
import nl.strohalm.cyclos.utils.RequestHelper;
import nl.strohalm.cyclos.utils.validation.ValidationException;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;

import org.apache.struts.action.ActionForward;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class ActivitiesController extends BaseRestController {
	private MemberService memberService;
	private ElementService elementService;

	public MemberService getMemberService() {
		return memberService;
	}

	@Inject
	public void setMemberService(final MemberService memberService) {
		this.memberService = memberService;
	}

	public static class ActivitiesRequestDto {
		private long memberId;
		private Member member;
		private Element element;

		public Member getMember() {
			return member;
		}

		public void setMember(Member member) {
			this.member = member;
		}

		public Element getElement() {
			return element;
		}

		public void setElement(Element element) {
			this.element = element;
		}

		public long getMemberId() {
			return memberId;
		}

		public void setMemberId(final long memberId) {
			this.memberId = memberId;
		}
	}

	public static class ActivitiesResponseDto {
		// private String message;
		private boolean myActivities;
		private Member member;
		private ActivitiesVO activities;

		public boolean isMyActivities() {
			return myActivities;
		}

		public void setMyActivities(boolean myActivities) {
			this.myActivities = myActivities;
		}

		public Member getMember() {
			return member;
		}

		public void setMember(Member member) {
			this.member = member;
		}

		public ActivitiesVO getActivities() {
			return activities;
		}

		public void setActivities(ActivitiesVO activities) {
			this.activities = activities;
		}

		public ActivitiesResponseDto(boolean myActivities, Member member,
				ActivitiesVO activities) {
			super();
			this.myActivities = myActivities;
			this.member = member;
			this.activities = activities;
		}
               public ActivitiesResponseDto(){
                }

	}

	@RequestMapping(value = "admin/activities", method = RequestMethod.POST)
	@ResponseBody
	protected ActivitiesResponseDto executeAction(
			@RequestBody ActivitiesRequestDto form) throws Exception {
		boolean myActivities = false;

		Member member;
		if (form.getMemberId() <= 0
				|| form.getElement().getId().equals(form.getMemberId())) {
			member = form.getMember();
			myActivities = true;
		} else {
			final Element element = elementService.load(form.getMemberId(),
					Element.Relationships.USER, Element.Relationships.GROUP);
			if (!(element instanceof Member)) {
				throw new ValidationException();
			}
			member = (Member) element;
		}

		// Get the activities
		final ActivitiesVO activities = memberService.getActivities(member);

		ActivitiesResponseDto response = new ActivitiesResponseDto(
				myActivities, member, activities);
		return response;
	}
}
