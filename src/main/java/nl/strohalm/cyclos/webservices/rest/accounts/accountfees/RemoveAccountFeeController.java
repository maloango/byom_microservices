package nl.strohalm.cyclos.webservices.rest.accounts.accountfees;

import org.apache.struts.action.ActionForward;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import nl.strohalm.cyclos.annotations.Inject;
import nl.strohalm.cyclos.controls.ActionContext;
import nl.strohalm.cyclos.controls.accounts.accountfees.RemoveAccountFeeForm;
import nl.strohalm.cyclos.services.accountfees.AccountFeeService;
import nl.strohalm.cyclos.utils.ActionHelper;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;

@Controller
public class RemoveAccountFeeController extends BaseRestController {

	private AccountFeeService accountFeeService;

	public AccountFeeService getAccountFeeService() {
		return accountFeeService;
	}

	@Inject
	public void setAccountFeeService(final AccountFeeService accountFeeService) {
		this.accountFeeService = accountFeeService;
	}

	@RequestMapping(value = "admin/removeAccountFee", method = RequestMethod.DELETE)
	@ResponseBody
	protected ActionForward executeAction(final ActionContext context)
			throws Exception {
		final RemoveAccountFeeForm form = context.getForm();
		try {
			accountFeeService.remove(form.getAccountFeeId());
			context.sendMessage("accountFee.removed");
		} catch (final Exception e) {
			context.sendMessage("accountFee.error.removing");
		}
		return ActionHelper.redirectWithParam(context.getRequest(),
				context.getSuccessForward(), "accountTypeId",
				form.getAccountTypeId());
	}
}
