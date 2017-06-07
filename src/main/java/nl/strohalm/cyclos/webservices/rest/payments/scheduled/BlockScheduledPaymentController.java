package nl.strohalm.cyclos.webservices.rest.payments.scheduled;

import org.apache.struts.action.ActionForward;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import nl.strohalm.cyclos.controls.ActionContext;
import nl.strohalm.cyclos.entities.accounts.transactions.ScheduledPayment;
import nl.strohalm.cyclos.utils.ActionHelper;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;

@Controller
public class BlockScheduledPaymentController extends BaseRestController {
	// later will be the implementation if required..
}
