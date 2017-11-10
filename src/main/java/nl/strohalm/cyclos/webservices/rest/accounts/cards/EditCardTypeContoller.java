package nl.strohalm.cyclos.webservices.rest.accounts.cards;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import nl.strohalm.cyclos.access.AdminSystemPermission;
import nl.strohalm.cyclos.annotations.Inject;
import nl.strohalm.cyclos.entities.accounts.cards.CardType;
import nl.strohalm.cyclos.services.accounts.cards.CardTypeService;
import nl.strohalm.cyclos.utils.RangeConstraint;
import nl.strohalm.cyclos.utils.RequestHelper;
import nl.strohalm.cyclos.utils.TimePeriod;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;
import nl.strohalm.cyclos.webservices.rest.GenericResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class EditCardTypeContoller extends BaseRestController {
    
    public static class CardTypesResponse extends GenericResponse {
        
        private List<CardType.CardSecurityCode> cardTypeSecurityCode;
        private boolean editable;
        private boolean hasCardGenerated;
        private List<TimePeriod.Field> time1;
        private List<TimePeriod.Field> time2;
        private CardTypesEntity cardTypes;
        
        public CardTypesEntity getCardTypes() {
            return cardTypes;
        }
        
        public void setCardTypes(CardTypesEntity cardTypes) {
            this.cardTypes = cardTypes;
        }
        
        public List<CardType.CardSecurityCode> getCardTypeSecurityCode() {
            return cardTypeSecurityCode;
        }
        
        public void setCardTypeSecurityCode(List<CardType.CardSecurityCode> cardTypeSecurityCode) {
            this.cardTypeSecurityCode = cardTypeSecurityCode;
        }
        
        public boolean isEditable() {
            return editable;
        }
        
        public void setEditable(boolean editable) {
            this.editable = editable;
        }
        
        public boolean isHasCardGenerated() {
            return hasCardGenerated;
        }
        
        public void setHasCardGenerated(boolean hasCardGenerated) {
            this.hasCardGenerated = hasCardGenerated;
        }
        
        public List<TimePeriod.Field> getTime1() {
            return time1;
        }
        
        public void setTime1(List<TimePeriod.Field> time1) {
            this.time1 = time1;
        }
        
        public List<TimePeriod.Field> getTime2() {
            return time2;
        }
        
        public void setTime2(List<TimePeriod.Field> time2) {
            this.time2 = time2;
        }
        
    }
    
    public static class CardTypesEntity {
        
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
        
    }
    
    @RequestMapping(value = "admin/editCardTypes", method = RequestMethod.GET)
    @ResponseBody
    public CardTypesResponse cardData() {
        CardTypesResponse response = new CardTypesResponse();
        boolean editable;
        boolean hasCardGenerated = false;
        
        editable = permissionService.hasPermission(AdminSystemPermission.CARD_TYPES_MANAGE);
        
        CardTypesEntity cardTypes = new CardTypesEntity();
        editable = true;
        response.setCardTypes(cardTypes);
        //request.setAttribute("isInsert", isInsert);
        response.setEditable(editable);
        response.setHasCardGenerated(hasCardGenerated);
        response.setTime1(Arrays.asList(TimePeriod.Field.MONTHS, TimePeriod.Field.YEARS));
        response.setTime2(Arrays.asList(TimePeriod.Field.MINUTES, TimePeriod.Field.HOURS, TimePeriod.Field.DAYS));
        //RequestHelper.storeEnum(request, CardType.CardSecurityCode.class, "cardSecurityCodes");
        List<CardType.CardSecurityCode> cardTypeSecurityCode = new ArrayList();
        cardTypeSecurityCode.add(CardType.CardSecurityCode.MANUAL);
        cardTypeSecurityCode.add(CardType.CardSecurityCode.AUTOMATIC);
        cardTypeSecurityCode.add(CardType.CardSecurityCode.NOT_USED);
        response.setCardTypeSecurityCode(cardTypeSecurityCode);
        response.setStatus(0);
        
        return response;
    }
    
    public static class CardTypesParameters {
        
        private Long id;
        private String name;
        private String cardFormatNumber;
        private String cardSecurityCode;
        private boolean ignoreDayInExpirationDate;
        private int maxSecurityCodeTries;
         private boolean showCardSecurityCode = false;

        private String securityCodeBlockTime_field;
        private int securityCodeBlockTime_number;
        private int cardSecurityCodeLength_min;
        private int cardSecurityCodeLength_max;

        public boolean isShowCardSecurityCode() {
            return showCardSecurityCode;
        }

        public void setShowCardSecurityCode(boolean showCardSecurityCode) {
            this.showCardSecurityCode = showCardSecurityCode;
        }
        
        
        public int getMaxSecurityCodeTries() {
            return maxSecurityCodeTries;
        }
        
        public void setMaxSecurityCodeTries(int maxSecurityCodeTries) {
            this.maxSecurityCodeTries = maxSecurityCodeTries;
        }
        
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
        
        public String getCardFormatNumber() {
            return cardFormatNumber;
        }
        
        public void setCardFormatNumber(String cardFormatNumber) {
            this.cardFormatNumber = cardFormatNumber;
        }
        
        public String getCardSecurityCode() {
            return cardSecurityCode;
        }
        
        public void setCardSecurityCode(String cardSecurityCode) {
            this.cardSecurityCode = cardSecurityCode;
        }
        
        public boolean isIgnoreDayInExpirationDate() {
            return ignoreDayInExpirationDate;
        }
        
        public void setIgnoreDayInExpirationDate(boolean ignoreDayInExpirationDate) {
            this.ignoreDayInExpirationDate = ignoreDayInExpirationDate;
        }

        public String getSecurityCodeBlockTime_field() {
            return securityCodeBlockTime_field;
        }

        public void setSecurityCodeBlockTime_field(String securityCodeBlockTime_field) {
            this.securityCodeBlockTime_field = securityCodeBlockTime_field;
        }

        public int getSecurityCodeBlockTime_number() {
            return securityCodeBlockTime_number;
        }

        public void setSecurityCodeBlockTime_number(int securityCodeBlockTime_number) {
            this.securityCodeBlockTime_number = securityCodeBlockTime_number;
        }

     
        public int getCardSecurityCodeLength_min() {
            return cardSecurityCodeLength_min;
        }
        
        public void setCardSecurityCodeLength_min(int cardSecurityCodeLength_min) {
            this.cardSecurityCodeLength_min = cardSecurityCodeLength_min;
        }
        
        public int getCardSecurityCodeLength_max() {
            return cardSecurityCodeLength_max;
        }
        
        public void setCardSecurityCodeLength_max(int cardSecurityCodeLength_max) {
            this.cardSecurityCodeLength_max = cardSecurityCodeLength_max;
        }
        
    }
    
    @RequestMapping(value = "admin/editCardTypes", method = RequestMethod.POST)
    @ResponseBody
    public GenericResponse submit(@RequestBody CardTypesParameters params) {
        GenericResponse response = new GenericResponse();
        CardType cardType = new CardType();
        if (params.getId() != null && params.getId() > 0L) {
            cardType.setId(params.getId());
        }
        cardType.setName(params.getName());
        cardType.setCardFormatNumber(params.getCardFormatNumber());
        cardType.setCardSecurityCode(CardType.CardSecurityCode.valueOf(params.getCardSecurityCode()));
        cardType.setIgnoreDayInExpirationDate(params.isIgnoreDayInExpirationDate());
        cardType.setMaxSecurityCodeTries(params.getMaxSecurityCodeTries());
        TimePeriod tp = new TimePeriod();
        tp.setField(TimePeriod.Field.valueOf(params.getSecurityCodeBlockTime_field()));
        tp.setNumber(params.getSecurityCodeBlockTime_number());
        cardType.setSecurityCodeBlockTime(tp);
        RangeConstraint rc = new RangeConstraint();
        rc.setMax(params.getCardSecurityCodeLength_max());
        rc.setMin(params.getCardSecurityCodeLength_min());
        cardType.setShowCardSecurityCode(params.isShowCardSecurityCode());
        cardType.setCardSecurityCodeLength(rc);
        
        final boolean isInsert = cardType.isTransient();
        cardType = cardTypeService.save(cardType);
        if (isInsert) {
            response.setMessage("cardType.inserted");
        } else {
            response.setMessage("cardType.modified");
        }
        response.setStatus(0);
        return response;
        
    }
    
}
