package nl.strohalm.cyclos.webservices.rest.members.references;

import java.util.Calendar;
import java.util.Collection;
import java.util.Map;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.hazelcast.impl.Request;

import nl.strohalm.cyclos.annotations.Inject;
import nl.strohalm.cyclos.controls.ActionContext;
import nl.strohalm.cyclos.controls.members.references.MemberReferencesForm;
import nl.strohalm.cyclos.controls.restapi.BaseRestController;
import nl.strohalm.cyclos.entities.accounts.AccountType;
import nl.strohalm.cyclos.entities.accounts.transactions.Payment;
import nl.strohalm.cyclos.entities.accounts.transactions.ScheduledPayment;
import nl.strohalm.cyclos.entities.accounts.transactions.Transfer;
import nl.strohalm.cyclos.entities.accounts.transactions.TransferType;
import nl.strohalm.cyclos.entities.groups.Group;
import nl.strohalm.cyclos.entities.members.Element;
import nl.strohalm.cyclos.entities.members.Member;
import nl.strohalm.cyclos.entities.members.Reference;
import nl.strohalm.cyclos.entities.members.Reference.Level;
import nl.strohalm.cyclos.entities.members.Reference.Nature;
import nl.strohalm.cyclos.entities.members.ReferenceQuery;
import nl.strohalm.cyclos.entities.members.TransactionFeedback;
import nl.strohalm.cyclos.entities.settings.LocalSettings;
import nl.strohalm.cyclos.services.elements.ElementService;
import nl.strohalm.cyclos.services.elements.ReferenceService;
import nl.strohalm.cyclos.services.settings.SettingsService;
import nl.strohalm.cyclos.utils.Period;
import nl.strohalm.cyclos.utils.RelationshipHelper;
import nl.strohalm.cyclos.utils.RequestHelper;
import nl.strohalm.cyclos.utils.TimePeriod;
import nl.strohalm.cyclos.utils.binding.BeanBinder;
import nl.strohalm.cyclos.utils.binding.DataBinder;
import nl.strohalm.cyclos.utils.binding.DataBinderHelper;
import nl.strohalm.cyclos.utils.conversion.CoercionHelper;
import nl.strohalm.cyclos.utils.query.QueryParameters;
import nl.strohalm.cyclos.utils.validation.ValidationException;
@Controller
public class MemberReferencesController extends BaseRestController{
	
	public static enum Direction {
        RECEIVED, GIVEN
    }

    private DataBinder<ReferenceQuery> dataBinder;
    private ReferenceService           referenceService;
    private ElementService elementService;
    private SettingsService settingsService;

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

	@Inject
    public void setReferenceService(final ReferenceService referenceService) {
        this.referenceService = referenceService;
    }
	public static class MemberReferencesRequestDTO{
	    private String            direction;
	    private long              memberId;
	    private Member            from;
	    private Member            to;
	    private Period            period;
	    private Nature  nature;
	    private Transfer          transfer;
	    private ScheduledPayment  scheduledPayment;
	    private Collection<Group> groups;

	    public Member getFrom() {
	        return from;
	    }

	    public Collection<Group> getGroups() {
	        return groups;
	    }

	   

	    public Period getPeriod() {
	        return period;
	    }

	    public ScheduledPayment getScheduledPayment() {
	        return scheduledPayment;
	    }

	    public Member getTo() {
	        return to;
	    }

	    public Transfer getTransfer() {
	        return transfer;
	    }

	    public void setFrom(final Member from) {
	        this.from = from;
	    }

	    public void setGroups(final Collection<Group> groups) {
	        this.groups = groups;
	    }

	    public void setNature(final Reference.Nature nature) {
	        this.nature = nature;
	    }

	    public void setPeriod(final Period period) {
	        this.period = period;
	    }

	    public void setScheduledPayment(final ScheduledPayment scheduledPayment) {
	        this.scheduledPayment = scheduledPayment;
	    }

	    public void setTo(final Member to) {
	        this.to = to;
	    }

	    public void setTransfer(final Transfer transfer) {
	        this.transfer = transfer;
	    }


