package nl.strohalm.cyclos.webservices.rest.alerts;

import org.apache.struts.action.ActionForward;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import nl.strohalm.cyclos.annotations.Inject;
import nl.strohalm.cyclos.controls.AbstractActionContext;
import nl.strohalm.cyclos.services.alerts.AlertService;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;
import org.springframework.web.bind.annotation.PathVariable;

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

        public static AbstractActionContext getActionMapping() {
            return actionMapping;
        }

        public static void setActionMapping(AbstractActionContext actionMapping) {
            RemoveAlertsRequestDto.actionMapping = actionMapping;
        }
        
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
                private String            alertType;
                private Long[]            alertIds;

                public String getAlertType() {
                    return alertType;
                }

                public void setAlertType(String alertType) {
                    this.alertType = alertType;
                }

                public Long[] getAlertIds() {
                    return alertIds;
                }

                public void setAlertIds(Long[] alertIds) {
                    this.alertIds = alertIds;
                }
                
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

                public boolean isIsMember() {
                    return isMember;
                }

                public void setIsMember(boolean isMember) {
                    this.isMember = isMember;
                }
                
    	public RemoveAlertsResponseDTO(){
        }
    }
    
    @RequestMapping(value = "admin/removeAlerts{alertIds}", method = RequestMethod.GET)
    @ResponseBody
    protected RemoveAlertsResponseDTO executeAction(@PathVariable ("alertIds")long alertIds) throws Exception {
        RemoveAlertsResponseDTO response = new RemoveAlertsResponseDTO();
        try{
        final boolean isMember = "MEMBER".equals(response.getAlertType());
        alertService.removeAlerts(response.getAlertIds());
        response.setMessage("alert.removed");
        RemoveAlertsRequestDto.findForward(isMember ? "toMember" : "toSystem");
        response = new RemoveAlertsResponseDTO();}
        catch(Exception e){
            e.printStackTrace();
            
        }
    
        return response;
	
	}
    }
}

    

