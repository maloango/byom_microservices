package nl.strohalm.cyclos.webservices.rest.customization.translationMessages;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts.action.ActionForward;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import nl.strohalm.cyclos.controls.ActionContext;
//import nl.strohalm.cyclos.controls.settings.ManageSettingsAction.Action;
import nl.strohalm.cyclos.entities.settings.Setting;
import nl.strohalm.cyclos.services.customization.MessageImportType;
import nl.strohalm.cyclos.utils.RequestHelper;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;

@Controller
public class ManageTranslationMessagesController extends BaseRestController {
	// later will be the implementation if required..
}
