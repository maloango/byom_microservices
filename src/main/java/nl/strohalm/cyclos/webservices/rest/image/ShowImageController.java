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

		public ShowImageResponseDTO getInputForward() {
			// TODO Auto-generated method stub
			return null;
		}
		
	}
	
	public static class ShowImageResponseDTO{
		String message;

		public final String getMessage() {
			return message;
		}

		public final void setMessage(String message) {
			this.message = message;
		}
		
		
	}
	
	 @RequestMapping(value = "member/showImage" ,method = RequestMethod.GET)
	    protected ShowImageResponseDTO executeAction(@RequestBody ShowImageRequestDTO form) throws Exception {
                    ShowImageResponseDTO response = null;
                    try{
		 response = new ShowImageResponseDTO();
		 response.setMessage(null);}
                    catch(Exception e){
                        e.printStackTrace();
                    }
		 return response;
	    }

	    //@Override
	    protected boolean storePath(final ActionMapping actionMapping, final HttpServletRequest request) {
	        return false;
	    }

}
