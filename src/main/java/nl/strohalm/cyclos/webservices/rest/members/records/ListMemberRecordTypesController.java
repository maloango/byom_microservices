package nl.strohalm.cyclos.webservices.rest.members.records;

import java.util.ArrayList;
import java.util.List;

import nl.strohalm.cyclos.access.AdminSystemPermission;
import nl.strohalm.cyclos.annotations.Inject;
import nl.strohalm.cyclos.entities.members.records.MemberRecordType;
import nl.strohalm.cyclos.entities.members.records.MemberRecordTypeQuery;
import nl.strohalm.cyclos.services.elements.MemberRecordTypeService;
import nl.strohalm.cyclos.services.permissions.PermissionService;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;
import nl.strohalm.cyclos.webservices.rest.GenericResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class ListMemberRecordTypesController extends BaseRestController {

    public static class ListMemberRecordTypesResponse extends GenericResponse {

        private List<MemberRecordType> memberRecordTypes;
        private boolean editable;

        public List<MemberRecordType> getMemberRecordTypes() {
            return memberRecordTypes;
        }

        public void setMemberRecordTypes(List<MemberRecordType> memberRecordTypes) {
            this.memberRecordTypes = memberRecordTypes;
        }

        public boolean isEditable() {
            return editable;
        }

        public void setEditable(boolean editable) {
            this.editable = editable;
        }

    }

    @RequestMapping(value = "admin/listMemberRecordTypes", method = RequestMethod.GET)
    @ResponseBody
    protected ListMemberRecordTypesResponse executeAction() throws Exception {
        ListMemberRecordTypesResponse response = new ListMemberRecordTypesResponse();
        final List<MemberRecordType> memberRecordTypes = memberRecordTypeService.search(new MemberRecordTypeQuery());
        response.setEditable(permissionService.hasPermission(AdminSystemPermission.MEMBER_RECORD_TYPES_MANAGE));
        List<MemberRecordType> list=new ArrayList();
        for(MemberRecordType memberList:memberRecordTypes){
            MemberRecordType memberRecord=new MemberRecordType();
            memberRecord.setId(memberList.getId());
            memberRecord.setName(memberList.getName());
            list.add(memberRecord);
        }
        response.setMemberRecordTypes(list);
        return response;

    }
    
    public static class ListMember{
        private long id;
        private String name;

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
        
    }

}
