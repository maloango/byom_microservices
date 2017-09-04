package nl.strohalm.cyclos.webservices.rest.accounts.accountfees;


import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import nl.strohalm.cyclos.annotations.Inject;
import nl.strohalm.cyclos.controls.ActionContext;
//import nl.strohalm.cyclos.controls.accounts.accountfees.AccountFeeLogForm;
import nl.strohalm.cyclos.entities.accounts.AccountType;
import nl.strohalm.cyclos.entities.accounts.MemberAccountType;
import nl.strohalm.cyclos.entities.accounts.fees.account.AccountFee;
import nl.strohalm.cyclos.entities.accounts.fees.account.AccountFeeLog;
import nl.strohalm.cyclos.entities.accounts.fees.account.AccountFeeLogQuery;
import nl.strohalm.cyclos.entities.accounts.fees.account.AccountFeeQuery;
import nl.strohalm.cyclos.entities.groups.AdminGroup;
import nl.strohalm.cyclos.entities.groups.MemberGroup;
import nl.strohalm.cyclos.services.accountfees.AccountFeeService;
import nl.strohalm.cyclos.services.groups.GroupService;
import nl.strohalm.cyclos.utils.RelationshipHelper;
import nl.strohalm.cyclos.utils.binding.BeanBinder;
import nl.strohalm.cyclos.utils.binding.DataBinder;
import nl.strohalm.cyclos.utils.binding.DataBinderHelper;
import nl.strohalm.cyclos.utils.query.QueryParameters;
import org.springframework.stereotype.Controller;

import nl.strohalm.cyclos.webservices.rest.BaseRestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class ListAccountFeeLogController extends BaseRestController {
	private AccountFeeService              accountFeeService;
        private GroupService groupService;
        private AccountFeeLog accountFeeLog;

    public AccountFeeLog getAccountFeeLog() {
        return accountFeeLog;
    }

    public void setAccountFeeLog(AccountFeeLog accountFeeLog) {
        this.accountFeeLog = accountFeeLog;
    }

    public GroupService getGroupService() {
        return groupService;
    }

    public void setGroupService(GroupService groupService) {
        this.groupService = groupService;
    }

    private DataBinder<AccountFeeLogQuery> dataBinder;

    public AccountFeeService getAccountFeeService() {
        return accountFeeService;
    }

    public void setDataBinder(DataBinder<AccountFeeLogQuery> dataBinder) {
        this.dataBinder = dataBinder;
    }

    @Inject
    public void setAccountFeeService(final AccountFeeService accountFeeService) {
        this.accountFeeService = accountFeeService;
    }
    public static class ListAccountFeeLogRequestDTO{
    private Collection<AccountFee> accountFees;
    private Calendar               periodStartAt;
    private MemberAccountType      accountType;
    private Boolean                accountFeeEnabled;

    public AccountFee getAccountFee() {
        if (accountFees == null || accountFees.isEmpty()) {
            return null;
        } else {
            return accountFees.iterator().next();
        }
    }

    public Boolean getAccountFeeEnabled() {
        return accountFeeEnabled;
    }

    public Collection<AccountFee> getAccountFees() {
        return accountFees;
    }

    public MemberAccountType getAccountType() {
        return accountType;
    }

    public Calendar getPeriodStartAt() {
        return periodStartAt;
    }

    public void setAccountFee(final AccountFee accountFee) {
        accountFees = Collections.singleton(accountFee);
    }

    public void setAccountFeeEnabled(final Boolean accountFeeEnabled) {
        this.accountFeeEnabled = accountFeeEnabled;
    }

    public void setAccountFees(final Collection<AccountFee> accountFees) {
        this.accountFees = accountFees;
    }

    public void setAccountType(final MemberAccountType accountType) {
        this.accountType = accountType;
    }

    public void setPeriodStartAt(final Calendar periodStartAt) {
        this.periodStartAt = periodStartAt;
    }
    }
    public static class ListAccountFeeLogResponseDTO{
        List<AccountFeeLog> logs;
        String message;

        public void setMessage(String message) {
            this.message = message;
        }
        private boolean hasRunningFees;
        private boolean accountFees;

        public boolean isHasRunningFees() {
            return hasRunningFees;
        }

        public void setHasRunningFees(boolean hasRunningFees) {
            this.hasRunningFees = hasRunningFees;
        }

        public boolean isAccountFees() {
            return accountFees;
        }

        public void setAccountFees(boolean accountFees) {
            this.accountFees = accountFees;
        }

        public List<AccountFeeLog> getLogs() {
            return logs;
        }

        public void setLogs(List<AccountFeeLog> logs) {
            this.logs = logs;
        }
        
        public ListAccountFeeLogResponseDTO(List<AccountFeeLog> logs) {
            this.logs = logs;
        }

        private void setMessage(String accountFeeLogs, List<AccountFeeLog> logs) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }
        public ListAccountFeeLogResponseDTO(){
        }

        public String getMessage() {
            return message;
        }
              
    }

    @RequestMapping(value = "admin/listAccountFeeLog", method = RequestMethod.GET)
	@ResponseBody
	protected ListAccountFeeLogResponseDTO executeQuery(
			final QueryParameters queryParameters) {
			
         ListAccountFeeLogResponseDTO response = null;
         try{
        final AccountFeeLogQuery query = (AccountFeeLogQuery) queryParameters;
        final List<AccountFeeLog> logs = accountFeeService.searchLogs(query);
        response.setMessage("accountFeeLogs", logs);
       response = new ListAccountFeeLogResponseDTO(logs);
         }
         catch(Exception e){
             e.printStackTrace();
             
             
         }
       
        return response;
        
       
    }


    //@Override
