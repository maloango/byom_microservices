/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.strohalm.cyclos.webservices.rest.members;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import nl.strohalm.cyclos.access.AdminMemberPermission;
import nl.strohalm.cyclos.annotations.Inject;
import nl.strohalm.cyclos.entities.customization.fields.CustomFieldValue;
import nl.strohalm.cyclos.entities.customization.fields.MemberCustomField;
import nl.strohalm.cyclos.entities.customization.fields.MemberCustomFieldValue;
import nl.strohalm.cyclos.entities.groups.AdminGroup;
import nl.strohalm.cyclos.entities.groups.Group;
import nl.strohalm.cyclos.entities.groups.GroupFilter;
import nl.strohalm.cyclos.entities.groups.GroupFilterQuery;
import nl.strohalm.cyclos.entities.groups.GroupQuery;
import nl.strohalm.cyclos.entities.groups.MemberGroup;
import nl.strohalm.cyclos.entities.members.Element;
import nl.strohalm.cyclos.entities.members.ElementQuery;
import nl.strohalm.cyclos.entities.members.FullTextElementQuery;
import nl.strohalm.cyclos.entities.members.FullTextMemberQuery;
import nl.strohalm.cyclos.entities.members.Member;
import nl.strohalm.cyclos.entities.members.MemberQuery;
import nl.strohalm.cyclos.entities.members.Operator;
import nl.strohalm.cyclos.entities.settings.LocalSettings;
import nl.strohalm.cyclos.services.customization.MemberCustomFieldService;
import nl.strohalm.cyclos.services.elements.ElementService;
import nl.strohalm.cyclos.services.groups.GroupFilterService;
import nl.strohalm.cyclos.services.groups.GroupService;
import nl.strohalm.cyclos.services.permissions.PermissionService;
import nl.strohalm.cyclos.utils.CustomFieldHelper;
import nl.strohalm.cyclos.utils.Period;
import nl.strohalm.cyclos.utils.access.LoggedUser;
import nl.strohalm.cyclos.utils.binding.DataBinder;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;
import nl.strohalm.cyclos.webservices.rest.GenericResponse;
import org.apache.commons.collections.CollectionUtils;
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
public class SearchMembersController extends BaseRestController{
    protected MemberCustomFieldService memberCustomFieldService;
    protected GroupFilterService       groupFilterService;

    protected CustomFieldHelper        customFieldHelper;
    private PermissionService permissionService;
    private ElementService elementService;
    private GroupService groupService;

    public void setPermissionService(PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    public void setElementService(ElementService elementService) {
        this.elementService = elementService;
    }

    public void setGroupService(GroupService groupService) {
        this.groupService = groupService;
    }
    
    

    public GroupFilterService getGroupFilterService() {
        return groupFilterService;
    }

    public MemberCustomFieldService getMemberCustomFieldService() {
        return memberCustomFieldService;
    }

    @Inject
    public void setCustomFieldHelper(final CustomFieldHelper customFieldHelper) {
        this.customFieldHelper = customFieldHelper;
    }

    @Inject
    public void setGroupFilterService(final GroupFilterService groupFilterService) {
        this.groupFilterService = groupFilterService;
    }

    @Inject
    public void setMemberCustomFieldService(final MemberCustomFieldService memberCustomFieldService) {
        this.memberCustomFieldService = memberCustomFieldService;
    }

    protected boolean allowRemovedGroups() {
        return true;
    }

  //  @Override
    protected Class<? extends CustomFieldValue> getCustomFieldValueClass() {
        return MemberCustomFieldValue.class;
    }

  //  @Override
    protected Class<FullTextMemberQuery> getQueryClass() {
        return FullTextMemberQuery.class;
    }
    
    public static class SearchMembersResponse extends GenericResponse{
        List<MemberCustomField> fields;
        private Collection<GroupFilter> groupFilters;

        public Collection<GroupFilter> getGroupFilters() {
            return groupFilters;
        }

        public void setGroupFilters(Collection<GroupFilter> groupFilters) {
            this.groupFilters = groupFilters;
        }
       

        public List<MemberCustomField> getFields() {
            return fields;
        }

        public void setFields(List<MemberCustomField> fields) {
            this.fields = fields;
        }
        
    }
    
    public static class SearchMembersRequest{
        private String keywords;

        public String getKeywords() {
            return keywords;
        }

        public void setKeywords(String keywords) {
            this.keywords = keywords;
        }
        
    }
           

   @RequestMapping(value = "member/searchMember", method = RequestMethod.POST)
   @ResponseBody
    public SearchMembersResponse prepareForm(@RequestBody SearchMembersRequest request ) {
        
       // final FullTextMemberQuery memberQuery = (FullTextMemberQuery) super.prepareForm();
       
      SearchMembersResponse response = new SearchMembersResponse();
        // Retrieve the custom fields that will be used on the search
        final List<MemberCustomField> fields = customFieldHelper.onlyForMemberSearch(memberCustomFieldService.list());
       

                
        final GroupFilterQuery groupFilterQuery = new GroupFilterQuery();
        if (LoggedUser.isAdministrator()) {
            final AdminGroup adminGroup = LoggedUser.group();
            groupFilterQuery.setAdminGroup(adminGroup);

            // Store the member groups for admins
            final GroupQuery groupQuery = new GroupQuery();
            groupQuery.setNatures(Group.Nature.MEMBER, Group.Nature.BROKER);
            if (!allowRemovedGroups()) {
                groupQuery.setStatus(Group.Status.NORMAL);
            }
            groupQuery.setManagedBy(adminGroup);
            //groupQuery.setGroupFilters(memberQuery.getGroupFilters());
            final List<MemberGroup> groups = (List<MemberGroup>) groupService.search(groupQuery);
//            request.setAttribute("groups", groups);
//            if (CollectionUtils.isEmpty(groups)) {
//                memberQuery.setGroups(groups);
//            }

            if (permissionService.hasPermission(AdminMemberPermission.MEMBERS_REGISTER)) {
                final Collection<MemberGroup> possibleNewGroups = new ArrayList<MemberGroup>();
                for (final MemberGroup memberGroup : groups) {
                    if (Group.Status.NORMAL.equals(memberGroup.getStatus())) {
                        possibleNewGroups.add(memberGroup);
                    }
                }
//                request.setAttribute("possibleNewGroups", possibleNewGroups);
            }
        } else {
            MemberGroup memberGroup;
            if (LoggedUser.isMember()) {
                memberGroup = LoggedUser.group();
            } else {
                final Operator operator = LoggedUser.element();
                memberGroup = operator.getMember().getMemberGroup();
            }
            groupFilterQuery.setViewableBy(memberGroup);
        }
        final Collection<GroupFilter> groupFilters = groupFilterService.search(groupFilterQuery);
        if (CollectionUtils.isNotEmpty(groupFilters)) {
            response.setGroupFilters(groupFilters);
        }

        response.setStatus(0);
        return response;
    }
}
    
    

