package nl.strohalm.cyclos.webservices.rest.members.brokering;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import nl.strohalm.cyclos.annotations.Inject;
import nl.strohalm.cyclos.controls.restapi.BaseRestController;
import nl.strohalm.cyclos.services.elements.CommissionService;
@Controller
public class AcceptBrokerCommissionContractController extends BaseRestController{
	private CommissionService commissionService;

    @Inject
    public void setCommissionService(final CommissionService commissionService) {
        this.commissionService = commissionService;
    }
    
    public static class AcceptBrokerCommissionContractRequestDTO{
    	 private long              brokerCommissionContractId;

    	    public long getBrokerCommissionContractId() {
    	        return brokerCommissionContractId;
    	    }

    	    public void setBrokerCommissionContractId(final long brokerCommissionContractId) {
    	        this.brokerCommissionContractId = brokerCommissionContractId;
    	    }

    }
    
    public static class AcceptBrokerCommissionContractResponseDTO{
    	String message;

		public final String getMessage() {
			return message;
		}

		public final void setMessage(String message) {
			this.message = message;
		}
    	
    }

   @RequestMapping(value = "", method = RequestMethod.GET)
   @ResponseBody
    protected AcceptBrokerCommissionContractResponseDTO executeAction(@RequestBody AcceptBrokerCommissionContractRequestDTO form) throws Exception {
        final long brokerCommissionContractId = form.getBrokerCommissionContractId();
        commissionService.acceptBrokerCommissionContract(brokerCommissionContractId);
        AcceptBrokerCommissionContractResponseDTO resposne = new AcceptBrokerCommissionContractResponseDTO();
        resposne.setMessage("brokerCommissionContract.accepted");
        return resposne;
    }

}
