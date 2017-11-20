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
import nl.strohalm.cyclos.controls.ActionContext;
import nl.strohalm.cyclos.entities.groups.AdminGroup;
import nl.strohalm.cyclos.entities.groups.SystemGroup;
import nl.strohalm.cyclos.entities.members.Element;
import nl.strohalm.cyclos.entities.members.Member;
import nl.strohalm.cyclos.entities.members.messages.Message;
import nl.strohalm.cyclos.entities.members.messages.MessageBox;
import nl.strohalm.cyclos.entities.members.messages.MessageCategory;
import nl.strohalm.cyclos.entities.members.messages.MessageQuery;
import nl.strohalm.cyclos.services.elements.ElementService;
import nl.strohalm.cyclos.services.elements.MessageService;
import nl.strohalm.cyclos.services.groups.GroupService;
import nl.strohalm.cyclos.services.permissions.PermissionService;
import nl.strohalm.cyclos.utils.RequestHelper;
import nl.strohalm.cyclos.utils.access.LoggedUser;
import nl.strohalm.cyclos.utils.binding.BeanBinder;
import nl.strohalm.cyclos.utils.binding.DataBinder;
import nl.strohalm.cyclos.utils.binding.DataBinderHelper;
import nl.strohalm.cyclos.utils.binding.PropertyBinder;
import nl.strohalm.cyclos.utils.query.QueryParameters;
import nl.strohalm.cyclos.utils.validation.ValidationException;
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
public class SearchMessagesController extends BaseRestController {

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

        private String messageBox;
        private Long category;
        private String keywords;
        private Long relatedMember;

        public Long getCategory() {
            return category;
        }

        public void setCategory(Long category) {
            this.category = category;
        }

        public Long getRelatedMember() {
            return relatedMember;
        }

        public void setRelatedMember(Long relatedMember) {
            this.relatedMember = relatedMember;
        }

        public String getMessageBox() {
            return messageBox;
        }

