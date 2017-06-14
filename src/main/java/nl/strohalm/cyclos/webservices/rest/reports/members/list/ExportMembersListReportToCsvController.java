package nl.strohalm.cyclos.webservices.rest.reports.members.list;

import org.springframework.stereotype.Controller;

import nl.strohalm.cyclos.webservices.rest.BaseRestController;
@Controller
public class ExportMembersListReportToCsvController extends BaseRestController{
	/*//private SettingsService settingsService;

	//class AccountTypeConverter implements Converter<Map<AccountType, BigDecimal>> {

        //private static final long serialVersionUID = 5139693651536516412L;
        private final AccountType accountType;
        private SettingsService settingsService;

        AccountTypeConverter(final AccountType accountType) {
            this.accountType = accountType;
        }

        //@Override
        public String toString(final Map<AccountType, BigDecimal> map) {
            final BigDecimal value = map.get(accountType);
            if (value == null) {
                return "";
            } else {
                return settingsService.getLocalSettings().getNumberConverter().toString(value);
            }
        }

       // @Override
        public Map<AccountType, BigDecimal> valueOf(final String string) {
            return null;
        }
    }

    class AdConverter implements Converter<Map<Ad.Status, Integer>> {

        private static final long serialVersionUID = -6394932401168004253L;
        private final Ad.Status   adStatus;

        AdConverter(final Ad.Status adStatus) {
            this.adStatus = adStatus;
        }

        @Override
        public String toString(final Map<Ad.Status, Integer> map) {
            final Integer value = map.get(adStatus);
            if (value == null) {
                return "";
            } else {
                return value.toString();
            }
        }

        @Override
        public Map<Ad.Status, Integer> valueOf(final String string) {
            return null;
        }
    }

    class ReferenceConverter implements Converter<Map<Reference.Level, Integer>> {

        private static final long     serialVersionUID = 366134132591193802L;
        private final Reference.Level referenceLevel;

        ReferenceConverter(final Reference.Level referenceLevel) {
            this.referenceLevel = referenceLevel;
        }

        @Override
        public String toString(final Map<Reference.Level, Integer> map) {
            final Integer value = map.get(referenceLevel);
            if (value == null) {
                return "";
            } else {
                return value.toString();
            }
        }

       // @Override
        public Map<Reference.Level, Integer> valueOf(final String string) {
            return null;
        }
    

    private MembersListReportHandler reportHandler;

    public MembersListReportHandler getReportHandler() {
        if (reportHandler == null) {
            reportHandler = new MembersListReportHandler(settingsService.getLocalSettings());
            SpringHelper.injectBeans(getServlet().getServletContext(), reportHandler);
        }
        return reportHandler;
    }
    

    @Override
    protected List<?> executeQuery(final ActionContext context) {
        List<?> list = null;
        final MembersListReportVOIterator voIterator = getReportHandler().handleReport(context);

        list = new AbstractList<MembersReportVO>() {
            @Override
            public MembersReportVO get(final int index) {
                throw new UnsupportedOperationException();
            }

            @Override
            public Iterator<MembersReportVO> iterator() {
                return voIterator;
            }

            @Override
            public int size() {
                throw new UnsupportedOperationException();
            }
        };

        return list;
    }

    @Override
    protected String fileName(final ActionContext context) {
        final User loggedUser = context.getUser();
        return "members_report_" + loggedUser.getUsername() + ".csv";
    }

    @Override
    protected CSVWriter<MembersReportVO> resolveCSVWriter(final ActionContext context) {
        final BeanBinder<MembersListReportDTO> binder = getReportHandler().getDataBinder();
        final MembersListReportForm form = context.getForm();
        final MembersListReportDTO dto = binder.readFromString(form.getMembersListReport());
        final List<MemberGroup> groups = (List<MemberGroup>) dto.getGroups();

        final LocalSettings settings = settingsService.getLocalSettings();
        final CSVWriter<MembersReportVO> csv = CSVWriter.instance(MembersReportVO.class, settings);

        csv.addColumn(context.message("member.username"), "member.username");

        // Add the conditional columns
        if (dto.isMemberName()) {
            csv.addColumn(context.message("member.name"), "member.name");
        }
        if (dto.isBrokerUsername()) {
            csv.addColumn(context.message("member.brokerUsername"), "member.broker.username");
        }
        if (dto.isBrokerName()) {
            csv.addColumn(context.message("member.brokerName"), "member.broker.name");
        }

        // Information about accounts
        if (dto.isAccountsInformation()) {
            final Collection<AccountType> accountTypes = getReportHandler().getAccountTypes(groups);
            for (final AccountType accountType : accountTypes) {
                if (dto.isAccountsCredits()) {
                    csv.addColumn(accountType.getName() + ": " + context.message("account.creditLimit"), "accountsCredits", new AccountTypeConverter(accountType));
                }
                if (dto.isAccountsUpperCredits()) {
                    csv.addColumn(accountType.getName() + ": " + context.message("account.upperCreditLimit"), "accountsUpperCredits", new AccountTypeConverter(accountType));
                }
                if (dto.isAccountsBalances()) {
                    csv.addColumn(accountType.getName() + ": " + context.message("account.balance"), "accountsBalances", new AccountTypeConverter(accountType));
                }
            }
        }

        // Information about ads
        if (dto.isActiveAds()) {
            csv.addColumn(context.message("reports.members.ads") + ": " + context.message("reports.members.ads.active"), "ads", new AdConverter(Ad.Status.ACTIVE));
        }
        if (dto.isExpiredAds()) {
            csv.addColumn(context.message("reports.members.ads") + ": " + context.message("reports.members.ads.expired"), "ads", new AdConverter(Ad.Status.EXPIRED));
        }
        if (dto.isPermanentAds()) {
            csv.addColumn(context.message("reports.members.ads") + ": " + context.message("reports.members.ads.permanent"), "ads", new AdConverter(Ad.Status.PERMANENT));
        }
        if (dto.isScheduledAds()) {
            csv.addColumn(context.message("reports.members.ads") + ": " + context.message("reports.members.ads.scheduled"), "ads", new AdConverter(Ad.Status.SCHEDULED));
        }

        // Information about references
        if (dto.isGivenReferences()) {
            csv.addColumn(context.message("reference.title.given.my") + ": " + context.message("reference.level.VERY_BAD"), "givenReferences", new ReferenceConverter(Reference.Level.VERY_BAD));
            csv.addColumn(context.message("reference.title.given.my") + ": " + context.message("reference.level.BAD"), "givenReferences", new ReferenceConverter(Reference.Level.BAD));
            csv.addColumn(context.message("reference.title.given.my") + ": " + context.message("reference.level.NEUTRAL"), "givenReferences", new ReferenceConverter(Reference.Level.NEUTRAL));
            csv.addColumn(context.message("reference.title.given.my") + ": " + context.message("reference.level.GOOD"), "givenReferences", new ReferenceConverter(Reference.Level.GOOD));
            csv.addColumn(context.message("reference.title.given.my") + ": " + context.message("reference.level.VERY_GOOD"), "givenReferences", new ReferenceConverter(Reference.Level.VERY_GOOD));
        }
        if (dto.isReceivedReferences()) {
            csv.addColumn(context.message("reference.title.received.my") + ": " + context.message("reference.level.VERY_BAD"), "receivedReferences", new ReferenceConverter(Reference.Level.VERY_BAD));
            csv.addColumn(context.message("reference.title.received.my") + ": " + context.message("reference.level.BAD"), "receivedReferences", new ReferenceConverter(Reference.Level.BAD));
            csv.addColumn(context.message("reference.title.received.my") + ": " + context.message("reference.level.NEUTRAL"), "receivedReferences", new ReferenceConverter(Reference.Level.NEUTRAL));
            csv.addColumn(context.message("reference.title.received.my") + ": " + context.message("reference.level.GOOD"), "receivedReferences", new ReferenceConverter(Reference.Level.GOOD));
            csv.addColumn(context.message("reference.title.received.my") + ": " + context.message("reference.level.VERY_GOOD"), "receivedReferences", new ReferenceConverter(Reference.Level.VERY_GOOD));
        }

        return csv;
    }*/
}
