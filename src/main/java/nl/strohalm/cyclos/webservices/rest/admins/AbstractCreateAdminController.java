package nl.strohalm.cyclos.webservices.rest.admins;

import java.util.HashMap;
import java.util.Map;
import nl.strohalm.cyclos.entities.access.AdminUser;
import nl.strohalm.cyclos.entities.access.MemberUser;
import nl.strohalm.cyclos.entities.access.User;
import nl.strohalm.cyclos.entities.customization.fields.AdminCustomField;
import nl.strohalm.cyclos.entities.customization.fields.AdminCustomFieldValue;
import nl.strohalm.cyclos.entities.customization.fields.CustomField;
import nl.strohalm.cyclos.entities.customization.fields.CustomFieldValue;
import nl.strohalm.cyclos.entities.customization.fields.MemberCustomFieldValue;
import nl.strohalm.cyclos.entities.groups.AdminGroup;
import nl.strohalm.cyclos.entities.groups.Group;
import nl.strohalm.cyclos.entities.members.Administrator;
import nl.strohalm.cyclos.entities.members.Element;
import nl.strohalm.cyclos.entities.members.Member;
import nl.strohalm.cyclos.entities.settings.AccessSettings;
import nl.strohalm.cyclos.entities.settings.LocalSettings;
import nl.strohalm.cyclos.utils.access.LoggedUser;
import nl.strohalm.cyclos.utils.binding.BeanBinder;
import nl.strohalm.cyclos.utils.binding.BeanCollectionBinder;
import nl.strohalm.cyclos.utils.binding.DataBinder;
import nl.strohalm.cyclos.utils.binding.PropertyBinder;
import nl.strohalm.cyclos.utils.conversion.HtmlConverter;
import nl.strohalm.cyclos.utils.conversion.ReferenceConverter;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;



public abstract class AbstractCreateAdminController<E extends Element> extends BaseRestController {

    // need to implement later due to already prepare form is available 
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static <E extends Element> DataBinder<E> getDataBinder(final LocalSettings localSettings, final AccessSettings accessSettings, final Class<E> elementClass, final Class userClass, final Class groupClass, final Class customField, final Class customFieldValue) {
        final BeanBinder<? extends CustomFieldValue> customValueBinder = BeanBinder.instance(customFieldValue);
        customValueBinder.registerBinder("field", PropertyBinder.instance(customField, "field", ReferenceConverter.instance(customField)));
        customValueBinder.registerBinder("value", PropertyBinder.instance(String.class, "value", HtmlConverter.instance()));
        if (MemberCustomFieldValue.class.isAssignableFrom(customFieldValue)) {
            customValueBinder.registerBinder("hidden", PropertyBinder.instance(Boolean.TYPE, "hidden"));
        }

        final BeanBinder<E> elementBinder = BeanBinder.instance(elementClass);
        elementBinder.registerBinder("name", PropertyBinder.instance(String.class, "name"));
        elementBinder.registerBinder("email", PropertyBinder.instance(String.class, "email"));
        if (Member.class.isAssignableFrom(elementClass)) {
            elementBinder.registerBinder("hideEmail", PropertyBinder.instance(Boolean.TYPE, "hideEmail"));
        }
        elementBinder.registerBinder("group", PropertyBinder.instance(groupClass, "group", ReferenceConverter.instance(groupClass)));
        elementBinder.registerBinder("customValues", BeanCollectionBinder.instance(customValueBinder, "customValues"));

        final BeanBinder<? extends User> userBinder = BeanBinder.instance(userClass, "user");
        if (!(Member.class.isAssignableFrom(elementClass) && accessSettings.isUsernameGenerated())) {
            userBinder.registerBinder("username", PropertyBinder.instance(String.class, "username"));
        }
        userBinder.registerBinder("password", PropertyBinder.instance(String.class, "password"));
        elementBinder.registerBinder("user", userBinder);

        return elementBinder;
    }

    protected DataBinder<? extends Element> dataBinder;

    public DataBinder<? extends Element> getDataBinder() {
        if (dataBinder == null) {
            dataBinder = getBaseBinder();
        }
        return dataBinder;
    }

    protected DataBinder<? extends Element> getBaseBinder() {
        final LocalSettings localSettings = settingsService.getLocalSettings();
        final AccessSettings accessSettings = settingsService.getAccessSettings();
        return getDataBinder(localSettings, accessSettings, getElementClass(), getUserClass(), getGroupClass(), getCustomFieldClass(), getCustomFieldValueClass());
    }

    protected abstract <CF extends CustomField> Class<CF> getCustomFieldClass();

    protected abstract <CFV extends CustomFieldValue> Class<CFV> getCustomFieldValueClass();

    protected abstract Class<E> getElementClass();

    protected abstract <G extends Group> Class<G> getGroupClass();

    protected abstract <U extends User> Class<U> getUserClass();

}