        public void setMessageBox(String messageBox) {
            this.messageBox = messageBox;
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

    @RequestMapping(value = "admin/searchMessage", method = RequestMethod.POST)
    @ResponseBody
    public GenericResponse executeQuery(@RequestBody SearchMessageRequest params) {
        GenericResponse response = new GenericResponse();
        try {
            final MessageQuery queries = new MessageQuery();
            queries.setKeywords(params.getKeywords());
            queries.setRelatedMember((Member) elementService.load(params.getRelatedMember(), Element.Relationships.USER));
            queries.setMessageBox(MessageBox.valueOf(params.getMessageBox()));
            if (params.getCategory() != null) {
                queries.setCategory(messageCategoryService.load(params.getCategory()));
            }

            final List<Message> list = messageService.search(queries);
            System.out.println("....." + list.size());
            for (int i = 0; i < list.size(); i++) {
                System.out.println("------" + list.get(i));
            }
            // list.getRequest.setAttribute("messages", list);

        } catch (Exception e) {
            e.printStackTrace();

        }
        response.setStatus(0);
        response.setMessage("Message displayed!!");
        return response;
    }

//    @RequestMapping(value = "member/serchMessage", method = RequestMethod.GET)
//    @ResponseBody
//    public SearchMessagesResponse prepareForm() {
//        // final HttpServletRequest request = context.getRequest();
////        final SearchMessagesForm form = context.getForm();
//
//        SearchMessagesResponse response = new SearchMessagesResponse();
//        
//        // Resolve the query object
//        
//        final MessageQuery query = getDataBinder().readFromString(form.getQuery());
//       
//        final MessageBox messageBox = query.getMessageBox();
//        if (messageBox == null) {
//           // throw new ValidationException();
//        }
//        if (query.getRelatedMember() != null) {
//            final Member relatedMember = elementService.load(query.getRelatedMember().getId(), Element.Relationships.USER);
//            response.setRelatedMember(relatedMember);
//        }
//
//        query.fetch(Message.Relationships.FROM_MEMBER, Message.Relationships.TO_MEMBER, Message.Relationships.TO_GROUPS);
//
//       // response.setMessageBox(messageBox);
//
//        // Store the required enums
////        RequestHelper.storeEnum(request, MessageBox.class, "messageBoxes");
////        RequestHelper.storeEnum(request, Message.RootType.class, "rootTypes");
//        if (LoggedUser.isAdministrator()) {
//            // Get the categories
//            AdminGroup adminGroup = LoggedUser.group();
//            adminGroup = groupService.load(adminGroup.getId(), SystemGroup.Relationships.MESSAGE_CATEGORIES);
//            //  response.setAttribute("categories", adminGroup.getMessageCategories());
//            response.setAdminGroup(adminGroup);
//
//        }
//
//        // Check if can send a message
//        boolean canSend = false;
//        if (LoggedUser.isMember()) {
//            canSend = permissionService.hasPermission(MemberPermission.MESSAGES_SEND_TO_MEMBER) || permissionService.hasPermission(MemberPermission.MESSAGES_SEND_TO_ADMINISTRATION) || permissionService.hasPermission(BrokerPermission.MESSAGES_SEND_TO_MEMBERS);
//        } else if (LoggedUser.isOperator()) {
//            canSend = permissionService.hasPermission(OperatorPermission.MESSAGES_SEND_TO_MEMBER) || permissionService.hasPermission(OperatorPermission.MESSAGES_SEND_TO_ADMINISTRATION);
//        } else if (LoggedUser.isAdministrator()) {
//            canSend = permissionService.hasPermission(AdminMemberPermission.MESSAGES_SEND_TO_MEMBER) || permissionService.hasPermission(AdminMemberPermission.MESSAGES_SEND_TO_GROUP);
//        }
//        response.setCanSend(canSend);
//
//        // Check if can manage messages
//        boolean canManage = false;
//        if (LoggedUser.isMember()) {
//            canManage = permissionService.hasPermission(MemberPermission.MESSAGES_MANAGE);
//        } else if (LoggedUser.isOperator()) {
//            canManage = permissionService.hasPermission(OperatorPermission.MESSAGES_MANAGE);
//        } else if (LoggedUser.isAdministrator()) {
//            canManage = permissionService.hasPermission(AdminMemberPermission.MESSAGES_MANAGE);
//        }
//        response.setCanManage(canManage);
//
//        response.setStatus(0);
//        return response;
//    }
    // @Override
    protected boolean willExecuteQuery(final ActionContext context, final QueryParameters queryParameters) throws Exception {
        return true;
    }

    public static class MessageList extends GenericResponse {

        private List<MessageEntity> messages;
        private List<MessageBox> messageBox;
        private List<Message.RootType> rootTypes;
        private List<CategoryEntity> messageCategory;
        private boolean canSend;
        private boolean canManage;

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

        public List<CategoryEntity> getMessageCategory() {
            return messageCategory;
        }

        public void setMessageCategory(List<CategoryEntity> messageCategory) {
            this.messageCategory = messageCategory;
        }

        public List<Message.RootType> getRootTypes() {
            return rootTypes;
        }

        public void setRootTypes(List<Message.RootType> rootTypes) {
            this.rootTypes = rootTypes;
        }

        public List<MessageBox> getMessageBox() {
            return messageBox;
        }

        public void setMessageBox(List<MessageBox> messageBox) {
            this.messageBox = messageBox;
        }

        public List<MessageEntity> getMessages() {
            return messages;
        }

        public void setMessages(List<MessageEntity> messages) {
            this.messages = messages;
        }

    }

    public static class MessageEntity {

        private Long id;
        private Calendar date;
        private Member fromMember;
        private String subject;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public Calendar getDate() {
            return date;
        }

        public void setDate(Calendar date) {
            this.date = date;
        }

        public Member getFromMember() {
            return fromMember;
        }

        public void setFromMember(Member fromMember) {
            this.fromMember = fromMember;
        }

        public String getSubject() {
            return subject;
        }

        public void setSubject(String subject) {
            this.subject = subject;
        }
    }

    public static class CategoryEntity {

        private Long id;
        private String name;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

    }

    @RequestMapping(value = "admin/searchMessage", method = RequestMethod.GET)
    @ResponseBody
    public MessageList prepareForm() {
        MessageList response = new MessageList();

        // Resolve the query object
//        final MessageBox messageBox = query.getMessageBox();
//        if (messageBox == null) {
//            throw new ValidationException();
//        }
//        if (query.getRelatedMember() != null) {
//            final Member relatedMember = elementService.load(query.getRelatedMember().getId(), Element.Relationships.USER);
//            request.setAttribute("relatedMember", relatedMember);
//        }
//
//        request.setAttribute("messageBox", messageBox);
        List<MessageCategory> categoryList = new ArrayList();

        //response.setMessage(message);
        // Store the required enums
        // RequestHelper.storeEnum(request, MessageBox.class, "messageBoxes");
        List<MessageBox> messageBox = new ArrayList();
        messageBox.add(MessageBox.SENT);
        messageBox.add(MessageBox.INBOX);
        messageBox.add(MessageBox.TRASH);
        response.setMessageBox(messageBox);

        //RequestHelper.storeEnum(request, Message.RootType.class, "rootTypes");
        List<Message.RootType> rootTypes = new ArrayList();
        rootTypes.add(Message.RootType.MEMBER);
        rootTypes.add(Message.RootType.ADMIN);
        rootTypes.add(Message.RootType.SYSTEM);
        response.setRootTypes(rootTypes);

        if (LoggedUser.isAdministrator()) {
            // Get the categories
            AdminGroup adminGroup = LoggedUser.group();
            adminGroup = groupService.load(adminGroup.getId(), SystemGroup.Relationships.MESSAGE_CATEGORIES);
            //  request.setAttribute("categories", adminGroup.getMessageCategories());
            List<CategoryEntity> categories = new ArrayList();
            for (MessageCategory category : adminGroup.getMessageCategories()) {
                CategoryEntity entity = new CategoryEntity();
                entity.setId(category.getId());
                entity.setName(category.getName());
                categoryList.add(messageCategoryService.load(category.getId()));
                categories.add(entity);
            }
            response.setMessageCategory(categories);

        }
        final MessageQuery query = new MessageQuery();
        //query.fetch(Message.Relationships.FROM_MEMBER, Message.Relationships.TO_MEMBER, Message.Relationships.TO_GROUPS);
        query.setMessageBox(MessageBox.INBOX);
        query.setCategories(categoryList);
        final List<Message> list = messageService.search(query);
        System.out.println("-----list:" + list);
        List<MessageEntity> messages = new ArrayList();
        for (Message msg : list) {
            MessageEntity entity = new MessageEntity();
            entity.setId(msg.getId());
            entity.setDate(msg.getDate());
            entity.setFromMember(msg.getFromMember());
            entity.setSubject(msg.getSubject());
            messages.add(entity);
        }
        response.setMessages(messages);

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
        return response;

    }

}
