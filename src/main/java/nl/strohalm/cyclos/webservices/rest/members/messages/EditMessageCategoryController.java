package nl.strohalm.cyclos.webservices.rest.members.messages;

import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import nl.strohalm.cyclos.annotations.Inject;
import nl.strohalm.cyclos.controls.ActionContext;
import nl.strohalm.cyclos.controls.members.messages.EditMessageCategoryForm;
import nl.strohalm.cyclos.entities.members.messages.MessageCategory;
import nl.strohalm.cyclos.services.elements.MessageCategoryService;
import nl.strohalm.cyclos.utils.binding.BeanBinder;
import nl.strohalm.cyclos.utils.binding.DataBinder;
import nl.strohalm.cyclos.utils.binding.PropertyBinder;
import nl.strohalm.cyclos.utils.conversion.IdConverter;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;

@Controller
public class EditMessageCategoryController extends BaseRestController {
	private MessageCategoryService messageCategoryService;
	private DataBinder<MessageCategory> dataBinder;

	public DataBinder<MessageCategory> getDataBinder() {

		if (dataBinder == null) {
			final BeanBinder<MessageCategory> binder = BeanBinder
					.instance(MessageCategory.class);
			binder.registerBinder(
					"id",
					PropertyBinder.instance(Long.class, "id",
							IdConverter.instance()));
			binder.registerBinder("name",
					PropertyBinder.instance(String.class, "name"));
			dataBinder = binder;
		}
		return dataBinder;
	}

	public MessageCategoryService getMessageCategoryService() {
		return messageCategoryService;
	}

	@Inject
	public void setMessageCategoryService(
			final MessageCategoryService messageCategoryService) {
		this.messageCategoryService = messageCategoryService;
	}

	public static class EditMessageCategoryRequestDto {
		protected Map<String, Object> values;

		public Map<String, Object> getValues() {
			return values;
		}

		public void setValues(final Map<String, Object> values) {
			this.values = values;
		}

		private long messageCategoryId;

		public Map<String, Object> getMessageCategory() {
			return values;
		}

		public Object getMessageCategory(final String key) {
			return values.get(key);
		}

		public long getMessageCategoryId() {
			return messageCategoryId;
		}

		public void setMessageCategory(final Map<String, Object> values) {
			this.values = values;
		}

		public void setMessageCategory(final String key, final Object value) {
			values.put(key, value);
		}

		public void setMessageCategoryId(final long messageCategoryId) {
			this.messageCategoryId = messageCategoryId;
		}
	}

	public static class EditMessageCategoryResponseDto {
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
	protected EditMessageCategoryResponseDto formAction(
			@RequestBody EditMessageCategoryRequestDto form) throws Exception {
		// final EditMessageCategoryForm form = context.getForm();
		final MessageCategory category = getDataBinder().readFromString(
				form.getMessageCategory());
		final boolean insert = category.getId() == null;
		messageCategoryService.save(category);
		EditMessageCategoryResponseDto response = new EditMessageCategoryResponseDto();
		String message = null;
		if (insert) {
			message = "messageCategory.inserted";
			response.setMessage(message);
			return response;
		} else {
			message = "messageCategory.modified";
			response.setMessage(message);
			return response;
		}

	}

	/*
	 * @Override protected void prepareForm(final ActionContext context) throws
	 * Exception { final EditMessageCategoryForm form = context.getForm(); final
	 * HttpServletRequest request = context.getRequest(); MessageCategory
	 * messageCategory; if (form.getMessageCategoryId() > 0) { messageCategory =
	 * messageCategoryService.load(form.getMessageCategoryId()); } else {
	 * messageCategory = new MessageCategory(); }
	 * 
	 * getDataBinder().writeAsString(form.getMessageCategory(),
	 * messageCategory); request.setAttribute("messageCategory",
	 * messageCategory); }
	 */

	protected void validateForm(final ActionContext context) {
		final EditMessageCategoryForm form = context.getForm();
		final MessageCategory messageCategory = getDataBinder().readFromString(
				form.getMessageCategory());
		getMessageCategoryService().validate(messageCategory);
	}

}
