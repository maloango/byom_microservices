package nl.strohalm.cyclos.webservices.rest.loangroups;

import nl.strohalm.cyclos.entities.accounts.loans.LoanGroup;
import nl.strohalm.cyclos.entities.members.Member;
import nl.strohalm.cyclos.utils.EntityHelper;
import org.springframework.stereotype.Controller;

import nl.strohalm.cyclos.webservices.rest.BaseRestController;
import nl.strohalm.cyclos.webservices.rest.GenericResponse;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class RemoveMemberFromLoanGroupController extends BaseRestController {

    @RequestMapping(value = "admin/removeMemberFromLoanGroup/{memberId}/{loanGroupId}", method = RequestMethod.GET)
    @ResponseBody
    public GenericResponse removeMember(@PathVariable("memberId") Long memberId, @PathVariable("loanGroupId") Long loanGroupId) {
        GenericResponse response = new GenericResponse();
        final Member member = EntityHelper.reference(Member.class, memberId);
        final LoanGroup loanGroup = EntityHelper.reference(LoanGroup.class, loanGroupId);
        loanGroupService.removeMember(member, loanGroup);
        response.setMessage("loanGroup.memberRemoved");
        response.setStatus(0);
        return response;
    }
    
}
