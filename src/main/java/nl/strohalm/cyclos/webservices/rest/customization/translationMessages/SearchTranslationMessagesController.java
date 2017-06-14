package nl.strohalm.cyclos.webservices.rest.customization.translationMessages;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import nl.strohalm.cyclos.annotations.Inject;
import nl.strohalm.cyclos.controls.ActionContext;
import nl.strohalm.cyclos.controls.customization.translationMessages.SearchTranslationMessagesForm;
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

	public static class SearchTranslationMessagesRequestDto {

	}

	public static class SearchTranslationMessagesResponseDto {
		private List<TranslationMessage> translationMessages;

		public SearchTranslationMessagesResponseDto(
				List<TranslationMessage> translationMessages) {
			super();
			this.translationMessages = translationMessages;
		}

	}

	@RequestMapping(value = "/admin/managePasswords", method = RequestMethod.POST)
	@ResponseBody
	protected SearchTranslationMessagesResponseDto executeQuery(
			@RequestBody SearchTranslationMessagesRequestDto form,
			final QueryParameters queryParameters) {
		// final HttpServletRequest request = context.getRequest();
		final TranslationMessageQuery query = (TranslationMessageQuery) queryParameters;
		final List<TranslationMessage> translationMessages = translationMessageService
				.search(query);
		SearchTranslationMessagesResponseDto response = new SearchTranslationMessagesResponseDto(
				translationMessages);
		return response;
	}

	protected QueryParameters prepareForm(final ActionContext context) {
		final SearchTranslationMessagesForm form = context.getForm();
		return getDataBinder().readFromString(form.getQuery());
	}

}
