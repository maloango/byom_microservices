package nl.strohalm.cyclos.webservices.rest.external;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts.action.ActionForward;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import nl.strohalm.cyclos.access.AdminSystemPermission;
import nl.strohalm.cyclos.annotations.Inject;
import nl.strohalm.cyclos.controls.ActionContext;
import nl.strohalm.cyclos.entities.accounts.AccountTypeQuery;
import nl.strohalm.cyclos.entities.accounts.MemberAccountType;
import nl.strohalm.cyclos.entities.accounts.SystemAccountType;
import nl.strohalm.cyclos.entities.accounts.external.ExternalAccount;
import nl.strohalm.cyclos.services.accounts.AccountTypeService;
import nl.strohalm.cyclos.services.accounts.MemberAccountTypeQuery;
import nl.strohalm.cyclos.services.accounts.SystemAccountTypeQuery;
import nl.strohalm.cyclos.services.accounts.external.ExternalAccountService;
import nl.strohalm.cyclos.utils.ActionHelper;
import nl.strohalm.cyclos.utils.binding.BeanBinder;
import nl.strohalm.cyclos.utils.binding.DataBinder;
import nl.strohalm.cyclos.utils.binding.PropertyBinder;
import nl.strohalm.cyclos.utils.conversion.IdConverter;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;

@Controller
public class EditExternalAccountController extends BaseRestController {
	private ExternalAccountService externalAccountService;
	private DataBinder<ExternalAccount> dataBinder;
	private AccountTypeService accountTypeService;

	@Inject
	public void setAccountTypeService(
			final AccountTypeService accountTypeService) {
		this.accountTypeService = accountTypeService;
	}

	@Inject
	public void setExternalAccountService(
			final ExternalAccountService externalAccountService) {
		this.externalAccountService = externalAccountService;
	}

	public static class EditExternalAccountRequestDto {
		private long externalAccountId;
		protected Map<String, Object> values;

		public Map<String, Object> getExternalAccount() {
			return values;
		}

		public Map<String, Object> getValues() {
			return values;
		}

		public void setValues(final Map<String, Object> values) {
			this.values = values;
		}

		public long getExternalAccountId() {
			return externalAccountId;
		}

		public void setExternalAccountId(long externalAccountId) {
			this.externalAccountId = externalAccountId;
		}

	}

	public static class EditExternalAccountResponseDto {
		public String message;

		public String getMessage() {
			return message;
		}

		public void setMessage(String message) {
			this.message = message;
		}
	}

	@RequestMapping(value = "", method = RequestMethod.PUT)
	@ResponseBody
	protected EditExternalAccountResponseDto handleSubmit(
			@RequestBody EditExternalAccountRequestDto form) throws Exception {

		ExternalAccount externalAccount = getDataBinder().readFromString(
				form.getExternalAccount());
		final boolean isInsert = externalAccount.isTransient();
		externalAccount = externalAccountService.save(externalAccount);
		EditExternalAccountResponseDto response = new EditExternalAccountResponseDto();
		if (isInsert) {
			response.setMessage("externalAccount.inserted");
		} else {
			response.setMessage("externalAccount.modified");
		}
		return response;
	}

	private DataBinder<ExternalAccount> getDataBinder() {
		if (dataBinder == null) {
			final BeanBinder<ExternalAccount> binder = BeanBinder
					.instance(ExternalAccount.class);
			binder.registerBinder(
					"id",
					PropertyBinder.instance(Long.class, "id",
							IdConverter.instance()));
			binder.registerBinder("name",
					PropertyBinder.instance(String.class, "name"));
			binder.registerBinder("systemAccountType", PropertyBinder.instance(
					SystemAccountType.class, "systemAccountType"));
			binder.registerBinder("memberAccountType", PropertyBinder.instance(
					MemberAccountType.class, "memberAccountType"));
			binder.registerBinder("description",
					PropertyBinder.instance(String.class, "description"));
			dataBinder = binder;
		}
		return dataBinder;
	}
}
