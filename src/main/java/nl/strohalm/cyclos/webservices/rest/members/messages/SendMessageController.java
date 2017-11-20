/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.strohalm.cyclos.webservices.rest.members.messages;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import nl.strohalm.cyclos.access.AdminMemberPermission;
import nl.strohalm.cyclos.annotations.Inject;
import nl.strohalm.cyclos.controls.ActionContext;
import nl.strohalm.cyclos.entities.groups.Group;
import nl.strohalm.cyclos.entities.groups.GroupQuery;
import nl.strohalm.cyclos.entities.groups.MemberGroup;
import nl.strohalm.cyclos.entities.members.Member;
import nl.strohalm.cyclos.entities.members.messages.Message;
import nl.strohalm.cyclos.entities.members.messages.MessageCategory;
import nl.strohalm.cyclos.entities.members.preferences.NotificationPreference;
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
import nl.strohalm.cyclos.utils.access.LoggedUser;
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
public class SendMessageController extends BaseRestController {

    private Map<Class<? extends SendMessageDTO>, DataBinder<? extends SendMessageDTO>> dataBindersByType;

    public static enum SendTo {
        MEMBER, ADMIN, GROUP, BROKERED_MEMBERS
    }

    public static class GroupEntity {

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

    public static class SendMessageResponse extends GenericResponse {

        private List<GroupEntity> groupList;
        private List<SendTo> sendTo;

        public List<SendTo> getSendTo() {
            return sendTo;
        }

        public void setSendTo(List<SendTo> sendTo) {
            this.sendTo = sendTo;
        }

        public List<GroupEntity> getGroupList() {
            return groupList;
        }

        public void setGroupList(List<GroupEntity> groupList) {
            this.groupList = groupList;
        }

    }

    @RequestMapping(value = "admin/sendMessage", method = RequestMethod.GET)
    @ResponseBody
    public SendMessageResponse prepareForm() throws Exception {
        SendMessageResponse response = new SendMessageResponse();
        final List<SendTo> sendTo = new ArrayList<SendTo>();

        final GroupQuery gq = new GroupQuery();
        gq.setNatures(Group.Nature.MEMBER, Group.Nature.BROKER);
        gq.setStatus(Group.Status.NORMAL);
        //request.setAttribute("groups", groupService.search(gq));
        List<GroupEntity> groups = new ArrayList();
        for (Group group : groupService.search(gq)) {
            GroupEntity entity = new GroupEntity();
            entity.setId(group.getId());
            entity.setName(group.getName());
            groups.add(entity);
        }
        response.setGroupList(groups);
        if (permissionService.hasPermission(AdminMemberPermission.MESSAGES_SEND_TO_MEMBER)) {
            sendTo.add(SendTo.MEMBER);
        }
        if (permissionService.hasPermission(AdminMemberPermission.MESSAGES_SEND_TO_GROUP)) {
            sendTo.add(SendTo.GROUP);
        }
        response.setSendTo(sendTo);
        response.setStatus(0);
        return response;

    }

    public static class SendMessageParameters {

        private String sendTo;
        private String body;
        private String subject;
        private Long category;
        private Long toMember;
        private List<Long> toGroups;

        public List<Long> getToGroups() {
            return toGroups;
        }

        public void setToGroups(List<Long> toGroups) {
            this.toGroups = toGroups;
        }

        public String getSendTo() {
            return sendTo;
        }

        public void setSendTo(String sendTo) {
            this.sendTo = sendTo;
        }

        public String getBody() {
            return body;
        }

        public void setBody(String body) {
            this.body = body;
        }

        public String getSubject() {
            return subject;
        }

        public void setSubject(String subject) {
            this.subject = subject;
        }

        public Long getCategory() {
            return category;
        }

        public void setCategory(Long category) {
            this.category = category;
        }

        public Long getToMember() {
            return toMember;
        }

        public void setToMember(Long toMember) {
            this.toMember = toMember;
        }

    }

    @RequestMapping(value = "admin/sendMessage", method = RequestMethod.POST)
    @ResponseBody
    public GenericResponse send(@RequestBody SendMessageParameters params) {
        GenericResponse response = new GenericResponse();
        final long toMemberId = params.getToMember();

        // Send the message
        final SendMessageDTO dto = resolveDTO(params);

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
            }
            response.setMessage(key);
        } catch (final MemberWontReceiveNotificationException e) {
            response.setMessage("message.error.memberWontReceiveNotification");
        }

