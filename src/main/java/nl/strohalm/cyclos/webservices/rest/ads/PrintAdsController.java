package nl.strohalm.cyclos.webservices.rest.ads;

import org.springframework.stereotype.Controller;

import nl.strohalm.cyclos.webservices.rest.BaseRestController;

@Controller
public class PrintAdsController extends BaseRestController {
	

	// later will be the implementation if required..
/*
	protected AbstractAdQuery prepareForm(final ActionContext context) {
		final HttpServletRequest request = context.getRequest();

		final AbstractAdQuery query = super.prepareForm(context);
		query.fetch(RelationshipHelper.nested(Ad.Relationships.CATEGORY,
				RelationshipHelper.nested(AdCategory.MAX_LEVEL,
						AdCategory.Relationships.PARENT)),
				Ad.Relationships.CUSTOM_VALUES);

		// Store the ad custom values
		request.setAttribute("adFields", adCustomFieldService.list());

		// Calculate since date
		final TimePeriod since = query.getSince();
		Calendar sinceDate = null;
		if (since != null && since.isValid()) {
			sinceDate = since.remove(Calendar.getInstance());
		}
		request.setAttribute("sinceDate", sinceDate);
		return query;
	}*/
}
