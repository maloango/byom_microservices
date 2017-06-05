package nl.strohalm.cyclos.webservices.rest.alerts;

import org.apache.struts.action.ActionForward;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import nl.strohalm.cyclos.annotations.Inject;
import nl.strohalm.cyclos.controls.AbstractActionContext;
import nl.strohalm.cyclos.controls.restapi.BaseRestController;
import nl.strohalm.cyclos.services.alerts.AlertService;

@Controller
public class RemoveAlertsController extends BaseRestController{

	private static AlertService alertService;

    public AlertService getAlertService() {
        return alertService;
    }

    @Inject
    public void setAlertService(final AlertService alertService) {
        RemoveAlertsController.alertService = alertService;
    }
    
    
    public static class RemoveAlertsRequestDto{
    	
    	private static AbstractActionContext actionMapping;
		private Long[]            alertIds;
        private String            alertType;

        public Long[] getAlertIds() {
            return alertIds;
        }

        public String getAlertType() {
            return alertType;
        }

        public void setAlertIds(final Long[] alertIds) {
            this.alertIds = alertIds;
        }

        public void setAlertType(final String alertType) {
            this.alertType = alertType;
        }

		public static ActionForward findForward(final String name) {
        return actionMapping.findForward(name);
    

    }

    	
    public static class RemoveAlertsResponseDTO{
    	public String message;

		public final String getMessage() {
			return message;
		}

		public final void setMessage(String message) {
			this.message = message;
		}
		private boolean isMember;

		public final boolean isMember() {
			return isMember;
		}

		public final void setMember(boolean isMember) {
			this.isMember = isMember;
		}
    	
    	
    }
    
    

    @RequestMapping(value = "/member/manageTransactionPassword", method = RequestMethod.DELETE)
    @ResponseBody
    protected void executeAction(@RequestBody final RemoveAlertsRequestDto form) throws Exception {
        // final RemoveAlertsForm form = context.getForm();
        final boolean isMember = "MEMBER".equals(form.getAlertType());
        RemoveAlertsResponseDTO response= new RemoveAlertsResponseDTO();
        alertService.removeAlerts(form.getAlertIds());
        response.setMessage("alert.removed");
        RemoveAlertsRequestDto.findForward(isMember ? "toMember" : "toSystem");
    }

	
	}


}
