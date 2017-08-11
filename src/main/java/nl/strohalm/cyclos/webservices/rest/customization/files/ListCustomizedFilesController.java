package nl.strohalm.cyclos.webservices.rest.customization.files;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import nl.strohalm.cyclos.annotations.Inject;
import nl.strohalm.cyclos.entities.customization.documents.Document;
import nl.strohalm.cyclos.entities.customization.files.CustomizedFile;
import nl.strohalm.cyclos.entities.customization.files.CustomizedFileQuery;
import nl.strohalm.cyclos.entities.groups.Group;
import nl.strohalm.cyclos.entities.groups.GroupFilter;
import nl.strohalm.cyclos.services.customization.CustomizedFileService;
import nl.strohalm.cyclos.services.customization.DocumentService;
import nl.strohalm.cyclos.utils.validation.ValidationException;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class ListCustomizedFilesController extends BaseRestController{
	
	private DocumentService documentService;
        private CustomizedFileService customizedFileService;

    public CustomizedFileService getCustomizedFileService() {
        return customizedFileService;
    }

    public void setCustomizedFileService(CustomizedFileService customizedFileService) {
        this.customizedFileService = customizedFileService;
    }
        
    public DocumentService getDocumentService() {
        return documentService;
    }

    @Inject
    public void setDocumentService(final DocumentService documentService) {
        this.documentService = documentService;
    }
    public static class ListCustomizedFilesRequestDTO{
    private Group               group;
    private GroupFilter         groupFilter;
    private boolean             all;
    private String              type;

    	    public String getType() {
    	        return type;
    	    }

    	    public void setType(final String type) {
    	        this.type = type;
    	    }

    public Group getGroup() {
        return group;
    }

    public GroupFilter getGroupFilter() {
        return groupFilter;
    }

    public boolean isAll() {
        return all;
    }

    public void setAll(final boolean all) {
        this.all = all;
    }

    public void setGroup(final Group group) {
        this.group = group;
    }

    public void setGroupFilter(final GroupFilter groupFilter) {
        this.groupFilter = groupFilter;
    }

    }
    
    public static class ListCustomizedFilesResponseDTO{
    	List<Document> docs;
        public String message;
        private String              type;

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }
        
        public List<Document> getDocs() {
            return docs;
        }

        public void setDocs(List<Document> docs) {
            this.docs = docs;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
        
    	public ListCustomizedFilesResponseDTO(List<Document> docs){
    		super();
    		this.docs=docs;
    	}
        public ListCustomizedFilesResponseDTO(){
        }
    	}

    @RequestMapping(value = "admin/listCustomizedFiles/{type}" , method  = RequestMethod.GET)
    @ResponseBody
    protected ListCustomizedFilesResponseDTO executeAction(@PathVariable ("type") String type, CustomizedFile.Type Type) throws Exception {
        ListCustomizedFilesResponseDTO response =null;
       
        try {
            
            Type = CustomizedFile.Type.valueOf(response.getType());
        } catch (final Exception e) {
            throw new ValidationException();
        }
        final CustomizedFileQuery query = new CustomizedFileQuery();
        query.setType(type);
        final List<CustomizedFile> files = customizedFileService.search(query);
        response.setMessage("files");
        response.setMessage("type");
        response = new ListCustomizedFilesResponseDTO();
        return response;
    }
}
