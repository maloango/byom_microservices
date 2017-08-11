package nl.strohalm.cyclos.webservices.rest.members.bulk;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.servlet.http.HttpServletRequest;

import nl.strohalm.cyclos.annotations.Inject;
import nl.strohalm.cyclos.controls.ActionContext;
import nl.strohalm.cyclos.controls.members.SearchMembersAction;
import nl.strohalm.cyclos.controls.members.bulk.MemberBulkActionsForm;
import nl.strohalm.cyclos.entities.access.Channel;
import nl.strohalm.cyclos.entities.members.FullTextMemberQuery;
import nl.strohalm.cyclos.entities.settings.LocalSettings;
import nl.strohalm.cyclos.entities.settings.events.LocalSettingsEvent;
import nl.strohalm.cyclos.services.access.ChannelService;
import nl.strohalm.cyclos.services.elements.BulkMemberActionResultVO;
import nl.strohalm.cyclos.services.elements.ElementService;
import nl.strohalm.cyclos.services.settings.SettingsService;
import nl.strohalm.cyclos.utils.binding.BeanBinder;
import nl.strohalm.cyclos.utils.binding.DataBinder;
import nl.strohalm.cyclos.utils.binding.MapBean;
import nl.strohalm.cyclos.utils.binding.SimpleCollectionBinder;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;

import org.apache.struts.action.ActionMapping;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class MemberBulkChangeChannelsController extends BaseRestController {
	private ElementService elementService;
	private SettingsService settingsService;

	public static class ChangeChannelsBean {
		Collection<Channel> enableChannels;
		Collection<Channel> disableChannels;

		public Collection<Channel> getDisableChannels() {
			return disableChannels;
		}

		public Collection<Channel> getEnableChannels() {
			return enableChannels;
		}

		public void setDisableChannels(
				final Collection<Channel> disabledChannels) {
			disableChannels = disabledChannels;
		}

		public void setEnableChannels(final Collection<Channel> enabledChannels) {
			enableChannels = enabledChannels;
		}
	}

	static void prepare(final ActionContext context,
			final ChannelService channelService) {
		final HttpServletRequest request = context.getRequest();
		final Collection<Channel> channels = channelService.list();
		// The "web" channel can not be customized by the user, so it should not
		// be sent to the JSP page
		final Channel webChannel = channelService
				.loadByInternalName(Channel.WEB);
		channels.remove(webChannel);
		request.setAttribute("channels", channels);
	}

	private DataBinder<FullTextMemberQuery> dataBinder;
	private BeanBinder<ChangeChannelsBean> beanBinder;
	private ReadWriteLock lock = new ReentrantReadWriteLock(true);
	protected ChannelService channelService;

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
	public final void setChannelService(final ChannelService channelService) {
		this.channelService = channelService;
	}

	public static class MemberBulkChangeChannelsRequestDto {

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

		protected Map<String, Object> values;

		public Map<String, Object> getValues() {
			return values;
		}

		public void setValues(final Map<String, Object> values) {
			this.values = values;
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
	}

	public static class MemberBulkChangeChannelsResponseDto {
		private String message;
		private int changed;
		private Long unChanged;

		public int getChanged() {
			return changed;
		}

		public void setChanged(int changed) {
			this.changed = changed;
		}

		public Long getUnChanged() {
			return unChanged;
		}

		public void setUnChanged(Long unChanged) {
			this.unChanged = unChanged;
		}

		public MemberBulkChangeChannelsResponseDto(String message, int changed,
				Long unChanged) {
			super();
			this.message = message;
			this.changed = changed;
			this.unChanged = unChanged;
		}

		public MemberBulkChangeChannelsResponseDto(String message, int changed) {
			super();
			this.message = message;
			this.changed = changed;
		}

		public MemberBulkChangeChannelsResponseDto(String message,
				Long unChanged) {
			super();
			this.message = message;
			this.unChanged = unChanged;
		}

		public String getMessage() {
			return message;
		}

		public void setMessage(String message) {
			this.message = message;
		}
	}

	@RequestMapping(value = "admin/memberBulkChangeChannels", method = RequestMethod.PUT)
	@ResponseBody
	protected MemberBulkChangeChannelsResponseDto formAction(
			@RequestBody MemberBulkChangeChannelsRequestDto form)
			throws Exception {
		
		final ChangeChannelsBean changeChanelsBean = getBeanBinder()
				.readFromString(form.getChangeChannels());
		final FullTextMemberQuery query = getDataBinder().readFromString(
				form.getQuery());
		final BulkMemberActionResultVO result = elementService
				.bulkChangeMemberChannels(query,
						changeChanelsBean.enableChannels,
						changeChanelsBean.disableChannels);
		MemberBulkChangeChannelsResponseDto response = null;
		if (result.getChanged() > 0 && result.getUnchanged() > 0) {
			new MemberBulkChangeChannelsResponseDto(
					"member.bulkActions.channelsChanged", result.getChanged(),
					(long) result.getUnchanged());
			return response;
		} else if (result.getChanged() > 0) {
			new MemberBulkChangeChannelsResponseDto(
					"member.bulkActions.channelsChangedForAll",
					result.getChanged());
			return response;
		} else {
			new MemberBulkChangeChannelsResponseDto(
					"member.bulkActions.channelsNotChanged",
					(long) result.getUnchanged());
			return response;
		}

	}

	protected void prepareForm(final ActionContext context) throws Exception {
		prepare(context, channelService);
	}

	protected void validateForm(final ActionContext context) {
		final MemberBulkActionsForm form = context.getForm();
		final ChangeChannelsBean changeChanelsBean = getBeanBinder()
				.readFromString(form.getChangeChannels());
		final FullTextMemberQuery query = getDataBinder().readFromString(
				form.getQuery());
		elementService.validateBulkChangeChannels(query,
				changeChanelsBean.enableChannels,
				changeChanelsBean.disableChannels);
	}

	private BeanBinder<ChangeChannelsBean> getBeanBinder() {
		if (beanBinder != null) {
			return beanBinder;
		}

		final BeanBinder<ChangeChannelsBean> beanBinder = BeanBinder
				.instance(ChangeChannelsBean.class);
		beanBinder.registerBinder("enableChannels", SimpleCollectionBinder
				.instance(Channel.class, HashSet.class, "enableIds"));
		beanBinder.registerBinder("disableChannels", SimpleCollectionBinder
				.instance(Channel.class, HashSet.class, "disableIds"));

		return beanBinder;
	}

}
