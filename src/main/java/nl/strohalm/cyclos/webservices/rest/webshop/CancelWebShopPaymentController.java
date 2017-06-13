package nl.strohalm.cyclos.webservices.rest.webshop;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import nl.strohalm.cyclos.annotations.Inject;
import nl.strohalm.cyclos.controls.restapi.BaseRestController;
import nl.strohalm.cyclos.controls.webshop.WebShopHelper;
import nl.strohalm.cyclos.entities.access.Channel;
import nl.strohalm.cyclos.entities.accounts.AccountOwner;
import nl.strohalm.cyclos.entities.accounts.Currency;
import nl.strohalm.cyclos.entities.accounts.transactions.Ticket;
import nl.strohalm.cyclos.entities.accounts.transactions.TransferType;
import nl.strohalm.cyclos.entities.accounts.transactions.WebShopTicket;
import nl.strohalm.cyclos.entities.customization.fields.PaymentCustomField;
import nl.strohalm.cyclos.entities.customization.fields.PaymentCustomFieldValue;
import nl.strohalm.cyclos.entities.members.Element;
import nl.strohalm.cyclos.services.transactions.DoPaymentDTO;
import nl.strohalm.cyclos.services.transactions.ScheduledPaymentDTO;
import nl.strohalm.cyclos.services.transactions.TicketService;
import nl.strohalm.cyclos.services.transactions.TransactionContext;
import nl.strohalm.cyclos.utils.validation.ValidationException;
@Controller
public class CancelWebShopPaymentController extends BaseRestController{
	private static final String RemoteAddr = null;
	private TicketService ticketService;
	public static class CancelWebShopPaymentRequestDTO{
		private AccountOwner                        from;
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
	    private String            clientAddress;
	    private String            memberAddress;
	    private String            returnUrl;

	    public String getClientAddress() {
	        return clientAddress;
	    }

	    public String getMemberAddress() {
	        return memberAddress;
	    }

	    public String getReturnUrl() {
	        return returnUrl;
	    }

	    public void setClientAddress(final String clientAddress) {
	        this.clientAddress = clientAddress;
	    }

	    public void setMemberAddress(final String memberAddress) {
	        this.memberAddress = memberAddress;
	    }

	    public void setReturnUrl(final String returnUrl) {
	        this.returnUrl = returnUrl;
	    }


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

	    public Class<PaymentCustomField> getCustomFieldClass() {
	        return PaymentCustomField.class;
	    }

	    public Class<PaymentCustomFieldValue> getCustomFieldValueClass() {
	        return PaymentCustomFieldValue.class;
	    }

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

		
	}
	public static class CancelWebShopPaymentResponseDTO{
		 WebShopTicket ticket ;

		public final void setTicket(WebShopTicket ticket) {
			this.ticket = ticket;
		}
		 
	}

	@RequestMapping(value = "/webshop/cancel",method = RequestMethod.DELETE)
	@ResponseBody
    public CancelWebShopPaymentResponseDTO executeAction( final CancelWebShopPaymentResponseDTO form) throws Exception {
        final DoPaymentDTO payment = resolvePayment(form);
        WebShopTicket ticket = (WebShopTicket) payment.getTicket();
        CancelWebShopPaymentResponseDTO response = new CancelWebShopPaymentResponseDTO();
        try {
            ticket = ticketService.cancelWebShopTicket(ticket.getId(), RemoteAddr);
        } catch (final Exception e) {
            // Ignore
        }
        response.setTicket(ticket);
        return response;
    }

    @Inject
    public void setTicketService(final TicketService ticketService) {
        this.ticketService = ticketService;
    }

    private DoPaymentDTO resolvePayment(final CancelWebShopPaymentResponseDTO form) {
        final DoPaymentDTO payment = WebShopHelper.getNewPayment(((HttpServletRequest) form).getSession());
        if (payment == null) {
            throw new ValidationException();
        }
        return payment;
    }

}
