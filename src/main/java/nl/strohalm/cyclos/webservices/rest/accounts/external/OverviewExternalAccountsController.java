package nl.strohalm.cyclos.webservices.rest.accounts.external;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import nl.strohalm.cyclos.annotations.Inject;
import nl.strohalm.cyclos.entities.accounts.external.ExternalAccountDetailsVO;
import nl.strohalm.cyclos.services.accounts.external.ExternalAccountService;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;
import nl.strohalm.cyclos.webservices.rest.GenericResponse;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class OverviewExternalAccountsController extends BaseRestController {

    public static class OverViewExternalAccountResponse extends GenericResponse {

        private List<ExternalAccountDetailsVO> externalAccounts;

        public List<ExternalAccountDetailsVO> getExternalAccounts() {
            return externalAccounts;
        }

        public void setExternalAccounts(List<ExternalAccountDetailsVO> externalAccounts) {
            this.externalAccounts = externalAccounts;
        }

    }

    @RequestMapping(value = "admin/overviewExternalAccounts", method = RequestMethod.GET)
    @ResponseBody
    protected OverViewExternalAccountResponse executeAction() throws Exception {
        OverViewExternalAccountResponse response = new OverViewExternalAccountResponse();
        final List<ExternalAccountDetailsVO> externalAccounts = externalAccountService.externalAccountOverview();
        response.setExternalAccounts(externalAccounts);
        response.setStatus(0);
        return response;
    }
}
