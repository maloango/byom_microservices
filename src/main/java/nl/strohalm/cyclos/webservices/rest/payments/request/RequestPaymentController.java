package nl.strohalm.cyclos.webservices.rest.payments.request;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import nl.strohalm.cyclos.annotations.Inject;
import nl.strohalm.cyclos.controls.ActionContext;
import nl.strohalm.cyclos.controls.payments.request.RequestPaymentForm;
import nl.strohalm.cyclos.entities.access.Channel;
import nl.strohalm.cyclos.entities.accounts.Currency;
import nl.strohalm.cyclos.entities.accounts.transactions.PaymentRequestTicket;
import nl.strohalm.cyclos.entities.accounts.transactions.Ticket;
import nl.strohalm.cyclos.entities.accounts.transactions.TicketQuery;
import nl.strohalm.cyclos.entities.exceptions.UnexpectedEntityException;
import nl.strohalm.cyclos.entities.groups.MemberGroup;
import nl.strohalm.cyclos.entities.members.Member;
import nl.strohalm.cyclos.entities.settings.LocalSettings;
import nl.strohalm.cyclos.entities.settings.events.LocalSettingsEvent;
import nl.strohalm.cyclos.services.access.ChannelService;
import nl.strohalm.cyclos.services.accounts.CurrencyService;
import nl.strohalm.cyclos.services.groups.GroupService;
import nl.strohalm.cyclos.services.settings.SettingsService;
import nl.strohalm.cyclos.services.transactions.TicketService;
import nl.strohalm.cyclos.services.transactions.exceptions.AuthorizedPaymentInPastException;
import nl.strohalm.cyclos.services.transactions.exceptions.CreditsException;
import nl.strohalm.cyclos.services.transactions.exceptions.InvalidChannelException;
import nl.strohalm.cyclos.utils.ActionHelper;
import nl.strohalm.cyclos.utils.RequestHelper;
import nl.strohalm.cyclos.utils.binding.BeanBinder;
import nl.strohalm.cyclos.utils.binding.DataBinder;
import nl.strohalm.cyclos.utils.binding.PropertyBinder;
import nl.strohalm.cyclos.utils.validation.ValidationException;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;
@Controller
public class RequestPaymentController extends BaseRestController{
	 private ChannelService                   channelService;
	    private CurrencyService                  currencyService;
	    private TicketService                    ticketService;
	    private DataBinder<PaymentRequestTicket> dataBinder;
	    private ReadWriteLock                    lock = new ReentrantReadWriteLock(true);
	    private GroupService groupService;
	    private ActionHelper actionHelper;
	    private SettingsService settingsService;
		private Ticket ticket;
		//private PaymentRequestTicket ticket;
	    

	   public final SettingsService getSettingsService() {
			return settingsService;
		}

		public final void setSettingsService(SettingsService settingsService) {
			this.settingsService = settingsService;
		}

	public final ReadWriteLock getLock() {
			return lock;
		}

		public final void setLock(ReadWriteLock lock) {
			this.lock = lock;
		}

		public final GroupService getGroupService() {
			return groupService;
		}

		public final void setGroupService(GroupService groupService) {
			this.groupService = groupService;
		}

		public final ActionHelper getActionHelper() {
			return actionHelper;
		}

		public final void setActionHelper(ActionHelper actionHelper) {
			this.actionHelper = actionHelper;
		}

		public final ChannelService getChannelService() {
			return channelService;
		}

		public final CurrencyService getCurrencyService() {
			return currencyService;
		}

		public final TicketService getTicketService() {
			return ticketService;
		}

		public final void setDataBinder(DataBinder<PaymentRequestTicket> dataBinder) {
			this.dataBinder = dataBinder;
		}

		// @Override
	    public void onLocalSettingsUpdate(final LocalSettingsEvent event) {
	        try {
	            lock.writeLock().lock();
	            dataBinder = null;
	        } finally {
	            lock.writeLock().unlock();
	        }
	    }

	    @Inject
	    public void setChannelService(final ChannelService channelService) {
	        this.channelService = channelService;
	    }

	    @Inject
	    public void setCurrencyService(final CurrencyService currencyService) {
	        this.currencyService = currencyService;
	    }

	    @Inject
	    public void setTicketService(final TicketService ticketService) {
	        this.ticketService = ticketService;
	    }
	    public static class RequestPaymentRequestDTO{
	    	private Channel           fromChannel;
	        private Channel           toChannel;
	        private String            traceData;

	        public Channel getFromChannel() {
	            return fromChannel;
	        }

	        public Channel getToChannel() {
	            return toChannel;
	        }

	        /**
	         * Returns the data set by the client at the moment of requesting a payment.<br>
	         * It depends on the client side then there is no guarantee of uniqueness between different clients.<br>
	         */
	        public String getTraceData() {
	            return traceData;
	        }

