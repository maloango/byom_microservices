package nl.strohalm.cyclos.webservices.rest.customization.documents;

import java.util.Collection;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import nl.strohalm.cyclos.annotations.Inject;
import nl.strohalm.cyclos.entities.customization.documents.Document;
import nl.strohalm.cyclos.entities.customization.documents.DocumentQuery;
import nl.strohalm.cyclos.entities.groups.Group;
import nl.strohalm.cyclos.entities.members.Element;
import nl.strohalm.cyclos.entities.members.Member;
import nl.strohalm.cyclos.services.customization.DocumentService;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;

@Controller
public class ListDocumentsController extends BaseRestController {
	private DocumentService documentService;

	public DocumentService getDocumentService() {
		return documentService;
	}

	@Inject
	public void setDocumentService(final DocumentService documentService) {
		this.documentService = documentService;
	}

	public static class ListDocumentsRequestDTO {
		private boolean brokerCanViewMemberDocuments = false;
		private Member member;
		private Collection<Document.Nature> natures;
		private Element viewer;
		private Long id;
		private Collection<Group> visibleGroups;

		public Long getId() {
			return id;
		}

		public Member getMember() {
			return member;
		}

		public Collection<Document.Nature> getNatures() {
			return natures;
		}

		public Element getViewer() {
			return viewer;
		}

		public Collection<Group> getVisibleGroups() {
			return visibleGroups;
		}

		public boolean isBrokerCanViewMemberDocuments() {
			return brokerCanViewMemberDocuments;
		}

		public void setBrokerCanViewMemberDocuments(final boolean brokerCanViewMemberDocuments) {
			this.brokerCanViewMemberDocuments = brokerCanViewMemberDocuments;
		}

		public void setId(final Long id) {
			this.id = id;
		}

		public void setMember(final Member member) {
			this.member = member;
		}

		public void setNatures(final Collection<Document.Nature> natures) {
			this.natures = natures;
		}

		public void setViewer(final Element viewer) {
			this.viewer = viewer;
		}

		public void setVisibleGroups(final Collection<Group> visibleGroups) {
			this.visibleGroups = visibleGroups;
		}

	}

	public static class ListDocumentsResponseDTO {

		List<Document> docs;

		public ListDocumentsResponseDTO(List<Document> docs) {
			super();
			this.docs = docs;
		}

	}

	@RequestMapping(value = "admin/listDocuments", method = RequestMethod.GET)
	@ResponseBody
	protected ListDocumentsResponseDTO executeAction(@RequestBody ListDocumentsRequestDTO form) throws Exception {
		ListDocumentsResponseDTO response = null;
                try{
		final List<Document> docs = documentService.search(new DocumentQuery());
		response = new ListDocumentsResponseDTO(docs);}
                catch(Exception e){
                    e.printStackTrace();
                }
		return response;
	}

}
