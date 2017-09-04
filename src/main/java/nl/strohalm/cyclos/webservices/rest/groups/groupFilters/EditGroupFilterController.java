package nl.strohalm.cyclos.webservices.rest.groups.groupFilters;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import nl.strohalm.cyclos.annotations.Inject;
import nl.strohalm.cyclos.controls.ActionContext;
//import nl.strohalm.cyclos.controls.groups.groupFilters.EditGroupFilterForm;
import nl.strohalm.cyclos.entities.customization.files.CustomizedFileQuery;
import nl.strohalm.cyclos.entities.groups.Group;
import nl.strohalm.cyclos.entities.groups.GroupFilter;
import nl.strohalm.cyclos.entities.groups.GroupQuery;
import nl.strohalm.cyclos.entities.groups.MemberGroup;
import nl.strohalm.cyclos.services.customization.CustomizedFileService;
import nl.strohalm.cyclos.services.groups.GroupFilterService;
import nl.strohalm.cyclos.services.groups.GroupService;
import nl.strohalm.cyclos.utils.binding.BeanBinder;
import nl.strohalm.cyclos.utils.binding.DataBinder;
import nl.strohalm.cyclos.utils.binding.PropertyBinder;
import nl.strohalm.cyclos.utils.binding.SimpleCollectionBinder;
import nl.strohalm.cyclos.utils.conversion.IdConverter;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

@Controller
public class EditGroupFilterController extends BaseRestController {

    private GroupFilterService groupFilterService;
    private GroupService groupService;

    public final GroupFilterService getGroupFilterService() {
        return groupFilterService;
    }

    public final CustomizedFileService getCustomizedFileService() {
        return customizedFileService;
    }

    private DataBinder<GroupFilter> dataBinder;
    private CustomizedFileService customizedFileService;

    public DataBinder<GroupFilter> getDataBinder() {
        if (dataBinder == null) {
            final BeanBinder<GroupFilter> binder = BeanBinder
                    .instance(GroupFilter.class);
            binder.registerBinder(
                    "id",
                    PropertyBinder.instance(Long.class, "id",
                            IdConverter.instance()));
            binder.registerBinder("name",
                    PropertyBinder.instance(String.class, "name"));
            binder.registerBinder("rootUrl",
                    PropertyBinder.instance(String.class, "rootUrl"));
            binder.registerBinder("loginPageName",
                    PropertyBinder.instance(String.class, "loginPageName"));
            binder.registerBinder("containerUrl",
                    PropertyBinder.instance(String.class, "containerUrl"));
            binder.registerBinder("description",
                    PropertyBinder.instance(String.class, "description"));
            binder.registerBinder("showInProfile",
                    PropertyBinder.instance(Boolean.TYPE, "showInProfile"));
            binder.registerBinder("groups", SimpleCollectionBinder.instance(
                    MemberGroup.class, "groups"));
            binder.registerBinder("viewableBy", SimpleCollectionBinder
                    .instance(MemberGroup.class, "viewableBy"));
            dataBinder = binder;
        }
        return dataBinder;
    }

    @Inject
    public void setCustomizedFileService(
            final CustomizedFileService customizedFileService) {
        this.customizedFileService = customizedFileService;
    }

    @Inject
    public void setGroupFilterService(
            final GroupFilterService groupFilterService) {
        this.groupFilterService = groupFilterService;
    }

    public static class EditGroupFilterRequestDTO {

        private long groupFilterId;

        private Map<String, Object> values=new HashMap<String,Object>();

        public Map<String, Object> getValues() {
            return values;
        }

        public void setValues(final Map<String, Object> values) {
            this.values = values;
        }

        public Map<String, Object> getGroupFilter() {
            return values;
        }

        public Object getGroupFilter(final String key) {
            return values.get(key);
        }

        public long getGroupFilterId() {
            return groupFilterId;
        }

        public void setGroupFilter(final Map<String, Object> map) {
            values = map;
        }

        public void setGroupFilter(final String key, final Object value) {
            values.put(key, value);
        }

        public void setGroupFilterId(final long groupFilterId) {
            this.groupFilterId = groupFilterId;
        }

    }

    public static class EditGroupFilterResponseDTO {

        public String message;
        Map<String, Object> params;

        public EditGroupFilterResponseDTO(String message,
                Map<String, Object> params) {
            super();
            this.message = message;
            this.params = params;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }

    @RequestMapping(value = "admin/editGroupFilter", method = RequestMethod.POST)
    @ResponseBody
    protected EditGroupFilterResponseDTO handleSubmit(@RequestBody EditGroupFilterRequestDTO form) throws Exception {

        EditGroupFilterResponseDTO response = null;
        try {
            GroupFilter groupFilter = getDataBinder().readFromString(
                    form.getGroupFilter());
            final boolean isInsert = (groupFilter.getId() == null);
            groupFilter = groupFilterService.save(groupFilter);
            String message = null;
            if (isInsert) {
                message = "groupFilter.inserted";
            } else {
                message = "groupFilter.modified";
            }
            final Map<String, Object> params = new HashMap<String, Object>();
            params.put("groupFilterId", groupFilter.getId());
            response = new EditGroupFilterResponseDTO(message, params);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return response;
    }

    public static class PrepareFormResponseDTO {

        public HashMap<String, Object> response = new HashMap<String, Object>();

        public HashMap<String, Object> getResponse() {
            return response;
        }

        public void setResponse(HashMap<String, Object> response) {
            this.response = response;
        }

        public PrepareFormResponseDTO() {
        }
        
    }

    @RequestMapping(value = "admin/editGroupFilter/{groupFilterId}", method = RequestMethod.GET)
    @ResponseBody
    public PrepareFormResponseDTO prepareForm(@PathVariable("groupFilterId") long groupFilterId) throws Exception {
        PrepareFormResponseDTO preFormResp = new PrepareFormResponseDTO();
        try {
            HashMap<String, Object> response = new HashMap<String, Object>();

            final long id = groupFilterId;
            final boolean isInsert = (id <= 0L);
            if (!isInsert) {
                final GroupFilter groupFilter = groupFilterService.load(id, GroupFilter.Relationships.GROUPS, GroupFilter.Relationships.VIEWABLE_BY, GroupFilter.Relationships.CUSTOMIZED_FILES);
                response.put("groupFilter", groupFilter);
                getDataBinder().writeAsString(groupFilterId, groupFilterId);

                // Retrieve the associated customized files
                final CustomizedFileQuery cfQuery = new CustomizedFileQuery();
                cfQuery.setGroupFilter(groupFilter);
                response.put("customizedFiles", customizedFileService.search(cfQuery));
            }

            // Get the groups that can belong to this group filter
            final GroupQuery query = new GroupQuery();
            query.setNatures(Group.Nature.MEMBER, Group.Nature.BROKER);
            final Collection<MemberGroup> groups = (Collection<MemberGroup>) groupService.search(query);

            // Get the groups that can view this group filter
            final Collection<MemberGroup> viewableBy = groups;

            response.put("isInsert", isInsert);
            response.put("groups", groups);
            response.put("viewableBy", viewableBy);
            response.put("canManageCustomizedFiles", customizedFileService.canViewOrManageInGroupFilters());
            preFormResp.setResponse(response);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return preFormResp;
    }

    // @Override
//    protected void validateForm(final ActionContext context) {
//        final EditGroupFilterForm form = context.getForm();
//        final GroupFilter groupFilter = getDataBinder().readFromString(form.getGroupFilter());
//        groupFilterService.validate(groupFilter);
//    }

}
