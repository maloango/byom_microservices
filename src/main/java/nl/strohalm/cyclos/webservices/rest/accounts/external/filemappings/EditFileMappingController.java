package nl.strohalm.cyclos.webservices.rest.accounts.external.filemappings;

import java.util.Collection;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import nl.strohalm.cyclos.access.AdminSystemPermission;
import nl.strohalm.cyclos.annotations.Inject;
import nl.strohalm.cyclos.controls.ActionContext;
import nl.strohalm.cyclos.controls.accounts.external.filemappings.EditFileMappingForm;
import nl.strohalm.cyclos.entities.accounts.external.ExternalAccount;
import nl.strohalm.cyclos.entities.accounts.external.filemapping.CSVFileMapping;
import nl.strohalm.cyclos.entities.accounts.external.filemapping.CustomFileMapping;
import nl.strohalm.cyclos.entities.accounts.external.filemapping.FieldMapping;
import nl.strohalm.cyclos.entities.accounts.external.filemapping.FileMapping;
import nl.strohalm.cyclos.entities.accounts.external.filemapping.FileMappingWithFields;
import nl.strohalm.cyclos.services.accounts.external.ExternalAccountService;
import nl.strohalm.cyclos.services.accounts.external.filemapping.FileMappingService;
import nl.strohalm.cyclos.services.elements.ElementService;
import nl.strohalm.cyclos.services.groups.GroupService;
import nl.strohalm.cyclos.services.permissions.PermissionService;
import nl.strohalm.cyclos.services.settings.SettingsService;
import nl.strohalm.cyclos.utils.ActionHelper;
import nl.strohalm.cyclos.utils.RelationshipHelper;
import nl.strohalm.cyclos.utils.RequestHelper;
import nl.strohalm.cyclos.utils.binding.BeanBinder;
import nl.strohalm.cyclos.utils.binding.DataBinder;
import nl.strohalm.cyclos.utils.binding.PropertyBinder;
import nl.strohalm.cyclos.utils.conversion.IdConverter;
import nl.strohalm.cyclos.utils.conversion.ReferenceConverter;
import nl.strohalm.cyclos.utils.validation.ValidationException;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;

