package nl.strohalm.cyclos.webservices.rest.alerts;

import java.util.Collection;

import org.springframework.stereotype.Controller;

import nl.strohalm.cyclos.annotations.Inject;
import nl.strohalm.cyclos.entities.alerts.Alert;
import nl.strohalm.cyclos.entities.groups.MemberGroup;
import nl.strohalm.cyclos.entities.members.Member;
import nl.strohalm.cyclos.services.alerts.AlertService;
import nl.strohalm.cyclos.utils.Period;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;

@Controller
public class BaseListAlertsController extends BaseRestController {

	private static final int MAX_ALERTS = 200;

	private AlertService alertService;

	public AlertService getAlertService() {
		return alertService;
	}

	@Inject
	public void setAlertService(final AlertService alertService) {
		this.alertService = alertService;
	}

	public static class BaseListAlertsRequestDTO {

		private Member member;
		private Collection<MemberGroup> groups;
		private Period period;
		private boolean showRemoved;
		private Alert.Type type;
		private String key;

		public BaseListAlertsRequestDTO() {
			super();
		}

		public Collection<MemberGroup> getGroups() {
			return groups;
		}

		public String getKey() {
			return key;
		}

		public Member getMember() {
			return member;
		}

		public Period getPeriod() {
			return period;
		}

		public Alert.Type getType() {
			return type;
		}

		public boolean isShowRemoved() {
			return showRemoved;
		}

		public void setGroups(final Collection<MemberGroup> groups) {
			this.groups = groups;
		}

		public void setKey(final String key) {
			this.key = key;
		}

		public void setMember(final Member member) {
			this.member = member;
		}

		public void setPeriod(final Period period) {
			this.period = period;
		}

		public void setShowRemoved(final boolean showRemoved) {
			this.showRemoved = showRemoved;
		}

		public void setType(final Alert.Type type) {
			this.type = type;
		}

	}

	public static class BaseListAlertsResponseDTO {
		private boolean alerts;
		private boolean isSystem;
		public final boolean isAlerts() {
			return alerts;
		}
		public final void setAlerts(boolean alerts) {
			this.alerts = alerts;
		}
		public final boolean isSystem() {
			return isSystem;
		}
		public final void setSystem(boolean isSystem) {
			this.isSystem = isSystem;
		}
		public final boolean isMember() {
			return isMember;
		}
		public final void setMember(boolean isMember) {
			this.isMember = isMember;
		}
		private boolean isMember;
		
		
	}

}

// need to implement the code ..