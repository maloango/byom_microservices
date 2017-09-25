package nl.strohalm.cyclos.webservices.rest.members.contacts;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.springframework.stereotype.Controller;
import nl.strohalm.cyclos.annotations.Inject;
import nl.strohalm.cyclos.entities.members.Contact;
import nl.strohalm.cyclos.entities.members.Member;
import nl.strohalm.cyclos.services.elements.ContactService;
import nl.strohalm.cyclos.utils.access.LoggedUser;
import nl.strohalm.cyclos.webservices.model.ContactVO;
import nl.strohalm.cyclos.webservices.model.MemberVO;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;
import nl.strohalm.cyclos.webservices.rest.GenericResponse;
import nl.strohalm.cyclos.webservices.rest.members.LoadMemberController;
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

    public static class ListContactsResponse extends GenericResponse {

        private List<ContactVO> contactVO;

        public List<ContactVO> getContactVO() {
            return contactVO;

        }

        public void setContactVO(List<ContactVO> contactVO) {
            this.contactVO = contactVO;
        }

    }

    @RequestMapping(value = "member/listContact", method = RequestMethod.GET)
    @ResponseBody
    public ListContactsResponse listContact() throws Exception {
        ListContactsResponse response = new ListContactsResponse();

        final Member member = (Member) LoggedUser.accountOwner();
        List<Contact> contacts = contactService.list(member);
        List<ContactVO> contactVO = contactService.getContactVOs(contacts, true, true);
        response.setContactVO(contactVO);
        response.setStatus(0);
        response.setMessage("!!Contact type list");
        return response;
    }
}
