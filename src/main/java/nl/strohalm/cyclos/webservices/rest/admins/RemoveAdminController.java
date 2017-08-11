package nl.strohalm.cyclos.webservices.rest.admins;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import nl.strohalm.cyclos.services.elements.ElementService;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;
import org.springframework.web.bind.annotation.PathVariable;

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
                public RemoveAdminResponseDTO(){
                }
               
	}

	@RequestMapping(value = "admin/removeAdmin/{id}", method = RequestMethod.GET)
	@ResponseBody
	protected RemoveAdminResponseDTO doRemove(@PathVariable ("id") long id) {
		RemoveAdminResponseDTO response = new RemoveAdminResponseDTO();
		try {
                        
			elementService.remove(id);
			response.setMessage("changeGroup.admin.permanentlyRemovedMessage");
                        
			return response;
                    
		} catch (final Exception e) {
                       // Can't remove active admin
			response.setMessage("changeGroup.error.remove.activeAdmin");
                       // e.printStackTrace();
                        
			return response;
		}
	}

}
