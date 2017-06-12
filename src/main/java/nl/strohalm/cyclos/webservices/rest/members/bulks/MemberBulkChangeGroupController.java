package nl.strohalm.cyclos.webservices.rest.members.bulks;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.servlet.http.HttpServletRequest;

import nl.strohalm.cyclos.controls.ActionContext;
import nl.strohalm.cyclos.controls.members.SearchMembersAction;
import nl.strohalm.cyclos.controls.members.bulk.MemberBulkActionsForm;
import nl.strohalm.cyclos.entities.customization.fields.MemberCustomFieldValue;
import nl.strohalm.cyclos.entities.groups.Group;
import nl.strohalm.cyclos.entities.groups.GroupFilter;
import nl.strohalm.cyclos.entities.groups.GroupQuery;
import nl.strohalm.cyclos.entities.groups.MemberGroup;
import nl.strohalm.cyclos.entities.members.ElementQuery;
import nl.strohalm.cyclos.entities.members.FullTextMemberQuery;
import nl.strohalm.cyclos.entities.members.Member;
import nl.strohalm.cyclos.entities.members.MemberQuery;
import nl.strohalm.cyclos.entities.members.Element.Nature;
import nl.strohalm.cyclos.entities.settings.LocalSettings;
import nl.strohalm.cyclos.entities.settings.LocalSettings.SortOrder;
import nl.strohalm.cyclos.entities.settings.events.LocalSettingsEvent;
import nl.strohalm.cyclos.services.elements.BulkMemberActionResultVO;
import nl.strohalm.cyclos.services.elements.ElementService;
import nl.strohalm.cyclos.services.groups.GroupService;
import nl.strohalm.cyclos.services.settings.SettingsService;
import nl.strohalm.cyclos.utils.Period;
import nl.strohalm.cyclos.utils.binding.DataBinder;
import nl.strohalm.cyclos.utils.binding.MapBean;
import nl.strohalm.cyclos.utils.conversion.CoercionHelper;
import nl.strohalm.cyclos.utils.validation.RequiredError;
import nl.strohalm.cyclos.utils.validation.ValidationException;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;

