package nl.strohalm.cyclos.webservices.rest.webshop;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import nl.strohalm.cyclos.annotations.Inject;
import nl.strohalm.cyclos.controls.ActionContext;
import nl.strohalm.cyclos.controls.restapi.BaseRestController;
import nl.strohalm.cyclos.controls.webshop.ConfirmWebShopPaymentForm;
import nl.strohalm.cyclos.controls.webshop.WebShopHelper;
import nl.strohalm.cyclos.entities.access.Channel;
import nl.strohalm.cyclos.entities.access.Channel.Credentials;
import nl.strohalm.cyclos.entities.accounts.AccountOwner;
import nl.strohalm.cyclos.entities.accounts.Currency;
import nl.strohalm.cyclos.entities.accounts.transactions.Payment;
import nl.strohalm.cyclos.entities.accounts.transactions.Ticket;
import nl.strohalm.cyclos.entities.accounts.transactions.TransferType;
import nl.strohalm.cyclos.entities.accounts.transactions.WebShopTicket;
import nl.strohalm.cyclos.entities.customization.fields.PaymentCustomField;
import nl.strohalm.cyclos.entities.customization.fields.PaymentCustomFieldValue;
import nl.strohalm.cyclos.entities.exceptions.UnexpectedEntityException;
import nl.strohalm.cyclos.entities.members.Element;
import nl.strohalm.cyclos.entities.members.Member;
import nl.strohalm.cyclos.services.access.AccessService;
import nl.strohalm.cyclos.services.access.ChannelService;
import nl.strohalm.cyclos.services.access.exceptions.BlockedCredentialsException;
import nl.strohalm.cyclos.services.access.exceptions.InvalidCredentialsException;
import nl.strohalm.cyclos.services.elements.ElementService;
import nl.strohalm.cyclos.services.transactions.DoPaymentDTO;
import nl.strohalm.cyclos.services.transactions.PaymentService;
import nl.strohalm.cyclos.services.transactions.ScheduledPaymentDTO;
import nl.strohalm.cyclos.services.transactions.TicketService;
import nl.strohalm.cyclos.services.transactions.TransactionContext;
import nl.strohalm.cyclos.services.transactions.exceptions.CreditsException;
import nl.strohalm.cyclos.services.transfertypes.TransactionFeePreviewDTO;
import nl.strohalm.cyclos.services.transfertypes.TransactionFeeService;
import nl.strohalm.cyclos.services.transfertypes.TransferTypeService;
import nl.strohalm.cyclos.utils.ActionHelper;
import nl.strohalm.cyclos.utils.MessageHelper;
import nl.strohalm.cyclos.utils.access.LoggedUser;
import nl.strohalm.cyclos.utils.validation.RequiredError;
import nl.strohalm.cyclos.utils.validation.ValidationException;
@Controller
public class ConfirmWebShopPaymentController extends BaseRestController{
	public static class ShouldValidateTPParameter {
        public final ActionMapping       mapping;
        public final ActionForm          actionForm;
        public final HttpServletRequest  request;
        public final HttpServletResponse response;
        public final DoPaymentDTO        payment;
        public final ChannelService      channelService;
        public final ElementService      elementService;
        public final TransferTypeService transferTypeService;
        public final MessageHelper       messageHelper;
        

        public ShouldValidateTPParameter(final ActionMapping mapping, final ActionForm actionForm, final HttpServletRequest request, final HttpServletResponse response, final DoPaymentDTO payment, final ChannelService channelService, final ElementService elementService, final TransferTypeService transferTypeService, final MessageHelper messageHelper) {
            this.mapping = mapping;
            this.actionForm = actionForm;
            this.request = request;
            this.response = response;
            this.payment = payment;
            this.channelService = channelService;
            this.elementService = elementService;
            this.transferTypeService = transferTypeService;
            this.messageHelper = messageHelper;
        }
    }

    private static boolean doShouldValidateTransactionPassword(final ShouldValidateTPParameter parameter) {
        final Channel channel = parameter.channelService.loadByInternalName(Channel.WEBSHOP);
        // Transaction password is only validated on default credentials
        if (channel.getCredentials() != Credentials.DEFAULT) {
            return false;
        }
        final Member member = parameter.elementService.load(((Member) parameter.payment.getFrom()).getId(), Element.Relationships.USER);
        final ActionContext context = new ActionContext(parameter.mapping, parameter.actionForm, parameter.request, parameter.response, member.getUser(), parameter.messageHelper);
        final TransferType transferType = parameter.transferTypeService.load(parameter.payment.getTransferType().getId(), TransferType.Relationships.FROM);
        return context.isTransactionPasswordEnabled(transferType.getFrom());
    }

