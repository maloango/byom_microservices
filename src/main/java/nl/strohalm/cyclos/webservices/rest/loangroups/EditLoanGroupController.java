/*package nl.strohalm.cyclos.webservices.rest.loangroups;

import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import nl.strohalm.cyclos.access.AdminSystemPermission;
import nl.strohalm.cyclos.entities.accounts.loans.LoanGroup;
import nl.strohalm.cyclos.entities.customization.fields.CustomFieldValue;
import nl.strohalm.cyclos.entities.customization.fields.LoanGroupCustomField;
import nl.strohalm.cyclos.entities.customization.fields.LoanGroupCustomFieldValue;
import nl.strohalm.cyclos.entities.groups.AdminGroup;
import nl.strohalm.cyclos.services.groups.GroupService;
import nl.strohalm.cyclos.services.permissions.PermissionService;
import nl.strohalm.cyclos.utils.ActionHelper;
import nl.strohalm.cyclos.utils.binding.BeanBinder;
import nl.strohalm.cyclos.utils.binding.BeanCollectionBinder;
import nl.strohalm.cyclos.utils.binding.DataBinder;
import nl.strohalm.cyclos.utils.binding.MapBean;
import nl.strohalm.cyclos.utils.binding.PropertyBinder;
import nl.strohalm.cyclos.utils.conversion.HtmlConverter;
import nl.strohalm.cyclos.utils.conversion.IdConverter;
import nl.strohalm.cyclos.utils.conversion.ReferenceConverter;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;
@Controller
public class EditLoanGroupController extends BaseRestController{
	
	 private DataBinder<LoanGroup> readDataBinder;
	    private DataBinder<LoanGroup> writeDataBinder;
	    private PermissionService permissionService;
	    private GroupService groupService;
	   

	    public DataBinder<LoanGroup> getReadDataBinder() {
	        if (readDataBinder == null) {
	            final BeanBinder<LoanGroup> binder = BeanBinder.instance(LoanGroup.class);
	            binder.registerBinder("id", PropertyBinder.instance(Long.class, "id", IdConverter.instance()));
	            binder.registerBinder("name", PropertyBinder.instance(String.class, "name"));
	            binder.registerBinder("description", PropertyBinder.instance(String.class, "description"));
	            readDataBinder = binder;
	        }
	        return readDataBinder;
	    }

	    public DataBinder<LoanGroup> getWriteDataBinder() {
	        if (writeDataBinder == null) {

	            final BeanBinder<? extends CustomFieldValue> customValueBinder = BeanBinder.instance(LoanGroupCustomFieldValue.class);
	            customValueBinder.registerBinder("field", PropertyBinder.instance(LoanGroupCustomField.class, "field", ReferenceConverter.instance(LoanGroupCustomField.class)));
	            customValueBinder.registerBinder("value", PropertyBinder.instance(String.class, "value", HtmlConverter.instance()));

	            final BeanBinder<LoanGroup> binder = BeanBinder.instance(LoanGroup.class);
	            binder.registerBinder("id", PropertyBinder.instance(Long.class, "id", IdConverter.instance()));
	            binder.registerBinder("name", PropertyBinder.instance(String.class, "name"));
	            binder.registerBinder("description", PropertyBinder.instance(String.class, "description"));
	            binder.registerBinder("customValues", BeanCollectionBinder.instance(customValueBinder, "customValues"));
	            writeDataBinder = binder;
	        }
	        return writeDataBinder;
	    }
	    
	    public static class EditLoanGroupRequestDTO{
	    	private long              loanGroupId;
	        private long              memberId;
	        private Map<String,Object>values;
	        public Map<String,Object>getvalues;

	        public void EditLoanGroupForm() {
	            setLoanGroup("customValues", new MapBean(true, "field", "value"));
	        }

	        public Map<String, Object> getLoanGroup() {
	            return values;
	        }

	        public Object getLoanGroup(final String name) {
	            return values.get(name);
	        }

	        public long getLoanGroupId() {
	            return loanGroupId;
	        }

	        public long getMemberId() {
	            return memberId;
	        }

	        public void setLoanGroup(final Map<String, Object> map) {
	            values = map;
	        }

	        public void setLoanGroup(final String name, final Object value) {
	            values.put(name, value);
	        }

	        public void setLoanGroupId(final long loanGroupId) {
	            this.loanGroupId = loanGroupId;
	        }

	        public void setMemberId(final long memberId) {
	            this.memberId = memberId;
	        }

			public boolean isAdmin() {
				// TODO Auto-generated method stub
				return false;
			}
	    	
	    }
	    
	    public static class EditLoanGroupResponseDTO{
	    	String message;

			public final String getMessage() {
				return message;
			}

			public final void setMessage(String message) {
				this.message = message;
			}
	    	
	    }

	    @RequestMapping(value= "", method =RequestMethod.POST)
	    @ResponseBody
	    public EditLoanGroupResponseDTO handleDisplay(@RequestBody EditLoanGroupRequestDTO form) throws Exception {
	       // prepareForm(form);
	       
	        final LoanGroup loanGroup = (LoanGroup) request.getAttribute("loanGroup");
	        boolean editable = false;
	        if (form.isAdmin()) {
	            editable = permissionService.hasPermission(AdminSystemPermission.LOAN_GROUPS_MANAGE);
	        }
	        if (!editable) {
	            return ActionHelper.redirectWithParam(request, context.findForward("view"), "loanGroupId", loanGroup.getId());
	        }
	        if (loanGroup.getId() != null) {
	            getReadDataBinder().writeAsString(form.getLoanGroup(), loanGroup);
	        }
	        request.setAttribute("editable", editable);

	        AdminGroup adminGroup = form.getGroup();
	        adminGroup = groupService.load(adminGroup.getId(), AdminGroup.Relationships.MANAGES_GROUPS);
	        request.setAttribute("managesGroups", adminGroup.getManagesGroups());

	        //return context.getInputForward();
	    }

	   // @Override
	    protected void formAction(final ActionContext context) throws Exception {
	        final EditLoanGroupForm form = context.getForm();
	        final LoanGroup loanGroup = getWriteDataBinder().readFromString(form.getLoanGroup());
	        final boolean isInsert = loanGroup.getId() == null;
	        getLoanGroupService().save(loanGroup);
	        context.sendMessage(isInsert ? "loanGroup.inserted" : "loanGroup.modified");
	    }

	  //  @Override
	    protected void validateForm(final ActionContext context) {
	        final EditLoanGroupForm form = context.getForm();
	        final LoanGroup loanGroup = getWriteDataBinder().readFromString(form.getLoanGroup());
	        getLoanGroupService().validate(loanGroup);
	    }


}
*/