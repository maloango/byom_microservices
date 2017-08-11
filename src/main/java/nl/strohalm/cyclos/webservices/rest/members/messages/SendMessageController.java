package nl.strohalm.cyclos.webservices.rest.members.messages;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import nl.strohalm.cyclos.access.AdminMemberPermission;
import nl.strohalm.cyclos.access.BrokerPermission;
import nl.strohalm.cyclos.access.MemberPermission;
import nl.strohalm.cyclos.access.OperatorPermission;
import nl.strohalm.cyclos.annotations.Inject;
import nl.strohalm.cyclos.controls.ActionContext;
import nl.strohalm.cyclos.controls.members.messages.SendMessageForm;
import nl.strohalm.cyclos.entities.groups.Group;
import nl.strohalm.cyclos.entities.groups.GroupQuery;
import nl.strohalm.cyclos.entities.groups.MemberGroup;
import nl.strohalm.cyclos.entities.members.Element;
import nl.strohalm.cyclos.entities.members.Member;
import nl.strohalm.cyclos.entities.members.messages.Message;
import nl.strohalm.cyclos.entities.members.messages.Message.Type;
import nl.strohalm.cyclos.entities.members.messages.MessageCategory;
import nl.strohalm.cyclos.entities.members.messages.MessageCategoryQuery;
import nl.strohalm.cyclos.entities.members.preferences.NotificationPreference;
import nl.strohalm.cyclos.entities.settings.LocalSettings;
import nl.strohalm.cyclos.exceptions.PermissionDeniedException;
import nl.strohalm.cyclos.services.elements.ElementService;
import nl.strohalm.cyclos.services.elements.MessageCategoryService;
import nl.strohalm.cyclos.services.elements.MessageService;
import nl.strohalm.cyclos.services.elements.SendDirectMessageToMemberDTO;
import nl.strohalm.cyclos.services.elements.SendMessageDTO;
import nl.strohalm.cyclos.services.elements.SendMessageFromBrokerToMembersDTO;
import nl.strohalm.cyclos.services.elements.SendMessageToAdminDTO;
import nl.strohalm.cyclos.services.elements.SendMessageToGroupDTO;
import nl.strohalm.cyclos.services.elements.exceptions.MemberWontReceiveNotificationException;
import nl.strohalm.cyclos.services.groups.GroupService;
import nl.strohalm.cyclos.services.permissions.PermissionService;
import nl.strohalm.cyclos.services.preferences.PreferenceService;
import nl.strohalm.cyclos.services.settings.SettingsService;
import nl.strohalm.cyclos.utils.ActionHelper;
import nl.strohalm.cyclos.utils.TextFormat;
import nl.strohalm.cyclos.utils.binding.BeanBinder;
import nl.strohalm.cyclos.utils.binding.DataBinder;
import nl.strohalm.cyclos.utils.binding.PropertyBinder;
import nl.strohalm.cyclos.utils.binding.SimpleCollectionBinder;
import nl.strohalm.cyclos.utils.conversion.CoercionHelper;
import nl.strohalm.cyclos.utils.conversion.HtmlConverter;
import nl.strohalm.cyclos.utils.conversion.StringTrimmerConverter;
import nl.strohalm.cyclos.utils.transaction.CurrentTransactionData;
import nl.strohalm.cyclos.utils.validation.ValidationException;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.WordUtils;
import org.apache.struts.action.ActionForward;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class SendMessageController extends BaseRestController {
	public static enum SendTo {
		MEMBER, ADMIN, GROUP, BROKERED_MEMBERS
	}

	private static final int WRAP_SIZE = 50;
	private MessageService messageService;
	private MessageCategoryService messageCategoryService;
	private PreferenceService preferenceService;
	private ElementService elementService;
	private PermissionService permissionService;
	private GroupService groupService;
	private SettingsService settingsService;
	private Map<Class<? extends SendMessageDTO>, DataBinder<? extends SendMessageDTO>> dataBindersByType;

	@Inject
	public void setMessageCategoryService(
			final MessageCategoryService messageCategoryService) {
		this.messageCategoryService = messageCategoryService;
	}

	@Inject
	public void setMessageService(final MessageService messageService) {
		this.messageService = messageService;
	}

	@Inject
	public void setPreferenceService(final PreferenceService preferenceService) {
		this.preferenceService = preferenceService;
	}

	public static class SendMessageRequestDto {
		private long toMemberId;
		private long inReplyTo;
		private String sendTo;
		private boolean toBrokeredMembers;
		protected Map<String, Object> values;

		public Map<String, Object> getValues() {
			return values;
		}

		public void setValues(final Map<String, Object> values) {
			this.values = values;
		}

		public long getInReplyTo() {
			return inReplyTo;
		}

		public Map<String, Object> getMessage() {
			return values;
		}

		public Object getMessage(final String key) {
			return values.get(key);
		}

		public String getSendTo() {
			return sendTo;
		}

		public long getToMemberId() {
			return toMemberId;
		}

		public boolean isToBrokeredMembers() {
			return toBrokeredMembers;
		}

		public void setInReplyTo(final long inReplyTo) {
			this.inReplyTo = inReplyTo;
		}

		public void setMessage(final Map<String, Object> message) {
			values = message;
		}

		public void setMessage(final String key, final Object value) {
			values.put(key, value);
		}

		public void setSendTo(final String sendTo) {
			this.sendTo = sendTo;
		}

		public void setToBrokeredMembers(final boolean toBrokeredMembers) {
			this.toBrokeredMembers = toBrokeredMembers;
		}

		public void setToMemberId(final long toMemberId) {
			this.toMemberId = toMemberId;
		}

		public boolean isAdmin() {
			// TODO Auto-generated method stub
			return false;
		}

		public boolean isBroker() {
			// TODO Auto-generated method stub
			return false;
		}

		public boolean isMember() {
			// TODO Auto-generated method stub
			return false;
		}

		public boolean isOperator() {
			// TODO Auto-generated method stub
			return false;
		}

		public void sendMessage(String key) {
			// TODO Auto-generated method stub

		}
	}

	public static class SendMessageResponseDto {
		private String key;
		String errMessage;
		long toMemberId;

		public SendMessageResponseDto(String key, String errMessage,
				long toMemberId) {
			super();
			this.key = key;
			this.errMessage = errMessage;
			this.toMemberId = toMemberId;
		}

	}

	@RequestMapping(value = "member/sendMessage", method = RequestMethod.POST)
	@ResponseBody
	protected SendMessageResponseDto handleSubmit(
			@RequestBody SendMessageRequestDto form) throws Exception {
		SendMessageResponseDto response = null;
                try{
		final long toMemberId = form.getToMemberId();

		// Send the message
		final SendMessageDTO dto = resolveDTO(form);

		// Call the correct service method
		String errMessage = null;
		String key = null;
		response = new SendMessageResponseDto(key, errMessage, toMemberId);
		try {
			 key = "message.sent";

			messageService.send(dto);
			if (dto instanceof SendDirectMessageToMemberDTO) {
				final SendDirectMessageToMemberDTO sendDirectMessageToMemberDTO = (SendDirectMessageToMemberDTO) dto;
				Type type = null;

				if (form.isAdmin()) {
					type = Message.Type.FROM_ADMIN_TO_MEMBER;
				} else {
					type = Message.Type.FROM_MEMBER;
				}

				if (CurrentTransactionData.hasMailError()) {
					final Member member = sendDirectMessageToMemberDTO
							.getToMember();
					final NotificationPreference np = preferenceService.load(
							member, type);
					if (np.isMessage()) {
						key = "message.warning.messageNotReceivedByEmail";
					} else {
						key = "message.error.emailNotSent";
						return response;
					}
				}
			}
			form.sendMessage(key);
		} catch (final MemberWontReceiveNotificationException e) {
			errMessage = "message.error.memberWontReceiveNotification";
		}

		// Go back to the correct location
		if (dto.getInReplyTo() == null && toMemberId > 0L) {
			return response;
		}}
                catch(Exception e){
                    e.printStackTrace();
                }
		return response;
	}

	protected void prepareForm(final ActionContext context) throws Exception {
		final SendMessageForm form = context.getForm();
		final HttpServletRequest request = context.getRequest();
		final Member toMember = resolveToMember(context);
		final Message inReplyTo = resolveInReplyTo(context);

		if (toMember == null) {
			final List<SendTo> sendTo = new ArrayList<SendTo>();
			if (context.isAdmin()) {
				// An admin may send to a group, so, we must get the groups
				if (inReplyTo == null) {
					final GroupQuery gq = new GroupQuery();
					gq.setNatures(Group.Nature.MEMBER, Group.Nature.BROKER);
					gq.setStatus(Group.Status.NORMAL);
					request.setAttribute("groups", groupService.search(gq));
					if (permissionService
							.hasPermission(AdminMemberPermission.MESSAGES_SEND_TO_MEMBER)) {
						sendTo.add(SendTo.MEMBER);
					}
					if (permissionService
							.hasPermission(AdminMemberPermission.MESSAGES_SEND_TO_GROUP)) {
						sendTo.add(SendTo.GROUP);
					}
				}
			} else {
				if (form.isToBrokeredMembers()) {
					if (context.isBroker()
							&& permissionService
									.hasPermission(BrokerPermission.MESSAGES_SEND_TO_MEMBERS)) {
						sendTo.add(SendTo.BROKERED_MEMBERS);
						request.setAttribute("toBrokeredMembers",
								SendTo.BROKERED_MEMBERS);
					}
				} else if (inReplyTo == null) {
					if (context.isMember()
							&& permissionService
									.hasPermission(MemberPermission.MESSAGES_SEND_TO_MEMBER)
							|| context.isOperator()
							&& permissionService
									.hasPermission(OperatorPermission.MESSAGES_SEND_TO_MEMBER)) {
						sendTo.add(SendTo.MEMBER);
					}
					if (context.isBroker()
							&& permissionService
									.hasPermission(BrokerPermission.MESSAGES_SEND_TO_MEMBERS)) {
						sendTo.add(SendTo.BROKERED_MEMBERS);
					}
					// A member may send to admin, so we must get the categories
					final MessageCategoryQuery query = new MessageCategoryQuery();
					query.setFromElement((Member) context.getAccountOwner());
					final List<MessageCategory> categories = messageCategoryService
							.search(query);
					request.setAttribute("categories", categories);
					if (CollectionUtils.isNotEmpty(categories)
							&& (context.isMember()
									&& permissionService
											.hasPermission(MemberPermission.MESSAGES_SEND_TO_ADMINISTRATION) || context
									.isOperator()
									&& permissionService
											.hasPermission(OperatorPermission.MESSAGES_SEND_TO_ADMINISTRATION))) {
						sendTo.add(SendTo.ADMIN);
					}
				}
			}
			if (inReplyTo == null && CollectionUtils.isEmpty(sendTo)) {
				throw new PermissionDeniedException();
			}
			request.setAttribute("sendTo", sendTo);
		} else {
			final MessageCategoryQuery query = new MessageCategoryQuery();
			query.setFromElement((Element) (context.isOperator() ? context
					.getAccountOwner() : context.getElement()));
			query.setToElement(toMember);
			request.setAttribute("categories",
					messageCategoryService.search(query));
		}

		// Message reply
		final LocalSettings localSettings = settingsService.getLocalSettings();
		TextFormat messageFormat = localSettings.getMessageFormat();
		if (inReplyTo != null) {
			form.setMessage(
					"subject",
					context.message("message.reply.subject",
							inReplyTo.getSubject()));
			String body;
			if (inReplyTo.isHtml()) {
				body = "<br><br><div style='padding-left:40px;border-left:1px solid black'>"
						+ inReplyTo.getBody() + "</div>";
				messageFormat = TextFormat.RICH;
			} else {
				body = " \n\n> "
						+ StringUtils.replace(
								WordUtils.wrap(inReplyTo.getBody(), WRAP_SIZE),
								"\n", "\n> ");
				messageFormat = TextFormat.PLAIN;
			}
			request.setAttribute("body", body);
			form.setMessage("html", inReplyTo.isHtml());
			if (inReplyTo.getCategory() != null) {
				form.setMessage("category", inReplyTo.getCategory().getId());
				if (inReplyTo.getToMember() != null) {
					// Reply to a member
					request.setAttribute("categoryName", inReplyTo
							.getCategory().getName());
					request.setAttribute("categoryEditable", false);
				} else {
					// Reply to administration
					final MessageCategoryQuery query = new MessageCategoryQuery();
					query.setFromElement((Element) (context.isOperator() ? context
							.getAccountOwner() : context.getElement()));
					request.setAttribute("categories",
							messageCategoryService.search(query));
					request.setAttribute("categoryId", inReplyTo.getCategory()
							.getId());
				}
			}
		}
		form.setMessage("html", messageFormat == TextFormat.RICH);
		request.setAttribute("inReplyTo", inReplyTo);
		request.setAttribute("toMember", toMember);
		request.setAttribute("messageFormat", messageFormat);
	}

	private <T extends SendMessageDTO> BeanBinder<T> basicDataBinderFor(
			final Class<T> type) {
		final BeanBinder<T> binder = BeanBinder.instance(type);
		// The body is not read here, as it can be either plain text or html
		binder.registerBinder("category",
				PropertyBinder.instance(MessageCategory.class, "category"));
		binder.registerBinder("subject",
				PropertyBinder.instance(String.class, "subject"));
		binder.registerBinder("inReplyTo",
				PropertyBinder.instance(Message.class, "inReplyTo"));
		binder.registerBinder("html",
				PropertyBinder.instance(Boolean.TYPE, "html"));
		return binder;
	}

	@SuppressWarnings("unchecked")
	private <T extends SendMessageDTO> DataBinder<T> getDataBinderFor(
			final Class<T> type) {
		if (dataBindersByType == null) {
			dataBindersByType = new HashMap<Class<? extends SendMessageDTO>, DataBinder<? extends SendMessageDTO>>();
			final BeanBinder<SendDirectMessageToMemberDTO> toMemberBinder = basicDataBinderFor(SendDirectMessageToMemberDTO.class);
			toMemberBinder.registerBinder("toMember",
					PropertyBinder.instance(Member.class, "toMember"));
			dataBindersByType.put(SendDirectMessageToMemberDTO.class,
					toMemberBinder);

			final BeanBinder<SendMessageToAdminDTO> toAdminBinder = basicDataBinderFor(SendMessageToAdminDTO.class);
			dataBindersByType.put(SendMessageToAdminDTO.class, toAdminBinder);

			final BeanBinder<SendMessageFromBrokerToMembersDTO> toBrokeredBinder = basicDataBinderFor(SendMessageFromBrokerToMembersDTO.class);
			dataBindersByType.put(SendMessageFromBrokerToMembersDTO.class,
					toBrokeredBinder);

			final BeanBinder<SendMessageToGroupDTO> toGroupBinder = basicDataBinderFor(SendMessageToGroupDTO.class);
			toGroupBinder.registerBinder("toGroups", SimpleCollectionBinder
					.instance(MemberGroup.class, "toGroups"));
			dataBindersByType.put(SendMessageToGroupDTO.class, toGroupBinder);
		}
		return (DataBinder<T>) dataBindersByType.get(type);
	}

	/**
	 * Resolve a send message dto
	 */
	private SendMessageDTO resolveDTO(final SendMessageRequestDto form) {
		// final SendMessageForm form = context.getForm();
		Class<? extends SendMessageDTO> dtoClass = null;
		final SendTo sendTo = CoercionHelper.coerce(SendTo.class,
				form.getSendTo());
		if (sendTo == null) {
			throw new ValidationException();
		}
		// Test and validate who to send the message
		switch (sendTo) {
		case MEMBER:
			dtoClass = SendDirectMessageToMemberDTO.class;
			break;
		case GROUP:
			if (!form.isAdmin()) {
				throw new ValidationException();
			}
			dtoClass = SendMessageToGroupDTO.class;
			break;
		case BROKERED_MEMBERS:
			if (!form.isBroker()) {
				throw new ValidationException();
			}
			dtoClass = SendMessageFromBrokerToMembersDTO.class;
			break;
		case ADMIN:
			if (!(form.isMember() || form.isOperator())) {
				throw new ValidationException();
			}
			dtoClass = SendMessageToAdminDTO.class;
			break;
		default:
			throw new ValidationException();
		}

		final SendMessageDTO dto = getDataBinderFor(dtoClass).readFromString(
				form.getMessage());
		if (dto.isHtml()) {
			dto.setBody(HtmlConverter.instance().valueOf(
					"" + form.getMessage("body")));
		} else {
			dto.setBody(StringTrimmerConverter.instance().valueOf(
					"" + form.getMessage("body")));
		}
		return dto;
	}

	private Message resolveInReplyTo(final ActionContext context) {
		final SendMessageForm form = context.getForm();
		final long inReplyToId = form.getInReplyTo();
		if (inReplyToId <= 0L) {
			return null;
		}
		final Message inReplyTo = messageService.load(inReplyToId,
				Message.Relationships.TO_MEMBER);
		if ((context.isAdmin() && inReplyTo.getToMember() != null)
				|| (context.isMember() && !context.getAccountOwner().equals(
						inReplyTo.getToMember()))) {
			throw new PermissionDeniedException();
		}
		return inReplyTo;
	}

	/**
	 * Resolve the member to send to, if any
	 */
	private Member resolveToMember(final ActionContext context) {
		final SendMessageForm form = context.getForm();
		final long toMemberId = form.getToMemberId();
		Member toMember = null;

		// Load the member to send to, if any
		if (toMemberId > 0L) {
			final Element loggedElement = (Element) (context.isOperator() ? context
					.getAccountOwner() : context.getElement());
			// Cannot send to self
			if (toMemberId == loggedElement.getId()) {
				throw new ValidationException();
			}
			// Ensure a member
			final Element element = elementService.load(toMemberId,
					Element.Relationships.USER);
			if (!(element instanceof Member)) {
				throw new ValidationException();
			}
			toMember = (Member) element;
		}

		return toMember;
	}

}
