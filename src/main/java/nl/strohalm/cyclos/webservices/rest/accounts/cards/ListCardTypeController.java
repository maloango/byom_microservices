package nl.strohalm.cyclos.webservices.rest.accounts.cards;

import java.util.ArrayList;
import java.util.List;
import nl.strohalm.cyclos.annotations.Inject;
import nl.strohalm.cyclos.entities.accounts.cards.CardType;
import nl.strohalm.cyclos.services.accounts.cards.CardTypeService;
import org.springframework.stereotype.Controller;

import nl.strohalm.cyclos.webservices.rest.BaseRestController;
import nl.strohalm.cyclos.webservices.rest.GenericResponse;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class ListCardTypeController extends BaseRestController {

    private CardTypeService cardTypeService;

    public CardTypeService getCardTypeService() {
        return cardTypeService;
    }

    @Inject
    public void setCardTypeService(CardTypeService cardTypeService) {
        this.cardTypeService = cardTypeService;
    }

    public static class ListCardTypeResponse extends GenericResponse {

        private List<CardType> cardTypes;

        public List<CardType> getCardTypes() {
            return cardTypes;
        }

        public void setCardTypes(List<CardType> cardTypes) {
            this.cardTypes = cardTypes;
        }

    }

    @RequestMapping(value = "admin/listCardTypes", method = RequestMethod.GET)
    @ResponseBody
    public ListCardTypeResponse listCardType()throws Exception{
        ListCardTypeResponse response=new ListCardTypeResponse();
         final List<CardType> cardTypes = new ArrayList<CardType>();
         cardTypes.addAll(cardTypeService.listAll());
         response.setCardTypes(cardTypes);
         response.setStatus(0);
         response.setMessage("card list!");
         return response;
    }


}
