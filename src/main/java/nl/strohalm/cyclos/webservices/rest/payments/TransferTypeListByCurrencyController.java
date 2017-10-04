/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.strohalm.cyclos.webservices.rest.payments;

import java.util.ArrayList;
import java.util.List;
import nl.strohalm.cyclos.entities.accounts.AccountOwner;
import nl.strohalm.cyclos.entities.accounts.Currency;
import nl.strohalm.cyclos.entities.accounts.transactions.TransferType;
import nl.strohalm.cyclos.entities.accounts.transactions.TransferTypeQuery;
import nl.strohalm.cyclos.services.transactions.TransactionContext;
import nl.strohalm.cyclos.utils.access.LoggedUser;
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
public class TransferTypeListByCurrencyController extends BaseRestController {

    public static class TransferTypeResponse extends GenericResponse {

        private List<TransferTypeEntity> transferTypes;

        public List<TransferTypeEntity> getTransferTypes() {
            return transferTypes;
        }

        public void setTransferTypes(List<TransferTypeEntity> transferTypes) {
            this.transferTypes = transferTypes;
        }

    

    }

    public static class TransferTypeEntity {

        private String name;
        private Long id;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

    }

    @RequestMapping(value = "admin/getTransferTypeByCurrency/{currencyId}", method = RequestMethod.GET)
    @ResponseBody
    public TransferTypeResponse getTransferType(@PathVariable("currencyId") Long CurrencyId) {
        TransferTypeResponse response=new TransferTypeResponse();
        Currency currency = new Currency();
        currency.setId(CurrencyId);
        final Long memberId = LoggedUser.user().getId();

        final TransferTypeQuery query = new TransferTypeQuery();
        query.setUsePriority(true);
        query.setContext(TransactionContext.SELF_PAYMENT);
        final AccountOwner owner = LoggedUser.accountOwner();
        query.setFromOwner(owner);
        query.setToOwner(owner);
        if (memberId != null) {
            query.setBy(LoggedUser.element());
        } else {
            query.setGroup(LoggedUser.group());
        }

        query.setCurrency(currency);
        final List<TransferType> tts = transferTypeService.search(query);

        List<TransferTypeEntity> transferList = new ArrayList();
        for (TransferType transferType : tts) {
            TransferTypeEntity transferEntity = new TransferTypeEntity();
            transferEntity.setId(transferType.getId());
            transferEntity.setName(transferType.getName());
            transferList.add(transferEntity);
        }

        response.setTransferTypes(transferList);
        response.setStatus(0);
        response.setMessage("Transfer type list");
        return response;

    }
}
