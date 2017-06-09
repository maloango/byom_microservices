package nl.strohalm.cyclos.webservices.rest.members.pending;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import nl.strohalm.cyclos.access.AdminMemberPermission;
import nl.strohalm.cyclos.access.BrokerPermission;
import nl.strohalm.cyclos.annotations.Inject;
import nl.strohalm.cyclos.controls.ActionContext;
import nl.strohalm.cyclos.controls.members.pending.PendingMemberProfileForm;
import nl.strohalm.cyclos.controls.restapi.BaseRestController;
import nl.strohalm.cyclos.entities.customization.fields.CustomField;
import nl.strohalm.cyclos.entities.customization.fields.MemberCustomField;
import nl.strohalm.cyclos.entities.customization.fields.MemberCustomFieldValue;
import nl.strohalm.cyclos.entities.members.Member;
import nl.strohalm.cyclos.entities.members.PendingMember;
import nl.strohalm.cyclos.services.customization.MemberCustomFieldService;
import nl.strohalm.cyclos.services.elements.ElementService;
import nl.strohalm.cyclos.services.permissions.PermissionService;
import nl.strohalm.cyclos.utils.CustomFieldHelper;
import nl.strohalm.cyclos.utils.binding.BeanBinder;
import nl.strohalm.cyclos.utils.binding.BeanCollectionBinder;
import nl.strohalm.cyclos.utils.binding.MapBean;
import nl.strohalm.cyclos.utils.binding.PropertyBinder;
import nl.strohalm.cyclos.utils.conversion.HtmlConverter;
import nl.strohalm.cyclos.utils.conversion.ReferenceConverter;
import nl.strohalm.cyclos.utils.validation.ValidationException;
@Controller
public class PendingMemberProfileController extends BaseRestController{
	private MemberCustomFieldService  memberCustomFieldService;
    private BeanBinder<PendingMember> dataBinder;

    private CustomFieldHelper         customFieldHelper;
    private ElementService elementService;
    private PermissionService permissionService;

    public BeanBinder<PendingMember> getDataBinder() {
        if (dataBinder == null) {
            final BeanBinder<MemberCustomFieldValue> customValueBinder = BeanBinder.instance(MemberCustomFieldValue.class);
            customValueBinder.registerBinder("field", PropertyBinder.instance(CustomField.class, "field", ReferenceConverter.instance(CustomField.class)));
            customValueBinder.registerBinder("value", PropertyBinder.instance(String.class, "value", HtmlConverter.instance()));
            customValueBinder.registerBinder("hidden", PropertyBinder.instance(Boolean.TYPE, "hidden"));

            final BeanBinder<PendingMember> binder = BeanBinder.instance(PendingMember.class);
            binder.registerBinder("id", PropertyBinder.instance(Long.class, "id"));
            binder.registerBinder("username", PropertyBinder.instance(String.class, "username"));
            binder.registerBinder("name", PropertyBinder.instance(String.class, "name"));
            binder.registerBinder("broker", PropertyBinder.instance(Member.class, "broker"));
            binder.registerBinder("email", PropertyBinder.instance(String.class, "email"));
            binder.registerBinder("hideEmail", PropertyBinder.instance(Boolean.TYPE, "hideEmail"));
            binder.registerBinder("customValues", BeanCollectionBinder.instance(customValueBinder, "customValues"));

            dataBinder = binder;
        }
        return dataBinder;
    }

    @Inject
    public void setCustomFieldHelper(final CustomFieldHelper customFieldHelper) {
        this.customFieldHelper = customFieldHelper;
    }

    @Inject
    public void setMemberCustomFieldService(final MemberCustomFieldService memberCustomFieldService) {
        this.memberCustomFieldService = memberCustomFieldService;
    }
    
    public static class PendingMemberProfileRequestDTO{
    	private Map<String,Object> values;
    	public Map<String,Object> getvalues;
    	public void PendingMemberProfileForm() {
    		
            setPendingMember("customValues", new MapBean(true, "field", "value", "hidden"));
        }

        public Map<String, Object> getPendingMember() {
            return values;
        }

        public Object getPendingMember(final String key) {
            return values.get(key);
        }

        public long getPendingMemberId() {
            try {
                return (Long) getPendingMember("id");
            } catch (final Exception e) {
                return 0L;
            }
        }

        public void setPendingMember(final Map<String, Object> map) {
            values = map;
        }

        public void setPendingMember(final String key, final Object value) {
            values.put(key, value);
        }

        public void setPendingMemberId(final long id) {
            setPendingMember("id", id);
        }

		public boolean isBroker() {
			// TODO Auto-generated method stub
			return false;
		}

		public Member getElement() {
			// TODO Auto-generated method stub
			return null;
		}
    }
    
    public static class PendingMemberProfileResponseDTO{
    	String message;

		public final String getMessage() {
			return message;
		}

		public final void setMessage(String message) {
			this.message = message;
		}
    }

    @RequestMapping(value ="/member/pendingMemberProfile", method = RequestMethod.GET)
    @ResponseBody
    protected PendingMemberProfileResponseDTO handleSubmit(@RequestBody PendingMemberProfileRequestDTO form) throws Exception {
        PendingMember pendingMember = elementService.loadPendingMember(form.getPendingMemberId());
        getDataBinder().readInto(pendingMember, form.getPendingMember(), true);
        if (form.isBroker()) {
            final Member loggedBroker = form.getElement();
            pendingMember.setBroker(loggedBroker);
        }
        pendingMember = elementService.update(pendingMember);
        PendingMemberProfileResponseDTO response = new PendingMemberProfileResponseDTO();
        response.setMessage("pendingMember.updated");
       return response;
    }

    //@Override
    protected void prepareForm(final ActionContext context) throws Exception {
        final HttpServletRequest request = context.getRequest();
        final PendingMemberProfileForm form = context.getForm();
        final long id = form.getPendingMemberId();
        if (id <= 0L) {
            throw new ValidationException();
        }
        final PendingMember pendingMember = elementService.loadPendingMember(id, PendingMember.Relationships.values());
        request.setAttribute("pendingMember", pendingMember);

        final List<MemberCustomField> customFields = customFieldHelper.onlyForGroup(memberCustomFieldService.list(), pendingMember.getMemberGroup());
        for (final Iterator<MemberCustomField> iterator = customFields.iterator(); iterator.hasNext();) {
            final MemberCustomField customField = iterator.next();
            if (!customField.getVisibilityAccess().granted(context.getGroup(), false, context.isBroker(), true, false)) {
                iterator.remove();
            }
        }
        request.setAttribute("customFields", customFieldHelper.buildEntries(customFields, pendingMember.getCustomValues()));

        boolean editable = false;
        if (context.isAdmin()) {
            editable = permissionService.hasPermission(AdminMemberPermission.MEMBERS_MANAGE_PENDING);
        } else if (context.isBroker()) {
            editable = permissionService.hasPermission(BrokerPermission.MEMBERS_MANAGE_PENDING);
        }
        request.setAttribute("editable", editable);
    }

   // @Override
    protected void validateForm(final ActionContext context) {
        final PendingMemberProfileForm form = context.getForm();
        final PendingMember pendingMember = getDataBinder().readFromString(form.getPendingMember());
        elementService.validate(pendingMember);
    }
}
