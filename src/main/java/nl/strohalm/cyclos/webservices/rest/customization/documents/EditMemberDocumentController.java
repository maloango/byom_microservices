package nl.strohalm.cyclos.webservices.rest.customization.documents;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts.action.ActionForward;
import org.apache.struts.upload.FormFile;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import nl.strohalm.cyclos.access.AdminMemberPermission;
import nl.strohalm.cyclos.access.BrokerPermission;
import nl.strohalm.cyclos.controls.ActionContext;
import nl.strohalm.cyclos.controls.customization.documents.EditMemberDocumentForm;
import nl.strohalm.cyclos.controls.customization.documents.EditStaticDocumentForm;
import nl.strohalm.cyclos.controls.restapi.BaseRestController;
import nl.strohalm.cyclos.entities.customization.documents.Document;
import nl.strohalm.cyclos.entities.customization.documents.MemberDocument;
import nl.strohalm.cyclos.entities.customization.documents.StaticDocument;
import nl.strohalm.cyclos.entities.members.Member;
import nl.strohalm.cyclos.services.customization.DocumentService;
import nl.strohalm.cyclos.services.customization.exceptions.CannotUploadFileException;
import nl.strohalm.cyclos.services.permissions.PermissionService;
import nl.strohalm.cyclos.utils.ActionHelper;
import nl.strohalm.cyclos.utils.EntityHelper;
import nl.strohalm.cyclos.utils.binding.BeanBinder;
import nl.strohalm.cyclos.utils.binding.DataBinder;
import nl.strohalm.cyclos.utils.binding.PropertyBinder;
import nl.strohalm.cyclos.utils.validation.ValidationException;
@Controller
public class EditMemberDocumentController extends BaseRestController {

    private DataBinder<MemberDocument> dataBinder;
    private DocumentService documentService;
    private PermissionService permissionService;

  

  // @Override
    protected Class<? extends StaticDocument> getEntityType() {
        return MemberDocument.class;
    }
   public static class EditMemberDocumentRequestDTO {
	   
	   
	   private long              documentId;
	    private FormFile          upload;
	    
	    
	    protected Map<String, Object> values;
	    public Map<String, Object> getValues() {
			return values;
		}

	    public void EditStaticDocumentForm() {
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
   
   
   
   

	public static class EditMemberDocumentResponseDTO {
		String message;
		Map<String, Object> param;
		public EditMemberDocumentResponseDTO(String message,Map<String, Object> param){
			super();
			this.message =message;
			this.param =param;
			
		}

	}

   

    @RequestMapping(value ="/admin/editMemberDocument" , method = RequestMethod.POST)
    @ResponseBody
    protected EditMemberDocumentResponseDTO handleSubmit(@RequestBody EditMemberDocumentRequestDTO form ) throws Exception {
        //final HttpServletRequest request = context.getRequest();
        //final EditStaticDocumentForm form = context.getForm();
        MemberDocument document = getDataBinder().readFromString(form.getDocument());
        final boolean isInsert = document.getId() == null;
        try {
            final FormFile upload = form.getUpload();
            document = (MemberDocument) documentService.saveStatic(document, upload.getInputStream(), upload.getFileSize(), upload.getFileName(), upload.getContentType());
            String message =null;
            if (isInsert) {
				message ="document.inserted";
            }
				else{
					message ="document.modified";
				}
            Map<String, Object> param = new HashMap<String, Object>();
            param.put("documentId", document.getId());
            EditMemberDocumentResponseDTO response = new EditMemberDocumentResponseDTO(message, param);
            return response ;
        
            //context.sendMessage(isInsert ? "document.inserted" : "document.modified");
            //response.setAttribute("document", document);
            //return ActionHelper.redirectWithParam(request, context.getSuccessForward(), "documentId", document.getId());
        } catch (final IOException e) {
            throw new CannotUploadFileException(e);}
        }
    

   
    protected void prepareForm(final ActionContext context) throws Exception {
        final HttpServletRequest request = context.getRequest();
        final EditMemberDocumentForm form = context.getForm();
        Member member;
        final long documentId = form.getDocumentId();
        MemberDocument document;
        if (documentId > 0L) {
            document = (MemberDocument) documentService.load(documentId);
            member = document.getMember();
        } else {
            final long memberId = form.getMemberId();
            if (memberId < 1) {
                throw new ValidationException();
            }
            member = EntityHelper.reference(Member.class, memberId);
            document = new MemberDocument();
            document.setMember(member);
        }
        final boolean byBroker = context.isBrokerOf(member) && permissionService.hasPermission(BrokerPermission.DOCUMENTS_MANAGE_MEMBER);
        final boolean adminCanManage = context.isAdmin() && permissionService.hasPermission(AdminMemberPermission.DOCUMENTS_MANAGE_MEMBER);

        getDataBinder().writeAsString(form.getDocument(), document);
        request.setAttribute("member", member);
        request.setAttribute("document", document);
        request.setAttribute("byBroker", byBroker);
        request.setAttribute("adminCanManage", adminCanManage);
        final List<MemberDocument.Visibility> visibilities = new ArrayList<MemberDocument.Visibility>();
        visibilities.add(MemberDocument.Visibility.BROKER);
        visibilities.add(MemberDocument.Visibility.MEMBER);
        if (!byBroker) {
            visibilities.add(MemberDocument.Visibility.ADMIN);
        }
        request.setAttribute("visibilities", visibilities);
    }

    
    protected void validateForm(final ActionContext context) {
        final EditStaticDocumentForm form = context.getForm();
        final Document document = getDataBinder().readFromString(form.getDocument());
        documentService.validate(document, false);
    }

    private DataBinder<MemberDocument> getDataBinder() {
        if (dataBinder == null) {
            final BeanBinder<MemberDocument> beanBinder = EditStaticDocumentAction.getDataBinder(MemberDocument.class);
            beanBinder.registerBinder("member", PropertyBinder.instance(Member.class, "member"));
            beanBinder.registerBinder("visibility", PropertyBinder.instance(MemberDocument.Visibility.class, "visibility"));
            dataBinder = beanBinder;
        }
        return dataBinder;
    }
}

    

   