	        public void setFromChannel(final Channel fromChannel) {
	            this.fromChannel = fromChannel;
	        }

	        public void setToChannel(final Channel toChannel) {
	            this.toChannel = toChannel;
	        }

	        public void setTraceData(final String traceData) {
	            this.traceData = traceData;
	        }

			public void sendMessage(String string, String name) {
				// TODO Auto-generated method stub
				
			}
	    }
	    
	    public static class RequestPaymentResponseDT{
	    	String message;

			public final String getMessage() {
				return message;
			}

			public final void setMessage(String message) {
				this.message = message;
			}
	    	
	    }

	    @RequestMapping(value= "operator/requestPayment",method = RequestMethod.GET)
	    @ResponseBody
	    protected RequestPaymentResponseDT formAction(@RequestBody RequestPaymentRequestDTO form) throws Exception {
	        ticket = resolveTicket(form);
	    	
	        try {
	            ticket = null;
	            form.sendMessage("paymentRequest.sent", ticket.getFrom().getName());
				
	        } catch (final CreditsException e) {
	            throw new ValidationException(actionHelper.resolveErrorKey(e), actionHelper.resolveParameters(e));
	        } catch (final InvalidChannelException e) {
	            throw new ValidationException("paymentRequest.error.invalidChannel", e.getUsername(), e.getChannelName());
	        } catch (final UnexpectedEntityException e) {
	            throw new ValidationException("payment.error.invalidTransferType");
	        } catch (final AuthorizedPaymentInPastException e) {
	            throw new ValidationException("payment.error.authorizedInPast");
	        }
	        RequestPaymentResponseDT response = new RequestPaymentResponseDT(); 
	        
	        return response;
	    }

	   private PaymentRequestTicket resolveTicket(RequestPaymentRequestDTO form) {
			// TODO Auto-generated method stub
			return null;
		}

	// @Override
	    protected void prepareForm(final ActionContext context) throws Exception {
	        final HttpServletRequest request = context.getRequest();
	        final Member member = (Member) context.getAccountOwner();
	        request.setAttribute("toMember", member);
	        final MemberGroup group = groupService.load(member.getMemberGroup().getId(), MemberGroup.Relationships.REQUEST_PAYMENT_BY_CHANNELS);

	        // Get the possible currencies
	        final List<Currency> currencies = currencyService.listByMemberGroup(group);
	        if (currencies.isEmpty()) {
	            throw new ValidationException("payment.error.noTransferType");
	        }
	        if (currencies.size() == 1) {
	            request.setAttribute("singleCurrency", currencies.iterator().next());
	        }
	        request.setAttribute("currencies", currencies);

	        // Get the possible channels
	        final Collection<Channel> channels = new ArrayList<Channel>(group.getRequestPaymentByChannels());
	        for (final Iterator<Channel> it = channels.iterator(); it.hasNext();) {
	            if (!it.next().isPaymentRequestSupported()) {
	                it.remove();
	            }
	        }
	        if (channels.isEmpty()) {
	            throw new ValidationException("paymentRequest.error.noChannels");
	        }
	        if (channels.size() == 1) {
	            request.setAttribute("singleChannel", channels.iterator().next());
	        }
	        request.setAttribute("channels", channels);

	        RequestHelper.storeEnum(request, TicketQuery.GroupedStatus.class, "status");
	    }

	   // @Override
	    protected void validateForm(final ActionContext context) {
	        final PaymentRequestTicket ticket = resolveTicket(context);
	        ticketService.validate(ticket);
	    }

	    private DataBinder<PaymentRequestTicket> getDataBinder() {
	        try {
	            lock.readLock().lock();
	            if (dataBinder == null) {
	                final LocalSettings localSettings = settingsService.getLocalSettings();
	                final BeanBinder<PaymentRequestTicket> binder = BeanBinder.instance(PaymentRequestTicket.class);
	                binder.registerBinder("from", PropertyBinder.instance(Member.class, "from"));
	                binder.registerBinder("amount", PropertyBinder.instance(BigDecimal.class, "amount", localSettings.getNumberConverter()));
	                binder.registerBinder("currency", PropertyBinder.instance(Currency.class, "currency"));
	                binder.registerBinder("toChannel", PropertyBinder.instance(Channel.class, "toChannel"));
	                binder.registerBinder("description", PropertyBinder.instance(String.class, "description"));
	                dataBinder = binder;
	            }
	            return dataBinder;
	        } finally {
	            lock.readLock().unlock();
	        }
	    }

	    private PaymentRequestTicket resolveTicket(final ActionContext context) {
	        final RequestPaymentForm form = context.getForm();
	        final PaymentRequestTicket ticket = getDataBinder().readFromString(form.getTicket());
	        ticket.setTo((Member) context.getAccountOwner());
	        ticket.setFromChannel(channelService.loadByInternalName(Channel.WEB));
	        return ticket;
	    }

}
