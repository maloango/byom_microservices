package nl.strohalm.cyclos.webservices.rest.accounts.cards;

import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.springframework.stereotype.Controller;

import nl.strohalm.cyclos.controls.restapi.BaseRestController;
import nl.strohalm.cyclos.entities.access.User;
import nl.strohalm.cyclos.entities.accounts.cards.CardQuery;
import nl.strohalm.cyclos.entities.settings.events.LocalSettingsChangeListener;
import nl.strohalm.cyclos.entities.settings.events.LocalSettingsEvent;
import nl.strohalm.cyclos.services.accounts.cards.CardService;
import nl.strohalm.cyclos.services.elements.ElementService;
import nl.strohalm.cyclos.services.settings.SettingsService;
import nl.strohalm.cyclos.utils.binding.DataBinder;
@Controller
public class CardDetailsController extends BaseRestController implements LocalSettingsChangeListener {
	// not required as per requirement later will implements ......................

	@Override
	public void onLocalSettingsUpdate(LocalSettingsEvent event) {
		// TODO Auto-generated method stub
		
	}
}
