package nl.strohalm.cyclos.webservices.rest.admintasks;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import nl.strohalm.cyclos.access.AdminSystemPermission;
import nl.strohalm.cyclos.annotations.Inject;
import nl.strohalm.cyclos.entities.IndexStatus;
import nl.strohalm.cyclos.exceptions.PermissionDeniedException;
import nl.strohalm.cyclos.services.application.ApplicationService;
import nl.strohalm.cyclos.services.permissions.PermissionService;
import nl.strohalm.cyclos.utils.ClassHelper;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;
@Controller
public class AdministrativeTasksController extends BaseRestController{
	private ApplicationService applicationService;
	public final PermissionService getPermissionService() {
		return permissionService;
	}

	public final void setPermissionService(PermissionService permissionService) {
		this.permissionService = permissionService;
	}

	public final ApplicationService getApplicationService() {
		return applicationService;
	}

	protected PermissionService permissionService;
	
	
	public interface Indexable {

	    Long getId();
	}

	public static class AdministrativeTasksRequestDto{
		public String message;
		
		public final String getMessage() {
			return message;
		}

		public final void setMessage(String message) {
			this.message = message;
		}

		public interface Indexable {

		    Long getId();
		}

		 public static String getClassName(final Class<?> clazz) {
		        final String name = clazz.getName();
		        final int pos = name.lastIndexOf('.');
		        if (pos < 0) {
		            return name;
		        } else {
		            return name.substring(pos + 1);
		        }
		    }

		public static void setAttribute(String string, Map<String, IndexStatus> indexesStatusAsString) {
			// TODO Auto-generated method stub
			
		}

		public static void setAttribute(String string, boolean allOptimized) {
			// TODO Auto-generated method stub
			
		}
	    public static AdministrativeTasksResponseDto getInputForward() {
	        return getInputForward();
	    }
		
		
	}
	
	public static class AdministrativeTasksResponseDto{
		private boolean allOptimized;
		private boolean systemOnline;
		public final boolean isAllOptimized() {
			return allOptimized;
		}
		public final void setAllOptimized(boolean allOptimized) {
			this.allOptimized = allOptimized;
		}
		public final boolean isIndexesStatus() {
			return indexesStatus;
		}
		public final void setIndexesStatus(boolean indexesStatus) {
			this.indexesStatus = indexesStatus;
		}
		public final boolean isCanViewIndexes() {
			return canViewIndexes;
		}
		public final void setCanViewIndexes(boolean canViewIndexes) {
			this.canViewIndexes = canViewIndexes;
		}
		private boolean indexesStatus;
		private boolean canViewIndexes;
		private boolean canManageOnlineState;
		public final boolean isSystemOnline() {
			return systemOnline;
		}
		public final void setSystemOnline(boolean systemOnline) {
			this.systemOnline = systemOnline;
		}
		public final boolean isCanManageOnlineState() {
			return canManageOnlineState;
		}
		public final void setCanManageOnlineState(boolean canManageOnlineState) {
			this.canManageOnlineState = canManageOnlineState;
		}
		
	}

    @Inject
    public void setApplicationService(final ApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @RequestMapping(value = "admin/adminTasks", method = RequestMethod.HEAD)
    protected AdministrativeTasksResponseDto executeAction(@RequestBody AdministrativeTasksRequestDto form) throws Exception {
        

        final boolean canViewIndexes = permissionService.hasPermission(AdminSystemPermission.TASKS_MANAGE_INDEXES);
        if (canViewIndexes) {
            final Map<Class<? extends nl.strohalm.cyclos.entities.Indexable>, IndexStatus> indexesStatus = applicationService.getFullTextIndexesStatus();
            final Map<String, IndexStatus> indexesStatusAsString = new LinkedHashMap<String, IndexStatus>();
            boolean allOptimized = true;
            for (final Entry<Class<? extends nl.strohalm.cyclos.entities.Indexable>, IndexStatus> entry : indexesStatus.entrySet()) {
                final String name = ClassHelper.getClassName(entry.getKey());
                final IndexStatus status = entry.getValue();
                final boolean optimized = status == IndexStatus.ACTIVE;
                if (!optimized) {
                    allOptimized = false;
                }
                indexesStatusAsString.put(name, status);
            }
            AdministrativeTasksRequestDto.setAttribute("allOptimized", allOptimized);
            AdministrativeTasksRequestDto.setAttribute("indexesStatus", indexesStatusAsString);
        }
        AdministrativeTasksRequestDto.setAttribute("canViewIndexes", canViewIndexes);

        final boolean canManageOnlineState = permissionService.hasPermission(AdminSystemPermission.TASKS_ONLINE_STATE);
        if (canManageOnlineState) {
            final boolean systemOnline = applicationService.isOnline();
            AdministrativeTasksRequestDto.setAttribute("systemOnline", systemOnline);
        }
        AdministrativeTasksRequestDto.setAttribute("canManageOnlineState", canManageOnlineState);

        if (!canViewIndexes && !canManageOnlineState) {
            throw new PermissionDeniedException();
        }

        return AdministrativeTasksRequestDto.getInputForward();
    }

}




