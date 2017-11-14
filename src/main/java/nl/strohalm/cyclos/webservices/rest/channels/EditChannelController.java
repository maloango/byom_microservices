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
      private Long id;
      private String internalName;
      private String displayName;
      private String credentials;
      private String paymentRequestWebServiceUrl;
      private List<String>principalTypes;
      private String defaultPrincipalType="USER";

        public String getDefaultPrincipalType() {
            return defaultPrincipalType;
        }

        public void setDefaultPrincipalType(String defaultPrincipalType) {
            this.defaultPrincipalType = defaultPrincipalType;
        }
      

        public List<String> getPrincipalTypes() {
            return principalTypes;
        }

        public void setPrincipalTypes(List<String> principalTypes) {
            this.principalTypes = principalTypes;
        }
      

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

    @RequestMapping(value = "admin/editChannel", method = RequestMethod.POST)
    @ResponseBody
    public GenericResponse edit(@RequestBody EditChannelParameters params) {
        GenericResponse response = new GenericResponse();
            Channel channel = resolveChannel(params);
        final boolean isInsert = channel.isTransient();
        channel = channelService.save(channel);
        if (isInsert) {
            response.setMessage("channel.inserted");
        } else {
            response.setMessage("channel.modified");
        }
        response.setStatus(0);
        return response;
    }
    
    
     private Channel resolveChannel(EditChannelParameters params) {
       final Channel channel=new Channel();
       if(params.getId()!=null && params.getId()>0L){
           channel.setId(params.getId());
       }
       channel.setCredentials(Credentials.valueOf(params.getCredentials()));
       channel.setDisplayName(params.getDisplayName());
       channel.setInternalName(params.getInternalName());
       if(params.getPaymentRequestWebServiceUrl()!=null){
           channel.setPaymentRequestWebServiceUrl(params.getPaymentRequestWebServiceUrl());
       }
      
        if (params.getPrincipalTypes() != null) {
            try {
                final PrincipalType defaultPrincipalType = channelService.resolvePrincipalType(params.getDefaultPrincipalType());
                final Set<PrincipalType> principalTypes = new HashSet<PrincipalType>();
                if (params.getPrincipalTypes() != null) {
                    for (final String principalTypeString : params.getPrincipalTypes()) {
                        if (StringUtils.isNotEmpty(principalTypeString)) {
                            principalTypes.add(channelService.resolvePrincipalType(principalTypeString));
                        }
                    }
                }
                channel.setPrincipalTypes(principalTypes, defaultPrincipalType);
            } catch (final Exception e) {
                throw new ValidationException();
            }
        }
        return channel;
    }

    public static class EditChannelResponse extends GenericResponse {

        private boolean isBuiltin;
        private Set<Credentials> possibleCredentials;
        private Map<PrincipalType, String> possiblePrincipalTypes;
        private Credentials singleCredential;
        private boolean allowsPaymentRequest;
        private boolean canManage;

        public boolean isCanManage() {
            return canManage;
        }

        public void setCanManage(boolean canManage) {
            this.canManage = canManage;
        }
        

        public boolean isAllowsPaymentRequest() {
            return allowsPaymentRequest;
        }

        public void setAllowsPaymentRequest(boolean allowsPaymentRequest) {
            this.allowsPaymentRequest = allowsPaymentRequest;
        }
        

        public Credentials getSingleCredential() {
            return singleCredential;
        }

        public void setSingleCredential(Credentials singleCredential) {
            this.singleCredential = singleCredential;
        }
        

        public Map<PrincipalType, String> getPossiblePrincipalTypes() {
            return possiblePrincipalTypes;
        }

        public void setPossiblePrincipalTypes(Map<PrincipalType, String> possiblePrincipalTypes) {
            this.possiblePrincipalTypes = possiblePrincipalTypes;
        }
        

        public Set<Credentials> getPossibleCredentials() {
            return possibleCredentials;
        }

        public void setPossibleCredentials(Set<Credentials> possibleCredentials) {
            this.possibleCredentials = possibleCredentials;
        }
        

        public boolean isIsBuiltin() {
            return isBuiltin;
        }

        public void setIsBuiltin(boolean isBuiltin) {
            this.isBuiltin = isBuiltin;
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
     response.setPossibleCredentials(possibleCredentials);
     response.setIsBuiltin(isBuiltin);
     response.setPossiblePrincipalTypes(possiblePrincipalTypes);
      response.setSingleCredential(possibleCredentials.size() == 1 ? possibleCredentials.iterator().next() : null);
     response.setAllowsPaymentRequest(allowsPaymentRequest);
     response.setCanManage(permissionService.hasPermission(AdminSystemPermission.CHANNELS_MANAGE));
        //response.setChannel(channel);
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
