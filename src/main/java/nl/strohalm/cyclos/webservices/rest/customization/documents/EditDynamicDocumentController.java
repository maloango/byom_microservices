package nl.strohalm.cyclos.webservices.rest.customization.documents;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import nl.strohalm.cyclos.annotations.Inject;
import nl.strohalm.cyclos.entities.customization.documents.DocumentPage;
import nl.strohalm.cyclos.entities.customization.documents.DynamicDocument;
import nl.strohalm.cyclos.services.customization.DocumentService;
import nl.strohalm.cyclos.utils.CustomizationHelper;
import nl.strohalm.cyclos.utils.binding.BeanBinder;
import nl.strohalm.cyclos.utils.binding.DataBinder;
import nl.strohalm.cyclos.utils.binding.PropertyBinder;
import nl.strohalm.cyclos.utils.conversion.CoercionConverter;
import nl.strohalm.cyclos.utils.conversion.IdConverter;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;
@Controller
public class EditDynamicDocumentController extends BaseRestController {
	
	private DocumentService             documentService;
    private DataBinder<DynamicDocument> dataBinder;
    private CustomizationHelper         customizationHelper;
    

    public final DocumentService getDocumentService() {
		return documentService;
	}

	public final CustomizationHelper getCustomizationHelper() {
		return customizationHelper;
	}

	public final void setDataBinder(DataBinder<DynamicDocument> dataBinder) {
		this.dataBinder = dataBinder;
	}

	public DataBinder<DynamicDocument> getDataBinder() {
        if (dataBinder == null) {
            final BeanBinder<DocumentPage> formPageBinder = BeanBinder.instance(DocumentPage.class, "formPage");
            formPageBinder.registerBinder("id", PropertyBinder.instance(Long.class, "id", IdConverter.instance()));
            formPageBinder.registerBinder("contents", PropertyBinder.instance(String.class, "contents", CoercionConverter.instance(String.class)));

            final BeanBinder<DocumentPage> documentPageBinder = BeanBinder.instance(DocumentPage.class, "documentPage");
            documentPageBinder.registerBinder("id", PropertyBinder.instance(Long.class, "id", IdConverter.instance()));
            documentPageBinder.registerBinder("contents", PropertyBinder.instance(String.class, "contents", CoercionConverter.instance(String.class)));

            final BeanBinder<DynamicDocument> binder = BeanBinder.instance(DynamicDocument.class);
            binder.registerBinder("id", PropertyBinder.instance(Long.class, "id", IdConverter.instance()));
            binder.registerBinder("name", PropertyBinder.instance(String.class, "name"));
            binder.registerBinder("description", PropertyBinder.instance(String.class, "description"));
            binder.registerBinder("formPage", formPageBinder);
            binder.registerBinder("documentPage", documentPageBinder);
            dataBinder = binder;
        }
        return dataBinder;
    }

    @Inject
    public void setCustomizationHelper(final CustomizationHelper customizationHelper) {
        this.customizationHelper = customizationHelper;
    }

    @Inject
    public void setDocumentService(final DocumentService documentService) {
        this.documentService = documentService;
    }
	public static class EditDynamicDocumentRequestDTO {
		 private long              documentId;
		 protected Map<String, Object> values;

			public Map<String, Object> getValues() {
				return values;
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

		    public void setDocument(final Map<String, Object> doc) {
		        values = doc;
		    }

		    public void setDocument(final String key, final Object value) {
		        values.put(key, value);
		    }

		    public void setDocumentId(final long documentId) {
		        this.documentId = documentId;
		    }

		}

		
	public static class EditDynamicDocumentResponseDTO {
		String message;
		Map<String, Object> param;
		public EditDynamicDocumentResponseDTO(String message, Map<String, Object> param) {
			super();
			this.message = message;
			this.param = param;
		}

	}

    @RequestMapping(value ="/admin/editDynamicDocument",method =RequestMethod.POST)
    @ResponseBody
    protected EditDynamicDocumentResponseDTO handleSubmit(@RequestBody  EditDynamicDocumentRequestDTO form) throws Exception {
       // final HttpServletRequest request = context.getRequest();
        //final EditDynamicDocumentForm form = form.getForm();
        DynamicDocument document = getDataBinder().readFromString(form.getDocument());
        final boolean isInsert = document.getId() == null;
        document = (DynamicDocument) documentService.saveDynamic(document);

        // Physically update the form and document pages
        if (document.isHasFormPage()) {
            final File formFile = customizationHelper.formFile(document);
            customizationHelper.updateFile(formFile, document.getFormPage());
        }

        final File documentFile = customizationHelper.documentFile(document);
        customizationHelper.updateFile(documentFile, document.getDocumentPage());
        Map<String, Object> param = new HashMap<String, Object>();
        String message = null;
        if (isInsert) {
        	message = "documentFile.inserted";
        }
        	else {
        		message = "documentFile.modified";
        	}
        //Map<String, Object> param = new HashMap<String, Object>();
        param.put("documentId", document.getId());
        EditDynamicDocumentResponseDTO response = new EditDynamicDocumentResponseDTO(message, param);
        return response;
    }
    

		
       // form.setMessage(isInsert ? "document.inserted" : "document.modified");
       // return ActionHelper.redirectWithParam(request, context.getSuccessForward(), "documentId", document.getId());
    

   /* @Override
    protected void prepareForm(final ActionContext context) throws Exception {
        final HttpServletRequest request = context.getRequest();
        final EditDynamicDocumentForm form = context.getForm();
        final long id = form.getDocumentId();
        DynamicDocument document;
        if (id > 0L) {
            document = (DynamicDocument) documentService.load(id, DynamicDocument.Relationships.FORM_PAGE, DynamicDocument.Relationships.DOCUMENT_PAGE);
        } else {
            document = new DynamicDocument();
        }
        getDataBinder().writeAsString(form.getDocument(), document);
        request.setAttribute("document", document);
    }

    @Override
    protected void validateForm(final ActionContext context) {
        final EditDynamicDocumentForm form = context.getForm();
        final Document document = retrieveDocument(form);
        documentService.validate(document, false);
    }

    private DynamicDocument retrieveDocument(final EditDynamicDocumentForm form) {
        final DynamicDocument document = getDataBinder().readFromString(form.getDocument());
        final DocumentPage formPage = document.getFormPage();
        if (formPage == null || StringUtils.isEmpty(formPage.getContents())) {
            document.setFormPage(null);
        } else {
            final File formFile = customizationHelper.formFile(document);
            formPage.setName(formFile.getName());
        }
        final DocumentPage documentPage = document.getDocumentPage();
        if (documentPage == null || StringUtils.isEmpty(documentPage.getContents())) {
            document.setDocumentPage(null);
        } else {
            final File documentFile = customizationHelper.documentFile(document);
            documentPage.setName(documentFile.getName());
        }
        return document;
    }*/

}




