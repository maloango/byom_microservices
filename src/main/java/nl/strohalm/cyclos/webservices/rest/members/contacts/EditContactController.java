package nl.strohalm.cyclos.webservices.rest.members.contacts;

import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import nl.strohalm.cyclos.annotations.Inject;
import nl.strohalm.cyclos.controls.ActionContext;
import nl.strohalm.cyclos.controls.members.contacts.ContactForm;
import nl.strohalm.cyclos.entities.members.Contact;
import nl.strohalm.cyclos.entities.members.Element;
import nl.strohalm.cyclos.entities.members.Member;
import nl.strohalm.cyclos.services.elements.ContactService;
import nl.strohalm.cyclos.utils.RelationshipHelper;
import nl.strohalm.cyclos.utils.binding.BeanBinder;
import nl.strohalm.cyclos.utils.binding.DataBinder;
import nl.strohalm.cyclos.utils.binding.PropertyBinder;
import nl.strohalm.cyclos.utils.conversion.IdConverter;
import nl.strohalm.cyclos.utils.conversion.ReferenceConverter;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;
@Controller
public class EditContactController extends BaseRestController{
	 private ContactService      contactService;
	    private DataBinder<Contact> dataBinder;

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
	    public static class EditContactRequestDTO{
	    	private long              id;
	    	private Map<String,Object> values;
	    	public Map<String,Object>getvalues;

	        public void ContactForm() {
	        }

	        public Map<String, Object> getContact() {
	            return values;
	        }

	        public Object getContact(final String key) {
	            return values.get(key);
	        }

	        public long getId() {
	            return id;
	        }

	        public void setContact(final Map<String, Object> map) {
	            values = map;
	        }

	        public void setContact(final String key, final Object value) {
	            values.put(key, value);
	        }

	        public void setId(final long id) {
	            this.id = id;
	        }
	    }
	    
	    public static class EditContactResponseDTO{
	    	String message;

			public final String getMessage() {
				return message;
			}

			public final void setMessage(String message) {
				this.message = message;
			}
	    	
	    }

	    @RequestMapping(value = "/member/editContact", method = RequestMethod.PUT)
	    @ResponseBody
	    protected EditContactResponseDTO formAction(@RequestBody EditContactRequestDTO form) throws Exception {
	        final Contact contact = dataBinder.readFromString(form.getContact());
	        contactService.save(contact);
	        EditContactResponseDTO response = new EditContactResponseDTO();
	        response.setMessage("contact.modified");
	        return response;
	    }

	   // @Override
	    protected void prepareForm(final ActionContext context) throws Exception {
	        final ContactForm form = context.getForm();
	        final Contact contact = contactService.load(form.getId(), RelationshipHelper.nested(Contact.Relationships.CONTACT, Element.Relationships.USER));
	        getDataBinder().writeAsString(form.getContact(), contact);
	        context.getRequest().setAttribute("contact", contact.getContact());
	    }

	   // @Override
	    protected void validateForm(final ActionContext context) {
	        final ContactForm form = context.getForm();
	        final Contact contact = dataBinder.readFromString(form.getContact());
	        contactService.validate(contact);
	    }

}
