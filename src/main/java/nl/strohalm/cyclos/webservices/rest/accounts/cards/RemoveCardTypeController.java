package nl.strohalm.cyclos.webservices.rest.accounts.cards;

import nl.strohalm.cyclos.entities.accounts.cards.CardType;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;
import nl.strohalm.cyclos.webservices.rest.GenericResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class RemoveCardTypeController extends BaseRestController {
    
    @RequestMapping(value = "admin/removeCardTypes/{id}", method = RequestMethod.GET)
    @ResponseBody
    public GenericResponse removeCard(@PathVariable("id") Long id) {
        GenericResponse response = new GenericResponse();
        int i = cardTypeService.remove(id);
        if (i > 0) {
            response.setMessage("Card type removed !");
        } else {
            response.setMessage("Error while reoveing!");
        }
        response.setStatus(0);
        return response;
    }
    
}
