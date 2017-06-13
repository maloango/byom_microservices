package nl.strohalm.cyclos.webservices.rest.accounts.accountfees;

import nl.strohalm.cyclos.annotations.Inject;
import nl.strohalm.cyclos.controls.ActionContext;
import nl.strohalm.cyclos.controls.accounts.accountfees.AccountFeeExecutionForm;
import nl.strohalm.cyclos.entities.accounts.fees.account.AccountFee;
import nl.strohalm.cyclos.services.accountfees.AccountFeeService;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;

import org.apache.struts.action.ActionForward;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class RunAccountFeeController extends BaseRestController {
	private AccountFeeService accountFeeService;

	@Inject
	public void setAccountFeeService(final AccountFeeService accountFeeService) {
		this.accountFeeService = accountFeeService;
	}

	public static class RunAccountFeeRequestDto {
		private long accountFeeId;
		private long accountFeeLogId;

		public long getAccountFeeId() {
			return accountFeeId;
		}

		public long getAccountFeeLogId() {
			return accountFeeLogId;
		}

		public void setAccountFeeId(final long accountFeeId) {
			this.accountFeeId = accountFeeId;
		}

		public void setAccountFeeLogId(final long accountFeeLogId) {
			this.accountFeeLogId = accountFeeLogId;
		}
	}

	public static class RunAccountFeeResponseDto {
		private String message;

		public RunAccountFeeResponseDto(String message) {
			super();
			this.message = message;
		}

		public String getMessage() {
			return message;
		}

		public void setMessage(String message) {
			this.message = message;
		}
	}

	@RequestMapping(value = "/admin/runAccountFee", method = RequestMethod.POST)
	@ResponseBody
	protected RunAccountFeeResponseDto executeAction(
			@RequestBody RunAccountFeeRequestDto form) throws Exception {
		// final AccountFeeExecutionForm form = context.getForm();
		final AccountFee fee = accountFeeService.load(form.getAccountFeeId());
		accountFeeService.chargeManual(fee);
		String message = "accountFee.action.running";
		RunAccountFeeResponseDto response = new RunAccountFeeResponseDto(
				message);
		return response;
	}
}
