package nl.strohalm.cyclos.webservices.rest.reports.members.transactions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.IteratorUtils;
import org.apache.commons.collections.Predicate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import nl.strohalm.cyclos.annotations.Inject;
import nl.strohalm.cyclos.controls.AbstractActionContext;
import nl.strohalm.cyclos.controls.ActionContext;
import nl.strohalm.cyclos.controls.reports.members.transactions.MembersReportHandler;
import nl.strohalm.cyclos.controls.reports.members.transactions.MembersTransactionsReportDTO;
import nl.strohalm.cyclos.controls.reports.members.transactions.MembersTransactionsReportForm;
import nl.strohalm.cyclos.controls.reports.members.transactions.MembersTransactionsReportDTO.DetailsLevel;
import nl.strohalm.cyclos.controls.restapi.BaseRestController;
import nl.strohalm.cyclos.entities.accounts.AccountType;
import nl.strohalm.cyclos.entities.accounts.transactions.PaymentFilter;
import nl.strohalm.cyclos.entities.groups.MemberGroup;
import nl.strohalm.cyclos.entities.members.MemberTransactionDetailsReportData;
import nl.strohalm.cyclos.entities.members.MemberTransactionSummaryReportData;
import nl.strohalm.cyclos.entities.members.MembersTransactionsReportParameters;
import nl.strohalm.cyclos.entities.settings.LocalSettings;
import nl.strohalm.cyclos.services.accounts.AccountService;
import nl.strohalm.cyclos.services.accounts.AccountTypeService;
import nl.strohalm.cyclos.services.settings.SettingsService;
import nl.strohalm.cyclos.services.transfertypes.PaymentFilterService;
import nl.strohalm.cyclos.utils.EntityHelper;
import nl.strohalm.cyclos.utils.IteratorListImpl;
import nl.strohalm.cyclos.utils.Pair;
import nl.strohalm.cyclos.utils.Period;
import nl.strohalm.cyclos.utils.ResponseHelper;
import nl.strohalm.cyclos.utils.SpringHelper;
import nl.strohalm.cyclos.utils.binding.BeanBinder;
import nl.strohalm.cyclos.utils.binding.DataBinderHelper;
import nl.strohalm.cyclos.utils.binding.PropertyBinder;
import nl.strohalm.cyclos.utils.binding.SimpleCollectionBinder;
import nl.strohalm.cyclos.utils.conversion.ReferenceConverter;
import nl.strohalm.cyclos.utils.csv.CSVWriter;
import nl.strohalm.cyclos.utils.query.PageParameters;
import nl.strohalm.cyclos.utils.validation.ValidationError;
import nl.strohalm.cyclos.utils.validation.ValidationException;
@Controller
public class ExportMembersTransactionsDetailsToCsvController extends BaseRestController{

	private MembersReportHandler reportHandler;
    private PaymentFilterService paymentFilterService;
    private ResponseHelper responseHelper;
    public final ResponseHelper getResponseHelper() {
		return responseHelper;
	}

	public final void setResponseHelper(ResponseHelper responseHelper) {
		this.responseHelper = responseHelper;
	}

	public final SettingsService getSettingsService() {
		return settingsService;
	}

	public final void setSettingsService(SettingsService settingsService) {
		this.settingsService = settingsService;
	}

	public final PaymentFilterService getPaymentFilterService() {
		return paymentFilterService;
	}

	public final void setReportHandler(MembersReportHandler reportHandler) {
		this.reportHandler = reportHandler;
	}

	private SettingsService settingsService;

    public MembersReportHandler getReportHandler() {
        if (reportHandler == null) {
            reportHandler = new MembersReportHandler(settingsService.getLocalSettings());
            SpringHelper.injectBeans(getServlet().getServletContext(), reportHandler);
        }
        return reportHandler;
    }
   

    private AbstractActionContext getServlet() {
		// TODO Auto-generated method stub
		return null;
	}

	@Inject
    public void setPaymentFilterService(final PaymentFilterService paymentFilterService) {
        this.paymentFilterService = paymentFilterService;
    }
    public static class ExportMembersTransactionsDetailsToCsvRequestDTO{
    	 private AccountService                           accountService;
    	    private AccountTypeService                       accountTypeService;
    	    private PaymentFilterService                     paymentFilterService;

    	    private BeanBinder<MembersTransactionsReportDTO> binder;

