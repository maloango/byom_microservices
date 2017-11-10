/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.strohalm.cyclos.webservices.rest.accounts.cards;

import nl.strohalm.cyclos.entities.accounts.cards.CardType;
import nl.strohalm.cyclos.utils.RangeConstraint;
import nl.strohalm.cyclos.utils.TimePeriod;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;
import nl.strohalm.cyclos.webservices.rest.GenericResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 *
 * @author Lue Infoservices
 */
@Controller
public class GetCardTypeByIdController extends BaseRestController {

    public static class CardTypeResponse extends GenericResponse {

        private CardTypesEntity cardTypeElement;

        public CardTypesEntity getCardTypeElement() {
            return cardTypeElement;
        }

        public void setCardTypeElement(CardTypesEntity cardTypeElement) {
            this.cardTypeElement = cardTypeElement;
        }

    }

    public static class CardTypesEntity {

        private Long id;
        private String name;
        private String cardFormatNumber = "#### #### #### ####";
        private TimePeriod defaultExpiration = new TimePeriod(1, TimePeriod.Field.YEARS);
        private CardType.CardSecurityCode cardSecurityCode = CardType.CardSecurityCode.NOT_USED;
        private boolean showCardSecurityCode = false;
        private boolean ignoreDayInExpirationDate = true;
        private RangeConstraint cardSecurityCodeLength = new RangeConstraint(4, 4);
        private int maxSecurityCodeTries = 3;
        private TimePeriod securityCodeBlockTime = new TimePeriod(1, TimePeriod.Field.DAYS);

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getCardFormatNumber() {
            return cardFormatNumber;
        }

        public void setCardFormatNumber(String cardFormatNumber) {
            this.cardFormatNumber = cardFormatNumber;
        }

        public TimePeriod getDefaultExpiration() {
            return defaultExpiration;
        }

        public void setDefaultExpiration(TimePeriod defaultExpiration) {
            this.defaultExpiration = defaultExpiration;
        }

        public CardType.CardSecurityCode getCardSecurityCode() {
            return cardSecurityCode;
        }

        public void setCardSecurityCode(CardType.CardSecurityCode cardSecurityCode) {
            this.cardSecurityCode = cardSecurityCode;
        }

        public boolean isShowCardSecurityCode() {
            return showCardSecurityCode;
        }

        public void setShowCardSecurityCode(boolean showCardSecurityCode) {
            this.showCardSecurityCode = showCardSecurityCode;
        }

        public boolean isIgnoreDayInExpirationDate() {
            return ignoreDayInExpirationDate;
        }

        public void setIgnoreDayInExpirationDate(boolean ignoreDayInExpirationDate) {
            this.ignoreDayInExpirationDate = ignoreDayInExpirationDate;
        }

        public RangeConstraint getCardSecurityCodeLength() {
            return cardSecurityCodeLength;
        }

        public void setCardSecurityCodeLength(RangeConstraint cardSecurityCodeLength) {
            this.cardSecurityCodeLength = cardSecurityCodeLength;
        }

        public int getMaxSecurityCodeTries() {
            return maxSecurityCodeTries;
        }

        public void setMaxSecurityCodeTries(int maxSecurityCodeTries) {
            this.maxSecurityCodeTries = maxSecurityCodeTries;
        }

        public TimePeriod getSecurityCodeBlockTime() {
            return securityCodeBlockTime;
        }

        public void setSecurityCodeBlockTime(TimePeriod securityCodeBlockTime) {
            this.securityCodeBlockTime = securityCodeBlockTime;
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

    }

    @RequestMapping(value = "admin/getCardTypeById/{id}", method = RequestMethod.GET)
    @ResponseBody
    public CardTypeResponse getCardType(@PathVariable("id") Long id) {
        CardTypeResponse response = new CardTypeResponse();
        CardType cardType = cardTypeService.load(id);
        CardTypesEntity card = new CardTypesEntity();
        card.setId(cardType.getId());
        card.setCardFormatNumber(cardType.getCardFormatNumber());
        card.setCardSecurityCode(cardType.getCardSecurityCode());
        card.setCardSecurityCodeLength(cardType.getCardSecurityCodeLength());
        card.setDefaultExpiration(cardType.getDefaultExpiration());
        card.setIgnoreDayInExpirationDate(cardType.isIgnoreDayInExpirationDate());
        card.setMaxSecurityCodeTries(cardType.getMaxSecurityCodeTries());
        card.setName(cardType.getName());
        card.setSecurityCodeBlockTime(cardType.getSecurityCodeBlockTime());
        card.setShowCardSecurityCode(cardType.isShowCardSecurityCode());
        response.setCardTypeElement(card);
        response.setStatus(0);
        return response;
    }

}
