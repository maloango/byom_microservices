package nl.strohalm.cyclos.webservices.rest.reports.members.list;

import java.security.acl.Group;
import java.util.Collection;
import org.springframework.stereotype.Controller;
//import nl.strohalm.cyclos.controls.reports.members.list.MembersListReportHandler;
import nl.strohalm.cyclos.entities.accounts.SystemAccountType;
import nl.strohalm.cyclos.entities.accounts.transactions.TransferType;
import nl.strohalm.cyclos.entities.customization.fields.AdminCustomField;
import nl.strohalm.cyclos.entities.groups.AdminGroup;
import nl.strohalm.cyclos.entities.groups.Group.Nature;
import nl.strohalm.cyclos.entities.groups.MemberGroup;
import nl.strohalm.cyclos.entities.members.records.MemberRecordType;
import nl.strohalm.cyclos.services.groups.GroupService;
import nl.strohalm.cyclos.services.settings.SettingsService;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;
@Controller
public class MembersListReportController extends BaseRestController{

	//private MembersListReportHandler reportHandler;
	private SettingsService settingsService;
	public final SettingsService getSettingsService() {
		return settingsService;
	}

	public final void setSettingsService(SettingsService settingsService) {
		this.settingsService = settingsService;
	}

	public final GroupService getGroupService() {
		return groupService;
	}

	public final void setGroupService(GroupService groupService) {
		this.groupService = groupService;
	}

//	public final void setReportHandler(MembersListReportHandler reportHandler) {
//		this.reportHandler = reportHandler;
//	}

	private GroupService groupService;
	private nl.strohalm.cyclos.entities.groups.Group adminGroup;
	
	public static class MembersListReportRequestDTO{
		private Collection<TransferType>      transferTypesAsMember;
	    private Collection<MemberGroup>       managesGroups;
	    private Collection<SystemAccountType> viewInformationOf;
	    private Collection<AdminGroup>        viewConnectedAdminsOf;
	    private Collection<AdminGroup>        connectedAdminsViewedBy;
	    private Collection<AdminCustomField>  adminCustomFields;
	    private Collection<MemberRecordType>  viewAdminRecordTypes;
	    private Collection<MemberRecordType>  createAdminRecordTypes;
	    private Collection<MemberRecordType>  modifyAdminRecordTypes;
	    private Collection<MemberRecordType>  deleteAdminRecordTypes;
	    private Collection<MemberRecordType>  viewMemberRecordTypes;
	    private Collection<MemberRecordType>  createMemberRecordTypes;
	    private Collection<MemberRecordType>  modifyMemberRecordTypes;
	    private Collection<MemberRecordType>  deleteMemberRecordTypes;

	    public Collection<AdminCustomField> getAdminCustomFields() {
	        return adminCustomFields;
	    }

	    public Collection<AdminGroup> getConnectedAdminsViewedBy() {
	        return connectedAdminsViewedBy;
	    }

	    public Collection<MemberRecordType> getCreateAdminRecordTypes() {
	        return createAdminRecordTypes;
	    }

	    public Collection<MemberRecordType> getCreateMemberRecordTypes() {
	        return createMemberRecordTypes;
	    }

	    public Collection<MemberRecordType> getDeleteAdminRecordTypes() {
	        return deleteAdminRecordTypes;
	    }

	    public Collection<MemberRecordType> getDeleteMemberRecordTypes() {
	        return deleteMemberRecordTypes;
	    }

	    public Collection<MemberGroup> getManagesGroups() {
	        return managesGroups;
	    }

	    public Collection<MemberRecordType> getModifyAdminRecordTypes() {
	        return modifyAdminRecordTypes;
	    }

	    public Collection<MemberRecordType> getModifyMemberRecordTypes() {
	        return modifyMemberRecordTypes;
	    }

	    //@Override
	    public Nature getNature() {
	        return Nature.ADMIN;
	    }

	    public Collection<TransferType> getTransferTypesAsMember() {
	        return transferTypesAsMember;
	    }

	    public Collection<MemberRecordType> getViewAdminRecordTypes() {
	        return viewAdminRecordTypes;
	    }

	    public Collection<AdminGroup> getViewConnectedAdminsOf() {
	        return viewConnectedAdminsOf;
	    }

	    public Collection<SystemAccountType> getViewInformationOf() {
	        return viewInformationOf;
	    }

	    public Collection<MemberRecordType> getViewMemberRecordTypes() {
	        return viewMemberRecordTypes;
	    }

	    public void setAdminCustomFields(final Collection<AdminCustomField> adminCustomFields) {
	        this.adminCustomFields = adminCustomFields;
	    }

