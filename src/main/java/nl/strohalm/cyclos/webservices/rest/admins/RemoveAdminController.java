package nl.strohalm.cyclos.webservices.rest.admins;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import nl.strohalm.cyclos.controls.restapi.BaseRestController;
import nl.strohalm.cyclos.services.elements.ElementService;

@Controller
public class RemoveAdminController extends BaseRestController {

	private ElementService elementService;

	public final ElementService getElementService() {
		return elementService;
	}

	public final void setElementService(ElementService elementService) {
		this.elementService = elementService;
	}

	public static class RemoveAdminRequestDTO {
		private long id;

		public final long getId() {
			return id;
		}

		public final void setId(long id) {
			this.id = id;
		}

	}

	public static class RemoveAdminResponseDTO {
		String message;

		public final String getMessage() {
			return message;
		}

		public final void setMessage(String message) {
			this.message = message;
		}
	}

	@RequestMapping(value = "/admin/removeAdmin", method = RequestMethod.DELETE)
	@ResponseBody
	protected RemoveAdminResponseDTO doRemove(@RequestBody RemoveAdminRequestDTO form, final long id) {
		RemoveAdminResponseDTO response = new RemoveAdminResponseDTO();
		try {
			elementService.remove(id);
			response.setMessage("changeGroup.admin.permanentlyRemovedMessage");
			return response;
		} catch (final Exception e) {
			response.setMessage("changeGroup.error.remove.activeAdmin");
			return response;
		}
	}

}