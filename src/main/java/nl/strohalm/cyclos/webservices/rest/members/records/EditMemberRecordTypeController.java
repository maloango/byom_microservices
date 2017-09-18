package nl.strohalm.cyclos.webservices.rest.members.records;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import nl.strohalm.cyclos.access.AdminSystemPermission;
import nl.strohalm.cyclos.annotations.Inject;
import nl.strohalm.cyclos.controls.ActionContext;
//import nl.strohalm.cyclos.controls.members.records.EditMemberRecordTypeForm;
import nl.strohalm.cyclos.entities.groups.Group;
import nl.strohalm.cyclos.entities.groups.GroupQuery;
import nl.strohalm.cyclos.entities.groups.MemberGroup;
import nl.strohalm.cyclos.entities.members.records.MemberRecordType;
import nl.strohalm.cyclos.services.elements.ElementService;
import nl.strohalm.cyclos.services.elements.MemberRecordTypeService;
import nl.strohalm.cyclos.services.groups.GroupService;
import nl.strohalm.cyclos.services.permissions.PermissionService;
import nl.strohalm.cyclos.utils.ActionHelper;
import nl.strohalm.cyclos.utils.RequestHelper;
import nl.strohalm.cyclos.utils.binding.BeanBinder;
import nl.strohalm.cyclos.utils.binding.DataBinder;
import nl.strohalm.cyclos.utils.binding.PropertyBinder;
import nl.strohalm.cyclos.utils.binding.SimpleCollectionBinder;
import nl.strohalm.cyclos.utils.conversion.IdConverter;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;
import nl.strohalm.cyclos.webservices.rest.GenericResponse;

import org.apache.struts.action.ActionForward;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class EditMemberRecordTypeController extends BaseRestController {

    public static class EditMemberRecordRequest {

        private long id;
        private String name;
        private String label;
        private String description;
        private Long[] groups;
        private String layout;
        private boolean editable;
        private boolean showMenuItem;

        public Long[] getGroups() {
            return groups;
        }

        public void setGroups(Long[] groups) {
            this.groups = groups;
        }

        public long getId() {
            return id;
        }

        public void setId(long id) {
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

    @RequestMapping(value = "admin/editMemberRecordType", method = RequestMethod.POST)
    @ResponseBody
    public GenericResponse editMemberRecord(@RequestBody EditMemberRecordRequest request) throws Exception {
        GenericResponse response = new GenericResponse();
        MemberRecordType memberRecordType = new MemberRecordType();
        memberRecordType.setId(request.getId());
        memberRecordType.setName(request.getName());
        memberRecordType.setLabel(request.getLabel());
        memberRecordType.setDescription(request.getDescription());
        memberRecordType.setLayout(MemberRecordType.Layout.valueOf(request.getLayout()));
        memberRecordType.setGroups(getGoups(request.getGroups()));
        memberRecordType.setEditable(request.isEditable());
        memberRecordType.setShowMenuItem(request.isShowMenuItem());
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

    private List<Group> getGoups(Long[] ids) {
        List<Group> groups = new ArrayList<Group>();

        for (long id : ids) {
            groups.add(groupService.load(id, Group.Relationships.ELEMENTS));
        }

        return groups;
    }

}
