package nl.strohalm.cyclos.webservices.rest.members.loangroups;

import java.util.Collection;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import nl.strohalm.cyclos.access.AdminMemberPermission;
import nl.strohalm.cyclos.annotations.Inject;
import nl.strohalm.cyclos.entities.accounts.loans.LoanGroup;
import nl.strohalm.cyclos.entities.accounts.loans.LoanGroupQuery;
import nl.strohalm.cyclos.entities.customization.fields.CustomFieldValue;
import nl.strohalm.cyclos.entities.members.Element;
import nl.strohalm.cyclos.entities.members.Member;
import nl.strohalm.cyclos.exceptions.PermissionDeniedException;
import nl.strohalm.cyclos.services.elements.ElementService;
import nl.strohalm.cyclos.services.loangroups.LoanGroupService;
import nl.strohalm.cyclos.services.permissions.PermissionService;
import nl.strohalm.cyclos.utils.validation.ValidationException;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;
@Controller
public class MemberLoanGroupsController extends BaseRestController{
	private LoanGroupService loanGroupService;
	private PermissionService permissionService;
	private ElementService elementService;

    public LoanGroupService getLoanGroupService() {
        return loanGroupService;
    }

    @Inject
    public void setLoanGroupService(final LoanGroupService loanGroupService) {
        this.loanGroupService = loanGroupService;
    }
    public static class MemberLoanGroupsRequestDTO{
    	private String                                 description;
        private Member                                 member;
        private String                                 name;
        private Boolean                                noLoans;
        private boolean                                notOfMember;
		public Collection<? extends CustomFieldValue> getCustomValues() {
            return getCustomValues();
        }
       

        public String getDescription() {
            return description;
        }

        public Member getMember() {
            return member;
        }

        public String getName() {
            return name;
        }

        public Boolean getNoLoans() {
            return noLoans;
        }

        public boolean isNotOfMember() {
            return notOfMember;
        }

        public void setCustomValues(final Collection<? extends CustomFieldValue> customValues) {
        }

        public void setDescription(final String description) {
            this.description = description;
        }

        public void setMember(final Member member) {
            this.member = member;
        }

        public void setName(final String name) {
            this.name = name;
        }

        public void setNoLoans(final Boolean noLoans) {
            this.noLoans = noLoans;
        }

        public void setNotOfMember(final boolean notOfMember) {
            this.notOfMember = notOfMember;
        }
    	private long              memberId;

        public long getMemberId() {
            return memberId;
        }

        public void setMemberId(final long memberId) {
            this.memberId = memberId;
        }
    }
    
    public static class MemberLoanGroupsResponseDTO{
    	String message;

		public final String getMessage() {
			return message;
		}

		public final void setMessage(String message, List<LoanGroup> list) {
			this.message = message;
		}

		public Element getElement() {
			// TODO Auto-generated method stub
			return null;
		}

		public boolean isMember() {
			// TODO Auto-generated method stub
			return false;
		}

		public boolean isBrokerOf(Member member) {
			// TODO Auto-generated method stub
			return false;
		}
    }
    

    @RequestMapping(value = "",method = RequestMethod.GET)
    @ResponseBody
    protected MemberLoanGroupsResponseDTO executeAction(@RequestBody MemberLoanGroupsRequestDTO form) throws Exception {
        final long id = form.getMemberId();
        boolean myLoanGroups = false;
        boolean byBroker = false;
        boolean editable = false;
        Member member = null;
        MemberLoanGroupsResponseDTO response = new MemberLoanGroupsResponseDTO();
        final Element loggedElement = response.getElement();
        if (id <= 0L || loggedElement.getId().equals(id)) {
            if (response.isMember()) {
                member = (Member) response.getElement();
                myLoanGroups = true;
            }
        } else {
            try {
                member = elementService.load(id, Element.Relationships.USER);
                if (response.isMember()) {
                    if (!response.isBrokerOf(member)) {
                        throw new PermissionDeniedException();
                    } else {
                        byBroker = true;
                    }
                } else {
                    editable = permissionService.hasPermission(AdminMemberPermission.LOAN_GROUPS_MANAGE);
                }
            } catch (final PermissionDeniedException e) {
                throw e;
            } catch (final Exception e) {
                member = null;
            }
        }
        if (member == null) {
            throw new ValidationException();
        }
        if (editable) {
            final LoanGroupQuery query = new LoanGroupQuery();
            query.setMember(member);
            query.setNotOfMember(true);
            response.setMessage("unrelatedLoanGroups", loanGroupService.search(query));
        }

        final LoanGroupQuery query = new LoanGroupQuery();
        query.setMember(member);
        response.setMessage("loanGroups", loanGroupService.search(query));

       /* response.setMessage("member", member);
        response.setMessage("myLoanGroups", myLoanGroups);
        response.setMessage("byBroker", byBroker);
        response.setMessage("editable", editable);*/

        //return context.getInputForward();
        
        return response;

}
}
