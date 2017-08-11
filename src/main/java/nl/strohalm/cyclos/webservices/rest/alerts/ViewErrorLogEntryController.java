package nl.strohalm.cyclos.webservices.rest.alerts;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import nl.strohalm.cyclos.annotations.Inject;
import nl.strohalm.cyclos.entities.access.AdminUser;
import nl.strohalm.cyclos.entities.access.MemberUser;
import nl.strohalm.cyclos.entities.access.User;
import nl.strohalm.cyclos.entities.alerts.ErrorLogEntry;
import nl.strohalm.cyclos.services.alerts.ErrorLogService;
import nl.strohalm.cyclos.utils.RelationshipHelper;
import nl.strohalm.cyclos.utils.validation.ValidationException;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class ViewErrorLogEntryController extends BaseRestController {
	private ErrorLogService errorLogService;

	public final ErrorLogService getErrorLogService() {
		return errorLogService;
	}

	@Inject
	public void setErrorLogService(final ErrorLogService errorLogService) {
		this.errorLogService = errorLogService;
	}

	public static class ViewErrorLogEntryRequestDTO {
		private long entryId;
                private Calendar            date;
        private String              path;
        private Map<String, String> parameters;
        private String              stackTrace;
        private User                loggedUser;
        private boolean             removed;
        private long id;
    
        public long getId() {
            return id;
        }

        public void setId(long id) {
            this.id = id;
        }
    

    public Calendar getDate() {
        return date;
    }

    public User getLoggedUser() {
        return loggedUser;
    }

    public Map<String, String> getParameters() {
        return parameters;
    }

    public String getPath() {
        return path;
    }

    public String getStackTrace() {
        return stackTrace;
    }

    public boolean isRemoved() {
        return removed;
    }

    public void setDate(final Calendar date) {
        this.date = date;
    }

    public void setLoggedUser(final User loggedUser) {
        this.loggedUser = loggedUser;
    }

    public void setParameters(final Map<String, String> parameters) {
        this.parameters = parameters;
    }

    public void setPath(final String path) {
        this.path = path;
    }

    public void setRemoved(final boolean removed) {
        this.removed = removed;
    }

    public void setStackTrace(final String stackTrace) {
        this.stackTrace = stackTrace;
    }

   @Override
    public String toString() {
        return getId() + " - " + path;
    }
		public long getEntryId() {
			return entryId;
		}

		public void setEntryId(final long entryId) {
			this.entryId = entryId;
		}
	}

	public static class ViewErrorLogEntryResponseDTO {
            private long id;
            private boolean loggedMember;
            private boolean loggedAdmin;

        public boolean isLoggedMember() {
            return loggedMember;
        }

        public void setLoggedMember(boolean loggedMember) {
            this.loggedMember = loggedMember;
        }

        public boolean isLoggedAdmin() {
            return loggedAdmin;
        }

        public void setLoggedAdmin(boolean loggedAdmin) {
            this.loggedAdmin = loggedAdmin;
        }
            
        public long getId() {
            return id;
        }

        public void setId(long id) {
            this.id = id;
        }
            
		public ViewErrorLogEntryResponseDTO(Map<String, Object> param) {
			super();
		}

	}

	@RequestMapping(value ="admin/viewErrorLogEntry/{entryId}",method =RequestMethod.GET)
	@ResponseBody
	protected ViewErrorLogEntryResponseDTO executeAction(@PathVariable ("entryId") long entryId)throws Exception {
			
		ViewErrorLogEntryResponseDTO response = null;
                try{
		final long id = entryId;
		if (id <= 0L) {
			throw new ValidationException();
		}
		
		final ErrorLogEntry entry = errorLogService.load(id, ErrorLogEntry.Relationships.PARAMETERS,
				RelationshipHelper.nested(ErrorLogEntry.Relationships.LOGGED_USER, User.Relationships.ELEMENT));
		final User loggedUser = entry.getLoggedUser();
		Map<String, Object> param = new HashMap<String, Object>();
		if (loggedUser instanceof MemberUser) {
			param.put("loggedMember", loggedUser.getElement());

		} else if (loggedUser instanceof AdminUser) {
			param.put("loggedAdmin", loggedUser.getElement());

		}
		param.put("errorLogEntry", entry);
		response = new ViewErrorLogEntryResponseDTO(param);}
                catch(ValidationException e){
                    e.printStackTrace();
                }
		return response;
		
	}
}