    	    private LocalSettings                            settings = null;
    	    private boolean                                     memberName;
    	    private boolean                                     brokerUsername;
    	    private boolean                                     brokerName;
    	    private Collection<AccountType>                     accountTypes;
    	    private Collection<MemberGroup>                     memberGroups;
    	    private Period                                      period;

    	    private boolean                                     incomingTransactions;
    	    private boolean                                     outgoingTransactions;
    	    private boolean                                     includeNoTraders;
    	    private DetailsLevel                                detailsLevel;
    	    private Collection<PaymentFilter>                   transactionsPaymentFilters;
    	    private Map<PaymentFilter, Integer>                 transactionsColSpan;
    	    private Map<AccountType, Collection<PaymentFilter>> paymentFiltersByAccountType;

    	    public Collection<AccountType> getAccountTypes() {
    	        return accountTypes;
    	    }

    	    public int getBrokerColSpan() {
    	        int colspan = 0;
    	        if (isBrokerName()) {
    	            colspan++;
    	        }
    	        if (isBrokerUsername()) {
    	            colspan++;
    	        }
    	        return colspan;
    	    }

    	    public DetailsLevel getDetailsLevel() {
    	        return detailsLevel;
    	    }

    	    public int getMemberColSpan() {
    	        if (isMemberName()) {
    	            return 2;
    	        } else {
    	            return 1;
    	        }
    	    }

    	    public Collection<MemberGroup> getMemberGroups() {
    	        return memberGroups;
    	    }

    	    public Map<AccountType, Collection<PaymentFilter>> getPaymentFiltersByAccountType() {
    	        return paymentFiltersByAccountType;
    	    }

    	    public Period getPeriod() {
    	        return period;
    	    }

    	    public Map<PaymentFilter, Integer> getTransactionsColSpan() {
    	        return transactionsColSpan;
    	    }

    	    public Collection<PaymentFilter> getTransactionsPaymentFilters() {
    	        return transactionsPaymentFilters;
    	    }

    	    public boolean isBrokerName() {
    	        return brokerName;
    	    }

    	    public boolean isBrokerUsername() {
    	        return brokerUsername;
    	    }

    	    public boolean isDebitsAndCredits() {
    	        return isIncomingTransactions() && isOutgoingTransactions();
    	    }

    	    public boolean isIncludeNoTraders() {
    	        return includeNoTraders;
    	    }

    	    public boolean isIncomingTransactions() {
    	        return incomingTransactions;
    	    }

    	    public boolean isMemberName() {
    	        return memberName;
    	    }

    	    public boolean isOutgoingTransactions() {
    	        return outgoingTransactions;
    	    }

    	    public boolean isTransactions() {
    	        return isIncomingTransactions() || isOutgoingTransactions();
    	    }

    	    public void setAccountTypes(final Collection<AccountType> accountTypes) {
    	        this.accountTypes = accountTypes;
    	    }

    	    public void setBrokerName(final boolean brokerName) {
    	        this.brokerName = brokerName;
    	    }

    	    public void setBrokerUsername(final boolean brokerUsername) {
    	        this.brokerUsername = brokerUsername;
    	    }

    	    public void setDetailsLevel(final DetailsLevel detailsLevel) {
    	        this.detailsLevel = detailsLevel;
    	    }

    	    public void setIncludeNoTraders(final boolean includeNoTraders) {
    	        this.includeNoTraders = includeNoTraders;
    	    }

    	    public void setIncomingTransactions(final boolean incomingTransactions) {
    	        this.incomingTransactions = incomingTransactions;
    	    }

    	    public void setMemberGroups(final Collection<MemberGroup> memberGroups) {
    	        this.memberGroups = memberGroups;
    	    }

    	    public void setMemberName(final boolean memberName) {
    	        this.memberName = memberName;
    	    }

    	    public void setOutgoingTransactions(final boolean outgoingTransactions) {
    	        this.outgoingTransactions = outgoingTransactions;
    	    }

    	    public void setPaymentFiltersByAccountType(final Map<AccountType, Collection<PaymentFilter>> paymentFiltersByAccountType) {
    	        this.paymentFiltersByAccountType = paymentFiltersByAccountType;
    	    }

    	    public void setPeriod(final Period period) {
    	        this.period = period;
    	    }

    	    public void setTransactionsColSpan(final Map<PaymentFilter, Integer> transactionsColSpan) {
    	        this.transactionsColSpan = transactionsColSpan;
    	    }

    	    public void setTransactionsPaymentFilters(final Collection<PaymentFilter> transactionsPaymentFilters) {
    	        this.transactionsPaymentFilters = transactionsPaymentFilters;
    	    }

