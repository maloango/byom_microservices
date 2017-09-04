package nl.strohalm.cyclos.webservices.rest.groups;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import nl.strohalm.cyclos.access.AdminSystemPermission;
import nl.strohalm.cyclos.access.Permission;
import nl.strohalm.cyclos.annotations.Inject;
import nl.strohalm.cyclos.controls.ActionContext;
//import nl.strohalm.cyclos.controls.groups.ListGroupsForm;
import nl.strohalm.cyclos.entities.accounts.MemberAccountType;
import nl.strohalm.cyclos.entities.accounts.transactions.PaymentFilter;
import nl.strohalm.cyclos.entities.groups.AdminGroup;
import nl.strohalm.cyclos.entities.groups.Group;
import nl.strohalm.cyclos.entities.groups.GroupFilter;
import nl.strohalm.cyclos.entities.groups.GroupFilterQuery;
import nl.strohalm.cyclos.entities.groups.GroupQuery;
import nl.strohalm.cyclos.entities.members.Member;
import nl.strohalm.cyclos.services.groups.GroupFilterService;
import nl.strohalm.cyclos.services.groups.GroupService;
import nl.strohalm.cyclos.services.permissions.PermissionService;
import nl.strohalm.cyclos.utils.binding.BeanBinder;
import nl.strohalm.cyclos.utils.binding.DataBinder;
import nl.strohalm.cyclos.utils.binding.PropertyBinder;
import nl.strohalm.cyclos.utils.query.QueryParameters;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;

@Controller
public class ListGroupsController extends BaseRestController {
	public static Map<Group.Nature, Permission> getManageGroupPermissionByNatureMap() {
        final Map<Group.Nature, Permission> permissionByNature = new EnumMap<Group.Nature, Permission>(Group.Nature.class);
        permissionByNature.put(Group.Nature.ADMIN, AdminSystemPermission.GROUPS_MANAGE_ADMIN);
        permissionByNature.put(Group.Nature.BROKER, AdminSystemPermission.GROUPS_MANAGE_BROKER);
        permissionByNature.put(Group.Nature.MEMBER, AdminSystemPermission.GROUPS_MANAGE_MEMBER);

        return permissionByNature;
    }

    private GroupFilterService     groupFilterService;

    private DataBinder<GroupQuery> dataBinder;
    private PermissionService permissionService;
    public final PermissionService getPermissionService() {
		return permissionService;
	}

	public final void setPermissionService(PermissionService permissionService) {
		this.permissionService = permissionService;
	}

	public final GroupService getGroupService() {
		return groupService;
	}

	public final void setGroupService(GroupService groupService) {
		this.groupService = groupService;
	}

	public final GroupFilterService getGroupFilterService() {
		return groupFilterService;
	}

	private GroupService groupService;

    public DataBinder<GroupQuery> getDataBinder() {
        if (dataBinder == null) {
            final BeanBinder<GroupQuery> binder = BeanBinder.instance(GroupQuery.class);
            binder.registerBinder("nature", PropertyBinder.instance(Group.Nature.class, "nature"));
            binder.registerBinder("groupFilter", PropertyBinder.instance(GroupFilter.class, "groupFilter"));
            dataBinder = binder;
        }
        return dataBinder;
    }

    @Inject
    public void setGroupFilterService(final GroupFilterService groupFilterService) {
        this.groupFilterService = groupFilterService;
    }
    public static class ListGroupsRequestDTO{
    	private Collection<Group>       possibleGroups;
        private Collection<GroupFilter> groupFilters;
        private Group.Nature[]          natures;
        private Group.Status[]          status;
        private MemberAccountType       memberAccountType;
        private PaymentFilter           paymentFilter;
        private AdminGroup              managedBy;
        private Member                  member;
        private boolean                 ignoreManagedBy;
        private boolean                 sortByNature;
        private boolean                 onlyActive;
        private Member                  broker;

        public Member getBroker() {
            return broker;
        }

        public GroupFilter getGroupFilter() {
            return CollectionUtils.isNotEmpty(groupFilters) ? groupFilters.iterator().next() : null;
        }

        public Collection<GroupFilter> getGroupFilters() {
            return groupFilters;
        }

        public AdminGroup getManagedBy() {
            return managedBy;
        }

        public Member getMember() {
            return member;
        }

        public MemberAccountType getMemberAccountType() {
            return memberAccountType;
        }

        public Group.Nature getNature() {
            if (natures == null || natures.length == 0) {
                return null;
            }
            return natures[0];
        }

        public Collection<String> getNatureDiscriminators() {
            if (natures == null || natures.length == 0) {
                return null;
            }
            final Collection<String> discriminators = new HashSet<String>();
            for (final Group.Nature nature : natures) {
                discriminators.add(nature.getDiscriminator());
            }
            return discriminators;
        }

        public Group.Nature[] getNatures() {
            return natures;
        }

        public Collection<Group.Nature> getNaturesCollection() {
            return natures == null ? null : Arrays.asList(natures);
        }

        public Collection<Group.Nature> getNaturesList() {
            if (natures != null) {
                return Arrays.asList(natures);
            } else {
                return null;
            }
        }

        public PaymentFilter getPaymentFilter() {
            return paymentFilter;
        }

        public Collection<Group> getPossibleGroups() {
            return possibleGroups;
        }

        public Group.Status[] getStatus() {
            return status;
        }

        public Collection<Group.Status> getStatusCollection() {
            return status == null || status.length == 0 ? null : Arrays.asList(status);
        }