    static boolean shouldValidateTransactionPassword(final ShouldValidateTPParameter parameter) {
        return LoggedUser.runAsSystem(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return doShouldValidateTransactionPassword(parameter);
            }
        });
    }

    private AccessService         accessService;
    private ChannelService        channelService;
    private PaymentService        paymentService;
    private TransactionFeeService transactionFeeService;
    private TransferTypeService   transferTypeService;

    private TicketService         ticketService;
    private ElementService elementService;
    private ActionHelper actionHelper;
    private MessageHelper messageHelper;

    @Inject
    public void setAccessService(final AccessService accessService) {
        this.accessService = accessService;
    }

    @Inject
    public void setChannelService(final ChannelService channelService) {
        this.channelService = channelService;
    }

    @Inject
    public void setPaymentService(final PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @Inject
    public void setTicketService(final TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @Inject
    public void setTransactionFeeService(final TransactionFeeService transactionFeeService) {
        this.transactionFeeService = transactionFeeService;
    }

    @Inject
    public void setTransferTypeService(final TransferTypeService transferTypeService) {
        this.transferTypeService = transferTypeService;
    }
    public static class ConfirmWebShopPaymentRequestDTO{
    	private String            transactionPassword;
    	 private AccountOwner                           from;
    	    private AccountOwner                        to;
    	    private Calendar                            date;
    	    private TransferType                        transferType;
    	    private BigDecimal                          amount;
    	    private Currency                            currency;
    	    private TransactionContext                  context;
    	    private String                              channel          = Channel.WEB;
    	    private String                              description;
    	    private Ticket                              ticket;
    	    private Element                             receiver;
    	    private List<ScheduledPaymentDTO>           payments;
    	    private Collection<PaymentCustomFieldValue> customValues;
    	    private String                              traceNumber;
    	    private boolean                             showScheduledToReceiver;

    	    /**
    	     * @see #getTraceData()
    	     */
    	    private String                              traceData;

    	    public BigDecimal getAmount() {
    	        return amount;
    	    }

    	    public String getChannel() {
    	        return channel;
    	    }

    	    public TransactionContext getContext() {
    	        return context;
    	    }

    	    public Currency getCurrency() {
    	        return currency;
    	    }

    	    //@Override
    	    public Class<PaymentCustomField> getCustomFieldClass() {
    	        return PaymentCustomField.class;
    	    }

    	    //@Override
    	    public Class<PaymentCustomFieldValue> getCustomFieldValueClass() {
    	        return PaymentCustomFieldValue.class;
    	    }

    	    //@Override
    	    public Collection<PaymentCustomFieldValue> getCustomValues() {
    	        return customValues;
    	    }

    	    public Calendar getDate() {
    	        return date;
    	    }

    	    public String getDescription() {
    	        return description;
    	    }

    	    public AccountOwner getFrom() {
    	        return from;
    	    }

    	    public List<ScheduledPaymentDTO> getPayments() {
    	        return payments;
    	    }

    	    public Element getReceiver() {
    	        return receiver;
    	    }

    	    public Ticket getTicket() {
    	        return ticket;
    	    }

    	    public AccountOwner getTo() {
    	        return to;
    	    }

    	    /**
    	     * Optional.
    	     * @returns the data set by the client making a payment that will be attached to the transfer and sent back when a notification related to this
    	     * payment is issued by Cyclos (likely to the same client).<br>
    	     * It depends on the client side then there is no guarantee of uniqueness between different clients.<br>
    	     * Note: <b>It has nothing to do with the traceNumber field in the parent class (used to tag transactions and query by these value).</b>
    	     */
    	    public String getTraceData() {
    	        return traceData;
    	    }

    	    public String getTraceNumber() {
    	        return traceNumber;
    	    }

    	    public TransferType getTransferType() {
    	        return transferType;
    	    }

    	    public boolean isShowScheduledToReceiver() {
    	        return showScheduledToReceiver;
    	    }

    	    public void setAmount(final BigDecimal amount) {
    	        this.amount = amount;
    	    }

    	    public void setChannel(final String channel) {
    	        this.channel = channel;
    	    }

    	    public void setContext(final TransactionContext context) {
    	        this.context = context;
    	    }

    	    public void setCurrency(final Currency currency) {
    	        this.currency = currency;
    	    }

    	    public void setCustomValues(final Collection<PaymentCustomFieldValue> customValues) {
    	        this.customValues = customValues;
    	    }

    	    public void setDate(final Calendar date) {
    	        this.date = date;
    	    }

    	    public void setDescription(final String description) {
    	        this.description = description;
    	    }

    	    public void setFrom(final AccountOwner from) {
    	        this.from = from;
    	    }

    	    public void setPayments(final List<ScheduledPaymentDTO> payments) {
    	        this.payments = payments;
    	    }

    	    public void setReceiver(final Element receiver) {
    	        this.receiver = receiver;
    	    }

    	    public void setShowScheduledToReceiver(final boolean showScheduledToReceiver) {
    	        this.showScheduledToReceiver = showScheduledToReceiver;
    	    }

    	    public void setTicket(final Ticket ticket) {
    	        this.ticket = ticket;
    	    }

    	    public void setTo(final AccountOwner to) {
    	        this.to = to;
    	    }

    	    public void setTraceData(final String traceData) {
    	        this.traceData = traceData;
    	    }

    	    public void setTraceNumber(final String traceNumber) {
    	        this.traceNumber = traceNumber;
    	    }

    	    public void setTransferType(final TransferType transferType) {
    	        this.transferType = transferType;
    	    }


        public String getTransactionPassword() {
            return transactionPassword;
        }

        public void setTransactionPassword(final String transactionPassword) {
            this.transactionPassword = transactionPassword;
        }
    }
    public static class ConfirmWebShopPaymentResponseDTO{
    	
    }

    @RequestMapping(value = "",method = RequestMethod.GET)
    @ResponseBody
    protected ActionForward handleSubmit(@RequestBody ConfirmWebShopPaymentRequestDTO form) {
        final ConfirmWebShopPaymentForm request = (ConfirmWebShopPaymentForm) request;
        final DoPaymentDTO paymentDTO = resolvePayment((HttpServletRequest) request);
        final Member from = (Member) paymentDTO.getFrom();

        final HttpSession session = ((HttpServletRequest) request).getSession();
        session.setAttribute("errorReturnTo", "/do/webshop/confirmPayment");
        // We must fool the model layer, pretending that there is a logged user
        return LoggedUser.runAs(from.getUser(), request.getRemoteAddr(), new Callable<ActionForward>() {
            @Override
            public ActionForward call() throws Exception {
                try {
                    // Check for transaction password
                    if (shouldValidateTransactionPassword(new ShouldValidateTPParameter(mapping, request, request, response, paymentDTO, channelService, elementService, transferTypeService, messageHelper))) {
                        accessService.checkTransactionPassword(form.getTransactionPassword());
                    }
                } catch (final InvalidCredentialsException e) {
                    throw new ValidationException("transactionPassword.error.invalid");
                } catch (final BlockedCredentialsException e) {
                    cancelTicket(request, paymentDTO);
                    throw new ValidationException("transactionPassword.error.blockedByTrials");
                }

                // Perform the actual payment
                Payment payment;
                try {
                    payment = paymentService.doPayment(paymentDTO);

                    // Store the payment on the session
                    WebShopHelper.setPerformedPayment(session, payment);

                    return mapping.findForward("success");
                } catch (final CreditsException e) {
                    cancelTicket(request, paymentDTO);
                    throw new ValidationException(actionHelper.resolveErrorKey(e), actionHelper.resolveParameters(e));
                } catch (final UnexpectedEntityException e) {
                    cancelTicket(request, paymentDTO);
                    throw new ValidationException("payment.error.invalidTransferType");
                }
            }
        });
    }

    //@Override
    protected void prepareForm(final ActionMapping mapping, final ActionForm actionForm, final HttpServletRequest request, final HttpServletResponse response) {
        final DoPaymentDTO payment = resolvePayment(request);
        request.setAttribute("payment", payment);
        request.setAttribute("requestTransactionPassword", shouldValidateTransactionPassword(new ShouldValidateTPParameter(mapping, actionForm, request, response, payment, channelService, elementService, transferTypeService, messageHelper)));

        TransactionFeePreviewDTO fees;
        fees = LoggedUser.runAsSystem(new Callable<TransactionFeePreviewDTO>() {
            @Override
            public TransactionFeePreviewDTO call() throws Exception {
                return transactionFeeService.preview(payment.getFrom(), payment.getTo(), payment.getTransferType(), payment.getAmount());
            }
        });
        request.setAttribute("finalAmount", fees.getFinalAmount());
        request.setAttribute("fees", fees.getFees());
    }

   // @Override
    protected void validateForm(final ActionMapping mapping, final ActionForm actionForm, final HttpServletRequest request, final HttpServletResponse response) throws ValidationException {
        final DoPaymentDTO payment = resolvePayment(request);
        if (shouldValidateTransactionPassword(new ShouldValidateTPParameter(mapping, actionForm, request, response, payment, channelService, elementService, transferTypeService, messageHelper))) {
            final ConfirmWebShopPaymentForm form = (ConfirmWebShopPaymentForm) actionForm;
            if (StringUtils.isEmpty(form.getTransactionPassword())) {
                throw new ValidationException("_transactionPassword", "login.transactionPassword", new RequiredError());
            }
        }
    }

    private void cancelTicket(final ConfirmWebShopPaymentForm request, final DoPaymentDTO payment) {
        final WebShopTicket ticket = ticketService.cancelWebShopTicket(payment.getTicket().getId(), request.getRemoteAddr());

        final HttpSession session = request.getSession();
        session.removeAttribute("forceBack");
        session.setAttribute("errorReturnTo", ticket.getReturnUrl());
    }

    private DoPaymentDTO resolvePayment(final HttpServletRequest request) {
        final DoPaymentDTO payment = WebShopHelper.getUpdatedPayment(request.getSession());
        if (payment == null) {
            throw new ValidationException();
        }
        if (StringUtils.isEmpty(payment.getDescription())) {
            payment.setDescription(payment.getTransferType().getDescription());
        }
        return payment;
    }

}
