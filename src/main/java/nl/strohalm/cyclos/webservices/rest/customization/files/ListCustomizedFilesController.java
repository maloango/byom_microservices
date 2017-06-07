package nl.strohalm.cyclos.webservices.rest.customization.files;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import nl.strohalm.cyclos.annotations.Inject;
import nl.strohalm.cyclos.controls.restapi.BaseRestController;
import nl.strohalm.cyclos.entities.customization.documents.Document;
import nl.strohalm.cyclos.entities.customization.documents.DocumentQuery;
import nl.strohalm.cyclos.services.customization.DocumentService;

@Controller
public class ListCustomizedFilesController extends BaseRestController{
	
	private DocumentService documentService;

    public DocumentService getDocumentService() {
        return documentService;
    }

    @Inject
    public void setDocumentService(final DocumentService documentService) {
        this.documentService = documentService;
    }
    public static class ListCustomizedFilesRequestDTO{
    	
    	 private String            type;

    	    public String getType() {
    	        return type;
    	    }

    	    public void setType(final String type) {
    	        this.type = type;
    	    }
    	
    }
    
    public static class ListCustomizedFilesResponseDTO{
    	List<Document> docs;
    	public ListCustomizedFilesResponseDTO(List<Document> docs){
    		super();
    		this.docs=docs;
    	}
    	}

    @RequestMapping(value = "/admin/listCustomizedFiles" , method  = RequestMethod.GET)
    @ResponseBody
    protected ListCustomizedFilesResponseDTO executeAction(@RequestBody ListCustomizedFilesRequestDTO form ) throws Exception {
        final List<Document> docs = documentService.search(new DocumentQuery());
        ListCustomizedFilesResponseDTO response = new ListCustomizedFilesResponseDTO(docs);
        return response;
        
    }

}
