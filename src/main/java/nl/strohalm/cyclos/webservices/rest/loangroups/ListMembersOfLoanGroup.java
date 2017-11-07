/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.strohalm.cyclos.webservices.rest.loangroups;

import java.util.ArrayList;
import java.util.List;
import nl.strohalm.cyclos.entities.accounts.loans.LoanGroup;
import nl.strohalm.cyclos.entities.members.Member;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;
import nl.strohalm.cyclos.webservices.rest.GenericResponse;
import nl.strohalm.cyclos.webservices.rest.loans.GrantLoanController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 *
 * @author Lue Infoservices
 */
@Controller
public class ListMembersOfLoanGroup extends BaseRestController {
    
    public static class ListMembersResponse extends GenericResponse {
        
        private List<MemberEntity> members;
        
        public List<MemberEntity> getMembers() {
            return members;
        }
        
        public void setMembers(List<MemberEntity> members) {
            this.members = members;
        }
        
    }
    
    public static class MemberEntity {
        
        private Long id;
        private String name;
        private String userName;

        public String getUserName() {
            return userName;
        }

        public void setUserName(String userName) {
            this.userName = userName;
        }
        
        
        public Long getId() {
            return id;
        }
        
        public void setId(Long id) {
            this.id = id;
        }
        
        public String getName() {
            return name;
        }
        
        public void setName(String name) {
            this.name = name;
        }
    }
    
    @RequestMapping(value = "admin/listMemberOfLoanGroup/{loanGroupId}", method = RequestMethod.GET)
    @ResponseBody
    public ListMembersResponse listMembers(@PathVariable("loanGroupId") Long loanGroupId) {
        ListMembersResponse response = new ListMembersResponse();
        LoanGroup loanGroup = null;
        loanGroup = loanGroupService.load(loanGroupId, LoanGroup.Relationships.MEMBERS);
        final List<Member> membersInGroup = new ArrayList<Member>(loanGroup.getMembers());
        List<MemberEntity> members = new ArrayList();
        for (Member m : membersInGroup) {
            MemberEntity entity = new MemberEntity();
            entity.setId(m.getId());
            entity.setName(m.getName());
            entity.setUserName(m.getUsername());
            members.add(entity);
        }
        response.setMembers(members);
        response.setMessage("");
        response.setStatus(0);
        return response;
    }
}