        public boolean isIgnoreManagedBy() {
            return ignoreManagedBy;
        }

        public boolean isOnlyActive() {
            return onlyActive;
        }

        public boolean isSortByNature() {
            return sortByNature;
        }

        public void setBroker(final Member broker) {
            this.broker = broker;
        }

        public void setGroupFilter(final GroupFilter groupFilter) {
            groupFilters = groupFilter == null ? null : Collections.singletonList(groupFilter);
        }

        public void setGroupFilters(final Collection<GroupFilter> groupFilters) {
            this.groupFilters = groupFilters;
        }

        public void setIgnoreManagedBy(final boolean ignoreManagedBy) {
            this.ignoreManagedBy = ignoreManagedBy;
        }

        public void setManagedBy(final AdminGroup managedBy) {
            this.managedBy = managedBy;
        }

        public void setMember(final Member member) {
            this.member = member;
        }

        public void setMemberAccountType(final MemberAccountType memberAccountType) {
            this.memberAccountType = memberAccountType;
        }

        public void setNature(final Group.Nature nature) {
            natures = nature == null ? null : new Group.Nature[] { nature };
        }

        public void setNatures(final Group.Nature... natures) {
            this.natures = natures;
        }

        public void setNaturesCollection(final Collection<Group.Nature> natures) {
            this.natures = natures == null ? null : natures.toArray(new Group.Nature[natures.size()]);
        }

        public void setOnlyActive(final boolean onlyActive) {
            this.onlyActive = onlyActive;
        }

        public void setPaymentFilter(final PaymentFilter paymentFilter) {
            this.paymentFilter = paymentFilter;
        }

        public void setPossibleGroups(final Collection<Group> possibleGroups) {
            this.possibleGroups = possibleGroups;
        }

        public void setSortByNature(final boolean sortByNature) {
            this.sortByNature = sortByNature;
        }

        public void setStatus(final Group.Status... status) {
            this.status = status;
        }

        public void setStatusCollection(final Collection<Group.Status> status) {
            this.status = status == null ? null : status.toArray(new Group.Status[status.size()]);
        }
    }
    public static class ListGroupsResponseDTO{
    	List<Group> groups;
    	public ListGroupsResponseDTO(List<Group> groups){
    		super();
    		this.groups=groups;
    	}
    	String message;

		public final String getMessage() {
			return message;
		}

		public final void setMessage(String message) {
			this.message = message;
		}
    	public ListGroupsResponseDTO(){
        }
    	
    }

    @RequestMapping(value = "admin/listGroups" ,method = RequestMethod.POST)
    @ResponseBody
    protected ListGroupsResponseDTO executeQuery(@RequestBody ListGroupsRequestDTO form, final QueryParameters queryParameters) {  
        final GroupQuery groupQuery = (GroupQuery) queryParameters;
        @SuppressWarnings("unused")
		final List<? extends Group> groups = groupService.search(groupQuery);
        @SuppressWarnings("unchecked")
		ListGroupsResponseDTO response = new ListGroupsResponseDTO((List<Group>) groupQuery);
       
        return response;
        
        
    }

    //@Override
//    protected GroupQuery prepareForm(final ActionContext context) {
//        final HttpServletRequest request = context.getRequest();
//        final ListGroupsForm form = context.getForm();
//        boolean manageAnyGroup = false;
//        final GroupQuery groupQuery = getDataBinder().readFromString(form.getQuery());
//        if (context.isAdmin()) {
//            groupQuery.setSortByNature(true);
//
//            // Put in the request the name of permission used to manage a type of group
//            final Map<Group.Nature, Permission> permissionByNature = getManageGroupPermissionByNatureMap();
//            request.setAttribute("permissionByNature", permissionByNature);
//
//            // Check if the user has permission to manage any group
//            for (final Permission permission : permissionByNature.values()) {
//                if (permissionService.hasPermission(permission)) {
//                    manageAnyGroup = true;
//                    break;
//                }
//            }
//
//            // List of groups that the administrator can manage
//            AdminGroup adminGroup = context.getGroup();
//            adminGroup = groupService.load(adminGroup.getId(), AdminGroup.Relationships.MANAGES_GROUPS);
//            request.setAttribute("managesGroups", adminGroup.getManagesGroups());
//
//            // List of group natures
//            request.setAttribute("natures", Arrays.asList(Group.Nature.ADMIN, Group.Nature.BROKER, Group.Nature.MEMBER));
//
//            // Search group filters and send to the JSP page
//            final GroupFilterQuery groupFilterQuery = new GroupFilterQuery();
//            groupFilterQuery.setAdminGroup(adminGroup);
//            final Collection<GroupFilter> groupFilters = groupFilterService.search(groupFilterQuery);
//            if (CollectionUtils.isNotEmpty(groupFilters)) {
//                request.setAttribute("groupFilters", groupFilters);
//            }
//        } else {
//            // It's a member listing operators groups
//            final Member member = (Member) context.getElement();
//            groupQuery.setNatures(Group.Nature.OPERATOR);
//            groupQuery.setMember(member);
//            groupQuery.setSortByNature(false);
//            manageAnyGroup = true;
//        }
//        request.setAttribute("manageAnyGroup", manageAnyGroup);
//        return groupQuery;
//    }

    //@Override
    protected boolean willExecuteQuery(final ActionContext context, final QueryParameters queryParameters) throws Exception {
        return true;
    }

}
