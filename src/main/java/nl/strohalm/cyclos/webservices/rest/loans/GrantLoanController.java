package nl.strohalm.cyclos.webservices.rest.loans;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import nl.strohalm.cyclos.access.AdminMemberPermission;
import nl.strohalm.cyclos.entities.accounts.SystemAccountType;
import nl.strohalm.cyclos.entities.accounts.loans.LoanGroup;
import nl.strohalm.cyclos.entities.accounts.loans.LoanGroupQuery;
import nl.strohalm.cyclos.entities.accounts.transactions.TransferType;
import nl.strohalm.cyclos.entities.accounts.transactions.TransferTypeQuery;
import nl.strohalm.cyclos.entities.groups.AdminGroup;
import nl.strohalm.cyclos.entities.groups.Group;
import nl.strohalm.cyclos.entities.members.Element;
import nl.strohalm.cyclos.entities.members.Member;
import nl.strohalm.cyclos.services.transactions.TransactionContext;
import nl.strohalm.cyclos.utils.access.LoggedUser;
import nl.strohalm.cyclos.utils.validation.ValidationException;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;
import nl.strohalm.cyclos.webservices.rest.GenericResponse;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class GrantLoanController extends BaseRestController {

    public static class GrantLoanResponse extends GenericResponse {

        private MemberEntity member;
        private LoanGroupEntity loanGroupEntity;
        private List<TransferTypeEntity> transferTypeEntity;
        private List<LoanGroupEntity> loanGroupsEntity;
        private List<MemberEntity> members;

        public List<MemberEntity> getMembers() {
            return members;
        }

        public void setMembers(List<MemberEntity> members) {
            this.members = members;
        }

        public List<LoanGroupEntity> getLoanGroupsEntity() {
            return loanGroupsEntity;
        }

        public void setLoanGroupsEntity(List<LoanGroupEntity> loanGroupsEntity) {
            this.loanGroupsEntity = loanGroupsEntity;
        }

        public List<TransferTypeEntity> getTransferTypeEntity() {
            return transferTypeEntity;
        }

        public void setTransferTypeEntity(List<TransferTypeEntity> transferTypeEntity) {
            this.transferTypeEntity = transferTypeEntity;
        }

        public LoanGroupEntity getLoanGroupEntity() {
            return loanGroupEntity;
        }

        public void setLoanGroupEntity(LoanGroupEntity loanGroupEntity) {
            this.loanGroupEntity = loanGroupEntity;
        }

        public MemberEntity getMember() {
            return member;
        }

        public void setMember(MemberEntity member) {
            this.member = member;
        }

    }

    public static class MemberEntity {

        private Long id;
        private String name;

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

    public static class LoanGroupEntity {

        private Long id;
        private String name;

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

    public static class TransferTypeEntity {

        private Long id;
        private String name;

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

    public static class GrantLoanParameters {

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

    @RequestMapping(value = "admin/grantLoan", method = RequestMethod.POST)
    @ResponseBody
    public GrantLoanResponse loanData(@RequestBody GrantLoanParameters params) {
        GrantLoanResponse response = new GrantLoanResponse();
        Member member = null;
        LoanGroup loanGroup = null;
        if (params.getMemberId() > 0L) {
            final Element element = elementService.load(params.getMemberId(), Element.Relationships.USER);
            if (element instanceof Member) {
                member = (Member) element;
            }

            MemberEntity memberEntity = new MemberEntity();
            memberEntity.setId(member.getId());
            memberEntity.setName(member.getName());
            response.setMember(memberEntity);
            System.out.println("----member: " + memberEntity);
        }
        if (params.getLoanGroupId() > 0L) {
            loanGroup = loanGroupService.load(params.getLoanGroupId(), LoanGroup.Relationships.MEMBERS);
            LoanGroupEntity loanGroupEntity = new LoanGroupEntity();
            loanGroupEntity.setId(loanGroup.getId());
            loanGroupEntity.setName(loanGroup.getName());
            response.setLoanGroupEntity(loanGroupEntity);
            System.out.println("----loangGroup: " + loanGroupEntity);
        }
        if (loanGroup != null) {
            final List<Member> membersInGroup = new ArrayList<Member>(loanGroup.getMembers());
            List<MemberEntity> members = new ArrayList();
            for (Member m : membersInGroup) {
                MemberEntity entity = new MemberEntity();
                entity.setId(m.getId());
                entity.setName(m.getName());
                members.add(entity);
            }
            response.setMembers(members);
        }
        if (member == null && loanGroup == null) {
            throw new ValidationException();
        }

       

            AdminGroup adminGroup = LoggedUser.group();
            adminGroup = groupService.load(adminGroup.getId(), AdminGroup.Relationships.VIEW_INFORMATION_OF, Group.Relationships.TRANSFER_TYPES);

            // Get the possible transfer types
            List<TransferType> transferTypes;
            final ArrayList<SystemAccountType> systemAccounts = new ArrayList<SystemAccountType>(adminGroup.getViewInformationOf());
            if (CollectionUtils.isEmpty(systemAccounts)) {
                transferTypes = Collections.emptyList();
            } else {
                final TransferTypeQuery ttQuery = new TransferTypeQuery();
                ttQuery.setContext(TransactionContext.LOAN);
                ttQuery.setToOwner(null);
                ttQuery.setUsePriority(true);
                ttQuery.setFromAccountTypes(systemAccounts);
                transferTypes = transferTypeService.search(ttQuery);
                // Remove transfer types without permission
                final Collection<TransferType> transferTypesWithPermission = adminGroup.getTransferTypes();
                for (final Iterator<TransferType> iter = transferTypes.iterator(); iter.hasNext();) {
                    final TransferType transferType = iter.next();
                    if (!transferTypesWithPermission.contains(transferType)) {
                        iter.remove();
                    }
                }
            }
            if (transferTypes.isEmpty()) {
                response.setMessage("loan.error.noTransferType");
            }
            List<TransferTypeEntity> TransferTypeEntityList = new ArrayList();
            for (TransferType type : transferTypes) {
                TransferTypeEntity transferTypeEntity = new TransferTypeEntity();
                transferTypeEntity.setId(type.getId());
                transferTypeEntity.setName(type.getName());
                TransferTypeEntityList.add(transferTypeEntity);
            }
            response.setTransferTypeEntity(TransferTypeEntityList);

            if (permissionService.hasPermission(AdminMemberPermission.LOAN_GROUPS_VIEW)) {
                // Get the loan groups of this member
                final LoanGroupQuery lgQuery = new LoanGroupQuery();
                lgQuery.setMember(member);
                final List<LoanGroup> loanGroups = loanGroupService.search(lgQuery);
                List<LoanGroupEntity> loanGroupEntityList = new ArrayList();
                for (LoanGroup group : loanGroups) {
                    LoanGroupEntity entity = new LoanGroupEntity();
                    entity.setId(group.getId());
                    entity.setName(group.getName());
                    loanGroupEntityList.add(entity);
                }
                //response.setLoanGroupsEntity(loanGroupEntityList);
            }
       

        response.setMessage("");
        response.setStatus(0);
        return response;

    }
}