import org.apache.struts.action.ActionForward;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class EditFileMappingController extends BaseRestController {
	private GroupService groupService;
	private ElementService elementService;
	private PermissionService permissionService;
	private SettingsService settingsService;
	private ExternalAccountService externalAccountService;
	private FileMappingService fileMappingService;
	private Map<FileMapping.Nature, DataBinder<? extends FileMapping>> dataBinders;

	public DataBinder<? extends FileMapping> getDataBinder(
			final FileMapping.Nature nature) {
		if (dataBinders == null) {
			dataBinders = new EnumMap<FileMapping.Nature, DataBinder<? extends FileMapping>>(
					FileMapping.Nature.class);

			final BeanBinder<CSVFileMapping> csvFileMappingBinder = BeanBinder
					.instance(CSVFileMapping.class);
			initBasicFileMapping(csvFileMappingBinder);
			initFileMappingWithFields(csvFileMappingBinder);
			csvFileMappingBinder.registerBinder("stringQuote",
					PropertyBinder.instance(Character.class, "stringQuote"));
			csvFileMappingBinder
					.registerBinder("columnSeparator", PropertyBinder.instance(
							Character.class, "columnSeparator"));
			csvFileMappingBinder.registerBinder("headerLines",
					PropertyBinder.instance(Integer.class, "headerLines"));
			dataBinders.put(FileMapping.Nature.CSV, csvFileMappingBinder);

			final BeanBinder<CustomFileMapping> customFileMappingBinder = BeanBinder
					.instance(CustomFileMapping.class);
			initBasicFileMapping(customFileMappingBinder);
			customFileMappingBinder.registerBinder("className",
					PropertyBinder.instance(String.class, "className"));
			dataBinders.put(FileMapping.Nature.CUSTOM, customFileMappingBinder);
		}
		return dataBinders.get(nature);
	}

	@Inject
	public void setExternalAccountService(
			final ExternalAccountService externalAccountService) {
		this.externalAccountService = externalAccountService;
	}

	public static class EditFileMappingRequestDto {
		private long externalAccountId;
		protected Map<String, Object> values;

		public Map<String, Object> getValues() {
			return values;
		}

		public void setValues(final Map<String, Object> values) {
			this.values = values;
		}

		public long getExternalAccountId() {
			return externalAccountId;
		}

		public Map<String, Object> getFileMapping() {
			return values;
		}

		public Object getFileMapping(final String key) {
			return values.get(key);
		}

		public void setExternalAccountId(final long externalAccountId) {
			this.externalAccountId = externalAccountId;
		}

		public void setFileMapping(final Map<String, Object> map) {
			values = map;
		}

		public void setFileMapping(final String key, final Object value) {
			values.put(key, value);
		}
	}

	public static class EditFileMappingResponseDto {
		private String message;
		long externalAccountId;

		public EditFileMappingResponseDto(String message, long externalAccountId) {
			super();
			this.message = message;
			this.externalAccountId = externalAccountId;
		}

		public String getMessage() {
			return message;
		}

		public void setMessage(String message) {
			this.message = message;
		}
	}

	@Inject
	public void setFileMappingService(
			final FileMappingService fileMappingService) {
		this.fileMappingService = fileMappingService;
	}

	@RequestMapping(value = "/admin/managePasswords", method = RequestMethod.POST)
	@ResponseBody
	protected EditFileMappingResponseDto handleSubmit(
			@RequestBody EditFileMappingRequestDto form) throws Exception {
		final FileMapping fileMapping = resolveFileMapping(form);
		final boolean isInsert = fileMapping.isTransient();
		fileMappingService.save(fileMapping);
		String message = null;

		if (isInsert)
			message = "fileMapping.inserted";
		else
			message = "fileMapping.modified";
		long externalAccountId = fileMapping.getAccount().getId();
		EditFileMappingResponseDto response = new EditFileMappingResponseDto(
				message, externalAccountId);
		return response;
	}
/*
	protected void prepareForm(final EditFileMappingRequestDto context)
			throws Exception {
		// final HttpServletRequest request = context.getRequest();
		final EditFileMappingForm form = context.getForm();
		final long externalAccountId = form.getExternalAccountId();
		if (externalAccountId <= 0) {
			throw new ValidationException();
		}
		final ExternalAccount externalAccount = externalAccountService.load(
				externalAccountId, RelationshipHelper.nested(
						ExternalAccount.Relationships.FILE_MAPPING,
						FileMappingWithFields.Relationships.FIELDS));

		final FileMapping fileMapping = externalAccount.getFileMapping();
		final boolean isInsert = (fileMapping == null);
		if (!isInsert) {
			final DataBinder<? extends FileMapping> dataBinder = getDataBinder(fileMapping
					.getNature());
			dataBinder.writeAsString(form.getFileMapping(), fileMapping);
			request.setAttribute("fileMapping", fileMapping);
			if (fileMapping instanceof FileMappingWithFields) {
				final FileMappingWithFields fmWithFields = (FileMappingWithFields) fileMapping;
				final Collection<FieldMapping> fieldMappings = fmWithFields
						.getFields();
				request.setAttribute("fieldMappings", fieldMappings);
			}
		} else {
			// Set default values on the form
			form.setFileMapping("columnSeparator",
					CSVFileMapping.DEFAULT_COLUMN_SEPARATOR);
			form.setFileMapping("stringQuote",
					CSVFileMapping.DEFAULT_STRING_QUOTE);
			form.setFileMapping("headerLines",
					CSVFileMapping.DEFAULT_HEADER_LINES);
			form.setFileMapping("dateFormat",
					FileMappingWithFields.DEFAULT_DATE_FORMAT);
			form.setFileMapping("numberFormat",
					FileMappingWithFields.DEFAULT_NUMBER_FORMAT);
			form.setFileMapping("decimalPlaces",
					FileMappingWithFields.DEFAULT_DECIMAL_PLACES);
			form.setFileMapping("decimalSeparator",
					FileMappingWithFields.DEFAULT_DECIMAL_SEPARATOR);
			form.setFileMapping("negativeAmountValue",
					FileMappingWithFields.DEFAULT_NEGATIVE_AMOUNT_VALUE);
		}
		final boolean editable = permissionService
				.hasPermission(AdminSystemPermission.EXTERNAL_ACCOUNTS_MANAGE);
		form.setFileMapping("account", externalAccount.getId());
		request.setAttribute("externalAccountId", externalAccount.getId());
		request.setAttribute("editable", editable);
		request.setAttribute("isInsert", isInsert);
		RequestHelper.storeEnum(request, FileMapping.Nature.class, "natures");
		RequestHelper.storeEnum(request,
				FileMappingWithFields.NumberFormat.class, "numberFormats");
	}

	protected void validateForm(final ActionContext context) {
		final FileMapping fileMapping = resolveFileMapping(context);
		fileMappingService.validate(fileMapping);
	}

*/	private void initBasicFileMapping(
			final BeanBinder<? extends FileMapping> fileMappingBinder) {
		fileMappingBinder.registerBinder(
				"id",
				PropertyBinder.instance(Long.class, "id",
						IdConverter.instance()));
		fileMappingBinder.registerBinder("account", PropertyBinder.instance(
				ExternalAccount.class, "account",
				ReferenceConverter.instance(ExternalAccount.class)));
	}

	private void initFileMappingWithFields(
			final BeanBinder<? extends FileMappingWithFields> fileMappingWithFieldsBinder) {
		fileMappingWithFieldsBinder.registerBinder("numberFormat",
				PropertyBinder.instance(
						FileMappingWithFields.NumberFormat.class,
						"numberFormat"));
		fileMappingWithFieldsBinder.registerBinder("decimalPlaces",
				PropertyBinder.instance(Integer.class, "decimalPlaces"));
		fileMappingWithFieldsBinder.registerBinder("decimalSeparator",
				PropertyBinder.instance(Character.class, "decimalSeparator"));
		fileMappingWithFieldsBinder.registerBinder("negativeAmountValue",
				PropertyBinder.instance(String.class, "negativeAmountValue"));
		fileMappingWithFieldsBinder.registerBinder("dateFormat",
				PropertyBinder.instance(String.class, "dateFormat"));
	}

	private FileMapping resolveFileMapping(
			final EditFileMappingRequestDto form) {
		//final EditFileMappingForm form = context.getForm();
		FileMapping.Nature nature = null;
		final String fileMappingNature = (String) form.getFileMapping("nature");
		if ("CSV".equals(fileMappingNature)) {
			nature = FileMapping.Nature.CSV;
		} else {
			nature = FileMapping.Nature.CUSTOM;
		}
		return getDataBinder(nature).readFromString(form.getFileMapping());
	}

}
