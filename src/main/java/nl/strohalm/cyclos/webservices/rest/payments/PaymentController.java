package nl.strohalm.cyclos.webservices.rest.payments;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import nl.strohalm.cyclos.controls.ActionContext;
import nl.strohalm.cyclos.controls.payments.PaymentForm;
import nl.strohalm.cyclos.entities.accounts.AccountOwner;
import nl.strohalm.cyclos.entities.members.Element;
import nl.strohalm.cyclos.entities.members.Member;
import nl.strohalm.cyclos.services.elements.ElementService;
import nl.strohalm.cyclos.services.groups.GroupService;
import nl.strohalm.cyclos.services.permissions.PermissionService;
import nl.strohalm.cyclos.services.settings.SettingsService;
import nl.strohalm.cyclos.utils.binding.MapBean;
import nl.strohalm.cyclos.utils.conversion.IdConverter;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;

@Controller
public class PaymentController extends BaseRestController {
	private GroupService groupService;
	private ElementService elementService;
	private PermissionService permissionService;
	private SettingsService settingsService;

	protected AccountOwner getFromOwner(final ActionContext context) {
		final PaymentForm form = context.getForm();
		final Long fromId = IdConverter.instance().valueOf(form.getFrom());
		if (fromId == null) {
			return context.getAccountOwner();
		}
		final Element element = elementService.load(fromId,
				Element.Relationships.GROUP);
		if (element instanceof Member) {
			return (Member) element;
		}
		return null;
	}

	public static class PaymentRequestDto {
		private boolean selectMember;
		private String to;
		private boolean toSystem;
		private Object payments = new MapBean(true, "date", "amount");
		private String from;

		public Object getPayments() {
			return payments;
		}

		public String getTo() {
			return to;
		}

		public boolean isSelectMember() {
			return selectMember;
		}

		public boolean isToSystem() {
			return toSystem;
		}

		public void setPayments(final Object payments) {
			this.payments = payments;
		}

		public void setSelectMember(final boolean selectMember) {
			this.selectMember = selectMember;
		}

		public void setTo(final String to) {
			this.to = to;
		}

		public void setToSystem(final boolean toSystem) {
			this.toSystem = toSystem;
		}

		private String amount;
		private String description;
		private String type;
		private String date;
		private String currency;

		private MapBean customValues = new MapBean(true, "field", "value");

		public String getAmount() {
			return amount;
		}

		public String getCurrency() {
			return currency;
		}

		public MapBean getCustomValues() {
			return customValues;
		}

		public String getDate() {
			return date;
		}

		public String getDescription() {
			return description;
		}

		public String getFrom() {
			return from;
		}

		public String getType() {
			return type;
		}

		public void setAmount(final String amount) {
			this.amount = amount;
		}

		public void setCurrency(final String currency) {
			this.currency = currency;
		}

		public void setCustomValues(final MapBean customValues) {
			this.customValues = customValues;
		}

		public void setDate(final String date) {
			this.date = date;
		}

		public void setDescription(final String description) {
			this.description = description;
		}

		public void setFrom(final String from) {
			this.from = from;
		}

		public void setType(final String type) {
			this.type = type;
		}
	}

	public static class PaymentResponseDto {
		private Map<String, Object> params;

		public PaymentResponseDto(Map<String, Object> params) {
			super();
			this.params = params;
		}

	}

	@RequestMapping(value = "/admin/managePasswords", method = RequestMethod.POST)
	@ResponseBody
	protected PaymentResponseDto handleSubmit(
			@RequestBody PaymentRequestDto form) throws Exception {
		// final PaymentForm form = context.getForm();
		final Map<String, Object> params = new HashMap<String, Object>();
		params.put("selectMember", form.isSelectMember());
		params.put("toSystem", form.isToSystem());
		params.put("from", form.getFrom());
		PaymentResponseDto response = new PaymentResponseDto(params);
		return response;
	}

}
