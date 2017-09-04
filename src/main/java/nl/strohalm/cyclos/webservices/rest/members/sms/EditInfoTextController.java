package nl.strohalm.cyclos.webservices.rest.members.sms;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;

import nl.strohalm.cyclos.access.AdminSystemPermission;
import nl.strohalm.cyclos.annotations.Inject;
import nl.strohalm.cyclos.controls.ActionContext;
//import nl.strohalm.cyclos.controls.members.sms.EditInfoTextForm;
import nl.strohalm.cyclos.entities.infotexts.InfoText;
import nl.strohalm.cyclos.entities.settings.LocalSettings;
import nl.strohalm.cyclos.services.elements.ElementService;
import nl.strohalm.cyclos.services.groups.GroupService;
import nl.strohalm.cyclos.services.infotexts.InfoTextService;
import nl.strohalm.cyclos.services.permissions.PermissionService;
import nl.strohalm.cyclos.services.settings.SettingsService;
import nl.strohalm.cyclos.utils.ActionHelper;
import nl.strohalm.cyclos.utils.binding.BeanBinder;
import nl.strohalm.cyclos.utils.binding.DataBinder;
import nl.strohalm.cyclos.utils.binding.DataBinderHelper;
import nl.strohalm.cyclos.utils.binding.MapBean;
import nl.strohalm.cyclos.utils.binding.PropertyBinder;
import nl.strohalm.cyclos.utils.conversion.IdConverter;
import nl.strohalm.cyclos.utils.conversion.SetConverter;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;

import org.apache.struts.action.ActionForward;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class EditInfoTextController extends BaseRestController {
	private DataBinder<InfoText> dataBinder;
	private InfoTextService infoTextService;
	private GroupService groupService;
	private ElementService elementService;
	private PermissionService permissionService;
	private SettingsService settingsService;

	@Inject
	public void setInfoTextService(final InfoTextService service) {
		infoTextService = service;
	}

	public static class EditInfoTextRequestDto {
		private Long infoTextId;
		protected Map<String, Object> values;

		public Map<String, Object> getValues() {
			return values;
		}

		public void setValues(final Map<String, Object> values) {
			this.values = values;
		}

		public Map<String, Object> getInfoText() {
			return values;
		}

		public Object getInfoText(final String key) {
			return values.get(key);
		}

		public Long getInfoTextId() {
			return infoTextId;
		}

		public void setInfoText(final Map<String, Object> data) {
			values = data;
		}

		public void setInfoText(final String key, final Object value) {
			values.put(key, value);
		}

		public void setInfoTextId(final Long infoTextId) {
			this.infoTextId = infoTextId;
		}
	}

	public static class EditInfoTextResponseDto {
		private String message;
		private Map<String, Object> params;

		public EditInfoTextResponseDto(String message,
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

	@RequestMapping(value = "admin/editInfoText", method = RequestMethod.PUT)
	@ResponseBody
	protected EditInfoTextResponseDto handleSubmit(
			@RequestBody EditInfoTextRequestDto form) throws Exception {
		EditInfoTextResponseDto response = null;
                try{
		final InfoText infoText = getDataBinder().readFromString(
				form.getValues());
		final boolean isInsert = infoText.getId() == null;

		infoTextService.save(infoText);
		String message = null;
		if (isInsert) {
			message = "infoText.inserted";
		} else {
			message = "infoText.modified";
		}

		final Map<String, Object> params = new HashMap<String, Object>();
		params.put("infoTextId", infoText.getId());
		response = new EditInfoTextResponseDto(message,
				params);}
                catch(Exception e){
                    e.printStackTrace();
                }
		return response;
	}

//	protected void prepareForm(final ActionContext context) throws Exception {
//		final EditInfoTextForm form = context.getForm();
//		if (form.getInfoTextId() != null) {
//			final InfoText infoText = infoTextService
//					.load(form.getInfoTextId());
//			context.getRequest().setAttribute("currentInfoText", infoText);
//		}
//		context.getRequest()
//				.setAttribute(
//						"hasManagePermissions",
//						permissionService
//								.hasPermission(AdminSystemPermission.INFO_TEXTS_MANAGE));
//	}

//	protected void validateForm(final ActionContext context) {
//		final EditInfoTextForm form = context.getForm();
//		final InfoText infoText = getDataBinder().readFromString(
//				form.getValues());
//		infoTextService.validate(infoText);
//	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private DataBinder<InfoText> getDataBinder() {
		if (dataBinder == null) {
			final BeanBinder<InfoText> binder = BeanBinder
					.instance(InfoText.class);

			final LocalSettings settings = settingsService.getLocalSettings();
			binder.registerBinder(
					"id",
					PropertyBinder.instance(Long.class, "id",
							IdConverter.instance()));
			binder.registerBinder("validity",
					DataBinderHelper.rawPeriodBinder(settings, "validity"));
			binder.registerBinder("aliases", PropertyBinder.instance(
					TreeSet.class, "aliases", new SetConverter(TreeSet.class,
							",")));
			binder.registerBinder("subject",
					PropertyBinder.instance(String.class, "subject"));
			binder.registerBinder("body",
					PropertyBinder.instance(String.class, "body"));
			binder.registerBinder("enabled",
					PropertyBinder.instance(Boolean.TYPE, "enabled"));
			dataBinder = binder;
		}
		return dataBinder;
	}
}
