package nl.strohalm.cyclos.webservices.rest.loangroups;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import nl.strohalm.cyclos.annotations.Inject;
import nl.strohalm.cyclos.entities.accounts.loans.LoanGroup;
import nl.strohalm.cyclos.entities.customization.fields.LoanGroupCustomField;
import nl.strohalm.cyclos.services.customization.LoanGroupCustomFieldService;
import nl.strohalm.cyclos.services.loangroups.LoanGroupService;
import nl.strohalm.cyclos.utils.CustomFieldHelper;
import nl.strohalm.cyclos.utils.binding.MapBean;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;
@Controller
public class BaseLoanGroupController extends BaseRestController{

	 private LoanGroupService            loanGroupService;
	    private LoanGroupCustomFieldService loanGroupCustomFieldService;

	    private CustomFieldHelper           customFieldHelper;

	    public LoanGroupCustomFieldService getLoanGroupCustomFieldService() {
	        return loanGroupCustomFieldService;
	    }

	    public LoanGroupService getLoanGroupService() {
	        return loanGroupService;
	    }

	    @Inject
	    public void setCustomFieldHelper(final CustomFieldHelper customFieldHelper) {
	        this.customFieldHelper = customFieldHelper;
	    }

	    @Inject
	    public void setLoanGroupCustomFieldService(final LoanGroupCustomFieldService loanGroupCustomFieldService) {
	        this.loanGroupCustomFieldService = loanGroupCustomFieldService;
	    }
	    
	    @Inject
	    public void setLoanGroupService(final LoanGroupService loanGroupService) {
	        this.loanGroupService = loanGroupService;
	    }
	    
	    public static class BaseLoanGroupRequestDTO{
	    	private long              loanGroupId;
	        private long              memberId;
	        private boolean           showInSearch;

	        public boolean isShowInSearch() {
	            return showInSearch;
	        }

	        public void setShowInSearch(final boolean showInSearch) {
	            this.showInSearch = showInSearch;
	        }
	        private Map<String,Object> values;
	        public Map<String,Object> getvalues;

	        public void  EditLoanGroupForm() {
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
	    }
	    
	    public static class BaseLoanGroupResponseDTO{
	    	List<LoanGroupCustomField>LoanGroupCustomField;

			public final List<LoanGroupCustomField> getLoanGroupCustomField() {
				return LoanGroupCustomField;
			}

			public final void setLoanGroupCustomField(List<LoanGroupCustomField> loanGroupCustomField) {
				LoanGroupCustomField = loanGroupCustomField;
			}
	    	
	    }

	    @RequestMapping(value = "",method = RequestMethod.GET)
	    @ResponseBody
	    protected BaseLoanGroupResponseDTO prepareForm(@RequestBody BaseLoanGroupRequestDTO  form) throws Exception {
	        final long id = form.getLoanGroupId();
	        LoanGroup loanGroup;
	        final List<LoanGroupCustomField> customFields = loanGroupCustomFieldService.list();
	        if (id > 0L) {
	            loanGroup = loanGroupService.load(id, LoanGroup.Relationships.CUSTOM_VALUES, LoanGroup.Relationships.MEMBERS);
	        } else {
	            loanGroup = new LoanGroup();
	        }
	        BaseLoanGroupResponseDTO response = new BaseLoanGroupResponseDTO();
	        response.setLoanGroupCustomField(customFields);
	        
	        return response;
	        /*response.setAttribute("loanGroup", loanGroup);
	        response .setAttribute("customFields", customFieldHelper.buildEntries(customFields, loanGroup.getCustomValues()));*/
	    }
}
