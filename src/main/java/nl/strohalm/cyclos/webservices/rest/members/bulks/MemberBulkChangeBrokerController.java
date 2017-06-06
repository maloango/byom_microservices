package nl.strohalm.cyclos.webservices.rest.members.bulks;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import nl.strohalm.cyclos.annotations.Inject;
import nl.strohalm.cyclos.controls.ActionContext;
import nl.strohalm.cyclos.controls.members.SearchMembersAction;
import nl.strohalm.cyclos.controls.members.bulk.MemberBulkActionsForm;
import nl.strohalm.cyclos.entities.customization.fields.MemberCustomFieldValue;
import nl.strohalm.cyclos.entities.members.FullTextMemberQuery;
import nl.strohalm.cyclos.entities.members.Member;
import nl.strohalm.cyclos.entities.settings.LocalSettings;
import nl.strohalm.cyclos.entities.settings.events.LocalSettingsEvent;
import nl.strohalm.cyclos.services.elements.BrokeringService;
import nl.strohalm.cyclos.services.elements.BulkMemberActionResultVO;
import nl.strohalm.cyclos.services.elements.ElementService;
import nl.strohalm.cyclos.services.settings.SettingsService;
import nl.strohalm.cyclos.utils.binding.DataBinder;
import nl.strohalm.cyclos.utils.binding.MapBean;
import nl.strohalm.cyclos.utils.conversion.CoercionHelper;
import nl.strohalm.cyclos.utils.validation.RequiredError;
import nl.strohalm.cyclos.utils.validation.ValidationException;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class MemberBulkChangeBrokerController extends BaseRestController {
	private BrokeringService brokeringService;
	private DataBinder<FullTextMemberQuery> dataBinder;
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

	@Inject
	public void setBrokeringService(final BrokeringService brokeringService) {
		this.brokeringService = brokeringService;
	}

	public static class MemberBulkChangeBrokerRequestDto {
		private MapBean changeGroup = new MapBean("newGroup", "comments");
		private MapBean changeBroker = new MapBean("newBroker", "comments",
				"suspendCommission");
		private MapBean generateCard = new MapBean("newCard", "comments",
				"generateForPending", "generateForActive");
		private MapBean changeChannels = new MapBean(true, "enableIds",
				"disableIds");

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
	}

	public static class MemberBulkChangeBrokerResponseDto {
		private String message;
		private int changed;
		private int unchanged;
		private String name;

		public MemberBulkChangeBrokerResponseDto(String message, int changed,
				int unchanged, String name) {
			super();
			this.message = message;
			this.changed = changed;
			this.unchanged = unchanged;
			this.name = name;
		}

		public int getChanged() {
			return changed;
		}

		public void setChanged(int changed) {
			this.changed = changed;
		}

		public int getUnchanged() {
			return unchanged;
		}

		public void setUnchanged(int unchanged) {
			this.unchanged = unchanged;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
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
	protected MemberBulkChangeBrokerResponseDto formAction(
			@RequestBody MemberBulkChangeBrokerRequestDto form)
			throws Exception {
		// final MemberBulkActionsForm form = context.getForm();

		// Read the user input
		final MapBean bean = form.getChangeBroker();
		final FullTextMemberQuery query = getDataBinder().readFromString(
				form.getQuery());
		final Member newBroker = elementService.load(CoercionHelper.coerce(
				Long.class, bean.get("newBroker")));
		final boolean suspendCommission = CoercionHelper.coerce(Boolean.TYPE,
				bean.get("suspendCommission"));
		final String comments = StringUtils.trimToNull((String) bean
				.get("comments"));

		final BulkMemberActionResultVO results = brokeringService
				.bulkChangeMemberBroker(query, newBroker, suspendCommission,
						comments);

		String message = "member.bulkActions.brokerChanged";
		int changed = results.getChanged();
		int unchanged = results.getUnchanged();
		String name = newBroker.getName();
		MemberBulkChangeBrokerResponseDto response = new MemberBulkChangeBrokerResponseDto(
				message, changed, unchanged, name);

		// Clear the change broker parameters
		form.getChangeBroker().clear();
		return response;
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

		final MapBean bean = form.getChangeBroker();
		final Member newBroker = CoercionHelper.coerce(Member.class,
				bean.get("newBroker"));
		final String comments = StringUtils.trimToNull((String) bean
				.get("comments"));
		if (newBroker == null || newBroker.isTransient()) {
			throw new ValidationException("newBroker", "changeBroker.new",
					new RequiredError());
		}
		if (StringUtils.isEmpty(comments)) {
			throw new ValidationException("comments", "remark.comments",
					new RequiredError());
		}
	}

}
