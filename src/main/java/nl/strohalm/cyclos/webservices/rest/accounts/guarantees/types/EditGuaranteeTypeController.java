package nl.strohalm.cyclos.webservices.rest.accounts.guarantees.types;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import nl.strohalm.cyclos.access.AdminSystemPermission;
import nl.strohalm.cyclos.annotations.Inject;
import nl.strohalm.cyclos.controls.ActionContext;
//import nl.strohalm.cyclos.controls.accounts.guarantees.types.EditGuaranteeTypeForm;
import nl.strohalm.cyclos.entities.accounts.AccountType;
import nl.strohalm.cyclos.entities.accounts.Currency;
import nl.strohalm.cyclos.entities.accounts.guarantees.GuaranteeType;
import nl.strohalm.cyclos.entities.accounts.guarantees.GuaranteeType.FeeType;
import nl.strohalm.cyclos.entities.accounts.transactions.TransferType;
import nl.strohalm.cyclos.entities.accounts.transactions.TransferTypeQuery;
import nl.strohalm.cyclos.entities.settings.LocalSettings;
import nl.strohalm.cyclos.services.accounts.CurrencyService;
import nl.strohalm.cyclos.services.accounts.guarantees.GuaranteeTypeFeeVO;
import nl.strohalm.cyclos.services.accounts.guarantees.GuaranteeTypeService;
import nl.strohalm.cyclos.services.permissions.PermissionService;
import nl.strohalm.cyclos.services.settings.SettingsService;
import nl.strohalm.cyclos.services.transactions.TransactionContext;
import nl.strohalm.cyclos.services.transfertypes.TransferTypeService;
import nl.strohalm.cyclos.utils.RequestHelper;
import nl.strohalm.cyclos.utils.TimePeriod;
import nl.strohalm.cyclos.utils.binding.BeanBinder;
import nl.strohalm.cyclos.utils.binding.DataBinder;
import nl.strohalm.cyclos.utils.binding.DataBinderHelper;
import nl.strohalm.cyclos.utils.binding.PropertyBinder;
import nl.strohalm.cyclos.utils.conversion.IdConverter;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class EditGuaranteeTypeController extends BaseRestController {

	private interface StringTransformer {
		String transform(Object obj);
	}

	private GuaranteeTypeService guaranteeTypeService;
	private CurrencyService currencyService;
	public final PermissionService getPermissionService() {
		return permissionService;
	}

	public final void setPermissionService(PermissionService permissionService) {
		this.permissionService = permissionService;
	}

	public final SettingsService getSettingsService() {
		return settingsService;
	}

	public final void setSettingsService(SettingsService settingsService) {
		this.settingsService = settingsService;
	}

	public final GuaranteeTypeService getGuaranteeTypeService() {
		return guaranteeTypeService;
	}

	public final CurrencyService getCurrencyService() {
		return currencyService;
	}

	public final TransferTypeService getTransferTypeService() {
		return transferTypeService;
	}

	private TransferTypeService transferTypeService;
	private PermissionService permissionService;
	private SettingsService settingsService;

	private DataBinder<GuaranteeType> dataBinderGuaranteeType;

	public DataBinder<GuaranteeType> getDataBinderGuaranteeType() {
		if (dataBinderGuaranteeType == null) {
			final LocalSettings localSettings = settingsService
					.getLocalSettings();
			final BeanBinder<GuaranteeType> binder = BeanBinder
					.instance(GuaranteeType.class);
			binder.registerBinder(
					"id",
					PropertyBinder.instance(Long.class, "id",
							IdConverter.instance()));
			binder.registerBinder("name",
					PropertyBinder.instance(String.class, "name"));
			binder.registerBinder("description",
					PropertyBinder.instance(String.class, "description"));
			binder.registerBinder("model",
					PropertyBinder.instance(GuaranteeType.Model.class, "model"));
			binder.registerBinder("authorizedBy", PropertyBinder.instance(
					GuaranteeType.AuthorizedBy.class, "authorizedBy"));
			binder.registerBinder("creditFeePayer", PropertyBinder.instance(
					GuaranteeType.FeePayer.class, "creditFeePayer"));
			binder.registerBinder("issueFeePayer", PropertyBinder.instance(
					GuaranteeType.FeePayer.class, "issueFeePayer"));
			binder.registerBinder("enabled",
					PropertyBinder.instance(Boolean.TYPE, "enabled"));
			// binder.registerBinder("allowLoanPaymentSetup",
			// PropertyBinder.instance(Boolean.class, "allowLoanPaymentSetup"));
			binder.registerBinder("paymentObligationPeriod", DataBinderHelper
					.timePeriodBinder("paymentObligationPeriod"));
			binder.registerBinder("pendingGuaranteeExpiration",
					DataBinderHelper
							.timePeriodBinder("pendingGuaranteeExpiration"));
			binder.registerBinder("currency",
					PropertyBinder.instance(Currency.class, "currency"));
			binder.registerBinder("creditFeeTransferType", PropertyBinder
					.instance(TransferType.class, "creditFeeTransferType"));
			binder.registerBinder("issueFeeTransferType", PropertyBinder
					.instance(TransferType.class, "issueFeeTransferType"));
			binder.registerBinder("forwardTransferType", PropertyBinder
					.instance(TransferType.class, "forwardTransferType"));
			binder.registerBinder("loanTransferType", PropertyBinder.instance(
					TransferType.class, "loanTransferType"));

			final BeanBinder<GuaranteeTypeFeeVO> issueFeeBinder = BeanBinder
					.instance(GuaranteeTypeFeeVO.class, "issueFee");
			issueFeeBinder.registerBinder("type",
					PropertyBinder.instance(FeeType.class, "type"));
			issueFeeBinder.registerBinder("fee",
					PropertyBinder.instance(BigDecimal.class, "fee",
							localSettings.getNumberConverter()));
			issueFeeBinder.registerBinder("readonly",
					PropertyBinder.instance(Boolean.TYPE, "readonly"));
			binder.registerBinder("issueFee", issueFeeBinder);

			final BeanBinder<GuaranteeTypeFeeVO> creditFeeBinder = BeanBinder
					.instance(GuaranteeTypeFeeVO.class, "creditFee");
			creditFeeBinder.registerBinder("type",
					PropertyBinder.instance(FeeType.class, "type"));
			creditFeeBinder.registerBinder("fee",
					PropertyBinder.instance(BigDecimal.class, "fee",
							localSettings.getNumberConverter()));
			creditFeeBinder.registerBinder("readonly",
					PropertyBinder.instance(Boolean.TYPE, "readonly"));
			binder.registerBinder("creditFee", creditFeeBinder);
			dataBinderGuaranteeType = binder;
		}
		return dataBinderGuaranteeType;
	}

	@Inject
	public void setCurrencyService(final CurrencyService currencyService) {
		this.currencyService = currencyService;
	}

	@Inject
	public void setGuaranteeTypeService(
			final GuaranteeTypeService guaranteeTypeService) {
		this.guaranteeTypeService = guaranteeTypeService;
	}

	@Inject
	public void setTransferTypeService(
			final TransferTypeService transferTypeService) {
		this.transferTypeService = transferTypeService;
	}

	public static class EditGuaranteeTypeRequestDto {
		private Long guaranteeTypeId;

		public Long getGuaranteeTypeId() {
			return guaranteeTypeId;
		}

		public void setGuaranteeTypeId(Long guaranteeTypeId) {
			this.guaranteeTypeId = guaranteeTypeId;
		}

		protected Map<String, Object> values;

		public Map<String, Object> getValues() {
			return values;
		}

		public void setValues(final Map<String, Object> values) {
			this.values = values;
		}

		public Map<String, Object> getGuaranteeType() {
			return values;
		}
	}

	public static class EditGuaranteeTypeResponseDto {
		public String message;

		public String getMessage() {
			return message;
		}

		public void setMessage(String message) {
			this.message = message;
		}
                public EditGuaranteeTypeResponseDto(){
                }

	}

	/**
	 * Handles form submission, returning the ActionForward
     * @param guaranteeTypeId
     * @return 
     * @throws java.lang.Exception
	 */
	@RequestMapping(value = "admin/editGuaranteeType/{guaranteeTypeId}", method = RequestMethod.GET)
	@ResponseBody
	protected EditGuaranteeTypeResponseDto handleSubmit(@PathVariable ("guaranteeTypeId")long guaranteeTypeId) throws Exception {
			
		
		GuaranteeType guaranteeType = getDataBinderGuaranteeType()
				.readFromString(guaranteeTypeId);
		final boolean isInsert = guaranteeType.isTransient();
		guaranteeType = guaranteeTypeService.save(guaranteeType);
		EditGuaranteeTypeResponseDto response = new EditGuaranteeTypeResponseDto();
		if (isInsert) {
			response.setMessage("guaranteeType.inserted");
		} else {
			response.setMessage("guaranteeType.updated");
		}
		return response;
	}

	/**
	 * Method use to prepare a form for being displayed
     * @param context
     * @throws java.lang.Exception
	 */
//not required..
//	protected void prepareForm(final ActionContext context) throws Exception {
//		final HttpServletRequest request = context.getRequest();
//		final EditGuaranteeTypeForm form = context.getForm();
//		final Long id = form.getGuaranteeTypeId();
//
//		final boolean isInsert = id == null || id <= 0L;
//		if (!isInsert) {
//			final GuaranteeType guaranteeType = guaranteeTypeService.load(id,
//					GuaranteeType.Relationships.CURRENCY,
//					GuaranteeType.Relationships.LOAN_TRANSFER_TYPE,
//					GuaranteeType.Relationships.CREDIT_FEE_TRANSFER_TYPE,
//					GuaranteeType.Relationships.ISSUE_FEE_TRANSFER_TYPE,
//					GuaranteeType.Relationships.FORWARD_TRANSFER_TYPE);
//			request.setAttribute("guaranteeType", guaranteeType);
//			final Currency currency = guaranteeType.getCurrency();
//			getDataBinderGuaranteeType().writeAsString(form.getGuaranteeType(),
//					guaranteeType);
//			searchTrasferTypes(request, currency);
//		}
//
//		final StringTransformer javaScriptTransformer = new StringTransformer() {
//			@Override
//			public String transform(final Object value) {
//				return "'" + value.toString() + "'";
//			}
//		};
//
//		final StringTransformer i18nTransformer = new StringTransformer() {
//			@Override
//			public String transform(final Object value) {
//				return javaScriptTransformer.transform(context
//						.message("guaranteeType.authorizedBy." + value));
//			}
//		};
//
//		final GuaranteeType.AuthorizedBy[] paymentObligationAuthorizers = new GuaranteeType.AuthorizedBy[] {
//				GuaranteeType.AuthorizedBy.ISSUER,
//				GuaranteeType.AuthorizedBy.BOTH };
//		request.setAttribute(
//				"allAuthorizersStr",
//				arrayToString(GuaranteeType.AuthorizedBy.values(),
//						javaScriptTransformer));
//		request.setAttribute(
//				"paymentObligationAuthorizersStr",
//				arrayToString(paymentObligationAuthorizers,
//						javaScriptTransformer));
//		request.setAttribute("feePayers", Arrays.asList(
//				GuaranteeType.FeePayer.BUYER, GuaranteeType.FeePayer.SELLER));
//
//		request.setAttribute("paymentObligationAuthorizersI18N",
//				arrayToString(paymentObligationAuthorizers, i18nTransformer));
//		request.setAttribute(
//				"allAuthorizersI18N",
//				arrayToString(GuaranteeType.AuthorizedBy.values(),
//						i18nTransformer));
//		// request.setAttribute("paymentObligationModelIdx",
//		// GuaranteeType.Model.WITH_PAYMENT_OBLIGATION.ordinal());
//		// request.setAttribute("withBuyerOnlyIdx",
//		// GuaranteeType.Model.WITH_BUYER_ONLY.ordinal());
//
//		request.setAttribute("currencies", currencyService.listAll());
//		request.setAttribute("isInsert", isInsert);
//		request.setAttribute("editable", permissionService
//				.hasPermission(AdminSystemPermission.GUARANTEE_TYPES_MANAGE));
//		request.setAttribute("paymentObligationPeriod", Arrays.asList(
//				TimePeriod.Field.DAYS, TimePeriod.Field.MONTHS,
//				TimePeriod.Field.YEARS));
//		request.setAttribute("pendingGuaranteeExpiration", Arrays.asList(
//				TimePeriod.Field.DAYS, TimePeriod.Field.MONTHS,
//				TimePeriod.Field.YEARS));
//
//		RequestHelper.storeEnum(request, GuaranteeType.Model.class, "model");
//		RequestHelper.storeEnum(request, GuaranteeType.AuthorizedBy.class,
//				"allAuthorizers");
//		RequestHelper.storeEnum(request, GuaranteeType.FeeType.class,
//				"feeTypes");
//	}
//
//	protected void validateForm(final ActionContext context) {
//		final EditGuaranteeTypeForm form = context.getForm();
//		final GuaranteeType guaranteeType = getDataBinderGuaranteeType()
//				.readFromString(form.getGuaranteeType());
//		guaranteeTypeService.validate(guaranteeType);
//	}

	private String arrayToString(final Object[] values,
			final StringTransformer transformer) {
		if (values == null || values.length == 0) {
			return "[]";
		}
		final StringBuilder str = new StringBuilder("[");
		for (final Object value : values) {
			str.append(transformer.transform(value)).append(",");
		}

		str.delete(str.length() - 1, str.length()).append("]");
		return str.toString();
	}

	private void searchTrasferTypes(final HttpServletRequest request,
			final Currency currency) {
		final TransferTypeQuery ttQuery = new TransferTypeQuery();
		ttQuery.setCurrency(currency);

		// Credit fee TT query
		ttQuery.setContext(TransactionContext.ANY);
		ttQuery.setFromNature(AccountType.Nature.MEMBER);
		ttQuery.setToNature(AccountType.Nature.SYSTEM);
		request.setAttribute("creditFeeTransferType",
				transferTypeService.search(ttQuery));

		// Issue fee TT query
		ttQuery.setContext(TransactionContext.ANY);
		ttQuery.setFromNature(AccountType.Nature.MEMBER);
		ttQuery.setToNature(AccountType.Nature.MEMBER);
		final List<TransferType> issueFeeQueryResult = transferTypeService
				.search(ttQuery);
		request.setAttribute("issueFeeTransferType", issueFeeQueryResult);

		// Forward TT query
		request.setAttribute("forwardTransferType", issueFeeQueryResult);

		// Loan TT query
		ttQuery.setContext(TransactionContext.AUTOMATIC_LOAN);
		ttQuery.setFromNature(AccountType.Nature.SYSTEM);
		ttQuery.setToNature(AccountType.Nature.MEMBER);
		request.setAttribute("loanTransferType",
				transferTypeService.search(ttQuery));
	}
}
