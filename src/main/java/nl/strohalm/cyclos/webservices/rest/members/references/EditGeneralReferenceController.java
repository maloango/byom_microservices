package nl.strohalm.cyclos.webservices.rest.members.references;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import nl.strohalm.cyclos.controls.ActionContext;
import nl.strohalm.cyclos.controls.members.references.EditReferenceForm;
import nl.strohalm.cyclos.controls.restapi.BaseRestController;
import nl.strohalm.cyclos.entities.accounts.AccountOwner;
import nl.strohalm.cyclos.entities.exceptions.EntityNotFoundException;
import nl.strohalm.cyclos.entities.members.GeneralReference;
import nl.strohalm.cyclos.entities.members.Member;
import nl.strohalm.cyclos.entities.members.Reference;
import nl.strohalm.cyclos.entities.settings.LocalSettings;
import nl.strohalm.cyclos.services.elements.ElementService;
import nl.strohalm.cyclos.services.elements.ReferenceService;
import nl.strohalm.cyclos.services.settings.SettingsService;
import nl.strohalm.cyclos.utils.binding.BeanBinder;
import nl.strohalm.cyclos.utils.binding.DataBinder;
import nl.strohalm.cyclos.utils.validation.ValidationException;
@Controller
public class EditGeneralReferenceController extends BaseRestController{
	private DataBinder<GeneralReference> dataBinder;
	private ReferenceService referenceService;
	public final ReferenceService getReferenceService() {
		return referenceService;
	}

	public final void setReferenceService(ReferenceService referenceService) {
		this.referenceService = referenceService;
	}

	public final SettingsService getSettingsService() {
		return settingsService;
	}

	public final void setSettingsService(SettingsService settingsService) {
		this.settingsService = settingsService;
	}

	public final ElementService getElementService() {
		return elementService;
	}

	public final void setElementService(ElementService elementService) {
		this.elementService = elementService;
	}

	private SettingsService settingsService;
	private ElementService elementService;
	
	public static class EditGeneralReferenceRequestDTO{
		 private long              referenceId;
		    private long              memberId;
		    private long              transferId;
		    private long              scheduledPaymentId;
		    private Map<String,Object>values;
		    public Map<String,Object> getValues;

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
	
	public static class EditGeneralReferenceResponseDTO{
		String message;

		public final String getMessage() {
			return message;
		}

		public final void setMessage(String message) {
			this.message = message;
		}
		
	}
	

    @RequestMapping (value = "/member/generalReferenceDetails", method = RequestMethod.POST)
    @ResponseBody
    protected EditGeneralReferenceResponseDTO handleSubmit(@RequestBody EditGeneralReferenceRequestDTO form) throws Exception {
        GeneralReference reference = resolveReference(form);
        final boolean isInsert = reference.isTransient();
        final GeneralReference generalReference = reference;
        reference = referenceService.save(generalReference);
        EditGeneralReferenceResponseDTO response = new EditGeneralReferenceResponseDTO();
        response.setMessage("reference." + (isInsert ? "inserted" : "modified"));
        return response;
    }

    private GeneralReference resolveReference(EditGeneralReferenceRequestDTO form) {
		// TODO Auto-generated method stub
		return null;
	}

	//@Override
    protected void prepareForm(final ActionContext context) throws Exception {
        final EditReferenceForm form = context.getForm();
        final HttpServletRequest request = context.getRequest();
        final long referenceId = form.getReferenceId();

        // Retrieve the generalReference
        GeneralReference reference;
        final AccountOwner accountOwner = context.getAccountOwner();
        if (referenceId > 0L) {
            if (form.getMemberId() == 0 && (accountOwner instanceof Member)) {
                form.setMemberId(((Member) accountOwner).getId());
            }
            reference = (GeneralReference) referenceService.load(referenceId, Reference.Relationships.FROM, Reference.Relationships.TO);
        } else {
            // Is a new general reference
            // We haven't received a reference id. Find by member
            if (form.getMemberId() <= 0L) {
                throw new ValidationException();
            }
            final Member loggedMember = (Member) accountOwner;
            Member member;
            try {
                member = elementService.load(form.getMemberId());
            } catch (final Exception e) {
                throw new ValidationException();
            }

            try {
                // Load the current reference
                reference = referenceService.loadGeneral(loggedMember, member);
            } catch (final EntityNotFoundException e) {
                // There's no reference from / to the member - we are inserting
                reference = new GeneralReference();
                reference.setFrom(loggedMember);
                reference.setTo(member);
            }
        }
        getDataBinder().writeAsString(form.getReference(), reference);

        final LocalSettings localSettings = settingsService.getLocalSettings();

        // Check whether the reference is editable
        final boolean editable = referenceService.canManage(reference);

        if (reference.isTransient() && !editable) {
            throw new ValidationException();
        }

        request.setAttribute("reference", reference);
        request.setAttribute("levels", localSettings.getReferenceLevelList());
        request.setAttribute("editable", editable);
    }

    //@Override
    protected GeneralReference resolveReference(final ActionContext context) {
        final EditReferenceForm form = context.getForm();
        return getDataBinder().readFromString(form.getReference());
    }

    private DataBinder<GeneralReference> getDataBinder() {
        if (dataBinder == null) {
            final BeanBinder<GeneralReference> binder = BeanBinder.instance(GeneralReference.class);
            initBinder(binder);
            dataBinder = binder;
        }
        return dataBinder;
    }

	private void initBinder(BeanBinder<GeneralReference> binder) {
		// TODO Auto-generated method stub
		
	}

}
