/*package nl.strohalm.cyclos.webservices.rest.operators;

import org.springframework.stereotype.Controller;

import nl.strohalm.cyclos.controls.restapi.BaseRestController;
import nl.strohalm.cyclos.entities.customization.fields.CustomFieldValue;
import nl.strohalm.cyclos.entities.customization.fields.OperatorCustomFieldValue;
import nl.strohalm.cyclos.entities.members.FullTextOperatorQuery;
import nl.strohalm.cyclos.services.groups.GroupService;
@Controller
public class SearchOperatorsController extends BaseRestController{
	private GroupService groupService;
	//@Override
    protected Class<? extends CustomFieldValue> getCustomFieldValueClass() {
        return OperatorCustomFieldValue.class;
    }

   // @Override
    protected Class<FullTextOperatorQuery> getQueryClass() {
        return FullTextOperatorQuery.class;
    }*/
  /*  

   @RequestMapping(value = "", method = RequestMethod.GET)
   @ResponseBody
    protected QueryParameters prepareForm(final ActionContext context) {
        final HttpServletRequest request = context.getRequest();
        final Member loggedMember = context.getElement();

        final GroupQuery possibleGroupQuery = new GroupQuery();
        possibleGroupQuery.setNatures(Group.Nature.OPERATOR);
        possibleGroupQuery.setStatus(Group.Status.NORMAL);
        possibleGroupQuery.setMember(loggedMember);
        final List<? extends Group> possibleNewGroups = groupService.search(possibleGroupQuery);
        if (possibleNewGroups.isEmpty()) {
            throw new ValidationException(new ValidationError("operator.noGroup"));
        }

        final FullTextOperatorQuery query = (FullTextOperatorQuery) super.prepareForm(context);
        query.setMember(loggedMember);
        query.setEnabled(null);

        // Store the groups
        final GroupQuery groupQuery = new GroupQuery();
        groupQuery.setNatures(Group.Nature.OPERATOR);
        groupQuery.setMember(loggedMember);
        request.setAttribute("groups", groupService.search(groupQuery));

        // Store the possible groups for new operator
        request.setAttribute("possibleNewGroups", possibleNewGroups);

        return query;
    }
*/