	    public String getDirection() {
	        return direction;
	    }

	    public long getMemberId() {
	        return memberId;
	    }

	    public Nature getNature() {
	        return nature;
	    }

	    public void setDirection(final String direction) {
	        this.direction = direction;
	    }

	    public void setMemberId(final long memberId) {
	        this.memberId = memberId;
	    }

	    
	}
	public static class MemberReferencesResponseDTO{
		String message;
		public final int getTotalAllTime() {
			return totalAllTime;
		}

		public final void setTotalAllTime(int totalAllTime) {
			this.totalAllTime = totalAllTime;
		}

		public final int getTotal30Days() {
			return total30Days;
		}

		public final void setTotal30Days(int total30Days) {
			this.total30Days = total30Days;
		}

		public final int getScore30Days() {
			return score30Days;
		}

		public final void setScore30Days(int score30Days) {
			this.score30Days = score30Days;
		}

		public final int getScoreAllTime() {
			return scoreAllTime;
		}

		public final void setScoreAllTime(int scoreAllTime) {
			this.scoreAllTime = scoreAllTime;
		}

		public final int getPercentAllTime() {
			return percentAllTime;
		}

		public final void setPercentAllTime(int percentAllTime) {
			this.percentAllTime = percentAllTime;
		}

		private int totalAllTime;
		private int total30Days;
		private int score30Days;
		private int scoreAllTime;
		private int percentAllTime;
		private int percent30Days;
		

		public final String getMessage() {
			return message;
		}

		public final void setMessage(String message) {
			this.message = message;
		}

		public int getPercent30Days() {
			return percent30Days;
		}

		public void setPercent30Days(int percent30Days) {
			this.percent30Days = percent30Days;
		}
	}

    @SuppressWarnings("unchecked")
    @RequestMapping(value = "", method = RequestMethod.GET)
    @ResponseBody
    protected MemberReferencesResponseDTO executeQuery(@RequestBody  MemberReferencesRequestDTO form, final QueryParameters queryParameters) {
        
        final Direction direction = CoercionHelper.coerce(Direction.class, form.getDirection());
        final Member member = (Member) request.getAttribute("member");
        final ReferenceQuery query = (ReferenceQuery) queryParameters;
        if (member != null) {
            form.setMemberId(member.getId());
        }

        // Retrieve both summaries for all time and last 30 days
        final Map<Level, Integer> allTime = referenceService.countReferencesByLevel(query.getNature(), member, direction == Direction.RECEIVED);
        ((ServletRequest) allTime).setAttribute("summaryAllTime", allTime);
        final Period period30 = new TimePeriod(30, TimePeriod.Field.DAYS).periodEndingAt(Calendar.getInstance());
        final Map<Level, Integer> last30Days = referenceService.countReferencesHistoryByLevel(query.getNature(), member, period30, direction == Direction.RECEIVED);
        request.setAttribute("summaryLast30Days", last30Days);

        // Calculate the score
        int totalAllTime = 0;
        int total30Days = 0;
        int scoreAllTime = 0;
        int score30Days = 0;
        final Collection<Level> levels = (Collection<Level>) request.getAttribute("levels");
        int nonNeutralCountAllTime = 0;
        int positiveCountAllTime = 0;
        int nonNeutralCount30Days = 0;
        int positiveCount30Days = 0;
        for (final Level level : levels) {
            final int value = level.getValue();
            final int allTimeCount = CoercionHelper.coerce(Integer.TYPE, allTime.get(level));
            final int last30DaysCount = CoercionHelper.coerce(Integer.TYPE, last30Days.get(level));

            // Calculate the total
            totalAllTime += allTimeCount;
            total30Days += last30DaysCount;

            // Calculate the score (sum of count * value)
            scoreAllTime += allTimeCount * value;
            score30Days += last30DaysCount * value;

            // Calculate the data for positive percentage
            if (value != 0) {
                nonNeutralCountAllTime += allTimeCount;
                nonNeutralCount30Days += last30DaysCount;
                if (value > 0) {
                    positiveCountAllTime += allTimeCount;
                    positiveCount30Days += last30DaysCount;
                }
            }
        }

        // Calculate the positive percentage
        final int percentAllTime = nonNeutralCountAllTime == 0 ? 0 : Math.round((float) positiveCountAllTime / nonNeutralCountAllTime * 100F);
        final int percentLast30Days = nonNeutralCount30Days == 0 ? 0 : Math.round((float) positiveCount30Days / nonNeutralCount30Days * 100F);

        // Store calculated data on request
        request.setAttribute("totalAllTime", totalAllTime);
        request.setAttribute("total30Days", total30Days);
        request.setAttribute("scoreAllTime", scoreAllTime);
        request.setAttribute("score30Days", score30Days);
        request.setAttribute("percentAllTime", percentAllTime);
        request.setAttribute("percent30Days", percentLast30Days);

        // Get the references list
        request.setAttribute("references", referenceService.search(query));
    }

