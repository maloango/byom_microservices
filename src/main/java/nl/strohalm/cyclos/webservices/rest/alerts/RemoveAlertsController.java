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
import nl.strohalm.cyclos.webservices.rest.GenericResponse;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class RemoveAlertsController extends BaseRestController {

    private static AlertService alertService;

    public AlertService getAlertService() {
        return alertService;
    }

    @Inject
    public void setAlertService(final AlertService alertService) {
        RemoveAlertsController.alertService = alertService;
    }

    @RequestMapping(value = "admin/removeAlerts/{alertIds}/alertTypeParam", method = RequestMethod.GET)
    @ResponseBody
    public GenericResponse removeAlert(@PathVariable("alertIds") long alertIds, @RequestParam("alertType") String alertType) throws Exception {
        GenericResponse response = new GenericResponse();
        try {
            final boolean isMember = "MEMBER".equals(alertType);
            alertService.removeAlerts(alertIds);
            response.setMessage("alert.removed");
            response.setStatus(0);
            response.setMessage("Alerts removed!!");
        } catch (Exception e) {

            e.printStackTrace();
            response.setStatus(1);
            response.setMessage("error in remove the alert");

        }

        return response;

    }
}
