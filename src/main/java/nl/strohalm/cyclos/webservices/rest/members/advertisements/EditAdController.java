/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.strohalm.cyclos.webservices.rest.members.advertisements;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import nl.strohalm.cyclos.annotations.Inject;
import nl.strohalm.cyclos.entities.Relationship;
import nl.strohalm.cyclos.entities.accounts.AccountType;
import nl.strohalm.cyclos.entities.accounts.Currency;
import nl.strohalm.cyclos.entities.accounts.MemberAccountType;
import nl.strohalm.cyclos.entities.ads.Ad;
import nl.strohalm.cyclos.entities.ads.AdCategory;
import nl.strohalm.cyclos.entities.customization.fields.AdCustomField;
import nl.strohalm.cyclos.entities.customization.fields.AdCustomFieldValue;
import nl.strohalm.cyclos.entities.customization.fields.CustomFieldValue;
import nl.strohalm.cyclos.entities.groups.MemberGroup;
import nl.strohalm.cyclos.entities.groups.MemberGroupSettings;
import nl.strohalm.cyclos.entities.members.Element;
import nl.strohalm.cyclos.entities.members.Member;
import nl.strohalm.cyclos.entities.members.Operator;
import nl.strohalm.cyclos.entities.settings.LocalSettings;
import nl.strohalm.cyclos.entities.settings.events.LocalSettingsEvent;
import nl.strohalm.cyclos.services.accounts.AccountTypeService;
import nl.strohalm.cyclos.services.accounts.CurrencyService;
import nl.strohalm.cyclos.services.ads.AdService;
import nl.strohalm.cyclos.services.elements.ElementService;
import nl.strohalm.cyclos.services.settings.SettingsService;
import nl.strohalm.cyclos.utils.Period;
import nl.strohalm.cyclos.utils.RelationshipHelper;
import nl.strohalm.cyclos.utils.TextFormat;
import nl.strohalm.cyclos.utils.TimePeriod;
import nl.strohalm.cyclos.utils.access.LoggedUser;
import nl.strohalm.cyclos.utils.binding.BeanBinder;
import nl.strohalm.cyclos.utils.binding.BeanCollectionBinder;
import nl.strohalm.cyclos.utils.binding.DataBinder;
import nl.strohalm.cyclos.utils.binding.DataBinderHelper;
import nl.strohalm.cyclos.utils.binding.PropertyBinder;
import nl.strohalm.cyclos.utils.conversion.CoercionHelper;
import nl.strohalm.cyclos.utils.conversion.HtmlConverter;
import nl.strohalm.cyclos.utils.conversion.IdConverter;
import nl.strohalm.cyclos.utils.conversion.ReferenceConverter;
import nl.strohalm.cyclos.utils.conversion.StringTrimmerConverter;
import nl.strohalm.cyclos.utils.validation.ValidationException;
import nl.strohalm.cyclos.webservices.WebServiceContext;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;
import nl.strohalm.cyclos.webservices.rest.GenericResponse;

import org.apache.struts.upload.FormFile;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 *
 * @author Lue
 */
@Controller
public class EditAdController extends BaseRestController {

    private AccountTypeService accountTypeService;
    private CurrencyService currencyService;
    private DataBinder<Ad> writeDataBinder;
    private ReadWriteLock lock = new ReentrantReadWriteLock(true);
    private ElementService elementService;
    private SettingsService settingsService;
    private AdService adService;
    
     private static final Relationship[] FETCH = { RelationshipHelper.nested(Ad.Relationships.CATEGORY, RelationshipHelper.nested(AdCategory.MAX_LEVEL, AdCategory.Relationships.PARENT)), RelationshipHelper.nested(Ad.Relationships.OWNER, Element.Relationships.USER), RelationshipHelper.nested(Ad.Relationships.OWNER, Element.Relationships.GROUP), Ad.Relationships.CUSTOM_VALUES, Ad.Relationships.IMAGES, Ad.Relationships.CURRENCY };

    public AdService getAdService() {
        return adService;
    }

    public void setWriteDataBinder(DataBinder<Ad> writeDataBinder) {
        this.writeDataBinder = writeDataBinder;
    }

    public void setLock(ReadWriteLock lock) {
        this.lock = lock;
    }

    public void setElementService(ElementService elementService) {
        this.elementService = elementService;
    }

    public void setSettingsService(SettingsService settingsService) {
        this.settingsService = settingsService;
    }

    @Inject
    public void setAdService(AdService adService) {
        this.adService = adService;
    }

