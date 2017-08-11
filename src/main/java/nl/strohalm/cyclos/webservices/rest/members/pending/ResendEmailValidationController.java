package nl.strohalm.cyclos.webservices.rest.members.pending;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import nl.strohalm.cyclos.entities.members.PendingMember;
import nl.strohalm.cyclos.services.elements.ElementService;
import nl.strohalm.cyclos.utils.validation.ValidationException;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;
@Controller
public class ResendEmailValidationController extends BaseRestController{
	private ElementService elementService;
	public final ElementService getElementService() {
		return elementService;
	}
	public final void setElementService(ElementService elementService) {
		this.elementService = elementService;
	}
	public static class ResendEmailValidationRequestDTO{
		private long              pendingMemberId;

	    public long getPendingMemberId() {
	        return pendingMemberId;
	    }

	    public void setPendingMemberId(final long pendingMemberId) {
	        this.pendingMemberId = pendingMemberId;
	    }
	}
	public static class ResendEmailValidationResponseDTO{
		String message;

		public final String getMessage() {
			return message;
		}

		public final void setMessage(String message) {
			this.message = message;
		}
		
	}
	@RequestMapping(value ="member/resendEmailValidation",method = RequestMethod.POST)
	@ResponseBody
	
    protected ResendEmailValidationResponseDTO executeAction(@RequestBody ResendEmailValidationRequestDTO form) throws Exception {
        ResendEmailValidationResponseDTO response = null;
        try{
        final long id = form.getPendingMemberId();
        PendingMember pendingMember;
        try {
            pendingMember = elementService.loadPendingMember(id);
        } catch (final Exception e) {
            throw new ValidationException();
        }
        elementService.resendEmail(pendingMember);
        response = new ResendEmailValidationResponseDTO();
        response.setMessage("pendingMember.emailResent");}
        catch(Exception e){
            e.printStackTrace();
        }
        return response;
    }


}
