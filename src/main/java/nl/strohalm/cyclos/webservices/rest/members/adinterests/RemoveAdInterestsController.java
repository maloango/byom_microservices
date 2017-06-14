package nl.strohalm.cyclos.webservices.rest.members.adinterests;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import nl.strohalm.cyclos.annotations.Inject;
import nl.strohalm.cyclos.services.elements.AdInterestService;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;
@Controller
public class RemoveAdInterestsController extends BaseRestController {
	
	private AdInterestService adInterestService;

    public AdInterestService getAdInterestService() {
        return adInterestService;
    }

    @Inject
    public void setAdInterestService(final AdInterestService adInterestService) {
        this.adInterestService = adInterestService;
    }
    
    public static class RemoveAdInterestsRequestDTO{
    	 private Long[]            adInterestsIds;

    	    public Long[] getAdInterestsIds() {
    	        return adInterestsIds;
    	    }

    	    public void setAdInterestsIds(final Long[] adInterestsIds) {
    	        this.adInterestsIds = adInterestsIds;
    	    }

    }
    
    public static class RemoveAdInterestsResponseDTO{
    	String message ;

		public final String getMessage() {
			return message;
		}

		public final void setMessage(String message) {
			this.message = message;
		}
    	
    }

    @RequestMapping(value = "/member/removeAdInterests", method = RequestMethod.DELETE)
	@ResponseBody
    protected RemoveAdInterestsResponseDTO executeAction(@RequestBody RemoveAdInterestsRequestDTO form) throws Exception {
        //final RemoveAdInterestsForm form = context.getForm();
        final Long[] ids = form.getAdInterestsIds();
        adInterestService.remove(ids);
        RemoveAdInterestsResponseDTO response = new RemoveAdInterestsResponseDTO();
        response.setMessage("adInterest.removed");
        return response;

    }

}
