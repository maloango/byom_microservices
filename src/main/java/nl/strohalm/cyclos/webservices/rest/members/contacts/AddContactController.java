package nl.strohalm.cyclos.webservices.rest.members.contacts;

import org.apache.struts.action.ActionForward;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import nl.strohalm.cyclos.annotations.Inject;
import nl.strohalm.cyclos.controls.ActionContext;
import nl.strohalm.cyclos.controls.members.contacts.AddContactForm;
import nl.strohalm.cyclos.controls.restapi.BaseRestController;
import nl.strohalm.cyclos.entities.members.Contact;
import nl.strohalm.cyclos.entities.members.Member;
import nl.strohalm.cyclos.services.elements.ContactService;
import nl.strohalm.cyclos.services.elements.exceptions.ContactAlreadyExistException;
import nl.strohalm.cyclos.utils.ActionHelper;
import nl.strohalm.cyclos.utils.EntityHelper;
import nl.strohalm.cyclos.utils.RequestHelper;
import nl.strohalm.cyclos.utils.ResponseHelper;
import nl.strohalm.cyclos.utils.validation.RequiredError;
import nl.strohalm.cyclos.utils.validation.ValidationException;
@Controller
public class AddContactController extends BaseRestController{
	
	private ContactService contactService;
    private ResponseHelper responseHelper;
 

    public ContactService getContactService() {
        return contactService;
    }

    @Inject
    public void setContactService(final ContactService contactService) {
        this.contactService = contactService;
    }

    @Inject
    public void setResponseHelper(final ResponseHelper responseHelper) {
        this.responseHelper = responseHelper;
    }
    
    public static class AddContactRequestDTO{
    	private long              memberId;
        private boolean           direct;

        public long getMemberId() {
            return memberId;
        }

        public boolean isDirect() {
            return direct;
        }

        public void setDirect(final boolean direct) {
            this.direct = direct;
        }

        public void setMemberId(final long memberId) {
            this.memberId = memberId;
        }
    }
    
    public static class AddContactResponseDTO{
    	private boolean Member;
    	String message;

		public final String getMessage() {
			return message;
		}

		public final void setMessage(String message) {
			this.message = message;
		}

		public boolean isMember() {
			return Member;
		}

		public void setMember(boolean member) {
			Member = member;
		}
    }

    @RequestMapping(value = "", method = RequestMethod.PUT)
    @ResponseBody
   
    protected AddContactResponseDTO executeAction(@RequestBody AddContactRequestDTO form) throws Exception {
        final long memberId = form.getMemberId();

        if (RequestHelper.isValidation(form.getRequest())) {
            try {
                if (memberId <= 0L) {
                    throw new ValidationException("contact", "member.member", new RequiredError());
                }
                responseHelper.writeValidationSuccess(form.getResponse());
            } catch (final ValidationException e) {
                responseHelper.writeValidationErrors(form.getResponse(), e);
            }
            return null;

        }
        if (memberId <= 0L) {
            throw new ValidationException();
        }

        final Contact contact = new Contact();
        final Member member = (Member) form.getAccountOwner();
        contact.setOwner(member);
        contact.setContact(EntityHelper.reference(Member.class, memberId));
        try {
            contactService.save(contact);
            AddContactResponseDTO response = new AddContactResponseDTO();
            String message = null;
            if (condition) {
				message ="contact.inserted";
			}
            else{
            	message = "contact.error.alreadyExists";
            }
            /*response.setMessage("contact.inserted");
        } catch (final ContactAlreadyExistException e) {
        	response.sendMessage("contact.error.alreadyExists");*/
        }

        if (form.isDirect()) {
            return context.findForward("backToList");
        } else {
           // return ActionHelper.redirectWithParam(context.getRequest(), context.findForward("backToProfile"), "memberId", memberId);
        }
    }


}
