/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.strohalm.cyclos.webservices.rest.members.messages;

import java.util.Map;
import nl.strohalm.cyclos.annotations.Inject;
import nl.strohalm.cyclos.entities.members.Member;
import nl.strohalm.cyclos.entities.members.messages.Message;
import nl.strohalm.cyclos.entities.members.preferences.NotificationPreference;
import nl.strohalm.cyclos.services.elements.ElementService;
import nl.strohalm.cyclos.services.elements.MessageCategoryService;
import nl.strohalm.cyclos.services.elements.MessageService;
import nl.strohalm.cyclos.services.elements.SendDirectMessageToMemberDTO;
import nl.strohalm.cyclos.services.elements.SendMessageDTO;
import nl.strohalm.cyclos.services.elements.exceptions.MemberWontReceiveNotificationException;
import nl.strohalm.cyclos.services.groups.GroupService;
import nl.strohalm.cyclos.services.permissions.PermissionService;
import nl.strohalm.cyclos.services.preferences.PreferenceService;
import nl.strohalm.cyclos.services.settings.SettingsService;
import nl.strohalm.cyclos.utils.access.LoggedUser;
import nl.strohalm.cyclos.utils.binding.DataBinder;
import nl.strohalm.cyclos.utils.transaction.CurrentTransactionData;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;
import nl.strohalm.cyclos.webservices.rest.GenericResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 *
 * @author Lue
 */
@Controller
public class MemberSendMessageController extends BaseRestController{
    public static enum SendTo {
        MEMBER, ADMIN, GROUP, BROKERED_MEMBERS
    }

    private static final int                                                           WRAP_SIZE = 50;

    private MessageService                                                             messageService;
    private MessageCategoryService                                                     messageCategoryService;
    private PreferenceService                                                          preferenceService;
    private ElementService elementService;
    private PermissionService permissionService;
    private GroupService groupService;
    private SettingsService settingsService;
    
    private Map<Class<? extends SendMessageDTO>, DataBinder<? extends SendMessageDTO>> dataBindersByType;
    

    public void setElementService(ElementService elementService) {
        this.elementService = elementService;
    }

