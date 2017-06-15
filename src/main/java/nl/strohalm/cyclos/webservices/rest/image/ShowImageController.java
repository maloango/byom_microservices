package nl.strohalm.cyclos.webservices.rest.image;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts.action.ActionMapping;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import nl.strohalm.cyclos.webservices.rest.BaseRestController;
@Controller
public class ShowImageController extends BaseRestController {
	
	// implement will be be do later if required
	
	public static class ShowImageRequestDTO{

		public ShowImageResponse getInputForward() {
			// TODO Auto-generated method stub
			return null;
		}
		
	}
	
	public static class ShowImageResponse{
		String message;

		public final String getMessage() {
			return message;
		}

		public final void setMessage(String message) {
			this.message = message;
		}
		
		
	}
	
	 @RequestMapping(value = "member/showImage" ,method = RequestMethod.GET)
	    protected ShowImageResponse executeAction(@RequestBody ShowImageRequestDTO form) throws Exception {
	      
		 ShowImageResponse response = new ShowImageResponse();
		 response.setMessage(null);
		 return response;
	    }

	    //@Override
	    protected boolean storePath(final ActionMapping actionMapping, final HttpServletRequest request) {
	        return false;
	    }

}
