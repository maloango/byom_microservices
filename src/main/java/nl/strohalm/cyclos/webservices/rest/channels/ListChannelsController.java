package nl.strohalm.cyclos.webservices.rest.channels;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import nl.strohalm.cyclos.access.AdminSystemPermission;
import nl.strohalm.cyclos.annotations.Inject;
import nl.strohalm.cyclos.entities.access.Channel;
import nl.strohalm.cyclos.services.access.ChannelService;
import nl.strohalm.cyclos.services.permissions.PermissionService;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class ListChannelsController extends BaseRestController {
	private ChannelService channelService;
	public final PermissionService getPermissionService() {
		return permissionService;
	}

	public final void setPermissionService(PermissionService permissionService) {
		this.permissionService = permissionService;
	}

	public final ChannelService getChannelService() {
		return channelService;
	}

	private PermissionService permissionService;

	@Inject
	public void setChannelService(final ChannelService channelService) {
		this.channelService = channelService;
	}

	public static class ListChannelsRequestDto {
            private long channelId;

        public long getChannelId() {
            return channelId;
        }

        public void setChannelId(long channelId) {
            this.channelId = channelId;
        }
	}

	public static class ListChannelsResponseDto {
		private List<Channel> channels;
		private List<Channel> builtin;
		private Boolean message;

		public ListChannelsResponseDto(List<Channel> channels2,
				List<Channel> builtin2, Boolean message) {
			super();

		}

		public List<Channel> getChannels() {
			return channels;
		}

		public void setChannels(List<Channel> channels) {
			this.channels = channels;
		}

		public List<Channel> getBuiltin() {
			return builtin;
		}

		public void setBuiltin(List<Channel> builtin) {
			this.builtin = builtin;
		}

		public Boolean getMessage() {
			return message;
		}

		public void setMessage(Boolean message) {
			this.message = message;
		}
                public ListChannelsResponseDto(){
                }

	}

	@RequestMapping(value = "admin/listChannels/{channelId}", method = RequestMethod.GET)
	@ResponseBody
	protected ListChannelsResponseDto executeAction(@PathVariable ("channelId") long channelId) throws Exception {
			
                        ListChannelsResponseDto response = new ListChannelsResponseDto();
		try{
		final List<Channel> channels = channelService.list();
		final List<Channel> builtin = channelService.listBuiltin();
		Boolean canManage = permissionService
				.hasPermission(AdminSystemPermission.CHANNELS_MANAGE);
                 response = new ListChannelsResponseDto(
				channels, builtin, canManage);}
                catch(Exception e){
                    e.printStackTrace();
                }
		return response;
	}
}
