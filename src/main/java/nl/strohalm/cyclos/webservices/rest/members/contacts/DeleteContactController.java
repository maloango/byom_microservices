package nl.strohalm.cyclos.webservices.rest.members.contacts;


import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import nl.strohalm.cyclos.annotations.Inject;
import nl.strohalm.cyclos.services.elements.ContactService;
import nl.strohalm.cyclos.utils.validation.ValidationException;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;
import nl.strohalm.cyclos.webservices.rest.GenericResponse;
import org.springframework.web.bind.annotation.PathVariable;
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
    
    public static class DeleteContactResponse extends GenericResponse{
    	private long id;

        public long getId() {
            return id;
        }

        public void setId(long id) {
            this.id = id;
        }
        
   
   }

    @RequestMapping(value = "member/deleteContact/{id}", method = RequestMethod.GET)
    @ResponseBody
    protected DeleteContactResponse deleteContact(@PathVariable("id") long id) throws Exception {
       DeleteContactResponse response = new DeleteContactResponse();
        if (id<= 0) {
            throw new ValidationException();
        }
        contactService.remove(id);
        response.setStatus(0);
        response.setMessage("!! Contact has deleted....");
        return response;
    }

}
