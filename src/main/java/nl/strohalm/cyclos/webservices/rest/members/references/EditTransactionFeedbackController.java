package nl.strohalm.cyclos.webservices.rest.members.references;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import nl.strohalm.cyclos.access.AdminMemberPermission;
import nl.strohalm.cyclos.controls.ActionContext;
import nl.strohalm.cyclos.controls.members.references.EditReferenceForm;
import nl.strohalm.cyclos.controls.restapi.BaseRestController;
import nl.strohalm.cyclos.entities.accounts.Account;
import nl.strohalm.cyclos.entities.accounts.AccountType;
import nl.strohalm.cyclos.entities.accounts.MemberAccount;
import nl.strohalm.cyclos.entities.accounts.transactions.Payment;
import nl.strohalm.cyclos.entities.accounts.transactions.ScheduledPayment;
import nl.strohalm.cyclos.entities.accounts.transactions.Transfer;
import nl.strohalm.cyclos.entities.exceptions.EntityNotFoundException;
import nl.strohalm.cyclos.entities.members.Member;
import nl.strohalm.cyclos.entities.members.Reference;
import nl.strohalm.cyclos.entities.members.Reference.Nature;
import nl.strohalm.cyclos.entities.members.TransactionFeedback;
import nl.strohalm.cyclos.entities.settings.LocalSettings;
import nl.strohalm.cyclos.services.elements.ReferenceService;
import nl.strohalm.cyclos.services.elements.TransactionFeedbackAction;
import nl.strohalm.cyclos.services.permissions.PermissionService;
import nl.strohalm.cyclos.services.settings.SettingsService;
import nl.strohalm.cyclos.services.transactions.PaymentService;
import nl.strohalm.cyclos.services.transactions.ScheduledPaymentService;
import nl.strohalm.cyclos.utils.EntityHelper;
import nl.strohalm.cyclos.utils.RelationshipHelper;
import nl.strohalm.cyclos.utils.access.LoggedUser;
import nl.strohalm.cyclos.utils.binding.BeanBinder;
import nl.strohalm.cyclos.utils.binding.DataBinder;
import nl.strohalm.cyclos.utils.binding.PropertyBinder;
import nl.strohalm.cyclos.utils.validation.RequiredError;
import nl.strohalm.cyclos.utils.validation.ValidationException;
@Controller
public class EditTransactionFeedbackController extends BaseRestController{
	private DataBinder<TransactionFeedback> dataBinder;
	private PaymentService paymentService;
	private ScheduledPaymentService scheduledPaymentService;
	public final PaymentService getPaymentService() {
		return paymentService;
	}
	public final void setPaymentService(PaymentService paymentService) {
		this.paymentService = paymentService;
	}
	public final ScheduledPaymentService getScheduledPaymentService() {
		return scheduledPaymentService;
	}
	public final void setScheduledPaymentService(ScheduledPaymentService scheduledPaymentService) {
		this.scheduledPaymentService = scheduledPaymentService;
	}
	public final ReferenceService getReferenceService() {
		return referenceService;
	}
	public final void setReferenceService(ReferenceService referenceService) {
		this.referenceService = referenceService;
	}
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

	private ReferenceService referenceService;
	private PermissionService permissionService;
	private SettingsService settingsService;
	

    public DataBinder<TransactionFeedback> getDataBinder() {
        if (dataBinder == null) {
            final BeanBinder<TransactionFeedback> binder = BeanBinder.instance(TransactionFeedback.class);
            initBinder(binder);
            binder.registerBinder("transfer", PropertyBinder.instance(Transfer.class, "transfer"));
            binder.registerBinder("scheduledPayment", PropertyBinder.instance(ScheduledPayment.class, "scheduledPayment"));
            binder.registerBinder("replyComments", PropertyBinder.instance(String.class, "replyComments"));
            binder.registerBinder("adminComments", PropertyBinder.instance(String.class, "adminComments"));
            dataBinder = binder;
        }
        return dataBinder;
    }
    private void initBinder(BeanBinder<TransactionFeedback> binder) {
		// TODO Auto-generated method stub
		
	}
	public static class EditTransactionFeedbackRequestDTO{
    	private long              referenceId;
        private long              memberId;
        private long              transferId;
        private long              scheduledPaymentId;
        private Transfer          transfer;
        private ScheduledPayment  scheduledPayment;
        private String            replyComments;
        private Calendar          replyCommentsDate;
        private String            adminComments;
        private Calendar          adminCommentsDate;

        public String getAdminComments() {
            return adminComments;
        }

        public Calendar getAdminCommentsDate() {
            return adminCommentsDate;
        }

        //@Override
        public Nature getNature() {
            return Nature.TRANSACTION;
        }

        public Payment getPayment() {
            return transfer == null ? scheduledPayment : transfer;
        }

        public String getReplyComments() {
            return replyComments;
        }

        public Calendar getReplyCommentsDate() {
            return replyCommentsDate;
        }

