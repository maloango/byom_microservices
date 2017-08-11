package nl.strohalm.cyclos.webservices.rest.accounts.external;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import nl.strohalm.cyclos.annotations.Inject;
import nl.strohalm.cyclos.entities.accounts.external.ExternalAccountDetailsVO;
import nl.strohalm.cyclos.services.accounts.external.ExternalAccountService;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class OverviewExternalAccountsController extends BaseRestController {
	private ExternalAccountService externalAccountService;

	public final ExternalAccountService getExternalAccountService() {
		return externalAccountService;
	}

	@Inject
	public void setExternalAccountService(final ExternalAccountService externalAccountService) {
		this.externalAccountService = externalAccountService;
	}

	public static class OverviewExternalAccountsRequestDto {
             private Long              id;
    private String            name;
    private BigDecimal        balance;

    public OverviewExternalAccountsRequestDto(final Long id, final String name, final BigDecimal balance) {

        this.id = id;
        this.name = name;
        if (balance == null) {
            this.balance = BigDecimal.ZERO;
        } else {
            this.balance = balance;
        }
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setBalance(final BigDecimal balance) {
        this.balance = balance;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public void setName(final String name) {
        this.name = name;
    }

	}

	public static class OverviewExternalAccountsResponseDto {
		List<ExternalAccountDetailsVO> externalAccounts;
                String message;

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
                

        public List<ExternalAccountDetailsVO> getExternalAccounts() {
            return externalAccounts;
        }

        public void setExternalAccounts(List<ExternalAccountDetailsVO> externalAccounts) {
            this.externalAccounts = externalAccounts;
        }
                

		public OverviewExternalAccountsResponseDto(List<ExternalAccountDetailsVO> externalAccounts) {
			super();
			this.externalAccounts = externalAccounts;
		}
                public OverviewExternalAccountsResponseDto(){
                }

        

	}

	@RequestMapping(value = "admin/overviewExternalAccounts/{id}", method = RequestMethod.GET)
	@ResponseBody
	protected OverviewExternalAccountsResponseDto executeAction(@PathVariable ("id") long id)throws Exception {
            OverviewExternalAccountsResponseDto response =null;
	try{
		final List<ExternalAccountDetailsVO> externalAccounts = externalAccountService.externalAccountOverview();
                response.setMessage("externalAccounts");
		response = new OverviewExternalAccountsResponseDto(externalAccounts);}
        catch(Exception e){
            e.printStackTrace();
        }
                        

		return response;
	}
}
