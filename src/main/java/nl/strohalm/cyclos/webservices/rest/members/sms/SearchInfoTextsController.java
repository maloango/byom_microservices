package nl.strohalm.cyclos.webservices.rest.members.sms;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import nl.strohalm.cyclos.access.AdminSystemPermission;
import nl.strohalm.cyclos.annotations.Inject;
import nl.strohalm.cyclos.controls.ActionContext;
import nl.strohalm.cyclos.controls.members.sms.SearchInfoTextsForm;
import nl.strohalm.cyclos.entities.infotexts.InfoText;
import nl.strohalm.cyclos.entities.infotexts.InfoTextQuery;
import nl.strohalm.cyclos.entities.settings.LocalSettings;
import nl.strohalm.cyclos.services.elements.ElementService;
import nl.strohalm.cyclos.services.groups.GroupService;
import nl.strohalm.cyclos.services.infotexts.InfoTextService;
import nl.strohalm.cyclos.services.permissions.PermissionService;
import nl.strohalm.cyclos.services.settings.SettingsService;
import nl.strohalm.cyclos.utils.binding.BeanBinder;
import nl.strohalm.cyclos.utils.binding.DataBinder;
import nl.strohalm.cyclos.utils.binding.DataBinderHelper;
import nl.strohalm.cyclos.utils.binding.PropertyBinder;
import nl.strohalm.cyclos.utils.query.QueryParameters;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class SearchInfoTextsController extends BaseRestController {
	private DataBinder<InfoTextQuery> dataBinder;
	private InfoTextService infoTextService;
	private GroupService groupService;
	private ElementService elementService;
	private PermissionService permissionService;
	private SettingsService settingsService;

	@Inject
	public void setInfoTextService(final InfoTextService service) {
		infoTextService = service;
	}

	public static class SearchInfoTextsRequestDto {

	}

	public static class SearchInfoTextsResponseDto {
		private String message;
		private List<InfoText> result;

		public SearchInfoTextsResponseDto(String message, List<InfoText> result) {
			super();
			this.message = message;
			this.result = result;
		}

		public String getMessage() {
			return message;
		}

		public void setMessage(String message) {
			this.message = message;
		}
	}

	@RequestMapping(value = "/admin/managePasswords", method = RequestMethod.GET)
	@ResponseBody
	protected SearchInfoTextsResponseDto executeQuery(
			@RequestBody SearchInfoTextsRequestDto form,
			final QueryParameters queryParameters) {
		// final HttpServletRequest request = context.getRequest();
		final InfoTextQuery query = (InfoTextQuery) queryParameters;
		String message = null;
		List<InfoText> result = null;
		SearchInfoTextsResponseDto response = new SearchInfoTextsResponseDto(message, result);
		if (query.getStartIn() != null && query.getStartIn().getBegin() != null
				&& query.getStartIn().getEnd() != null) {
			if (query.getStartIn().getBegin()
					.after(query.getStartIn().getEnd())) {
				message=
						settingsService.getLocalSettings()
								.getRawDateConverter()
								.toString(query.getStartIn().getBegin());
				return response;
			}
		}

		if (query.getEndIn() != null && query.getEndIn().getBegin() != null
				&& query.getEndIn().getEnd() != null) {
			if (query.getEndIn().getBegin().after(query.getEndIn().getEnd())) {
				message=
						settingsService.getLocalSettings()
								.getRawDateConverter()
								.toString(query.getEndIn().getBegin());
				return response;
			}
		}

		result = infoTextService.search(query);
		return response;
	}

	protected QueryParameters prepareForm(final ActionContext context) {
		final SearchInfoTextsForm form = context.getForm();
		final InfoTextQuery query = getDataBinder().readFromString(
				form.getQuery());
		context.getRequest()
				.setAttribute(
						"hasManagePermissions",
						permissionService
								.hasPermission(AdminSystemPermission.INFO_TEXTS_MANAGE));

		return query;
	}

	protected boolean willExecuteQuery(final ActionContext context,
			final QueryParameters queryParameters) throws Exception {
		// The query is always executed
		return true;
	}

	private DataBinder<InfoTextQuery> getDataBinder() {
		if (dataBinder == null) {
			final BeanBinder<InfoTextQuery> binder = BeanBinder
					.instance(InfoTextQuery.class);

			final LocalSettings settings = settingsService.getLocalSettings();
			binder.registerBinder("keywords",
					PropertyBinder.instance(String.class, "keywords"));
			binder.registerBinder("startIn",
					DataBinderHelper.rawPeriodBinder(settings, "validity"));
			binder.registerBinder("endIn",
					DataBinderHelper.rawPeriodBinder(settings, "validityEnd"));

			binder.registerBinder("pageParameters",
					DataBinderHelper.pageBinder());

			dataBinder = binder;
		}
		return dataBinder;
	}
}