        public ScheduledPayment getScheduledPayment() {
            return scheduledPayment;
        }

        public Transfer getTransfer() {
            return transfer;
        }

        public void setAdminComments(final String adminComments) {
            this.adminComments = adminComments;
        }

        public void setAdminCommentsDate(final Calendar adminCommentsDate) {
            this.adminCommentsDate = adminCommentsDate;
        }

        public void setPayment(final Payment payment) {
            if (payment instanceof Transfer) {
                transfer = (Transfer) payment;
                scheduledPayment = null;
            } else if (payment instanceof ScheduledPayment) {
                scheduledPayment = (ScheduledPayment) payment;
                transfer = null;
            } else {
                transfer = null;
                scheduledPayment = null;
            }
        }

        public void setReplyComments(final String replyComments) {
            this.replyComments = replyComments;
        }

        public void setReplyCommentsDate(final Calendar replyCommentsDate) {
            this.replyCommentsDate = replyCommentsDate;
        }

        public void setScheduledPayment(final ScheduledPayment scheduledPayment) {
            this.scheduledPayment = scheduledPayment;
        }

        public void setTransfer(final Transfer transfer) {
            this.transfer = transfer;
        }

        @Override
        public String toString() {
            return getId() + " - " + getLevel() + " for " + (transfer == null ? scheduledPayment : transfer);
        }
        private String getLevel() {
			// TODO Auto-generated method stub
			return null;
		}

		private String getId() {
			// TODO Auto-generated method stub
			return null;
		}
		private Map<String,Object> values;
        public long getMemberId() {
            return memberId;
        }

        public Map<String, Object> getReference() {
            return values;
        }

        public Object getReference(final String key) {
            return values.get(key);
        }

        public long getReferenceId() {
            return referenceId;
        }

        public long getScheduledPaymentId() {
            return scheduledPaymentId;
        }

        public long getTransferId() {
            return transferId;
        }

        public void setMemberId(final long memberId) {
            this.memberId = memberId;
        }

        public void setReference(final Map<String, Object> map) {
            values = map;
        }

        public void setReference(final String key, final Object value) {
            values.put(key, value);
        }

        public void setReferenceId(final long id) {
            referenceId = id;
        }

        public void setScheduledPaymentId(final long scheduledPaymentId) {
            this.scheduledPaymentId = scheduledPaymentId;
        }

        public void setTransferId(final long transferId) {
            this.transferId = transferId;
        }
    }
    public static class EditTransactionFeedbackResponseDTO{
    	String message;

		public final String getMessage() {
			return message;
		}

		public final void setMessage(String message) {
			this.message = message;
		}
    }

    @RequestMapping(value ="/member/transactionFeedbackDetails",method = RequestMethod.POST)
    @ResponseBody
    protected EditTransactionFeedbackResponseDTO handleSubmit(@RequestBody EditTransactionFeedbackRequestDTO form) throws Exception {
   
        final long memberId = form.getMemberId();
        TransactionFeedback feedback = resolveReference(form);
        final TransactionFeedbackAction tfa = referenceService.getPossibleAction(feedback);
        feedback = referenceService.save(feedback);
        EditTransactionFeedbackResponseDTO response = new EditTransactionFeedbackResponseDTO();

        switch (tfa) {
            case COMMENTS:
                response.setMessage("reference.transactionFeedback.saved");
                break;
            case REPLY_COMMENTS:
                response.setMessage("reference.transactionFeedback.replyComments.saved");
                break;
            case ADMIN_EDIT:
                response.setMessage("reference.transactionFeedback.saved");
                break;
		default:
			break;
        }
        final Map<String, Object> params = new HashMap<String, Object>();
        params.put("memberId", memberId);
        params.put("referenceId", feedback.getId());
        return response;
    }

