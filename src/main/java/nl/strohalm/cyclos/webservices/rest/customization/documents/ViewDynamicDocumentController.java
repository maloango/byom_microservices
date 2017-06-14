package nl.strohalm.cyclos.webservices.rest.customization.documents;

import org.springframework.stereotype.Controller;

import nl.strohalm.cyclos.annotations.Inject;
import nl.strohalm.cyclos.services.customization.DocumentService;
import nl.strohalm.cyclos.services.customization.MemberCustomFieldService;
import nl.strohalm.cyclos.services.elements.ElementService;
import nl.strohalm.cyclos.utils.CustomFieldHelper;
import nl.strohalm.cyclos.utils.CustomizationHelper;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;
@Controller
public class ViewDynamicDocumentController extends BaseRestController{
	private DocumentService          documentService;
    private MemberCustomFieldService memberCustomFieldService;
    private CustomizationHelper      customizationHelper;
    private CustomFieldHelper        customFieldHelper;

    public DocumentService getDocumentService() {
        return documentService;
    }

    public MemberCustomFieldService getMemberCustomFieldService() {
        return memberCustomFieldService;
    }

    @Inject
    public void setCustomFieldHelper(final CustomFieldHelper customFieldHelper) {
        this.customFieldHelper = customFieldHelper;
    }

    @Inject
    public void setCustomizationHelper(final CustomizationHelper customizationHelper) {
        this.customizationHelper = customizationHelper;
    }

    @Inject
    public void setDocumentService(final DocumentService documentService) {
        this.documentService = documentService;
    }

    @Inject
    public void setMemberCustomFieldService(final MemberCustomFieldService memberCustomFieldService) {
        this.memberCustomFieldService = memberCustomFieldService;
    }
    public static class ViewDynamicDocumentRequestDTO {
    	
    	private long              memberId;
        private long              documentId;

        public long getDocumentId() {
            return documentId;
        }

        public long getMemberId() {
            return memberId;
        }

        public void setDocumentId(final long documentId) {
            this.documentId = documentId;
        }

        public void setMemberId(final long memberId) {
            this.memberId = memberId;
        }

	}

	public static class ViewDynamicDocumentResponseDTO {
		private ElementService elementService;

		public final ElementService getElementService() {
			return elementService;
		}

		public final void setElementService(ElementService elementService) {
			this.elementService = elementService;
		}

	}
/*
    @RequestMapping(value= "",method = RequestMethod.GET)
    @ResponseBody
    protected ViewDynamicDocumentResponseDTO executeAction(@RequestBody ViewDynamicDocumentRequestDTO  form) throws Exception {
        //final HttpServletRequest request = context.getRequest();
        //final ViewDocumentForm form = context.getForm();
        final long memberId = form.getMemberId();
        final long documentId = form.getDocumentId();
        if (memberId <= 0L || documentId <= 0L) {
            throw new ValidationException();
        }
        Member member;
        DynamicDocument document;
        try {
            member = elementService.load(memberId, Element.Relationships.USER, Element.Relationships.GROUP, Member.Relationships.BROKER, Member.Relationships.CUSTOM_VALUES);
            document = (DynamicDocument) documentService.load(documentId);
        } catch (final Exception e) {
            throw new ValidationException();
        }
        if (document.isHasFormPage()) {
            final String formPageName = customizationHelper.formFile(document).getName();
            request.setAttribute("formPage", CustomizationHelper.DOCUMENT_PATH + formPageName);
        }
        final String documentPageName = customizationHelper.documentFile(document).getName();
        request.setAttribute("documentPage", CustomizationHelper.DOCUMENT_PATH + documentPageName);

        request.setAttribute("document", document);

        // Set the member inside a wrapper, allowing access to custom fields the same way as properties
        final List<MemberCustomField> customFields = customFieldHelper.onlyForGroup(memberCustomFieldService.list(), member.getMemberGroup());
        request.setAttribute("member", new EntityWithCustomFieldsWrapper(member, customFields, customFieldHelper));
        request.setAttribute("now", Calendar.getInstance());

        if (document.isHasFormPage() && RequestHelper.isGet(request)) {
            return context.getInputForward();
        } else {
            return context.getSuccessForward();
        }
    }
*/
}
