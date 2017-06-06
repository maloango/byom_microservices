package nl.strohalm.cyclos.webservices.rest.channels;

import org.apache.struts.action.ActionForward;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import nl.strohalm.cyclos.annotations.Inject;
import nl.strohalm.cyclos.controls.ActionContext;
import nl.strohalm.cyclos.controls.channels.RemoveChannelForm;
import nl.strohalm.cyclos.services.access.ChannelService;
import nl.strohalm.cyclos.utils.validation.ValidationException;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;

@Controller
public class RemoveChannelController extends BaseRestController {
	private ChannelService channelService;

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
	}

	@RequestMapping(value = "/admin/removeChannel", method = RequestMethod.DELETE)
	@ResponseBody
	protected RemoveChannelResponseDto executeAction(
			@RequestBody RemoveChannelRequestDto form) throws Exception {
		// final RemoveChannelForm form = context.getForm();
		final long id = form.getChannelId();
		if (id <= 0L) {
			throw new ValidationException();
		}
		RemoveChannelResponseDto response = new RemoveChannelResponseDto();
		try {
			channelService.remove(id);
			response.setMessage("channel.removed");
		} catch (final Exception e) {
			response.setMessage("channel.errorRemoving");
		}
		return response;
	}
}
