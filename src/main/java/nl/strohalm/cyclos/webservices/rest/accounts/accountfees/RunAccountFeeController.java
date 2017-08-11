package nl.strohalm.cyclos.webservices.rest.accounts.accountfees;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import nl.strohalm.cyclos.annotations.Inject;
import nl.strohalm.cyclos.entities.accounts.fees.account.AccountFee;
import nl.strohalm.cyclos.services.accountfees.AccountFeeService;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class RunAccountFeeController extends BaseRestController {
	private AccountFeeService accountFeeService;

	public final AccountFeeService getAccountFeeService() {
		return accountFeeService;
	}

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
		 String message;
               

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
                 public RunAccountFeeResponseDto(){
                 }
	}
        // due to error flow missing flow later will be implement 
	@RequestMapping(value = "admin/runAccountFee/{accountFeeId}", method = RequestMethod.GET)
	@ResponseBody
	protected RunAccountFeeResponseDto executeAction(@PathVariable("accountFeeId") Long accountFeeId 
			 ) throws Exception {
		RunAccountFeeResponseDto response = new RunAccountFeeResponseDto();
                
                try {
		final AccountFee fee = accountFeeService.load(accountFeeId);
		accountFeeService.chargeManual(fee);
                
                    response.setMessage("accountFee.action.running");
                    response.message = toString();
                    
                    
                }
                catch(Exception e){
                    e.printStackTrace();
                    
                }
		
				
		return response;
	}
}
