package nl.strohalm.cyclos.webservices.rest.accounts.external.transferimports;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

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
import nl.strohalm.cyclos.utils.Period;
import nl.strohalm.cyclos.utils.binding.BeanBinder;
import nl.strohalm.cyclos.utils.binding.DataBinder;
import nl.strohalm.cyclos.utils.binding.DataBinderHelper;
import nl.strohalm.cyclos.utils.query.QueryParameters;
import nl.strohalm.cyclos.utils.validation.ValidationException;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
@Controller
public class SearchTransferImportsController extends BaseRestController {
	private GroupService groupService;
	private ElementService elementService;
	public final GroupService getGroupService() {
		return groupService;
	}

	public final void setGroupService(GroupService groupService) {
		this.groupService = groupService;
	}

	public final ElementService getElementService() {
		return elementService;
	}

	public final void setElementService(ElementService elementService) {
		this.elementService = elementService;
	}

	public final PermissionService getPermissionService() {
		return permissionService;
	}

	public final void setPermissionService(PermissionService permissionService) {
		this.permissionService = permissionService;
	}

	public final SettingsService getSettingsService() {
		return settingsService;
	}

	public final void setSettingsService(SettingsService settingsService) {
		this.settingsService = settingsService;
	}

	public final ExternalAccountService getExternalAccountService() {
		return externalAccountService;
	}

	public final ExternalTransferImportService getExternalTransferImportService() {
		return externalTransferImportService;
	}

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
           private long              externalAccountId;
           private ExternalAccount   account;
           private Period            period;

    public ExternalAccount getAccount() {
        return account;
    }

    public Period getPeriod() {
        return period;
    }

    public void setAccount(final ExternalAccount externalAccount) {
        account = externalAccount;
    }

    public void setPeriod(final Period period) {
        this.period = period;
    }

        public long getExternalAccountId() {
        return externalAccountId;
    }

    public void setExternalAccountId(final long externalAccountId) {
        this.externalAccountId = externalAccountId;
    }
           
	}

	public static class SearchTransferImportsResponseDto {
		private List<ExternalTransferImport> externalTransferImports;

		public SearchTransferImportsResponseDto(
				List<ExternalTransferImport> imports) {
			super();
			this.externalTransferImports = imports;
		}
                public SearchTransferImportsResponseDto(){
                }

	}

	@RequestMapping(value = "admin/searchTransferImports/{externalAccountId}", method = RequestMethod.GET)
	@ResponseBody
	protected SearchTransferImportsResponseDto executeQuery(@PathVariable ("externalAccountId")long externalAccountId, ExternalTransferImportQuery ExternalTransferImportQuery) {
			
            SearchTransferImportsResponseDto response = null;
            try{
              		final List<ExternalTransferImport> imports = externalTransferImportService.search((ExternalTransferImportQuery));
				
		 response = new SearchTransferImportsResponseDto(imports);}
            catch(Exception e){
                e.printStackTrace();
            }
			
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
