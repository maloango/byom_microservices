package nl.strohalm.cyclos.webservices.rest.members.messages;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import nl.strohalm.cyclos.annotations.Inject;
import nl.strohalm.cyclos.controls.ActionContext;
import nl.strohalm.cyclos.entities.members.Member;
import nl.strohalm.cyclos.entities.members.messages.Message;
import nl.strohalm.cyclos.entities.members.messages.Message.RootType;
import nl.strohalm.cyclos.entities.members.messages.MessageBox;
import nl.strohalm.cyclos.entities.members.messages.MessageCategory;
import nl.strohalm.cyclos.entities.members.messages.MessageQuery;
import nl.strohalm.cyclos.services.elements.MessageService;
import nl.strohalm.cyclos.utils.binding.BeanBinder;
import nl.strohalm.cyclos.utils.binding.DataBinder;
import nl.strohalm.cyclos.utils.binding.DataBinderHelper;
import nl.strohalm.cyclos.utils.binding.PropertyBinder;
import nl.strohalm.cyclos.utils.query.QueryParameters;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;

@Controller
public class SearchMessagesController extends BaseRestController {

	private DataBinder<MessageQuery> dataBinder;
	private MessageService messageService;

	public DataBinder<MessageQuery> getDataBinder() {
		if (dataBinder == null) {
			final BeanBinder<MessageQuery> binder = BeanBinder
					.instance(MessageQuery.class);
			binder.registerBinder("messageBox",
					PropertyBinder.instance(MessageBox.class, "messageBox"));
			binder.registerBinder("rootType",
					PropertyBinder.instance(RootType.class, "rootType"));
			binder.registerBinder("relatedMember",
					PropertyBinder.instance(Member.class, "relatedMember"));
			binder.registerBinder("category",
					PropertyBinder.instance(MessageCategory.class, "category"));
			binder.registerBinder("keywords",
					PropertyBinder.instance(String.class, "keywords"));
			binder.registerBinder("pageParameters",
					DataBinderHelper.pageBinder());
			dataBinder = binder;
		}
		return dataBinder;
	}

	@Inject
	public void setMessageService(final MessageService messageService) {
		this.messageService = messageService;
	}

	public static class SearchMessagesRequestDto {

	}

	public static class SearchMessagesResponseDto {

		private List<Message> list;

		public SearchMessagesResponseDto(List<Message> list) {
			super();
			this.list = list;
		}

	}

	@RequestMapping(value = "", method = RequestMethod.GET)
	@ResponseBody
	protected SearchMessagesResponseDto executeQuery(
			@RequestBody SearchMessagesRequestDto context,
			final QueryParameters queryParameters) {
		final List<Message> list = messageService
				.search((MessageQuery) queryParameters);
		SearchMessagesResponseDto response = new SearchMessagesResponseDto(list);
		return response;
	}

	protected boolean willExecuteQuery(final ActionContext context,
			final QueryParameters queryParameters) throws Exception {
		return true;
	}
}
