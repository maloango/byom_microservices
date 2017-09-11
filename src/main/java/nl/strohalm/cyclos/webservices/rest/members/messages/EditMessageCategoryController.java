///*
// * To change this license header, choose License Headers in Project Properties.
// * To change this template file, choose Tools | Templates
// * and open the template in the editor.
// */
//package nl.strohalm.cyclos.webservices.rest.members.messages;
//
//import javax.servlet.http.HttpServletRequest;
//import nl.strohalm.cyclos.annotations.Inject;
//import nl.strohalm.cyclos.controls.ActionContext;
//import nl.strohalm.cyclos.entities.members.messages.MessageCategory;
//import nl.strohalm.cyclos.services.elements.MessageCategoryService;
//import nl.strohalm.cyclos.utils.binding.BeanBinder;
//import nl.strohalm.cyclos.utils.binding.DataBinder;
//import nl.strohalm.cyclos.utils.binding.PropertyBinder;
//import nl.strohalm.cyclos.utils.conversion.IdConverter;
//import nl.strohalm.cyclos.webservices.rest.BaseRestController;
//import nl.strohalm.cyclos.webservices.rest.GenericResponse;
//import org.springframework.stereotype.Controller;
//
///**
// *
// * @author Lue
// */
//@Controller
//public class EditMessageCategoryController extends BaseRestController{
//    
//    private MessageCategoryService      messageCategoryService;
//    private DataBinder<MessageCategory> dataBinder;
//
//    public DataBinder<MessageCategory> getDataBinder() {
//
//        if (dataBinder == null) {
//            final BeanBinder<MessageCategory> binder = BeanBinder.instance(MessageCategory.class);
//            binder.registerBinder("id", PropertyBinder.instance(Long.class, "id", IdConverter.instance()));
//            binder.registerBinder("name", PropertyBinder.instance(String.class, "name"));
//            dataBinder = binder;
//        }
//        return dataBinder;
//    }
//
//    public MessageCategoryService getMessageCategoryService() {
//        return messageCategoryService;
//    }
//
//    @Inject
//    public void setMessageCategoryService(final MessageCategoryService messageCategoryService) {
//        this.messageCategoryService = messageCategoryService;
//    }
//    public static class EditMessageCategoryRequest extends GenericResponse{
//        public static class 
//        
//    }
//
//   // @Override
//    protected void formAction(final ActionContext context) throws Exception {
//        final EditMessageCategoryForm form = context.getForm();
//        final MessageCategory category = getDataBinder().readFromString(form.getMessageCategory());
//        final boolean insert = category.getId() == null;
//        messageCategoryService.save(category);
//        context.sendMessage(insert ? "messageCategory.inserted" : "messageCategory.modified");
//    }
//
//    @Override
//    protected void prepareForm(final ActionContext context) throws Exception {
//        final EditMessageCategoryForm form = context.getForm();
//        final HttpServletRequest request = context.getRequest();
//        MessageCategory messageCategory;
//        if (form.getMessageCategoryId() > 0) {
//            messageCategory = messageCategoryService.load(form.getMessageCategoryId());
//        } else {
//            messageCategory = new MessageCategory();
//        }
//
//        getDataBinder().writeAsString(form.getMessageCategory(), messageCategory);
//        request.setAttribute("messageCategory", messageCategory);
//    }
//
//   // @Override
//    protected void validateForm() {
//        //final EditMessageCategoryForm form = context.getForm();
//        final MessageCategory messageCategory = getDataBinder().readFromString(form.getMessageCategory());
//        getMessageCategoryService().validate(messageCategory);
//    }
//
//    
//}
