package nl.strohalm.cyclos.webservices.rest.members.bulks;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
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

	public static class MemberBulkChangeGroupRequestDto extends
			HashMap<String, Object> {
		private Member broker;
		private Period activationPeriod;
		private Collection<GroupFilter> groupFilters;
		private SortOrder memberSortOrder;
		private boolean withImagesOnly;

		public Period getActivationPeriod() {
			return activationPeriod;
		}

		public Member getBroker() {
			return broker;
		}

		public Collection<GroupFilter> getGroupFilters() {
			return groupFilters;
		}

		public SortOrder getMemberSortOrder() {
			return memberSortOrder;
		}

		public Nature getNature() {
			return Nature.MEMBER;
		}

		public Class<? extends ElementQuery> getQueryClass() {
			return MemberQuery.class;
		}

		public boolean isWithImagesOnly() {
			return withImagesOnly;
		}

		public void setActivationPeriod(final Period activationPeriod) {
			this.activationPeriod = activationPeriod;
		}

		public void setBroker(final Member broker) {
			this.broker = broker;
		}

		public void setGroupFilters(final Collection<GroupFilter> groupFilters) {
			this.groupFilters = groupFilters;
		}

		public void setMemberSortOrder(final SortOrder memberSortOrder) {
			this.memberSortOrder = memberSortOrder;
		}

		public void setWithImagesOnly(final boolean withImagesOnly) {
			this.withImagesOnly = withImagesOnly;
		}

		public Map<String, Object> getQuery() {
			return values;
		}

		public Object getQuery(final String key) {
			return values.get(key);
		}

		public void setQuery(final Map<String, Object> query) {
			values = query;
		}

		public void setQuery(final String key, final Object value) {
			values.put(key, value);
		}

		protected Map<String, Object> values;

		public Map<String, Object> getValues() {
			return values;
		}

		public void setValues(final Map<String, Object> values) {
			this.values = values;
		}

		boolean isArray= false;
		private Map<String, Class<?>> propertyTypes = new HashMap<String, Class<?>>();

		public boolean contains(final String name, final String key) {
			return get(name, key) != null;
		}

		public Object get(final Object key) {
			final String name = (String) key;
			if (!propertyTypes.containsKey(key)) {
				propertyTypes
						.put(name, isArray ? Object[].class : Object.class);
			}
			return super.get(key);
		}

		public Object get(final String name) {
			return this.get((Object) name);
		}

		public Object get(final String name, final int index) {
			final Object value = this.get(name);
			if (value != null) {
				if (value instanceof List<?>) {
					return ((List<?>) value).get(index);
				} else if (value.getClass().isArray()) {
					return Array.get(value, index);
				}
			}
			return null;
		}

		public Object get(final String name, final String key) {
			final Object value = this.get(name);
			if (value != null) {
				if (value instanceof DynaBean) {
					return ((DynaBean) value).get(key);
				} else {
					try {
						return PropertyUtils.getProperty(value, key);
					} catch (final Exception e) {
						// Keep on
					}
				}
			}
			return null;
		}

		public DynaClass getDynaClass() {
			final LazyDynaClass dynaClass = new LazyDynaClass();
			for (final Map.Entry<String, Class<?>> entry : propertyTypes
					.entrySet()) {
				dynaClass.add(entry.getKey(), entry.getValue());
			}
			return dynaClass;
		}

		public Object put(final String key, final Object value) {
			final String name = key;
			if (!propertyTypes.containsKey(key)) {
				propertyTypes
						.put(name, isArray ? Object[].class : Object.class);
			}
			return super.put(key, value);
		}

		public void remove(final String name, final String key) {
			final Object value = this.get(name);
			if (value != null) {
				if (value instanceof DynaBean) {
					((DynaBean) value).set(key, null);
				} else {
					try {
						PropertyUtils.setProperty(value, key, null);
					} catch (final Exception e) {
						// Keep on
					}
				}
			}
		}

		@SuppressWarnings("unchecked")
		public void set(final String name, final int index, final Object value) {
			final Object bean = this.get(name);
			if (bean != null) {
				if (bean instanceof List) {
					((List<Object>) bean).set(index, value);
				} else if (value.getClass().isArray()) {
					Array.set(bean, index, value);
				}
			}
		}

		public void set(final String name, final Object value) {
			put(name, value);
		}

		public void set(final String name, final String key, final Object value) {
			final Object bean = this.get(name);
			if (bean != null) {
				if (bean instanceof DynaBean) {
					((DynaBean) bean).set(key, value);
				} else {
					try {
						PropertyUtils.setProperty(bean, key, value);
					} catch (final Exception e) {
						// Keep on
					}
				}
			}
		}

		public void setArray(final String name) {
			setType(name, Object[].class);
		}

		public void setType(final String name, final Class<?> type) {
			propertyTypes.put(name, type);
		}

	}

	public static class MemberBulkChangeGroupResponseDto {
		private String message;

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

		// Read the user input
		//final MapBean bean = form.getChangeGroup();
		final FullTextMemberQuery query = getDataBinder().readFromString(
				form.getQuery());
		final MemberGroup newGroup = groupService.load(CoercionHelper.coerce(
				Long.class, bean.get("newGroup")));
		final String comments = StringUtils.trimToNull((String) bean
				.get("comments"));

		final BulkMemberActionResultVO results = elementService
				.bulkChangeMemberGroup(query, newGroup, comments);
		context.sendMessage("member.bulkActions.groupChanged",
				results.getChanged(), results.getUnchanged(),
				newGroup.getName());

		// Clear the change group parameters
		form.getChangeGroup().clear();
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
