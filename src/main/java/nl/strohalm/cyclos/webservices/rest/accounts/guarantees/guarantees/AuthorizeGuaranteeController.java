package nl.strohalm.cyclos.webservices.rest.accounts.guarantees.guarantees;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import nl.strohalm.cyclos.access.AdminSystemPermission;
import nl.strohalm.cyclos.annotations.Inject;
import nl.strohalm.cyclos.controls.ActionContext;
import nl.strohalm.cyclos.controls.accounts.guarantees.guarantees.AuthorizeGuaranteeForm;
import nl.strohalm.cyclos.entities.access.AdminUser;
import nl.strohalm.cyclos.entities.access.User;
import nl.strohalm.cyclos.entities.accounts.guarantees.Guarantee;
import nl.strohalm.cyclos.entities.accounts.guarantees.GuaranteeType;
import nl.strohalm.cyclos.entities.accounts.guarantees.GuaranteeType.FeeType;
import nl.strohalm.cyclos.entities.accounts.transactions.TransferType;
import nl.strohalm.cyclos.entities.customization.fields.CustomFieldValue;
import nl.strohalm.cyclos.entities.customization.fields.PaymentCustomField;
import nl.strohalm.cyclos.entities.customization.fields.PaymentCustomFieldValue;
import nl.strohalm.cyclos.entities.exceptions.UnexpectedEntityException;
import nl.strohalm.cyclos.entities.settings.LocalSettings;
import nl.strohalm.cyclos.services.accounts.guarantees.GuaranteeFeeVO;
import nl.strohalm.cyclos.services.accounts.guarantees.GuaranteeService;
import nl.strohalm.cyclos.services.accounts.guarantees.exceptions.GuaranteeStatusChangeException;
import nl.strohalm.cyclos.services.customization.PaymentCustomFieldService;
import nl.strohalm.cyclos.services.elements.ElementService;
import nl.strohalm.cyclos.services.groups.GroupService;
import nl.strohalm.cyclos.services.permissions.PermissionService;
import nl.strohalm.cyclos.services.settings.SettingsService;
import nl.strohalm.cyclos.services.transactions.exceptions.AuthorizedPaymentInPastException;
import nl.strohalm.cyclos.services.transactions.exceptions.CreditsException;
import nl.strohalm.cyclos.utils.CustomFieldHelper;
import nl.strohalm.cyclos.utils.RequestHelper;
import nl.strohalm.cyclos.utils.binding.BeanBinder;
import nl.strohalm.cyclos.utils.binding.BeanCollectionBinder;
import nl.strohalm.cyclos.utils.binding.DataBinder;
import nl.strohalm.cyclos.utils.binding.DataBinderHelper;
import nl.strohalm.cyclos.utils.binding.MapBean;
import nl.strohalm.cyclos.utils.binding.PropertyBinder;
import nl.strohalm.cyclos.utils.conversion.CalendarConverter;
import nl.strohalm.cyclos.utils.conversion.HtmlConverter;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;

@Controller
public class AuthorizeGuaranteeController extends BaseRestController {

	private GuaranteeService guaranteeService;
	private PaymentCustomFieldService paymentCustomFieldService;
	private DataBinder<Guarantee> dataBinder;
	private GroupService groupService;
	private ElementService elementService;
	private PermissionService permissionService;
	private SettingsService settingsService;
	private DataBinder<Guarantee> readDataBinder;

	private CustomFieldHelper customFieldHelper;

	@Inject
	public void setCustomFieldHelper(final CustomFieldHelper customFieldHelper) {
		this.customFieldHelper = customFieldHelper;
	}

	@Inject
	public void setGuaranteeService(final GuaranteeService guaranteeService) {
		this.guaranteeService = guaranteeService;
	}

	@Inject
	public void setPaymentCustomFieldService(
			final PaymentCustomFieldService paymentCustomFieldService) {
		this.paymentCustomFieldService = paymentCustomFieldService;
	}

