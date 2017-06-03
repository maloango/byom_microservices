package nl.strohalm.cyclos.webservices.rest.guarantees;

import org.apache.struts.action.ActionForward;
import org.springframework.stereotype.Controller;

import nl.strohalm.cyclos.annotations.Inject;
import nl.strohalm.cyclos.controls.ActionContext;
import nl.strohalm.cyclos.controls.accounts.guarantees.guarantees.ChangeStatusGuaranteeForm;
import nl.strohalm.cyclos.entities.accounts.guarantees.Guarantee;
import nl.strohalm.cyclos.services.accounts.guarantees.GuaranteeService;
import nl.strohalm.cyclos.services.accounts.guarantees.exceptions.GuaranteeStatusChangeException;
import nl.strohalm.cyclos.utils.ActionHelper;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;

@Controller
public class ChangeStatusGuaranteeController extends BaseRestController {
	// later will be the implementation if required..
}
