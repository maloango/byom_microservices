package nl.strohalm.cyclos.webservices.rest.accounts.external;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import nl.strohalm.cyclos.access.AdminSystemPermission;
import nl.strohalm.cyclos.annotations.Inject;
import nl.strohalm.cyclos.controls.ActionContext;
import nl.strohalm.cyclos.controls.accounts.external.EditExternalTransferTypeForm;
import nl.strohalm.cyclos.entities.accounts.external.ExternalAccount;
import nl.strohalm.cyclos.entities.accounts.external.ExternalTransferType;
import nl.strohalm.cyclos.entities.accounts.external.ExternalTransferType.Action;
import nl.strohalm.cyclos.entities.accounts.transactions.TransferType;
import nl.strohalm.cyclos.entities.accounts.transactions.TransferTypeQuery;
import nl.strohalm.cyclos.services.accounts.external.ExternalAccountService;
import nl.strohalm.cyclos.services.accounts.external.ExternalTransferTypeService;
import nl.strohalm.cyclos.services.permissions.PermissionService;
import nl.strohalm.cyclos.services.transactions.TransactionContext;
import nl.strohalm.cyclos.services.transfertypes.TransferTypeService;
import nl.strohalm.cyclos.utils.ActionHelper;
import nl.strohalm.cyclos.utils.RequestHelper;
import nl.strohalm.cyclos.utils.binding.BeanBinder;
import nl.strohalm.cyclos.utils.binding.DataBinder;
import nl.strohalm.cyclos.utils.binding.PropertyBinder;
import nl.strohalm.cyclos.utils.conversion.IdConverter;
import nl.strohalm.cyclos.utils.validation.ValidationException;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;

import org.apache.struts.action.ActionForward;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class EditExternalTransferTypeController extends BaseRestController {
	private ExternalTransferTypeService externalTransferTypeService;
	private DataBinder<ExternalTransferType> dataBinder;
	private TransferTypeService transferTypeService;
	private ExternalAccountService externalAccountService;
	private PermissionService permissionService;

	public PermissionService getPermissionService() {
		return permissionService;
	}

	public void setPermissionService(PermissionService permissionService) {
		this.permissionService = permissionService;
	}

	@Inject
	public void setExternalAccountService(
			final ExternalAccountService externalAccountService) {
		this.externalAccountService = externalAccountService;
	}

	@Inject
	public void setExternalTransferTypeService(
			final ExternalTransferTypeService externalTransferTypeService) {
		this.externalTransferTypeService = externalTransferTypeService;
	}

	@Inject
	public void setTransferTypeService(
			final TransferTypeService transferTypeService) {
		this.transferTypeService = transferTypeService;
	}

	public static class EditExternalTransferTypeRequestDTO {
		private long externalTransferTypeId;
		private long account;
		protected Map<String, Object> values;

		public Map<String, Object> getValues() {
			return values;
		}

		public void setValues(final Map<String, Object> values) {
			this.values = values;
		}

		public Map<String, Object> getExternalTransferType() {
			return values;
		}

		public long getAccount() {
			return account;
		}

		public void setAccount(long account) {
			this.account = account;
		}

		public long getExternalTransferTypeId() {
			return externalTransferTypeId;
		}

		public void setExternalTransferTypeId(long externalTransferTypeId) {
			this.externalTransferTypeId = externalTransferTypeId;
		}

	}

	public static class EditExternalTransferTypeResponseDTO {
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
	protected EditExternalTransferTypeResponseDTO handleSubmit(
			@RequestBody EditExternalTransferTypeRequestDTO form)
			throws Exception {

		ExternalTransferType externalTransferType = getDataBinder()
				.readFromString(form.getExternalTransferType());
		final boolean isInsert = externalTransferType.isTransient();
		externalTransferType = externalTransferTypeService
				.save(externalTransferType);
		EditExternalTransferTypeResponseDTO response = new EditExternalTransferTypeResponseDTO();
		if (isInsert) {
			response.setMessage("externalTransferType.inserted");
		} else {
			response.setMessage("externalTransferType.modified");
		}
		return response;
	}


	private DataBinder<ExternalTransferType> getDataBinder() {
		if (dataBinder == null) {

			final BeanBinder<ExternalTransferType> binder = BeanBinder
					.instance(ExternalTransferType.class);
			binder.registerBinder(
					"id",
					PropertyBinder.instance(Long.class, "id",
							IdConverter.instance()));
			binder.registerBinder("name",
					PropertyBinder.instance(String.class, "name"));
			binder.registerBinder("code",
					PropertyBinder.instance(String.class, "code"));
			binder.registerBinder("description",
					PropertyBinder.instance(String.class, "description"));
			binder.registerBinder("action",
					PropertyBinder.instance(Action.class, "action"));
			binder.registerBinder("account",
					PropertyBinder.instance(ExternalAccount.class, "account"));
			binder.registerBinder("transferType",
					PropertyBinder.instance(TransferType.class, "transferType"));

			dataBinder = binder;
		}
		return dataBinder;
	}
}
