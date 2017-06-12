package nl.strohalm.cyclos.webservices.rest.members.messages;

import java.util.Collection;
import java.util.List;

import nl.strohalm.cyclos.annotations.Inject;
import nl.strohalm.cyclos.entities.groups.Group;
import nl.strohalm.cyclos.entities.members.Element;
import nl.strohalm.cyclos.entities.members.messages.MessageCategory;
import nl.strohalm.cyclos.entities.members.messages.MessageCategoryQuery;
import nl.strohalm.cyclos.services.elements.MessageCategoryService;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class ListMessageCategoriesController extends BaseRestController {
	private MessageCategoryService messageCategoryService;

	@Inject
	public void setMessageCategoryService(
			final MessageCategoryService messageCategoryService) {
		this.messageCategoryService = messageCategoryService;
	}

	public static class ListMessageCategoriesRequestDto {
		private Element fromElement;
		private Element toElement;
		private Collection<? extends Group> groups;

		public Element getFromElement() {
			return fromElement;
		}

		public Collection<? extends Group> getGroups() {
			return groups;
		}

		public Element getToElement() {
			return toElement;
		}

		public void setFromElement(final Element fromElement) {
			this.fromElement = fromElement;
		}

		public void setGroups(final Collection<? extends Group> groups) {
			this.groups = groups;
		}

		public void setToElement(final Element toElement) {
			this.toElement = toElement;
		}

	}

	public static class ListMessageCategoriesResponseDto {
		private List<MessageCategory> search;

		public ListMessageCategoriesResponseDto(List<MessageCategory> search) {
			super();
			this.search = search;
		}

	}

	@RequestMapping(value = "/admin/managePasswords", method = RequestMethod.POST)
	@ResponseBody
	protected ListMessageCategoriesResponseDto executeAction(
			@RequestBody ListMessageCategoriesRequestDto form) throws Exception {
		final MessageCategoryQuery query = new MessageCategoryQuery();
		List<MessageCategory> search = messageCategoryService.search(query);
		ListMessageCategoriesResponseDto response = new ListMessageCategoriesResponseDto(
				search);
		return response;
	}
}
