package nl.strohalm.cyclos.webservices.rest.members.printsettings;

import javax.servlet.ServletRequest;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import nl.strohalm.cyclos.annotations.Inject;
import nl.strohalm.cyclos.controls.restapi.BaseRestController;
import nl.strohalm.cyclos.services.preferences.ReceiptPrinterSettingsService;
import nl.strohalm.cyclos.utils.RequestHelper;
@Controller
public class RemoveReceiptPrinterSettingsController extends BaseRestController{
	private ReceiptPrinterSettingsService receiptPrinterSettingsService;

    @Inject
    public void setReceiptPrinterSettingsService(final ReceiptPrinterSettingsService receiptPrinterSettingsService) {
        this.receiptPrinterSettingsService = receiptPrinterSettingsService;
    }
    public static class RemoveReceiptPrinterSettingsRequestDTO{
    	public static String getCookieValue(final ServletRequest servletRequest, final String name) {
            final Cookie cookie = getCookie(servletRequest, name);
            return cookie == null ? null : cookie.getValue();
        }
    	private static Cookie getCookie(ServletRequest servletRequest, String name) {
			// TODO Auto-generated method stub
			return null;
		}
		private Long              id;

        public Long getId() {
            return id;
        }

        public void setId(final Long id) {
            this.id = id;
        }
    }
    public static class RemoveReceiptPrinterSettingsResponseDTO{
    	String message;

		public final String getMessage() {
			return message;
		}

		public final void setMessage(String message) {
			this.message = message;
		}

		public void addCookie(Cookie cookie) {
			// TODO Auto-generated method stub
			
		}
    	
    	
    }

    @RequestMapping(value = "/member/removeReceiptPrinterSettings", method = RequestMethod.DELETE)
    @ResponseBody
    protected RemoveReceiptPrinterSettingsResponseDTO executeAction(@RequestBody RemoveReceiptPrinterSettingsRequestDTO  form) throws Exception {
       
        final Long id = form.getId();
        receiptPrinterSettingsService.remove(id);
        RemoveReceiptPrinterSettingsResponseDTO response = new RemoveReceiptPrinterSettingsResponseDTO();
        response.setMessage("receiptPrinterSettings.removed");
        final String currentReceiptPrinter = RequestHelper.getCookieValue((ServletRequest) form, "receiptPrinterId");
        // If the member removes a printer he's currently using, clear the cookie. We cannot, however, do this for cookies on other clients.
        // They'll get an error when trying to print
        if (StringUtils.isNotEmpty(currentReceiptPrinter) && id.toString().equals(currentReceiptPrinter)) {
           // final HttpServletResponse response = context.getResponse();
            final Cookie cookie = new Cookie("receiptPrinterId", "");
            cookie.setPath(((HttpServletRequest) form).getContextPath());
            response.addCookie(cookie);
        }
        return response;
    }


}
