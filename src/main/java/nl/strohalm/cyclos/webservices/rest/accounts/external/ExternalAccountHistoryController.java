package nl.strohalm.cyclos.webservices.rest.accounts.external;

import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

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

import org.springframework.stereotype.Controller;

@Controller
public class ExternalAccountHistoryController extends BaseRestController
		implements LocalSettingsChangeListener {
	private PermissionService permissionService;
	private SettingsService settingsService;
	public PermissionService getPermissionService() {
		return permissionService;
	}

	public void setPermissionService(PermissionService permissionService) {
		this.permissionService = permissionService;
	}

	public static DataBinder<ExternalTransferQuery> externalTransferQueryDataBinder(
			final LocalSettings localSettings) {
		final BeanBinder<ExternalTransferQuery> binder = BeanBinder
				.instance(ExternalTransferQuery.class);
		binder.registerBinder("account", PropertyBinder.instance(
				ExternalAccount.class, "account",
				ReferenceConverter.instance(ExternalAccount.class)));
		binder.registerBinder("type", PropertyBinder.instance(
				ExternalTransferType.class, "type",
				ReferenceConverter.instance(ExternalTransferType.class)));
		binder.registerBinder("transferImport", PropertyBinder.instance(
				ExternalTransferImport.class, "transferImport",
				ReferenceConverter.instance(ExternalTransferImport.class)));
		binder.registerBinder("status", PropertyBinder.instance(
				ExternalTransfer.SummaryStatus.class, "status"));
		binder.registerBinder("member", PropertyBinder.instance(Member.class,
				"member", ReferenceConverter.instance(Member.class)));
		binder.registerBinder("initialAmount",
				PropertyBinder.instance(BigDecimal.class, "initialAmount"));
		binder.registerBinder("finalAmount",
				PropertyBinder.instance(BigDecimal.class, "finalAmount"));
		binder.registerBinder("period",
				DataBinderHelper.periodBinder(localSettings, "period"));
		binder.registerBinder("pageParameters", DataBinderHelper.pageBinder());
		return binder;
	}

	private DataBinder<ExternalTransferQuery> dataBinder;
	private ExternalAccountService externalAccountService;
	private ExternalTransferImportService externalTransferImportService;

	private ExternalTransferService externalTransferService;

	public DataBinder<ExternalTransferQuery> getDataBinder() {
		if (dataBinder == null) {
			final LocalSettings localSettings = settingsService
					.getLocalSettings();
			dataBinder = externalTransferQueryDataBinder(localSettings);
		}
		return dataBinder;
	}

	

	@Inject
	public void setExternalAccountService(
			final ExternalAccountService externalAccountService) {
		this.externalAccountService = externalAccountService;
	}

	@Inject
	public void setExternalTransferImportService(
			final ExternalTransferImportService externalTransferImportService) {
		this.externalTransferImportService = externalTransferImportService;
	}

	@Inject
	public void setExternalTransferService(
			final ExternalTransferService externalTransferService) {
		this.externalTransferService = externalTransferService;
	}


	protected void executeQuery(final ActionContext context,
			final QueryParameters queryParameters) {
		final HttpServletRequest request = context.getRequest();
		final ExternalTransferQuery query = (ExternalTransferQuery) queryParameters;
		final List<ExternalTransfer> externalTransfers = externalTransferService
				.search(query);
		request.setAttribute("externalTransfers", externalTransfers);
		request.setAttribute("summary", processSummary(externalTransfers));
	}


	protected boolean willExecuteQuery(final ActionContext context,
			final QueryParameters queryParameters) throws Exception {
		// The query is always executed
		return true;
	}

	private Map<ExternalTransfer.SummaryStatus, TransactionSummaryVO> processSummary(
			final List<ExternalTransfer> externalTransfers) {
		// Initialize summary VOs with 0 values
		final Map<ExternalTransfer.SummaryStatus, TransactionSummaryVO> summary = new EnumMap<ExternalTransfer.SummaryStatus, TransactionSummaryVO>(
				ExternalTransfer.SummaryStatus.class);
		for (final SummaryStatus summaryStatus : ExternalTransfer.SummaryStatus
				.values()) {
			summary.put(summaryStatus, new NegativeAllowedTransactionSummaryVO(
					0, new BigDecimal(0)));
		}
		final TransactionSummaryVO totalVo = summary
				.get(ExternalTransfer.SummaryStatus.TOTAL);
		for (final ExternalTransfer transfer : externalTransfers) {
			final ExternalTransfer.Status status = transfer.getStatus();
			// Get the summary VO corresponding to the external transfer status
			TransactionSummaryVO vo = null;
			switch (status) {
			case PENDING:
				if (transfer.isComplete()) {
					vo = summary
							.get(ExternalTransfer.SummaryStatus.COMPLETE_PENDING);
				} else {
					vo = summary
							.get(ExternalTransfer.SummaryStatus.INCOMPLETE_PENDING);
				}
				break;
			case CHECKED:
				vo = summary.get(ExternalTransfer.SummaryStatus.CHECKED);
				break;
			case PROCESSED:
				vo = summary.get(ExternalTransfer.SummaryStatus.PROCESSED);
				break;
			}
			// Update summary on both current and total
			final BigDecimal amount = BigDecimalHelper
					.nvl(transfer.getAmount());
			vo.setCount(vo.getCount() + 1);
			vo.setAmount(vo.getAmount().add(amount));
			totalVo.setCount(totalVo.getCount() + 1);
			totalVo.setAmount(totalVo.getAmount().add(amount));
		}
		return summary;
	}

	@Override
	public void onLocalSettingsUpdate(LocalSettingsEvent event) {
		// TODO Auto-generated method stub
		
	}
}
