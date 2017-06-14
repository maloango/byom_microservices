package nl.strohalm.cyclos.webservices.rest.payments;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import nl.strohalm.cyclos.entities.access.User;
import nl.strohalm.cyclos.entities.accounts.AccountOwner;
import nl.strohalm.cyclos.entities.members.Element;
import nl.strohalm.cyclos.entities.members.Member;
import nl.strohalm.cyclos.services.elements.ElementService;
import nl.strohalm.cyclos.utils.binding.MapBean;
import nl.strohalm.cyclos.utils.conversion.IdConverter;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;

@Controller
public class SelfPaymentController extends BaseRestController {
	private ElementService elementService;

	public static class SelfPaymentRequestDto {
		private String amount;
		private String description;
		private String type;
		private String date;
		private String currency;
		private String from;
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

		public AccountOwner getAccountOwner() {
			try {
				final Element element = getElement();
				return element.getAccountOwner();
			} catch (final NullPointerException e) {
				return null;
			}
		}

		User user;

		public <E extends Element> E getElement() {
			return (E) user.getElement();
		}
	}

	public static class SelfPaymentResponseDto {
		private String message;

		public String getMessage() {
			return message;
		}

		public void setMessage(String message) {
			this.message = message;
		}
	}

	@RequestMapping(value = "/admin/managePasswords", method = RequestMethod.POST)
	@ResponseBody
	protected AccountOwner getFromOwner(@RequestBody SelfPaymentRequestDto form) {
		// final SelfPaymentForm form = context.getForm();
		final Long memberId = IdConverter.instance().valueOf(form.getFrom());
		if (memberId == null) {
			return form.getAccountOwner();
		} else {
			return (Member) elementService.load(memberId);
		}
	}
/*
	protected void prepareForm(final ActionContext context) throws Exception {
		super.prepareForm(context);
		final HttpServletRequest request = context.getRequest();
		final boolean asMember = (Boolean) request.getAttribute("asMember");
		String titleKey;
		if (asMember) {
			titleKey = "payment.title.asMemberToSelf";
		} else if (context.isAdmin()) {
			titleKey = "payment.title.systemToSystem";
		} else {
			titleKey = "payment.title.memberToSelf";
		}
		request.setAttribute("titleKey", titleKey);
	}

	protected DoPaymentDTO resolvePaymentDTO(final ActionContext context) {
		final DoPaymentDTO dto = super.resolvePaymentDTO(context);
		dto.setContext(TransactionContext.SELF_PAYMENT);
		dto.setTo(getFromOwner(context));
		dto.setFrom(getFromOwner(context));
		// Self payment TTs don't use channel
		dto.setChannel(null);
		return dto;
	}

	protected TransferTypeQuery resolveTransferTypeQuery(
			final ActionContext context) {

		final SelfPaymentForm form = context.getForm();
		final Long memberId = IdConverter.instance().valueOf(form.getFrom());

		final TransferTypeQuery query = new TransferTypeQuery();
		query.setUsePriority(true);
		query.setContext(TransactionContext.SELF_PAYMENT);
		final AccountOwner owner = getFromOwner(context);
		query.setFromOwner(owner);
		query.setToOwner(owner);
		if (memberId != null) {
			query.setBy(context.getElement());
		} else {
			query.setGroup(context.getGroup());
		}
		return query;
	}

*/}
