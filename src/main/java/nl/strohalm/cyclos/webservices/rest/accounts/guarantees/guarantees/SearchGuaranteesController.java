package nl.strohalm.cyclos.webservices.rest.accounts.guarantees.guarantees;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import nl.strohalm.cyclos.access.AdminMemberPermission;
import nl.strohalm.cyclos.annotations.Inject;
import nl.strohalm.cyclos.controls.ActionContext;
import nl.strohalm.cyclos.controls.accounts.guarantees.guarantees.SearchGuaranteesForm;
import nl.strohalm.cyclos.entities.accounts.guarantees.Guarantee;
import nl.strohalm.cyclos.entities.accounts.guarantees.GuaranteeQuery;
import nl.strohalm.cyclos.entities.accounts.guarantees.GuaranteeType;
import nl.strohalm.cyclos.entities.accounts.guarantees.GuaranteeTypeQuery;
import nl.strohalm.cyclos.entities.customization.fields.PaymentCustomField;
import nl.strohalm.cyclos.entities.customization.fields.PaymentCustomField.Access;
import nl.strohalm.cyclos.entities.customization.fields.PaymentCustomFieldValue;
import nl.strohalm.cyclos.entities.groups.Group;
import nl.strohalm.cyclos.entities.members.Member;
import nl.strohalm.cyclos.entities.settings.LocalSettings;
import nl.strohalm.cyclos.services.accounts.guarantees.GuaranteeService;
import nl.strohalm.cyclos.services.accounts.guarantees.GuaranteeTypeService;
import nl.strohalm.cyclos.services.customization.PaymentCustomFieldService;
import nl.strohalm.cyclos.services.elements.ElementService;
import nl.strohalm.cyclos.services.groups.GroupService;
import nl.strohalm.cyclos.services.permissions.PermissionService;
import nl.strohalm.cyclos.services.settings.SettingsService;
import nl.strohalm.cyclos.utils.CustomFieldHelper;
import nl.strohalm.cyclos.utils.EntityHelper;
import nl.strohalm.cyclos.utils.RelationshipHelper;
import nl.strohalm.cyclos.utils.RequestHelper;
import nl.strohalm.cyclos.utils.binding.BeanBinder;
import nl.strohalm.cyclos.utils.binding.BeanCollectionBinder;
import nl.strohalm.cyclos.utils.binding.DataBinder;
import nl.strohalm.cyclos.utils.binding.DataBinderHelper;
import nl.strohalm.cyclos.utils.binding.PropertyBinder;
import nl.strohalm.cyclos.utils.binding.SimpleCollectionBinder;
import nl.strohalm.cyclos.utils.query.QueryParameters;

public class SearchGuaranteesController {
	private DataBinder<GuaranteeQuery> guaranteeDataBinder;
	private GuaranteeService guaranteeService;
	private GuaranteeTypeService guaranteeTypeService;
	private PaymentCustomFieldService paymentCustomFieldService;
	private GroupService groupService;
	private ElementService elementService;
	private PermissionService permissionService;
	private SettingsService settingsService;
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
	public void setGuaranteeTypeService(
			final GuaranteeTypeService guaranteeTypeService) {
		this.guaranteeTypeService = guaranteeTypeService;
	}

	@Inject
	public void setPaymentCustomFieldService(
			final PaymentCustomFieldService paymentCustomFieldService) {
		this.paymentCustomFieldService = paymentCustomFieldService;
	}

	public static class SearchGuaranteesRequestDto {

	}

	public static class SearchGuaranteesResponseDto {
		private List<Guarantee> guarantees;
		boolean executeGuaranteesQuery;

		public SearchGuaranteesResponseDto(List<Guarantee> guarantees,
				boolean executeGuaranteesQuery) {
			super();
			this.guarantees = guarantees;
			this.executeGuaranteesQuery = executeGuaranteesQuery;
		}

	}

