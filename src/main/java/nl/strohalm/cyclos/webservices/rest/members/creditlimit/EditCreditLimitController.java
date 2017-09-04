package nl.strohalm.cyclos.webservices.rest.members.creditlimit;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import nl.strohalm.cyclos.annotations.Inject;
import nl.strohalm.cyclos.controls.ActionContext;
//import nl.strohalm.cyclos.controls.members.creditlimit.EditCreditLimitForm;
import nl.strohalm.cyclos.entities.accounts.AccountType;
import nl.strohalm.cyclos.entities.members.Element;
import nl.strohalm.cyclos.entities.members.Member;
import nl.strohalm.cyclos.entities.settings.LocalSettings;
import nl.strohalm.cyclos.entities.settings.events.LocalSettingsEvent;
import nl.strohalm.cyclos.services.accounts.AccountService;
import nl.strohalm.cyclos.services.accounts.CreditLimitDTO;
import nl.strohalm.cyclos.services.elements.ElementService;
import nl.strohalm.cyclos.services.settings.SettingsService;
import nl.strohalm.cyclos.utils.ActionHelper;
import nl.strohalm.cyclos.utils.EntityHelper;
import nl.strohalm.cyclos.utils.binding.BeanBinder;
import nl.strohalm.cyclos.utils.binding.DataBinder;
import nl.strohalm.cyclos.utils.binding.MapBinder;
import nl.strohalm.cyclos.utils.binding.PropertyBinder;
import nl.strohalm.cyclos.utils.validation.ValidationException;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;

import org.apache.struts.action.ActionForward;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class EditCreditLimitController extends BaseRestController {
	private AccountService accountService;
	private ElementService elementService;
	private SettingsService settingsService;

	private DataBinder<CreditLimitDTO> dataBinder;

	public AccountService getAccountService() {
		return accountService;
	}

	public DataBinder<CreditLimitDTO> getDataBinder() {
		if (dataBinder == null) {

			final LocalSettings localSettings = settingsService
					.getLocalSettings();

			final PropertyBinder<AccountType> keyBinder = PropertyBinder
					.instance(AccountType.class, "accountTypeIds");
			final PropertyBinder<BigDecimal> limitValueBinder = PropertyBinder
					.instance(BigDecimal.class, "newCreditLimits",
							localSettings.getNumberConverter()
									.negativeToAbsolute());
			final PropertyBinder<BigDecimal> upperLimitValueBinder = PropertyBinder
					.instance(BigDecimal.class, "newUpperCreditLimits",
							localSettings.getNumberConverter());

			final MapBinder<AccountType, BigDecimal> limitBinder = MapBinder
					.instance(keyBinder, limitValueBinder);
			final MapBinder<AccountType, BigDecimal> upperLimitBinder = MapBinder
					.instance(keyBinder, upperLimitValueBinder);

			final BeanBinder<CreditLimitDTO> binder = BeanBinder
					.instance(CreditLimitDTO.class);
			binder.registerBinder("limitPerType", limitBinder);
			binder.registerBinder("upperLimitPerType", upperLimitBinder);
			dataBinder = binder;
		}
		return dataBinder;
	}

	public void onLocalSettingsUpdate(final LocalSettingsEvent event) {
		dataBinder = null;
	}

	@Inject
	public void setAccountService(final AccountService accountService) {
		this.accountService = accountService;
	}

	public static class EditCreditLimitRequestDto {

		 private long              memberId;
		    private long[]            accountTypeIds;
		    private String[]          newCreditLimits;
		    private String[]          newUpperCreditLimits;

		    public long[] getAccountTypeIds() {
		        return accountTypeIds;
		    }

		    public long getMemberId() {
		        return memberId;
		    }

		    public String[] getNewCreditLimits() {
		        return newCreditLimits;
		    }

		    public String[] getNewUpperCreditLimits() {
		        return newUpperCreditLimits;
		    }

		    public void setAccountTypeIds(final long[] accountTypeIds) {
		        this.accountTypeIds = accountTypeIds;
		    }

		    public void setMemberId(final long memberId) {
		        this.memberId = memberId;
		    }

		    public void setNewCreditLimits(final String[] newCreditLimits) {
		        this.newCreditLimits = newCreditLimits;
		    }

		    public void setNewUpperCreditLimits(final String[] newUpperCreditLimits) {
		        this.newUpperCreditLimits = newUpperCreditLimits;
		    }

	}

	public static class EditCreditLimitResponseDto {
		private String message;
		private Long memberId;

		public EditCreditLimitResponseDto(String message, Long memberId) {
			super();
			this.message = message;
			this.memberId = memberId;
		}

		public Long getMemberId() {
			return memberId;
		}

		public void setMemberId(Long memberId) {
			this.memberId = memberId;
		}

		public String getMessage() {
			return message;
		}

		public void setMessage(String message) {
			this.message = message;
		}
	}

	@RequestMapping(value = "admin/editCreditLimit", method = RequestMethod.PUT)
	@ResponseBody
	protected EditCreditLimitResponseDto handleSubmit(
			@RequestBody final EditCreditLimitRequestDto form) throws Exception {
		EditCreditLimitResponseDto response = null;
                try{
		final CreditLimitDTO creditLimit = getDataBinder().readFromString(form);
		accountService.setCreditLimit(getMember(form), creditLimit);
		String message = "creditLimit.modified";
		response = new EditCreditLimitResponseDto(
				message, form.getMemberId());}
                catch(Exception e){
                    e.printStackTrace();
                }
		return response;
	}

	/*protected void prepareForm(final ActionContext context) throws Exception {
		final HttpServletRequest request = context.getRequest();
		final EditCreditLimitForm form = context.getForm();
		final long id = form.getMemberId();
		if (id <= 0L) {
			throw new ValidationException();
		}
		final Member member = elementService.load(id,
				Element.Relationships.USER);
		final CreditLimitDTO creditLimit = accountService
				.getCreditLimits(member);

		// Transform positive to negative values to pass to the JSP
		Map<? extends AccountType, BigDecimal> limitPerType = creditLimit
				.getLimitPerType();
		final Map<AccountType, BigDecimal> newLimitPerType = new HashMap<AccountType, BigDecimal>();
		for (final AccountType accountType : limitPerType.keySet()) {
			BigDecimal limit = limitPerType.get(accountType);
			if (limit != null && limit.compareTo(new BigDecimal(0)) == 1) {
				limit = limit.negate();
			}
			newLimitPerType.put(accountType, limit);
		}
		limitPerType = newLimitPerType;
		creditLimit.setLimitPerType(limitPerType);

		request.setAttribute("member", member);
		request.setAttribute("limits", creditLimit.getEntries());
	}*/

/*	protected void validateForm(final ActionContext context) {
		final EditCreditLimitForm form = context.getForm();
		final CreditLimitDTO creditLimit = getDataBinder().readFromString(form);
		accountService.validate(getMember(form), creditLimit);
	}*/

	/**
	 * @param form
	 */
	private Member getMember(final EditCreditLimitRequestDto form) {
		return EntityHelper.reference(Member.class, form.getMemberId());
	}
}
