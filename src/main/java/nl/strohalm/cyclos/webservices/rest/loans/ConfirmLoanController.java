package nl.strohalm.cyclos.webservices.rest.loans;

import nl.strohalm.cyclos.webservices.rest.BaseRestController;
import nl.strohalm.cyclos.webservices.rest.GenericResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class ConfirmLoanController extends BaseRestController {
	
	@RequestMapping(value = "admin/confirmLoan", method = RequestMethod.POST)
	@ResponseBody
	protected GenericResponse handleSubmit(){
            GenericResponse response=new GenericResponse();
            
            response.setStatus(0);
            return response;
        }
		
}
