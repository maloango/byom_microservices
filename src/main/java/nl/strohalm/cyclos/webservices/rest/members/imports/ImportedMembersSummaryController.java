package nl.strohalm.cyclos.webservices.rest.members.imports;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import nl.strohalm.cyclos.annotations.Inject;
import nl.strohalm.cyclos.controls.ActionContext;
import nl.strohalm.cyclos.controls.members.imports.ImportedMembersSummaryForm;
import nl.strohalm.cyclos.entities.accounts.AccountType;
import nl.strohalm.cyclos.entities.members.imports.MemberImport;
import nl.strohalm.cyclos.services.elements.MemberImportService;
import nl.strohalm.cyclos.services.transactions.exceptions.CreditsException;
import nl.strohalm.cyclos.utils.RelationshipHelper;
import nl.strohalm.cyclos.utils.validation.ValidationException;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;

@Controller
public class ImportedMembersSummaryController extends BaseRestController {
	private MemberImportService memberImportService;

	@Inject
	public void setMemberImportService(
			final MemberImportService memberImportService) {
		this.memberImportService = memberImportService;
	}

	public static class ImportedMembersSummaryRequestDto {
		private long importId;
		private boolean sendActivationMail;

		public long getImportId() {
			return importId;
		}

		public boolean isSendActivationMail() {
			return sendActivationMail;
		}

		public void setImportId(final long importId) {
			this.importId = importId;
		}

		public void setSendActivationMail(final boolean sendActivationMail) {
			this.sendActivationMail = sendActivationMail;
		}
	}

	public static class ImportedMembersSummaryResponseDto {
		private String message;

		public String getMessage() {
			return message;
		}

		public void setMessage(String message) {
			this.message = message;
		}
	}

	@RequestMapping(value = "/admin/managePasswords", method = RequestMethod.POST)
	@ResponseBody
	protected void formAction(final ImportedMembersSummaryRequestDto form)
			throws Exception {
		// final ImportedMembersSummaryForm form = context.getForm();
		final MemberImport memberImport = getImport(form);
		String message=null;
		try {
			memberImportService.processImport(memberImport,
					form.isSendActivationMail());
		} catch (final CreditsException e) {
			/*throw new ValidationException(actionHelper.resolveErrorKey(e),
					actionHelper.resolveParameters(e));*/
			message=e.toString(); 
		}
		message = "memberImport.processed";
	}

/*	protected void prepareForm(final ActionContext context) throws Exception {
		final MemberImport memberImport = getImport(context);
		final HttpServletRequest request = context.getRequest();
		request.setAttribute("memberImport", memberImport);
		request.setAttribute("summary",
				memberImportService.getSummary(memberImport));
	}*/

	private MemberImport getImport(
			final ImportedMembersSummaryRequestDto form) {
	
		// final ImportedMembersSummaryForm form = context.getForm();
		return memberImportService.load(form.getImportId(),
				MemberImport.Relationships.GROUP, RelationshipHelper.nested(
						MemberImport.Relationships.ACCOUNT_TYPE,
						AccountType.Relationships.CURRENCY),
				MemberImport.Relationships.INITIAL_CREDIT_TRANSFER_TYPE,
				MemberImport.Relationships.INITIAL_DEBIT_TRANSFER_TYPE);
	}
}
