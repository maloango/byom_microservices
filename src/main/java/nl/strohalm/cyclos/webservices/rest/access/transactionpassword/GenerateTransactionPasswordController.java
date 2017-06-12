package nl.strohalm.cyclos.webservices.rest.access.transactionpassword;

import nl.strohalm.cyclos.controls.ActionContext;
import nl.strohalm.cyclos.entities.access.TransactionPassword;
import nl.strohalm.cyclos.exceptions.PermissionDeniedException;
import nl.strohalm.cyclos.services.access.AccessService;
import nl.strohalm.cyclos.utils.JSONBuilder;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;

import org.apache.commons.httpclient.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class GenerateTransactionPasswordController extends BaseRestController {
	// later will be the implementation if required..
}
