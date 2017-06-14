package nl.strohalm.cyclos.webservices.rest.members.printsettings;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import nl.strohalm.cyclos.annotations.Inject;
import nl.strohalm.cyclos.controls.ActionContext;
import nl.strohalm.cyclos.controls.members.printsettings.EditReceiptPrinterSettingsForm;
import nl.strohalm.cyclos.entities.members.Member;
import nl.strohalm.cyclos.entities.members.printsettings.ReceiptPrinterSettings;
import nl.strohalm.cyclos.services.preferences.ReceiptPrinterSettingsService;
import nl.strohalm.cyclos.utils.binding.BeanBinder;
import nl.strohalm.cyclos.utils.binding.DataBinder;
import nl.strohalm.cyclos.utils.binding.PropertyBinder;
import nl.strohalm.cyclos.utils.conversion.IdConverter;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;
@Controller
public class EditReceiptPrinterSettingsController extends BaseRestController{

	    private ReceiptPrinterSettingsService      receiptPrinterSettingsService;
	    private DataBinder<ReceiptPrinterSettings> dataBinder;

	    @Inject
	    public void setReceiptPrinterSettingsService(final ReceiptPrinterSettingsService receiptPrinterSettingsService) {
	        this.receiptPrinterSettingsService = receiptPrinterSettingsService;
	    }
	    public static class EditReceiptPrinterSettingsRequestDTO{
	    	
	    	 private Member            member;
	    	    private String            name;
	    	    private String            printerName;
	    	    private String            beginOfDocCommand;
	    	    private String            endOfDocCommand;
	    	    private String            paymentAdditionalMessage;

	    	    public String getBeginOfDocCommand() {
	    	        return beginOfDocCommand;
	    	    }

	    	    public String getEndOfDocCommand() {
	    	        return endOfDocCommand;
	    	    }

	    	    public Member getMember() {
	    	        return member;
	    	    }

	    	    public String getName() {
	    	        return name;
	    	    }

	    	    public String getPaymentAdditionalMessage() {
	    	        return paymentAdditionalMessage;
	    	    }

	    	    public String getPrinterName() {
	    	        return printerName;
	    	    }

	    	    public void setBeginOfDocCommand(final String beginOfDocCommand) {
	    	        this.beginOfDocCommand = beginOfDocCommand;
	    	    }

	    	    public void setEndOfDocCommand(final String endOfDocCommand) {
	    	        this.endOfDocCommand = endOfDocCommand;
	    	    }

	    	    public void setMember(final Member member) {
	    	        this.member = member;
	    	    }

	    	    public void setName(final String name) {
	    	        this.name = name;
	    	    }

	    	    public void setPaymentAdditionalMessage(final String paymentAdditionalMessage) {
	    	        this.paymentAdditionalMessage = paymentAdditionalMessage;
	    	    }

	    	    public void setPrinterName(final String printerName) {
	    	        this.printerName = printerName;
	    	    }

	    	    @Override
	    	    public String toString() {
	    	        return getId() + " " + name + ", printer: " + printerName + ", member: " + member;
	    	    }

				private String getId() {
					// TODO Auto-generated method stub
					return null;
				}
	    }
	    public static class EditReceiptPrinterSettingsResponseDTO{
	    	String message;

			public final String getMessage() {
				return message;
			}

			public final void setMessage(String message) {
				this.message = message;
			}
	    }

	    @RequestMapping(value = "/member/editReceiptPrinterSettings", method = RequestMethod.POST)
	    @ResponseBody
	    protected EditReceiptPrinterSettingsResponseDTO handleSubmit(@RequestBody ActionContext form) throws Exception {
	        final ReceiptPrinterSettings receiptPrinterSettings = read(form);
	        final boolean isInsert = receiptPrinterSettings.isTransient();
	        receiptPrinterSettingsService.save(receiptPrinterSettings);
	        EditReceiptPrinterSettingsResponseDTO response = new EditReceiptPrinterSettingsResponseDTO();
	        if (isInsert) {
	            response.setMessage("receiptPrinterSettings.created");
	        } else {
	            response.setMessage("receiptPrinterSettings.modified");
	        }
	        return response;
	    }

	   // @Override
	    protected void prepareForm(final ActionContext context) throws Exception {
	        final EditReceiptPrinterSettingsForm form = context.getForm();
	        final Long id = form.getId();
	        ReceiptPrinterSettings receiptPrinterSettings;
	        if (id == null) {
	            receiptPrinterSettings = new ReceiptPrinterSettings();
	        } else {
	            receiptPrinterSettings = receiptPrinterSettingsService.load(id);
	        }
	        getDataBinder().writeAsString(form.getReceiptPrinterSettings(), receiptPrinterSettings);
	        final HttpServletRequest request = context.getRequest();
	        request.setAttribute("receiptPrinterSettings", receiptPrinterSettings);
	        request.setAttribute("editable", context.isMember());
	    }

	    //@Override
	    protected void validateForm(final ActionContext context) {
	        final ReceiptPrinterSettings receiptPrinterSettings = read(context);
	        receiptPrinterSettingsService.validate(receiptPrinterSettings);
	    }

	    private DataBinder<ReceiptPrinterSettings> getDataBinder() {
	        if (dataBinder == null) {
	            final BeanBinder<ReceiptPrinterSettings> binder = BeanBinder.instance(ReceiptPrinterSettings.class);
	            binder.registerBinder("id", PropertyBinder.instance(Long.class, "id", IdConverter.instance()));
	            binder.registerBinder("name", PropertyBinder.instance(String.class, "name"));
	            binder.registerBinder("printerName", PropertyBinder.instance(String.class, "printerName"));
	            binder.registerBinder("beginOfDocCommand", PropertyBinder.instance(String.class, "beginOfDocCommand"));
	            binder.registerBinder("endOfDocCommand", PropertyBinder.instance(String.class, "endOfDocCommand"));
	            binder.registerBinder("paymentAdditionalMessage", PropertyBinder.instance(String.class, "paymentAdditionalMessage"));
	            dataBinder = binder;
	        }
	        return dataBinder;
	    }

	    private ReceiptPrinterSettings read(final ActionContext context) {
	        final EditReceiptPrinterSettingsForm form = context.getForm();
	        final ReceiptPrinterSettings receiptPrinterSettings = getDataBinder().readFromString(form.getReceiptPrinterSettings());
	        receiptPrinterSettings.setMember(context.getMember());
	        return receiptPrinterSettings;
	    }


}

