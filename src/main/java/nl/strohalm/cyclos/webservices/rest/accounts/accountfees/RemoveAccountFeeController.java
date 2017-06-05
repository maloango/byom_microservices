package nl.strohalm.cyclos.webservices.rest.accounts.accountfees;

import org.apache.struts.action.ActionForward;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
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

	public static class RemoveAccountFeeRequestDTO {
		private long accountTypeId;
		private long accountFeeId;

		public long getAccountFeeId() {
			return accountFeeId;
		}

		public long getAccountTypeId() {
			return accountTypeId;
		}

		public void setAccountFeeId(final long accountFeeId) {
			this.accountFeeId = accountFeeId;
		}

		public void setAccountTypeId(final long accountTypeId) {
			this.accountTypeId = accountTypeId;
		}
	}

	public static class RemoveAccountFeeResponseDTO {
		String message;

		public final String getMessage() {
			return message;
		}

		public final void setMessage(String message) {
			this.message = message;
		}
	}

	@RequestMapping(value = "/admin/removeAccountFee", method = RequestMethod.DELETE)
	@ResponseBody
	protected RemoveAccountFeeResponseDTO executeAction(@RequestBody RemoveAccountFeeRequestDTO form) throws Exception {
		// final RemoveAccountFeeForm form = context.getForm();
		RemoveAccountFeeResponseDTO response = new RemoveAccountFeeResponseDTO();
		try {
			accountFeeService.remove(form.getAccountFeeId());
			response.setMessage("accountFee.removed");
		} catch (final Exception e) {
			response.setMessage("accountFee.error.removing");
		}
		return response;
	}
}
