/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.strohalm.cyclos.webservices.rest.members.messages;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import nl.strohalm.cyclos.access.AdminMemberPermission;
import nl.strohalm.cyclos.access.BrokerPermission;
import nl.strohalm.cyclos.access.MemberPermission;
import nl.strohalm.cyclos.access.OperatorPermission;
import nl.strohalm.cyclos.annotations.Inject;
import nl.strohalm.cyclos.entities.accounts.transactions.InvoiceQuery.Direction;
import nl.strohalm.cyclos.entities.groups.AdminGroup;
import nl.strohalm.cyclos.entities.groups.SystemGroup;
import nl.strohalm.cyclos.entities.members.Element;
import nl.strohalm.cyclos.entities.members.Member;
import nl.strohalm.cyclos.entities.members.messages.Message;
import nl.strohalm.cyclos.entities.members.messages.Message.Type;
import nl.strohalm.cyclos.entities.members.messages.MessageBox;
import nl.strohalm.cyclos.entities.members.messages.MessageCategory;
import nl.strohalm.cyclos.entities.members.messages.MessageQuery;
import nl.strohalm.cyclos.services.elements.ElementService;
import nl.strohalm.cyclos.services.elements.MessageAction;
import nl.strohalm.cyclos.services.elements.MessageService;
import nl.strohalm.cyclos.services.groups.GroupService;
import nl.strohalm.cyclos.services.permissions.PermissionService;
import nl.strohalm.cyclos.utils.access.LoggedUser;
import nl.strohalm.cyclos.utils.binding.BeanBinder;
import nl.strohalm.cyclos.utils.binding.DataBinder;
import nl.strohalm.cyclos.utils.binding.DataBinderHelper;
import nl.strohalm.cyclos.utils.binding.PropertyBinder;
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
public class MemberSearchMessagesController extends BaseRestController {

    private DataBinder<MessageQuery> dataBinder;
    private MessageService messageService;
    private PermissionService permissionService;
    private ElementService elementService;
    private GroupService groupService;

    public PermissionService getPermissionService() {
        return permissionService;
    }

