package nl.strohalm.cyclos.webservices.rest.members.messages;

import org.apache.struts.action.ActionForward;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import nl.strohalm.cyclos.annotations.Inject;
import nl.strohalm.cyclos.controls.ActionContext;
import nl.strohalm.cyclos.controls.members.messages.RemoveMessageCategoryForm;
import nl.strohalm.cyclos.entities.exceptions.DaoException;
import nl.strohalm.cyclos.services.elements.MessageCategoryService;
import nl.strohalm.cyclos.utils.validation.ValidationException;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;

@Controller
public class RemoveMessageCategoryController extends BaseRestController {
	private MessageCategoryService messageCategoryService;

	public MessageCategoryService getMessageCategoryService() {
		return messageCategoryService;
	}

	@Inject
	public void setMessageCategoryService(
			final MessageCategoryService messageCategoryService) {
		this.messageCategoryService = messageCategoryService;
	}

	public static class RemoveMessageCategoryRequestDto {
		private long messageCategoryId;

		public long getMessageCategoryId() {
			return messageCategoryId;
		}

		public void setMessageCategoryId(final long messageCategoryId) {
			this.messageCategoryId = messageCategoryId;
		}
	}

	public static class RemoveMessageCategoryResponseDto {
		private String message;

		public String getMessage() {
			return message;
		}

		public void setMessage(String message) {
			this.message = message;
		}
	}

	@RequestMapping(value = "admin/removeMessageCategory", method = RequestMethod.DELETE)
	@ResponseBody
	protected RemoveMessageCategoryResponseDto executeAction(
			@RequestBody RemoveMessageCategoryRequestDto form) throws Exception {
		RemoveMessageCategoryResponseDto response = null;
                try{
		final long id = form.getMessageCategoryId();
		if (id <= 0L) {
			throw new ValidationException();
		}
		String message = null;
		//response = new RemoveMessageCategoryResponseDto();
		try {
			messageCategoryService.remove(id);
			message = "messageCategory.removed";
			response.setMessage(message);
			return response;
		} catch (final DaoException e) {

			message = "messageCategory.error.removing";
			response.setMessage(message);
			return response;
		} catch (final DataIntegrityViolationException e) {

			message = "messageCategory.error.removing";
			response.setMessage(message);}
                        response = new RemoveMessageCategoryResponseDto();}
                catch(Exception e){
                    e.printStackTrace();
                }
			return response;
		}
	}


