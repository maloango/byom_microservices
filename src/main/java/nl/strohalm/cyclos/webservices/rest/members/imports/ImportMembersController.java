package nl.strohalm.cyclos.webservices.rest.members.imports;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts.upload.FormFile;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import nl.strohalm.cyclos.annotations.Inject;
import nl.strohalm.cyclos.controls.ActionContext;
import nl.strohalm.cyclos.controls.members.imports.ImportMembersForm;
import nl.strohalm.cyclos.entities.accounts.MemberAccountType;
import nl.strohalm.cyclos.entities.accounts.transactions.TransferType;
import nl.strohalm.cyclos.entities.groups.AdminGroup;
import nl.strohalm.cyclos.entities.groups.Group;
import nl.strohalm.cyclos.entities.groups.GroupQuery;
import nl.strohalm.cyclos.entities.groups.MemberGroup;
import nl.strohalm.cyclos.entities.members.imports.MemberImport;
import nl.strohalm.cyclos.services.elements.MemberImportService;
import nl.strohalm.cyclos.services.groups.GroupService;
import nl.strohalm.cyclos.utils.binding.BeanBinder;
import nl.strohalm.cyclos.utils.binding.DataBinder;
import nl.strohalm.cyclos.utils.binding.PropertyBinder;
import nl.strohalm.cyclos.utils.csv.UnknownColumnException;
import nl.strohalm.cyclos.utils.validation.RequiredError;
import nl.strohalm.cyclos.utils.validation.ValidationException;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;

@Controller
public class ImportMembersController extends BaseRestController {
	private MemberImportService memberImportService;
	private DataBinder<MemberImport> dataBinder;
	private GroupService groupService;

	@Inject
	public void setMemberImportService(
			final MemberImportService memberImportService) {
		this.memberImportService = memberImportService;
	}

	public static class ImportMembersRequestDto {
		private FormFile upload;
		protected Map<String, Object> values;

		public Map<String, Object> getValues() {
			return values;
		}

		public void setValues(final Map<String, Object> values) {
			this.values = values;
		}

		public Map<String, Object> getImport() {
			return values;
		}

		public Object getImport(final String property) {
			return values.get(property);
		}

		public FormFile getUpload() {
			return upload;
		}

		public void setImport(final Map<String, Object> values) {
			this.values = values;
		}

		public void setImport(final String property, final Object value) {
			values.put(property, value);
		}

		public void setUpload(final FormFile upload) {
			this.upload = upload;
		}
	}

	public static class ImportMembersResponseDto {
		private String message;
		private Long id;

		public Long getId() {
			return id;
		}

		public void setId(Long id) {
			this.id = id;
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
	protected ImportMembersResponseDto handleSubmit(
			final ImportMembersRequestDto form) throws Exception {
		// final ImportMembersForm form = context.getForm();
		final FormFile upload = form.getUpload();
		if (upload == null || upload.getFileSize() == 0) {
			throw new ValidationException("upload", "memberImport.file",
					new RequiredError());
		}
		MemberImport memberImport = getDataBinder().readFromString(
				form.getImport());
		String message = null;
		ImportMembersResponseDto response = new ImportMembersResponseDto();
		try {
			memberImport = memberImportService.importMembers(memberImport,
					upload.getInputStream());
			Long id = memberImport.getId();
			response.setId(id);
			return response;
		} catch (final UnknownColumnException e) {
			message = "general.error.csv.unknownColumn";
			response.setMessage(message);
			return response;
		} finally {
			upload.destroy();
		}
	}

	protected void prepareForm(final ActionContext context) throws Exception {
		final HttpServletRequest request = context.getRequest();

		// Get the possible groups
		final GroupQuery groupQuery = new GroupQuery();
		groupQuery.setNatures(Group.Nature.MEMBER, Group.Nature.BROKER);
		groupQuery.setManagedBy(context.<AdminGroup> getGroup());
		groupQuery.setStatus(Group.Status.NORMAL);
		final List<? extends Group> groups = groupService.search(groupQuery);
		request.setAttribute("groups", groups);
	}

	protected void validateForm(final ActionContext context) {
		final ImportMembersForm form = context.getForm();
		final MemberImport memberImport = getDataBinder().readFromString(
				form.getImport());
		memberImportService.validate(memberImport);
	}

	private DataBinder<MemberImport> getDataBinder() {
		if (dataBinder == null) {
			final BeanBinder<MemberImport> binder = BeanBinder
					.instance(MemberImport.class);
			binder.registerBinder("group",
					PropertyBinder.instance(MemberGroup.class, "group"));
			binder.registerBinder("accountType", PropertyBinder.instance(
					MemberAccountType.class, "accountType"));
			binder.registerBinder("initialDebitTransferType", PropertyBinder
					.instance(TransferType.class, "initialDebitTransferType"));
			binder.registerBinder("initialCreditTransferType", PropertyBinder
					.instance(TransferType.class, "initialCreditTransferType"));
			dataBinder = binder;
		}
		return dataBinder;
	}
}
