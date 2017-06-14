package nl.strohalm.cyclos.webservices.rest.operators;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;

import nl.strohalm.cyclos.annotations.Inject;
import nl.strohalm.cyclos.entities.Relationship;
import nl.strohalm.cyclos.entities.access.OperatorUser;
import nl.strohalm.cyclos.entities.access.User;
import nl.strohalm.cyclos.entities.customization.fields.CustomFieldValue;
import nl.strohalm.cyclos.entities.customization.fields.OperatorCustomField;
import nl.strohalm.cyclos.entities.customization.fields.OperatorCustomFieldValue;
import nl.strohalm.cyclos.entities.groups.Group;
import nl.strohalm.cyclos.entities.groups.OperatorGroup;
import nl.strohalm.cyclos.entities.members.Element;
import nl.strohalm.cyclos.entities.members.Operator;
import nl.strohalm.cyclos.services.access.AccessService;
import nl.strohalm.cyclos.services.customization.OperatorCustomFieldService;
import nl.strohalm.cyclos.services.elements.ElementService;
import nl.strohalm.cyclos.utils.CustomFieldHelper;
import nl.strohalm.cyclos.utils.RelationshipHelper;
import nl.strohalm.cyclos.utils.binding.MapBean;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;
@Controller
public class OperatorProfileController extends BaseRestController{
	
	private static final Relationship[] FETCH = { RelationshipHelper.nested(User.Relationships.ELEMENT, Element.Relationships.GROUP), RelationshipHelper.nested(User.Relationships.ELEMENT, Element.Relationships.USER), RelationshipHelper.nested(User.Relationships.ELEMENT, Operator.Relationships.CUSTOM_VALUES), RelationshipHelper.nested(User.Relationships.ELEMENT, Operator.Relationships.MEMBER) };

    private OperatorCustomFieldService  operatorCustomFieldService;

    private CustomFieldHelper           customFieldHelper;
    private ElementService 				elementService;
    private AccessService 				accessService;

    public final AccessService getAccessService() {
		return accessService;
	}

	public final void setAccessService(AccessService accessService) {
		this.accessService = accessService;
	}

	public final ElementService getElementService() {
		return elementService;
	}

	public final void setElementService(ElementService elementService) {
		this.elementService = elementService;
	}

	public static final Relationship[] getFetch() {
		return FETCH;
	}

	public final OperatorCustomFieldService getOperatorCustomFieldService() {
		return operatorCustomFieldService;
	}

	public final CustomFieldHelper getCustomFieldHelper() {
		return customFieldHelper;
	}

	@Inject
    public void setCustomFieldHelper(final CustomFieldHelper customFieldHelper) {
        this.customFieldHelper = customFieldHelper;
    }

    @Inject
    public void setOperatorCustomFieldService(final OperatorCustomFieldService operatorCustomFieldService) {
        this.operatorCustomFieldService = operatorCustomFieldService;
    }

   // @Override
    @SuppressWarnings("unchecked")
    protected <CFV extends CustomFieldValue> Class<CFV> getCustomFieldValueClass() {
        return (Class<CFV>) OperatorCustomFieldValue.class;
    }

    //@Override
    protected Class<Operator> getElementClass() {
        return Operator.class;
    }

   // @Override
    @SuppressWarnings("unchecked")
    protected <G extends Group> Class<G> getGroupClass() {
        return (Class<G>) OperatorGroup.class;
    }

    //@Override
    @SuppressWarnings("unchecked")
    protected <U extends User> Class<U> getUserClass() {
        return (Class<U>) OperatorUser.class;
    }
    public static class OperatorProfileRequestDTO{
    	private long              operatorId;
    	private Map<String,Object>values;

        public void OperatorProfileForm() {
            setOperator("user", new MapBean("id", "username"));
            setOperator("group", new MapBean("id", "name"));
            setOperator("customValues", new MapBean(true, "field", "value"));
        }

        public Map<String, Object> getOperator() {
            return values;
        }

        public Object getOperator(final String key) {
            return values.get(key);
        }

        public long getOperatorId() {
            return operatorId;
        }

        public void setOperator(final Map<String, Object> map) {
            values = map;
        }

        public void setOperator(final String key, final Object value) {
            values.put(key, value);
        }

        public void setOperatorId(final long operatorId) {
            this.operatorId = operatorId;
        }

		public boolean isMember() {
			// TODO Auto-generated method stub
			return false;
		}
    }
    public static class OperatorProfileResponseDTO{
    	List<OperatorCustomField> customFields;

