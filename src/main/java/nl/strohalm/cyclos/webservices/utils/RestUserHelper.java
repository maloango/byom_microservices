/*
    This file is part of Cyclos (www.cyclos.org).
    A project of the Social Trade Organisation (www.socialtrade.org).

    Cyclos is free software; you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation; either version 2 of the License, or
    (at your option) any later version.

    Cyclos is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with Cyclos; if not, write to the Free Software
    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA

 */
package nl.strohalm.cyclos.webservices.utils;

import java.io.Serializable;

import nl.strohalm.cyclos.entities.access.AdminUser;
import nl.strohalm.cyclos.entities.access.MemberUser;
import nl.strohalm.cyclos.entities.access.OperatorUser;
import nl.strohalm.cyclos.entities.access.TransactionPassword;
import nl.strohalm.cyclos.entities.access.User;
import nl.strohalm.cyclos.entities.access.User.TransactionPasswordStatus;
import nl.strohalm.cyclos.entities.accounts.AccountOwner;
import nl.strohalm.cyclos.entities.accounts.AccountType;
import nl.strohalm.cyclos.entities.accounts.MemberGroupAccountSettings;
import nl.strohalm.cyclos.entities.exceptions.EntityNotFoundException;
import nl.strohalm.cyclos.entities.groups.Group;
import nl.strohalm.cyclos.entities.groups.OperatorGroup;
import nl.strohalm.cyclos.entities.members.Element;
import nl.strohalm.cyclos.entities.members.Member;
import nl.strohalm.cyclos.services.access.AccessService;
import nl.strohalm.cyclos.services.access.exceptions.BlockedCredentialsException;
import nl.strohalm.cyclos.services.access.exceptions.InvalidCredentialsException;
import nl.strohalm.cyclos.services.groups.GroupService;
import nl.strohalm.cyclos.utils.RelationshipHelper;
import nl.strohalm.cyclos.utils.validation.ValidationException;

/**
 * Abstract class for the Struts context
 * @author luis
 */
public  class RestUserHelper implements Serializable {

    private static final long         serialVersionUID = 7565129435156786075L;

    private final User                user;
    private final AccessService accessService;
    private  final GroupService groupService;

    public RestUserHelper(final User user,final GroupService groupService,final AccessService accessService) {
        this.user = user;
        this.groupService=groupService;
        this.accessService=accessService;
    }

   
    /**
     * Checks the transaction password for the logged user
     */
    public void checkTransactionPassword(final String transactionPassword) {
        try {
          //  final AccessService accessService = SpringHelper.bean(getServletContext(), AccessService.class);
            accessService.checkTransactionPassword(transactionPassword);
        } catch (final InvalidCredentialsException e) {
            throw new ValidationException("transactionPassword.error.invalid");
        } catch (final BlockedCredentialsException e) {
//            final HttpSession session = getSession();
//            session.setAttribute("errorReturnTo", session.getAttribute("pathPrefix") + "/home");
            throw new ValidationException("transactionPassword.error.blockedByTrials");
        } catch (final RuntimeException e) {
            throw e;
        }
    }

   
    /**
     * Returns the logged user's account owner
     */
    public AccountOwner getAccountOwner() {
        try {
            final Element element = getElement();
            return element.getAccountOwner();
        } catch (final NullPointerException e) {
            return null;
        }
    }

  
    /**
     * Returns the logged element
     */
    @SuppressWarnings("unchecked")
    public <E extends Element> E getElement() {
        return (E) user.getElement();
    }

    /**
     * Returns the logged element's group
     */
    @SuppressWarnings("unchecked")
    public <G extends Group> G getGroup() {
        final Element element = getElement();
        return (G) element.getGroup();
    }

  

  
    /**
     * Returns the logged user
     */
    @SuppressWarnings("unchecked")
    public <U extends User> U getUser() {
        return (U) user;
    }

    /**
   
    /**
     * Returns whether the logged user is an administrator
     */
    public boolean isAdmin() {
        return user instanceof AdminUser;
    }

    /**
     * Returns whether the logged user is a broker
     */
    public boolean isBroker() {
        if (!isMember()) {
            return false;
        }
        final Member member = getElement();
        return member.getMemberGroup().isBroker();
    }

    /**
     * Returns whether the logged user is a member (a broker is a member too)
     */
    public boolean isMember() {
        return user instanceof MemberUser;
    }

    /**
     * Returns whether the logged user is a member but not a broker
     */
    public boolean isMemberAndNotBroker() {
        return isMember() && !isBroker();
    }

    /**
     * Returns whether the logged user is an operator
     */
    public boolean isOperator() {
        return user instanceof OperatorUser;
    }

    /**
     * Returns whether the transaction password is used for the logged user
     */
    public boolean isTransactionPasswordEnabled() {
        Group loggedGroup = getGroup();
        if (loggedGroup instanceof OperatorGroup) {
           // final GroupService groupService = SpringHelper.bean(getServletContext(), GroupService.class);
            loggedGroup = groupService.load(loggedGroup.getId(), RelationshipHelper.nested(OperatorGroup.Relationships.MEMBER, Element.Relationships.GROUP));
        }
        final TransactionPassword transactionPassword = loggedGroup.getBasicSettings().getTransactionPassword();
        return transactionPassword.isUsed();
    }

    public boolean isTransactionPasswordEnabled(final AccountType accountType) {
        if (!isTransactionPasswordEnabled()) {
            return false;
        } else if (isAdmin()) {
            return true; // the group settings is true
        } else { // checks the member-group settings
            final Member member = (Member) getAccountOwner();
           // final GroupService groupService = SpringHelper.bean(getServletContext(), GroupService.class);
            try {
                final MemberGroupAccountSettings mgas = groupService.loadAccountSettings(member.getGroup().getId(), accountType.getId());
                return mgas.isTransactionPasswordRequired();
            } catch (final EntityNotFoundException e) {
                return false;
            }
        }
    }

  
    /**
     * Throws a ValidationException if the transaction password is not active
     */
    public void validateTransactionPassword() {
        validateTransactionPassword(getUser().getTransactionPasswordStatus());
    }

    protected void validateTransactionPassword(final TransactionPasswordStatus tpStatus) {
        String errorKey = null;
        switch (tpStatus) {
            case BLOCKED:
                errorKey = "transactionPassword.error.blocked";
                break;
            case PENDING:
            case NEVER_CREATED:
                errorKey = "transactionPassword.error.pending";
                break;
        }
        if (errorKey != null) {
         //   request.getSession().setAttribute("errorReturnTo", request.getSession().getAttribute("pathPrefix") + "/home");
           // request.getSession().setAttribute("errorButtonKey", "global.ok");
            throw new ValidationException(errorKey);
        }
    }
}
