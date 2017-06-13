package nl.strohalm.cyclos.webservices.rest.accounts.external.transferreports;

import java.io.InputStreamReader;
import java.io.Reader;

import nl.strohalm.cyclos.annotations.Inject;
import nl.strohalm.cyclos.controls.ActionContext;
import nl.strohalm.cyclos.controls.accounts.external.transferimports.NewTransferImportForm;
import nl.strohalm.cyclos.entities.accounts.external.ExternalAccount;
import nl.strohalm.cyclos.entities.accounts.external.ExternalTransferImport;
import nl.strohalm.cyclos.entities.settings.LocalSettings;
import nl.strohalm.cyclos.services.accounts.external.ExternalAccountService;
import nl.strohalm.cyclos.services.accounts.external.ExternalTransferImportService;
import nl.strohalm.cyclos.services.settings.SettingsService;
import nl.strohalm.cyclos.utils.ActionHelper;
import nl.strohalm.cyclos.utils.transactionimport.IllegalTransactionFileFormatException;
import nl.strohalm.cyclos.utils.validation.ValidationException;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;

import org.apache.commons.lang.StringUtils;
import org.apache.struts.action.ActionForward;
import org.apache.struts.upload.FormFile;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class NewTransferImportController extends BaseRestController {

	private ExternalAccountService externalAccountService;
	private ExternalTransferImportService externalTransferImportService;
	private SettingsService settingsService;

	public void setSettingsService(SettingsService settingsService) {
		this.settingsService = settingsService;
	}

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

	public static class NewTransferImportRequestDto {
		private long externalAccountId;
		private FormFile file;

		public long getExternalAccountId() {
			return externalAccountId;
		}

		public FormFile getFile() {
			return file;
		}

		public void setExternalAccountId(final long externalAccountId) {
			this.externalAccountId = externalAccountId;
		}

		public void setFile(final FormFile file) {
			this.file = file;
		}
	}

	public static class NewTransferImportResponseDto {
		private String message;
		private long transferImportId;

		public long getTransferImportId() {
			return transferImportId;
		}

		public void setTransferImportId(long transferImportId) {
			this.transferImportId = transferImportId;
		}

		public String getMessage() {
			return message;
		}

		public void setMessage(String message) {
			this.message = message;
		}
	}

	@RequestMapping(value = "/admin/managePasswords", method = RequestMethod.POST)
	@ResponseBody
	protected NewTransferImportResponseDto executeAction(
			@RequestBody NewTransferImportRequestDto form) throws Exception {
		final LocalSettings settings = settingsService.getLocalSettings();
		// final NewTransferImportForm form = context.getForm();
		final long externalAccountId = form.getExternalAccountId();
		if (externalAccountId <= 0L) {
			throw new ValidationException();
		}
		final FormFile file = form.getFile();
		if (file == null || file.getFileSize() == 0) {
			throw new ValidationException();
		}

		final ExternalAccount externalAccount = externalAccountService.load(
				externalAccountId, ExternalAccount.Relationships.FILE_MAPPING);
		String message = null;
		NewTransferImportResponseDto response = new NewTransferImportResponseDto();
		try {
			final Reader in = new InputStreamReader(file.getInputStream(),
					settings.getCharset());
			final ExternalTransferImport transferImport = externalTransferImportService
					.importNew(externalAccount.getFileMapping(), in);
			message = "externalTransferImport.imported";
			long transferImportId = transferImport.getId();
			response.setTransferImportId(transferImportId);
			response.setMessage(message);
			return response;
		} catch (final IllegalTransactionFileFormatException e) {
			final String errmessage = e.getMessage();
			if (StringUtils.isEmpty(errmessage)) {
				message = "externalTransferImport.error.format.detailed";
				response.setMessage(message);
				return response;
				// externalTransferImport.error.format.detailed;e.getLine(),
				// e.getColumn(), e.getField(), e.getValue());
			} else {
				message = "externalTransferImport.error.format.general";
				response.setMessage(message);
				return response;
			}
		} catch (final Exception e) {
			message = "externalTransferImport.error.importing";
			response.setMessage(message);
			return response;
		}

	}
}
