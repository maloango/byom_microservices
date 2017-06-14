package nl.strohalm.cyclos.webservices.rest.members.documents;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import nl.strohalm.cyclos.access.AdminMemberPermission;
import nl.strohalm.cyclos.access.BrokerPermission;
import nl.strohalm.cyclos.annotations.Inject;
import nl.strohalm.cyclos.entities.Entity;
import nl.strohalm.cyclos.entities.customization.documents.Document;
import nl.strohalm.cyclos.entities.customization.documents.DocumentQuery;
import nl.strohalm.cyclos.entities.members.Element;
import nl.strohalm.cyclos.entities.members.Member;
import nl.strohalm.cyclos.services.customization.DocumentService;
import nl.strohalm.cyclos.services.elements.ElementService;
import nl.strohalm.cyclos.services.permissions.PermissionService;
import nl.strohalm.cyclos.utils.validation.ValidationException;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;
@Controller
public class SelectDocumentController extends BaseRestController{
	 private DocumentService documentService;
	 private PermissionService permissionService;
	 private ElementService elementService;

	    @Inject
	    public void setDocumentService(final DocumentService documentService) {
	        this.documentService = documentService;
	    }
	    
	    public static class SelectDocumenRequestDTO{
	    	private long              memberId;
	

	        public long getMemberId() {
	            return memberId;
	        }

	        public void setMemberId(final long memberId) {
	            this.memberId = memberId;
	        }

			public Entity getElement() {
				// TODO Auto-generated method stub
				return null;
			}

			public boolean isBrokerOf(Member member) {
				// TODO Auto-generated method stub
				return false;
			}

			public boolean isOperator() {
				// TODO Auto-generated method stub
				return false;
			}

			public boolean isAdmin() {
				// TODO Auto-generated method stub
				return false;
			}
	    }
	    	public static class SelectDocumenResponseDTO {
	    		
	    		String message;
	    		public final String getMessage() {
					return message;
				}
				public final void setMessage(String message) {
					this.message = message;
				}
				private boolean member;
	    		private boolean documents;
	    		private boolean myDocuments;
	    		public SelectDocumenResponseDTO(Member member2, List<Document> documents2, boolean myDocuments2,
						boolean byBroker2, boolean adminCanManage2, boolean brokerCanManage2) {
					// TODO Auto-generated constructor stub
				}
				public final boolean isMember() {
					return member;
				}
				public final void setMember(boolean member) {
					this.member = member;
				}
				public final boolean isDocuments() {
					return documents;
				}
				public final void setDocuments(boolean documents) {
					this.documents = documents;
				}
				public final boolean isMyDocuments() {
					return myDocuments;
				}
				public final void setMyDocuments(boolean myDocuments) {
					this.myDocuments = myDocuments;
				}
				public final boolean isByBroker() {
					return byBroker;
				}
				public final void setByBroker(boolean byBroker) {
					this.byBroker = byBroker;
				}
				public final boolean isAdminCanManage() {
					return adminCanManage;
				}
				public final void setAdminCanManage(boolean adminCanManage) {
					this.adminCanManage = adminCanManage;
				}
				public final boolean isBrokerCanManage() {
					return brokerCanManage;
				}
				public final void setBrokerCanManage(boolean brokerCanManage) {
					this.brokerCanManage = brokerCanManage;
				}
				public final boolean isRemoved() {
					return removed;
				}
				public final void setRemoved(boolean removed) {
					this.removed = removed;
				}
				private boolean byBroker;
	    		private boolean adminCanManage;
	    		private boolean brokerCanManage;
	    		private boolean removed;
	    		
	    	}
	    
	    
	     

	    @RequestMapping(value = "/member/selectDocument",method = RequestMethod.GET)
	    @ResponseBody
	    
	    protected SelectDocumenResponseDTO executeAction(@RequestBody SelectDocumenRequestDTO form) throws Exception {
	        
	        boolean myDocuments = false;
	        boolean adminCanManage = false;
	        boolean brokerCanManage = false;
	        boolean byBroker = false;
	        long memberId = form.getMemberId();
	        Member member = null;
	        if (memberId > 0L) {
	            try {
	                member = elementService.load(memberId, Element.Relationships.USER);
	                if (memberId == form.getElement().getId()) {
	                    myDocuments = true;
	                } else {
	                    byBroker = form.isBrokerOf(member);
	                    adminCanManage = permissionService.hasPermission(AdminMemberPermission.DOCUMENTS_MANAGE_MEMBER);
	                    brokerCanManage = permissionService.hasPermission(BrokerPermission.DOCUMENTS_MANAGE_MEMBER);
	                }
	            } catch (final Exception e) {
	                // Just ignore it
	            }
	        } else {
	            if (form.isAdmin() || form.isOperator()) {
	                throw new ValidationException();
	            }
	            member = (Member) form.getElement();
	            memberId = member.getId();
	            myDocuments = true;
	        }
	        if (member == null) {
	            throw new ValidationException();
	        }
	        List<Document> documents;
	        final DocumentQuery documentQuery = new DocumentQuery();
	        documentQuery.setMember(member);
	        documents = documentService.search(documentQuery);
	        SelectDocumenResponseDTO resposne = new SelectDocumenResponseDTO(member,documents,myDocuments,byBroker,adminCanManage,brokerCanManage);
	        String message = null;
	        message = "removed";
	        return resposne;
	    }


}
