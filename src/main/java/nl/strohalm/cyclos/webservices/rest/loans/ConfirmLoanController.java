package nl.strohalm.cyclos.webservices.rest.loans;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import nl.strohalm.cyclos.access.AdminMemberPermission;
import nl.strohalm.cyclos.controls.ActionContext;
import nl.strohalm.cyclos.entities.accounts.AccountType;
import nl.strohalm.cyclos.entities.accounts.loans.Loan;
import nl.strohalm.cyclos.entities.accounts.loans.LoanGroup;
import nl.strohalm.cyclos.entities.accounts.loans.LoanParameters;
import nl.strohalm.cyclos.entities.accounts.loans.LoanPayment;
import nl.strohalm.cyclos.entities.accounts.transactions.TransferType;
import nl.strohalm.cyclos.entities.customization.fields.PaymentCustomField;
import nl.strohalm.cyclos.entities.customization.fields.PaymentCustomFieldValue;
import nl.strohalm.cyclos.entities.members.Element;
import nl.strohalm.cyclos.entities.members.Member;
import nl.strohalm.cyclos.entities.settings.LocalSettings;
import nl.strohalm.cyclos.exceptions.PermissionDeniedException;
import static nl.strohalm.cyclos.http.AttributeHolder.Factory.context;
import nl.strohalm.cyclos.services.transactions.GrantLoanDTO;
import nl.strohalm.cyclos.services.transactions.GrantLoanWithInterestDTO;
import nl.strohalm.cyclos.services.transactions.GrantMultiPaymentLoanDTO;
import nl.strohalm.cyclos.services.transactions.GrantSinglePaymentLoanDTO;
import nl.strohalm.cyclos.services.transactions.exceptions.AuthorizedPaymentInPastException;
import nl.strohalm.cyclos.services.transactions.exceptions.CreditsException;
import nl.strohalm.cyclos.utils.RelationshipHelper;
import nl.strohalm.cyclos.utils.binding.BeanBinder;
import nl.strohalm.cyclos.utils.binding.BeanCollectionBinder;
import nl.strohalm.cyclos.utils.binding.DataBinder;
import nl.strohalm.cyclos.utils.binding.PropertyBinder;
import nl.strohalm.cyclos.utils.conversion.CoercionHelper;
import nl.strohalm.cyclos.utils.conversion.HtmlConverter;
import nl.strohalm.cyclos.utils.conversion.ReferenceConverter;
import nl.strohalm.cyclos.utils.validation.InvalidError;
import nl.strohalm.cyclos.utils.validation.ValidationException;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;
import nl.strohalm.cyclos.webservices.rest.GenericResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class ConfirmLoanController extends BaseRestController {

    private static class ConfirmLoanParameters {

        private Long loanGroup;
        private Long member;
        private Long transferType;
        private BigDecimal amount;
        private String repaymemtDate;
        private String description;
        private int paymentCount;
        private String firstRepaymentDate;

        public Long getLoanGroup() {
            return loanGroup;
        }

        public void setLoanGroup(Long loanGroup) {
            this.loanGroup = loanGroup;
        }

        public Long getMember() {
            return member;
        }

        public void setMember(Long member) {
            this.member = member;
        }

        public Long getTransferType() {
            return transferType;
        }

        public void setTransferType(Long transferType) {
            this.transferType = transferType;
        }

        public BigDecimal getAmount() {
            return amount;
        }

        public void setAmount(BigDecimal amount) {
            this.amount = amount;
        }

        public String getRepaymemtDate() {
            return repaymemtDate;
        }

        public void setRepaymemtDate(String repaymemtDate) {
            this.repaymemtDate = repaymemtDate;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public int getPaymentCount() {
            return paymentCount;
        }

        public void setPaymentCount(int paymentCount) {
            this.paymentCount = paymentCount;
        }

        public String getFirstRepaymentDate() {
            return firstRepaymentDate;
        }

        public void setFirstRepaymentDate(String firstRepaymentDate) {
            this.firstRepaymentDate = firstRepaymentDate;
        }

    }

    @RequestMapping(value = "admin/confirmLoan", method = RequestMethod.POST)
    @ResponseBody
    protected GenericResponse handleSubmit(@RequestBody ConfirmLoanParameters params) {
        GenericResponse response = new GenericResponse();
        final GrantLoanDTO dto = resolveDTO(params);

        // Check for the transaction password, if needed
//        if (LoggedUser.isTransactionPasswordEnabled()) {
//            context.checkTransactionPassword(form.getTransactionPassword());
//        }

        // Check which method we need to use to grant the loan
        Loan loan=null;
        try {
            loan = loanService.grant(dto);
        } catch (final CreditsException e) {
             response.setMessage(" "+e);
        } catch (final AuthorizedPaymentInPastException e) {
            response.setMessage("payment.error.authorizedInPast");
        }
        final boolean pending = loan.getTransfer().getProcessDate() == null;
        response.setMessage("loan payment successfull!");
        response.setStatus(0);
        return response;
    }

    private GrantLoanDTO resolveDTO(final ConfirmLoanParameters params) {
        final LocalSettings settings = settingsService.getLocalSettings();
        Map<String, Object> query = new HashMap();
        query.put("member", (Member) elementService.load(params.getMember(), Element.Relationships.USER));
        query.put("description", params.getDescription());
        query.put("amount", params.getAmount());
        query.put("loanGroup", loanGroupService.load(params.getLoanGroup(), LoanGroup.Relationships.MEMBERS));
        query.put("transferType", transferTypeService.load(params.getTransferType(), TransferType.Relationships.FROM));
        query.put("date", settings.getDateConverter().valueOf(params.getRepaymemtDate()));

        final long transferTypeId = CoercionHelper.coerce(Long.TYPE, params.getTransferType());
        if (transferTypeId <= 0L) {
            throw new ValidationException();
        }
        final TransferType transferType = transferTypeService.load(transferTypeId, RelationshipHelper.nested(TransferType.Relationships.TO, AccountType.Relationships.CURRENCY));
        final LoanParameters loanParameters = transferType.getLoan();
        if (loanParameters == null || loanParameters.getType() == null) {
            throw new ValidationException("transferType", "transfer.type", new InvalidError());
        }
        final GrantLoanDTO dto = getDataBinder(loanParameters.getType()).readFromString(query);
        dto.setTransferType(transferType);
        if (dto.getLoanGroup() != null && !permissionService.hasPermission(AdminMemberPermission.LOAN_GROUPS_VIEW)) {
            throw new PermissionDeniedException();
        }
        return dto;
    }
    
       private DataBinder<GrantLoanDTO> getDataBinder(final Loan.Type type) {
        final LocalSettings settings = settingsService.getLocalSettings();

        final BeanBinder<PaymentCustomFieldValue> customValueBinder = BeanBinder.instance(PaymentCustomFieldValue.class);
        customValueBinder.registerBinder("field", PropertyBinder.instance(PaymentCustomField.class, "field", ReferenceConverter.instance(PaymentCustomField.class)));
        customValueBinder.registerBinder("value", PropertyBinder.instance(String.class, "value", HtmlConverter.instance()));

        final BeanBinder binder = new BeanBinder();
        binder.registerBinder("member", PropertyBinder.instance(Member.class, "member", ReferenceConverter.instance(Member.class)));
        binder.registerBinder("loanGroup", PropertyBinder.instance(LoanGroup.class, "loanGroup", ReferenceConverter.instance(LoanGroup.class)));
        binder.registerBinder("amount", PropertyBinder.instance(BigDecimal.class, "amount", settings.getNumberConverter()));
        binder.registerBinder("date", PropertyBinder.instance(Calendar.class, "date", settings.getRawDateConverter()));
        binder.registerBinder("description", PropertyBinder.instance(String.class, "description"));
        binder.registerBinder("transferType", PropertyBinder.instance(TransferType.class, "transferType", ReferenceConverter.instance(TransferType.class)));
        binder.registerBinder("customValues", BeanCollectionBinder.instance(customValueBinder, "customValues"));

        switch (type) {
            case SINGLE_PAYMENT:
                binder.setType(GrantSinglePaymentLoanDTO.class);
                binder.registerBinder("repaymentDate", PropertyBinder.instance(Calendar.class, "repaymentDate", settings.getRawDateConverter()));
                break;
            case MULTI_PAYMENT:
                binder.setType(GrantMultiPaymentLoanDTO.class);
                final BeanBinder<LoanPayment> paymentBinder = BeanBinder.instance(LoanPayment.class);
                paymentBinder.registerBinder("expirationDate", PropertyBinder.instance(Calendar.class, "expirationDate", settings.getRawDateConverter()));
                paymentBinder.registerBinder("amount", PropertyBinder.instance(BigDecimal.class, "amount", settings.getNumberConverter()));
                binder.registerBinder("payments", BeanCollectionBinder.instance(paymentBinder, "payments"));
                break;
            case WITH_INTEREST:
                binder.setType(GrantLoanWithInterestDTO.class);
                binder.registerBinder("firstRepaymentDate", PropertyBinder.instance(Calendar.class, "firstRepaymentDate", settings.getRawDateConverter()));
                binder.registerBinder("paymentCount", PropertyBinder.instance(Integer.TYPE, "paymentCount"));
                break;
            default:
                throw new IllegalArgumentException("Invalid loan type: " + type);
        }
        return binder;
    }

}
