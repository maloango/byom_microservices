package nl.strohalm.cyclos.webservices.rest.loangroups;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import nl.strohalm.cyclos.annotations.Inject;
import nl.strohalm.cyclos.controls.restapi.BaseRestController;
import nl.strohalm.cyclos.entities.accounts.loans.LoanGroup;
import nl.strohalm.cyclos.entities.members.Member;
import nl.strohalm.cyclos.services.loangroups.LoanGroupService;
import nl.strohalm.cyclos.services.loangroups.exceptions.MemberAlreadyInListException;
import nl.strohalm.cyclos.utils.EntityHelper;
import nl.strohalm.cyclos.utils.validation.ValidationException;
@Controller
public class BaseLoanGroupMembershipController extends BaseRestController{

	private LoanGroupService loanGroupService;

    public LoanGroupService getLoanGroupService() {
        return loanGroupService;
    }

    @Inject
    public void setLoanGroupService(final LoanGroupService loanGroupService) {
        this.loanGroupService = loanGroupService;
    }

    protected boolean add() {
		return false;
	}
    
    public static class BaseLoanGroupMembershipRequestDTO{
    	 private long              loanGroupId;
    	    private long              memberId;

    	    public long getLoanGroupId() {
    	        return loanGroupId;
    	    }

    	    public long getMemberId() {
    	        return memberId;
    	    }

    	    public void setLoanGroupId(final long loanGroupId) {
    	        this.loanGroupId = loanGroupId;
    	    }

    	    public void setMemberId(final long memberId) {
    	        this.memberId = memberId;
    	    }
    }
    
    public static class BaseLoanGroupMembershipResponseDTo{
    	
    	String message;
		public final String getMessage() {
			return message;
		}
		public final void setMessage(String message) {
			this.message = message;
		}
		Map<String, Object> param;
		public BaseLoanGroupMembershipResponseDTo(String message, Map<String, Object> param) {
			super();
			this.message = message;
			this.param = param;
		}
    }

    @RequestMapping(value = "",method =RequestMethod.GET)
    @ResponseBody
    protected BaseLoanGroupMembershipResponseDTo executeAction(@RequestBody BaseLoanGroupMembershipRequestDTO form) throws Exception {
        final long loanGroupId = form.getLoanGroupId();
        final long memberId = form.getMemberId();
        if (loanGroupId <= 0L || memberId <= 0L) {
            throw new ValidationException();
        }
       
        final LoanGroup loanGroup = EntityHelper.reference(LoanGroup.class, loanGroupId);
        final Member member = EntityHelper.reference(Member.class, memberId);
        final Map<String, Object> params = new HashMap<String, Object>();
        String message = null ;
        params.put("loanGroupId", loanGroupId);
        params.put("memberId", memberId);
        BaseLoanGroupMembershipResponseDTo response = new BaseLoanGroupMembershipResponseDTo(message, params);
  
        if (add()) {
            try {
                loanGroupService.addMember(member, loanGroup);
                response.setMessage("loanGroup.memberAdded");
                
            } catch (final MemberAlreadyInListException e) {
                response.setMessage("loanGroup.error.memberAlreadyInList");
            }
        } else {
            loanGroupService.removeMember(member, loanGroup);
            response.setMessage("loanGroup.memberRemoved");
        }
       
        /*params.put("loanGroupId", loanGroupId);
        params.put("memberId", memberId);*/
        return response;
    }
}
