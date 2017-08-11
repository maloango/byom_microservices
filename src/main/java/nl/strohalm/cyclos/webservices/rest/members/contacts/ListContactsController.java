package nl.strohalm.cyclos.webservices.rest.members.contacts;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import antlr.collections.List;
import nl.strohalm.cyclos.annotations.Inject;
import nl.strohalm.cyclos.entities.members.Member;
import nl.strohalm.cyclos.services.elements.ContactService;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;
@Controller
public class ListContactsController extends BaseRestController{
	
	 private ContactService contactService;

	    public ContactService getContactService() {
	        return contactService;
	    }

	    @Inject
	    public void setContactService(final ContactService memberService) {
	        contactService = memberService;
	    }
	    
	    public static class ListContactsRequestDTo{
	    	private boolean member;

			public boolean isMember() {
				return member;
			}

			public void setMember(boolean member) {
				this.member = member;
			}
	    }
	    
	    public static class ListContactsResponse{
	    	List contacts;
	    	public final List getContacts() {
				return contacts;
			}
			public final void setContacts(List contacts) {
				this.contacts = contacts;
			}
			public ListContactsResponse(){
	    		super();
	    		this.contacts=contacts;
	    	}
	    }

	    @RequestMapping(value = "member/contacts", method = RequestMethod.GET)
	    @ResponseBody
	    protected ListContactsResponse executeAction(@RequestBody ListContactsRequestDTo form) throws Exception {
	        //final Member member = (Member) (member) .getAccountOwner();
	        ListContactsResponse resposne = new ListContactsResponse() ;
	       // ListGroupsResponseDTO response = new ListGroupsResponseDTO((List<Group>) groupQuery);
	        //resposne.getRequest().setAttribute("contacts", contactService.list(member));
	       // return context.getInputForward(); 
			return resposne;
	    }

}
