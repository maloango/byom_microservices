package nl.strohalm.cyclos.webservices.rest.webshop;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import nl.strohalm.cyclos.annotations.Inject;
import nl.strohalm.cyclos.controls.restapi.BaseRestController;
import nl.strohalm.cyclos.controls.webshop.NewWebShopPaymentForm;
import nl.strohalm.cyclos.controls.webshop.WebShopHelper;
import nl.strohalm.cyclos.entities.access.Channel;
import nl.strohalm.cyclos.entities.accounts.transactions.Ticket;
import nl.strohalm.cyclos.entities.accounts.transactions.WebShopTicket;
import nl.strohalm.cyclos.entities.exceptions.EntityNotFoundException;
import nl.strohalm.cyclos.services.transactions.DoPaymentDTO;
import nl.strohalm.cyclos.services.transactions.TicketService;
import nl.strohalm.cyclos.services.transactions.TransactionContext;
import nl.strohalm.cyclos.utils.validation.ValidationException;

public class NewWebShopPaymentController extends BaseRestController{
	private TicketService ticketService;
	
	public static class NewWebShopPaymentRequestDTO{
		private String            ticket;

	    public String getTicket() {
	        return ticket;
	    }

	    public void setTicket(final String ticket) {
	        this.ticket = ticket;
	    }
	}
	public static class NewWebShopPaymentResponseDTO{
		WebShopTicket ticket;

		public final WebShopTicket getTicket() {
			return ticket;
		}

		public final void setTicket(WebShopTicket ticket) {
			this.ticket = ticket;
		}
	}
	public static class NewWebShopPaymentRequestDTO{
		
	}
	public static class NewWebShopPaymentResponseDTO{
		
	}
    @Override
    public ActionForward executeAction() throws IOException {
        final NewWebShopPaymentForm form = (NewWebShopPaymentForm) actionForm;

        // Retrieve the ticket
        final String ticketStr = form.getTicket();
        WebShopTicket ticket;
        try {
            final Ticket loaded = ticketService.loadPendingWebShopTicket(ticketStr, request.getRemoteAddr(), Ticket.Relationships.CURRENCY, Ticket.Relationships.TO);
            ticket = (WebShopTicket) loaded;
        } catch (final EntityNotFoundException e) {
            throw new ValidationException("webshop.error.ticket");
        }

        // Build the payment DTO
        final DoPaymentDTO payment = new DoPaymentDTO();
        payment.setChannel(Channel.WEBSHOP);
        payment.setContext(TransactionContext.PAYMENT);
        payment.setTo(ticket.getTo());
        payment.setTicket(ticket);
        payment.setAmount(ticket.getAmount());
        payment.setCurrency(ticket.getCurrency());
        payment.setDescription(ticket.getDescription());

        // Store the session attributes
        final HttpSession session = request.getSession();
        WebShopHelper.setNewPayment(session, payment);
        session.setAttribute("errorReturnTo", WebShopHelper.returnForward(ticket).getPath());

        session.setAttribute("isWebShop", true);
        session.setAttribute("isPosWeb", false);

        return mapping.findForward("success");
    }

    @Inject
    public void setTicketService(final TicketService ticketService) {
        this.ticketService = ticketService;
    }

}
