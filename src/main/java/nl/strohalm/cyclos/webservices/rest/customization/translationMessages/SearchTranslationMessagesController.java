package nl.strohalm.cyclos.webservices.rest.customization.translationMessages;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import nl.strohalm.cyclos.annotations.Inject;
import nl.strohalm.cyclos.controls.ActionContext;
//import nl.strohalm.cyclos.controls.customization.translationMessages.SearchTranslationMessagesForm;
import nl.strohalm.cyclos.entities.customization.translationMessages.TranslationMessage;
import nl.strohalm.cyclos.entities.customization.translationMessages.TranslationMessageQuery;
import nl.strohalm.cyclos.services.customization.TranslationMessageService;
import nl.strohalm.cyclos.utils.binding.BeanBinder;
import nl.strohalm.cyclos.utils.binding.DataBinder;
import nl.strohalm.cyclos.utils.binding.DataBinderHelper;
import nl.strohalm.cyclos.utils.binding.PropertyBinder;
import nl.strohalm.cyclos.utils.query.QueryParameters;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;
import nl.strohalm.cyclos.webservices.rest.GenericResponse;

@Controller
public class SearchTranslationMessagesController extends BaseRestController {
    
    public static class TranslationMessageResponse extends GenericResponse {
        
        private List<TranslationMessageEntiy> translationMessages;
        
        public List<TranslationMessageEntiy> getTranslationMessages() {
            return translationMessages;
        }
        
        public void setTranslationMessages(List<TranslationMessageEntiy> translationMessages) {
            this.translationMessages = translationMessages;
        }
        
    }
    
    public static class TranslationMessageEntiy {
        
        private String key;
        private String value;
        private Long id;
        
        public String getKey() {
            return key;
        }
        
        public void setKey(String key) {
            this.key = key;
        }
        
        public String getValue() {
            return value;
        }
        
        public void setValue(String value) {
            this.value = value;
        }
        
        public Long getId() {
            return id;
        }
        
        public void setId(Long id) {
            this.id = id;
        }
        
    }
    
    public static class TranslationMessageParameters {

        private String value;
        private String key;
        private boolean showOnlyEmpity;
        
        public String getValue() {
            return value;
        }
        
        public void setValue(String value) {
            this.value = value;
        }
        
        public String getKey() {
            return key;
        }
        
        public void setKey(String key) {
            this.key = key;
        }
        
        public boolean isShowOnlyEmpity() {
            return showOnlyEmpity;
        }
        
        public void setShowOnlyEmpity(boolean showOnlyEmpity) {
            this.showOnlyEmpity = showOnlyEmpity;
        }
        
    }
    
    @RequestMapping(value = "admin/searchTranslationMessages", method = RequestMethod.POST)
    @ResponseBody
    protected TranslationMessageResponse executeQuery(@RequestBody TranslationMessageParameters params) {
        TranslationMessageResponse response = new TranslationMessageResponse();
        final TranslationMessageQuery query = new TranslationMessageQuery();
        query.setKey(params.getKey());
        query.setValue(params.getValue());
        query.setShowOnlyEmpty(params.isShowOnlyEmpity());
        final List<TranslationMessage> translationMessages = translationMessageService.search(query);
        List<TranslationMessageEntiy> translationMessageList = new ArrayList();
        for (TranslationMessage msg : translationMessages) {
            TranslationMessageEntiy entity = new TranslationMessageEntiy();
            entity.setId(msg.getId());
            entity.setKey(msg.getKey());
            entity.setValue(msg.getValue());
            translationMessageList.add(entity);
        }
        response.setTranslationMessages(translationMessageList);
        response.setStatus(0);
        return response;
    }
    
}