	@RequestMapping(value = "/admin/managePasswords", method = RequestMethod.POST)
	@ResponseBody
	protected SearchGuaranteesResponseDto executeQuery(
			@RequestBody SearchGuaranteesRequestDto form,
			final QueryParameters queryParameters) {
		// final HttpServletRequest request = context.getRequest();
		final GuaranteeQuery query = (GuaranteeQuery) queryParameters;
		query.fetch(RelationshipHelper.nested(
				Guarantee.Relationships.GUARANTEE_TYPE,
				GuaranteeType.Relationships.CURRENCY));
		final List<Guarantee> guarantees = guaranteeService.search(query);
		boolean executeGuaranteesQuery = true;
		SearchGuaranteesResponseDto response = new SearchGuaranteesResponseDto(
				guarantees, executeGuaranteesQuery);
		return response;

	}

	
	protected QueryParameters prepareForm(final ActionContext context) {
		final HttpServletRequest request = context.getRequest();
		final SearchGuaranteesForm form = context.getForm();
		final GuaranteeQuery query = getGuaranteeDataBinder().readFromString(
				form.getQuery());

		final boolean isIssuer = (Boolean) context.getSession().getAttribute(
				"isIssuer");
		final boolean isBuyer = (Boolean) context.getSession().getAttribute(
				"isBuyer");
		final boolean isSeller = (Boolean) context.getSession().getAttribute(
				"isSeller");
		final boolean hasViewPermission = permissionService
				.hasPermission(AdminMemberPermission.GUARANTEES_VIEW_GUARANTEES);

		final boolean hasNoRole = !isIssuer && !isSeller && !isBuyer
				&& !hasViewPermission;
		final boolean showIssuer = isSeller || isBuyer || hasViewPermission
				|| hasNoRole;
		final boolean showBuyer = isSeller || isIssuer || hasViewPermission;
		final boolean showSeller = isBuyer || isIssuer || hasViewPermission;
		final boolean showGuaranteeType = isIssuer || hasViewPermission;

		request.setAttribute("hasNoRole", hasNoRole);
		request.setAttribute("showIssuer", showIssuer);
		request.setAttribute("showBuyer", showBuyer);
		request.setAttribute("showSeller", showSeller);
		request.setAttribute("showGuaranteeType", showGuaranteeType);

		request.setAttribute(
				"issuerGroupsId",
				showIssuer ? EntityHelper.toIdsAsString(guaranteeService
						.getIssuers()) : "[]");
		request.setAttribute(
				"buyerGroupsId",
				showBuyer ? EntityHelper.toIdsAsString(guaranteeService
						.getBuyers()) : "[]");
		request.setAttribute(
				"sellerGroupsId",
				showSeller ? EntityHelper.toIdsAsString(guaranteeService
						.getSellers()) : "[]");
		request.setAttribute("hasViewPermission", hasViewPermission);

		// we need to load the filtered member to show the user login and name
		// in the text fields
		query.setIssuer(ensureFilter(query.getIssuer()));
		query.setBuyer(ensureFilter(query.getBuyer()));
		query.setSeller(ensureFilter(query.getSeller()));
		query.setMember(ensureFilter(query.getMember()));

		Collection<GuaranteeType> guaranteeTypes;
		final Group group = groupService.load(context.getGroup().getId(),
				Group.Relationships.GUARANTEE_TYPES);
		if (context.isAdmin()) {
			request.setAttribute("guaranteeTypesToRegister",
					group.getEnabledGuaranteeTypes());
			guaranteeTypes = guaranteeTypeService
					.search(new GuaranteeTypeQuery());
		} else {
			guaranteeTypes = group.getGuaranteeTypes();
		}

		request.setAttribute("guaranteeTypes", guaranteeTypes);
		final Collection<GuaranteeType> guaranteeTypesWithBuyerOnly = filterWithBuyerOnly(guaranteeTypes);
		request.setAttribute("guaranteeTypeIdsWithBuyerOnly",
				EntityHelper.toIdsAsString(guaranteeTypesWithBuyerOnly));

		RequestHelper.storeEnum(request, Guarantee.Status.class,
				"guaranteeStatuses");

		if (RequestHelper.isFromMenu(request)) { // reset the flag
			context.getSession().setAttribute("executeGuaranteesQuery", false);
		}

		// Get the custom fields for search
		Collection<PaymentCustomField> customFields;
		if (query.getGuaranteeType() != null) {
			final GuaranteeType guaranteeType = guaranteeTypeService.load(query
					.getGuaranteeType().getId());
			customFields = paymentCustomFieldService.list(
					guaranteeType.getLoanTransferType(), true);
		} else {
			customFields = new LinkedHashSet<PaymentCustomField>();
			for (final GuaranteeType guaranteeType : guaranteeTypes) {
				customFields.addAll(paymentCustomFieldService.list(
						guaranteeType.getLoanTransferType(), true));
			}
		}
		for (final Iterator<PaymentCustomField> iterator = customFields
				.iterator(); iterator.hasNext();) {
			final Access searchAccess = iterator.next().getSearchAccess();
			if (searchAccess == null
					|| searchAccess == PaymentCustomField.Access.NONE) {
				// Not for search. Remove from list
				iterator.remove();
			}
		}
		request.setAttribute(
				"customFields",
				customFieldHelper.buildEntries(customFields,
						query.getCustomValues()));

		return query;
	}