import org.apache.commons.beanutils.DynaBean;
import org.apache.commons.beanutils.DynaClass;
import org.apache.commons.beanutils.LazyDynaClass;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.struts.action.ActionMapping;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class MemberBulkChangeGroupController extends BaseRestController {
	static void prepare(final ActionContext context,
			final GroupService groupService) {
		final HttpServletRequest request = context.getRequest();
		final GroupQuery query = new GroupQuery();
		query.setNatures(Group.Nature.MEMBER, Group.Nature.BROKER);
		query.setStatus(Group.Status.NORMAL);
		request.setAttribute("possibleNewGroups", groupService.search(query));
	}

	private DataBinder<FullTextMemberQuery> dataBinder;
	private GroupService groupService;
	private ElementService elementService;
	private SettingsService settingsService;

	private ReadWriteLock lock = new ReentrantReadWriteLock(true);

	public DataBinder<FullTextMemberQuery> getDataBinder() {
		try {
			lock.readLock().lock();

			if (dataBinder == null) {
				final LocalSettings localSettings = settingsService
						.getLocalSettings();
				dataBinder = SearchMembersAction
						.memberQueryDataBinder(localSettings);
			}
			return dataBinder;
		} finally {
			lock.readLock().unlock();
		}
	}

	public void onLocalSettingsUpdate(final LocalSettingsEvent event) {

		try {
			lock.writeLock().lock();
			dataBinder = null;
		} finally {
			lock.writeLock().unlock();
		}

	}

	public static class MemberBulkChangeGroupRequestDto {
		private MapBean changeGroup = new MapBean("newGroup", "comments");
		private MapBean changeBroker = new MapBean("newBroker", "comments",
				"suspendCommission");
		private MapBean generateCard = new MapBean("newCard", "comments",
				"generateForPending", "generateForActive");
		private MapBean changeChannels = new MapBean(true, "enableIds",
				"disableIds");

		public Map<String, Object> getQuery() {
			return values;
		}

		public Object getQuery(final String key) {
			return values.get(key);
		}

		protected Map<String, Object> values;

		public Map<String, Object> getValues() {
			return values;
		}

		public void setValues(final Map<String, Object> values) {
			this.values = values;
		}

		public void setQuery(final Map<String, Object> query) {
			values = query;
		}

		public void setQuery(final String key, final Object value) {
			values.put(key, value);
		}

		public MapBean getChangeBroker() {
			return changeBroker;
		}

		public MapBean getChangeChannels() {
			return changeChannels;
		}

		public MapBean getChangeGroup() {
			return changeGroup;
		}

		public MapBean getGenerateCard() {
			return generateCard;
		}

		public void setChangeBroker(final MapBean changeBroker) {
			this.changeBroker = changeBroker;
		}

		public void setChangeChannels(final MapBean changeChannels) {
			this.changeChannels = changeChannels;
		}

		public void setChangeGroup(final MapBean changeGroup) {
			this.changeGroup = changeGroup;
		}

		public void setGenerateCard(final MapBean generateCard) {
			this.generateCard = generateCard;
		}
	}

	public static class MemberBulkChangeGroupResponseDto {
		private String message;
		int changed;
		int unchanged;
		String name;

		public MemberBulkChangeGroupResponseDto(String message, int changed,
				int unchanged, String name) {
			super();
			this.message = message;
			this.changed = changed;
			this.unchanged = unchanged;
			this.name = name;
		}

		public String getMessage() {
			return message;
		}

		public void setMessage(String message) {
			this.message = message;
		}
	}

	@RequestMapping(value = "/admin/managePasswords", method = RequestMethod.POST)
	@ResponseBody
	protected MemberBulkChangeGroupResponseDto formAction(
			final MemberBulkChangeGroupRequestDto form) throws Exception {
		// final MemberBulkActionsForm form = context.getForm();
		String message = null;
		// Read the user input
		final MapBean bean = form.getChangeGroup();
		final FullTextMemberQuery query = getDataBinder().readFromString(
				form.getQuery());
		final MemberGroup newGroup = groupService.load(CoercionHelper.coerce(
				Long.class, bean.get("newGroup")));
		final String comments = StringUtils.trimToNull((String) bean
				.get("comments"));

		final BulkMemberActionResultVO results = elementService
				.bulkChangeMemberGroup(query, newGroup, comments);
		message = "member.bulkActions.groupChanged";

		int changed = results.getChanged();
		int unchanged = results.getUnchanged();
		String name = newGroup.getName();
		MemberBulkChangeGroupResponseDto response = new MemberBulkChangeGroupResponseDto(
				message, changed, unchanged, name);
		// Clear the change group parameters
		form.getChangeGroup().clear();
		return response;
	}

	protected void prepareForm(final ActionContext context) throws Exception {
		prepare(context, groupService);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected void validateForm(final ActionContext context) {
		final MemberBulkActionsForm form = context.getForm();

		final FullTextMemberQuery query = getDataBinder().readFromString(
				form.getQuery());

		final Collection<MemberCustomFieldValue> customValues = (Collection<MemberCustomFieldValue>) query
				.getCustomValues();
		for (final Iterator it = customValues.iterator(); it.hasNext();) {
			final MemberCustomFieldValue fieldValue = (MemberCustomFieldValue) it
					.next();
			if (StringUtils.isEmpty(fieldValue.getValue())) {
				it.remove();
			}
		}
		if (CollectionUtils.isEmpty(query.getGroupFilters())
				&& CollectionUtils.isEmpty(query.getGroups())
				&& query.getBroker() == null
				&& CollectionUtils.isEmpty(customValues)) {
			throw new ValidationException("member.bulkActions.error.emptyQuery");
		}

		final MapBean bean = form.getChangeGroup();
		final MemberGroup newGroup = CoercionHelper.coerce(MemberGroup.class,
				bean.get("newGroup"));
		final String comments = StringUtils.trimToNull((String) bean
				.get("comments"));
		if (newGroup == null || newGroup.isTransient()) {
			throw new ValidationException("newGroup", "changeGroup.new",
					new RequiredError());
		}
		if (StringUtils.isEmpty(comments)) {
			throw new ValidationException("comments", "remark.comments",
					new RequiredError());
		}
	}

}
