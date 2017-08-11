package nl.strohalm.cyclos.webservices.rest.customization.documents;

import java.sql.Blob;
import java.util.Calendar;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import nl.strohalm.cyclos.annotations.Inject;
import nl.strohalm.cyclos.entities.customization.binaryfiles.BinaryFile;
import nl.strohalm.cyclos.entities.customization.documents.StaticDocument;
import nl.strohalm.cyclos.services.customization.DocumentService;
import nl.strohalm.cyclos.utils.ResponseHelper;
import nl.strohalm.cyclos.utils.validation.ValidationException;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;


@Controller
public class ViewDocumentController extends BaseRestController{
	 protected DocumentService documentService;
	    protected ResponseHelper  responseHelper;

	    public final DocumentService getDocumentService() {
			return documentService;
		}

		@Inject
	    public void setDocumentService(final DocumentService documentService) {
	        this.documentService = documentService;
	    }

	    @Inject
	    public void setResponseHelper(final ResponseHelper responseHelper) {
	        this.responseHelper = responseHelper;
	        
	    }
	    public static class ViewDocumentRequestDTO{
	    	private String            contentType;
	        private String            name;
	        private Integer           size;
	        private Calendar          lastModified;
	        private Blob              contents;

	        public Blob getContents() {
	            return contents;
	        }

	        public String getContentType() {
	            return contentType;
	        }

	        public Calendar getLastModified() {
	            return lastModified;
	        }

	        public String getName() {
	            return name;
	        }

	        public Integer getSize() {
	            return size;
	        }

	        public void setContents(final Blob contents) {
	            this.contents = contents;
	        }

	        public void setContentType(final String contentType) {
	            this.contentType = contentType;
	        }

	        public void setLastModified(final Calendar lastModified) {
	            this.lastModified = lastModified;
	        }

	        public void setName(final String name) {
	            this.name = name;
	        }

	        public void setSize(final Integer size) {
	            this.size = size;
	        }

			public long getDocumentId() {
				
				return 0;
			}

			public HttpServletResponse ViewDocumentResponseDTO() {
				// TODO Auto-generated method stub
				return null;
			}

	      
	    	
	    	
	    }
	    
	    public static class ViewDocumentResponseDTO{
	    	
	    }

	    @RequestMapping(value = "/member/viewMemberDocument" ,method = RequestMethod.GET)
	    @ResponseBody
	    protected ViewDocumentResponseDTO executeAction(@RequestBody ViewDocumentRequestDTO  form) throws Exception {
	        //final PreviewDocumentForm form = context.getForm();
	       final long documentId = form.getDocumentId();
	        if (documentId < 1) {
	            throw new ValidationException();
	        }
	        final StaticDocument document = (StaticDocument) documentService.load(documentId, StaticDocument.Relationships.BINARY_FILE);
	        final BinaryFile binaryFile = document.getBinaryFile();
	        final HttpServletResponse response = form.ViewDocumentResponseDTO();
	        
	        responseHelper.setDownload(response, binaryFile.getName());
	        response.setContentType(binaryFile.getContentType());
	        response.setContentLength(binaryFile.getSize());
	        response.setDateHeader("Last-Modified", binaryFile.getLastModified().getTimeInMillis());
	        IOUtils.copy(binaryFile.getContents().getBinaryStream(), response.getOutputStream());
	        response.flushBuffer();
	        return null;
	    }


}
