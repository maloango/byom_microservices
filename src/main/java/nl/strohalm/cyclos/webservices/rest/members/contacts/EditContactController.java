package nl.strohalm.cyclos.webservices.rest.members.contacts;

import java.security.acl.Owner;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import nl.strohalm.cyclos.annotations.Inject;
//import nl.strohalm.cyclos.controls.members.contacts.ContactForm;
import nl.strohalm.cyclos.entities.members.Contact;
import nl.strohalm.cyclos.services.elements.ContactService;
import nl.strohalm.cyclos.utils.binding.DataBinder;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;
import nl.strohalm.cyclos.webservices.rest.GenericResponse;
@Controller
public class EditContactController extends BaseRestController{
	 private ContactService      contactService;
	    private DataBinder<Contact> dataBinder;

	    public void EditContactAction() {
	    }

	    public ContactService getContactService() {
	        return contactService;
	    }
//
//	    public DataBinder<Contact> getDataBinder() {
//	        if (dataBinder == null) {
//	            final BeanBinder<Contact> binder = BeanBinder.instance(Contact.class);
//	            binder.registerBinder("id", PropertyBinder.instance(Long.class, "id", IdConverter.instance()));
//	            binder.registerBinder("contact", PropertyBinder.instance(Member.class, "contact", ReferenceConverter.instance(Member.class)));
//	            binder.registerBinder("owner", PropertyBinder.instance(Member.class, "owner", ReferenceConverter.instance(Member.class)));
//	            binder.registerBinder("notes", PropertyBinder.instance(String.class, "notes"));
//	            dataBinder = binder;
//	        }
//	        return dataBinder;
//	    }

	    @Inject
	    public void setContactService(final ContactService memberService) {
	        contactService = memberService;
	    }
	    public static class EditContactResponse extends GenericResponse {
	    	private long   id;
                private Contact contact;
                private String notes;
                private Owner owner;

        public Contact getContact() {
            return contact;
        }

        public void setContact(Contact contact) {
            this.contact = contact;
        }

        public String getNotes() {
            return notes;
        }

        public void setNotes(String notes) {
            this.notes = notes;
        }

        public Owner getOwner() {
            return owner;
        }

        public void setOwner(Owner owner) {
            this.owner = owner;
        }
                

        public long getId() {
            return id;
        }

        public void setId(long id) {
            this.id = id;
        }
                
	    	
            }
	    
	  

	    @RequestMapping(value = "member/editContact", method = RequestMethod.POST)
	    @ResponseBody
	    public  EditContactResponse editContact(@RequestBody EditContactResponse request) throws Exception {
              EditContactResponse response = new EditContactResponse();
                try{
	        final Contact contact = dataBinder.readFromString(request.getContact());
	        contactService.save(contact);
	        
	        response.setMessage("contact.modified");}
               
                catch(Exception e){
                    e.printStackTrace();
                }
                response.setStatus(0);
                response.setMessage("contact modified!!!");
	        return response;
	    }

	   // @Override
//	    protected void prepareForm(final ActionContext context) throws Exception {
//	        final ContactForm form = context.getForm();
//	        final Contact contact = contactService.load(form.getId(), RelationshipHelper.nested(Contact.Relationships.CONTACT, Element.Relationships.USER));
//	        getDataBinder().writeAsString(form.getContact(), contact);
//	        context.getRequest().setAttribute("contact", contact.getContact());
//	    }

//	   // @Override
//	    protected void validateForm(final ActionContext context) {
//	        final ContactForm form = context.getForm();
//	        final Contact contact = dataBinder.readFromString(form.getContact());
//	        contactService.validate(contact);
//	    }

}