    	    public void MembersReportHandler(final LocalSettings settings) {
    	        this.settings = settings;
    	    }

    	    public BeanBinder<MembersTransactionsReportDTO> getDataBinder() {
    	        if (binder == null) {
    	            BeanBinder<MembersTransactionsReportDTO> temp;
    	            final ReferenceConverter<AccountType> accountTypeConverter = ReferenceConverter.instance(AccountType.class);
    	            final ReferenceConverter<PaymentFilter> paymentFilterConverter = ReferenceConverter.instance(PaymentFilter.class);
    	            final ReferenceConverter<MemberGroup> memberGroupConverter = ReferenceConverter.instance(MemberGroup.class);

    	            temp = BeanBinder.instance(MembersTransactionsReportDTO.class);
    	            temp.registerBinder("memberName", PropertyBinder.instance(Boolean.TYPE, "memberName"));
    	            temp.registerBinder("brokerUsername", PropertyBinder.instance(Boolean.TYPE, "brokerUsername"));
    	            temp.registerBinder("brokerName", PropertyBinder.instance(Boolean.TYPE, "brokerName"));
    	            temp.registerBinder("accountTypes", SimpleCollectionBinder.instance(AccountType.class, "accountTypes", accountTypeConverter));
    	            temp.registerBinder("memberGroups", SimpleCollectionBinder.instance(MemberGroup.class, "memberGroups", memberGroupConverter));
    	            temp.registerBinder("period", DataBinderHelper.periodBinder(settings, "period"));

    	            // Transactions related binding
    	            temp.registerBinder("transactionsPaymentFilters", SimpleCollectionBinder.instance(PaymentFilter.class, "transactionsPaymentFilters", paymentFilterConverter));
    	            temp.registerBinder("incomingTransactions", PropertyBinder.instance(Boolean.TYPE, "incomingTransactions"));
    	            temp.registerBinder("outgoingTransactions", PropertyBinder.instance(Boolean.TYPE, "outgoingTransactions"));
    	            temp.registerBinder("includeNoTraders", PropertyBinder.instance(Boolean.TYPE, "includeNoTraders"));
    	            temp.registerBinder("detailsLevel", PropertyBinder.instance(MembersTransactionsReportDTO.DetailsLevel.class, "detailsLevel"));
    	            binder = temp;
    	        }
    	        return binder;
    	    }

    	    public Pair<MembersTransactionsReportDTO, Iterator<MemberTransactionDetailsReportData>> handleTransactionsDetails(final ActionContext context) {
    	        final MembersTransactionsReportDTO dto = readDTO(context);
    	        final MembersTransactionsReportParameters params = toTransactionReportParameters(dto);
    	        final Iterator<MemberTransactionDetailsReportData> iterator = accountService.membersTransactionsDetailsReport(params);
    	        return new Pair<MembersTransactionsReportDTO, Iterator<MemberTransactionDetailsReportData>>(dto, iterator);

    	    }

    	    public Pair<MembersTransactionsReportDTO, Iterator<MemberTransactionSummaryReportData>> handleTransactionsSummary(final ActionContext context) {
    	        final MembersTransactionsReportDTO dto = readDTO(context);
    	        final MembersTransactionsReportParameters params = toTransactionReportParameters(dto);
    	        params.setPageParameters(new PageParameters(-1, 0));
    	        final Iterator<MemberTransactionSummaryReportData> iterator = accountService.membersTransactionsSummaryReport(params);
    	        return new Pair<MembersTransactionsReportDTO, Iterator<MemberTransactionSummaryReportData>>(dto, iterator);
    	    }

    	    @Inject
    	    public void setAccountService(final AccountService accountService) {
    	        this.accountService = accountService;
    	    }

    	    @Inject
    	    public void setAccountTypeService(final AccountTypeService accountTypeService) {
    	        this.accountTypeService = accountTypeService;
    	    }

    	    @Inject
    	    public void setPaymentFilterService(final PaymentFilterService paymentFilterService) {
    	        this.paymentFilterService = paymentFilterService;
    	    }

