package nl.strohalm.cyclos.webservices.rest.members.imports;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import nl.strohalm.cyclos.annotations.Inject;
import nl.strohalm.cyclos.controls.ActionContext;
//import nl.strohalm.cyclos.controls.members.imports.ImportedMembersDetailsForm;
import nl.strohalm.cyclos.entities.accounts.AccountType;
import nl.strohalm.cyclos.entities.accounts.MemberAccountType;
import nl.strohalm.cyclos.entities.members.imports.ImportedMember;
import nl.strohalm.cyclos.entities.members.imports.ImportedMemberQuery;
import nl.strohalm.cyclos.entities.members.imports.MemberImport;
import nl.strohalm.cyclos.entities.members.imports.ImportedMemberQuery.Status;
import nl.strohalm.cyclos.services.elements.MemberImportService;
import nl.strohalm.cyclos.utils.RelationshipHelper;
import nl.strohalm.cyclos.utils.binding.BeanBinder;
import nl.strohalm.cyclos.utils.binding.DataBinder;
import nl.strohalm.cyclos.utils.binding.DataBinderHelper;
import nl.strohalm.cyclos.utils.binding.PropertyBinder;
import nl.strohalm.cyclos.utils.query.QueryParameters;
import nl.strohalm.cyclos.utils.validation.ValidationException;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;

@Controller
public class ImportedMembersDetailsController extends BaseRestController {
	private MemberImportService memberImportService;
	private DataBinder<ImportedMemberQuery> dataBinder;

	public DataBinder<ImportedMemberQuery> getDataBinder() {
		if (dataBinder == null) {
			final BeanBinder<ImportedMemberQuery> binder = BeanBinder
					.instance(ImportedMemberQuery.class);
			binder.registerBinder("memberImport",
					PropertyBinder.instance(MemberImport.class, "memberImport"));
			binder.registerBinder("status", PropertyBinder.instance(
					ImportedMemberQuery.Status.class, "status"));
			binder.registerBinder("lineNumber",
					PropertyBinder.instance(Integer.class, "lineNumber"));
			binder.registerBinder("nameOrUsername",
					PropertyBinder.instance(String.class, "nameOrUsername"));
			binder.registerBinder("pageParameters",
					DataBinderHelper.pageBinder());
			dataBinder = binder;
		}
		return dataBinder;
	}

	@Inject
	public void setMemberImportService(
			final MemberImportService memberImportService) {
		this.memberImportService = memberImportService;
	}

	public static class ImportedMembersDetailsRequestDto {
		public static enum Status {
			ALL, SUCCESS, ERROR
		}

		private static final long serialVersionUID = 5077502876178599042L;
		private MemberImport memberImport;
		private Status status;
		private Integer lineNumber;
		private String nameOrUsername;

		public Integer getLineNumber() {
			return lineNumber;
		}

		public MemberImport getMemberImport() {
			return memberImport;
		}

		public String getNameOrUsername() {
			return nameOrUsername;
		}

		public Status getStatus() {
			return status;
		}

		public void setLineNumber(final Integer lineNumber) {
			this.lineNumber = lineNumber;
		}

		public void setMemberImport(final MemberImport memberImport) {
			this.memberImport = memberImport;
		}

		public void setNameOrUsername(final String nameOrUsername) {
			this.nameOrUsername = nameOrUsername;
		}

		public void setStatus(final Status status) {
			this.status = status;
		}
	}

	public static class ImportedMembersDetailsResponseDto {
		private List<ImportedMember> members;

		public ImportedMembersDetailsResponseDto(List<ImportedMember> members) {
			super();
			this.members = members;
		}

	}

	@RequestMapping(value = "admin/importedMembersDetails", method = RequestMethod.GET)
	@ResponseBody
	protected ImportedMembersDetailsResponseDto executeQuery(
			@RequestBody ImportedMembersDetailsRequestDto form,
			final QueryParameters queryParameters) {
		ImportedMembersDetailsResponseDto response = null;
                try{
		final ImportedMemberQuery query = (ImportedMemberQuery) queryParameters;
		final List<ImportedMember> members = memberImportService
				.searchImportedMembers(query);
		response = new ImportedMembersDetailsResponseDto(
				members);}
                catch(Exception e){
                    e.printStackTrace();
                }
		return response;
	}
//
//	protected QueryParameters prepareForm(final ActionContext context) {
//		final HttpServletRequest request = context.getRequest();
//		final ImportedMembersDetailsForm form = context.getForm();
//		final ImportedMemberQuery query = getDataBinder().readFromString(
//				form.getQuery());
//		final MemberImport memberImport = memberImportService.load(query
//				.getMemberImport().getId(), RelationshipHelper.nested(
//				MemberImport.Relationships.ACCOUNT_TYPE,
//				AccountType.Relationships.CURRENCY));
//		if (memberImport == null || query.getStatus() == null) {
//			throw new ValidationException();
//		}
//		query.setMemberImport(memberImport);
//		// Check whether account type will be used
//		final MemberAccountType accountType = memberImport.getAccountType();
//		if (accountType != null) {
//			request.setAttribute("unitsPattern", accountType.getCurrency()
//					.getPattern());
//			request.setAttribute("hasCreditLimit", true);
//			// Check whether the initial balance will be used
//			if (memberImport.getInitialCreditTransferType() != null
//					|| memberImport.getInitialDebitTransferType() != null) {
//				request.setAttribute("hasBalance", true);
//			}
//		}
//		request.setAttribute("lowercaseStatus", query.getStatus().name()
//				.toLowerCase());
//		return query;
//	}

	protected boolean willExecuteQuery(final ActionContext context,
			final QueryParameters queryParameters) throws Exception {
		return true;
	}
}
