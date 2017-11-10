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
    
    public static class ListCardTypeResponse extends GenericResponse {
        
        private List<CardTypesEntity> cardTypesList;
        
        public List<CardTypesEntity> getCardTypesList() {
            return cardTypesList;
        }
        
        public void setCardTypesList(List<CardTypesEntity> cardTypesList) {
            this.cardTypesList = cardTypesList;
        }
        
    }
    
    public static class CardTypesEntity {

        private Long id;
        private String name;
        
        public Long getId() {
            return id;
        }
        
        public void setId(Long id) {
            this.id = id;
        }
        
        public String getName() {
            return name;
        }
        
        public void setName(String name) {
            this.name = name;
        }
        
    }
    
    @RequestMapping(value = "admin/listCardTypes", method = RequestMethod.GET)
    @ResponseBody
    public ListCardTypeResponse listCardType() throws Exception {
        ListCardTypeResponse response = new ListCardTypeResponse();
        final List<CardType> cardTypes = cardTypeService.listAll();
        List<CardTypesEntity> cardTypesList = new ArrayList();
        for (CardType type : cardTypes) {
            CardTypesEntity entity = new CardTypesEntity();
            entity.setId(type.getId());
            entity.setName(type.getName());
            cardTypesList.add(entity);
        }
        response.setCardTypesList(cardTypesList);
        response.setStatus(0);
        response.setMessage("card list!");
        return response;
    }
    
}
