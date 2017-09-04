package nl.strohalm.cyclos.webservices.rest.customization.documents;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts.upload.FormFile;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import nl.strohalm.cyclos.annotations.Inject;
import nl.strohalm.cyclos.controls.ActionContext;
//import nl.strohalm.cyclos.controls.customization.documents.EditStaticDocumentAction;
//import nl.strohalm.cyclos.controls.customization.documents.EditStaticDocumentForm;
import nl.strohalm.cyclos.entities.customization.documents.Document;
import nl.strohalm.cyclos.entities.customization.documents.StaticDocument;
import nl.strohalm.cyclos.services.customization.DocumentService;
import nl.strohalm.cyclos.services.customization.exceptions.CannotUploadFileException;
import nl.strohalm.cyclos.utils.ClassHelper;
import nl.strohalm.cyclos.utils.binding.BeanBinder;
import nl.strohalm.cyclos.utils.binding.DataBinder;
import nl.strohalm.cyclos.utils.binding.PropertyBinder;
import nl.strohalm.cyclos.utils.conversion.IdConverter;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;
@Controller
public class EditStaticDocumentController extends BaseRestController {
	public static <T extends StaticDocument> BeanBinder<T> getDataBinder(final Class<T> type) {
        final BeanBinder<T> binder = BeanBinder.instance(type);
        binder.registerBinder("id", PropertyBinder.instance(Long.class, "id", IdConverter.instance()));
        binder.registerBinder("name", PropertyBinder.instance(String.class, "name"));
        binder.registerBinder("description", PropertyBinder.instance(String.class, "description"));
        return binder;
    }

    protected DocumentService          documentService;

    public final DocumentService getDocumentService() {
		return documentService;
	}

	private DataBinder<StaticDocument> dataBinder;

    @Inject
    public void setDocumentService(final DocumentService documentService) {
        this.documentService = documentService;
    }

    protected Class<? extends StaticDocument> getEntityType() {
        return StaticDocument.class;
    }
    public static class EditStaticDocumentRequestDTO {
    	private long              documentId;
        private FormFile          upload;
        
        protected Map<String, Object> values;
	    public Map<String, Object> getValues() {
			return values;
		}


        public void  EditStaticDocumentForm() {
        }

        public Map<String, Object> getDocument() {
            return values;
        }

        public Object getDocument(final String key) {
            return values.get(key);
        }

        public long getDocumentId() {
            return documentId;
        }

        public FormFile getUpload() {
            return upload;
        }

        public void setDocument(final Map<String, Object> doc) {
            values = doc;
        }

        public void setDocument(final String key, final Object value) {
            values.put(key, value);
        }

        public void setDocumentId(final long documentId) {
            this.documentId = documentId;
        }

        public void setUpload(final FormFile upload) {
            this.upload = upload;
        }


	}

	public static class EditStaticDocumentResponseDTO {
		String message;
		Map<String, Object> param;
		public EditStaticDocumentResponseDTO(String message,Map<String, Object> param){
			super();
			this.message =message;
			this.param =param;
			
		}

	}
    

//    @RequestMapping(value ="admin/editStaticDocument" , method = RequestMethod.POST)
//    @ResponseBody
//    protected EditStaticDocumentResponseDTO handleSubmit(@RequestBody EditStaticDocumentRequestDTO form) throws Exception {
//        EditStaticDocumentResponseDTO response = null;
//        StaticDocument document = getDataBinder().readFromString(form.getDocument());
//        final boolean isInsert = document.getId() == null;
//        try {
//            final FormFile upload = form.getUpload();
//            document = (StaticDocument) documentService.saveStatic(document, upload.getInputStream(), upload.getFileSize(), upload.getFileName(), upload.getContentType());
//            String message =null;
//            if (isInsert) {
//                message ="document.inserted";
//            }
//            else{
//                message = "document.modified";
//                
//            }
//            
//            Map<String, Object> param = new HashMap<String, Object>();
//            param.put("documentId", document.getId());
//            response = new EditStaticDocumentResponseDTO(message, param);}
//        catch(Exception e){
//            e.printStackTrace();
//        }
//        return response ;
        
        /*context.sendMessage(isInsert ? "document.inserted" : "document.modified");
        request.setAttribute("document", document);
        
        return ActionHelper.redirectWithParam(request, context.getSuccessForward(), "documentId", document.getId());*/
//    }

    //@Override
//    protected void prepareForm(final ActionContext context) throws Exception {
//        final HttpServletRequest request = context.getRequest();
//        final EditStaticDocumentForm form = context.getForm();
//        final long id = form.getDocumentId();
//        StaticDocument document;
//        if (id > 0L) {
//            document = (StaticDocument) documentService.load(id);
//        } else {
//            document = ClassHelper.instantiate(getEntityType());
//        }
//        getDataBinder().writeAsString(form.getDocument(), document);
//        request.setAttribute("document", document);
//    }

    //@Override
//    protected void validateForm(final ActionContext context) {
//        final EditStaticDocumentForm form = context.getForm();
//        final Document document = getDataBinder().readFromString(form.getDocument());
//        documentService.validate(document, false);
//    }

//    private DataBinder<StaticDocument> getDataBinder() {
//        if (dataBinder == null) {
//            dataBinder = EditStaticDocumentAction.getDataBinder(StaticDocument.class);
//        }
//        return dataBinder;
//    }


}
