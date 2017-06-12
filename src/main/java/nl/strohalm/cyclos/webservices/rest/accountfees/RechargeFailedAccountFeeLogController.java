package nl.strohalm.cyclos.webservices.rest.accountfees;

import org.apache.struts.action.ActionForward;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import nl.strohalm.cyclos.annotations.Inject;
import nl.strohalm.cyclos.controls.ActionContext;
import nl.strohalm.cyclos.controls.accounts.accountfees.AccountFeeExecutionForm;
import nl.strohalm.cyclos.entities.accounts.fees.account.AccountFeeLog;
import nl.strohalm.cyclos.services.accountfees.AccountFeeService;
import nl.strohalm.cyclos.utils.ActionHelper;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;

@Controller
public class RechargeFailedAccountFeeLogController extends BaseRestController {
	private AccountFeeService accountFeeService;

	@Inject
	public void setAccountFeeService(final AccountFeeService accountFeeService) {
		this.accountFeeService = accountFeeService;
	}

	public static class RechargeFailedAccountFeeLogRequestDto {
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

	public static class RechargeFailedAccountFeeLogResponseDto {
		private Long accountFeeLogId;

		public RechargeFailedAccountFeeLogResponseDto(Long accountFeeLogId) {
			super();
			this.accountFeeLogId = accountFeeLogId;
		}

	}

	@RequestMapping(value = "", method = RequestMethod.GET)
	@ResponseBody
	protected RechargeFailedAccountFeeLogResponseDto executeAction(
			@RequestBody RechargeFailedAccountFeeLogRequestDto form)
			throws Exception {
		// final AccountFeeExecutionForm form = context.getForm();
		final AccountFeeLog log = accountFeeService.loadLog(form
				.getAccountFeeLogId());
		accountFeeService.rechargeFailed(log);
		Long accountFeeLogId = log.getId();
		RechargeFailedAccountFeeLogResponseDto response = new RechargeFailedAccountFeeLogResponseDto(
				accountFeeLogId);
		return response;
	}

}
