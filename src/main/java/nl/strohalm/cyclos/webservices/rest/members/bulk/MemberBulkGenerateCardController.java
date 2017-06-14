package nl.strohalm.cyclos.webservices.rest.members.bulk;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import nl.strohalm.cyclos.annotations.Inject;
import nl.strohalm.cyclos.controls.ActionContext;
import nl.strohalm.cyclos.controls.members.SearchMembersAction;
import nl.strohalm.cyclos.controls.members.bulk.MemberBulkActionsForm;
import nl.strohalm.cyclos.entities.customization.fields.MemberCustomFieldValue;
import nl.strohalm.cyclos.entities.members.FullTextMemberQuery;
import nl.strohalm.cyclos.entities.settings.LocalSettings;
import nl.strohalm.cyclos.entities.settings.events.LocalSettingsEvent;
import nl.strohalm.cyclos.services.accounts.cards.CardService;
import nl.strohalm.cyclos.services.elements.BulkMemberActionResultVO;
import nl.strohalm.cyclos.services.settings.SettingsService;
import nl.strohalm.cyclos.utils.binding.DataBinder;
import nl.strohalm.cyclos.utils.binding.MapBean;
import nl.strohalm.cyclos.utils.conversion.CoercionHelper;
import nl.strohalm.cyclos.utils.validation.RequiredError;
import nl.strohalm.cyclos.utils.validation.ValidationException;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;

public class MemberBulkGenerateCardController extends BaseRestController {
	private DataBinder<FullTextMemberQuery> dataBinder;
	private CardService cardService;
	private SettingsService settingsService;
	private ReadWriteLock lock = new ReentrantReadWriteLock(true);

	public CardService getCardService() {
		return cardService;
	}

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

	/**
	 * @see nl.strohalm.cyclos.entities.settings.events.LocalSettingsChangeListener#onLocalSettingsUpdate(nl.strohalm.cyclos.entities.settings.events.LocalSettingsEvent)
	 */

	public void onLocalSettingsUpdate(final LocalSettingsEvent event) {
		try {
			lock.writeLock().lock();
			dataBinder = null;
		} finally {
			lock.writeLock().unlock();
		}
	}

	@Inject
	public void setCardService(final CardService cardService) {
		this.cardService = cardService;
	}

	public static class MemberBulkGenerateCardRequestDto {
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

		public Object getQuery() {
			return values;
		}

		protected Map<String, Object> values;

		public Map<String, Object> getValues() {
			return values;
		}

		public void setValues(final Map<String, Object> values) {
			this.values = values;
		}
	}

	public static class MemberBulkGenerateCardResponseDto {
		private String message;
		private int changed;
		private int unChanged;

		public MemberBulkGenerateCardResponseDto(String message, int changed,
				int unChanged) {
			super();
			this.message = message;
			this.changed = changed;
			this.unChanged = unChanged;
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
	protected MemberBulkGenerateCardResponseDto formAction(
			@RequestBody MemberBulkGenerateCardRequestDto form)
			throws Exception {
		final MapBean bean = form.getGenerateCard();
		final FullTextMemberQuery query = getDataBinder().readFromString(
				form.getQuery());
		final boolean generateForPending = CoercionHelper.coerce(Boolean.TYPE,
				bean.get("generateForPending"));
		final boolean generateForActive = CoercionHelper.coerce(Boolean.TYPE,
				bean.get("generateForActive"));

		final BulkMemberActionResultVO results = cardService
				.bulkGenerateNewCard(query, generateForPending,
						generateForActive);
		String message = "member.bulkActions.cardGenerated";
		int changed = results.getChanged();
		int unchanged = results.getUnchanged();

		// Clear the generate card parameters
		form.getGenerateCard().clear();
		MemberBulkGenerateCardResponseDto response = new MemberBulkGenerateCardResponseDto(
				message, changed, unchanged);
		return response;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected void validateForm(final ActionContext context) {
		final MemberBulkActionsForm form = context.getForm();
		final MapBean bean = form.getGenerateCard();
		final FullTextMemberQuery query = getDataBinder().readFromString(
				form.getQuery());
		final String comments = StringUtils.trimToNull((String) bean
				.get("comments"));

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
		if (StringUtils.isEmpty(comments)) {
			throw new ValidationException("comments", "remark.comments",
					new RequiredError());
		}
	}
}
