/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.strohalm.cyclos.webservices.rest.members.records;

/**
 *
 * @author Lue Infoservices
 */
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

        private List<MemberRecord> memberRecords;
        private boolean editable;

        public List<MemberRecord> getMemberRecords() {
            return memberRecords;
        }

        public void setMemberRecords(List<MemberRecord> memberRecords) {
            this.memberRecords = memberRecords;
        }

        public boolean isEditable() {
            return editable;
        }

        public void setEditable(boolean editable) {
            this.editable = editable;
        }

    }

    public static class MemberRecord {

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

    @RequestMapping(value = "admin/listMemberRecordTypes", method = RequestMethod.GET)
    @ResponseBody
    protected ListMemberRecordTypesResponse executeAction() throws Exception {
        ListMemberRecordTypesResponse response = new ListMemberRecordTypesResponse();
        final List<MemberRecordType> memberRecordTypes = memberRecordTypeService.search(new MemberRecordTypeQuery());
        List<MemberRecord> memberRecords = new ArrayList();
        for (MemberRecordType type : memberRecordTypes) {
            MemberRecord record = new MemberRecord();
            record.setId(type.getId());
            record.setName(type.getName());
            memberRecords.add(record);
        }
        response.setMemberRecords(memberRecords);
        response.setEditable(permissionService.hasPermission(AdminSystemPermission.MEMBER_RECORD_TYPES_MANAGE));
        response.setStatus(0);
        return response;
    }
}
