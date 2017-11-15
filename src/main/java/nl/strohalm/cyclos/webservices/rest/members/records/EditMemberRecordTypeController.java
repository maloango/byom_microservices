/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.strohalm.cyclos.webservices.rest.members.records;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import nl.strohalm.cyclos.access.AdminSystemPermission;
import nl.strohalm.cyclos.entities.groups.Group;
import nl.strohalm.cyclos.entities.groups.GroupQuery;
import nl.strohalm.cyclos.entities.groups.MemberGroup;
import nl.strohalm.cyclos.entities.members.records.MemberRecordType;
import nl.strohalm.cyclos.utils.binding.BeanBinder;
import nl.strohalm.cyclos.utils.binding.DataBinder;
import nl.strohalm.cyclos.utils.binding.PropertyBinder;
import nl.strohalm.cyclos.utils.binding.SimpleCollectionBinder;
import nl.strohalm.cyclos.utils.conversion.IdConverter;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;
import nl.strohalm.cyclos.webservices.rest.GenericResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 *
 * @author Lue Infoservices
 */
@Controller
public class EditMemberRecordTypeController extends BaseRestController {

    private DataBinder<MemberRecordType> dataBinder;

    public static class MemberRecordTypeResponse extends GenericResponse {

        private boolean editable;
        private List<MemberRecordType.Layout> laoyout;
        private List<GroupEntity> groupList;

        public boolean isEditable() {
            return editable;
        }

        public void setEditable(boolean editable) {
            this.editable = editable;
        }

        public List<MemberRecordType.Layout> getLaoyout() {
            return laoyout;
        }

        public void setLaoyout(List<MemberRecordType.Layout> laoyout) {
            this.laoyout = laoyout;
        }

        public List<GroupEntity> getGroupList() {
            return groupList;
        }

        public void setGroupList(List<GroupEntity> groupList) {
            this.groupList = groupList;
        }

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

    @RequestMapping(value = "admin/editMemberRecordType", method = RequestMethod.GET)
    @ResponseBody
    public MemberRecordTypeResponse prepareForm() {
        MemberRecordTypeResponse response = new MemberRecordTypeResponse();
        boolean editable = permissionService.hasPermission(AdminSystemPermission.MEMBER_RECORD_TYPES_MANAGE);
        MemberRecordType memberRecordType;
        memberRecordType = new MemberRecordType();
        editable = true;
//        request.setAttribute("memberRecordType", memberRecordType);
//        request.setAttribute("editable", editable);
        //request.setAttribute("isInsert", isInsert);
        response.setEditable(editable);
        // Search groups and send to JSP
        final GroupQuery groupQuery = new GroupQuery();
        groupQuery.setNatures(Group.Nature.MEMBER, Group.Nature.BROKER, Group.Nature.ADMIN);
        groupQuery.setStatus(Group.Status.NORMAL);
        final List<? extends Group> groups = groupService.search(groupQuery);
        List<GroupEntity> groupList = new ArrayList();
        for (Group group : groups) {
            GroupEntity entity = new GroupEntity();
            entity.setId(group.getId());
            entity.setName(group.getName());
            groupList.add(entity);

        }
        //  request.setAttribute("groups", groups);
        response.setGroupList(groupList);

        // Send layouts enum to JSP
//        RequestHelper.storeEnum(request, MemberRecordType.Layout.class, "layouts");
        List<MemberRecordType.Layout> layouts = new ArrayList();
        layouts.add(MemberRecordType.Layout.FLAT);
        layouts.add(MemberRecordType.Layout.LIST);

        response.setLaoyout(layouts);
        response.setStatus(0);
        return response;
    }

    @RequestMapping(value = "admin/editMemberRecordType", method = RequestMethod.POST)
    @ResponseBody
    public GenericResponse submit(@RequestBody MemberRecordTypeParameters params) {
        GenericResponse response = new GenericResponse();
        Map<String, Object> query = new HashMap();
        if (params.getId() != null && params.getId() > 0L) {
            query.put("id", params.getId());
        }
        query.put("name", params.getName());
        query.put("label", params.getLabel());
        query.put("description", params.getDescription());
        query.put("groups", params.getGroups());
        query.put("layout", params.layout);
        query.put("editable", params.isEditable());
        query.put("showMenuItem", params.isShowMenuItem());
        MemberRecordType memberRecordType = getDataBinder().readFromString(query);

        final boolean isInsert = memberRecordType.isTransient();
        memberRecordType = memberRecordTypeService.save(memberRecordType);
        if (isInsert) {
            response.setMessage("memberRecordType.inserted");
        } else {
            response.setMessage("memberRecordType.modified");
        }
        response.setStatus(0);
        return response;
    }

