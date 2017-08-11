package nl.strohalm.cyclos.webservices.rest.operators;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import nl.strohalm.cyclos.controls.ActionContext;
import nl.strohalm.cyclos.controls.operators.ChangeOperatorGroupForm;
import nl.strohalm.cyclos.entities.groups.Group;
import nl.strohalm.cyclos.entities.members.Operator;
import nl.strohalm.cyclos.services.groups.GroupService;
import nl.strohalm.cyclos.utils.ResponseHelper;
import nl.strohalm.cyclos.utils.transaction.CurrentTransactionData;
import nl.strohalm.cyclos.utils.validation.ValidationException;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;
@Controller
public class ChangeOperatorGroupController extends BaseRestController{
	private GroupService groupService;
	private ResponseHelper responseHelper;
	
	public final GroupService getGroupService() {
		return groupService;
	}

	public final void setGroupService(GroupService groupService) {
		this.groupService = groupService;
	}

	private static class ChangeOperatorGroupRequestDTO{
	}
	@SuppressWarnings("unused")
	private static class ChangeOperatorGroupResponseDTO{
		String message;

		public final String getMessage() {
			return message;
		}

		public final void setMessage(String message) {
			this.message = message;
		}
		
	}
	 @RequestMapping(value = "member/changeOperatorGroup",method =RequestMethod.POST)
	 @ResponseBody
	    protected ChangeOperatorGroupResponseDTO handleSubmit(@RequestBody ChangeOperatorGroupRequestDTO form) throws Exception {
	        ChangeOperatorGroupResponseDTO response = null;
                try{
	        String key = "changeGroup.operator.changed";
	        if (CurrentTransactionData.hasMailError()) {
	            key += ".mailError";
	        }
	         response = new ChangeOperatorGroupResponseDTO();
	        response.setMessage(key);}
                catch(Exception e){
                    e.printStackTrace();
                }
	        return response;
	    }

	   // @Override
	    protected ChangeOperatorGroupResponseDTO handleValidation(final ActionContext context) {
	        try {
	            validateForm(context);
	            final ChangeOperatorGroupForm form = context.getForm();
	            final Group newGroup = groupService.load(form.getNewGroupId());
	            if (newGroup.getStatus() == Group.Status.REMOVED) {
	                final Map<String, Object> fields = new HashMap<String, Object>();
	                fields.put("confirmationMessage", context.message("changeGroup.confirmRemove", newGroup.getName()));
	                responseHelper.writeStatus(context.getResponse(), ResponseHelper.Status.SUCCESS, fields);
	            } else {
	                responseHelper.writeValidationSuccess(context.getResponse());
	            }
	        } catch (final ValidationException e) {
	            responseHelper.writeValidationErrors(context.getResponse(), e);
	        }
	        return null;
	    }

	   private void validateForm(ActionContext context) {
		// TODO Auto-generated method stub
		
	}

	// @Override
	    protected void prepareForm(final ActionContext context) throws Exception {
	        //super.prepareForm(context);
	        final HttpServletRequest request = context.getRequest();
	        final Operator operator = (Operator) request.getAttribute("element");
	        request.setAttribute("operator", operator);
	    }


}
