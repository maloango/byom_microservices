package nl.strohalm.cyclos.webservices.rest.invoices;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;


import nl.strohalm.cyclos.annotations.Inject;
import nl.strohalm.cyclos.controls.ActionContext;
import nl.strohalm.cyclos.entities.EntityReference;
import nl.strohalm.cyclos.entities.accounts.AccountOwner;
import nl.strohalm.cyclos.entities.accounts.SystemAccountOwner;
import nl.strohalm.cyclos.entities.accounts.transactions.Invoice;
import nl.strohalm.cyclos.entities.accounts.transactions.InvoiceQuery;
import nl.strohalm.cyclos.entities.accounts.transactions.TransferType;
import nl.strohalm.cyclos.entities.accounts.transactions.TransferTypeQuery;
import nl.strohalm.cyclos.entities.groups.AdminGroup;
import nl.strohalm.cyclos.entities.groups.MemberGroup;
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
import nl.strohalm.cyclos.utils.Period;
import nl.strohalm.cyclos.utils.TransformedIteratorList;
import nl.strohalm.cyclos.utils.access.LoggedUser;
import nl.strohalm.cyclos.utils.binding.BeanBinder;
import nl.strohalm.cyclos.utils.binding.DataBinder;
import nl.strohalm.cyclos.utils.binding.DataBinderHelper;
import nl.strohalm.cyclos.utils.binding.PropertyBinder;
import nl.strohalm.cyclos.utils.conversion.AccountOwnerConverter;
import nl.strohalm.cyclos.utils.conversion.CoercionHelper;
import nl.strohalm.cyclos.utils.conversion.ReferenceConverter;
import nl.strohalm.cyclos.utils.conversion.Transformer;
import nl.strohalm.cyclos.utils.query.Page;
import nl.strohalm.cyclos.utils.query.PageImpl;
import nl.strohalm.cyclos.utils.query.QueryParameters;
import nl.strohalm.cyclos.utils.validation.ValidationException;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;
import static org.apache.commons.httpclient.util.URIUtil.getQuery;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class SearchInvoicesController extends BaseRestController {
	private ElementService elementService;
	private SettingsService settingsService;
	private GroupService groupService;

	public final ElementService getElementService() {
		return elementService;
	}

	public final void setElementService(ElementService elementService) {
		this.elementService = elementService;
	}

	public final SettingsService getSettingsService() {
		return settingsService;
	}

	public final void setSettingsService(SettingsService settingsService) {
		this.settingsService = settingsService;
	}

	public final GroupService getGroupService() {
		return groupService;
	}

	public final void setGroupService(GroupService groupService) {
		this.groupService = groupService;
	}

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

        public AccountOwner getOwner() {
            return owner;
        }

        public void setOwner(AccountOwner owner) {
            this.owner = owner;
        }

        public LocalSettings getLocalSettings() {
            return localSettings;
        }

        public void setLocalSettings(LocalSettings localSettings) {
            this.localSettings = localSettings;
        }
                
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
            private AccountOwner            owner;
    private AccountOwner            relatedOwner;
    private Collection<MemberGroup> groups;
    private String                  description;
    private InvoiceQuery.Direction               direction;
    private Period                  period;
    private Period                  paymentPeriod;
    private Invoice.Status          status;
    private TransferType            transferType;
    private String                  transactionNumber;
    private Element                 by;
    private boolean                 advanced;
    
    

    

    public long getMemberId() {
        return CoercionHelper.coerce(Long.TYPE, getQuery("owner"));
    }

    public boolean isAdvanced() {
        return advanced;
    }

    public void setAdvanced(final boolean advanced) {
        this.advanced = advanced;
    }

    public void setMemberId(final long memberId) {
        setQuery("owner", memberId);
    }

    public Element getBy() {
        return by;
    }

    public String getDescription() {
        return description;
    }

    public InvoiceQuery.Direction getDirection() {
        return direction;
    }

    public Collection<MemberGroup> getGroups() {
        return groups;
    }

    public Member getMember() {
        return owner instanceof Member ? (Member) owner : null;
    }

    public AccountOwner getOwner() {
        return owner;
    }

    public Period getPaymentPeriod() {
        return paymentPeriod;
    }

    public Period getPeriod() {
        return period;
    }

    public Member getRelatedMember() {
        return relatedOwner instanceof Member ? (Member) relatedOwner : null;
    }

    public AccountOwner getRelatedOwner() {
        return relatedOwner;
    }

    public Invoice.Status getStatus() {
        return status;
    }

    public String getTransactionNumber() {
        return transactionNumber;
    }

    public TransferType getTransferType() {
        return transferType;
    }

    public void setBy(final Element by) {
        this.by = by;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public void setDirection(final InvoiceQuery.Direction direction) {
        this.direction = direction;
    }

    public void setGroups(final Collection<MemberGroup> groups) {
        this.groups = groups;
    }

    public void setOwner(final AccountOwner owner) {
        this.owner = owner;
    }

    public void setPaymentPeriod(final Period paymentPeriod) {
        this.paymentPeriod = paymentPeriod;
    }

    public void setPeriod(final Period period) {
        this.period = period;
    }

    public void setRelatedOwner(final AccountOwner relatedOwner) {
        this.relatedOwner = relatedOwner;
    }

    public void setStatus(final Invoice.Status status) {
        this.status = status;
    }

    public void setTransactionNumber(final String transactionNumber) {
        this.transactionNumber = transactionNumber;
    }

    public void setTransferType(final TransferType transferType) {
        this.transferType = transferType;
    }

        public void setQuery(String owner, long memberId) {
            
        }

        
	}

	public static class SearchInvoicesResponseDto {
		private List<Entry> entries;

		public void setEntries(List<Entry> entries) {
			this.entries = entries;
		}

	}

	@RequestMapping(value = "admin/searchInvoices", method = RequestMethod.POST)
	@ResponseBody
	protected SearchInvoicesResponseDto executeQuery(
			@RequestBody SearchInvoicesRequestDto form,
			final QueryParameters queryParameters) {
                SearchInvoicesResponseDto response = null;
                try{
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
		response = new SearchInvoicesResponseDto();
		response.setEntries(entries);}
                catch(Exception e){
                    e.printStackTrace();
                }
		return response;
	}
        public static class PrepareFormResponseDTO{
             public HashMap<String,Object> response=new HashMap<String,Object>();
             private boolean myInvoices;
             private boolean member;
             private boolean byBroker;
             private boolean transferTypes;
            
        public boolean isMyInvoices() {
            return myInvoices;
        }

        public void setMyInvoices(boolean myInvoices) {
            this.myInvoices = myInvoices;
        }

        public boolean isMember() {
            return member;
        }

        public void setMember(boolean member) {
            this.member = member;
        }

        public boolean isByBroker() {
            return byBroker;
        }

        public void setByBroker(boolean byBroker) {
            this.byBroker = byBroker;
        }

        public boolean isTransferTypes() {
            return transferTypes;
        }

        public void setTransferTypes(boolean transferTypes) {
            this.transferTypes = transferTypes;
        }
             

        public HashMap<String, Object> getResponse() {
            return response;
        }

        public void setResponse(HashMap<String, Object> response) {
            this.response = response;
        }
             
        }
        @RequestMapping(value = "admin/searchInvoices/{memberId}", method = RequestMethod.GET)
	@ResponseBody
	public PrepareFormResponseDTO prepareForm(@PathVariable ("memberId") long memberId, boolean myInvoices) {
		
            PrepareFormResponseDTO prepareFormRes = new PrepareFormResponseDTO();
            try{
                HashMap<String,Object> response=new HashMap<String,Object>();
            
		
            final InvoiceQuery query = getDataBinder().readFromString(memberId);
				

		final Element loggedElement =  LoggedUser.element();  //WebServiceContext.g
		// Set the initial parameters
		if (query.getOwner() == null) {
			final AccountOwner owner = LoggedUser.accountOwner();//context.getAccountOwner();
			query.setOwner(owner);
			//query.setQuery("memberId",owner instanceof Member ? ((Member) owner).getId()
							
					
		}
		if (query.getDirection() == null) {
			query.setDirection(InvoiceQuery.Direction.INCOMING);
			//form.setQuery("direction", InvoiceQuery.Direction.INCOMING.name());
		}
		if (LoggedUser.isAdministrator()) {
			AdminGroup adminGroup = LoggedUser.group();
			adminGroup = groupService.load(adminGroup.getId(),
					AdminGroup.Relationships.MANAGES_GROUPS);
			query.setGroups(adminGroup.getManagesGroups());
			//form.setAdvanced(true);
		}

		// Retrieve the data we need
		Member member = null;
		//boolean myInvoices = false;
		boolean byBroker = false;
		boolean byOperator = false;
		final AccountOwner owner = query.getOwner();
		if (owner instanceof SystemAccountOwner) {
			if (LoggedUser.isAdministrator()) {
				myInvoices = true;
			} else {
				throw new ValidationException();
			}
		} else {
			member = elementService.load(((Member) owner).getId(),
					Element.Relationships.USER);
			myInvoices = loggedElement.equals(member);
			//byBroker = LoggedUser;
			byOperator = LoggedUser.isOperator()
					&& member.equals(((Operator) loggedElement).getMember());
			if (!LoggedUser.isAdministrator() && !myInvoices && !byBroker && !byOperator) {
				throw new ValidationException();
			}
		}

		// Search the possible transfer types
		final TransferTypeQuery ttQuery = new TransferTypeQuery();
		ttQuery.setContext(TransactionContext.PAYMENT);
		ttQuery.setFromOrToOwner(owner);
		if (LoggedUser.isAdministrator()) {
			AdminGroup adminGroup = LoggedUser.group();
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
		if (LoggedUser.isAdministrator()&& LoggedUser.isMember()) {
			final OperatorQuery oq = new OperatorQuery();
			oq.setMember((Member) LoggedUser.element());
			final List<? extends Element> operators = elementService.search(oq);
			response.put("operators", operators);
		}

		// Store the request attributes
		response.put("myInvoices", myInvoices);
		response.put("member", member);
		response.put("byBroker", byBroker);
		response.put("transferTypes", transferTypes);
		
		
		loggedElement.getId();
		prepareFormRes.setResponse(response);
            }
                catch(Exception e){
                        
                        }
            return prepareFormRes;
        }
	

	protected boolean willExecuteQuery(final ActionContext context,
			final QueryParameters queryParameters) throws Exception {
		// The query is always executed
		return true;
	}

}
