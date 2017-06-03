package nl.strohalm.cyclos.webservices.rest.external;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import nl.strohalm.cyclos.annotations.Inject;
import nl.strohalm.cyclos.entities.accounts.external.ExternalAccountDetailsVO;
import nl.strohalm.cyclos.services.accounts.external.ExternalAccountService;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class OverviewExternalAccountsController extends BaseRestController {
	private ExternalAccountService externalAccountService;

	@Inject
	public void setExternalAccountService(
			final ExternalAccountService externalAccountService) {
		this.externalAccountService = externalAccountService;
	}

	public static class OverviewExternalAccountsRequestDto {

	}

	public static class OverviewExternalAccountsResponseDto {

	}

	@RequestMapping(value = "", method = RequestMethod.GET)
	@ResponseBody
	protected List<ExternalAccountDetailsVO> executeAction(@RequestBody OverviewExternalAccountsRequestDto form)
			throws Exception {
		final List<ExternalAccountDetailsVO> externalAccounts = externalAccountService
				.externalAccountOverview();

		//request.setAttribute("externalAccounts", externalAccounts);

		return externalAccounts;
	}
}
