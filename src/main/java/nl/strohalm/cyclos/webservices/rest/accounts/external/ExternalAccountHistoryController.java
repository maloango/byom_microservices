package nl.strohalm.cyclos.webservices.rest.accounts.external;

import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;

import nl.strohalm.cyclos.annotations.Inject;
import nl.strohalm.cyclos.controls.ActionContext;
import nl.strohalm.cyclos.entities.accounts.external.ExternalAccount;
import nl.strohalm.cyclos.entities.accounts.external.ExternalTransfer;
import nl.strohalm.cyclos.entities.accounts.external.ExternalTransfer.SummaryStatus;
import nl.strohalm.cyclos.entities.accounts.external.ExternalTransferImport;
import nl.strohalm.cyclos.entities.accounts.external.ExternalTransferQuery;
import nl.strohalm.cyclos.entities.accounts.external.ExternalTransferType;
import nl.strohalm.cyclos.entities.members.Member;
import nl.strohalm.cyclos.entities.settings.LocalSettings;
import nl.strohalm.cyclos.entities.settings.events.LocalSettingsChangeListener;
import nl.strohalm.cyclos.entities.settings.events.LocalSettingsEvent;
import nl.strohalm.cyclos.services.accounts.external.ExternalAccountService;
import nl.strohalm.cyclos.services.accounts.external.ExternalTransferImportService;
import nl.strohalm.cyclos.services.accounts.external.ExternalTransferService;
import nl.strohalm.cyclos.services.permissions.PermissionService;
import nl.strohalm.cyclos.services.settings.SettingsService;
import nl.strohalm.cyclos.services.transactions.NegativeAllowedTransactionSummaryVO;
import nl.strohalm.cyclos.services.transactions.TransactionSummaryVO;
import nl.strohalm.cyclos.utils.BigDecimalHelper;
import nl.strohalm.cyclos.utils.binding.BeanBinder;
import nl.strohalm.cyclos.utils.binding.DataBinder;
import nl.strohalm.cyclos.utils.binding.DataBinderHelper;
import nl.strohalm.cyclos.utils.binding.PropertyBinder;
import nl.strohalm.cyclos.utils.conversion.ReferenceConverter;
import nl.strohalm.cyclos.utils.query.QueryParameters;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;
import nl.strohalm.cyclos.webservices.rest.GenericResponse;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class ExternalAccountHistoryController extends BaseRestController {

    public static class ExternalAccountHistoryResponse extends GenericResponse {

        private List<ExternalTransfer> externalTransfers;

        public List<ExternalTransfer> getExternalTransfers() {
            return externalTransfers;
        }

        public void setExternalTransfers(List<ExternalTransfer> externalTransfers) {
            this.externalTransfers = externalTransfers;
        }

    }

    @RequestMapping(value = "admin/externalAccountHistory", method = RequestMethod.GET)
    @ResponseBody
    public ExternalAccountHistoryResponse executeQuery() {
//        final HttpServletRequest request = context.getRequest();
        ExternalAccountHistoryResponse response =new ExternalAccountHistoryResponse();
        final ExternalTransferQuery query = new ExternalTransferQuery();
        query.setStatus(SummaryStatus.TOTAL);
        query.setResultType(QueryParameters.ResultType.LIST);
        query.fetch(ExternalTransfer.Relationships.TRANSFER_IMPORT, ExternalTransfer.Relationships.TYPE);
        final List<ExternalTransfer> externalTransfers = externalTransferService.search(query);
        response.setExternalTransfers(externalTransfers);
        response.setStatus(0);
        response.setMessage("External account list");
        return response;
    }

}
