package nl.strohalm.cyclos.webservices.rest.customization.translationMessages;

import java.util.HashMap;
import java.util.Map;


import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import nl.strohalm.cyclos.annotations.Inject;
import nl.strohalm.cyclos.controls.ActionContext;
import nl.strohalm.cyclos.controls.customization.translationMessages.EditTranslationMessageForm;
import nl.strohalm.cyclos.entities.customization.translationMessages.TranslationMessage;
import nl.strohalm.cyclos.entities.exceptions.DaoException;
import nl.strohalm.cyclos.services.customization.TranslationMessageService;
import nl.strohalm.cyclos.utils.binding.BeanBinder;
import nl.strohalm.cyclos.utils.binding.DataBinder;
import nl.strohalm.cyclos.utils.binding.PropertyBinder;
import nl.strohalm.cyclos.utils.conversion.HtmlConverter;
import nl.strohalm.cyclos.utils.conversion.IdConverter;
import nl.strohalm.cyclos.utils.validation.UniqueError;
import nl.strohalm.cyclos.utils.validation.ValidationException;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class EditTranslationMessageController extends BaseRestController {

	private TranslationMessageService translationMessageService;
        
	public final TranslationMessageService getTranslationMessageService() {
		return translationMessageService;
	}

	private DataBinder<TranslationMessage> dataBinder;

	public DataBinder<TranslationMessage> getDataBinder() {
		if (dataBinder == null) {
			final BeanBinder<TranslationMessage> binder = BeanBinder
					.instance(TranslationMessage.class);
			binder.registerBinder(
					"id",
					PropertyBinder.instance(Long.class, "id",
							IdConverter.instance()));
			binder.registerBinder("key",
					PropertyBinder.instance(String.class, "key"));
			binder.registerBinder("value", PropertyBinder.instance(
					String.class, "value", HtmlConverter.instance(false)));
			dataBinder = binder;
		}
		return dataBinder;
	}

	@Inject
	public void setTranslationMessageService(
			final TranslationMessageService translationMessageService) {
		this.translationMessageService = translationMessageService;
	}

	public static class EditTranslationMessageRequestDto {
		private long messageId;
		protected Map<String, Object> values;

		public Map<String, Object> getValues() {
			return values;
		}

		public void setValues(final Map<String, Object> values) {
			this.values = values;
		}

		public Map<String, Object> getMessage() {
			return values;
		}

		public Object getMessage(final String key) {
			return values.get(key);
		}

		public long getMessageId() {
			return messageId;
		}

		public void setMessage(final Map<String, Object> message) {
			values = message;
		}

		public void setMessage(final String key, final Object value) {
			values.put(key, value);
		}

		public void setMessageId(final long messageId) {
			this.messageId = messageId;
		}
	}

	public static class EditTranslationMessageResponseDto {
		private String message;

		public String getMessage() {
			return message;
		}

		public void setMessage(String message) {
			this.message = message;
		}
                public EditTranslationMessageResponseDto(){
                }
	}

	@RequestMapping(value = "admin/editTranslationMessage", method = RequestMethod.POST)
	@ResponseBody
	protected EditTranslationMessageResponseDto formAction(
			@RequestBody EditTranslationMessageRequestDto form)
			throws Exception {
		// final EditTranslationMessageForm form = context.getForm();
		final TranslationMessage translationMessage = getDataBinder()
				.readFromString(form.getMessage());
		final boolean isInsert = translationMessage.getId() == null;
		String message = null;
		EditTranslationMessageResponseDto response = new EditTranslationMessageResponseDto();
		try {
			translationMessageService.save(translationMessage);
			if (isInsert) {
				message = "translationMessage.inserted";
				response.setMessage(message);
				return response;
			} else {
				message = "translationMessage.modified";
				response.setMessage(message);
				return response;
			}
		} catch (final DaoException e) {
			throw new ValidationException("key", "translationMessage.key",
					new UniqueError(translationMessage.getKey()));
		}
	}

        public static class PrepareFormResponseDto{
           private  TranslationMessage message;
            private boolean isInsert;

        public TranslationMessage getMessage() {
            return message;
        }

        public void setMessage(TranslationMessage message) {
            this.message = message;
        }

       

        public boolean isIsInsert() {
            return isInsert;
        }

        public void setIsInsert(boolean isInsert) {
            this.isInsert = isInsert;
        }
            public PrepareFormResponseDto(){
            }
            
        }
        
        @RequestMapping(value = "admin/editTranslationMessage/{messageId}", method = RequestMethod.GET)
	@ResponseBody
	protected PrepareFormResponseDto prepareForm(@PathVariable("messageId") Long messageId ) throws Exception {
//		final HttpServletRequest request = context.getRequest();
//		final EditTranslationMessageForm form = context.getForm();
//		final long id = form.getMessageId();
              PrepareFormResponseDto response=new PrepareFormResponseDto();
		final boolean isInsert = messageId <= 0L;
		if (!isInsert) {
                    Map<String, Object>  message=new HashMap<String,Object>();
			final TranslationMessage translationMessage = translationMessageService
					.load(messageId);
			getDataBinder()
					.writeAsString(message, translationMessage);
			//request.setAttribute("message", translationMessage);
                        response.setMessage(translationMessage);
		}
		//request.setAttribute("isInsert", isInsert);
                response.setIsInsert(isInsert);
                return response;
	}

	protected void validateForm(final ActionContext context) {
		final EditTranslationMessageForm form = context.getForm();
		final TranslationMessage translationMessage = getDataBinder()
				.readFromString(form.getMessage());
		translationMessageService.validate(translationMessage);
	}

}