	private Member ensureFilter(final Member filter) {
		return filter == null ? null : (Member) elementService.load(filter
				.getId());
	}

	private Collection<GuaranteeType> filterWithBuyerOnly(
			final Collection<GuaranteeType> guaranteeTypes) {
		final Collection<GuaranteeType> result = new ArrayList<GuaranteeType>();

		for (final GuaranteeType gt : guaranteeTypes) {
			if (gt.getModel() == GuaranteeType.Model.WITH_BUYER_ONLY) {
				result.add(gt);
			}
		}
		return result;
	}

	private DataBinder<GuaranteeQuery> getGuaranteeDataBinder() {
		if (guaranteeDataBinder == null) {
			final BeanBinder<PaymentCustomFieldValue> customValueBinder = BeanBinder
					.instance(PaymentCustomFieldValue.class);
			customValueBinder.registerBinder("field",
					PropertyBinder.instance(PaymentCustomField.class, "field"));
			customValueBinder.registerBinder("value",
					PropertyBinder.instance(String.class, "value"));

			final LocalSettings localSettings = settingsService
					.getLocalSettings();
			final BeanBinder<GuaranteeQuery> binder = BeanBinder
					.instance(GuaranteeQuery.class);
			binder.registerBinder("statusList", SimpleCollectionBinder
					.instance(Guarantee.Status.class, "statusList"));
			binder.registerBinder("issuer",
					PropertyBinder.instance(Member.class, "issuer"));
			binder.registerBinder("buyer",
					PropertyBinder.instance(Member.class, "buyer"));
			binder.registerBinder("seller",
					PropertyBinder.instance(Member.class, "seller"));
			binder.registerBinder("member",
					PropertyBinder.instance(Member.class, "member"));
			binder.registerBinder("withBuyerOnly",
					PropertyBinder.instance(Boolean.class, "withBuyerOnly"));
			binder.registerBinder("startIn",
					DataBinderHelper.periodBinder(localSettings, "starts"));
			binder.registerBinder("endIn",
					DataBinderHelper.periodBinder(localSettings, "expires"));
			binder.registerBinder("amountLowerLimit", PropertyBinder.instance(
					BigDecimal.class, "amountLowerLimit",
					localSettings.getNumberConverter()));
			binder.registerBinder("amountUpperLimit", PropertyBinder.instance(
					BigDecimal.class, "amountUpperLimit",
					localSettings.getNumberConverter()));
			binder.registerBinder("guaranteeType", PropertyBinder.instance(
					GuaranteeType.class, "guaranteeType"));
			binder.registerBinder("customValues", BeanCollectionBinder
					.instance(customValueBinder, "customValues"));
			binder.registerBinder("pageParameters",
					DataBinderHelper.pageBinder());
			guaranteeDataBinder = binder;
		}
		return guaranteeDataBinder;
	}
}
