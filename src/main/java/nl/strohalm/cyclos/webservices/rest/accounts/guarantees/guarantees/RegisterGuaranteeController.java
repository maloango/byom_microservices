package nl.strohalm.cyclos.webservices.rest.accounts.guarantees.guarantees;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import nl.strohalm.cyclos.annotations.Inject;
import nl.strohalm.cyclos.controls.ActionContext;
import nl.strohalm.cyclos.controls.accounts.guarantees.guarantees.RegisterGuaranteeForm;
import nl.strohalm.cyclos.entities.accounts.guarantees.Guarantee;
import nl.strohalm.cyclos.entities.accounts.guarantees.GuaranteeType;
import nl.strohalm.cyclos.entities.accounts.guarantees.GuaranteeType.FeeType;
import nl.strohalm.cyclos.entities.customization.fields.CustomFieldValue;
import nl.strohalm.cyclos.entities.customization.fields.PaymentCustomField;
import nl.strohalm.cyclos.entities.customization.fields.PaymentCustomFieldValue;
import nl.strohalm.cyclos.entities.groups.Group;
import nl.strohalm.cyclos.entities.members.Member;
import nl.strohalm.cyclos.entities.settings.LocalSettings;
import nl.strohalm.cyclos.services.accounts.guarantees.GuaranteeFeeVO;
import nl.strohalm.cyclos.services.accounts.guarantees.GuaranteeService;
import nl.strohalm.cyclos.services.accounts.guarantees.GuaranteeTypeService;
import nl.strohalm.cyclos.services.customization.PaymentCustomFieldService;
import nl.strohalm.cyclos.services.settings.SettingsService;
import nl.strohalm.cyclos.utils.EntityHelper;
import nl.strohalm.cyclos.utils.RequestHelper;
import nl.strohalm.cyclos.utils.binding.BeanBinder;
import nl.strohalm.cyclos.utils.binding.BeanCollectionBinder;
import nl.strohalm.cyclos.utils.binding.DataBinder;
import nl.strohalm.cyclos.utils.binding.DataBinderHelper;
import nl.strohalm.cyclos.utils.binding.PropertyBinder;
import nl.strohalm.cyclos.utils.conversion.HtmlConverter;
import nl.strohalm.cyclos.utils.conversion.IdConverter;
import nl.strohalm.cyclos.utils.validation.ValidationException;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

public class RegisterGuaranteeController {
	private GuaranteeTypeService guaranteeTypeService;
	private GuaranteeService guaranteeService;
	public final SettingsService getSettingsService() {
		return settingsService;
	}

	public final void setSettingsService(SettingsService settingsService) {
		this.settingsService = settingsService;
	}

	public final GuaranteeTypeService getGuaranteeTypeService() {
		return guaranteeTypeService;
	}

	public final GuaranteeService getGuaranteeService() {
		return guaranteeService;
	}

	public final PaymentCustomFieldService getPaymentCustomFieldService() {
		return paymentCustomFieldService;
	}

	private PaymentCustomFieldService paymentCustomFieldService;
	private DataBinder<Guarantee> dataBinder;
	private SettingsService settingsService;

	public void setDataBinder(final DataBinder<Guarantee> dataBinder) {
		this.dataBinder = dataBinder;
	}

	@Inject
	public void setGuaranteeService(final GuaranteeService guaranteeService) {
		this.guaranteeService = guaranteeService;
	}

	@Inject
	public void setGuaranteeTypeService(
			final GuaranteeTypeService guaranteeTypeService) {
		this.guaranteeTypeService = guaranteeTypeService;
	}

	@Inject
	public void setPaymentCustomFieldService(
			final PaymentCustomFieldService paymentCustomFieldService) {
		this.paymentCustomFieldService = paymentCustomFieldService;
	}

	public static class RegisterGuaranteeRequestDto {
		private Long guaranteeTypeId;

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

		public Long getGuaranteeTypeId() {
			return guaranteeTypeId;
		}

		public void setGuarantee(final Map<String, Object> values) {
			this.values = values;
		}

		public void setGuarantee(final String key, final Object value) {
			values.put(key, value);
		}

		public void setGuaranteeTypeId(final Long guaranteeTypeId) {
			this.guaranteeTypeId = guaranteeTypeId;
		}
	}

