package nl.strohalm.cyclos.webservices.rest.customization.documents;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import nl.strohalm.cyclos.annotations.Inject;
import nl.strohalm.cyclos.entities.customization.documents.DynamicDocument;
import nl.strohalm.cyclos.services.customization.DocumentService;
import nl.strohalm.cyclos.utils.CustomizationHelper;
import nl.strohalm.cyclos.utils.validation.ValidationException;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;

@Controller
public class PreviewDynamicDocumentController extends BaseRestController {
	private DocumentService documentService;
	private CustomizationHelper customizationHelper;

	@Inject
	public void setCustomizationHelper(final CustomizationHelper customizationHelper) {
		this.customizationHelper = customizationHelper;
	}

	@Inject
	public void setDocumentService(final DocumentService documentService) {
		this.documentService = documentService;
	}

	public static class PreviewDynamicDocumentRequestDTO {
		private long documentId;

		
		public long getDocumentId() {
			return documentId;
		}

		public void setDocumentId(final long documentId) {
			this.documentId = documentId;
		}

	}

	public static class PreviewDynamicDocumentResponseDTO {
		DynamicDocument document;
		String documentPageName;

		public PreviewDynamicDocumentResponseDTO(DynamicDocument document, String documentPageName) {
			super();
			this.document = document;
			this.documentPageName = documentPageName;
		}
	}

	@RequestMapping(value = "/admin/previewDynamicDocument", method = RequestMethod.GET)
	@ResponseBody
	protected PreviewDynamicDocumentResponseDTO executeAction(PreviewDynamicDocumentRequestDTO form) throws Exception {
		/*
		 * final HttpServletRequest request = context.getRequest(); final
		 * PreviewDocumentForm form = context.getForm();
		 */
		final long documentId = form.getDocumentId();

		DynamicDocument document;
		try {
			document = (DynamicDocument) documentService.load(documentId);
		} catch (final Exception e) {
			throw new ValidationException();
		}
		if (document.isHasFormPage()) {
			customizationHelper.formFile(document).getName();
		}
		final String documentPageName = customizationHelper.documentFile(document).getName();
		PreviewDynamicDocumentResponseDTO response = new PreviewDynamicDocumentResponseDTO(document, documentPageName);
		return response;
	}

}