    // Used to get data and save to database
    public DataBinder<Ad> getWriteDataBinder() {
        try {
            lock.readLock().lock();
            if (writeDataBinder == null) {
                final LocalSettings settings = settingsService.getLocalSettings();
                final BeanBinder<? extends CustomFieldValue> customValueBinder = BeanBinder.instance(AdCustomFieldValue.class);
                customValueBinder.registerBinder("field", PropertyBinder.instance(AdCustomField.class, "field", ReferenceConverter.instance(AdCustomField.class)));
                customValueBinder.registerBinder("value", PropertyBinder.instance(String.class, "value", HtmlConverter.instance()));

                final BeanBinder<Ad> binder = BeanBinder.instance(Ad.class);
                binder.registerBinder("id", PropertyBinder.instance(Long.class, "id", IdConverter.instance()));
                binder.registerBinder("owner", PropertyBinder.instance(Member.class, "owner", ReferenceConverter.instance(Member.class)));
                binder.registerBinder("tradeType", PropertyBinder.instance(Ad.TradeType.class, "tradeType"));
                binder.registerBinder("category", PropertyBinder.instance(AdCategory.class, "category", ReferenceConverter.instance(AdCategory.class)));
                binder.registerBinder("title", PropertyBinder.instance(String.class, "title"));
                binder.registerBinder("externalPublication", PropertyBinder.instance(Boolean.TYPE, "externalPublication"));
                binder.registerBinder("permanent", PropertyBinder.instance(Boolean.TYPE, "permanent"));
                binder.registerBinder("publicationPeriod", DataBinderHelper.rawPeriodBinder(settings, "publicationPeriod"));
                binder.registerBinder("currency", PropertyBinder.instance(Currency.class, "currency"));
                binder.registerBinder("price", PropertyBinder.instance(BigDecimal.class, "price", settings.getNumberConverter()));
                binder.registerBinder("html", PropertyBinder.instance(Boolean.TYPE, "html"));
                binder.registerBinder("customValues", BeanCollectionBinder.instance(customValueBinder, "customValues"));

                writeDataBinder = binder;
            }
            return writeDataBinder;
        } finally {
            lock.readLock().unlock();
        }
    }