	    public void setConnectedAdminsViewedBy(final Collection<AdminGroup> connectedAdminsViewedBy) {
	        this.connectedAdminsViewedBy = connectedAdminsViewedBy;
	    }

	    public void setCreateAdminRecordTypes(final Collection<MemberRecordType> createAdminRecordTypes) {
	        this.createAdminRecordTypes = createAdminRecordTypes;
	    }

	    public void setCreateMemberRecordTypes(final Collection<MemberRecordType> createMemberRecordTypes) {
	        this.createMemberRecordTypes = createMemberRecordTypes;
	    }

	    public void setDeleteAdminRecordTypes(final Collection<MemberRecordType> deleteAdminRecordTypes) {
	        this.deleteAdminRecordTypes = deleteAdminRecordTypes;
	    }

	    public void setDeleteMemberRecordTypes(final Collection<MemberRecordType> deleteMemberRecordTypes) {
	        this.deleteMemberRecordTypes = deleteMemberRecordTypes;
	    }

	    public void setManagesGroups(final Collection<MemberGroup> managesGroups) {
	        this.managesGroups = managesGroups;
	    }

	    public void setModifyAdminRecordTypes(final Collection<MemberRecordType> modifyAdminRecordTypes) {
	        this.modifyAdminRecordTypes = modifyAdminRecordTypes;
	    }

	    public void setModifyMemberRecordTypes(final Collection<MemberRecordType> modifyMemberRecordTypes) {
	        this.modifyMemberRecordTypes = modifyMemberRecordTypes;
	    }

	    public void setTransferTypesAsMember(final Collection<TransferType> transferTypesAsMember) {
	        this.transferTypesAsMember = transferTypesAsMember;
	    }

	    public void setViewAdminRecordTypes(final Collection<MemberRecordType> viewAdminRecordTypes) {
	        this.viewAdminRecordTypes = viewAdminRecordTypes;
	    }

	    public void setViewConnectedAdminsOf(final Collection<AdminGroup> viewConnectedAdminsOf) {
	        this.viewConnectedAdminsOf = viewConnectedAdminsOf;
	    }

	    public void setViewInformationOf(final Collection<SystemAccountType> viewInformationOf) {
	        this.viewInformationOf = viewInformationOf;
	    }

	    public void setViewMemberRecordTypes(final Collection<MemberRecordType> viewMemberRecordTypes) {
	        this.viewMemberRecordTypes = viewMemberRecordTypes;
	    }

	}
	public static class MembersListReportResponseDTO{
		private Group groups;
		private boolean adminGroup;

		public final boolean isAdminGroup() {
			return adminGroup;
		}

		public final void setAdminGroup(boolean adminGroup) {
			this.adminGroup = adminGroup;
		}

		public Group getGroups() {
			return groups;
		}

		public void setGroups(Group groups) {
			this.groups = groups;
		}
	}

  /*  @RequestMapping(value = "",method = RequestMethod.GET)
    @ResponseBody
    protected MembersListReportResponseDTO handleDisplay(@RequestBody MembersListReportRequestDTO form) throws Exception {
        
        adminGroup = groupService.load(AdminGroup.getId(), AdminGroup.Relationships.MANAGES_GROUPS);
        final Collection<MemberGroup> groups = new TreeSet<MemberGroup>(adminGroup.getManagesGroups());
        //final HttpServletRequest request = context.getRequest();
        request.setAttribute("groups", groups);
        return context.getInputForward();
    }

   // @Override
    protected ActionForward handleSubmit(final ActionContext context) throws Exception {
        final MembersListReportVOIterator voIterator = getReportHandler().handleReport(context);

        final HttpServletRequest request = context.getRequest();
        final MembersListReportForm form = context.getForm();
        final MembersListReportDTO dto = getReportHandler().getDataBinder().readFromString(form.getMembersListReport());
        request.setAttribute("dto", dto);

        if (dto.isAccountsInformation()) {
            final List<MemberGroup> groups = (List<MemberGroup>) dto.getGroups();
            final Collection<AccountType> accountTypes = getReportHandler().getAccountTypes(groups);
            request.setAttribute("accountTypes", accountTypes);
        }

        request.setAttribute("voIterator", voIterator);
        RequestHelper.storeEnumMap(request, Ad.Status.class, "adStatus");
        RequestHelper.storeEnumMap(request, Reference.Level.class, "referenceLevels");
        return context.getSuccessForward();
    }

    private MembersListReportHandler getReportHandler() {
        if (reportHandler == null) {
            reportHandler = new MembersListReportHandler(settingsService.getLocalSettings());
            SpringHelper.injectBeans(getServlet().getServletContext(), reportHandler);
        }
        return reportHandler;
    }
*/
}
