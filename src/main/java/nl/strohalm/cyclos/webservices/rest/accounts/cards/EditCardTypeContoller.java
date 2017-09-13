package nl.strohalm.cyclos.webservices.rest.accounts.cards;

import nl.strohalm.cyclos.annotations.Inject;
import nl.strohalm.cyclos.services.accounts.cards.CardTypeService;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;
import nl.strohalm.cyclos.webservices.rest.GenericResponse;

public class EditCardTypeContoller extends BaseRestController {

    private CardTypeService cardTypeService;

    public CardTypeService getCardTypeService() {
        return cardTypeService;
    }

    @Inject
    public void setCardTypeService(CardTypeService cardTypeService) {
        this.cardTypeService = cardTypeService;
    }
    
    public static class EditCardResponse extends GenericResponse{
        private Long id;
        private String name;
        private String cardFormatNumber;
        private Integer defaultExpiration;
        
        
    }

}
