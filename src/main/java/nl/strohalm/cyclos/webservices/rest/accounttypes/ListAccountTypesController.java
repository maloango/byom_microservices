package nl.strohalm.cyclos.webservices.rest.accounttypes;

import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import nl.strohalm.cyclos.annotations.Inject;
import nl.strohalm.cyclos.controls.ActionContext;
import nl.strohalm.cyclos.entities.accounts.AccountType;
import nl.strohalm.cyclos.services.accounts.AccountTypeService;
import nl.strohalm.cyclos.services.accounts.MemberAccountTypeQuery;
import nl.strohalm.cyclos.services.accounts.SystemAccountTypeQuery;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;
import org.apache.struts.action.ActionForward;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class ListAccountTypesController extends BaseRestController {
	private AccountTypeService accountTypeService;

	public AccountTypeService getAccountTypeService() {
		return accountTypeService;
	}

	@Inject
	public void setAccountTypeService(
			final AccountTypeService accountTypeService) {
		this.accountTypeService = accountTypeService;
	}

	@RequestMapping(value = "/admin/listAccountTypes", method = RequestMethod.POST)
	@ResponseBody
	protected ActionForward executeAction(final ActionContext context)
			throws Exception {
		final HttpServletRequest request = context.getRequest();
		final List<AccountType> accountTypes = new ArrayList<AccountType>();
		// Get the system accounts
		final SystemAccountTypeQuery systemQuery = new SystemAccountTypeQuery();
		systemQuery.fetch(AccountType.Relationships.CURRENCY);
		accountTypes.addAll(accountTypeService.search(systemQuery));
		// Get the member accounts
		final MemberAccountTypeQuery memberQuery = new MemberAccountTypeQuery();
		memberQuery.fetch(AccountType.Relationships.CURRENCY);
		accountTypes.addAll(accountTypeService.search(memberQuery));

		request.setAttribute("accountTypes", accountTypes);
		return context.getInputForward();
	}
}
