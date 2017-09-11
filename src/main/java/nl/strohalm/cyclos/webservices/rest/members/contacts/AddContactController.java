package nl.strohalm.cyclos.webservices.rest.members.contacts;

import nl.strohalm.cyclos.annotations.Inject;
import nl.strohalm.cyclos.entities.members.Contact;
import nl.strohalm.cyclos.entities.members.Member;
import nl.strohalm.cyclos.services.elements.ContactService;
import nl.strohalm.cyclos.services.elements.exceptions.ContactAlreadyExistException;
import nl.strohalm.cyclos.utils.EntityHelper;
import nl.strohalm.cyclos.utils.ResponseHelper;
import nl.strohalm.cyclos.utils.access.LoggedUser;
import org.springframework.stereotype.Controller;

import nl.strohalm.cyclos.webservices.rest.BaseRestController;
import nl.strohalm.cyclos.webservices.rest.GenericResponse;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class AddContactController extends BaseRestController {

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

    public static class AddContactResponse extends GenericResponse {

        private long memberId;
        private boolean direct;

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

    @RequestMapping(value = "member/addContact", method = RequestMethod.POST)
    @ResponseBody
    protected AddContactResponse addContact(@RequestBody AddContactResponse request) throws Exception {
        // final AddContactForm form = context.getForm();
        AddContactResponse response = new AddContactResponse();
        final long memberId = request.getMemberId();

//        if (RequestHelper.isValidation(context.getRequest())) {
//            try {
//                if (memberId <= 0L) {
//                    throw new ValidationException("contact", "member.member", new RequiredError());
//                }
//                responseHelper.writeValidationSuccess(context.getResponse());
//            } catch (final ValidationException e) {
//                responseHelper.writeValidationErrors(context.getResponse(), e);
//            }
//            return null;
//
//        }
//        if (memberId <= 0L) {
//            throw new ValidationException();
//        }

        final Contact contact = new Contact();
        final Member member = (Member) LoggedUser.accountOwner();
        contact.setOwner(member);
        contact.setContact(EntityHelper.reference(Member.class, memberId));
        try {
            contactService.save(contact);
            response.setMessage("contact.inserted");
        } catch (final ContactAlreadyExistException e) {
            response.setMessage("contact.error.alreadyExists");
        }

//        if (context.isDirect()) {
//       if()
//            return context.findForward("backToList");
//        } else {
//            return ActionHelper.redirectWithParam(context.getRequest(), context.findForward("backToProfile"), "memberId", memberId);
//        }
        response.setStatus(0);
        response.setMessage("contact.inserted");
        return response;
    }

}