//    protected QueryParameters prepareForm(final ActionContext context) {
//        final HttpServletRequest request = context.getRequest();
//
//        // Groups managed by the admin group
//        AdminGroup adminGroup = context.getGroup();
//        adminGroup = groupService.load(adminGroup.getId(), AdminGroup.Relationships.MANAGES_GROUPS);
//        final Collection<MemberGroup> managedGroups = (adminGroup.getManagesGroups());
//
//        final AccountFeeQuery feeQuery = new AccountFeeQuery();
//        feeQuery.fetch(AccountFee.Relationships.LOGS, RelationshipHelper.nested(AccountFee.Relationships.ACCOUNT_TYPE, AccountType.Relationships.CURRENCY));
//        feeQuery.setReturnDisabled(false);
//        feeQuery.setGroups(managedGroups);
//        final List<AccountFee> fees = accountFeeService.search(feeQuery);
//
//        feeQuery.setReturnDisabled(true);
//
//        final AccountFeeLogForm form = context.getForm();
//
//        final AccountFeeLogQuery logQuery = getDataBinder().readFromString(form.getQuery());
//        logQuery.setAccountFees(fees);
//
//        // Check if there is at least one fee which is currently running
//        boolean hasRunningFees = false;
//        for (final AccountFee fee : fees) {
//            final AccountFeeLog lastExecution = fee.getLastExecution();
//            if (lastExecution != null && !lastExecution.isFinished()) {
//                hasRunningFees = true;
//                break;
//            }
//        }
//        request.setAttribute("hasRunningFees", hasRunningFees);
//
//        request.setAttribute("accountFees", fees);
//
//        return logQuery;
//    }

   // @Override
    protected boolean willExecuteQuery(final ActionContext context, final QueryParameters queryParameters) throws Exception {
        return true;
    }

    private DataBinder<AccountFeeLogQuery> getDataBinder() {
        if (dataBinder == null) {
            final BeanBinder<AccountFeeLogQuery> binder = BeanBinder.instance(AccountFeeLogQuery.class);
            binder.registerBinder("pageParameters", DataBinderHelper.pageBinder());
            dataBinder = binder;
        }
        return dataBinder;
    }


            
}

