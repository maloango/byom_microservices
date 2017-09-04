package nl.strohalm.cyclos.webservices.rest.customization.translationMessages;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import nl.strohalm.cyclos.annotations.Inject;
import nl.strohalm.cyclos.controls.ActionContext;
//import nl.strohalm.cyclos.controls.customization.translationMessages.SearchTranslationMessagesForm;
import nl.strohalm.cyclos.entities.customization.translationMessages.TranslationMessage;
import nl.strohalm.cyclos.entities.customization.translationMessages.TranslationMessageQuery;
import nl.strohalm.cyclos.services.customization.TranslationMessageService;
import nl.strohalm.cyclos.utils.binding.BeanBinder;
import nl.strohalm.cyclos.utils.binding.DataBinder;
import nl.strohalm.cyclos.utils.binding.DataBinderHelper;
import nl.strohalm.cyclos.utils.binding.PropertyBinder;
import nl.strohalm.cyclos.utils.query.QueryParameters;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;

@Controller
public class SearchTranslationMessagesController extends BaseRestController {

	private TranslationMessageService translationMessageService;
	public final TranslationMessageService getTranslationMessageService() {
		return translationMessageService;
	}

	private DataBinder<TranslationMessageQuery> dataBinder;

	public DataBinder<TranslationMessageQuery> getDataBinder() {
		if (dataBinder == null) {
			final BeanBinder<TranslationMessageQuery> binder = BeanBinder
					.instance(TranslationMessageQuery.class);
			binder.registerBinder("key",
					PropertyBinder.instance(String.class, "key"));
			binder.registerBinder("value",
					PropertyBinder.instance(String.class, "value"));
			binder.registerBinder("showOnlyEmpty",
					PropertyBinder.instance(Boolean.TYPE, "showOnlyEmpty"));
			binder.registerBinder("pageParameters",
					DataBinderHelper.pageBinder());
			dataBinder = binder;
		}
		return dataBinder;
	}

	@Inject
	public void setTranslationMessageService(
			final TranslationMessageService translationMessageService) {
		this.translationMessageService = translationMessageService;
	}

//	public static class SearchTranslationMessagesRequestDto {
//
//	}

	public static class SearchTranslationMessagesResponseDto {
		private List<TranslationMessage> translationMessages;

		public SearchTranslationMessagesResponseDto(
				List<TranslationMessage> translationMessages) {
			
			this.translationMessages = translationMessages;
		}

        public List<TranslationMessage> getTranslationMessages() {
            return translationMessages;
        }

        public void setTranslationMessages(List<TranslationMessage> translationMessages) {
            this.translationMessages = translationMessages;
        }
                
                

	}

	@RequestMapping(value = "admin/searchTranslationMessages", method = RequestMethod.POST)
	@ResponseBody
	protected SearchTranslationMessagesResponseDto executeQuery(
			@RequestBody TranslationMessageQuery form) {
		SearchTranslationMessagesResponseDto response =null;
                try{
		final TranslationMessageQuery query = (TranslationMessageQuery) form;
		final List<TranslationMessage> translationMessages = translationMessageService
				.search(query);
		 response = new SearchTranslationMessagesResponseDto(translationMessages);}
                catch(Exception e){
                    e.printStackTrace();
                }
		          System.out.println("Sending response  "+response.toString());		
		return response;
	}

//	protected QueryParameters prepareForm(final ActionContext context) {
//		final SearchTranslationMessagesForm form = context.getForm();
//		return getDataBinder().readFromString(form.getQuery());
//	}

}
