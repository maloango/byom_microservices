package nl.strohalm.cyclos.webservices.rest.accounts.accounttypes;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import nl.strohalm.cyclos.annotations.Inject;
import nl.strohalm.cyclos.entities.accounts.AccountType;
import nl.strohalm.cyclos.services.accounts.AccountTypeService;
import nl.strohalm.cyclos.services.accounts.MemberAccountTypeQuery;
import nl.strohalm.cyclos.services.accounts.SystemAccountTypeQuery;
import nl.strohalm.cyclos.webservices.model.AccountTypeVO;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;
import nl.strohalm.cyclos.webservices.rest.GenericResponse;
import nl.strohalm.cyclos.webservices.utils.AccountHelper;

@Controller
public class ListAccountTypesController extends BaseRestController {
	private AccountTypeService accountTypeService;
        private AccountHelper accountHelper;

	public AccountTypeService getAccountTypeService() {
		return accountTypeService;
	}

	@Inject
	public void setAccountTypeService(final AccountTypeService accountTypeService) {
		this.accountTypeService = accountTypeService;
	}

    public AccountHelper getAccountHelper() {
        return accountHelper;
    }

    public void setAccountHelper(AccountHelper accountHelper) {
        this.accountHelper = accountHelper;
    }

        

	public static class ListAccountTypesResponseDTO  extends GenericResponse{
		private List<AccountTypeVO> accountType=new ArrayList<AccountTypeVO>();
                

        public ListAccountTypesResponseDTO(List<AccountTypeVO> accountType) {
            this.accountType = accountType;
        }
                

		public final List<AccountTypeVO> getAccountType() {
			return accountType;
		}

		public final void setAccountType(List<AccountTypeVO> accountType) {
			this.accountType = accountType;
		}

		public ListAccountTypesResponseDTO(){}
	}

	@RequestMapping(value = "admin/listAccountTypes", method = RequestMethod.GET)
	@ResponseBody
	protected ListAccountTypesResponseDTO executeAction() throws Exception {
		
                ListAccountTypesResponseDTO response = new ListAccountTypesResponseDTO();
                try{
		final List<AccountType> accountTypes = new ArrayList<AccountType>();
		// Get the system accounts
		final SystemAccountTypeQuery systemQuery = new SystemAccountTypeQuery();
		systemQuery.fetch(AccountType.Relationships.CURRENCY);
		accountTypes.addAll(accountTypeService.search(systemQuery));
                   
		// Get the member accounts
		final MemberAccountTypeQuery memberQuery = new MemberAccountTypeQuery();
		memberQuery.fetch(AccountType.Relationships.CURRENCY);
		accountTypes.addAll(accountTypeService.search(memberQuery));
		System.out.println("----------- "+accountTypes.size());
                for(AccountType at:accountTypes){
                    response.getAccountType().add(accountHelper.toVO(at));
                }
                response.setStatus(0);
                response.setMessage("Account type list!!");
		//response.setAccountType(accountTypes);
               // response = new ListAccountTypesResponseDTO(accountTypes)
		
	       }
                catch (Exception ex) {
                ex.printStackTrace();
            }
                return response;
}
}
