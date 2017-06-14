package nl.strohalm.cyclos.webservices.rest.webshop;

import nl.strohalm.cyclos.webservices.rest.BaseRestController;

public class NewWebShopPaymentController extends BaseRestController{
/*	private TicketService ticketService;
	
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
*/
}