    // @Override
    public void onLocalSettingsUpdate(final LocalSettingsEvent event) {
        try {
            lock.writeLock().lock();
            //super.onLocalSettingsUpdate(event);
            writeDataBinder = null;
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Inject
    public void setAccountTypeService(final AccountTypeService accountTypeService) {
        this.accountTypeService = accountTypeService;
    }

    @Inject
    public void setCurrencyService(final CurrencyService currencyService) {
        this.currencyService = currencyService;
    }

    /**
     * gets the member
     *
     * @return
     */
    public Member getMember() {
        Member member;
        final long adId = LoggedUser.user().getId();
        if (adId > 0) {
            return getAdService().load(adId, Ad.Relationships.OWNER).getOwner();
        }

        final Element loggedElement = LoggedUser.element();
        if (LoggedUser.user().getId() <= 0 || LoggedUser.user().getId() == loggedElement.getId()) {
            if (LoggedUser.isAdministrator()) {
                throw new ValidationException();
            }
            if (LoggedUser.isOperator()) {
                member = ((Operator) LoggedUser.element()).getMember();
            } else { // context.isMember()
                member = LoggedUser.element();
            }
        } else {
            final Element element = elementService.load(LoggedUser.user().getId(), Element.Relationships.USER);
            if (!(element instanceof Member)) {
                throw new ValidationException();
            }
            member = (Member) element;
        }
        member = elementService.load(member.getId(), Element.Relationships.GROUP);
        return member;
    }

    /**
     * gets the number of ads for this member
     *
     * @param member
     * @return an int indicating the number of ads
     */
    public int getNumberOfAds(final Member member) {
        final Map<Ad.Status, Integer> adMap = getAdService().getNumberOfAds(null, member);
        final Collection<Integer> values = adMap.values();
        int totalAds = 0;
        for (final Integer i : values) {
            totalAds += i;
        }
        return totalAds;
    }

    public static class EditAdsRequest {

        private long id;
        private long memberId;
        private FormFile picture;
        private String pictureCaption;
        private Map<String, Object> ad=new HashMap<String, Object>();

//        public EditAdsRequest() {
//            setAd("publicationPeriod", new MapBean("begin", "end"));
//            setAd("customValues", new MapBean(true, "field", "value"));
//        }
        public Map<String, Object> getAd() {
            return ad;
        }

        public Object getAd(final String key) {
            return ad.get(key);
        }

        public long getId() {
            return id;
        }

        public long getMemberId() {
            return memberId;
        }

        public FormFile getPicture() {
            return picture;
        }

        public String getPictureCaption() {
            return pictureCaption;
        }

        public void setAd(final Map<String, Object> map) {
            ad = map;
        }

        public final void setAd(final String key, final Object value) {
            ad.put(key, value);
        }

        public void setId(final long memberId) {
            id = memberId;
        }

        public void setMemberId(final long memberId) {
            this.memberId = memberId;
        }

        public void setPicture(final FormFile picture) {
            this.picture = picture;
        }

        public void setPictureCaption(final String pictureCaption) {
            this.pictureCaption = pictureCaption;
        }
    }

    public static class EditAdsResponse extends GenericResponse {

        private int singleCurrency;
        private boolean maxAds;
        private final Map<String, Object> params = new HashMap<String, Object>();
        private boolean enablePermanent;
        private boolean enableExternalPublication;
        private String descriptionFormat;
        private boolean editable;
        private List<Currency> currencies;
        private String ad;

        public String getAd() {
            return ad;
        }

        public void setAd(String ad) {
            this.ad = ad;
        }

        public int getSingleCurrency() {
            return singleCurrency;
        }

        public void setSingleCurrency(int singleCurrency) {
            this.singleCurrency = singleCurrency;
        }

        public boolean isMaxAds() {
            return maxAds;
        }

        public void setMaxAds(boolean maxAds) {
            this.maxAds = maxAds;
        }

        public List<Currency> getCurrencies() {
            return currencies;
        }

        public void setCurrencies(List<Currency> currencies) {
            this.currencies = currencies;
        }

        public String getDescriptionFormat() {
            return descriptionFormat;
        }

        public void setDescriptionFormat(String descriptionFormat) {
            this.descriptionFormat = descriptionFormat;
        }

        public boolean isEditable() {
            return editable;
        }

        public void setEditable(boolean editable) {
            this.editable = editable;
        }

        public boolean isEnablePermanent() {
            return enablePermanent;
        }

        public void setEnablePermanent(boolean enablePermanent) {
            this.enablePermanent = enablePermanent;
        }

        public boolean isEnableExternalPublication() {
            return enableExternalPublication;
        }

        public void setEnableExternalPublication(boolean enableExternalPublication) {
            this.enableExternalPublication = enableExternalPublication;
        }

    }

    @RequestMapping(value = "member/editAdvertisements/{id}", method = RequestMethod.GET)
    @ResponseBody
    public EditAdsResponse handleDisplay(@PathVariable("id") long id) throws Exception {
        EditAdsResponse response = new EditAdsResponse();
        // final AdForm form = context.getForm();
        // final ActionForward forward = super.handleDisplay(context);
        final Ad ad = adService.load(id, FETCH);
        //ad = (Ad) response.setAd("ad");

        TextFormat descriptionFormat;
        if (ad.isTransient()) {
            final LocalSettings localSettings = settingsService.getLocalSettings();
            descriptionFormat = localSettings.getAdDescriptionFormat();
            response.setAd("descriptionFormat == TextFormat.RICH");
        } else {
            descriptionFormat = ad.isHtml() ? TextFormat.RICH : TextFormat.PLAIN;
        }
        response.setDescriptionFormat("descriptionFormat");
        final boolean editable;
        editable = true;
        final MemberGroup memberGroup = ad.getOwner().getMemberGroup();
        final List<Currency> currencies = currencyService.listByMemberGroup(memberGroup);
        response.setCurrencies(currencies);
        if (editable) {

            if (currencies.size() == 1) {
                // Set a single currency variable when there's only one option
                response.setSingleCurrency(0);
            } else if (currencies.size() > 1 && ad.getCurrency() == null) {
                // When there's multiple currencies, pre select the one of the default account
                final MemberAccountType defaultAccountType = accountTypeService.getDefault(memberGroup, AccountType.Relationships.CURRENCY);
                if (defaultAccountType != null) {
                    response.setAd(CoercionHelper.coerce(String.class, defaultAccountType.getCurrency()));
                }
            }
            final Member member = WebServiceContext.getMember();
            final MemberGroupSettings memberSettings = member.getMemberGroup().getMemberSettings();

            // Check if more ads can be added
            final int adCount = getNumberOfAds(member);
            final int maxAdsPerMember = memberSettings.getMaxAdsPerMember();
            final boolean maxAds = (adCount >= maxAdsPerMember);
            response.setMaxAds(maxAds);

            // Store the restrictions
            response.setEnablePermanent(memberSettings.isEnablePermanentAds());
            response.setEnableExternalPublication(memberSettings.getExternalAdPublication() == MemberGroupSettings.ExternalAdPublication.ALLOW_CHOICE);
            return response;
        } else {
            // Non-editable ads cannot change currency: use the first one
            //response.setCurrencies(currencies.isEmpty() ? null : currencies.get(0));
            if (currencies != null) {
                response.setCurrencies(currencies);
            } else {
                response.setCurrencies(currencies.subList(0, 0));
            }
            response.setStatus(0);
            return response;
            
        }
    }

    @RequestMapping(value = "member/editAdvertisements", method = RequestMethod.POST)
    @ResponseBody
    public GenericResponse handleSubmit(@RequestBody EditAdsRequest request) throws Exception {
        // final AdForm form = context.getForm();
        GenericResponse response = new GenericResponse();
        Ad ad = readAd(request.getAd());
        
        final boolean isInsert = ad.isTransient();

        // Save the advertisement
        ad = getAdService().save(ad);

        // Save the uploaded image
        final FormFile upload = request.getPicture();
        if (upload != null && upload.getFileSize() > 0) {
            try {
                StringBuffer newFileName = new StringBuffer(upload.getFileName());
                if (upload.getFileName().length() > 100) {
                    // File name cannot be greater than 100 characters
                    newFileName = new StringBuffer();
                    final String name = upload.getFileName();
                    final int extensionPos = name.lastIndexOf(".");
                    final String extension = name.substring(extensionPos);
                    newFileName.append(name.substring(0, 100 - extension.length()));
                    newFileName.append(extension);
                }
                //getImageService().save(ad, request.getPictureCaption(), ImageHelper.ImageType.getByContentType(upload.getContentType()), newFileName.toString(), upload.getInputStream());
            } finally {
                upload.destroy();
            }
        }

        response.setMessage(isInsert ? "ad.inserted" : "ad.modified");
        final Map<String, Object> params = new HashMap<String, Object>();
        params.put("id", ad.getId());
        params.put("memberId", ad.getOwner().getId());
        response.setStatus(0);
        return response;

    }

//    // @Override
    protected Ad resolveAd(EditAdsRequest form) throws Exception {
         
        if (form.getId() > 0L) {
            // Edit an ad - the superclass will read an existing ad
            return adService.load(form.getId(), FETCH);
        } else {
            // Insert a new ad
            Member member;
            final Element loggedElement = LoggedUser.element();
            if (LoggedUser.user().getId() > 0L && LoggedUser.user().getId() != loggedElement.getId()) {
                final Element element = elementService.load(LoggedUser.user().getId(), Element.Relationships.GROUP);
                if (!(element instanceof Member)) {
                    throw new ValidationException();
                }
                member = (Member) element;
            } else {
                if (LoggedUser.isAdministrator()) {
                    throw new ValidationException();
                } else if (LoggedUser.isMember()) {
                    member = LoggedUser.element();
                } else { // context.isOperator()
                    member = ((Operator) LoggedUser.element()).getMember();
                }
            }

            final MemberGroup group = member.getMemberGroup();
            final MemberGroupSettings settings = group.getMemberSettings();
            final TimePeriod defaultPublicationTime = (settings == null ? null : settings.getDefaultAdPublicationTime());

            // Set the default values
            final Ad ad = new Ad();
            ad.setOwner(member);
            ad.setTradeType(Ad.TradeType.OFFER);
            final Calendar today = Calendar.getInstance();
            ad.setPublicationPeriod(Period.between(today, defaultPublicationTime.add(today)));
            return ad;
        }
       

    }
//
//    //@Override
//    protected void validateForm(final ActionContext context) {
//        final Ad ad = readAd(context);
//        getAdService().validate(ad);
//    }
//

    public Ad readAd(Map<String,Object> values) {

        final Ad ad = getWriteDataBinder().readFromString(values);
        if (ad.isHtml()) {
            ad.setDescription(HtmlConverter.instance().valueOf("" + values.get("description")));
        } else {
            ad.setDescription(StringTrimmerConverter.instance().valueOf("" + values.get("description")));
        }
        if (ad.getOwner() == null) {
            if (LoggedUser.isMember()) {
                ad.setOwner((Member) LoggedUser.element());
            } else if (LoggedUser.isOperator()) {
                final Operator operator = (Operator) LoggedUser.element();
                ad.setOwner(operator.getMember());
            }
        }
        return ad;
    }
}
