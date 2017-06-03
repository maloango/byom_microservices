package nl.strohalm.cyclos.webservices.rest.transactionpassword;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import nl.strohalm.cyclos.controls.ActionContext;
import nl.strohalm.cyclos.exceptions.PermissionDeniedException;
import nl.strohalm.cyclos.services.access.AccessService;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;

@Controller
public class GenerateTransactionPasswordController extends BaseRestController {
	private AccessService accessService;
	@RequestMapping(value="path=/member/generateTransactionPassword",method =RequestMethod.GET,produces ="application/json")
	@ResponseBody
    public ResponseEntity<String> renderContent(final ActionContext context) throws Exception {
        String transactionPassword = null;
        String errorKey = null;
        try {
            transactionPassword = accessService.generateTransactionPassword();
        } catch (final PermissionDeniedException e) {
            errorKey = "transactionPassword.error.permissionDenied";
        } catch (final Exception e) {
            errorKey = "transactionPassword.error.generating";
        }
        return new ResponseEntity<String>(transactionPassword,HttpStatus.ACCEPTED);

        
    }

}
