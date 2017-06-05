package nl.strohalm.cyclos.webservices.rest.alerts;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import nl.strohalm.cyclos.annotations.Inject;
import nl.strohalm.cyclos.controls.restapi.BaseRestController;
import nl.strohalm.cyclos.entities.access.AdminUser;
import nl.strohalm.cyclos.entities.access.MemberUser;
import nl.strohalm.cyclos.entities.access.User;
import nl.strohalm.cyclos.entities.alerts.ErrorLogEntry;
import nl.strohalm.cyclos.services.alerts.ErrorLogService;
import nl.strohalm.cyclos.utils.RelationshipHelper;
import nl.strohalm.cyclos.utils.validation.ValidationException;

@Controller
public class ViewErrorLogEntryController extends BaseRestController {
	private ErrorLogService errorLogService;

	@Inject
	public void setErrorLogService(final ErrorLogService errorLogService) {
		this.errorLogService = errorLogService;
	}

	public static class ViewErrorLogEntryRequestDTO {
		private long entryId;

		public long getEntryId() {
			return entryId;
		}

		public void setEntryId(final long entryId) {
			this.entryId = entryId;
		}
	}

	public static class ViewErrorLogEntryResponseDTO {
		public ViewErrorLogEntryResponseDTO(Map<String, Object> param) {
			super();
		}

	}

	@RequestMapping()
	@ResponseBody
	protected ViewErrorLogEntryResponseDTO executeAction(@RequestBody ViewErrorLogEntryRequestDTO form)
			throws Exception {
		// final ViewErrorLogEntryForm form = context.getForm();
		final long id = form.getEntryId();
		if (id <= 0L) {
			throw new ValidationException();
		}
		// final HttpServletRequest request = context.getRequest();
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
		ViewErrorLogEntryResponseDTO response = new ViewErrorLogEntryResponseDTO(param);
		return response;
		
	}
}