	public static class AuthorizeGuaranteeRequestDto {
		private Long guaranteeId;
		private boolean automaticLoanAuthorization;

		protected Map<String, Object> values;

		public Map<String, Object> getValues() {
			return values;
		}

		public void setValues(final Map<String, Object> values) {
			this.values = values;
		}

		public Map<String, Object> getGuarantee() {
			return values;
		}

		public Object getGuarantee(final String key) {
			return values.get(key);
		}

		public Long getGuaranteeId() {
			return guaranteeId;
		}

		public boolean isAutomaticLoanAuthorization() {
			return automaticLoanAuthorization;
		}

		public void setAutomaticLoanAuthorization(
				final boolean automaticLoanAuthorization) {
			this.automaticLoanAuthorization = automaticLoanAuthorization;
		}

		public void setGuarantee(final Map<String, Object> values) {
			this.values = values;
		}

		User user;

		public boolean isAdmin() {
			return user instanceof AdminUser;
		}
	}

	public static class AuthorizeGuaranteeResponseDto {
		private String message;
		private Long guaranteeId;

		public AuthorizeGuaranteeResponseDto(String message, Long guaranteeId) {
			super();
			this.message = message;
			this.guaranteeId = guaranteeId;
		}

		public String getMessage() {
			return message;
		}

		public void setMessage(String message) {
			this.message = message;
		}
	}

	@RequestMapping(value = "/admin/managePasswords", method = RequestMethod.POST)
	@ResponseBody
	protected AuthorizeGuaranteeResponseDto handleSubmit(
			@RequestBody AuthorizeGuaranteeRequestDto form) throws Exception {
		// final AuthorizeGuaranteeForm form = context.getForm();
		final Guarantee guarantee = guaranteeService.load(
				form.getGuaranteeId(), Guarantee.Relationships.GUARANTEE_TYPE);
		updateGuarantee(form, guarantee);
		String message=null;
		Long guaranteeId=null;
		AuthorizeGuaranteeResponseDto response = new AuthorizeGuaranteeResponseDto(message, guaranteeId);
		try {
			guaranteeService.acceptGuarantee(guarantee,
					form.isAutomaticLoanAuthorization());
			guaranteeId =guarantee.getId();
			return response;
		} catch (final GuaranteeStatusChangeException e) {
			 message ="guarantee.error.changeStatus";
					//context.message("guarantee.status." + e.getNewstatus()));
			 guaranteeId =guarantee.getId();
			return response;
		} catch (final CreditsException e) {
			message=e.toString();
			return response;
		} catch (final UnexpectedEntityException e) {
			 message ="payment.error.invalidTransferType";
		} catch (final AuthorizedPaymentInPastException e) {
			 message = "payment.error.authorizedInPast";
			return response;
		}
		return response;
	}

	/**
	 * Method use to prepare a form for being displayed
	 */

	protected void prepareForm(final ActionContext context) throws Exception {
		final HttpServletRequest request = context.getRequest();
		final AuthorizeGuaranteeForm form = context.getForm();
		final Long id = form.getGuaranteeId();
		final Guarantee guarantee = guaranteeService.load(id,
				Guarantee.Relationships.GUARANTEE_TYPE);
		final boolean canAcceptLoan = permissionService
				.hasPermission(AdminSystemPermission.PAYMENTS_AUTHORIZE);
		getReadDataBinder().writeAsString(form.getGuarantee(), guarantee);

		// suggest the validity begin as the current date
		if (guarantee.getValidity() == null
				|| guarantee.getValidity().getBegin() == null) {
			final LocalSettings localSettings = settingsService
					.getLocalSettings();
			final CalendarConverter calendarConverter = localSettings
					.getRawDateConverter();

			((MapBean) form.getGuarantee("validity")).set("begin",
					calendarConverter.toString(Calendar.getInstance()));
		}

		final TransferType transferType = guarantee.getGuaranteeType()
				.getLoanTransferType();
		final List<PaymentCustomField> customFields = paymentCustomFieldService
				.list(transferType, false);
		request.setAttribute(
				"customFields",
				customFieldHelper.buildEntries(customFields,
						guarantee.getCustomValues()));
		request.setAttribute("canAcceptLoan", canAcceptLoan);

		request.setAttribute("guarantee", guarantee);
		RequestHelper.storeEnum(request, GuaranteeType.FeeType.class,
				"feeTypes");
	}