    	    public void validateDTO(final MembersTransactionsReportDTO dto) throws ValidationException {
    	        adjustDto(dto);
    	        final ValidationException validationException = new ValidationException();

    	        if (CollectionUtils.isEmpty(dto.getMemberGroups())) {
    	            validationException.addGeneralError(new ValidationError("reports.members_reports.transactions.memberGroupsRequired"));
    	        }

    	        if (CollectionUtils.isEmpty(dto.getAccountTypes())) {
    	            validationException.addGeneralError(new ValidationError("reports.members_reports.transactions.accountTypesRequired"));
    	        }
    	        if (dto.getDetailsLevel() == DetailsLevel.SUMMARY) {
    	            if (CollectionUtils.isEmpty(dto.getTransactionsPaymentFilters())) {
    	                validationException.addGeneralError(new ValidationError("reports.members_reports.transactions.paymentFilterRequired"));
    	            } else if (!dto.isIncomingTransactions() && !dto.isOutgoingTransactions()) {
    	                validationException.addGeneralError(new ValidationError("reports.members_reports.transactions.transactionModeRequired"));
    	            }
    	        }
    	        validationException.throwIfHasErrors();
    	    }

    	    /*
    	     * Clean data that is not used by current 'what to show' selection
    	     */
    	    private void adjustDto(final MembersTransactionsReportDTO dto) {

    	        // Initialize account types
    	        final Collection<AccountType> accountTypes = accountTypeService.load(EntityHelper.toIdsAsList(dto.getAccountTypes()));
    	        dto.setAccountTypes(accountTypes);

    	        // Separate payment filters by account type
    	        final Collection<PaymentFilter> transactionsPaymentFilters = paymentFilterService.load(EntityHelper.toIdsAsList(dto.getTransactionsPaymentFilters()), PaymentFilter.Relationships.ACCOUNT_TYPE);
    	        final Map<AccountType, Collection<PaymentFilter>> paymentFiltersByAccountType = new HashMap<AccountType, Collection<PaymentFilter>>();
    	        for (final AccountType accountType : accountTypes) {
    	            final Collection<PaymentFilter> accountTypePaymentFilters = new ArrayList<PaymentFilter>();
    	            for (final PaymentFilter paymentFilter : transactionsPaymentFilters) {
    	                if (paymentFilter.getAccountType().equals(accountType)) {
    	                    accountTypePaymentFilters.add(paymentFilter);
    	                }
    	            }
    	            paymentFiltersByAccountType.put(accountType, accountTypePaymentFilters);
    	        }
    	        dto.setPaymentFiltersByAccountType(paymentFiltersByAccountType);

    	        // Calculate the colspan for each payment filter
    	        final Map<PaymentFilter, Integer> transactionsColSpan = new HashMap<PaymentFilter, Integer>();
    	        final boolean incomingTransactions = dto.isIncomingTransactions();
    	        final boolean outgoingTransactions = dto.isOutgoingTransactions();
    	        if (incomingTransactions || outgoingTransactions) {
    	            for (final Iterator<PaymentFilter> it = transactionsPaymentFilters.iterator(); it.hasNext();) {
    	                final PaymentFilter paymentFilter = it.next();
    	                int colSpan = 0;
    	                if (incomingTransactions) {
    	                    colSpan += 2;
    	                }
    	                if (outgoingTransactions) {
    	                    colSpan += 2;
    	                }
    	                if (colSpan == 0) {
    	                    it.remove();
    	                } else {
    	                    transactionsColSpan.put(paymentFilter, colSpan);
    	                }
    	            }
    	        }
    	        dto.setTransactionsPaymentFilters(transactionsPaymentFilters);
    	        dto.setTransactionsColSpan(transactionsColSpan);
    	    }

    	    private MembersTransactionsReportDTO readDTO(final ActionContext context) {
    	        final MembersTransactionsReportForm form = context.getForm();
    	        final MembersTransactionsReportDTO dto = getDataBinder().readFromString(form.getMembersTransactionsReport());
    	        dto.setTransactionsPaymentFilters(paymentFilterService.load(EntityHelper.toIdsAsList(dto.getTransactionsPaymentFilters()), PaymentFilter.Relationships.TRANSFER_TYPES));
    	        adjustDto(dto);
    	        return dto;
    	    }

