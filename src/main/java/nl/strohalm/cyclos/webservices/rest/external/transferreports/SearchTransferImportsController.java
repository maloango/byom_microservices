package nl.strohalm.cyclos.webservices.rest.external.transferreports;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import nl.strohalm.cyclos.annotations.Inject;
import nl.strohalm.cyclos.controls.ActionContext;
import nl.strohalm.cyclos.controls.accounts.external.transferimports.SearchTransferImportsForm;
import nl.strohalm.cyclos.entities.accounts.external.ExternalAccount;
import nl.strohalm.cyclos.entities.accounts.external.ExternalTransferImport;
import nl.strohalm.cyclos.entities.accounts.external.ExternalTransferImportQuery;
import nl.strohalm.cyclos.entities.settings.LocalSettings;
import nl.strohalm.cyclos.services.accounts.external.ExternalAccountService;
import nl.strohalm.cyclos.services.accounts.external.ExternalTransferImportService;
import nl.strohalm.cyclos.services.elements.ElementService;
import nl.strohalm.cyclos.services.groups.GroupService;
import nl.strohalm.cyclos.services.permissions.PermissionService;
import nl.strohalm.cyclos.services.settings.SettingsService;
import nl.strohalm.cyclos.utils.binding.BeanBinder;
import nl.strohalm.cyclos.utils.binding.DataBinder;
import nl.strohalm.cyclos.utils.binding.DataBinderHelper;
import nl.strohalm.cyclos.utils.query.QueryParameters;
import nl.strohalm.cyclos.utils.validation.ValidationException;

public class SearchTransferImportsController {
	private GroupService groupService;
	private ElementService elementService;
	private PermissionService permissionService;
	private SettingsService settingsService;
	private ExternalAccountService externalAccountService;
	private ExternalTransferImportService externalTransferImportService;
	private DataBinder<ExternalTransferImportQuery> dataBinder;

	@Inject
	public void setExternalAccountService(
			final ExternalAccountService externalAccountService) {
		this.externalAccountService = externalAccountService;
	}

	@Inject
	public void setExternalTransferImportService(
			final ExternalTransferImportService externalTransferImportService) {
		this.externalTransferImportService = externalTransferImportService;
	}

	public static class SearchTransferImportsRequestDto {

	}

	public static class SearchTransferImportsResponseDto {
		private List<ExternalTransferImport> externalTransferImports;

		public SearchTransferImportsResponseDto(
				List<ExternalTransferImport> imports) {
			super();
			this.externalTransferImports = imports;
		}

	}

	@RequestMapping(value = "/admin/managePasswords", method = RequestMethod.POST)
	@ResponseBody
	protected SearchTransferImportsResponseDto executeQuery(
			@RequestBody SearchTransferImportsRequestDto context,
			final QueryParameters queryParameters) {
		final List<ExternalTransferImport> imports = externalTransferImportService
				.search((ExternalTransferImportQuery) queryParameters);
		SearchTransferImportsResponseDto response = new SearchTransferImportsResponseDto(
				imports);
		return response;
	}

	protected QueryParameters prepareForm(final ActionContext context) {
		final SearchTransferImportsForm form = context.getForm();
		final HttpServletRequest request = context.getRequest();
		final long externalAccountId = form.getExternalAccountId();
		if (externalAccountId <= 0L) {
			throw new ValidationException();
		}
		final ExternalAccount externalAccount = externalAccountService
				.load(externalAccountId);
		request.setAttribute("externalAccount", externalAccount);

		final boolean editable = true;
		request.setAttribute("editable", editable);

		final ExternalTransferImportQuery query = getDataBinder()
				.readFromString(form.getQuery());
		query.setAccount(externalAccount);
		return query;
	}

	protected boolean willExecuteQuery(final ActionContext context,
			final QueryParameters queryParameters) throws Exception {
		return true;
	}

	private DataBinder<ExternalTransferImportQuery> getDataBinder() {
		if (dataBinder == null) {
			final LocalSettings localSettings = settingsService
					.getLocalSettings();
			final BeanBinder<ExternalTransferImportQuery> binder = BeanBinder
					.instance(ExternalTransferImportQuery.class);
			binder.registerBinder("period",
					DataBinderHelper.periodBinder(localSettings, "period"));
			binder.registerBinder("pageParameters",
					DataBinderHelper.pageBinder());
			dataBinder = binder;
		}
		return dataBinder;
	}
}