		public OperatorProfileResponseDTO(List<OperatorCustomField> customFields) {
			super();
			this.customFields = customFields;
		}
    }
/*
    @RequestMapping(value = "/member/operatorProfile", method = RequestMethod.POST)
    @ResponseBody
    protected OperatorProfileResponseDTO handleDisplay(@RequestBody OperatorProfileRequestDTO form) throws Exception {
        boolean myProfile = false;

        // Load the user
        final Long operatorId = form.getOperatorId();
        OperatorUser operatorUser = null;
        Operator operator = null;
        final Element loggedElement = form.getElement();

        if (form.isMember()) {
            // Member viewing an operator's profile
            if (operatorId <= 0) {
                throw new ValidationException();
            }
            final User loaded = elementService.loadUser(operatorId, FETCH);
            if (!(loaded instanceof OperatorUser)) {
                throw new ValidationException();
            }

            operatorUser = (OperatorUser) loaded;
            operator = operatorUser.getOperator();
            if (!operator.getMember().equals(loggedElement)) {
                throw new ValidationException();
            }
            try {
                request.setAttribute("isLoggedIn", accessService.isLoggedIn(operatorUser));
            } catch (final NotConnectedException e) {
                // OK - the user is not online
            }

            request.setAttribute("disabledLogin", accessService.isLoginBlocked(operatorUser));

        } else if (operatorId <= 0L || operatorId.equals(context.getElement().getId())) { // context.isOperator()
            // The logged user (operator) is viewing it's own profile
            operatorUser = elementService.loadUser(context.getUser().getId(), FETCH);
            operator = operatorUser.getOperator();
            myProfile = true;
        } else {
            throw new ValidationException();
        }

        // Write the operator to the form
        getReadDataBinder(context).writeAsString(form.getOperator(), operator);

        // Retrieve the custom fields
        final List<OperatorCustomField> customFields = operatorCustomFieldService.list(operator.getMember());

        // This map will store, for each field, if it is editable or not
        final Map<OperatorCustomField, Boolean> editableFields = new HashMap<OperatorCustomField, Boolean>();
        for (final Iterator<OperatorCustomField> it = customFields.iterator(); it.hasNext();) {
            final OperatorCustomField field = it.next();
            final Visibility visibility = field.getVisibility();
            // Check if the field is visible
            if (myProfile && visibility == Visibility.NOT_VISIBLE) {
                it.remove();
            } else {
                // Check if the field is editable
                editableFields.put(field, (!myProfile || visibility == Visibility.EDITABLE));
            }
        }

        // Store the request attributes
        request.setAttribute("operator", operator);
        request.setAttribute("removed", operator.getGroup().getStatus() == Group.Status.REMOVED);
        request.setAttribute("customFields", customFieldHelper.buildEntries(customFields, operator.getCustomValues()));
        request.setAttribute("myProfile", myProfile);
        request.setAttribute("editableFields", editableFields);

        return context.getInputForward();
    }

   // @Override
    protected ActionForward handleSubmit(final ActionContext context) throws Exception {
        final OperatorProfileForm form = context.getForm();
        Operator operator = getWriteDataBinder(context).readFromString(form.getOperator());
        operator = elementService.changeProfile(operator);
        context.sendMessage("profile.modified");
        return ActionHelper.redirectWithParam(context.getRequest(), super.handleSubmit(context), "operatorId", operator.getId());
    }

    //@Override
    protected DataBinder<Operator> initDataBinderForRead(final ActionContext context) {
        final BeanBinder<Operator> dataBinder = (BeanBinder<Operator>) super.initDataBinderForRead(context);
        return dataBinder;
    }

   // @Override
    @SuppressWarnings({ "unchecked", "rawtypes" })
    protected DataBinder<Operator> initDataBinderForWrite(final ActionContext context) {
        final BeanBinder<Operator> dataBinder = (BeanBinder<Operator>) super.initDataBinderForWrite(context);

        final BeanBinder<OperatorCustomFieldValue> customValueBinder = BeanBinder.instance(OperatorCustomFieldValue.class);
        customValueBinder.registerBinder("field", PropertyBinder.instance(OperatorCustomField.class, "field", ReferenceConverter.instance(OperatorCustomField.class)));
        customValueBinder.registerBinder("value", PropertyBinder.instance(String.class, "value", HtmlConverter.instance()));

        final BeanCollectionBinder collectionBinder = (BeanCollectionBinder) dataBinder.getMappings().get("customValues");
        collectionBinder.setElementBinder(customValueBinder);

        return dataBinder;
    }

   // @Override
    protected void validateForm(final ActionContext context) {
        final OperatorProfileForm form = context.getForm();
        final Operator operator = getWriteDataBinder(context).readFromString(form.getOperator());
        elementService.validate(operator, WhenSaving.PROFILE, false);
    }
*/
}
