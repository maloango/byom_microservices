package nl.strohalm.cyclos.webservices.rest.members.contacts;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import nl.strohalm.cyclos.annotations.Inject;
import nl.strohalm.cyclos.entities.members.Contact;
import nl.strohalm.cyclos.entities.members.Element;
import nl.strohalm.cyclos.entities.members.Member;
import nl.strohalm.cyclos.services.elements.ContactService;
import nl.strohalm.cyclos.services.elements.ElementService;
import nl.strohalm.cyclos.utils.RelationshipHelper;
import nl.strohalm.cyclos.utils.binding.BeanBinder;
import nl.strohalm.cyclos.utils.binding.DataBinder;
import nl.strohalm.cyclos.utils.binding.PropertyBinder;
import nl.strohalm.cyclos.utils.conversion.IdConverter;
import nl.strohalm.cyclos.utils.conversion.ReferenceConverter;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;
import nl.strohalm.cyclos.webservices.rest.GenericResponse;

@Controller
public class EditContactController extends BaseRestController {

    private ContactService contactService;
    private DataBinder<Contact> dataBinder;
    private RelationshipHelper relationshipHelper;
    private ElementService elementService;

    @Inject
    public void setElementService(ElementService elementService) {
        this.elementService = elementService;
    }

    @Inject
    public void setRelationshipHelper(RelationshipHelper relationshipHelper) {
        this.relationshipHelper = relationshipHelper;
    }

    @Inject
    public void setDataBinder(DataBinder<Contact> dataBinder) {
        this.dataBinder = dataBinder;
    }

    public void EditContactAction() {
    }

    public ContactService getContactService() {
        return contactService;
    }

    public DataBinder<Contact> getDataBinder() {
        if (dataBinder == null) {
            final BeanBinder<Contact> binder = BeanBinder.instance(Contact.class);
            binder.registerBinder("id", PropertyBinder.instance(Long.class, "id", IdConverter.instance()));
            binder.registerBinder("contact", PropertyBinder.instance(Member.class, "contact", ReferenceConverter.instance(Member.class)));
            binder.registerBinder("owner", PropertyBinder.instance(Member.class, "owner", ReferenceConverter.instance(Member.class)));
            binder.registerBinder("notes", PropertyBinder.instance(String.class, "notes"));
            dataBinder = binder;
        }
        return dataBinder;
    }

    @Inject
    public void setContactService(final ContactService memberService) {
        contactService = memberService;
    }

    public static class EditContactResponse extends GenericResponse {

        private Long id;
        private Long contact;
        private String notes;
        private Long owner;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public Long getContact() {
            return contact;
        }

        public void setContact(Long contact) {
            this.contact = contact;
        }

        public String getNotes() {
            return notes;
        }

        public void setNotes(String notes) {
            this.notes = notes;
        }

        public Long getOwner() {
            return owner;
        }

        public void setOwner(Long owner) {
            this.owner = owner;
        }

    }

    @RequestMapping(value = "member/editContact", method = RequestMethod.POST)
    @ResponseBody
    public EditContactResponse editContact(@RequestBody EditContactResponse request) throws Exception {
        EditContactResponse response = new EditContactResponse();
        try {
            Contact contact = new Contact();
            System.out.println(".... contact" +  elementService.load(request.getId(), Element.Relationships.USER));
            contact.setId(request.getId());
            contact.setNotes(request.getNotes());
            contact.setOwner((Member) elementService.load(request.getId(), Element.Relationships.USER));
            contact.setContact((Member) elementService.load(request.getId(), Element.Relationships.USER));

            contactService.save(contact);
        } catch (Exception exe) {
            exe.printStackTrace();
        }
        response.setStatus(0);

        response.setMessage("!!contact has modified");
        return response;
    }

//    @RequestMapping(value = "member/editContact/{id}", method = RequestMethod.GET)
//    @ResponseBody
//    public EditContactResponse prepareForm(@PathVariable("id") long id) throws Exception {
//        EditContactResponse response = new EditContactResponse();
//        final Contact contact = contactService.load(id, RelationshipHelper.nested(Contact.Relationships.CONTACT, Element.Relationships.USER));
//        getDataBinder().writeAsString(response.getContact(), contact);
//        response.setContact(contact);
//        response.setStatus(0);
//        return response;
//    }
}
