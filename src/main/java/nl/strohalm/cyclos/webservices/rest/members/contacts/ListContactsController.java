package nl.strohalm.cyclos.webservices.rest.members.contacts;

import java.util.Collection;
import org.springframework.stereotype.Controller;
import nl.strohalm.cyclos.annotations.Inject;
import nl.strohalm.cyclos.entities.members.Contact;
import nl.strohalm.cyclos.entities.members.Member;
import nl.strohalm.cyclos.services.elements.ContactService;
import nl.strohalm.cyclos.utils.access.LoggedUser;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;
import nl.strohalm.cyclos.webservices.rest.GenericResponse;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class ListContactsController extends BaseRestController {

    private ContactService contactService;

    public ContactService getContactService() {
        return contactService;
    }

    @Inject
    public void setContactService(final ContactService memberService) {
        contactService = memberService;
    }
    
    public static class ListContactsResponse extends GenericResponse{
        private Member member;
        private Collection<Contact> contact;

        public Member getMember() {
            return member;
        }

        public void setMember(Member member) {
            this.member = member;
        }

        public Collection<Contact> getContact() {
            return contact;
        }

        public void setContact(Collection<Contact> contact) {
            this.contact = contact;
        }
    }
    
    @RequestMapping(value = "member/listContact", method = RequestMethod.GET)
    @ResponseBody
    public ListContactsResponse executeAction() throws Exception {
        ListContactsResponse response = new ListContactsResponse();
        final Member member = (Member) LoggedUser.accountOwner();
        Collection<Contact> contact= contactService.list(member);
        System.out.println("contact list: "+contact);
        response.setContact(contact);
        response.setStatus(0);
        return response;
    }
}

