package nl.strohalm.cyclos.webservices.rest.channels;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import nl.strohalm.cyclos.annotations.Inject;
import nl.strohalm.cyclos.services.access.ChannelService;
import nl.strohalm.cyclos.utils.validation.ValidationException;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;
import nl.strohalm.cyclos.webservices.rest.GenericResponse;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class RemoveChannelController extends BaseRestController {

    @RequestMapping(value = "admin/removeChannel/{channelId}", method = RequestMethod.GET)
    @ResponseBody
    protected GenericResponse executeAction(@PathVariable("channelId") long channelId) throws Exception {
        GenericResponse response = new GenericResponse();

        final long id = channelId;
        if (id <= 0L) {
            throw new ValidationException();
        }
        try {
            channelService.remove(id);
            response.setMessage("channel.removed");
        } catch (final Exception e) {
            response.setMessage("channel.errorRemoving");
        }
        response.setStatus(0);
        return response;

    }
}