    public void setPermissionService(PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    public void setGroupService(GroupService groupService) {
        this.groupService = groupService;
    }

    public void setSettingsService(SettingsService settingsService) {
        this.settingsService = settingsService;
    }
    @Inject
    public void setDataBindersByType(Map<Class<? extends SendMessageDTO>, DataBinder<? extends SendMessageDTO>> dataBindersByType) {
        this.dataBindersByType = dataBindersByType;
    }
    
    @Inject
    public void setMessageCategoryService(final MessageCategoryService messageCategoryService) {
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
    
    
    public static class SendMessageResponse extends GenericResponse{
    private long              toMemberId;
    private long              inReplyTo;
    private String            sendTo;
    private boolean           toBrokeredMembers;
    private boolean           html;
    private String            toMember;
    private long              category;
    private String            subject;
    private String              body;

        public boolean isHtml() {
            return html;
        }

        public void setHtml(boolean html) {
            this.html = html;
        }
    

        public String getToMember() {
            return toMember;
        }

        public void setToMember(String toMember) {
            this.toMember = toMember;
        }

        public long getCategory() {
            return category;
        }

        public void setCategory(long category) {
            this.category = category;
        }

        public String getSubject() {
            return subject;
        }

        public void setSubject(String subject) {
            this.subject = subject;
        }

        public String getBody() {
            return body;
        }

        public void setBody(String body) {
            this.body = body;
        }

        public Map<String, Object> getValues() {
            return values;
        }

        public void setValues(Map<String, Object> values) {
            this.values = values;
        }
    

   protected Map<String, Object> values;
   
  
    public long getInReplyTo() {
        return inReplyTo;
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

    public final void setMessage(final String key, final Object value) {
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
        
    }
    
    

    @RequestMapping(value = "member/sendMessage", method = RequestMethod.POST)
    @ResponseBody
    public SendMessageResponse sendMessage(@RequestBody SendMessageResponse request) throws Exception {
       // final SendMessageForm form = context.getForm();
       SendMessageResponse response = new SendMessageResponse();
        final long toMemberId = request.getToMemberId();

        // Send the message
        
      // final SendMessageDTO dto = resolveDTO(request);
       Class<? extends SendMessageDTO> dtoClass = null;
       final SendMessageDTO dto = dtoClass.newInstance();

        // Call the correct service method
        try {
            String key = "message.sent";

            messageService.send(dto);
            if (dto instanceof SendDirectMessageToMemberDTO) {
                final SendDirectMessageToMemberDTO sendDirectMessageToMemberDTO = (SendDirectMessageToMemberDTO) dto;
                Message.Type type = null;

                if (LoggedUser.isAdministrator()) {
                    type = Message.Type.FROM_ADMIN_TO_MEMBER;
                } else {
                    type = Message.Type.FROM_MEMBER;
                }

                if (CurrentTransactionData.hasMailError()) {
                    final Member member = sendDirectMessageToMemberDTO.getToMember();
                    final NotificationPreference np = preferenceService.load(member, type);
                    if (np.isMessage()) {
                        key = "message.warning.messageNotReceivedByEmail";
                    } else {
                        response.setMessage("message.error.emailNotSent");
                       
                    }
                }
                 return response;
            }
           //response.setMessage(key);
        } catch (final MemberWontReceiveNotificationException e) {
            response.setMessage("message.error.memberWontReceiveNotification");
            
        }

        // Go back to the correct location
        if (dto.getInReplyTo() == null && toMemberId > 0L) {
            //return ActionHelper.redirectWithParam(context.getRequest(), context.findForward("backToProfile"), "memberId", toMemberId);
            
        }
        response.setMessage("backToList");
         response.setStatus(0);
          response.setMessage("message.sent");
        return response;
    }
    
    

//   @RequestMapping(value = "member/sendMessage", method = RequestMethod.GET)
//    @ResponseBody
//    public SendMessageResponse prepareForm() throws Exception {
//        //final SendMessageForm form = context.getForm();
//        //final HttpServletRequest request = context.getRequest();
//        
//        SendMessageResponse response = new SendMessageResponse();
//        final Member toMember = resolveToMember(Context);
//        final Message inReplyTo = resolveInReplyTo(context);
//
//        if (toMember == null) {
//            final List<SendTo> sendTo = new ArrayList<SendTo>();
//            if (LoggedUser.isAdministrator()) {
//                // An admin may send to a group, so, we must get the groups
//                if (inReplyTo == null) {
//                    final GroupQuery gq = new GroupQuery();
//                    gq.setNatures(Group.Nature.MEMBER, Group.Nature.BROKER);
//                    gq.setStatus(Group.Status.NORMAL);
//                   // response.setAttribute("groups", groupService.search(gq));
//                    if (permissionService.hasPermission(AdminMemberPermission.MESSAGES_SEND_TO_MEMBER)) {
//                        sendTo.add(SendTo.MEMBER);
//                    }
//                    if (permissionService.hasPermission(AdminMemberPermission.MESSAGES_SEND_TO_GROUP)) {
//                        sendTo.add(SendTo.GROUP);
//                    }
//                }
//            } else {
//                if (LoggedUser.isBroker()) {
//                    if (LoggedUser.isBroker() && permissionService.hasPermission(BrokerPermission.MESSAGES_SEND_TO_MEMBERS)) {
//                        sendTo.add(SendTo.BROKERED_MEMBERS);
//                        response.setToBrokeredMembers(true);
//                    }
//                } else if (inReplyTo == null) {
//                    if (LoggedUser.isMember() && permissionService.hasPermission(MemberPermission.MESSAGES_SEND_TO_MEMBER) || LoggedUser.isOperator() && permissionService.hasPermission(OperatorPermission.MESSAGES_SEND_TO_MEMBER)) {
//                        sendTo.add(SendTo.MEMBER);
//                    }
//                    if (LoggedUser.isBroker() && permissionService.hasPermission(BrokerPermission.MESSAGES_SEND_TO_MEMBERS)) {
//                        sendTo.add(SendTo.BROKERED_MEMBERS);
//                    }
//                    // A member may send to admin, so we must get the categories
//                    final MessageCategoryQuery query = new MessageCategoryQuery();
//                    query.setFromElement((Member) LoggedUser.accountOwner());
//                    final List<MessageCategory> categories = messageCategoryService.search(query);
//                    response.setCategories(WRAP_SIZE);
//                    if (CollectionUtils.isNotEmpty(categories) && (LoggedUser.isMember() && permissionService.hasPermission(MemberPermission.MESSAGES_SEND_TO_ADMINISTRATION) || LoggedUser.isOperator() && permissionService.hasPermission(OperatorPermission.MESSAGES_SEND_TO_ADMINISTRATION))) {
//                        sendTo.add(SendTo.ADMIN);
//                    }
//                }
//            }
//            if (inReplyTo == null && CollectionUtils.isEmpty(sendTo)) {
//                throw new PermissionDeniedException();
//            }
//            response.setSendTo("sendTo");
//        } else {
//            final MessageCategoryQuery query = new MessageCategoryQuery();
//            query.setFromElement((Element) (LoggedUser.isOperator() ? LoggedUser.accountOwner(): LoggedUser.element()));
//            query.setToElement(toMember);
//           // response.setCategories("messageCategoryService.search(query)");
//           response.setCategories(WRAP_SIZE);
//        }

        // Message reply
//        final LocalSettings localSettings = settingsService.getLocalSettings();
//        TextFormat messageFormat = localSettings.getMessageFormat();
//        if (inReplyTo != null) {
//            response.setMessage("subject", message("message.reply.subject", inReplyTo.getSubject()));
//            String body;
//            if (inReplyTo.isHtml()) {
//                body = "<br><br><div style='padding-left:40px;border-left:1px solid black'>" + inReplyTo.getBody() + "</div>";
//                messageFormat = TextFormat.RICH;
//            } else {
//                body = " \n\n> " + StringUtils.replace(WordUtils.wrap(inReplyTo.getBody(), WRAP_SIZE), "\n", "\n> ");
//                messageFormat = TextFormat.PLAIN;
//            }
//            response.setBody("body");
//            response.setHtml(inReplyTo.isHtml());
//            if (inReplyTo.getCategory() != null) {
//                response.setInReplyTo(inReplyTo.getCategory().getId());
//                if (inReplyTo.getToMember() != null) {
//                    // Reply to a member
//                    response.setCategoryName(inReplyTo.getCategory().getName());
//                    response.setCategoryEditable(false);
//                } else {
//                    // Reply to administration
//                    final MessageCategoryQuery query = new MessageCategoryQuery();
//                    query.setFromElement((Element) (LoggedUser.isOperator() ? LoggedUser.accountOwner(): LoggedUser.element()));
//                    response.setCategories("messageCategoryService.search(query)");
//                    response.setCategoryId(inReplyTo.getCategory().getId());
//                }
//            }
//        }
//        response.setHtml(messageFormat == TextFormat.RICH);
//        response.setInReplyTo(WRAP_SIZE);
//        response.setToMember("toMember");
//        response.setMessageFormat("messageFormat");
//        return response;
//    }}

    //@Override
//    protected void validateForm(final ActionContext context) {
//        final SendMessageDTO dto = resolveDTO(context);
//        messageService.validate(dto);
//    }

//    private <T extends SendMessageDTO> BeanBinder<T> basicDataBinderFor(final Class<T> type) {
//        final BeanBinder<T> binder = BeanBinder.instance(type);
//        // The body is not read here, as it can be either plain text or html
//        binder.registerBinder("category", PropertyBinder.instance(MessageCategory.class, "category"));
//        binder.registerBinder("subject", PropertyBinder.instance(String.class, "subject"));
//        binder.registerBinder("inReplyTo", PropertyBinder.instance(Message.class, "inReplyTo"));
//        binder.registerBinder("html", PropertyBinder.instance(Boolean.TYPE, "html"));
//        return binder;
//    }
//
//    @SuppressWarnings("unchecked")
//    private <T extends SendMessageDTO> DataBinder<T> getDataBinderFor(final Class<T> type) {
//        if (dataBindersByType == null) {
//            dataBindersByType = new HashMap<Class<? extends SendMessageDTO>, DataBinder<? extends SendMessageDTO>>();
//            final BeanBinder<SendDirectMessageToMemberDTO> toMemberBinder = basicDataBinderFor(SendDirectMessageToMemberDTO.class);
//            toMemberBinder.registerBinder("toMember", PropertyBinder.instance(Member.class, "toMember"));
//            dataBindersByType.put(SendDirectMessageToMemberDTO.class, toMemberBinder);
//
//            final BeanBinder<SendMessageToAdminDTO> toAdminBinder = basicDataBinderFor(SendMessageToAdminDTO.class);
//            dataBindersByType.put(SendMessageToAdminDTO.class, toAdminBinder);
//
//            final BeanBinder<SendMessageFromBrokerToMembersDTO> toBrokeredBinder = basicDataBinderFor(SendMessageFromBrokerToMembersDTO.class);
//            dataBindersByType.put(SendMessageFromBrokerToMembersDTO.class, toBrokeredBinder);
//
//            final BeanBinder<SendMessageToGroupDTO> toGroupBinder = basicDataBinderFor(SendMessageToGroupDTO.class);
//            toGroupBinder.registerBinder("toGroups", SimpleCollectionBinder.instance(MemberGroup.class, "toGroups"));
//            dataBindersByType.put(SendMessageToGroupDTO.class, toGroupBinder);
//        }
//        return (DataBinder<T>) dataBindersByType.get(type);
//    }

    /**
     * Resolve a send message dto
     */
//    private SendMessageDTO resolveDTO(final ActionContext context) {
//        //final SendMessageForm form = context.getForm();
//        Class<? extends SendMessageDTO> dtoClass = null;
//        final SendTo sendTo = CoercionHelper.coerce(SendTo.class, form.getSendTo());
//        if (sendTo == null) {
//            throw new ValidationException();
//        }
//        // Test and validate who to send the message
//        switch (sendTo) {
//            case MEMBER:
//                dtoClass = SendDirectMessageToMemberDTO.class;
//                break;
//            case GROUP:
//                if (!LoggedUser.isAdministrator()) {
//                    throw new ValidationException();
//                }
//                dtoClass = SendMessageToGroupDTO.class;
//                break;
//            case BROKERED_MEMBERS:
//                if (!LoggedUser.isBroker()) {
//                    throw new ValidationException();
//                }
//                dtoClass = SendMessageFromBrokerToMembersDTO.class;
//                break;
//            case ADMIN:
//                if (!(LoggedUser.isMember() || context.isOperator())) {
//                    throw new ValidationException();
//                }
//                dtoClass = SendMessageToAdminDTO.class;
//                break;
//            default:
//                throw new ValidationException();
//        }
//
//        final SendMessageDTO dto = getDataBinderFor(dtoClass).readFromString(form.getMessage());
//        if (dto.isHtml()) {
//            dto.setBody(HtmlConverter.instance().valueOf("" + form.getMessage("body")));
//        } else {
//            dto.setBody(StringTrimmerConverter.instance().valueOf("" + form.getMessage("body")));
//        }
//        return dto;
//    }
//
//    private Message resolveInReplyTo(final ActionContext context) {
//        final SendMessageForm form = context.getForm();
//        final long inReplyToId = form.getInReplyTo();
//        if (inReplyToId <= 0L) {
//            return null;
//        }
//        final Message inReplyTo = messageService.load(inReplyToId, Message.Relationships.TO_MEMBER);
//        if ((context.isAdmin() && inReplyTo.getToMember() != null) || (context.isMember() && !context.getAccountOwner().equals(inReplyTo.getToMember()))) {
//            throw new PermissionDeniedException();
//        }
//        return inReplyTo;
//    }
//
//    /**
//     * Resolve the member to send to, if any
//     */
//    private Member resolveToMember(final ActionContext context) {
//        final SendMessageForm form = context.getForm();
//        final long toMemberId = form.getToMemberId();
//        Member toMember = null;
//
//        // Load the member to send to, if any
//        if (toMemberId > 0L) {
//            final Element loggedElement = (Element) (LoggedUser.isOperator() ? LoggedUser.accountOwner(): LoggedUser.getElement());
//            // Cannot send to self
//            if (toMemberId == loggedElement.getId()) {
//                throw new ValidationException();
//            }
//            // Ensure a member
//            final Element element = elementService.load(toMemberId, Element.Relationships.USER);
//            if (!(element instanceof Member)) {
//                throw new ValidationException();
//            }
//            toMember = (Member) element;
//        }
//
//        return toMember;
//    }
    
}
