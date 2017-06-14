package nl.strohalm.cyclos.webservices.rest.accounts.accountfees;

import java.util.Collection;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import nl.strohalm.cyclos.annotations.Inject;
import nl.strohalm.cyclos.controls.ActionContext;
import nl.strohalm.cyclos.controls.accounts.accountfees.AccountFeeLogForm;
import nl.strohalm.cyclos.entities.accounts.AccountType;
import nl.strohalm.cyclos.entities.accounts.fees.account.AccountFee;
import nl.strohalm.cyclos.entities.accounts.fees.account.AccountFeeLog;
import nl.strohalm.cyclos.entities.accounts.fees.account.AccountFeeLogQuery;
import nl.strohalm.cyclos.entities.accounts.fees.account.AccountFeeQuery;
import nl.strohalm.cyclos.entities.groups.AdminGroup;
import nl.strohalm.cyclos.entities.groups.MemberGroup;
import nl.strohalm.cyclos.services.accountfees.AccountFeeService;
import nl.strohalm.cyclos.services.groups.GroupService;
import nl.strohalm.cyclos.utils.RelationshipHelper;
import nl.strohalm.cyclos.utils.binding.BeanBinder;
import nl.strohalm.cyclos.utils.binding.DataBinder;
import nl.strohalm.cyclos.utils.binding.DataBinderHelper;
import nl.strohalm.cyclos.utils.query.QueryParameters;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;

@Controller
public class ListAccountFeeLogController extends BaseRestController {
	private AccountFeeService accountFeeService;

	private DataBinder<AccountFeeLogQuery> dataBinder;
	private GroupService groupService;

	public AccountFeeService getAccountFeeService() {
		return accountFeeService;
	}

	@Inject
	public void setAccountFeeService(final AccountFeeService accountFeeService) {
		this.accountFeeService = accountFeeService;
	}

	public static class ListAccountFeeLogRequestDto {

	}

	public static class ListAccountFeeLogResponseDto {
		private List<AccountFeeLog> accountFeeLogs;

		public ListAccountFeeLogResponseDto(List<AccountFeeLog> accountFeeLogs) {
			super();
			this.accountFeeLogs = accountFeeLogs;
		}
	}

	@RequestMapping(value = "admin/listAccountFeeLog", method = RequestMethod.GET)
	@ResponseBody
	protected ListAccountFeeLogResponseDto executeQuery(
			@RequestBody ListAccountFeeLogRequestDto context,
			final QueryParameters queryParameters) {
		// final HttpServletRequest request = context.getRequest();
		final AccountFeeLogQuery query = (AccountFeeLogQuery) queryParameters;
		final List<AccountFeeLog> logs = accountFeeService.searchLogs(query);
		ListAccountFeeLogResponseDto response = new ListAccountFeeLogResponseDto(
				logs);
		return response;
	}

	protected QueryParameters prepareForm(final ActionContext context) {
		final HttpServletRequest request = context.getRequest();

		// Groups managed by the admin group
		AdminGroup adminGroup = context.getGroup();
		adminGroup = groupService.load(adminGroup.getId(),
				AdminGroup.Relationships.MANAGES_GROUPS);
		final Collection<MemberGroup> managedGroups = (adminGroup
				.getManagesGroups());

		final AccountFeeQuery feeQuery = new AccountFeeQuery();
		feeQuery.fetch(AccountFee.Relationships.LOGS, RelationshipHelper
				.nested(AccountFee.Relationships.ACCOUNT_TYPE,
						AccountType.Relationships.CURRENCY));
		feeQuery.setReturnDisabled(false);
		feeQuery.setGroups(managedGroups);
		final List<AccountFee> fees = accountFeeService.search(feeQuery);

		feeQuery.setReturnDisabled(true);

		final AccountFeeLogForm form = context.getForm();

		final AccountFeeLogQuery logQuery = getDataBinder().readFromString(
				form.getQuery());
		logQuery.setAccountFees(fees);

		// Check if there is at least one fee which is currently running
		boolean hasRunningFees = false;
		for (final AccountFee fee : fees) {
			final AccountFeeLog lastExecution = fee.getLastExecution();
			if (lastExecution != null && !lastExecution.isFinished()) {
				hasRunningFees = true;
				break;
			}
		}
		request.setAttribute("hasRunningFees", hasRunningFees);

		request.setAttribute("accountFees", fees);

		return logQuery;
	}

	protected boolean willExecuteQuery(final ActionContext context,
			final QueryParameters queryParameters) throws Exception {
		return true;
	}

	private DataBinder<AccountFeeLogQuery> getDataBinder() {
		if (dataBinder == null) {
			final BeanBinder<AccountFeeLogQuery> binder = BeanBinder
					.instance(AccountFeeLogQuery.class);
			binder.registerBinder("pageParameters",
					DataBinderHelper.pageBinder());
			dataBinder = binder;
		}
		return dataBinder;
	}

}
