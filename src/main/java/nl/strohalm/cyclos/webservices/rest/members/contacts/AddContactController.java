package nl.strohalm.cyclos.webservices.rest.members.contacts;

import org.springframework.stereotype.Controller;

import nl.strohalm.cyclos.webservices.rest.BaseRestController;
@Controller
public class AddContactController extends BaseRestController{
	
//	private ContactService contactService;
//    private ResponseHelper responseHelper;
// 
//
//    public ContactService getContactService() {
//        return contactService;
//    }
//
//    @Inject
//    public void setContactService(final ContactService contactService) {
//        this.contactService = contactService;
//    }
//
//    @Inject
//    public void setResponseHelper(final ResponseHelper responseHelper) {
//        this.responseHelper = responseHelper;
//    }
//    
//    public static class AddContactRequestDTO{
//    	private long              memberId;
//        private boolean           direct;
//
//        public long getMemberId() {
//            return memberId;
//        }
//
//        public boolean isDirect() {
//            return direct;
//        }
//
//        public void setDirect(final boolean direct) {
//            this.direct = direct;
//        }
//
//        public void setMemberId(final long memberId) {
//            this.memberId = memberId;
//        }
//    }
//    
//    public static class AddContactResponseDTO{
//    	private boolean Member;
//    	String message;
//
//		public final String getMessage() {
//			return message;
//		}
//
//		public final void setMessage(String message) {
//			this.message = message;
//		}
//
//		public boolean isMember() {
//			return Member;
//		}
//
//		public void setMember(boolean member) {
//			Member = member;
//		}
//    }
//
//    @RequestMapping(value = "", method = RequestMethod.PUT)
//    @ResponseBody
//   
//    protected AddContactResponseDTO executeAction(@RequestBody AddContactRequestDTO form) throws Exception {
//        final long memberId = form.getMemberId();
//
//        if (RequestHelper.isValidation(form.getRequest())) {
//            try {
//                if (memberId <= 0L) {
//                    throw new ValidationException("contact", "member.member", new RequiredError());
//                }
//                responseHelper.writeValidationSuccess(form.getResponse());
//            } catch (final ValidationException e) {
//                responseHelper.writeValidationErrors(form.getResponse(), e);
//            }
//            return null;
//
//        }
//        if (memberId <= 0L) {
//            throw new ValidationException();
//        }
//
//        final Contact contact = new Contact();
//        final Member member = (Member) form.getAccountOwner();
//        contact.setOwner(member);
//        contact.setContact(EntityHelper.reference(Member.class, memberId));
//        try {
//            contactService.save(contact);
//            AddContactResponseDTO response = new AddContactResponseDTO();
//            String message = null;
//            if (condition) {
//				message ="contact.inserted";
//			}
//            else{
//            	message = "contact.error.alreadyExists";
//            }
//            /*response.setMessage("contact.inserted");
//        } catch (final ContactAlreadyExistException e) {
//        	response.sendMessage("contact.error.alreadyExists");*/
//        }
//
//        if (form.isDirect()) {
//            return context.findForward("backToList");
//        } else {
//           // return ActionHelper.redirectWithParam(context.getRequest(), context.findForward("backToProfile"), "memberId", memberId);
//        }
//    }


}
