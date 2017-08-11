package nl.strohalm.cyclos.webservices.rest.members.contacts;

import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import nl.strohalm.cyclos.annotations.Inject;
import nl.strohalm.cyclos.services.elements.ContactService;
import nl.strohalm.cyclos.utils.validation.ValidationException;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;
@Controller
public class DeleteContactController extends BaseRestController{
	private ContactService contactService;

    public ContactService getContactService() {
        return contactService;
    }

    @Inject
    public void setContactService(final ContactService contactService) {
        this.contactService = contactService;
    }
    
    public static class DeleteContactRequestDTO{
    	private long              id;
    	private Map<String,Object> values;
    	public Map<String,Object> getValues;

        public void  ContactForm() {
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
    
   public static class DeleteContactResponseDTO{
	   String message;

	public final String getMessage() {
		return message;
	}

	public final void setMessage(String message) {
		this.message = message;
	}
	   
	   
   }

    @RequestMapping(value = "member/deleteContact", method = RequestMethod.DELETE)
    @ResponseBody
    protected DeleteContactResponseDTO executeAction(@RequestBody DeleteContactRequestDTO form) throws Exception {
        DeleteContactResponseDTO response = null;
        try{
        if (form.getId() <= 0) {
            throw new ValidationException();
        }
        contactService.remove(form.getId());
        
        response.setMessage("contact.removed");
        response = new DeleteContactResponseDTO();}
        catch(Exception e){
            e.printStackTrace();
        }
        return response;
    }

}