	protected void validateForm(final ActionContext context) {
		final AuthorizeGuaranteeForm form = context.getForm();
		final Guarantee guarantee = getDataBinder().readFromString(
				form.getGuarantee());
		guarantee.setId(form.getGuaranteeId()); // the id is not read by the
												// binder
		guaranteeService.validate(guarantee, true);
	}

	private DataBinder<Guarantee> getDataBinder() {
		if (dataBinder == null) {

			final BeanBinder<Guarantee> binder = BeanBinder
					.instance(Guarantee.class);
			final LocalSettings localSettings = settingsService
					.getLocalSettings();
			binder.registerBinder("validity",
					DataBinderHelper.rawPeriodBinder(localSettings, "validity"));
			binder.registerBinder("amount",
					PropertyBinder.instance(BigDecimal.class, "amount"));

			final BeanBinder<? extends CustomFieldValue> customValueBinder = BeanBinder
					.instance(PaymentCustomFieldValue.class);
			customValueBinder.registerBinder("field",
					PropertyBinder.instance(PaymentCustomField.class, "field"));
			customValueBinder.registerBinder("value", PropertyBinder.instance(
					String.class, "value", HtmlConverter.instance()));
			binder.registerBinder("customValues", BeanCollectionBinder
					.instance(customValueBinder, "customValues"));

			final BeanBinder<GuaranteeFeeVO> issueFeeBinder = BeanBinder
					.instance(GuaranteeFeeVO.class, "issueFeeSpec");
			issueFeeBinder.registerBinder("type",
					PropertyBinder.instance(FeeType.class, "type"));
			issueFeeBinder.registerBinder("fee",
					PropertyBinder.instance(BigDecimal.class, "fee",
							localSettings.getNumberConverter()));
			binder.registerBinder("issueFeeSpec", issueFeeBinder);

			final BeanBinder<GuaranteeFeeVO> creditFeeBinder = BeanBinder
					.instance(GuaranteeFeeVO.class, "creditFeeSpec");
			creditFeeBinder.registerBinder("type",
					PropertyBinder.instance(FeeType.class, "type"));
			creditFeeBinder.registerBinder("fee",
					PropertyBinder.instance(BigDecimal.class, "fee",
							localSettings.getNumberConverter()));
			binder.registerBinder("creditFeeSpec", creditFeeBinder);

			dataBinder = binder;
		}
		return dataBinder;
	}

	private DataBinder<Guarantee> getReadDataBinder() {
		if (readDataBinder == null) {
			readDataBinder = getDataBinder();
			dataBinder = null;
			final BeanBinder<Guarantee> beanBinder = (BeanBinder<Guarantee>) readDataBinder;
			beanBinder.getMappings().remove("customValues");
		}
		return readDataBinder;
	}

	private void updateGuarantee(final AuthorizeGuaranteeRequestDto form,
			final Guarantee guarantee) {
		final Guarantee updatedGuarantee = getDataBinder().readFromString(
				form.getGuarantee());

		guarantee.setValidity(updatedGuarantee.getValidity());
		guarantee.setCustomValues(updatedGuarantee.getCustomValues());
		if (form.isAdmin()
				&& !guarantee.getGuaranteeType().getCreditFee().isReadonly()) { // only
																				// the
																				// admin
																				// can
																				// change
																				// the
																				// credit
																				// fee
			guarantee.setCreditFeeSpec(updatedGuarantee.getCreditFeeSpec());
		}
		if (!guarantee.getGuaranteeType().getIssueFee().isReadonly()) {
			guarantee.setIssueFeeSpec(updatedGuarantee.getIssueFeeSpec());
		}
	}
}
