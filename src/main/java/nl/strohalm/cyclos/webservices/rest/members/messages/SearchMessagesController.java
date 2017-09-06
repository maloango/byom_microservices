/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.strohalm.cyclos.webservices.rest.members.messages;

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
import nl.strohalm.cyclos.utils.access.LoggedUser;
import nl.strohalm.cyclos.utils.binding.BeanBinder;
import nl.strohalm.cyclos.utils.binding.DataBinder;
import nl.strohalm.cyclos.utils.binding.DataBinderHelper;
import nl.strohalm.cyclos.utils.binding.PropertyBinder;
import nl.strohalm.cyclos.utils.query.QueryParameters;
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
    
    public static class SearchMessageRequest{
        private boolean advanced;
        private String messageBox;
        private String rootType;
        private String keywords;
        private long relatedMember;

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

        public long getRelatedMember() {
            return relatedMember;
        }

        public void setRelatedMember(long relatedMember) {
            this.relatedMember = relatedMember;
        }

     
        
        
    }

    public static class SearchMessagesResponse extends GenericResponse {

        private boolean advanced;
        private boolean canManage;
        private boolean canSend;
        private Member relatedMember;
        private AdminGroup adminGroup;

       
        
        public Member getRelatedMember() {
            return relatedMember;
        }

        public void setRelatedMember(Member relatedMember) {
            this.relatedMember = relatedMember;
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

//         public SearchMessagesResponse() {
//        
//        setQuery("messageBox", MessageBox.INBOX.name());
//    }
        public boolean isAdvanced() {
            return advanced;
        }

        public void setAdvanced(final boolean advanced) {
            this.advanced = advanced;
        }

    }

    @RequestMapping(value = "member/serchMessage", method = RequestMethod.POST)
    @ResponseBody
    public GenericResponse executeQuery(@RequestBody SearchMessageRequest request) {
        GenericResponse response = new GenericResponse();
        
        Map<String, Object> query=new HashMap<String, Object>();
        query.put("messageBox", request.getMessageBox());
        query.put("rootType", request.getRootType());
        query.put("relatedMember", request.relatedMember);
        query.put("keywords", request.getKeywords());
        query.put("advanced", request.isAdvanced());
        
        final MessageQuery queries = getDataBinder().readFromString(query);
        final List<Message> list = messageService.search(queries);
        //list.getRequest().setAttribute("messages", list);
        list.set(0, (Message) list);
        response.setStatus(0);
        response.setMessage("messange sent!!");
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

}