	public static class RegisterGuaranteeResponseDto {
		private String message;
		Long guaranteeId;

		public RegisterGuaranteeResponseDto(String message, Long guaranteeId) {
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

	@RequestMapping(value = "admin/registerGuarantee{guaranteeTypeId}", method = RequestMethod.GET)
	@ResponseBody
	protected RegisterGuaranteeResponseDto handleSubmit(@PathVariable ("guaranteeTypeId")long guaranteeTypeId) throws Exception {
			
		RegisterGuaranteeResponseDto response =null;
                try{
		Guarantee guarantee = getDataBinder().readFromString(guaranteeTypeId);
				
		final boolean isInsert = guarantee.isTransient();
		guarantee = guaranteeService.registerGuarantee(guarantee);
		String message = null;
		Long guaranteeId = null;
		if (isInsert) {
			message = "guarantee.inserted";
		} else {
			message = "guarantee.modified";
		}
		 response = new RegisterGuaranteeResponseDto(message, guaranteeId);
				}
                catch(Exception e){
                    e.printStackTrace();
                }
		return response;
	}

	/**
	 * Method use to prepare a form for being displayed
	 */

	protected void prepareForm(final ActionContext context) throws Exception {
		final HttpServletRequest request = context.getRequest();
		final RegisterGuaranteeForm form = context.getForm();
		final Long id = form.getGuaranteeTypeId();
		final GuaranteeType guaranteeType = guaranteeTypeService.load(id);

		final Collection<? extends Group> issuers = guaranteeService
				.getIssuers(guaranteeType);
		if (CollectionUtils.isEmpty(issuers)) {
			throw new ValidationException("guarantee.error.noIssuer");
		}
		final Collection<? extends Group> sellers = guaranteeService
				.getSellers();

		request.setAttribute("issuerGroupsId",
				EntityHelper.toIdsAsString(issuers));
		// only with this model we must filter the buyers groups
		if (guaranteeType.getModel() != GuaranteeType.Model.WITH_BUYER_ONLY) {
			final Collection<? extends Group> buyers = guaranteeService
					.getBuyers();
			request.setAttribute("buyerGroupsId",
					EntityHelper.toIdsAsString(buyers));
		}

		final List<PaymentCustomField> customFields = paymentCustomFieldService
				.list(guaranteeType.getLoanTransferType(), false);
		request.setAttribute("customFields", customFields);

		request.setAttribute("sellerGroupsId",
				EntityHelper.toIdsAsString(sellers));
		request.setAttribute(
				"isWithBuyerAndSeller",
				guaranteeType.getModel() == GuaranteeType.Model.WITH_BUYER_AND_SELLER);
		request.setAttribute("guaranteeType", guaranteeType);
		RequestHelper.storeEnum(request, GuaranteeType.FeeType.class,
				"feeTypes");
	}

	protected void validateForm(final ActionContext context) {
		final RegisterGuaranteeForm form = context.getForm();
		final Guarantee guarantee = getDataBinder().readFromString(
				form.getGuarantee());
		guaranteeService.validate(guarantee, false);
	}

	private DataBinder<Guarantee> getDataBinder() {
		if (dataBinder == null) {

			final BeanBinder<Guarantee> binder = BeanBinder
					.instance(Guarantee.class);
			final LocalSettings localSettings = settingsService
					.getLocalSettings();
			binder.registerBinder(
					"id",
					PropertyBinder.instance(Long.class, "id",
							IdConverter.instance()));
			binder.registerBinder("buyer",
					PropertyBinder.instance(Member.class, "buyer"));
			binder.registerBinder("seller",
					PropertyBinder.instance(Member.class, "seller"));
			binder.registerBinder("issuer",
					PropertyBinder.instance(Member.class, "issuer"));
			binder.registerBinder("amount", PropertyBinder.instance(
					BigDecimal.class, "amount",
					localSettings.getNumberConverter()));
			binder.registerBinder("guaranteeType", PropertyBinder.instance(
					GuaranteeType.class, "guaranteeType"));
			binder.registerBinder("validity",
					DataBinderHelper.rawPeriodBinder(localSettings, "validity"));

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
}