    public static class MemberRecordTypeParameters {

        private Long id;
        private String name;
        private String label;
        private String description;
        private Long[] groups;
        private String layout;
        private boolean editable;
        private boolean showMenuItem;

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

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public Long[] getGroups() {
            return groups;
        }

        public void setGroups(Long[] groups) {
            this.groups = groups;
        }

        public String getLayout() {
            return layout;
        }

        public void setLayout(String layout) {
            this.layout = layout;
        }

        public boolean isEditable() {
            return editable;
        }

        public void setEditable(boolean editable) {
            this.editable = editable;
        }

        public boolean isShowMenuItem() {
            return showMenuItem;
        }

        public void setShowMenuItem(boolean showMenuItem) {
            this.showMenuItem = showMenuItem;
        }

    }

    private DataBinder<MemberRecordType> getDataBinder() {
        if (dataBinder == null) {
            final BeanBinder<MemberRecordType> binder = BeanBinder.instance(MemberRecordType.class);
            binder.registerBinder("id", PropertyBinder.instance(Long.class, "id", IdConverter.instance()));
            binder.registerBinder("name", PropertyBinder.instance(String.class, "name"));
            binder.registerBinder("label", PropertyBinder.instance(String.class, "label"));
            binder.registerBinder("description", PropertyBinder.instance(String.class, "description"));
            binder.registerBinder("groups", SimpleCollectionBinder.instance(MemberGroup.class, "groups"));
            binder.registerBinder("layout", PropertyBinder.instance(MemberRecordType.Layout.class, "layout"));
            binder.registerBinder("editable", PropertyBinder.instance(Boolean.TYPE, "editable"));
            binder.registerBinder("showMenuItem", PropertyBinder.instance(Boolean.TYPE, "showMenuItem"));
            dataBinder = binder;
        }
        return dataBinder;
    }

    @RequestMapping(value = "admin/GetMemberRecordTypeById/{id}", method = RequestMethod.GET)
    @ResponseBody
    public SingleMemberRecordTypeResponse getMemberRecord(@PathVariable("id") Long id) {
        SingleMemberRecordTypeResponse response = new SingleMemberRecordTypeResponse();
        MemberRecordType memberRecordTypes = memberRecordTypeService.load(id, MemberRecordType.Relationships.GROUPS);
        MemberRecordTypeEntity entity = new MemberRecordTypeEntity();
        entity.setId(memberRecordTypes.getId());
        entity.setDescription(memberRecordTypes.getDescription());
        entity.setEditable(memberRecordTypes.isEditable());
        List<GroupEntity> groups = new ArrayList();
        for (Group group : memberRecordTypes.getGroups()) {
            GroupEntity groupEntity = new GroupEntity();
            groupEntity.setId(group.getId());
            groupEntity.setName(group.getName());
            groups.add(groupEntity);
        }
        entity.setGroups(groups);
        entity.setLabel(memberRecordTypes.getLabel());
        entity.setLayout(memberRecordTypes.getLayout());
        entity.setName(memberRecordTypes.getName());
        entity.setShowMenuItem(memberRecordTypes.isShowMenuItem());
        response.setMemberRecordTypes(entity);
        response.setStatus(0);
        return response;

    }

    public static class SingleMemberRecordTypeResponse extends GenericResponse {

        private MemberRecordTypeEntity memberRecordTypes;

        public MemberRecordTypeEntity getMemberRecordTypes() {
            return memberRecordTypes;
        }

        public void setMemberRecordTypes(MemberRecordTypeEntity memberRecordTypes) {
            this.memberRecordTypes = memberRecordTypes;
        }

    }

    public static class MemberRecordTypeEntity {

        private Long id;
        private String name;
        private String label;
        private String description;
        private Collection<GroupEntity> groups;
        private MemberRecordType.Layout layout;
        private boolean editable;
        private boolean showMenuItem;

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

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public Collection<GroupEntity> getGroups() {
            return groups;
        }

        public void setGroups(Collection<GroupEntity> groups) {
            this.groups = groups;
        }

        public MemberRecordType.Layout getLayout() {
            return layout;
        }

        public void setLayout(MemberRecordType.Layout layout) {
            this.layout = layout;
        }

        public boolean isEditable() {
            return editable;
        }

        public void setEditable(boolean editable) {
            this.editable = editable;
        }

        public boolean isShowMenuItem() {
            return showMenuItem;
        }

        public void setShowMenuItem(boolean showMenuItem) {
            this.showMenuItem = showMenuItem;
        }

    }

}
