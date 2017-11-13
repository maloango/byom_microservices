package nl.strohalm.cyclos.webservices.rest.channels;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import nl.strohalm.cyclos.access.AdminSystemPermission;
import nl.strohalm.cyclos.annotations.Inject;
import nl.strohalm.cyclos.controls.ActionContext;
//import nl.strohalm.cyclos.controls.channels.EditChannelForm;
import nl.strohalm.cyclos.entities.access.Channel;
import nl.strohalm.cyclos.entities.access.Channel.Credentials;
import nl.strohalm.cyclos.entities.access.Channel.Principal;
import nl.strohalm.cyclos.entities.access.PrincipalType;
import nl.strohalm.cyclos.entities.customization.fields.MemberCustomField;
import nl.strohalm.cyclos.entities.settings.LocalSettings;
import nl.strohalm.cyclos.services.access.ChannelService;
import nl.strohalm.cyclos.services.permissions.PermissionService;
import nl.strohalm.cyclos.services.settings.SettingsService;
import nl.strohalm.cyclos.utils.binding.BeanBinder;
import nl.strohalm.cyclos.utils.binding.DataBinder;
import nl.strohalm.cyclos.utils.binding.PropertyBinder;
import nl.strohalm.cyclos.utils.conversion.IdConverter;
import nl.strohalm.cyclos.utils.validation.ValidationException;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;
import nl.strohalm.cyclos.webservices.rest.GenericResponse;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class EditChannelController extends BaseRestController {

    public static class EditChannelParameters {

    }

    @RequestMapping(value = "admin/editChannel", method = RequestMethod.POST)
    @ResponseBody
    public GenericResponse edit() {
        GenericResponse response = new GenericResponse();

        response.setStatus(0);
        return response;
    }

    public static class EditChannelResponse extends GenericResponse {

        private Channel channel;

        public Channel getChannel() {
            return channel;
        }

        public void setChannel(Channel channel) {
            this.channel = channel;
        }
    }

    @RequestMapping(value = "admin/editChannel", method = RequestMethod.GET)
    @ResponseBody
    public EditChannelResponse prepareForm() {
        EditChannelResponse response = new EditChannelResponse();
        Channel channel;
        boolean isBuiltin;
        boolean allowsPaymentRequest;
        channel = new Channel();
        isBuiltin = false;
        allowsPaymentRequest = true;

        final LocalSettings localSettings = settingsService.getLocalSettings();

        // Find the possible principal types
        final Map<PrincipalType, String> possiblePrincipalTypes = new LinkedHashMap<PrincipalType, String>();
        final List<MemberCustomField> customFields = channelService.possibleCustomFieldsAsPrincipal();
        for (final Principal principal : Principal.values()) {
            if (principal == Principal.CUSTOM_FIELD) {
                for (final MemberCustomField customField : customFields) {
                    possiblePrincipalTypes.put(new PrincipalType(customField), customField.getName());
                }
            } else {
                if (principal == Principal.EMAIL && !localSettings.isEmailUnique()) {
                    // Skip e-mail when it is not unique
                    continue;
                }

                possiblePrincipalTypes.put(new PrincipalType(principal), principal.getKey());
            }
        }
        final Set<Credentials> possibleCredentials = channelService.getPossibleCredentials(channel);

        response.setChannel(channel);
//        request.setAttribute("isBuiltin", isBuiltin);
//        request.setAttribute("possiblePrincipalTypes", possiblePrincipalTypes);
//        request.setAttribute("possibleCredentials", possibleCredentials);
//        request.setAttribute("singleCredential", possibleCredentials.size() == 1 ? possibleCredentials.iterator().next() : null);
//        request.setAttribute("allowsPaymentRequest", allowsPaymentRequest);
//        request.setAttribute("canManage", permissionService.hasPermission(AdminSystemPermission.CHANNELS_MANAGE));
        response.setStatus(0);
        return response;
    }

}
