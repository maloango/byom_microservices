package nl.strohalm.cyclos.webservices.rest.members.pending;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import nl.strohalm.cyclos.services.elements.ElementService;
import nl.strohalm.cyclos.utils.validation.ValidationException;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;
@Controller
public class RemovePendingMemberController extends BaseRestController{
	private ElementService elementService;
	

	public final ElementService getElementService() {
		return elementService;
	}


	public final void setElementService(ElementService elementService) {
		this.elementService = elementService;
	}
	
	public static class RemovePendingMemberRequestDTO{
		private long              pendingMemberId;

	    public long getPendingMemberId() {
	        return pendingMemberId;
	    }

	    public void setPendingMemberId(final long pendingMemberId) {
	        this.pendingMemberId = pendingMemberId;
	    }
	}
	public static class RemovePendingMemberResponseDTO{
		String message;

		public final String getMessage() {
			return message;
		}

		public final void setMessage(String message) {
			this.message = message;
		}
		
	}

	@RequestMapping(value = "member/removePendingMember",method= RequestMethod.DELETE)
	@ResponseBody
    protected RemovePendingMemberResponseDTO executeAction(@RequestBody RemovePendingMemberRequestDTO form) throws Exception {
        RemovePendingMemberResponseDTO response = null;
        try{
        final long id = form.getPendingMemberId();
        if (id <= 0L) {
            throw new ValidationException();
        }
        elementService.removePendingMembers(id);
        response = new RemovePendingMemberResponseDTO();
        response.setMessage("pendingMember.removed");}
        catch(Exception e){
            e.printStackTrace();
        }
        return response;
    }

}
