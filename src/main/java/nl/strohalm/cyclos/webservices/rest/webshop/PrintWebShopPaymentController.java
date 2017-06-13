package nl.strohalm.cyclos.webservices.rest.webshop;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import nl.strohalm.cyclos.controls.restapi.BaseRestController;
import nl.strohalm.cyclos.entities.accounts.transactions.Payment;
import nl.strohalm.cyclos.entities.accounts.transactions.Transfer;
import nl.strohalm.cyclos.entities.accounts.transactions.WebShopTicket;
import nl.strohalm.cyclos.services.transactions.DoPaymentDTO;
import nl.strohalm.cyclos.utils.validation.ValidationException;
import nl.strohalm.cyclos.webservices.rest.webshop.PrintWebShopPaymentController.PrintWebShopPaymentRequestDTO;
@Controller
public class PrintWebShopPaymentController extends BaseRestController{
	
	public static class PrintWebShopPaymentRequestDTO{
		private static final String PERFORMED_PAYMENT_KEY = "webshopPerformedPayment";
	    private static final String NEW_PAYMENT_KEY       = "webshopNewPayment";
	    private static final String UPDATED_PAYMENT_KEY   = "webshopUpdatedPayment";

	    /**
	     * Returns the new payment DTO
	     */
	    public static DoPaymentDTO getNewPayment(final HttpSession session) {
	        return (DoPaymentDTO) session.getAttribute(NEW_PAYMENT_KEY);
	    }

	    /**
	     * Sets the performed payment
	     */
	    public static Payment getPerformedPayment(final HttpSession session) {
	        return (Payment) session.getAttribute(PERFORMED_PAYMENT_KEY);
	    }

	    /**
	     * Returns the payment DTO, updated with context data
	     */
	    public static DoPaymentDTO getUpdatedPayment(final HttpSession session) {
	        return (DoPaymentDTO) session.getAttribute(UPDATED_PAYMENT_KEY);
	    }

	    /**
	     * Returns a forward to the ticket's return url
	     */
	    public static ActionForward returnForward(final WebShopTicket ticket) {
	        final String url = ticket.getReturnUrl();
	        final String separator = url.contains("?") ? "&" : "?";
	        final String fullUrl = url + separator + "ticket=" + ticket.getTicket();
	        return new ActionForward(fullUrl, true);
	    }

	    /**
	     * Sets the new payment DTO
	     */
	    public static void setNewPayment(final HttpSession session, final DoPaymentDTO payment) {
	        session.setAttribute(NEW_PAYMENT_KEY, payment);
	    }

	    /**
	     * Sets the performed payment
	     */
	    public static void setPerformedPayment(final HttpSession session, final Payment payment) {
	        session.setAttribute(PERFORMED_PAYMENT_KEY, payment);
	    }

	    /**
	     * Sets the payment DTO, updated with context data
	     */
	    public static void setUpdatedPayment(final HttpSession session, final DoPaymentDTO payment) {
	        session.setAttribute(UPDATED_PAYMENT_KEY, payment);
	    }
	}
	
	
	public static class PrintWebShopPaymentResponseDTO{
		WebShopTicket ticket;

		public final WebShopTicket getTicket() {
			return ticket;
		}

		public final void setTicket(WebShopTicket ticket) {
			this.ticket = ticket;
		}
	
	
	
	
	@RequestMapping(value ="",method = RequestMethod.GET)
	@ResponseBody
    protected PrintWebShopPaymentResponseDTO executeAction (@RequestBody PrintWebShopPaymentRequestDTO form) throws Exception {
        final Payment payment = WebShopHelper.getPerformedPayment(request.getSession());
        if (!(payment instanceof Transfer)) {
            throw new ValidationException();
        }
        request.setAttribute("transfer", payment);

        // Store the return path
        final WebShopTicket ticket = (WebShopTicket) WebShopHelper.getUpdatedPayment(request.getSession()).getTicket();
        final String returnUrl = WebShopHelper.returnForward(ticket).getPath();
        request.setAttribute("returnUrl", returnUrl);

        return mapping.getInputForward();
    }

	}
	
}

