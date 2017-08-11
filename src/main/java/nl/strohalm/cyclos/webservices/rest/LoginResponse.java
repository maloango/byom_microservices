package nl.strohalm.cyclos.webservices.rest;

import java.util.List;
import nl.strohalm.cyclos.entities.access.User;
import nl.strohalm.cyclos.entities.members.Element;
import nl.strohalm.cyclos.entities.members.records.MemberRecordType;
import nl.strohalm.cyclos.utils.ElementVO;
import nl.strohalm.cyclos.utils.UserVO;

/**
 *
 * @author Lue Infoservices
 */
public class LoginResponse {
    
        private UserVO loggedUser;
         private User user;
        private ElementVO loggedElement;
        private Long loggedUserId;
        private Long uniqueMemberPosId;
        private String errorMessage;
        private int errorCode;
        private String forwardPage;

        private boolean isAdmin;
        private boolean isMember;
        private boolean isBroker;
        private boolean isOperator;
        private boolean isPosWeb;

        private boolean isBuyer;
        private boolean isSeller;
        private boolean isIssuer;

        boolean hasAccounts;
        boolean singleAccount;
        boolean hasDocuments;
        boolean hasLoanGroups;
        boolean hasGeneralReferences;
        boolean hasTransactionFeedbacks;
        boolean hasPin;
        boolean hasExternalChannels;
        boolean hasCards;
        boolean hasPos;
        boolean hasCommissionContracts;
        boolean loggedMemberHasGuarantees;

        private List<MemberRecordType> memberRecordTypesInMenu;

        public UserVO getLoggedUser() {
            return loggedUser;
        }

        public void setLoggedUser(UserVO loggedUser) {
            this.loggedUser = loggedUser;
        }

        public ElementVO getLoggedElement() {
            return loggedElement;
        }

        public void setLoggedElement(ElementVO loggedElement) {
            this.loggedElement = loggedElement;
        }

        public Long getLoggedUserId() {
            return loggedUserId;
        }

        public void setLoggedUserId(Long loggedUserId) {
            this.loggedUserId = loggedUserId;
        }

        public String getErrorMessage() {
            return errorMessage;
        }

        public void setErrorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
        }

        public String getForwardPage() {
            return forwardPage;
        }

        public void setForwardPage(String forwardPage) {
            this.forwardPage = forwardPage;
        }

        public boolean isIsAdmin() {
            return isAdmin;
        }

        public void setIsAdmin(boolean isAdmin) {
            this.isAdmin = isAdmin;
        }

        public boolean isIsMember() {
            return isMember;
        }

        public void setIsMember(boolean isMember) {
            this.isMember = isMember;
        }

        public boolean isIsBroker() {
            return isBroker;
        }

        public void setIsBroker(boolean isBroker) {
            this.isBroker = isBroker;
        }

        public boolean isIsOperator() {
            return isOperator;
        }

        public void setIsOperator(boolean isOperator) {
            this.isOperator = isOperator;
        }

        public boolean isIsPosWeb() {
            return isPosWeb;
        }

        public void setIsPosWeb(boolean isPosWeb) {
            this.isPosWeb = isPosWeb;
        }

        public boolean isIsBuyer() {
            return isBuyer;
        }

        public void setIsBuyer(boolean isBuyer) {
            this.isBuyer = isBuyer;
        }

        public boolean isIsSeller() {
            return isSeller;
        }

        public void setIsSeller(boolean isSeller) {
            this.isSeller = isSeller;
        }

        public boolean isIsIssuer() {
            return isIssuer;
        }

        public void setIsIssuer(boolean isIssuer) {
            this.isIssuer = isIssuer;
        }

        public boolean isHasAccounts() {
            return hasAccounts;
        }

        public void setHasAccounts(boolean hasAccounts) {
            this.hasAccounts = hasAccounts;
        }

        public boolean isSingleAccount() {
            return singleAccount;
        }

        public void setSingleAccount(boolean singleAccount) {
            this.singleAccount = singleAccount;
        }

        public boolean isHasDocuments() {
            return hasDocuments;
        }

        public void setHasDocuments(boolean hasDocuments) {
            this.hasDocuments = hasDocuments;
        }

        public boolean isHasLoanGroups() {
            return hasLoanGroups;
        }

        public void setHasLoanGroups(boolean hasLoanGroups) {
            this.hasLoanGroups = hasLoanGroups;
        }

        public boolean isHasGeneralReferences() {
            return hasGeneralReferences;
        }

        public void setHasGeneralReferences(boolean hasGeneralReferences) {
            this.hasGeneralReferences = hasGeneralReferences;
        }

        public boolean isHasTransactionFeedbacks() {
            return hasTransactionFeedbacks;
        }

        public void setHasTransactionFeedbacks(boolean hasTransactionFeedbacks) {
            this.hasTransactionFeedbacks = hasTransactionFeedbacks;
        }

        public boolean isHasPin() {
            return hasPin;
        }

        public void setHasPin(boolean hasPin) {
            this.hasPin = hasPin;
        }

        public boolean isHasExternalChannels() {
            return hasExternalChannels;
        }

        public void setHasExternalChannels(boolean hasExternalChannels) {
            this.hasExternalChannels = hasExternalChannels;
        }

        public boolean isHasCards() {
            return hasCards;
        }

        public void setHasCards(boolean hasCards) {
            this.hasCards = hasCards;
        }

        public boolean isHasPos() {
            return hasPos;
        }

        public void setHasPos(boolean hasPos) {
            this.hasPos = hasPos;
        }

        public boolean isHasCommissionContracts() {
            return hasCommissionContracts;
        }

        public void setHasCommissionContracts(boolean hasCommissionContracts) {
            this.hasCommissionContracts = hasCommissionContracts;
        }

        public List<MemberRecordType> getMemberRecordTypesInMenu() {
            return memberRecordTypesInMenu;
        }

        public void setMemberRecordTypesInMenu(List<MemberRecordType> memberRecordTypesInMenu) {
            this.memberRecordTypesInMenu = memberRecordTypesInMenu;
        }

    public Long getUniqueMemberPosId() {
        return uniqueMemberPosId;
    }

    public void setUniqueMemberPosId(Long uniqueMemberPosId) {
        this.uniqueMemberPosId = uniqueMemberPosId;
    }

    public boolean isLoggedMemberHasGuarantees() {
        return loggedMemberHasGuarantees;
    }

    public void setLoggedMemberHasGuarantees(boolean loggedMemberHasGuarantees) {
        this.loggedMemberHasGuarantees = loggedMemberHasGuarantees;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
    
        
}
