package nl.strohalm.cyclos.webservices.rest.members.pending;

import java.util.Collection;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import nl.strohalm.cyclos.annotations.Inject;
import nl.strohalm.cyclos.controls.ActionContext;
import nl.strohalm.cyclos.controls.members.pending.SearchPendingMembersForm;
import nl.strohalm.cyclos.entities.access.User;
import nl.strohalm.cyclos.entities.customization.fields.MemberCustomField;
import nl.strohalm.cyclos.entities.customization.fields.MemberCustomFieldValue;
import nl.strohalm.cyclos.entities.groups.MemberGroup;
import nl.strohalm.cyclos.entities.members.Member;
import nl.strohalm.cyclos.entities.members.PendingMember;
import nl.strohalm.cyclos.entities.members.PendingMemberQuery;
import nl.strohalm.cyclos.entities.settings.AccessSettings;
import nl.strohalm.cyclos.entities.settings.AccessSettings.UsernameGeneration;
import nl.strohalm.cyclos.entities.settings.LocalSettings;
import nl.strohalm.cyclos.entities.settings.events.LocalSettingsEvent;
import nl.strohalm.cyclos.services.customization.MemberCustomFieldService;
import nl.strohalm.cyclos.services.elements.ElementService;
import nl.strohalm.cyclos.services.settings.SettingsService;
import nl.strohalm.cyclos.utils.Period;
import nl.strohalm.cyclos.utils.RelationshipHelper;
import nl.strohalm.cyclos.utils.binding.DataBinder;
import nl.strohalm.cyclos.utils.conversion.CustomFieldConverter;
import nl.strohalm.cyclos.utils.csv.CSVWriter;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;
@Controller
public class ExportPendingMembersToCsvController extends BaseRestController{
	private DataBinder<PendingMemberQuery> dataBinder;
    private MemberCustomFieldService       memberCustomFieldService;
    private SettingsService settingsService;
    private ElementService elementService;

    public MemberCustomFieldService getMemberCustomFieldService() {
        return memberCustomFieldService;
    }

   //@Override
    public void onLocalSettingsUpdate(final LocalSettingsEvent event) {
        //super.onLocalSettingsUpdate(event);
        dataBinder = null;
    }

    @Inject
    public void setMemberCustomFieldService(final MemberCustomFieldService memberCustomFieldService) {
        this.memberCustomFieldService = memberCustomFieldService;
    }
    
    public static class ExportPendingMembersToCsvRequestDTo{
    	 private Collection<MemberGroup>            groups;
    	    private String                             name;
    	    private Period                             creationPeriod;
    	    private Member                             broker;
    	    private Collection<MemberCustomFieldValue> customValues;

    	    public Member getBroker() {
    	        return broker;
    	    }

    	    public Period getCreationPeriod() {
    	        return creationPeriod;
    	    }

    	    public Collection<MemberCustomFieldValue> getCustomValues() {
    	        return customValues;
    	    }

    	    public Collection<MemberGroup> getGroups() {
    	        return groups;
    	    }

    	    public String getName() {
    	        return name;
    	    }

    	    public void setBroker(final Member broker) {
    	        this.broker = broker;
    	    }

    	    public void setCreationPeriod(final Period creationPeriod) {
    	        this.creationPeriod = creationPeriod;
    	    }

    	    public void setCustomValues(final Collection<MemberCustomFieldValue> customValues) {
    	        this.customValues = customValues;
    	    }

    	    public void setGroups(final Collection<MemberGroup> groups) {
    	        this.groups = groups;
    	    }

    	    public void setName(final String name) {
    	        this.name = name;
    	    }
    	
    }
    
    public static class ExportPendingMembersToCsvResponse{
    	String message;

		public final String getMessage() {
			return message;
		}

		public final void setMessage(String message) {
			this.message = message;
		}
    	
    	
    }

    @RequestMapping(value = "",method = RequestMethod.GET)
    @ResponseBody
    protected List<?> executeQuery(final ActionContext context) {
        final SearchPendingMembersForm form = context.getForm();
        final PendingMemberQuery query = getDataBinder().readFromString(form.getQuery());
        query.fetch(PendingMember.Relationships.CUSTOM_VALUES, PendingMember.Relationships.MEMBER, RelationshipHelper.nested(PendingMember.Relationships.BROKER));
        return elementService.search(query);
    }

   // @Override
    protected String fileName(final ActionContext context) {
        final User loggedUser = context.getUser();
        return "pending_members_" + loggedUser.getUsername() + ".csv";
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
   // @Override
    protected CSVWriter resolveCSVWriter(final ActionContext context) {
        final LocalSettings settings = settingsService.getLocalSettings();
        final AccessSettings accessSettings = settingsService.getAccessSettings();

        final CSVWriter<PendingMember> csv = CSVWriter.instance(PendingMember.class, settings);
        if (accessSettings.getUsernameGeneration() == UsernameGeneration.NONE) {
            csv.addColumn(context.message("member.username"), "username");
        }
        csv.addColumn(context.message("member.name"), "name");
        csv.addColumn(context.message("member.email"), "email");
        csv.addColumn(context.message("member.creationDate"), "creationDate", settings.getDateConverter());
        csv.addColumn(context.message("member.group"), "memberGroup.name");
        final List<MemberCustomField> customFields = memberCustomFieldService.list();
        for (final MemberCustomField field : customFields) {
            csv.addColumn(field.getName(), "customValues", new CustomFieldConverter(field, elementService, settings));
        }
        if (context.isAdmin()) {
            csv.addColumn(context.message("member.brokerUsername"), "broker.username");
            csv.addColumn(context.message("member.brokerName"), "broker.name");
        }
        return csv;
    }

    private DataBinder<PendingMemberQuery> getDataBinder() {
        if (dataBinder == null) {
            final LocalSettings settings = settingsService.getLocalSettings();
            dataBinder = SearchPendingMembersAction.createDataBinder(settings);
        }
        return dataBinder;
    }

}
