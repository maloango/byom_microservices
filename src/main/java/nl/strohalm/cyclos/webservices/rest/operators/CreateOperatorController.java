package nl.strohalm.cyclos.webservices.rest.operators;

import java.util.List;
import java.util.Map;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;

import org.apache.struts.action.ActionForward;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import nl.strohalm.cyclos.annotations.Inject;
import nl.strohalm.cyclos.controls.ActionContext;
import nl.strohalm.cyclos.controls.operators.CreateOperatorForm;
import nl.strohalm.cyclos.controls.restapi.BaseRestController;
import nl.strohalm.cyclos.entities.access.OperatorUser;
import nl.strohalm.cyclos.entities.customization.fields.OperatorCustomField;
import nl.strohalm.cyclos.entities.customization.fields.OperatorCustomFieldValue;
import nl.strohalm.cyclos.entities.groups.OperatorGroup;
import nl.strohalm.cyclos.entities.members.Element;
import nl.strohalm.cyclos.entities.members.Member;
import nl.strohalm.cyclos.entities.members.Operator;
import nl.strohalm.cyclos.services.customization.OperatorCustomFieldService;
import nl.strohalm.cyclos.services.elements.ElementService;
import nl.strohalm.cyclos.services.elements.WhenSaving;
import nl.strohalm.cyclos.services.groups.GroupService;
import nl.strohalm.cyclos.utils.ActionHelper;
import nl.strohalm.cyclos.utils.transaction.CurrentTransactionData;
import nl.strohalm.cyclos.utils.validation.ValidationException;
@Controller
public class CreateOperatorController extends BaseRestController{
	private OperatorCustomFieldService operatorCustomFieldService;
	private GroupService groupService;
	public final GroupService getGroupService() {
		return groupService;
	}


	public final void setGroupService(GroupService groupService) {
		this.groupService = groupService;
	}


	public final ElementService getElementService() {
		return elementService;
	}


	public final void setElementService(ElementService elementService) {
		this.elementService = elementService;
	}


	public final OperatorCustomFieldService getOperatorCustomFieldService() {
		return operatorCustomFieldService;
	}

	private ElementService elementService;

    @Inject
    public void setOperatorCustomFieldService(final OperatorCustomFieldService operatorCustomFieldService) {
        this.operatorCustomFieldService = operatorCustomFieldService;
    }
    public static class CreateOperatorRequestDTO{
    	private Map<String,Object>values;
    	 public Map<String, Object> getOperator() {
    	        return values;
    	    }

    	    public Object getOperator(final String key) {
    	        return values.get(key);
    	    }

    	    public void setOperator(final Map<String, Object> admin) {
    	        values = admin;
    	    }

    	    public void setOperator(final String key, final Object value) {
    	        values.put(key, value);
    	    }

			public ServletRequest getRequest() {
				// TODO Auto-generated method stub
				return null;
			}

			public boolean isOpenProfile() {
				// TODO Auto-generated method stub
				return false;
			}

    }
    
   public static class CreateOperatorResponseDTO{
	   String message;
	   private String isOpenProfile;

	public final String getMessage() {
		return message;
	}

	public final void setMessage(String message) {
		this.message = message;
	}

	public String getIsOpenProfile() {
		return isOpenProfile;
	}

	public void setIsOpenProfile(String isOpenProfile) {
		this.isOpenProfile = isOpenProfile;
	}

	public ActionForward findForward(String string) {
		// TODO Auto-generated method stub
		return null;
	}
   }
    

    @RequestMapping(value ="/member/createOperator",method = RequestMethod.POST)
    @ResponseBody
    protected CreateOperatorResponseDTO create(@RequestBody Element Element , final CreateOperatorRequestDTO form) {
        //final CreateOperatorForm form = context.getForm();
        //ensureMember(context, Element);
        final Operator operator = (Operator) elementService.register(Element, false, form.getRequest().getRemoteAddr());

        String successKey = "operator.created";

        boolean sendMessage = false;

        // Check if there's a mail exception
        if (CurrentTransactionData.hasMailError()) {
            successKey += ".mailError";
            sendMessage = true;
        }

        // Redirect to the correct profile
        String paramName;
        Object paramValue;
        ActionForward forward;
        CreateOperatorResponseDTO response = new CreateOperatorResponseDTO();
        if (form.isOpenProfile()) {
            paramName = "operatorId";
            paramValue = operator.getId();
            forward = response.findForward("profile");
        } else {
            sendMessage = true;
            paramName = "groupId";
            paramValue = operator.getGroup().getId();
            forward = response.findForward("new");
        }
        if (sendMessage) {
            response.setMessage(successKey);
        }
        return response ;

    }

    //@Override
    @SuppressWarnings("unchecked")
    protected Class<OperatorCustomField> getCustomFieldClass() {
        return OperatorCustomField.class;
    }

    @Override
    @SuppressWarnings("unchecked")
    protected Class<OperatorCustomFieldValue> getCustomFieldValueClass() {
        return OperatorCustomFieldValue.class;
    }

   // @Override
    protected Class<Operator> getElementClass() {
        return Operator.class;
    }

   // @Override
    @SuppressWarnings("unchecked")
    protected Class<OperatorGroup> getGroupClass() {
        return OperatorGroup.class;
    }

  //  @Override
    @SuppressWarnings("unchecked")
    protected Class<OperatorUser> getUserClass() {
        return OperatorUser.class;
    }

   // @Override
    protected void prepareForm(final ActionContext context) throws Exception {
        final HttpServletRequest request = context.getRequest();
        final CreateOperatorForm form = context.getForm();

        // Get the selected group
        final long groupId = form.getGroupId();
        if (groupId <= 0L) {
            throw new ValidationException();
        }
        final OperatorGroup group = groupService.load(groupId);

        // Get the custom fields
        final List<OperatorCustomField> customFields = operatorCustomFieldService.list((Member) context.getElement());
        request.setAttribute("customFields", customFields);
        request.setAttribute("group", group);
    }

    //@Override
    protected void runValidation(final ActionContext context, final Element element) {
        ensureMember(context, element);
        elementService.validate(element, WhenSaving.OPERATOR, true);
    }

    private void ensureMember(final ActionContext context, final Element element) {
        final Operator operator = (Operator) element;
        if (operator.getMember() == null) {
            operator.setMember(context.getMember());
        }
    }

}