   private TransactionFeedback resolveReference(EditTransactionFeedbackRequestDTO form) {
		// TODO Auto-generated method stub
		return null;
	}
// @Override
    protected void prepareForm(final ActionContext context) throws Exception {
        final EditReferenceForm form = context.getForm();
        final HttpServletRequest request = context.getRequest();
        long feedbackId = form.getReferenceId();
        final long transferId = form.getTransferId();
        final long scheduledPaymentId = form.getScheduledPaymentId();

        // Retrieve the reference
        TransactionFeedback transactionFeedback = null;
        boolean showPayment = false;
        boolean canComment = false;
        boolean canReply = false;
        boolean editable = false;

        // Check whether it's by payment
        if (transferId > 0 || scheduledPaymentId > 0) {
            // Is a new transaction feedback
            Payment payment;
            if (transferId > 0) {
                payment = paymentService.load(transferId, RelationshipHelper.nested(Payment.Relationships.FROM, Account.Relationships.TYPE, AccountType.Relationships.CURRENCY), RelationshipHelper.nested(Payment.Relationships.FROM, MemberAccount.Relationships.MEMBER), RelationshipHelper.nested(Payment.Relationships.TO, MemberAccount.Relationships.MEMBER));
            } else {
                payment = scheduledPaymentService.load(scheduledPaymentId, RelationshipHelper.nested(Payment.Relationships.FROM, Account.Relationships.TYPE, AccountType.Relationships.CURRENCY), RelationshipHelper.nested(Payment.Relationships.FROM, MemberAccount.Relationships.MEMBER), RelationshipHelper.nested(Payment.Relationships.TO, MemberAccount.Relationships.MEMBER));
            }

            // Check whether the payment already exists
            try {
                final TransactionFeedback feedback = referenceService.loadTransactionFeedback(payment);

                // Already exists - set the id
                feedbackId = feedback.getId();
            } catch (final EntityNotFoundException e) {
                // Don't exists - create a new one
                final Member loggedMember = (Member) context.getAccountOwner();
                if (!loggedMember.equals(payment.getFromOwner())) {
                    throw new ValidationException();
                }
                transactionFeedback = new TransactionFeedback();
                transactionFeedback.setPayment(payment);
                transactionFeedback.setFrom(loggedMember);
                transactionFeedback.setTo((Member) payment.getToOwner());
                showPayment = true;
                canComment = true;
            }

        }

        // Check by id
        if (feedbackId > 0) {
            try {
                transactionFeedback = (TransactionFeedback) referenceService.load(feedbackId, Reference.Relationships.FROM, Reference.Relationships.TO, RelationshipHelper.nested(TransactionFeedback.Relationships.TRANSFER, Payment.Relationships.FROM, MemberAccount.Relationships.MEMBER), RelationshipHelper.nested(TransactionFeedback.Relationships.TRANSFER, Payment.Relationships.TO, MemberAccount.Relationships.MEMBER), RelationshipHelper.nested(TransactionFeedback.Relationships.TRANSFER, Payment.Relationships.TYPE), RelationshipHelper.nested(TransactionFeedback.Relationships.SCHEDULED_PAYMENT, Payment.Relationships.FROM, MemberAccount.Relationships.MEMBER), RelationshipHelper.nested(TransactionFeedback.Relationships.SCHEDULED_PAYMENT, Payment.Relationships.TO, MemberAccount.Relationships.MEMBER), RelationshipHelper.nested(TransactionFeedback.Relationships.SCHEDULED_PAYMENT, Payment.Relationships.TYPE));
            } catch (final Exception e) {
                throw new ValidationException();
            }
            final Payment payment = transactionFeedback.getPayment();
            if (payment instanceof ScheduledPayment) {
                form.setScheduledPaymentId(payment.getId());
            } else {
                form.setTransferId(payment.getId());
            }
            showPayment = context.isAdmin() || context.getAccountOwner().equals(payment.getFromOwner()) || context.getAccountOwner().equals(payment.getToOwner());
            canReply = (!LoggedUser.isAdministrator()) && (LoggedUser.element().getAccountOwner().equals(transactionFeedback.getTo()) && StringUtils.isEmpty(transactionFeedback.getReplyComments())) && referenceService.canReplyFeedbackNow(transactionFeedback);
            editable = permissionService.hasPermission(AdminMemberPermission.TRANSACTION_FEEDBACKS_MANAGE);
        }

        // Couldn't find the feedback
        if (transactionFeedback == null) {
            throw new ValidationException();
        }

        getDataBinder().writeAsString(form.getReference(), transactionFeedback);

        if (editable) {
            canComment = true;
            canReply = true;
        }

        final LocalSettings localSettings = settingsService.getLocalSettings();

        request.setAttribute("transactionFeedback", transactionFeedback);
        request.setAttribute("levels", localSettings.getReferenceLevelList());
        request.setAttribute("showPayment", showPayment);
        request.setAttribute("canComment", canComment);
        request.setAttribute("canReply", canReply);
        request.setAttribute("editable", editable);
    }

   // @Override
    protected TransactionFeedback resolveReference(final ActionContext context) {
        final EditReferenceForm form = context.getForm();
        final TransactionFeedback feedback = getDataBinder().readFromString(form.getReference());
        if (form.getTransferId() > 0) {
            feedback.setTransfer(EntityHelper.reference(Transfer.class, form.getTransferId()));
        } else if (form.getScheduledPaymentId() > 0) {
            feedback.setScheduledPayment(EntityHelper.reference(ScheduledPayment.class, form.getScheduledPaymentId()));
        }
        return feedback;
    }

   // @Override
    protected void validateForm(final ActionContext context) {
        final TransactionFeedback transactionFeedback = resolveReference(context);
        switch (referenceService.getPossibleAction(transactionFeedback)) {
            case COMMENTS:
                referenceService.validate(transactionFeedback);
                break;
            case REPLY_COMMENTS:
                if (StringUtils.isEmpty(transactionFeedback.getReplyComments())) {
                    throw new ValidationException("replyComments", "reference.replyComments", new RequiredError());
                }
                break;
		default:
			break;
        }
    }
}