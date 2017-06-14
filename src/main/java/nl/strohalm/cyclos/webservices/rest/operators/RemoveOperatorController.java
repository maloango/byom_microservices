package nl.strohalm.cyclos.webservices.rest.operators;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import nl.strohalm.cyclos.entities.members.Element.Nature;
import nl.strohalm.cyclos.services.elements.ElementService;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;
@Controller
public class RemoveOperatorController extends BaseRestController{
	private ElementService elementService;
	
	
	
	public final ElementService getElementService() {
		return elementService;
	}

	public final void setElementService(ElementService elementService) {
		this.elementService = elementService;
	}
	public static class RemoveOperatorRequestDTO{
		private long id;

		public final long getId() {
			return id;
		}

		public final void setId(long id) {
			this.id = id;
		}
	}
	
	public static class RemoveOperatorResponseDTo{
		String messsage;

		public final String getMesssage() {
			return messsage;
		}

		public final void setMesssage(String messsage) {
			this.messsage = messsage;
		}
	}

	@RequestMapping(value = "/member/changeOperatorGroup", method = RequestMethod.DELETE )
	@ResponseBody
    protected RemoveOperatorResponseDTo doRemove(@RequestBody RemoveOperatorRequestDTO   form, Long id) {
        try {
            elementService.remove(id);
            //RemoveOperatorResponseDTo response = new RemoveOperatorResponseDTo();
            String message = null;
            message = "changeGroup.operator.permanentlyRemovedMessage";
            message = "changeGroup.error.remove.activeOperator";
           // response.setMesssage("changeGroup.operator.permanentlyRemovedMessage");
           // return response.getSuccessForward();
        } catch (final Exception e) {
        	RemoveOperatorResponseDTo response = new RemoveOperatorResponseDTo();
        	return response;
        }
		return null;
    }

   // @Override
    protected Nature expectedNature() {
        return Nature.OPERATOR;
    }

}