    	    private MembersTransactionsReportParameters toTransactionReportParameters(final MembersTransactionsReportDTO dto) {
    	        final MembersTransactionsReportParameters params = new MembersTransactionsReportParameters();
    	        params.setFetchBroker(dto.isBrokerName() || dto.isBrokerUsername());
    	        params.setMemberGroups(dto.getMemberGroups());
    	        params.setPeriod(dto.getPeriod());
    	        params.setCredits(dto.isIncomingTransactions());
    	        params.setDebits(dto.isOutgoingTransactions());
    	        final Map<AccountType, Collection<PaymentFilter>> paymentFiltersByAccountType = dto.getPaymentFiltersByAccountType();
    	        if (paymentFiltersByAccountType != null) {
    	            final Set<PaymentFilter> allPaymentFilters = new HashSet<PaymentFilter>();
    	            for (final Collection<PaymentFilter> paymentFilters : paymentFiltersByAccountType.values()) {
    	                allPaymentFilters.addAll(paymentFilters);
    	            }
    	            params.setPaymentFilters(allPaymentFilters);
    	        }
    	        return params;
    	    }

    }
    public static class ExportMembersTransactionsDetailsToCsvResposneDTO{
    	Iterator<MemberTransactionDetailsReportData> reportIterator;

		public ExportMembersTransactionsDetailsToCsvResposneDTO(
				Iterator<MemberTransactionDetailsReportData> reportIterator) {
			super();
			this.reportIterator = reportIterator;
		}

		public final Iterator<MemberTransactionDetailsReportData> getReportIterator() {
			return reportIterator;
		}

		public final void setReportIterator(Iterator<MemberTransactionDetailsReportData> reportIterator) {
			this.reportIterator = reportIterator;
		}
    }

    @RequestMapping(value ="",method = RequestMethod.GET)
    @ResponseBody
    @SuppressWarnings({ "unchecked", "rawtypes" })
    protected ExportMembersTransactionsDetailsToCsvResposneDTO executeQuery( @RequestBody ExportMembersTransactionsDetailsToCsvRequestDTO form) {
        final MembersReportHandler reportHandler = getReportHandler();
        final Pair<MembersTransactionsReportDTO, Iterator<MemberTransactionDetailsReportData>> pair = reportHandler.handleTransactionsDetails(form);
        final MembersTransactionsReportDTO dto = pair.getFirst();
        final Iterator<MemberTransactionDetailsReportData> reportIterator = pair.getSecond();
        final Iterator iterator = IteratorUtils.filteredIterator(reportIterator, new Predicate() {
            @Override
            public boolean evaluate(final Object element) {
                final MemberTransactionDetailsReportData data = (MemberTransactionDetailsReportData) element;
                if (dto.isIncludeNoTraders()) {
                    return true;
                }
                return data.getAmount() != null;
            }
        });
        ExportMembersTransactionsDetailsToCsvResposneDTO response = new ExportMembersTransactionsDetailsToCsvResposneDTO(reportIterator);
        
        return response;
    }

   // @Override
    protected String fileName(final ActionContext context) {
        return "members_transactions_details_" + context.getUser().getUsername() + ".csv";
    }

    //@Override
    protected CSVWriter<?> resolveCSVWriter(final ActionContext context) {
        final LocalSettings localSettings = settingsService.getLocalSettings();
        final MembersTransactionsReportForm form = context.getForm();
        final MembersTransactionsReportDTO dto = getReportHandler().getDataBinder().readFromString(form.getMembersTransactionsReport());
        dto.setTransactionsPaymentFilters(paymentFilterService.load(EntityHelper.toIdsAsList(dto.getTransactionsPaymentFilters())));

        responseHelper.setDownload(context.getResponse(), "members_transactions_details.csv");
        final CSVWriter<MemberTransactionDetailsReportData> csv = CSVWriter.instance(MemberTransactionDetailsReportData.class, localSettings);

        csv.addColumn(context.message("member.username"), "username");
        if (dto.isMemberName()) {
            csv.addColumn(context.message("member.name"), "name");
        }
        if (dto.isBrokerUsername()) {
            csv.addColumn(context.message("member.brokerUsername"), "brokerUsername");
        }
        if (dto.isBrokerName()) {
            csv.addColumn(context.message("member.brokerName"), "brokerName");
        }
        csv.addColumn(context.message("account.type"), "accountTypeName");
        csv.addColumn(context.message("transfer.type"), "transferTypeName");
        csv.addColumn(context.message("transfer.date"), "date", localSettings.getDateConverter());
        csv.addColumn(context.message("transfer.amount"), "amount", localSettings.getNumberConverter());
        csv.addColumn(context.message("transfer.fromOrTo"), "relatedUsername");
        if (dto.isMemberName()) {
            csv.addColumn(context.message("transfer.fromOrTo"), "relatedName");
        }
        if (localSettings.getTransactionNumber() != null && localSettings.getTransactionNumber().isValid()) {
            csv.addColumn(context.message("transfer.transactionNumber"), "transactionNumber");
        }
        csv.addColumn(context.message("transfer.description"), "description");
        return csv;
    }
}
