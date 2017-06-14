package nl.strohalm.cyclos.webservices.rest.accounts.accountfees;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import nl.strohalm.cyclos.annotations.Inject;
import nl.strohalm.cyclos.controls.ActionContext;
import nl.strohalm.cyclos.controls.accounts.accountfees.AccountFeeLogDetailsForm;
import nl.strohalm.cyclos.entities.accounts.fees.account.AccountFee;
import nl.strohalm.cyclos.entities.accounts.fees.account.AccountFee.InvoiceMode;
import nl.strohalm.cyclos.entities.accounts.fees.account.AccountFeeLog;
import nl.strohalm.cyclos.entities.accounts.fees.account.AccountFeeLogDetailsDTO;
import nl.strohalm.cyclos.entities.accounts.fees.account.MemberAccountFeeLog;
import nl.strohalm.cyclos.entities.accounts.fees.account.MemberAccountFeeLogQuery;
import nl.strohalm.cyclos.entities.groups.MemberGroup;
import nl.strohalm.cyclos.entities.members.Member;
import nl.strohalm.cyclos.services.accountfees.AccountFeeService;
import nl.strohalm.cyclos.services.elements.ElementService;
import nl.strohalm.cyclos.services.groups.GroupService;
import nl.strohalm.cyclos.services.permissions.PermissionService;
import nl.strohalm.cyclos.services.settings.SettingsService;
import nl.strohalm.cyclos.utils.binding.BeanBinder;
import nl.strohalm.cyclos.utils.binding.DataBinder;
import nl.strohalm.cyclos.utils.binding.DataBinderHelper;
import nl.strohalm.cyclos.utils.binding.PropertyBinder;
import nl.strohalm.cyclos.utils.binding.SimpleCollectionBinder;
import nl.strohalm.cyclos.utils.query.QueryParameters;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;

@Controller
public class AccountFeeLogDetailsController extends BaseRestController {
	private AccountFeeService accountFeeService;

	private DataBinder<MemberAccountFeeLogQuery> dataBinder;
	private GroupService groupService;
	private ElementService elementService;
	private PermissionService permissionService;
	private SettingsService settingsService;

	@Inject
	public void setAccountFeeService(final AccountFeeService accountFeeService) {
		this.accountFeeService = accountFeeService;
	}

	public static class AccountFeeLogDetailsRequestDto {

	}

	public static class AccountFeeLogDetailsResponseDto {
		List<MemberAccountFeeLog> members;

		public AccountFeeLogDetailsResponseDto(List<MemberAccountFeeLog> members) {
			super();
			this.members = members;
		}

	}

	@RequestMapping(value = "admin/viewAccountFeeLogDetails", method = RequestMethod.GET)
	@ResponseBody
	protected AccountFeeLogDetailsResponseDto executeQuery(
			@RequestBody AccountFeeLogDetailsRequestDto context,
			final QueryParameters queryParameters) {
		// final HttpServletRequest request = context.getRequest();
		final MemberAccountFeeLogQuery query = (MemberAccountFeeLogQuery) queryParameters;
		final List<MemberAccountFeeLog> members = accountFeeService
				.searchMembers(query);
		AccountFeeLogDetailsResponseDto response = new AccountFeeLogDetailsResponseDto(
				members);
		return response;
	}

	protected QueryParameters prepareForm(final ActionContext context) {
		final HttpServletRequest request = context.getRequest();
		final AccountFeeLogDetailsForm form = context.getForm();
		final Long accountFeeLogId = form.getAccountFeeLogId();

		// Get the details
		final AccountFeeLogDetailsDTO details = accountFeeService
				.getLogDetails(accountFeeLogId);
		final AccountFeeLog log = details.getAccountFeeLog();
		final AccountFee fee = log.getAccountFee();
		final boolean invoiceAlways = fee.getInvoiceMode() == InvoiceMode.ALWAYS;
		final boolean invoiceNever = fee.getInvoiceMode() == InvoiceMode.NEVER;

		// Prepare the query
		final MemberAccountFeeLogQuery query = getDataBinder().readFromString(
				form.getQuery());
		query.setAccountFeeLog(log);
		if (query.getStatus() == null) {
			query.setStatus(log.getFailedMembers() == 0 ? MemberAccountFeeLogQuery.Status.PROCESSED
					: MemberAccountFeeLogQuery.Status.ERROR);
			form.setQuery("status", query.getStatus().name());
		}
		if (query.getMember() != null) {
			query.setMember((Member) elementService.load(query.getMember()
					.getId()));
		}

		// Get the possible statuses for search
		final Set<MemberAccountFeeLogQuery.Status> statuses = EnumSet
				.allOf(MemberAccountFeeLogQuery.Status.class);
		if (invoiceAlways) {
			statuses.remove(MemberAccountFeeLogQuery.Status.TRANSFER);
		} else if (invoiceNever) {
			statuses.remove(MemberAccountFeeLogQuery.Status.INVOICE);
			statuses.remove(MemberAccountFeeLogQuery.Status.ACCEPTED_INVOICE);
			statuses.remove(MemberAccountFeeLogQuery.Status.OPEN_INVOICE);
		}

		// Get the possible groups
		final List<MemberGroup> groups = new ArrayList<MemberGroup>(
				permissionService.getManagedMemberGroups());
		Collections.sort(groups);

		final boolean isRunning = log.getFinishDate() == null
				|| log.isRechargingFailed();

		// Store the request attributes
		request.setAttribute("details", details);
		request.setAttribute("log", log);
		request.setAttribute("fee", fee);
		request.setAttribute("currencyPattern", fee.getAccountType()
				.getCurrency().getPattern());
		request.setAttribute("invoiceAlways", invoiceAlways);
		request.setAttribute("invoiceNever", invoiceNever);
		request.setAttribute("statuses", statuses);
		request.setAttribute("groups", groups);
		request.setAttribute("isRunning", isRunning);
		return query;
	}

	protected boolean willExecuteQuery(final ActionContext context,
			final QueryParameters queryParameters) throws Exception {
		return true;
	}

	private DataBinder<MemberAccountFeeLogQuery> getDataBinder() {
		if (dataBinder == null) {
			final BeanBinder<MemberAccountFeeLogQuery> binder = BeanBinder
					.instance(MemberAccountFeeLogQuery.class);
			binder.registerBinder("accountFeeLog", PropertyBinder.instance(
					AccountFeeLog.class, "accountFeeLog"));
			binder.registerBinder("status", PropertyBinder.instance(
					MemberAccountFeeLogQuery.Status.class, "status"));
			binder.registerBinder("groups", SimpleCollectionBinder.instance(
					MemberGroup.class, "groups"));
			binder.registerBinder("member",
					PropertyBinder.instance(Member.class, "member"));
			binder.registerBinder("pageParameters",
					DataBinderHelper.pageBinder());
			dataBinder = binder;
		}
		return dataBinder;
	}

}
