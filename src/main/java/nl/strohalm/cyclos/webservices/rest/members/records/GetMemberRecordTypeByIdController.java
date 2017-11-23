/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.strohalm.cyclos.webservices.rest.members.records;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import nl.strohalm.cyclos.entities.groups.Group;
import nl.strohalm.cyclos.entities.members.records.MemberRecordType;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;
import nl.strohalm.cyclos.webservices.rest.GenericResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 *
 * @author Lue Infoservices
 */
@Controller
public class GetMemberRecordTypeByIdController extends BaseRestController {

    public static class MemberRecordTypeResponse extends GenericResponse {

        private MemberRecordEntity memberRecordType;

        public MemberRecordEntity getMemberRecordType() {
            return memberRecordType;
        }

        public void setMemberRecordType(MemberRecordEntity memberRecordType) {
            this.memberRecordType = memberRecordType;
        }

    }

    public static class MemberRecordEntity {

        private Long id;
        private String name;
        private String label;
        private String description;
        private Collection<GroupEntity> groups;
        private boolean showMenuItem;
        private boolean editable;
        private MemberRecordType.Layout layout;

        public MemberRecordType.Layout getLayout() {
            return layout;
        }

        public void setLayout(MemberRecordType.Layout layout) {
            this.layout = layout;
        }

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



        public boolean isShowMenuItem() {
            return showMenuItem;
        }

        public void setShowMenuItem(boolean showMenuItem) {
            this.showMenuItem = showMenuItem;
        }

        public boolean isEditable() {
            return editable;
        }

        public void setEditable(boolean editable) {
            this.editable = editable;
        }
    }
    public static class GroupEntity{
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

    @RequestMapping(value = "admin/getMemberRecordTypeById/{id}", method = RequestMethod.GET)
    @ResponseBody
    public MemberRecordTypeResponse getRecord(@PathVariable("id") Long id) {
        MemberRecordTypeResponse response = new MemberRecordTypeResponse();
        final MemberRecordType memberRecordType = memberRecordTypeService.load(id, MemberRecordType.Relationships.GROUPS);
        MemberRecordEntity entity = new MemberRecordEntity();

        entity.setDescription(memberRecordType.getDescription());
        entity.setName(memberRecordType.getName());
        entity.setId(memberRecordType.getId());
        entity.setLabel(memberRecordType.getLabel());
        List<GroupEntity> groups=new ArrayList();
        for(Group group:memberRecordType.getGroups()){
            GroupEntity groupEntity=new GroupEntity();
          groupEntity.setId(group.getId());
          groupEntity.setName(group.getName());
          groups.add(groupEntity);
        }
        entity.setGroups(groups);
        entity.setLayout(memberRecordType.getLayout());
        entity.setShowMenuItem(memberRecordType.isShowMenuItem());
        entity.setEditable(memberRecordType.isEditable());

        response.setMemberRecordType(entity);
        response.setStatus(0);
        return response;

    }
}