        // Go back to the correct location
//        if (dto.getInReplyTo() == null && toMemberId > 0L) {
//            return ActionHelper.redirectWithParam(context.getRequest(), context.findForward("backToProfile"), "memberId", toMemberId);
//        }
        response.setStatus(0);
        return response;
    }

    private SendMessageDTO resolveDTO(final SendMessageParameters params) {

        Class<? extends SendMessageDTO> dtoClass = null;
        final SendTo sendTo = CoercionHelper.coerce(SendTo.class, params.getSendTo());
        if (sendTo == null) {
            throw new ValidationException();
        }
        // Test and validate who to send the message
        switch (sendTo) {
            case MEMBER:
                dtoClass = SendDirectMessageToMemberDTO.class;
                break;
            case GROUP:
                if (!LoggedUser.isAdministrator()) {
                    throw new ValidationException();
                }
                dtoClass = SendMessageToGroupDTO.class;
                break;
            case BROKERED_MEMBERS:
                if (!LoggedUser.isBroker()) {
                    throw new ValidationException();
                }
                dtoClass = SendMessageFromBrokerToMembersDTO.class;
                break;
            case ADMIN:
                if (!(LoggedUser.isMember() || LoggedUser.isOperator())) {
                    throw new ValidationException();
                }
                dtoClass = SendMessageToAdminDTO.class;
                break;
            default:
                throw new ValidationException();
        }

        Map<String, Object> query = new HashMap();
        if (params.getToMember() != null && params.getToMember() > 0L) {
            query.put("toMember", params.getToMember());
        }
        if (params.getToGroups() != null && !params.getToGroups().isEmpty()) {
            query.put("toGroups", params.getToGroups());
        }
        query.put("category", params.getCategory());
        query.put("subject", params.getSubject());

        final SendMessageDTO dto = getDataBinderFor(dtoClass).readFromString(query);
        if (dto.isHtml()) {
            dto.setBody(HtmlConverter.instance().valueOf("" + params.getBody()));
        } else {
            dto.setBody(StringTrimmerConverter.instance().valueOf("" + params.getBody()));
        }
        return dto;
    }

    private <T extends SendMessageDTO> DataBinder<T> getDataBinderFor(final Class<T> type) {
        if (dataBindersByType == null) {
            dataBindersByType = new HashMap<Class<? extends SendMessageDTO>, DataBinder<? extends SendMessageDTO>>();
            final BeanBinder<SendDirectMessageToMemberDTO> toMemberBinder = basicDataBinderFor(SendDirectMessageToMemberDTO.class);
            toMemberBinder.registerBinder("toMember", PropertyBinder.instance(Member.class, "toMember"));
            dataBindersByType.put(SendDirectMessageToMemberDTO.class, toMemberBinder);

            final BeanBinder<SendMessageToAdminDTO> toAdminBinder = basicDataBinderFor(SendMessageToAdminDTO.class);
            dataBindersByType.put(SendMessageToAdminDTO.class, toAdminBinder);

            final BeanBinder<SendMessageFromBrokerToMembersDTO> toBrokeredBinder = basicDataBinderFor(SendMessageFromBrokerToMembersDTO.class);
            dataBindersByType.put(SendMessageFromBrokerToMembersDTO.class, toBrokeredBinder);

            final BeanBinder<SendMessageToGroupDTO> toGroupBinder = basicDataBinderFor(SendMessageToGroupDTO.class);
            toGroupBinder.registerBinder("toGroups", SimpleCollectionBinder.instance(MemberGroup.class, "toGroups"));
            dataBindersByType.put(SendMessageToGroupDTO.class, toGroupBinder);
        }
        return (DataBinder<T>) dataBindersByType.get(type);
    }

    private <T extends SendMessageDTO> BeanBinder<T> basicDataBinderFor(final Class<T> type) {
        final BeanBinder<T> binder = BeanBinder.instance(type);
        // The body is not read here, as it can be either plain text or html
        binder.registerBinder("category", PropertyBinder.instance(MessageCategory.class, "category"));
        binder.registerBinder("subject", PropertyBinder.instance(String.class, "subject"));
        binder.registerBinder("inReplyTo", PropertyBinder.instance(Message.class, "inReplyTo"));
        binder.registerBinder("html", PropertyBinder.instance(Boolean.TYPE, "html"));
        return binder;
    }

}
