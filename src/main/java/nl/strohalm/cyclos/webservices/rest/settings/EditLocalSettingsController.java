package nl.strohalm.cyclos.webservices.rest.settings;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TimeZone;
import nl.strohalm.cyclos.controls.ActionContext;
import nl.strohalm.cyclos.entities.access.Channel;
import nl.strohalm.cyclos.entities.settings.LocalSettings;
import nl.strohalm.cyclos.services.customization.MessageImportType;
import nl.strohalm.cyclos.utils.FileUnits;
import nl.strohalm.cyclos.utils.RequestHelper;
import nl.strohalm.cyclos.utils.TextFormat;
import nl.strohalm.cyclos.utils.TimePeriod;
import nl.strohalm.cyclos.utils.binding.MapBean;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;
import nl.strohalm.cyclos.webservices.rest.GenericResponse;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class EditLocalSettingsController extends BaseRestController {

    public static class LocalSettingsResponse extends GenericResponse {

        private List<LocalSettings.Language> language;
        private List<FileUnits> uploadUnits;
        private List<LocalSettings.NumberLocale> numberLocales;
        private List<LocalSettings.Precision> precisions;
        private List<LocalSettings.DecimalInputMethod> decimalInputMethods;
        private Map<LocalSettings.DatePattern, String> datePatterns;
        private List<LocalSettings.TimePattern> timePatterns;
        private List< LocalSettings.CsvRecordSeparator> csvRecordSeparators;
        private List<LocalSettings.CsvValueSeparator> csvValueSeparators;
        private List<LocalSettings.CsvStringQuote> csvStringQuotes;
        private List<LocalSettings.MemberResultDisplay> memberResultDisplays;
        private List<LocalSettings.SortOrder> memberSortOrders;
        private List< TextFormat> textFormats;
        private List<String> timeZones;
        private List<TimePeriod.Field> brokeringExpirationUnits;
        private List<TimePeriod.Field> deleteMessagesExpirationUnits;
        private List<TimePeriod.Field> maxChargebackTimeUnits;
        private List<TimePeriod.Field> indexRebuildingTimeUnits;
        private List<Channel> smsChannels;
        private List<ChannelEntity> listNonChannel;

        public List<ChannelEntity> getListNonChannel() {
            return listNonChannel;
        }

        public void setListNonChannel(List<ChannelEntity> listNonChannel) {
            this.listNonChannel = listNonChannel;
        }

        public List<Channel> getSmsChannels() {
            return smsChannels;
        }

        public void setSmsChannels(List<Channel> smsChannels) {
            this.smsChannels = smsChannels;
        }

        public List<TimePeriod.Field> getBrokeringExpirationUnits() {
            return brokeringExpirationUnits;
        }

        public void setBrokeringExpirationUnits(List<TimePeriod.Field> brokeringExpirationUnits) {
            this.brokeringExpirationUnits = brokeringExpirationUnits;
        }

        public List<TimePeriod.Field> getDeleteMessagesExpirationUnits() {
            return deleteMessagesExpirationUnits;
        }

        public void setDeleteMessagesExpirationUnits(List<TimePeriod.Field> deleteMessagesExpirationUnits) {
            this.deleteMessagesExpirationUnits = deleteMessagesExpirationUnits;
        }

        public List<TimePeriod.Field> getMaxChargebackTimeUnits() {
            return maxChargebackTimeUnits;
        }

        public void setMaxChargebackTimeUnits(List<TimePeriod.Field> maxChargebackTimeUnits) {
            this.maxChargebackTimeUnits = maxChargebackTimeUnits;
        }

        public List<TimePeriod.Field> getIndexRebuildingTimeUnits() {
            return indexRebuildingTimeUnits;
        }

        public void setIndexRebuildingTimeUnits(List<TimePeriod.Field> indexRebuildingTimeUnits) {
            this.indexRebuildingTimeUnits = indexRebuildingTimeUnits;
        }

        public List<String> getTimeZones() {
            return timeZones;
        }

        public void setTimeZones(List<String> timeZones) {
            this.timeZones = timeZones;
        }

        public List<TextFormat> getTextFormats() {
            return textFormats;
        }

        public void setTextFormats(List<TextFormat> textFormats) {
            this.textFormats = textFormats;
        }

        public List<LocalSettings.SortOrder> getMemberSortOrders() {
            return memberSortOrders;
        }

        public void setMemberSortOrders(List<LocalSettings.SortOrder> memberSortOrders) {
            this.memberSortOrders = memberSortOrders;
        }

        public List<LocalSettings.MemberResultDisplay> getMemberResultDisplays() {
            return memberResultDisplays;
        }

        public void setMemberResultDisplays(List<LocalSettings.MemberResultDisplay> memberResultDisplays) {
            this.memberResultDisplays = memberResultDisplays;
        }

        public List<LocalSettings.CsvStringQuote> getCsvStringQuotes() {
            return csvStringQuotes;
        }

        public void setCsvStringQuotes(List<LocalSettings.CsvStringQuote> csvStringQuotes) {
            this.csvStringQuotes = csvStringQuotes;
        }

        public List<LocalSettings.CsvValueSeparator> getCsvValueSeparators() {
            return csvValueSeparators;
        }

        public void setCsvValueSeparators(List<LocalSettings.CsvValueSeparator> csvValueSeparators) {
            this.csvValueSeparators = csvValueSeparators;
        }

        public List<LocalSettings.CsvRecordSeparator> getCsvRecordSeparators() {
            return csvRecordSeparators;
        }

        public void setCsvRecordSeparators(List<LocalSettings.CsvRecordSeparator> csvRecordSeparators) {
            this.csvRecordSeparators = csvRecordSeparators;
        }

        public List<LocalSettings.TimePattern> getTimePatterns() {
            return timePatterns;
        }

        public void setTimePatterns(List<LocalSettings.TimePattern> timePatterns) {
            this.timePatterns = timePatterns;
        }

        public Map<LocalSettings.DatePattern, String> getDatePatterns() {
            return datePatterns;
        }

        public void setDatePatterns(Map<LocalSettings.DatePattern, String> datePatterns) {
            this.datePatterns = datePatterns;
        }

        public List<LocalSettings.DecimalInputMethod> getDecimalInputMethods() {
            return decimalInputMethods;
        }

        public void setDecimalInputMethods(List<LocalSettings.DecimalInputMethod> decimalInputMethods) {
            this.decimalInputMethods = decimalInputMethods;
        }

        public List<LocalSettings.Precision> getPrecisions() {
            return precisions;
        }

        public void setPrecisions(List<LocalSettings.Precision> precisions) {
            this.precisions = precisions;
        }

        public List<LocalSettings.NumberLocale> getNumberLocales() {
            return numberLocales;
        }

        public void setNumberLocales(List<LocalSettings.NumberLocale> numberLocales) {
            this.numberLocales = numberLocales;
        }

        public List<FileUnits> getUploadUnits() {
            return uploadUnits;
        }

        public void setUploadUnits(List<FileUnits> uploadUnits) {
            this.uploadUnits = uploadUnits;
        }

        public List<LocalSettings.Language> getLanguage() {
            return language;
        }

        public void setLanguage(List<LocalSettings.Language> language) {
            this.language = language;
        }

    }

    public static class ChannelEntity {

        private Long id;
        private String internalName;
        private String displayName;
        private String credentials;
        private String paymentRequestWebServiceUrl;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getInternalName() {
            return internalName;
        }

        public void setInternalName(String internalName) {
            this.internalName = internalName;
        }

        public String getDisplayName() {
            return displayName;
        }

        public void setDisplayName(String displayName) {
            this.displayName = displayName;
        }

        public String getCredentials() {
            return credentials;
        }

        public void setCredentials(String credentials) {
            this.credentials = credentials;
        }

        public String getPaymentRequestWebServiceUrl() {
            return paymentRequestWebServiceUrl;
        }

        public void setPaymentRequestWebServiceUrl(String paymentRequestWebServiceUrl) {
            this.paymentRequestWebServiceUrl = paymentRequestWebServiceUrl;
        }

    }

    @RequestMapping(value = "admin/editLocalSettings", method = RequestMethod.GET)
    @ResponseBody
    public LocalSettingsResponse prepareForm() {
        LocalSettingsResponse response = new LocalSettingsResponse();
        final LocalSettings settings = settingsService.getLocalSettings();

//        form.setSetting("enableSms", StringUtils.isNotEmpty(settings.getSendSmsWebServiceUrl()) || StringUtils.isNotEmpty(settings.getSmsChannelName()));
//        final LocalSettings.TransactionNumber transactionNumber = settings.getTransactionNumber();
//        form.setSetting("enableTransactionNumber", transactionNumber != null && transactionNumber.getPadLength() > 0);
//
//        getDataBinder().writeAsString(form.getSetting(), settings);
        //RequestHelper.storeEnum(request, LocalSettings.Language.class, "languages");
        List<LocalSettings.Language> language = new ArrayList();
        language.add(LocalSettings.Language.CZECH);
        language.add(LocalSettings.Language.CHINESE_SIMPLIFIED);
        language.add(LocalSettings.Language.DUTCH);
        language.add(LocalSettings.Language.ENGLISH);
        language.add(LocalSettings.Language.FRENCH);
        language.add(LocalSettings.Language.GERMAN);
        language.add(LocalSettings.Language.GREEK);
        language.add(LocalSettings.Language.ITALIAN);
        language.add(LocalSettings.Language.JAPANESE);
        language.add(LocalSettings.Language.PORTUGUESE_BRAZIL);
        language.add(LocalSettings.Language.RUSSIAN);
        language.add(LocalSettings.Language.SPANISH);
        response.setLanguage(language);

        //RequestHelper.storeEnum(request, FileUnits.class, "uploadUnits");
        List<FileUnits> uploadUnits = new ArrayList();
        uploadUnits.add(FileUnits.BYTES);
        uploadUnits.add(FileUnits.KILO_BYTES);
        uploadUnits.add(FileUnits.MEGA_BYTES);
        response.setUploadUnits(uploadUnits);

        //  RequestHelper.storeEnum(request, LocalSettings.NumberLocale.class, "numberLocales");
        List<LocalSettings.NumberLocale> numberLocales = new ArrayList();
        numberLocales.add(LocalSettings.NumberLocale.COMMA_AS_DECIMAL);
        numberLocales.add(LocalSettings.NumberLocale.PERIOD_AS_DECIMAL);
        response.setNumberLocales(numberLocales);

        // RequestHelper.storeEnum(request, LocalSettings.Precision.class, "precisions");
        List<LocalSettings.Precision> precisions = new ArrayList();
        precisions.add(LocalSettings.Precision.ONE);
        precisions.add(LocalSettings.Precision.TWO);
        precisions.add(LocalSettings.Precision.THREE);
        precisions.add(LocalSettings.Precision.FOUR);
        precisions.add(LocalSettings.Precision.FIVE);
        precisions.add(LocalSettings.Precision.SIX);
        response.setPrecisions(precisions);

        // RequestHelper.storeEnum(request, LocalSettings.DecimalInputMethod.class, "decimalInputMethods");
        List<LocalSettings.DecimalInputMethod> decimalInputMethods = new ArrayList();
        decimalInputMethods.add(LocalSettings.DecimalInputMethod.RTL);
        decimalInputMethods.add(LocalSettings.DecimalInputMethod.LTR);
        response.setDecimalInputMethods(decimalInputMethods);

        final Map<LocalSettings.DatePattern, String> datePatterns = new LinkedHashMap<LocalSettings.DatePattern, String>();
        for (final LocalSettings.DatePattern datePattern : LocalSettings.DatePattern.values()) {
            datePatterns.put(datePattern, messageHelper.getDatePatternDescription(datePattern).toUpperCase());
        }
        //request.setAttribute("datePatterns", datePatterns);
        response.setDatePatterns(datePatterns);

        //RequestHelper.storeEnum(request, LocalSettings.TimePattern.class, "timePatterns");
        List<LocalSettings.TimePattern> timePatterns = new ArrayList();
        timePatterns.add(LocalSettings.TimePattern.HH24_MM);
        timePatterns.add(LocalSettings.TimePattern.HH12_MM_SS);
        timePatterns.add(LocalSettings.TimePattern.HH24_MM_SS);
        timePatterns.add(LocalSettings.TimePattern.HH12_MM);
        response.setTimePatterns(timePatterns);

        // RequestHelper.storeEnum(request, LocalSettings.CsvRecordSeparator.class, "csvRecordSeparators");
        List< LocalSettings.CsvRecordSeparator> csvRecordSeparators = new ArrayList();
        csvRecordSeparators.add(LocalSettings.CsvRecordSeparator.LF);
        csvRecordSeparators.add(LocalSettings.CsvRecordSeparator.CR);
        csvRecordSeparators.add(LocalSettings.CsvRecordSeparator.CR_LF);
        response.setCsvRecordSeparators(csvRecordSeparators);

        // RequestHelper.storeEnum(request, LocalSettings.CsvValueSeparator.class, "csvValueSeparators");
        List<LocalSettings.CsvValueSeparator> csvValueSeparators = new ArrayList();
        csvValueSeparators.add(LocalSettings.CsvValueSeparator.TAB);
        csvValueSeparators.add(LocalSettings.CsvValueSeparator.COMMA);
        csvValueSeparators.add(LocalSettings.CsvValueSeparator.SEMICOLON);
        response.setCsvValueSeparators(csvValueSeparators);

        //  RequestHelper.storeEnum(request, LocalSettings.CsvStringQuote.class, "csvStringQuotes");
        List<LocalSettings.CsvStringQuote> csvStringQuotes = new ArrayList();
        csvStringQuotes.add(LocalSettings.CsvStringQuote.NONE);
        csvStringQuotes.add(LocalSettings.CsvStringQuote.DOUBLE_QUOTE);
        csvStringQuotes.add(LocalSettings.CsvStringQuote.SINGLE_QUOTE);
        response.setCsvStringQuotes(csvStringQuotes);

        //RequestHelper.storeEnum(request, LocalSettings.MemberResultDisplay.class, "memberResultDisplays");
        List<LocalSettings.MemberResultDisplay> memberResultDisplays = new ArrayList();
        memberResultDisplays.add(LocalSettings.MemberResultDisplay.NAME);
        memberResultDisplays.add(LocalSettings.MemberResultDisplay.USERNAME);
        response.setMemberResultDisplays(memberResultDisplays);

        // RequestHelper.storeEnum(request, LocalSettings.SortOrder.class, "memberSortOrders");
        List<LocalSettings.SortOrder> memberSortOrders = new ArrayList();
        memberSortOrders.add(LocalSettings.SortOrder.ALPHABETICAL);
        memberSortOrders.add(LocalSettings.SortOrder.CHRONOLOGICAL);
        response.setMemberSortOrders(memberSortOrders);

        //RequestHelper.storeEnum(request, TextFormat.class, "textFormats");
        List< TextFormat> textFormats = new ArrayList();
        textFormats.add(TextFormat.RICH);
        textFormats.add(TextFormat.PLAIN);
        response.setTextFormats(textFormats);
        response.setBrokeringExpirationUnits(Arrays.asList(TimePeriod.Field.DAYS, TimePeriod.Field.MONTHS, TimePeriod.Field.YEARS));
        response.setDeleteMessagesExpirationUnits(Arrays.asList(TimePeriod.Field.DAYS, TimePeriod.Field.MONTHS, TimePeriod.Field.YEARS));
        response.setMaxChargebackTimeUnits(Arrays.asList(TimePeriod.Field.DAYS, TimePeriod.Field.WEEKS, TimePeriod.Field.MONTHS));
        response.setIndexRebuildingTimeUnits(Arrays.asList(TimePeriod.Field.DAYS, TimePeriod.Field.WEEKS, TimePeriod.Field.MONTHS));
        List<Channel> channels = channelService.listNonBuiltin();
        System.out.println("-----channels: " + channels);
        List<ChannelEntity> listChannels = new ArrayList();
        for (Channel channel : channels) {
            ChannelEntity entity = new ChannelEntity();
            entity.setId(channel.getId());
            entity.setDisplayName(channel.getDisplayName());
            entity.setInternalName(channel.getInternalName());
            entity.setPaymentRequestWebServiceUrl(channel.getPaymentRequestWebServiceUrl());
            entity.setCredentials(channel.getCredentials().LOGIN_PASSWORD.toString());

        }
        response.setListNonChannel(listChannels);

//        request.setAttribute("smsCustomFields", channelService.possibleCustomFieldsAsPrincipal());
        // Transform the time zones in an
        final List<String> timeZones = new ArrayList<String>();
        for (final String id : TimeZone.getAvailableIDs()) {
            if (!id.contains("/") || id.contains("Etc")) {
                continue;
            }
            timeZones.add(id);
        }
        Collections.sort(timeZones);
        timeZones.add(0, "GMT");
        response.setTimeZones(timeZones);
        response.setStatus(0);
        return response;
    }

    public static class LocalSettingsParameters {

        private String prefix;
        private Integer padLength;
        private String suffix;
        private Boolean enabled;

        private String applicationName;
        private String applicationUsername;
        private String rootUrl;

        private String language;
        private String numberLocale;
        private String precision;
        private String highPrecision;

        private String decimalInputMethod;
        private String datePattern;
        private String timePattern;

        private String timeZone;
        private String containerUrl;

        private Integer maxIteratorResults;
        private Integer maxPageResults;
        private Integer maxAjaxResults;
        private Integer maxUploadSize;

        private String maxUploadUnits;
        private Integer maxImageWidth;
        private Integer maxImageHeight;

        private Integer maxThumbnailWidth;

        private Integer maxThumbnailHeight;

        private Boolean csvUseHeader;
        private String csvRecordSeparator;
        private String csvValueSeparator;
        private String csvStringQuote;

        private String cyclosId;
        private Boolean smsEnabled;
        private String sendSmsWebServiceUrl;
        private Long smsCustomFieldId;
        private String smsChannelName;
        private Boolean emailRequired;
        private Boolean emailUnique;

        private String brokeringExpirationPeriod;
        private String deleteMessagesOnTrashAfter;
        private String deletePendingRegistrationsAfter;
        private String memberSortOrder;
        private String memberResultDisplay;
        private String adDescriptionFormat;
        private String messageFormat;

        private Integer schedulingHour;
        private String schedulingMinute;
        private String transferListenerClass;

        private String maxChargebackTime;
        private String chargebackDescription;
        private Boolean showCountersInAdCategories;

        public String getPrefix() {
            return prefix;
        }

        public void setPrefix(String prefix) {
            this.prefix = prefix;
        }

        public Integer getPadLength() {
            return padLength;
        }

        public void setPadLength(Integer padLength) {
            this.padLength = padLength;
        }

        public String getSuffix() {
            return suffix;
        }

        public void setSuffix(String suffix) {
            this.suffix = suffix;
        }

        public Boolean getEnabled() {
            return enabled;
        }

        public void setEnabled(Boolean enabled) {
            this.enabled = enabled;
        }

        public String getApplicationName() {
            return applicationName;
        }

        public void setApplicationName(String applicationName) {
            this.applicationName = applicationName;
        }

        public String getApplicationUsername() {
            return applicationUsername;
        }

        public void setApplicationUsername(String applicationUsername) {
            this.applicationUsername = applicationUsername;
        }

        public String getRootUrl() {
            return rootUrl;
        }

        public void setRootUrl(String rootUrl) {
            this.rootUrl = rootUrl;
        }

        public String getLanguage() {
            return language;
        }

        public void setLanguage(String language) {
            this.language = language;
        }

        public String getNumberLocale() {
            return numberLocale;
        }

        public void setNumberLocale(String numberLocale) {
            this.numberLocale = numberLocale;
        }

        public String getPrecision() {
            return precision;
        }

        public void setPrecision(String precision) {
            this.precision = precision;
        }

        public String getHighPrecision() {
            return highPrecision;
        }

        public void setHighPrecision(String highPrecision) {
            this.highPrecision = highPrecision;
        }

        public String getDecimalInputMethod() {
            return decimalInputMethod;
        }

        public void setDecimalInputMethod(String decimalInputMethod) {
            this.decimalInputMethod = decimalInputMethod;
        }

        public String getDatePattern() {
            return datePattern;
        }

        public void setDatePattern(String datePattern) {
            this.datePattern = datePattern;
        }

        public String getTimePattern() {
            return timePattern;
        }

        public void setTimePattern(String timePattern) {
            this.timePattern = timePattern;
        }

        public String getTimeZone() {
            return timeZone;
        }

        public void setTimeZone(String timeZone) {
            this.timeZone = timeZone;
        }

        public String getContainerUrl() {
            return containerUrl;
        }

        public void setContainerUrl(String containerUrl) {
            this.containerUrl = containerUrl;
        }

        public Integer getMaxIteratorResults() {
            return maxIteratorResults;
        }

        public void setMaxIteratorResults(Integer maxIteratorResults) {
            this.maxIteratorResults = maxIteratorResults;
        }

        public Integer getMaxPageResults() {
            return maxPageResults;
        }

        public void setMaxPageResults(Integer maxPageResults) {
            this.maxPageResults = maxPageResults;
        }

        public Integer getMaxAjaxResults() {
            return maxAjaxResults;
        }

        public void setMaxAjaxResults(Integer maxAjaxResults) {
            this.maxAjaxResults = maxAjaxResults;
        }

        public Integer getMaxUploadSize() {
            return maxUploadSize;
        }

        public void setMaxUploadSize(Integer maxUploadSize) {
            this.maxUploadSize = maxUploadSize;
        }

        public String getMaxUploadUnits() {
            return maxUploadUnits;
        }

        public void setMaxUploadUnits(String maxUploadUnits) {
            this.maxUploadUnits = maxUploadUnits;
        }

        public Integer getMaxImageWidth() {
            return maxImageWidth;
        }

        public void setMaxImageWidth(Integer maxImageWidth) {
            this.maxImageWidth = maxImageWidth;
        }

        public Integer getMaxImageHeight() {
            return maxImageHeight;
        }

        public void setMaxImageHeight(Integer maxImageHeight) {
            this.maxImageHeight = maxImageHeight;
        }

        public Integer getMaxThumbnailWidth() {
            return maxThumbnailWidth;
        }

        public void setMaxThumbnailWidth(Integer maxThumbnailWidth) {
            this.maxThumbnailWidth = maxThumbnailWidth;
        }

        public Integer getMaxThumbnailHeight() {
            return maxThumbnailHeight;
        }

        public void setMaxThumbnailHeight(Integer maxThumbnailHeight) {
            this.maxThumbnailHeight = maxThumbnailHeight;
        }

        public Boolean getCsvUseHeader() {
            return csvUseHeader;
        }

        public void setCsvUseHeader(Boolean csvUseHeader) {
            this.csvUseHeader = csvUseHeader;
        }

        public String getCsvRecordSeparator() {
            return csvRecordSeparator;
        }

        public void setCsvRecordSeparator(String csvRecordSeparator) {
            this.csvRecordSeparator = csvRecordSeparator;
        }

        public String getCsvValueSeparator() {
            return csvValueSeparator;
        }

        public void setCsvValueSeparator(String csvValueSeparator) {
            this.csvValueSeparator = csvValueSeparator;
        }

        public String getCsvStringQuote() {
            return csvStringQuote;
        }

        public void setCsvStringQuote(String csvStringQuote) {
            this.csvStringQuote = csvStringQuote;
        }

        public String getCyclosId() {
            return cyclosId;
        }

        public void setCyclosId(String cyclosId) {
            this.cyclosId = cyclosId;
        }

        public Boolean getSmsEnabled() {
            return smsEnabled;
        }

        public void setSmsEnabled(Boolean smsEnabled) {
            this.smsEnabled = smsEnabled;
        }

        public String getSendSmsWebServiceUrl() {
            return sendSmsWebServiceUrl;
        }

        public void setSendSmsWebServiceUrl(String sendSmsWebServiceUrl) {
            this.sendSmsWebServiceUrl = sendSmsWebServiceUrl;
        }

        public Long getSmsCustomFieldId() {
            return smsCustomFieldId;
        }

        public void setSmsCustomFieldId(Long smsCustomFieldId) {
            this.smsCustomFieldId = smsCustomFieldId;
        }

        public String getSmsChannelName() {
            return smsChannelName;
        }

        public void setSmsChannelName(String smsChannelName) {
            this.smsChannelName = smsChannelName;
        }

        public Boolean getEmailRequired() {
            return emailRequired;
        }

        public void setEmailRequired(Boolean emailRequired) {
            this.emailRequired = emailRequired;
        }

        public Boolean getEmailUnique() {
            return emailUnique;
        }

        public void setEmailUnique(Boolean emailUnique) {
            this.emailUnique = emailUnique;
        }

        public String getBrokeringExpirationPeriod() {
            return brokeringExpirationPeriod;
        }

        public void setBrokeringExpirationPeriod(String brokeringExpirationPeriod) {
            this.brokeringExpirationPeriod = brokeringExpirationPeriod;
        }

        public String getDeleteMessagesOnTrashAfter() {
            return deleteMessagesOnTrashAfter;
        }

        public void setDeleteMessagesOnTrashAfter(String deleteMessagesOnTrashAfter) {
            this.deleteMessagesOnTrashAfter = deleteMessagesOnTrashAfter;
        }

        public String getDeletePendingRegistrationsAfter() {
            return deletePendingRegistrationsAfter;
        }

        public void setDeletePendingRegistrationsAfter(String deletePendingRegistrationsAfter) {
            this.deletePendingRegistrationsAfter = deletePendingRegistrationsAfter;
        }

        public String getMemberSortOrder() {
            return memberSortOrder;
        }

        public void setMemberSortOrder(String memberSortOrder) {
            this.memberSortOrder = memberSortOrder;
        }

        public String getMemberResultDisplay() {
            return memberResultDisplay;
        }

        public void setMemberResultDisplay(String memberResultDisplay) {
            this.memberResultDisplay = memberResultDisplay;
        }

        public String getAdDescriptionFormat() {
            return adDescriptionFormat;
        }

        public void setAdDescriptionFormat(String adDescriptionFormat) {
            this.adDescriptionFormat = adDescriptionFormat;
        }

        public String getMessageFormat() {
            return messageFormat;
        }

        public void setMessageFormat(String messageFormat) {
            this.messageFormat = messageFormat;
        }

        public Integer getSchedulingHour() {
            return schedulingHour;
        }

        public void setSchedulingHour(Integer schedulingHour) {
            this.schedulingHour = schedulingHour;
        }

        public String getSchedulingMinute() {
            return schedulingMinute;
        }

        public void setSchedulingMinute(String schedulingMinute) {
            this.schedulingMinute = schedulingMinute;
        }

        public String getTransferListenerClass() {
            return transferListenerClass;
        }

        public void setTransferListenerClass(String transferListenerClass) {
            this.transferListenerClass = transferListenerClass;
        }

        public String getMaxChargebackTime() {
            return maxChargebackTime;
        }

        public void setMaxChargebackTime(String maxChargebackTime) {
            this.maxChargebackTime = maxChargebackTime;
        }

        public String getChargebackDescription() {
            return chargebackDescription;
        }

        public void setChargebackDescription(String chargebackDescription) {
            this.chargebackDescription = chargebackDescription;
        }

        public Boolean getShowCountersInAdCategories() {
            return showCountersInAdCategories;
        }

        public void setShowCountersInAdCategories(Boolean showCountersInAdCategories) {
            this.showCountersInAdCategories = showCountersInAdCategories;
        }

    }

    @RequestMapping(value = "admin/editLocalSettings", method = RequestMethod.POST)
    @ResponseBody
    public GenericResponse submit() {
        GenericResponse response = new GenericResponse();
        final LocalSettings oldSettings = settingsService.getLocalSettings();

        LocalSettings settings = resolveLocalSettings();
        settings = settingsService.save(settings);

        // There are some steps when the language is changed...
        if (oldSettings.getLanguage() != settings.getLanguage()) {
            // Replace message bundle file
            final Properties properties = translationMessageService.readFile(settings.getLocale());
            translationMessageService.importFromProperties(properties, MessageImportType.REPLACE);

            // Replace the translation settings (mail and messages)
            settingsService.reloadTranslation();
        }

        response.setStatus(0);
        return response;

    }
    
      private LocalSettings resolveLocalSettings() {
//        final EditLocalSettingsForm form = context.getForm();
        final LocalSettings settings = null;
//                getDataBinder().readFromString(form.getSetting());
//        // If transaction number is not enabled, clear the setting
//        final MapBean tn = (MapBean) form.getSetting("transactionNumber");
//        if (!Boolean.parseBoolean((String) tn.get("enabled"))) {
//            settings.setTransactionNumber(null);
//        }
        return settings;
    }

}
