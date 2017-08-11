package nl.strohalm.cyclos.webservices.rest.groups.groupFilters;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import nl.strohalm.cyclos.annotations.Inject;
import nl.strohalm.cyclos.entities.customization.files.CustomizedFile;
import nl.strohalm.cyclos.entities.groups.GroupFilter;
import nl.strohalm.cyclos.entities.groups.GroupFilterQuery;
import nl.strohalm.cyclos.entities.groups.MemberGroup;
import nl.strohalm.cyclos.services.groups.GroupFilterService;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;

@Controller
public class ListGroupFiltersController extends BaseRestController {
	
    private GroupFilterService groupFilterService;
        
	public final GroupFilterService getGroupFilterService() {
		return groupFilterService;
	}

	@Inject
	public void setGroupFilterService(
			final GroupFilterService groupFilterService) {
		this.groupFilterService = groupFilterService;
		
	}

	public static class ListGroupFiltersRequestDto {
    private String                     name;
    private String                     description;
    private String                     loginPageName;
    private String                     rootUrl;
    private String                     containerUrl;
    private boolean                    showInProfile;
    private Collection<MemberGroup>    groups;
    private Collection<MemberGroup>    viewableBy;
    private Collection<CustomizedFile> customizedFiles;
   

    public String getContainerUrl() {
        return containerUrl;
    }

    public Collection<CustomizedFile> getCustomizedFiles() {
        return customizedFiles;
    }

    public String getDescription() {
        return description;
    }

    public Collection<MemberGroup> getGroups() {
        return groups;
    }

    public String getLoginPageName() {
        return loginPageName;
    }

    //@Override
    public String getName() {
        return name;
    }

    public String getRootUrl() {
        return rootUrl;
    }

    public Collection<MemberGroup> getViewableBy() {
        return viewableBy;
    }

    public boolean isShowInProfile() {
        return showInProfile;
    }

    public void setContainerUrl(final String containerUrl) {
        this.containerUrl = containerUrl;
    }

    public void setCustomizedFiles(final Collection<CustomizedFile> customizedFiles) {
        this.customizedFiles = customizedFiles;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public void setGroups(final Collection<MemberGroup> groups) {
        this.groups = groups;
    }

    public void setLoginPageName(final String loginPageName) {
        this.loginPageName = loginPageName;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public void setRootUrl(final String rootUrl) {
        this.rootUrl = rootUrl;
    }

    public void setShowInProfile(final boolean showInProfile) {
        this.showInProfile = showInProfile;
    }

    public void setViewableBy(final Collection<MemberGroup> viewableBy) {
        this.viewableBy = viewableBy;
    }

}

	public static final class ListGroupFiltersResponseDto {
		private List<GroupFilter> groupFilters;
                
                public HashMap<String, Object> response = new HashMap<String, Object>();

		public ListGroupFiltersResponseDto(List<GroupFilter> groupFilters) {
			super();
			this.setGroupFilters(groupFilters);
		}

		public List<GroupFilter> getGroupFilters() {
			return groupFilters;
		}

		public void setGroupFilters(List<GroupFilter> groupFilters) {
			this.groupFilters = groupFilters;
		}
                public ListGroupFiltersResponseDto(){
                }

	}

	@RequestMapping(value = "admin/listGroupFilters", method = RequestMethod.GET)
	@ResponseBody
	public ListGroupFiltersResponseDto executeAction() throws Exception {
			
		ListGroupFiltersResponseDto response =new ListGroupFiltersResponseDto();
                
                try{
                    

		final List<GroupFilter> groupFilters = groupFilterService.search(new GroupFilterQuery());
		response = new ListGroupFiltersResponseDto(groupFilters);
                }
				
                catch(Exception e){
                    e.printStackTrace();
                }
		return response;
	}
}
