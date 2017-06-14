package nl.strohalm.cyclos.webservices.rest.accounts.external;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import nl.strohalm.cyclos.annotations.Inject;
import nl.strohalm.cyclos.entities.accounts.external.ExternalAccountDetailsVO;
import nl.strohalm.cyclos.services.accounts.external.ExternalAccountService;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;

@Controller
public class OverviewExternalAccountsController extends BaseRestController {
	private ExternalAccountService externalAccountService;

	@Inject
	public void setExternalAccountService(final ExternalAccountService externalAccountService) {
		this.externalAccountService = externalAccountService;
	}

	public static class OverviewExternalAccountsRequestDto {

	}

	public static class OverviewExternalAccountsResponseDto {
		List<ExternalAccountDetailsVO> externalAccounts;

		public OverviewExternalAccountsResponseDto(List<ExternalAccountDetailsVO> externalAccounts) {
			super();
			this.externalAccounts = externalAccounts;
		}

	}

	@RequestMapping(value = "admin/overviewExternalAccounts", method = RequestMethod.GET)
	@ResponseBody
	protected OverviewExternalAccountsResponseDto executeAction(@RequestBody OverviewExternalAccountsRequestDto form)
			throws Exception {
		final List<ExternalAccountDetailsVO> externalAccounts = externalAccountService.externalAccountOverview();
		OverviewExternalAccountsResponseDto response = new OverviewExternalAccountsResponseDto(externalAccounts);

		return response;
	}
}
