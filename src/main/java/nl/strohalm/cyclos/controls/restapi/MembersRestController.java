/*
   This file is part of Cyclos.

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
package nl.strohalm.cyclos.controls.restapi;

import java.math.BigDecimal;
import java.util.*;

import javax.servlet.http.HttpServletRequest;

import nl.strohalm.cyclos.entities.access.MemberUser;
import nl.strohalm.cyclos.entities.access.PrincipalType;
import nl.strohalm.cyclos.entities.access.User;
import nl.strohalm.cyclos.entities.accounts.MemberAccountType;
import nl.strohalm.cyclos.entities.accounts.MemberGroupAccountSettings;
import nl.strohalm.cyclos.entities.accounts.SystemAccountOwner;
import nl.strohalm.cyclos.entities.customization.fields.*;
import nl.strohalm.cyclos.entities.exceptions.EntityNotFoundException;
import nl.strohalm.cyclos.entities.groups.MemberGroup;
import nl.strohalm.cyclos.entities.members.Contact;
import nl.strohalm.cyclos.entities.members.Member;
import nl.strohalm.cyclos.entities.members.imports.ImportedMember;
import nl.strohalm.cyclos.entities.members.imports.ImportedMemberRecord;
import nl.strohalm.cyclos.entities.members.imports.ImportedMemberRecordCustomFieldValue;
import nl.strohalm.cyclos.entities.members.imports.MemberImport;
import nl.strohalm.cyclos.entities.members.records.MemberRecordType;
import nl.strohalm.cyclos.entities.settings.AccessSettings;
import nl.strohalm.cyclos.entities.settings.LocalSettings;
import nl.strohalm.cyclos.services.accounts.CreditLimitDTO;
import nl.strohalm.cyclos.services.customization.MemberCustomFieldService;
import nl.strohalm.cyclos.services.elements.ContactService;
import nl.strohalm.cyclos.services.elements.ElementService;
import nl.strohalm.cyclos.services.elements.ElementServiceLocal;
import nl.strohalm.cyclos.services.elements.MemberService;
import nl.strohalm.cyclos.services.fetch.FetchServiceLocal;
import nl.strohalm.cyclos.services.settings.SettingsServiceLocal;
import nl.strohalm.cyclos.services.transactions.TransactionContext;
import nl.strohalm.cyclos.services.transactions.TransferDTO;
import nl.strohalm.cyclos.utils.CustomFieldHelper;
import nl.strohalm.cyclos.utils.DateHelper;
import nl.strohalm.cyclos.utils.access.LoggedUser;
import nl.strohalm.cyclos.utils.conversion.CalendarConverter;
import nl.strohalm.cyclos.utils.csv.UnknownColumnException;
import nl.strohalm.cyclos.utils.validation.*;
import nl.strohalm.cyclos.webservices.WebServiceContext;
import nl.strohalm.cyclos.webservices.members.FullTextMemberSearchParameters;
import nl.strohalm.cyclos.webservices.members.MemberResultPage;
import nl.strohalm.cyclos.webservices.model.MemberDataVO;
import nl.strohalm.cyclos.webservices.model.MemberVO;
import nl.strohalm.cyclos.webservices.model.MyProfileVO;
import nl.strohalm.cyclos.webservices.model.RegistrationFieldValueVO;

import nl.strohalm.cyclos.webservices.rest.MembersRestPayload;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Controller which handles /members paths
 * 
 * @author luis
 */
@Controller
public class MembersRestController extends BaseRestController {

    /**
     * Parameters used to update the profile
     * @author luis
     */
    public static class UpdateProfileParameters {
        private String                         name;
        private String                         username;
        private String                         email;
        private List<RegistrationFieldValueVO> customValues;

        public List<RegistrationFieldValueVO> getCustomValues() {
            return customValues;
        }

        public String getEmail() {
            return email;
        }

        public String getName() {
            return name;
        }

        public String getUsername() {
            return username;
        }

        public void setCustomValues(final List<RegistrationFieldValueVO> customValues) {
            this.customValues = customValues;
        }

        public void setEmail(final String email) {
            this.email = email;
        }

        public void setName(final String name) {
            this.name = name;
        }

        public void setUsername(final String username) {
            this.username = username;
        }
    }

    private static final String        CUSTOM_VALUE_PREFIX = "customValue.";

    private ElementService             elementService;
    private MemberCustomFieldService   memberCustomFieldService;
    private ContactService             contactService;
    private MemberService              memberService;
    private MemberFieldsRestController memberFieldsRestController;
    private CustomFieldHelper          customFieldHelper;

    //TODO -- INJECTS THESE FROM XML
    private ElementServiceLocal        elementServiceLocal  ;
    private FetchServiceLocal          fetchService;

    /**
     * Gets member and custom fields by member id
     */
    @RequestMapping(value = "members/memberData/{id}")
    @ResponseBody
    public MemberDataVO getMemberData(@PathVariable final Long id) {

        Member member = memberService.loadByIdOrPrincipal(id, null, null);

        MemberVO memberVO = memberService.getMemberVO(member, true, false);

        boolean canAddMemberAsContact = false;

        // The member can be added to the logged user's contact list if it's not already added.
        Contact contact = contactService.loadIfExists(LoggedUser.member(), member);
        if (contact == null) {
            canAddMemberAsContact = true;
        }

        return new MemberDataVO(memberVO, canAddMemberAsContact);
    }

