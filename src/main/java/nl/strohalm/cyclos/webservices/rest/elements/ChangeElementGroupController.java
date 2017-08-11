package nl.strohalm.cyclos.webservices.rest.elements;

import org.springframework.stereotype.Controller;

import nl.strohalm.cyclos.webservices.rest.BaseRestController;
@Controller
public class ChangeElementGroupController extends BaseRestController{
	 /*private RemarkService remarkService;
	 private ElementService elementService;

	    public final ElementService getElementService() {
		return elementService;
	}

	public final void setElementService(ElementService elementService) {
		this.elementService = elementService;
	}

		public RemarkService getRemarkService() {
	        return remarkService;
	    }

	    @Inject
	    public void setRemarkService(final RemarkService remarkService) {
	        this.remarkService = remarkService;
	    }
	    public static class RequestDTOChangeElement{
	    	private long              elementId;
	        private long              newGroupId;
	        private String            comments;

	        public String getComments() {
	            return comments;
	        }

	        public long getElementId() {
	            return elementId;
	        }

	        public long getNewGroupId() {
	            return newGroupId;
	        }

	        public void setComments(final String comments) {
	            this.comments = comments;
	        }

	        public void setElementId(final long elementId) {
	            this.elementId = elementId;
	        }

	        public void setNewGroupId(final long newGroupId) {
	            this.newGroupId = newGroupId;
	        }
	    }
	    public static class ResponseDTOChangeElement{
	    	String message;
	    	List<GroupRemark> history;
			public ResponseDTOChangeElement(List<GroupRemark> history) {
				super();
				this.history = history;
			}
			public final String getMessage() {
				return message;
			}
			public final void setMessage(String message) {
				this.message = message;
			}
			public final List<GroupRemark> getHistory() {
				return history;
			}
			public final void setHistory(List<GroupRemark> history) {
				this.history = history;
			}
	    }

	    @RequestMapping(value = "",method = RequestMethod.PUT)
	    @ResponseBody
	    protected ResponseDTOChangeElement formAction(@RequestBody RequestDTOChangeElement form) throws Exception {
	        //final ChangeElementGroupForm form = context.getForm();
	        final String comments = form.getComments();
	        final Element element = elementService.load(form.getElementId());
	        final Group newGroup = EntityHelper.reference(Group.class, form.getNewGroupId());
	        elementService.changeGroup(element, newGroup, comments);
	        ResponseDTOChangeElement response = new ResponseDTOChangeElement(null);
			return response;
	    }

	   // @Override
	    protected void prepareForm(final ActionContext context) throws Exception {
	        final HttpServletRequest request = context.getRequest();
	        final ChangeElementGroupForm form = context.getForm();
	        Element element = null;
	        try {
	            element = elementService.load(form.getElementId(), Element.Relationships.GROUP);
	            final Element loggedElement = context.getElement();
	            if (loggedElement.equals(element)) {
	                throw new Exception();
	            }
	        } catch (final Exception e) {
	            element = null;
	        }
	        if (element == null) {
	            throw new ValidationException();
	        }

	        // Retrieve the possible new groups
	        final List<? extends Group> possible = elementService.getPossibleNewGroups(element);
	        final Group currentGroup = element.getGroup();
	        form.setNewGroupId(currentGroup.getId());
	        request.setAttribute("permanentlyRemoved", currentGroup.getStatus() == Group.Status.REMOVED);

	        // Retrieve the history
	        final List<GroupRemark> history = remarkService.listGroupChangeHistory(element);

	        request.setAttribute("element", element);
	        request.setAttribute("possibleGroups", possible);
	        request.setAttribute("history", history);
	    }

	    //@Override
	    protected void validateForm(final ActionContext context) {
	        final ChangeElementGroupForm form = context.getForm();
	        final ValidationException val = new ValidationException();
	        val.setPropertyKey("elementId", "member.member");
	        val.setPropertyKey("newGroupId", "changeGroup.new");
	        val.setPropertyKey("comments", "remark.comments");
	        if (form.getElementId() <= 0) {
	            val.addPropertyError("elementId", new RequiredError());
	        }
	        if (form.getNewGroupId() <= 0) {
	            val.addPropertyError("newGroupId", new RequiredError());
	        }
	        if (StringUtils.isEmpty(form.getComments())) {
	            val.addPropertyError("comments", new RequiredError());
	        }
	        val.throwIfHasErrors();
	    }*/
}
