package nl.strohalm.cyclos.webservices.rest.members.references;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import nl.strohalm.cyclos.annotations.Inject;
import nl.strohalm.cyclos.entities.members.Reference;
import nl.strohalm.cyclos.services.elements.ReferenceService;
import nl.strohalm.cyclos.utils.validation.ValidationException;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;
@Controller
public class RemoveReferenceController extends BaseRestController{
	private ReferenceService referenceService;

    @Inject
    public void setReferenceService(final ReferenceService referenceService) {
        this.referenceService = referenceService;
    }
    public static class RemoveReferenceRequestDTO{
    	private long              memberId;
        private long              referenceId;

        public long getMemberId() {
            return memberId;
        }

        public long getReferenceId() {
            return referenceId;
        }

        public void setMemberId(final long memberId) {
            this.memberId = memberId;
        }

        public void setReferenceId(final long referenceId) {
            this.referenceId = referenceId;
        }
    }
    public static class RemoveReferenceResponseDTO{
    	String message;

		public final String getMessage() {
			return message;
		}

		public final void setMessage(String message) {
			this.message = message;
		}
    	
    }

    @RequestMapping (value = "/member/removeReference",method = RequestMethod.DELETE)
    @ResponseBody
    protected RemoveReferenceResponseDTO executeAction(@RequestBody RemoveReferenceRequestDTO form) throws Exception {
    	RemoveReferenceResponseDTO response = new RemoveReferenceResponseDTO();
        if (form.getReferenceId() <= 0 || form.getMemberId() <= 0L) {
            throw new ValidationException();
        }
        final Reference reference = referenceService.load(form.getReferenceId());
        referenceService.remove(reference.getId());
        response.setMessage("reference.removed");
        return response;
    }

}
