package nl.strohalm.cyclos.webservices.rest.customization.documents;

import java.io.File;

import org.apache.struts.action.ActionForward;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import nl.strohalm.cyclos.annotations.Inject;
import nl.strohalm.cyclos.entities.customization.documents.Document;
import nl.strohalm.cyclos.entities.customization.documents.DynamicDocument;
import nl.strohalm.cyclos.entities.customization.documents.MemberDocument;
import nl.strohalm.cyclos.entities.customization.documents.StaticDocument;
import nl.strohalm.cyclos.services.customization.DocumentService;
import nl.strohalm.cyclos.utils.CustomizationHelper;
import nl.strohalm.cyclos.utils.validation.ValidationException;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;
@Controller
public class RemoveDocumentController extends BaseRestController{

	public final DocumentService getDocumentService() {
		return documentService;
	}

	private DocumentService     documentService;
    private CustomizationHelper customizationHelper;

    @Inject
    public void setCustomizationHelper(final CustomizationHelper customizationHelper) {
        this.customizationHelper = customizationHelper;
    }

    @Inject
    public void setDocumentService(final DocumentService documentService) {
        this.documentService = documentService;
    }
    public static class RemoveDocumentRequestDTo{
    	private long              documentId;

        public long getDocumentId() {
            return documentId;
        }

        public void setDocumentId(final long documentId) {
            this.documentId = documentId;
            
        }
    	
    }
    
    public static class RemoveDocumentResponseDTO{
    	private long memberId;
    	String findForward;
    	public final String getFindForward() {
			return findForward;
		}
		public final ActionForward setFindForward(String findForward) {
			this.findForward = findForward;
			return null;
		}
		public RemoveDocumentResponseDTO(long memberId) {
			super();
			this.memberId = memberId;
		}
		String forwardName;
    	public RemoveDocumentResponseDTO(String forwardName) {
			super();
			this.forwardName = forwardName;
		}
		public final long getMemberId() {
			return memberId;
		}
		public final void setMemberId(long memberId) {
			this.memberId = memberId;
		}
		public final String getForwardName() {
			return forwardName;
		}
		public final void setForwardName(String forwardName) {
			this.forwardName = forwardName;
		}
		public final String getMessage() {
			return message;
		}
		public final void setMessage(String message) {
			this.message = message;
		}
		String message =null;
    }

    @RequestMapping(value ="/admin/removeDocument" , method = RequestMethod.DELETE)
    @ResponseBody
    protected RemoveDocumentResponseDTO executeAction(@RequestBody RemoveDocumentRequestDTo form) throws Exception {
            RemoveDocumentResponseDTO response = null;
            try{
          final long id = form.getDocumentId();
          if (id <= 0L) {
              throw new ValidationException();
          }
          final Document document = documentService.load(id, DynamicDocument.Relationships.FORM_PAGE, DynamicDocument.Relationships.DOCUMENT_PAGE, StaticDocument.Relationships.BINARY_FILE);
          String forwardName = null;
          Long memberId = null;
          documentService.remove(id);
          if (document instanceof DynamicDocument) {
              final DynamicDocument dynamicDocument = (DynamicDocument) document;
              if (dynamicDocument.isHasFormPage()) {
                  final File formFile = customizationHelper.formFile(dynamicDocument);
                  customizationHelper.deleteFile(formFile);
              }
              final File docFile = customizationHelper.documentFile(dynamicDocument);
              customizationHelper.deleteFile(docFile);
              forwardName = "listDocuments";
          } else if (document instanceof MemberDocument) {
              final MemberDocument memberDocument = (MemberDocument) document;
              memberId = memberDocument.getMember().getId();
              forwardName = "selectDocument";
          } else { // document instance of StaticDocument
              forwardName = "listDocuments";
          }
          response =new RemoveDocumentResponseDTO(memberId);
         response.setMessage("document.removed");}
            
           catch(Exception e){
               e.printStackTrace();
           }
          return response;
      }

    }



