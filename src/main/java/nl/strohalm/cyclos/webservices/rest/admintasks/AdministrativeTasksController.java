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
import nl.strohalm.cyclos.webservices.rest.GenericResponse;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class AdministrativeTasksController extends BaseRestController {

    public static class AdminTaskResponse extends GenericResponse {

        private boolean allOptimized;
        private Map<String, IndexStatus> indexesStatusAsString;
        private boolean canViewIndexes;
        private boolean systemOnline;
        private boolean canManageOnlineState;

        public boolean isAllOptimized() {
            return allOptimized;
        }

        public void setAllOptimized(boolean allOptimized) {
            this.allOptimized = allOptimized;
        }

        public Map<String, IndexStatus> getIndexesStatusAsString() {
            return indexesStatusAsString;
        }

        public void setIndexesStatusAsString(Map<String, IndexStatus> indexesStatusAsString) {
            this.indexesStatusAsString = indexesStatusAsString;
        }

        public boolean isCanViewIndexes() {
            return canViewIndexes;
        }

        public void setCanViewIndexes(boolean canViewIndexes) {
            this.canViewIndexes = canViewIndexes;
        }

        public boolean isSystemOnline() {
            return systemOnline;
        }

        public void setSystemOnline(boolean systemOnline) {
            this.systemOnline = systemOnline;
        }

        public boolean isCanManageOnlineState() {
            return canManageOnlineState;
        }

        public void setCanManageOnlineState(boolean canManageOnlineState) {
            this.canManageOnlineState = canManageOnlineState;
        }

    }

    @RequestMapping(value = "admin/adminTasks", method = RequestMethod.GET)
    @ResponseBody
    public AdminTaskResponse executeAction() throws Exception {
        AdminTaskResponse response = new AdminTaskResponse();
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
            response.setAllOptimized(allOptimized);
            response.setIndexesStatusAsString(indexesStatusAsString);
        }
        response.setCanViewIndexes(canViewIndexes);

        final boolean canManageOnlineState = permissionService.hasPermission(AdminSystemPermission.TASKS_ONLINE_STATE);
        if (canManageOnlineState) {
            final boolean systemOnline = applicationService.isOnline();
            response.setSystemOnline(systemOnline);
        }
        response.setCanManageOnlineState(canManageOnlineState);

        if (!canViewIndexes && !canManageOnlineState) {
            throw new PermissionDeniedException();
        }
        response.setStatus(0);
        return response;
    }

}
