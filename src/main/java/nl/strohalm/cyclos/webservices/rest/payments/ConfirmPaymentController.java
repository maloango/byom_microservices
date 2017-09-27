package nl.strohalm.cyclos.webservices.rest.payments;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import org.apache.struts.action.ActionForward;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import nl.strohalm.cyclos.annotations.Inject;
import nl.strohalm.cyclos.controls.ActionContext;
import nl.strohalm.cyclos.entities.accounts.Currency;
import nl.strohalm.cyclos.entities.accounts.transactions.Payment;
import nl.strohalm.cyclos.entities.accounts.transactions.ScheduledPayment;
import nl.strohalm.cyclos.entities.accounts.transactions.Transfer;
import nl.strohalm.cyclos.entities.accounts.transactions.TransferType;
import nl.strohalm.cyclos.entities.exceptions.UnexpectedEntityException;
import nl.strohalm.cyclos.services.access.AccessService;
import nl.strohalm.cyclos.services.access.exceptions.BlockedCredentialsException;
import nl.strohalm.cyclos.services.access.exceptions.InvalidCredentialsException;
import nl.strohalm.cyclos.services.customization.PaymentCustomFieldService;
import nl.strohalm.cyclos.services.elements.ElementService;
import nl.strohalm.cyclos.services.transactions.DoPaymentDTO;
import nl.strohalm.cyclos.services.transactions.PaymentService;
import nl.strohalm.cyclos.services.transactions.exceptions.AuthorizedPaymentInPastException;
import nl.strohalm.cyclos.services.transactions.exceptions.CreditsException;
import nl.strohalm.cyclos.services.transfertypes.TransactionFeeService;
import nl.strohalm.cyclos.services.transfertypes.TransferTypeService;
import nl.strohalm.cyclos.utils.ActionHelper;
import nl.strohalm.cyclos.utils.CustomFieldHelper;
import nl.strohalm.cyclos.utils.validation.ValidationException;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;
import nl.strohalm.cyclos.webservices.rest.GenericResponse;

@Controller
public class ConfirmPaymentController extends BaseRestController {

    public static class PaymentEntity {

        private BigDecimal amount;
        private String description;
        private Long currencyId;
        private Long transferTypeId;

        public BigDecimal getAmount() {
            return amount;
        }

        public void setAmount(BigDecimal amount) {
            this.amount = amount;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public Long getCurrencyId() {
            return currencyId;
        }

        public void setCurrencyId(Long currencyId) {
            this.currencyId = currencyId;
        }

        public Long getTransferTypeId() {
            return transferTypeId;
        }

        public void setTransferTypeId(Long transferTypeId) {
            this.transferTypeId = transferTypeId;
        }

    }

    @RequestMapping(value = "admin/confirmPayment", method = RequestMethod.POST)
    @ResponseBody
    public GenericResponse doPayment(@RequestBody PaymentEntity request) {
        GenericResponse response = new GenericResponse();
        
        final DoPaymentDTO paymentDTO = new DoPaymentDTO();
        paymentDTO.setDescription(request.getDescription());
        paymentDTO.setAmount(request.getAmount());
        Currency currency=new Currency();
        currency.setId(request.getCurrencyId());
        paymentDTO.setCurrency(currency);
        
        TransferType transferType=new TransferType();
        transferType.setId(request.getTransferTypeId());
        paymentDTO.setTransferType(transferType);
        
        System.out.println("-----currency: ---"+currency);
        System.out.println("-----Transfertyep: ---"+transferType);
        
        
        // Validate the transaction password if needed
//        if (shouldValidateTransactionPassword(context, paymentDTO)) {
//            context.checkTransactionPassword(form.getTransactionPassword());
//        }
        // Perform the actual payment
        Payment payment;
        try {
            payment = paymentService.doPayment(paymentDTO);
            System.out.println("---paymentDto---:"+paymentDTO);
//            context.getSession().removeAttribute("payment");
        } catch (final CreditsException e) {
         response.setStatus(1);
        } catch (final UnexpectedEntityException e) {
            response.setMessage("payment.error.invalidTransferType");
        } catch (final AuthorizedPaymentInPastException e) {
             response.setMessage("payment.error.authorizedInPast");
        }
        // Redirect to the next action
//        final Map<String, Object> params = new HashMap<String, Object>();
//        ActionForward forward;
//        if (payment instanceof Transfer) {
//            params.put("transferId", payment.getId());
//            forward = context.getSuccessForward();
//        } else if (payment instanceof ScheduledPayment) {
//            params.put("paymentId", payment.getId());
//            forward = context.findForward("scheduledPayment");
//        } else {
//            throw new IllegalStateException("Unknown payment type: " + payment);
//        }
//        params.put("selectMember", form.getSelectMember());
//        params.put("from", form.getFrom());
        return response;

    }
}
