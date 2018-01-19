package nl.strohalm.cyclos.webservices.rest.ads;

import java.util.Map;

import org.apache.struts.upload.FormFile;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import nl.strohalm.cyclos.annotations.Inject;
import nl.strohalm.cyclos.services.ads.AdService;
import nl.strohalm.cyclos.utils.validation.ValidationException;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;
import nl.strohalm.cyclos.webservices.rest.GenericResponse;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class RemoveAdController extends BaseRestController {

    @RequestMapping(value = "admin/removeAd/{adId}", method = RequestMethod.GET)
    @ResponseBody
    protected GenericResponse executeAction(@PathVariable("adId") long adId) throws Exception {

        GenericResponse response = new GenericResponse();
        if (adId <= 0) {
            throw new ValidationException();
        }
        // Remove the advertisement
        adService.remove(adId);
        response.setMessage("ad.removed");
        response.setStatus(0);
        return response;
    }
}
