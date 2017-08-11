package nl.strohalm.cyclos.webservices.rest.accounts.accountfees;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import nl.strohalm.cyclos.annotations.Inject;
import nl.strohalm.cyclos.entities.accounts.fees.account.AccountFeeLog;
import nl.strohalm.cyclos.services.accountfees.AccountFeeService;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;

@Controller
public class RechargeFailedAccountFeeLogController extends BaseRestController {
	private AccountFeeService accountFeeService;

	public final AccountFeeService getAccountFeeService() {
		return accountFeeService;
	}

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

	public static class RechargeFailedAccountFeeLogResponseDTO {
		private Long accountFeeLogId;
                private Long getID;

        public Long getAccountFeeLogId() {
            return accountFeeLogId;
        }

        public void setAccountFeeLogId(Long accountFeeLogId) {
            this.accountFeeLogId = accountFeeLogId;
        }

        public Long getGetID() {
            return getID;
        }

        public void setGetID(Long getID) {
            this.getID = getID;
        }
                public RechargeFailedAccountFeeLogResponseDTO(){}

		public RechargeFailedAccountFeeLogResponseDTO(Long accountFeeLogId) {
			super();
			this.accountFeeLogId = accountFeeLogId;
		}

	}

	@RequestMapping(value = "admin/rechargeFailedAccountFeeLog", method = RequestMethod.GET)
	@ResponseBody
	protected RechargeFailedAccountFeeLogResponseDTO executeAction(
			@RequestBody RechargeFailedAccountFeeLogRequestDto form, Long accountFeeLogId)
			throws Exception {
            RechargeFailedAccountFeeLogResponseDTO response = new RechargeFailedAccountFeeLogResponseDTO(accountFeeLogId);
				
		try{
		final AccountFeeLog log = accountFeeService.loadLog(form
				.getAccountFeeLogId());
		accountFeeService.rechargeFailed(log);
		response = new RechargeFailedAccountFeeLogResponseDTO(accountFeeLogId);}
                catch(Exception e){
                    e.printStackTrace();
                }
		return response;
	}

}
