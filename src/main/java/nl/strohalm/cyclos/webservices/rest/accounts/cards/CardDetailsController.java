package nl.strohalm.cyclos.webservices.rest.accounts.cards;

import org.springframework.stereotype.Controller;

import nl.strohalm.cyclos.entities.settings.events.LocalSettingsChangeListener;
import nl.strohalm.cyclos.entities.settings.events.LocalSettingsEvent;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;
@Controller
public class CardDetailsController extends BaseRestController implements LocalSettingsChangeListener {
	// not required as per requirement later will implements ......................

	@Override
	public void onLocalSettingsUpdate(LocalSettingsEvent event) {
		// TODO Auto-generated method stub
		
	}
}
