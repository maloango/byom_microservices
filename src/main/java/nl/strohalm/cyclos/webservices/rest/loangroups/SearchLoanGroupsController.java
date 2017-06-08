package nl.strohalm.cyclos.webservices.rest.loangroups;

import java.util.Collection;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import nl.strohalm.cyclos.annotations.Inject;
import nl.strohalm.cyclos.controls.ActionContext;
import nl.strohalm.cyclos.controls.loangroups.SearchLoanGroupsForm;
import nl.strohalm.cyclos.controls.restapi.BaseRestController;
import nl.strohalm.cyclos.entities.EntityReference;
import nl.strohalm.cyclos.entities.accounts.loans.LoanGroup;
import nl.strohalm.cyclos.entities.accounts.loans.LoanGroupQuery;
import nl.strohalm.cyclos.entities.customization.fields.CustomFieldValue;
import nl.strohalm.cyclos.entities.members.Element;
import nl.strohalm.cyclos.entities.members.Member;
import nl.strohalm.cyclos.services.elements.ElementService;
import nl.strohalm.cyclos.services.loangroups.LoanGroupService;
import nl.strohalm.cyclos.utils.binding.BeanBinder;
import nl.strohalm.cyclos.utils.binding.DataBinder;
import nl.strohalm.cyclos.utils.binding.PropertyBinder;
import nl.strohalm.cyclos.utils.conversion.ReferenceConverter;
import nl.strohalm.cyclos.utils.query.QueryParameters;
@Controller
public class SearchLoanGroupsController extends BaseRestController {
	
	private LoanGroupService           loanGroupService;
    private DataBinder<LoanGroupQuery> dataBinder;
    private ElementService elementService;

    public DataBinder<LoanGroupQuery> getDataBinder() {
        if (dataBinder == null) {
            final BeanBinder<LoanGroupQuery> binder = BeanBinder.instance(LoanGroupQuery.class);
            binder.registerBinder("name", PropertyBinder.instance(String.class, "name"));
            binder.registerBinder("description", PropertyBinder.instance(String.class, "description"));
            binder.registerBinder("member", PropertyBinder.instance(Member.class, "member", ReferenceConverter.instance(Member.class)));
            dataBinder = binder;
        }
        return dataBinder;
    }

    public LoanGroupService getLoanGroupService() {
        return loanGroupService;
    }

    @Inject
    public void setLoanGroupService(final LoanGroupService loanGroupService) {
        this.loanGroupService = loanGroupService;
    }
    
    public static class SearchLoanGroupsRequestDTO{
    	private String                                 description;
        private Member                                 member;
        private String                                 name;
        private Boolean                                noLoans;
        private boolean                                notOfMember;
		public Collection<? extends CustomFieldValue> getCustomValues() {
            return getCustomValues();
        }

        public String getDescription() {
            return description;
        }

        public Member getMember() {
            return member;
        }

        public String getName() {
            return name;
        }

        public Boolean getNoLoans() {
            return noLoans;
        }

        public boolean isNotOfMember() {
            return notOfMember;
        }

        public void setCustomValues(final Collection<? extends CustomFieldValue> customValues) {
        }

        public void setDescription(final String description) {
            this.description = description;
        }

        public void setMember(final Member member) {
            this.member = member;
        }

        public void setName(final String name) {
            this.name = name;
        }

        public void setNoLoans(final Boolean noLoans) {
            this.noLoans = noLoans;
        }

        public void setNotOfMember(final boolean notOfMember) {
            this.notOfMember = notOfMember;
        }
    	
    }
    
    public static class SearchLoanGroupsResponseDTo{
    	String message;
    	public final String getMessage() {
			return message;
		}

		public final void setMessage(String message) {
			this.message = message;
		}

		List<LoanGroup> loanGroups;
		public final List<LoanGroup> getLoanGroups() {
			return loanGroups;
		}

		public final void setLoanGroups(List<LoanGroup> loanGroups) {
			this.loanGroups = loanGroups;
		}
    	
    }

    @RequestMapping(value= "/admin/searchLoanGroups", method =RequestMethod.GET)
    @ResponseBody
    protected SearchLoanGroupsResponseDTo executeQuery(@RequestBody SearchLoanGroupsRequestDTO form, final QueryParameters queryParameters) {
        final LoanGroupQuery query = (LoanGroupQuery) queryParameters;
        final List<LoanGroup> loanGroups = loanGroupService.search(query);
        SearchLoanGroupsResponseDTo response = new SearchLoanGroupsResponseDTo();
        //response.getRequest().setAttribute("loanGroups", loanGroups);
        return response;
    }

  //  @Override
    protected QueryParameters prepareForm(final ActionContext context) {
        final SearchLoanGroupsForm form = context.getForm();
        final LoanGroupQuery query = getDataBinder().readFromString(form.getQuery());
        if (query.getMember() instanceof EntityReference) {
            query.setMember((Member) elementService.load(query.getMember().getId(), Element.Relationships.USER));
        }
        return query;
    }

  //  @Override
    protected boolean willExecuteQuery(final ActionContext context, final QueryParameters queryParameters) throws Exception {
        return true;
    }

}
