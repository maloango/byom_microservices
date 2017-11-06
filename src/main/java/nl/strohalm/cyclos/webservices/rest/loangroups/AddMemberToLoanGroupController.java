package nl.strohalm.cyclos.webservices.rest.loangroups;

import nl.strohalm.cyclos.entities.accounts.loans.LoanGroup;
import nl.strohalm.cyclos.entities.members.Member;
import nl.strohalm.cyclos.services.loangroups.exceptions.MemberAlreadyInListException;
import nl.strohalm.cyclos.utils.EntityHelper;
import nl.strohalm.cyclos.utils.validation.ValidationException;
import org.springframework.stereotype.Controller;

import nl.strohalm.cyclos.webservices.rest.BaseRestController;
import nl.strohalm.cyclos.webservices.rest.GenericResponse;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class AddMemberToLoanGroupController extends BaseRestController {
    
    public static class AddMemberParameters {
        
        private Long memberId;
        private Long loanGroupId;
        
        public Long getMemberId() {
            return memberId;
        }
        
        public void setMemberId(Long memberId) {
            this.memberId = memberId;
        }

        public Long getLoanGroupId() {
            return loanGroupId;
        }

        public void setLoanGroupId(Long loanGroupId) {
            this.loanGroupId = loanGroupId;
        }
        
   
        
    }
    
    @RequestMapping(value = "admin/addMemberToLoanGroup", method = RequestMethod.POST)
    @ResponseBody
    public GenericResponse addMember(@RequestBody AddMemberParameters params) {
        GenericResponse response = new GenericResponse();
        
        final long memberId = params.getMemberId();
        final long loanGroupId = params.getLoanGroupId();
        if (loanGroupId <= 0L || memberId <= 0L) {
            throw new ValidationException();
        }
        final LoanGroup loanGroup = EntityHelper.reference(LoanGroup.class, loanGroupId);
        final Member member = EntityHelper.reference(Member.class, memberId);
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
        response.setStatus(0);
        return response;
    }
    
    protected boolean add() {
        return true;
    }
}