   // @Override
    protected QueryParameters prepareForm(final ActionContext context) {
        final MemberReferencesForm form = context.getForm();
        final HttpServletRequest request = context.getRequest();
        final ReferenceQuery query = getDataBinder().readFromString(form.getQuery());
        query.setNature(CoercionHelper.coerce(Nature.class, form.getNature()));

        if (query.getNature() == null) {
            query.setNature(Nature.GENERAL);
        }

        // Find out the member
        Member member;
        try {
            member = (Member) (form.getMemberId() <= 0L ? context.getAccountOwner() : elementService.load(form.getMemberId(), Element.Relationships.GROUP));
        } catch (final Exception e) {
            throw new ValidationException();
        }

        final boolean myReferences = member.equals(context.getAccountOwner());

        // Retrieve the direction we're looking at
        Direction direction = CoercionHelper.coerce(Direction.class, form.getDirection());
        if (direction == null) {
            direction = Direction.RECEIVED;
            form.setDirection(direction.name());
        }
        final boolean isGiven = direction == Direction.GIVEN;
        if (isGiven) {
            query.setFrom(member);
        } else {
            query.setTo(member);
        }
        final boolean isGeneral = query.getNature() == Reference.Nature.GENERAL;

        if (!isGeneral) {
            query.fetch(RelationshipHelper.nested(TransactionFeedback.Relationships.TRANSFER, Payment.Relationships.TYPE, TransferType.Relationships.FROM, AccountType.Relationships.CURRENCY));
        }

        // When it's a member (or operator) viewing of other member's received general references, he can set his own too
        final boolean canSetReference = isGeneral && referenceService.canGiveGeneralReference(member);

        // Check whether the logged user can manage references on the list
        final boolean canManage = isGeneral && (myReferences && isGiven || !myReferences) && referenceService.canManageGeneralReference(member);

        // Bind the form and store the request attributes
        final LocalSettings localSettings = settingsService.getLocalSettings();
        getDataBinder().writeAsString(form.getQuery(), query);
        request.setAttribute("member", member);
        request.setAttribute("canManage", canManage);
        request.setAttribute("myReferences", myReferences);
        request.setAttribute("isGiven", isGiven);
        request.setAttribute("isGeneral", isGeneral);
        request.setAttribute("levels", localSettings.getReferenceLevelList());
        request.setAttribute("canSetReference", canSetReference);
        RequestHelper.storeEnum(request, Direction.class, "directions");

        if (!isGeneral) {
            final boolean showAmount = context.isAdmin() || context.getAccountOwner().equals(member);
            request.setAttribute("showAmount", showAmount);
        }
        return query;
    }

   // @Override
    protected boolean willExecuteQuery(final ActionContext context, final QueryParameters queryParameters) throws Exception {
        return true;
    }

    private DataBinder<ReferenceQuery> getDataBinder() {
        if (dataBinder == null) {
            final BeanBinder<ReferenceQuery> binder = BeanBinder.instance(ReferenceQuery.class);
            binder.registerBinder("pageParameters", DataBinderHelper.pageBinder());
            dataBinder = binder;
        }
        return dataBinder;
    }

}
