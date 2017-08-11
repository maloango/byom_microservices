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
import nl.strohalm.cyclos.controls.channels.EditChannelForm;
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
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class EditChannelController extends BaseRestController {
	private ChannelService channelService;
	private DataBinder<Channel> dataBinder;
	private PermissionService permissionService;
	public final PermissionService getPermissionService() {
		return permissionService;
	}

	public final void setPermissionService(PermissionService permissionService) {
		this.permissionService = permissionService;
	}

	public final SettingsService getSettingsService() {
		return settingsService;
	}

	public final void setSettingsService(SettingsService settingsService) {
		this.settingsService = settingsService;
	}

	public final ChannelService getChannelService() {
		return channelService;
	}

	private SettingsService settingsService;

	@Inject
	public void setChannelService(final ChannelService channelService) {
		this.channelService = channelService;
	}

    public Channel resolveChannel(long channelId) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
       
   
	public static class EditChannelRequestDto {
		private long channelId;
		private String[] principalTypes;
		private String defaultPrincipalType;
		protected Map<String, Object> values;
                
		public Map<String, Object> getValues() {
			return values;
		}

		public void setValues(final Map<String, Object> values) {
			this.values = values;
		}

		public Map<String, Object> getChannel() {
			return values;
		}

		public Object getChannel(final String key) {
			return values.get(key);
		}

		public void setChannel(final Map<String, Object> map) {
			values = map;
		}

		public void setChannel(final String key, final Object value) {
			values.put(key, value);
		}

		public long getChannelId() {
			return channelId;
		}

		public void setChannelId(long channelId) {
			this.channelId = channelId;
		}

		public String[] getPrincipalTypes() {
			return principalTypes;
		}

		public void setPrincipalTypes(String[] principalTypes) {
			this.principalTypes = principalTypes;
		}

		public String getDefaultPrincipalType() {
			return defaultPrincipalType;
		}

		public void setDefaultPrincipalType(String defaultPrincipalType) {
			this.defaultPrincipalType = defaultPrincipalType;
		}

	}

	public static class EditChannelResponseDto {
		public String message;
                private boolean isTransient;

        public boolean isIsTransient() {
            return isTransient;
        }

        public void setIsTransient(boolean isTransient) {
            this.isTransient = isTransient;
        }
		public String getMessage() {
			return message;
		}

		public void setMessage(String message) {
			this.message = message;
		}
                public EditChannelResponseDto(){
                }
	}

	@RequestMapping(value = "admin/editChannel/{channelId}", method = RequestMethod.GET)
	@ResponseBody
	protected EditChannelResponseDto handleSubmit(@PathVariable ("channelId")long channelId) throws Exception {
		
                Channel channel = resolveChannel(channelId);
                
                EditChannelResponseDto response =  new EditChannelResponseDto();
                try{
		final boolean isInsert = channel.isTransient();
		channel = channelService.save(channel);
		
		if (isInsert) {
			response.setMessage("channel.inserted");
		} else {
			response.setMessage("channel.modified");
		}
                response = new EditChannelResponseDto();}
                catch(Exception e){
                }
		return response;
	}

	protected void prepareForm(final ActionContext context) throws Exception {
		final HttpServletRequest request = context.getRequest();
		final EditChannelForm form = context.getForm();
		final long id = form.getChannelId();
		Channel channel;
		boolean isBuiltin;
		boolean allowsPaymentRequest;
		if (id <= 0) {
			channel = new Channel();
			isBuiltin = false;
			allowsPaymentRequest = true;
		} else {
			channel = channelService.load(id);
			final String internalName = channel.getInternalName();
			isBuiltin = channelService.isBuiltin(internalName);
			allowsPaymentRequest = channelService
					.allowsPaymentRequest(internalName);
		}

		final LocalSettings localSettings = settingsService.getLocalSettings();

		// Find the possible principal types
		final Map<PrincipalType, String> possiblePrincipalTypes = new LinkedHashMap<PrincipalType, String>();
		final List<MemberCustomField> customFields = channelService
				.possibleCustomFieldsAsPrincipal();
		for (final Principal principal : Principal.values()) {
			if (principal == Principal.CUSTOM_FIELD) {
				for (final MemberCustomField customField : customFields) {
					possiblePrincipalTypes.put(new PrincipalType(customField),
							customField.getName());
				}
			} else {
				if (principal == Principal.EMAIL
						&& !localSettings.isEmailUnique()) {
					// Skip e-mail when it is not unique
					continue;
				}
				final String label = context.message(principal.getKey());
				possiblePrincipalTypes.put(new PrincipalType(principal), label);
			}
		}
		final Set<Credentials> possibleCredentials = channelService
				.getPossibleCredentials(channel);
		getDataBinder().writeAsString(form.getChannel(), channel);
		request.setAttribute("channel", channel);
		request.setAttribute("isBuiltin", isBuiltin);
		request.setAttribute("possiblePrincipalTypes", possiblePrincipalTypes);
		request.setAttribute("possibleCredentials", possibleCredentials);
		request.setAttribute("singleCredential",
				possibleCredentials.size() == 1 ? possibleCredentials
						.iterator().next() : null);
		request.setAttribute("allowsPaymentRequest", allowsPaymentRequest);
		request.setAttribute("canManage", permissionService
				.hasPermission(AdminSystemPermission.CHANNELS_MANAGE));
	}

	private DataBinder<Channel> getDataBinder() {
		if (dataBinder == null) {
			final BeanBinder<Channel> binder = BeanBinder
					.instance(Channel.class);
			binder.registerBinder(
					"id",
					PropertyBinder.instance(Long.class, "id",
							IdConverter.instance()));
			binder.registerBinder("internalName",
					PropertyBinder.instance(String.class, "internalName"));
			binder.registerBinder("displayName",
					PropertyBinder.instance(String.class, "displayName"));
			binder.registerBinder("credentials",
					PropertyBinder.instance(Credentials.class, "credentials"));
			binder.registerBinder("paymentRequestWebServiceUrl", PropertyBinder
					.instance(String.class, "paymentRequestWebServiceUrl"));
			dataBinder = binder;
		}
		return dataBinder;
	}

	private Channel resolveChannel(final EditChannelRequestDto form2) {
		// final EditChannelForm form = form2.getForm();
		final Channel channel = getDataBinder().readFromString(
				form2.getChannel());
		if (form2.getPrincipalTypes() != null) {
			try {
				final PrincipalType defaultPrincipalType = channelService
						.resolvePrincipalType(form2.getDefaultPrincipalType());
				final Set<PrincipalType> principalTypes = new HashSet<PrincipalType>();
				if (form2.getPrincipalTypes() != null) {
					for (final String principalTypeString : form2
							.getPrincipalTypes()) {
						if (StringUtils.isNotEmpty(principalTypeString)) {
							principalTypes.add(channelService
									.resolvePrincipalType(principalTypeString));
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
}
