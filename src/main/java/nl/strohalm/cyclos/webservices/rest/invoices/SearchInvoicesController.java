package nl.strohalm.cyclos.webservices.rest.invoices;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.servlet.http.HttpServletRequest;

import nl.strohalm.cyclos.annotations.Inject;
import nl.strohalm.cyclos.controls.ActionContext;
import nl.strohalm.cyclos.controls.invoices.SearchInvoicesForm;
import nl.strohalm.cyclos.entities.EntityReference;
import nl.strohalm.cyclos.entities.accounts.AccountOwner;
import nl.strohalm.cyclos.entities.accounts.SystemAccountOwner;
import nl.strohalm.cyclos.entities.accounts.transactions.Invoice;
import nl.strohalm.cyclos.entities.accounts.transactions.InvoiceQuery;
import nl.strohalm.cyclos.entities.accounts.transactions.TransferType;
import nl.strohalm.cyclos.entities.accounts.transactions.TransferTypeQuery;
import nl.strohalm.cyclos.entities.groups.AdminGroup;
import nl.strohalm.cyclos.entities.members.Element;
import nl.strohalm.cyclos.entities.members.Member;
import nl.strohalm.cyclos.entities.members.Operator;
import nl.strohalm.cyclos.entities.members.OperatorQuery;
import nl.strohalm.cyclos.entities.settings.LocalSettings;
import nl.strohalm.cyclos.entities.settings.events.LocalSettingsEvent;
import nl.strohalm.cyclos.exceptions.PermissionDeniedException;
import nl.strohalm.cyclos.services.elements.ElementService;
import nl.strohalm.cyclos.services.groups.GroupService;
import nl.strohalm.cyclos.services.settings.SettingsService;
import nl.strohalm.cyclos.services.transactions.InvoiceService;
import nl.strohalm.cyclos.services.transactions.TransactionContext;
import nl.strohalm.cyclos.services.transfertypes.TransferTypeService;
import nl.strohalm.cyclos.utils.RequestHelper;
import nl.strohalm.cyclos.utils.TransformedIteratorList;
import nl.strohalm.cyclos.utils.binding.BeanBinder;
import nl.strohalm.cyclos.utils.binding.DataBinder;
import nl.strohalm.cyclos.utils.binding.DataBinderHelper;
import nl.strohalm.cyclos.utils.binding.PropertyBinder;
import nl.strohalm.cyclos.utils.conversion.AccountOwnerConverter;
import nl.strohalm.cyclos.utils.conversion.ReferenceConverter;
import nl.strohalm.cyclos.utils.conversion.Transformer;
import nl.strohalm.cyclos.utils.query.Page;
import nl.strohalm.cyclos.utils.query.PageImpl;
import nl.strohalm.cyclos.utils.query.QueryParameters;
import nl.strohalm.cyclos.utils.validation.ValidationException;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class SearchInvoicesController extends BaseRestController {
	private ElementService elementService;
	private SettingsService settingsService;
	private GroupService groupService;

	public static class Entry {
		private Invoice invoice;
		private Member relatedMember;
		private String relatedName;

		public Invoice getInvoice() {
			return invoice;
		}

		public Member getRelatedMember() {
			return relatedMember;
		}

		public String getRelatedName() {
			return relatedName;
		}

		public void setInvoice(final Invoice invoice) {
			this.invoice = invoice;
		}

		public void setRelatedMember(final Member relatedMember) {
			this.relatedMember = relatedMember;
		}

		public void setRelatedName(final String relatedName) {
			this.relatedName = relatedName;
		}
	}

	public class TransformInvoiceInEntry implements Transformer<Invoice, Entry> {

		private AccountOwner owner;
		private LocalSettings localSettings;

		public TransformInvoiceInEntry(final AccountOwner owner) {
			this.owner = owner;
			localSettings = settingsService.getLocalSettings();
		}

		@Override
		public Entry transform(final Invoice invoice) {
			final Entry entry = new Entry();
			entry.setInvoice(invoice);
			final AccountOwner from = invoice.getFrom();
			final AccountOwner to = invoice.getTo();
			final AccountOwner related = owner.equals(from) ? to : from;
			if (related instanceof Member) {
				try {
					entry.setRelatedMember((Member) elementService
							.load(((Member) related).getId()));
				} catch (final PermissionDeniedException e) {
					// Ok. The logged member cannot see this member
				}
			}
			if (entry.getRelatedMember() == null) {
				if (related instanceof Member) {
					entry.setRelatedName(((Member) related).getUsername());
				} else {
					entry.setRelatedName(localSettings.getApplicationUsername());
				}
			}
			return entry;
		}

	}

	private DataBinder<InvoiceQuery> dataBinder;

	private InvoiceService invoiceService;
	private TransferTypeService transferTypeService;

	private ReadWriteLock lock = new ReentrantReadWriteLock(true);

	public DataBinder<InvoiceQuery> getDataBinder() {
		try {
			lock.readLock().lock();
			if (dataBinder == null) {
				final LocalSettings localSettings = settingsService
						.getLocalSettings();
				final BeanBinder<InvoiceQuery> binder = BeanBinder
						.instance(InvoiceQuery.class);
				binder.registerBinder("owner", PropertyBinder.instance(
						AccountOwner.class, "owner",
						AccountOwnerConverter.instance()));
				binder.registerBinder("direction", PropertyBinder.instance(
						InvoiceQuery.Direction.class, "direction"));
				binder.registerBinder("status",
						PropertyBinder.instance(Invoice.Status.class, "status"));
				binder.registerBinder("transferType", PropertyBinder.instance(
						TransferType.class, "transferType",
						ReferenceConverter.instance(TransferType.class)));
				binder.registerBinder("period",
						DataBinderHelper.periodBinder(localSettings, "period"));
				binder.registerBinder("relatedOwner", PropertyBinder.instance(
						AccountOwner.class, "relatedMemberId",
						AccountOwnerConverter.zeroIsSystemInstance()));
				binder.registerBinder("description",
						PropertyBinder.instance(String.class, "description"));
				binder.registerBinder("transactionNumber", PropertyBinder
						.instance(String.class, "transactionNumber"));
				binder.registerBinder("by", PropertyBinder.instance(
						Element.class, "by",
						ReferenceConverter.instance(Element.class)));
				binder.registerBinder("pageParameters",
						DataBinderHelper.pageBinder());
				dataBinder = binder;
			}
			return dataBinder;
		} finally {
			lock.readLock().unlock();
		}
	}

	public InvoiceService getInvoiceService() {
		return invoiceService;
	}

	public TransferTypeService getTransferTypeService() {
		return transferTypeService;
	}

	public void onLocalSettingsUpdate(final LocalSettingsEvent event) {
		try {
			lock.writeLock().lock();
			// super.onLocalSettingsUpdate(event);
			dataBinder = null;
		} finally {
			lock.writeLock().unlock();
		}
	}

	@Inject
	public void setInvoiceService(final InvoiceService invoiceService) {
		this.invoiceService = invoiceService;
	}

	@Inject
	public void setTransferTypeService(
			final TransferTypeService transferTypeService) {
		this.transferTypeService = transferTypeService;
	}

	public static class SearchInvoicesRequestDto {

	}

	public static class SearchInvoicesResponseDto {
		private List<Entry> entries;

		public void setEntries(List<Entry> entries) {
			this.entries = entries;
		}

	}

	@RequestMapping(value = "", method = RequestMethod.POST)
	@ResponseBody
	protected SearchInvoicesResponseDto executeQuery(
			@RequestBody SearchInvoicesRequestDto form,
			final QueryParameters queryParameters) {
		final InvoiceQuery query = (InvoiceQuery) queryParameters;
		final List<Invoice> invoices = invoiceService.search(query);
		final TransformInvoiceInEntry transformer = new TransformInvoiceInEntry(
				query.getOwner());
		List<Entry> entries = new TransformedIteratorList<Invoice, Entry>(
				transformer, invoices);
		if (invoices instanceof Page<?>) {
			final Page<Invoice> page = (Page<Invoice>) invoices;
			entries = new PageImpl<Entry>(queryParameters.getPageParameters(),
					page.getTotalCount(), new LinkedList<Entry>(entries));
		}
		SearchInvoicesResponseDto response = new SearchInvoicesResponseDto();
		response.setEntries(entries);
		return response;
	}

	protected QueryParameters prepareForm(final ActionContext context) {
		final HttpServletRequest request = context.getRequest();
		final SearchInvoicesForm form = context.getForm();
		final InvoiceQuery query = getDataBinder().readFromString(
				form.getQuery());

		final Element loggedElement = context.getElement();
		// Set the initial parameters
		if (query.getOwner() == null) {
			final AccountOwner owner = context.getAccountOwner();
			query.setOwner(owner);
			form.setQuery("memberId",
					owner instanceof Member ? ((Member) owner).getId()
							.toString() : "0");
		}
		if (query.getDirection() == null) {
			query.setDirection(InvoiceQuery.Direction.INCOMING);
			form.setQuery("direction", InvoiceQuery.Direction.INCOMING.name());
		}
		if (context.isAdmin()) {
			AdminGroup adminGroup = context.getGroup();
			adminGroup = groupService.load(adminGroup.getId(),
					AdminGroup.Relationships.MANAGES_GROUPS);
			query.setGroups(adminGroup.getManagesGroups());
			form.setAdvanced(true);
		}

		// Retrieve the data we need
		Member member = null;
		boolean myInvoices = false;
		boolean byBroker = false;
		boolean byOperator = false;
		final AccountOwner owner = query.getOwner();
		if (owner instanceof SystemAccountOwner) {
			if (context.isAdmin()) {
				myInvoices = true;
			} else {
				throw new ValidationException();
			}
		} else {
			member = elementService.load(((Member) owner).getId(),
					Element.Relationships.USER);
			myInvoices = loggedElement.equals(member);
			byBroker = context.isBrokerOf(member);
			byOperator = context.isOperator()
					&& member.equals(((Operator) loggedElement).getMember());
			if (!context.isAdmin() && !myInvoices && !byBroker && !byOperator) {
				throw new ValidationException();
			}
		}

		// Search the possible transfer types
		final TransferTypeQuery ttQuery = new TransferTypeQuery();
		ttQuery.setContext(TransactionContext.PAYMENT);
		ttQuery.setFromOrToOwner(owner);
		if (context.isAdmin()) {
			AdminGroup adminGroup = context.getGroup();
			adminGroup = groupService.load(adminGroup.getId(),
					AdminGroup.Relationships.MANAGES_GROUPS);
			ttQuery.setFromOrToGroups(adminGroup.getManagesGroups());
		}
		final List<TransferType> transferTypes = transferTypeService
				.search(ttQuery);

		// Fetch the query entities
		if (query.getRelatedMember() instanceof EntityReference) {
			query.setRelatedOwner((Member) elementService.load(query
					.getRelatedMember().getId(), Element.Relationships.USER));
		}
		if (query.getTransferType() instanceof EntityReference) {
			query.setTransferType(transferTypeService.load(query
					.getTransferType().getId()));
		}

		// Lists the operators when member
		if (context.isMember() && form.isAdvanced()) {
			final OperatorQuery oq = new OperatorQuery();
			oq.setMember((Member) context.getElement());
			final List<? extends Element> operators = elementService.search(oq);
			request.setAttribute("operators", operators);
		}

		// Store the request attributes
		request.setAttribute("myInvoices", myInvoices);
		request.setAttribute("member", member);
		request.setAttribute("byBroker", byBroker);
		request.setAttribute("transferTypes", transferTypes);
		RequestHelper.storeEnum(request, InvoiceQuery.Direction.class,
				"directions");
		RequestHelper.storeEnum(request, Invoice.Status.class, "status");

		form.setMemberId((member == null) ? 0L : member.getId());
		return query;
	}

	protected boolean willExecuteQuery(final ActionContext context,
			final QueryParameters queryParameters) throws Exception {
		// The query is always executed
		return true;
	}

}
