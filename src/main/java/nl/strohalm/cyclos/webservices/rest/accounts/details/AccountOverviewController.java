/*package nl.strohalm.cyclos.webservices.rest.accounts.details;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import nl.strohalm.cyclos.annotations.Inject;
import nl.strohalm.cyclos.entities.accounts.AccountOwner;
import nl.strohalm.cyclos.entities.members.Element;
import nl.strohalm.cyclos.entities.members.Member;
import nl.strohalm.cyclos.services.accounts.AccountService;
import nl.strohalm.cyclos.services.accounts.AccountTypeService;
import nl.strohalm.cyclos.services.elements.ElementService;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;

@Controller
public class AccountOverviewController extends BaseRestController {

	private AccountService accountService;
	private AccountTypeService accountTypeService;
	private ElementService elementService;

	public AccountService getAccountService() {
		return accountService;
	}

	public AccountTypeService getAccountTypeService() {
		return accountTypeService;
	}

	@Inject
	public void setAccountService(final AccountService accountService) {
		this.accountService = accountService;
	}

	@Inject
	public void setAccountTypeService(final AccountTypeService accountTypeService) {
		this.accountTypeService = accountTypeService;
	}

	public static class AccountOverviewRequestDTO {
		private long memberId;
		Element element;

		public final Element getElement() {
			return element;
		}

		public final void setElement(Element element) {
			this.element = element;
		}

		public long getMemberId() {
			return memberId;
		}

		public void setMemberId(final long memberId) {
			this.memberId = memberId;
		}

	}

	public static class AccountOverviewResponseDTO {
		AccountOwner owner;
		Member member;
		boolean myAccounts;
		boolean byBrokerfalse;

		public final AccountOwner getOwner() {
			return owner;
		}

		public final void setOwner(AccountOwner owner) {
			this.owner = owner;
		}

		public final Member getMember() {
			return member;
		}

		public final void setMember(Member member) {
			this.member = member;
		}

		public final boolean isMyAccounts() {
			return myAccounts;
		}

		public final void setMyAccounts(boolean myAccounts) {
			this.myAccounts = myAccounts;
		}

		public final boolean isByBrokerfalse() {
			return byBrokerfalse;
		}

		public final void setByBrokerfalse(boolean byBrokerfalse) {
			this.byBrokerfalse = byBrokerfalse;
		}

	}

	@RequestMapping(value = "/admin/editCurrency", method = RequestMethod.POST)
	@ResponseBody
	protected AccountOverviewResponseDTO executeAction(@RequestBody AccountOverviewRequestDTO form) throws Exception {
		// final AccountOverviewForm form = context.getForm();
		// final HttpServletRequest request = context.getRequest();
		final long memberId = form.getMemberId();
		AccountOwner owner;
		Member member = null;
		boolean myAccounts = false;
		boolean byBroker = false;

		final Element loggedElement = form.getElement();
		// Resolve the account owner we will use
		if (memberId <= 0L || memberId == loggedElement.getId()) {
			owner = form.getAccountOwner();
			myAccounts = true;
			if (form.isMember()) {
				member = form.getElement();
			}
		} else {
			final Element element = elementService.load(memberId, Element.Relationships.USER);
			if (!(element instanceof Member)) {
				throw new ValidationException();
			}
			member = (Member) element;
			owner = member;
			if (context.isMember()) {
				if (!context.isBrokerOf(member)) {
					throw new ValidationException();
				}
				byBroker = true;
			}
		}

		// Get the account types of the owner
		final List<? extends Account> accounts = accountService.getAccounts(owner,
				RelationshipHelper.nested(Account.Relationships.TYPE, AccountType.Relationships.CURRENCY));

		if (accounts.isEmpty()) {
			// No accounts = error
			return context.sendError("accountOverview.error.noAccounts");
		} else if (accounts.size() == 1) {
			// Single account = redirect to the account details

			// Remove the overview from the navigation
			context.getNavigation().removeCurrent();

			// Redirect
			final Account account = accounts.get(0);
			final AccountType accountType = account.getType();
			final Map<String, Object> params = new HashMap<String, Object>();
			params.put("memberId", form.getMemberId());
			params.put("typeId", accountType.getId());
			params.put("singleAccount", true);
			params.put("fromQuickAccess", request.getParameter("fromQuickAccess"));
			return ActionHelper.redirectWithParams(context.getRequest(), context.findForward("accountDetails"), params);
		}

		request.setAttribute("member", member);
		request.setAttribute("myAccounts", myAccounts);
		request.setAttribute("byBroker", byBroker);
		request.setAttribute("overview", resolveAccountOverview(accounts));

		return context.getInputForward();
	}

	private Map<Account, BigDecimal> resolveAccountOverview(final List<? extends Account> accounts) {
		final Map<Account, BigDecimal> overview = new LinkedHashMap<Account, BigDecimal>();
		for (final Account account : accounts) {
			try {
				final BigDecimal balance = accountService.getBalance(new AccountDateDTO(account));
				overview.put(account, balance);
			} catch (final EntityNotFoundException e) {
				// Ignore this account
			}
		}
		return overview;
	}

}
}
*/