    /**
     * Returns authenticated user's profile
     */
    @RequestMapping(value = "members/me", method = RequestMethod.GET)
    @ResponseBody
    public MyProfileVO getMyProfile() {
        Member member = LoggedUser.element();
        return memberService.getMyProfileVO(member);
    }

    /**
     * Loads a member by id
     */
    @RequestMapping(value = "members/{id}", method = RequestMethod.GET)
    @ResponseBody
    public MemberVO loadById(@PathVariable final Long id) {
        Member member;
        try {
            member = elementService.load(id);
        } catch (Exception e) {
            throw new EntityNotFoundException(Member.class);
        }
        return memberService.getMemberVO(member, true, true);
    }

    /**
     * Loads a member by principal
     */
    @RequestMapping(value = "members/principal/{principal}", method = RequestMethod.GET)
    @ResponseBody
    public MemberVO loadByPrincipal(@PathVariable final String principal) {
        Member member;
        try {
            PrincipalType principalType = WebServiceContext.getChannel().getDefaultPrincipalType();
            member = elementService.loadByPrincipal(principalType, principal);
        } catch (Exception e) {
            throw new EntityNotFoundException(Member.class);
        }
        return memberService.getMemberVO(member, true, true);
    }

    /**
     * Searches for members
     */
    @RequestMapping(value = "members", method = RequestMethod.GET)
    @ResponseBody
    public MemberResultPage search(final FullTextMemberSearchParameters params, final HttpServletRequest request) {
        params.setFields(memberFieldsRestController.requestParametersToFieldValues(request, CUSTOM_VALUE_PREFIX));
        return memberService.getMemberResultPage(params);
    }

    public void setContactService(final ContactService contactService) {
        this.contactService = contactService;
    }

    public void setCustomFieldHelper(final CustomFieldHelper customFieldHelper) {
        this.customFieldHelper = customFieldHelper;
    }

    public void setElementService(final ElementService elementService) {
        this.elementService = elementService;
    }

    public void setMemberCustomFieldService(final MemberCustomFieldService memberCustomFieldService) {
        this.memberCustomFieldService = memberCustomFieldService;
    }

    public void setMemberFieldsRestController(final MemberFieldsRestController memberFieldsRestController) {
        this.memberFieldsRestController = memberFieldsRestController;
    }

    public void setMemberService(final MemberService memberService) {
        this.memberService = memberService;
    }


    public void setElementServiceLocal(final ElementServiceLocal elementServiceLocal) {
        this.elementServiceLocal = elementServiceLocal;
    }

    public void setFetchServiceLocal(final FetchServiceLocal fetchService) {
        this.fetchService = fetchService;
    }

    /**
     * Updates the authenticated user's profile
     */
    @RequestMapping(value = "members/me", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.OK)
    public void updateMyProfile(@RequestBody final UpdateProfileParameters params) {
        if (params == null) {
            throw new ValidationException();
        }

        Member member = (Member) LoggedUser.element().clone();
        member.setUser((User) LoggedUser.element().getUser().clone());
        if (StringUtils.isNotEmpty(params.getName())) {
            member.setName(params.getName());
        }
        if (StringUtils.isNotEmpty(params.getUsername())) {
            member.getUser().setUsername(params.getUsername());
        }
        if (StringUtils.isNotEmpty(params.getEmail())) {
            member.setEmail(params.getEmail());
        }
        // Merge the custom fields
        List<RegistrationFieldValueVO> fieldValueVOs = params.getCustomValues();
        List<MemberCustomField> allowedFields = customFieldHelper.onlyForGroup(memberCustomFieldService.list(), member.getMemberGroup());
        Collection<MemberCustomFieldValue> newFieldValues = customFieldHelper.mergeFieldValues(member, fieldValueVOs, allowedFields);
        member.setCustomValues(newFieldValues);
        elementService.changeProfile(member);
    }



    /**
     * Create a new member
     */
    @RequestMapping(value = "members/create", method = RequestMethod.POST)
    @ResponseBody
    public MemberVO create(@RequestBody final MembersRestPayload params) {
        return saveMember(params);
    }


    private MemberVO saveMember(MembersRestPayload params) {

        Boolean sendActivationMail = Boolean.FALSE ;
        // Fill the member
        Member member = new Member();
        final MemberUser user = new MemberUser();
        member.setUser(user);
        member.setCreationDate(params.getCreationDate());

        Member loggedMember = (Member) LoggedUser.element().clone();
        //final MemberGroup group = fetchService.fetch((MemberGroup)null);
        final MemberGroup group = loggedMember.getMemberGroup();
        member.setGroup(group);

        //if (StringUtils.isNotBlank(params.getName())) {
            //member.setGroup(params.getGroup());
          //  member.setGroup(group);
        //}

        if (StringUtils.isNotBlank(params.getName())) {
            member.setName(params.getName());
        } else {}

        if (StringUtils.isNotBlank(params.getSalt())) {
            user.setSalt(params.getSalt());
        } else {}

        if (StringUtils.isNotBlank(params.getUsername())) {
            user.setUsername(params.getUsername());
        } else {}

        if (StringUtils.isNotBlank(params.getPassword())) {
            user.setPassword(params.getPassword());
        } else {}

        if (StringUtils.isNotBlank(params.getEmail())) {
            member.setEmail(params.getEmail());
        } else {}

        Date now = new Date();
        Calendar cal = Calendar.getInstance();
        cal.setTime(now);

        member.setActivationDate(cal);
        member = elementServiceLocal.insertMember(member, !sendActivationMail, false);
        MemberVO memberVO = memberService.getMemberVO(member, true, false);
        return memberVO ;

       }

}