    public void setPermissionService(PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    public ElementService getElementService() {
        return elementService;
    }

    public void setElementService(ElementService elementService) {
        this.elementService = elementService;
    }

    public GroupService getGroupService() {
        return groupService;
    }

    public void setGroupService(GroupService groupService) {
        this.groupService = groupService;
    }

    public DataBinder<MessageQuery> getDataBinder() {
        if (dataBinder == null) {
            final BeanBinder<MessageQuery> binder = BeanBinder.instance(MessageQuery.class);
            binder.registerBinder("messageBox", PropertyBinder.instance(MessageBox.class, "messageBox"));
            binder.registerBinder("rootType", PropertyBinder.instance(Message.RootType.class, "rootType"));
            binder.registerBinder("relatedMember", PropertyBinder.instance(Member.class, "relatedMember"));
            binder.registerBinder("category", PropertyBinder.instance(MessageCategory.class, "category"));
            binder.registerBinder("keywords", PropertyBinder.instance(String.class, "keywords"));
            binder.registerBinder("pageParameters", DataBinderHelper.pageBinder());
            dataBinder = binder;
        }
        return dataBinder;
    }

    @Inject
    public void setMessageService(final MessageService messageService) {
        this.messageService = messageService;
    }

    public static class SearchMessageRequest {

        private boolean advanced;
        private String messageBox;
        private String rootType;
        private String keywords;
        private String relatedMember;
        private Long memberId;

        public Long getMemberId() {
            return memberId;
        }

        public void setMemberId(Long memberId) {
            this.memberId = memberId;
        }

        public String getRelatedMember() {
            return relatedMember;
        }

        public void setRelatedMember(String relatedMember) {
            this.relatedMember = relatedMember;
        }

        public boolean isAdvanced() {
            return advanced;
        }

        public void setAdvanced(boolean advanced) {
            this.advanced = advanced;
        }

        public String getMessageBox() {
            return messageBox;
        }

        public void setMessageBox(String messageBox) {
            this.messageBox = messageBox;
        }

         
        
        public String getRootType() {
            return rootType;
        }

        public void setRootType(String rootType) {
            this.rootType = rootType;
        }

        public String getKeywords() {
            return keywords;
        }

        public void setKeywords(String keywords) {
            this.keywords = keywords;
        }

    }

    public static class SearchMessagesResponse extends GenericResponse {

        private boolean advanced;
        private boolean canManage;
        private boolean canSend;
        private AdminGroup adminGroup;
        private List<MessageBox> messageBoxList = new ArrayList();
        private List<Message.RootType> messageRoot = new ArrayList();
        private Member relatedMember;
        private List<MessageAction> possibleActions;
        private List<MessageEntity> list;

        public List<MessageEntity> getList() {
            return list;
        }

        public void setList(List<MessageEntity> list) {
            this.list = list;
        }

        public List<MessageAction> getPossibleActions() {
            return possibleActions;
        }

        public void setPossibleActions(List<MessageAction> possibleActions) {
            this.possibleActions = possibleActions;
        }

        public Member getRelatedMember() {
            return relatedMember;
        }

        public void setRelatedMember(Member relatedMember) {
            this.relatedMember = relatedMember;
        }

        public List<Message.RootType> getMessageRoot() {
            return messageRoot;
        }

        public void setMessageRoot(List<Message.RootType> messageRoot) {
            this.messageRoot = messageRoot;
        }

        public List<MessageBox> getMessageBoxList() {
            return messageBoxList;
        }

        public void setMessageBoxList(List<MessageBox> messageBoxList) {
            this.messageBoxList = messageBoxList;
        }

        public AdminGroup getAdminGroup() {
            return adminGroup;
        }

        public void setAdminGroup(AdminGroup adminGroup) {
            this.adminGroup = adminGroup;
        }

        public boolean isCanManage() {
            return canManage;
        }

        public void setCanManage(boolean canManage) {
            this.canManage = canManage;
        }

        public boolean isCanSend() {
            return canSend;
        }

        public void setCanSend(boolean canSend) {
            this.canSend = canSend;
        }

        public boolean isAdvanced() {
            return advanced;
        }

        public void setAdvanced(final boolean advanced) {
            this.advanced = advanced;
        }

    }

    public static class MessageEntity {

        private Long memberId;
        private Calendar date;
        private Long fromMember;
        private Long toMember;
        private Direction direction;
        private String subject;
        private String body;
        private List<Type> type;
        private boolean read;
        private Calendar removedAt;
        private boolean replied;
        private boolean html;
        private boolean emailSent;
        private Long messageId;
        private List<MessageEntity> messageList = new ArrayList();
        

        public Long getMemberId() {
            return memberId;
        }

        public void setMemberId(Long memberId) {
            this.memberId = memberId;
        }

        public Long getMessageId() {
            return messageId;
        }

        public void setMessageId(Long messageId) {
            this.messageId = messageId;
        }

        public List<MessageEntity> getMessageList() {
            return messageList;
        }

        public void setMessageList(List<MessageEntity> messageList) {
            this.messageList = messageList;
        }

        public Calendar getDate() {
            return date;
        }

        public void setDate(Calendar date) {
            this.date = date;
        }

        public Long getFromMember() {
            return fromMember;
        }

        public void setFromMember(Long fromMember) {
            this.fromMember = fromMember;
        }

        public Long getToMember() {
            return toMember;
        }

        public void setToMember(Long toMember) {
            this.toMember = toMember;
        }

        public Direction getDirection() {
            return direction;
        }

        public void setDirection(Direction direction) {
            this.direction = direction;
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

        public List<Type> getType() {
            return type;
        }

        public void setType(List<Type> type) {
            this.type = type;
        }

        public boolean isRead() {
            return read;
        }

        public void setRead(boolean read) {
            this.read = read;
        }

        public Calendar getRemovedAt() {
            return removedAt;
        }

        public void setRemovedAt(Calendar removedAt) {
            this.removedAt = removedAt;
        }

        public boolean isReplied() {
            return replied;
        }

        public void setReplied(boolean replied) {
            this.replied = replied;
        }

        public boolean isHtml() {
            return html;
        }

        public void setHtml(boolean html) {
            this.html = html;
        }

        public boolean isEmailSent() {
            return emailSent;
        }

        public void setEmailSent(boolean emailSent) {
            this.emailSent = emailSent;
        }

    }

    @RequestMapping(value = "member/searchMessage", method = RequestMethod.POST)
    @ResponseBody
    public SearchMessagesResponse executeQuery(@RequestBody SearchMessageRequest request) {
        SearchMessagesResponse response = new SearchMessagesResponse();
        Map<String, Object> query = new HashMap<String, Object>();
        query.put("messageBox", request.getMessageBox());
        query.put("rootType", request.getRootType());
        query.put("relatedMember", request.relatedMember);
        query.put("keywords", request.getKeywords());
        query.put("advanced", request.isAdvanced());
        

        final MessageQuery queries = getDataBinder().readFromString(query);

        final List<Message> list = messageService.search(queries);
        List<MessageEntity> messageList = new ArrayList();
        for (Message msg : list) {
            MessageEntity entity = new MessageEntity();
            entity.setBody(msg.getBody());
            entity.setDate(msg.getDate());
            // entity.setToMember(msg.getToMember());

            entity.setEmailSent(msg.isEmailSent());
            entity.setRemovedAt(msg.getRemovedAt());
            entity.setSubject(msg.getSubject());
            entity.setEmailSent(msg.isEmailSent());
            entity.setDirection(Direction.INCOMING);
            entity.setFromMember(msg.getOwner().getId());
            // entity.setToMember(msg.getId());
            entity.setToMember(msg.getId());
            entity.setHtml(msg.isHtml());
            entity.setRead(msg.isRead());
            entity.setReplied(msg.isReplied());
            entity.setMessageId(msg.getId());
            entity.setMemberId(msg.getId());
            messageList.add(entity);

        }

        System.out.println("....." + list.size());
        for (int i = 0; i < list.size(); i++) {
            System.out.println("------" + list.get(i));
        }

        response.setList(messageList);
        response.setStatus(0);
        response.setMessage("!! ..Display the search Message..");
        return response;

    }

    @RequestMapping(value = "member/searchMessage", method = RequestMethod.GET)
    @ResponseBody
    public SearchMessagesResponse prepareForm() {
        SearchMessagesResponse response = new SearchMessagesResponse();

        // Resolve the query object
        final MessageQuery query = new MessageQuery();
        final MessageBox messageBox = query.getMessageBox();
        List<MessageAction> possibleActions = new ArrayList();
        possibleActions.add(MessageAction.DELETE);
        possibleActions.add(MessageAction.MARK_AS_READ);
        possibleActions.add(MessageAction.MARK_AS_UNREAD);
        possibleActions.add(MessageAction.MOVE_TO_TRASH);
        possibleActions.add(MessageAction.RESTORE);
        response.setPossibleActions(possibleActions);

        if (messageBox == null) {
            // throw new ValidationException();
        }
        if (query.getRelatedMember() != null) {
            final Member relatedMember = elementService.load(query.getRelatedMember().getId(), Element.Relationships.USER);
            //response.setRelatedMember(relatedMember);
            response.setRelatedMember(relatedMember);
           
        }

        query.fetch(Message.Relationships.FROM_MEMBER, Message.Relationships.TO_MEMBER, Message.Relationships.TO_GROUPS);

        // Store the required enums
//        RequestHelper.storeEnum(request, MessageBox.class, "messageBoxes");
//        RequestHelper.storeEnum(request, Message.RootType.class, "rootTypes");
        List<MessageBox> messageBoxList = new ArrayList();
        messageBoxList.add(MessageBox.INBOX);
        messageBoxList.add(MessageBox.SENT);
        messageBoxList.add(MessageBox.TRASH);
        response.setMessageBoxList(messageBoxList);

        List<Message.RootType> messageRoot = new ArrayList();
        messageRoot.add(Message.RootType.ADMIN);
        messageRoot.add(Message.RootType.MEMBER);
        messageRoot.add(Message.RootType.SYSTEM);
        response.setMessageRoot(messageRoot);

        if (LoggedUser.isAdministrator()) {
            // Get the categories
            AdminGroup adminGroup = LoggedUser.group();
            adminGroup = groupService.load(adminGroup.getId(), SystemGroup.Relationships.MESSAGE_CATEGORIES);
            //  response.setAttribute("categories", adminGroup.getMessageCategories());
            response.setAdminGroup(adminGroup);
           
            

        }

        // Check if can send a message
        boolean canSend = false;
        if (LoggedUser.isMember()) {
            canSend = permissionService.hasPermission(MemberPermission.MESSAGES_SEND_TO_MEMBER) || permissionService.hasPermission(MemberPermission.MESSAGES_SEND_TO_ADMINISTRATION) || permissionService.hasPermission(BrokerPermission.MESSAGES_SEND_TO_MEMBERS);
        } else if (LoggedUser.isOperator()) {
            canSend = permissionService.hasPermission(OperatorPermission.MESSAGES_SEND_TO_MEMBER) || permissionService.hasPermission(OperatorPermission.MESSAGES_SEND_TO_ADMINISTRATION);
        } else if (LoggedUser.isAdministrator()) {
            canSend = permissionService.hasPermission(AdminMemberPermission.MESSAGES_SEND_TO_MEMBER) || permissionService.hasPermission(AdminMemberPermission.MESSAGES_SEND_TO_GROUP);
        }
        response.setCanSend(canSend);

        // Check if can manage messages
        
        boolean canManage = false;
        if (LoggedUser.isMember()) {
            canManage = permissionService.hasPermission(MemberPermission.MESSAGES_MANAGE);
        } else if (LoggedUser.isOperator()) {
            canManage = permissionService.hasPermission(OperatorPermission.MESSAGES_MANAGE);
        } else if (LoggedUser.isAdministrator()) {
            canManage = permissionService.hasPermission(AdminMemberPermission.MESSAGES_MANAGE);
        }
        response.setCanManage(canManage);

        response.setStatus(0);
        response.setMessage("....List of the searchmessage");
        return response;
    }
}
