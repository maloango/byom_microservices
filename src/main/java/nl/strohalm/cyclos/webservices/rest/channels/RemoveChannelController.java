package nl.strohalm.cyclos.webservices.rest.channels;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import nl.strohalm.cyclos.annotations.Inject;
import nl.strohalm.cyclos.services.access.ChannelService;
import nl.strohalm.cyclos.utils.validation.ValidationException;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class RemoveChannelController extends BaseRestController {
	private ChannelService channelService;

	public final ChannelService getChannelService() {
		return channelService;
	}

	@Inject
	public void setChannelService(final ChannelService channelService) {
		this.channelService = channelService;
	}

	public static class RemoveChannelRequestDto {
		private long channelId;

		public long getChannelId() {
			return channelId;
		}

		public void setChannelId(final long channelId) {
			this.channelId = channelId;
		}
	}

	public static class RemoveChannelResponseDto {
		public String message;

		public String getMessage() {
			return message;
		}

		public void setMessage(String message) {
			this.message = message;
		}
                public RemoveChannelResponseDto(){ // default constructor
                }
	}

	@RequestMapping(value = "admin/removeChannel/{channelId}", method = RequestMethod.GET)
	@ResponseBody
	protected RemoveChannelResponseDto executeAction(@PathVariable ("channelId") long channelId) throws Exception {
			
		RemoveChannelResponseDto response = new RemoveChannelResponseDto();
                try{
		final long id = channelId;
		if (id <= 0L) {
			
			channelService.remove(id);
			response.setMessage("channel.removed");
		
			response.setMessage("channel.errorRemoving");
                        response = new RemoveChannelResponseDto();
		}}
                catch(Exception e){
                    e.printStackTrace();
                }
		return response;
	}
}
