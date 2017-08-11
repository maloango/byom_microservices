package nl.strohalm.cyclos.webservices.rest.accounts.currencies;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import nl.strohalm.cyclos.annotations.Inject;
import nl.strohalm.cyclos.entities.accounts.ARateParameters;
import nl.strohalm.cyclos.entities.accounts.Currency;
import nl.strohalm.cyclos.entities.accounts.DRateParameters;
import nl.strohalm.cyclos.entities.accounts.IRateParameters;
import nl.strohalm.cyclos.services.accounts.CurrencyService;
import nl.strohalm.cyclos.services.permissions.PermissionService;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class ListCurrenciesController extends BaseRestController {
	private CurrencyService currencyService;
	private PermissionService permissionService;
        private List<Currency> currencies;

    public List<Currency> getCurrencies() {
        return currencies;
    }

    public void setCurrencies(List<Currency> currencies) {
        this.currencies = currencies;
    }
        
	public final PermissionService getPermissionService() {
		return permissionService;
	}

	public final void setPermissionService(PermissionService permissionService) {
		this.permissionService = permissionService;
	}

	public final CurrencyService getCurrencyService() {
		return currencyService;
	}

	@Inject
	public void setCurrencyService(final CurrencyService currencyService) {
		this.currencyService = currencyService;
	}

	public static class ListCurrenciesRequestDTO {
           private String            name;
           private String            description;
           private String            symbol;
           private String            pattern          = "#amount#";
           private DRateParameters   dRateParameters;
           private ARateParameters   aRateParameters;
           private IRateParameters   iRateParameters;
           private long currencyID ;

        public long getCurrencyID() {
            return currencyID;
        }

        public void setCurrencyID(long currencyID) {
            this.currencyID = currencyID;
        }
    public ARateParameters getaRateParameters() {
        return aRateParameters;
    }

    public String getDescription() {
        return description;
    }

    public DRateParameters getdRateParameters() {
        return dRateParameters;
    }

    public IRateParameters getiRateParameters() {
        return iRateParameters;
    }

    public BigDecimal getMinimalD() {
        if (isEnableDRate()) {
            return dRateParameters.getMinimalD();
        }
        return null;
    }

   // @Override
    public String getName() {
        return name;
    }

    public String getPattern() {
        return pattern;
    }

    public String getSymbol() {
        return symbol;
    }

    public boolean isEnableARate() {
        return aRateParameters != null;
    }

    public boolean isEnableDRate() {
        return dRateParameters != null;
    }

    public boolean isEnableIRate() {
        return iRateParameters != null;
    }

    public void setaRateParameters(final ARateParameters rateParameters) {
        aRateParameters = rateParameters;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public void setdRateParameters(final DRateParameters dRateParameters) {
        this.dRateParameters = dRateParameters;
    }

    public void setiRateParameters(final IRateParameters iRateParameters) {
        this.iRateParameters = iRateParameters;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public void setPattern(final String pattern) {
        this.pattern = pattern;
    }

    public void setSymbol(final String symbol) {
        this.symbol = symbol;
    }

    
           
	}

	public static class ListCurrenciesResponseDTO {
		private long currencyID;
                String message;

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public long getCurrencyID() {
            return currencyID;
        }

        public void setCurrencyID(long currencyID) {
            this.currencyID = currencyID;
        }
	}

	@RequestMapping(value = "admin/listCurrencies/{currencyID}", method = RequestMethod.GET)
	@ResponseBody
	protected ListCurrenciesResponseDTO executeAction(@PathVariable("currencyID") long currencyID) throws Exception {
            ListCurrenciesResponseDTO response =null;
            //ListCurrenciesResponseDTO response = new ListCurrenciesResponseDTO(currencies, Boolean.TRUE);
            try{
                System.out.println("list currency is running");
		final List<Currency> currencies = currencyService.listAll();
		//Boolean editable = permissionService.hasPermission(AdminSystemPermission.CURRENCIES_MANAGE);
		response = new ListCurrenciesResponseDTO();}
            
            catch(Exception e){
                e.printStackTrace();
            }
		return response;
	}

}
