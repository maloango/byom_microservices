package nl.strohalm.cyclos.webservices.rest.groups.accounts;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import nl.strohalm.cyclos.entities.accounts.MemberAccountType;
import nl.strohalm.cyclos.entities.groups.MemberGroup;
import nl.strohalm.cyclos.services.groups.GroupService;
import nl.strohalm.cyclos.utils.EntityHelper;
import nl.strohalm.cyclos.utils.validation.ValidationException;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;

@Controller
public class RemoveGroupAccountController extends BaseRestController {
	private GroupService groupService;

	public GroupService getGroupService() {
		return groupService;
	}

	public void setGroupService(GroupService groupService) {
		this.groupService = groupService;
	}

	public static class RemoveGroupAccountRequestDto {
		private long groupId;
		private long accountTypeId;

		public long getAccountTypeId() {
			return accountTypeId;
		}

		public long getGroupId() {
			return groupId;
		}

		public void setAccountTypeId(final long accountTypeId) {
			this.accountTypeId = accountTypeId;
		}

		public void setGroupId(final long groupId) {
			this.groupId = groupId;
		}
	}

	public static class RemoveGroupAccountResponseDto {
		public String message;

		public String getMessage() {
			return message;
		}

		public void setMessage(String message) {
			this.message = message;
		}
	}

	@RequestMapping(value = "/admin/removeGroupAccount", method = RequestMethod.DELETE)
	@ResponseBody
	protected RemoveGroupAccountResponseDto executeAction(
			@RequestBody RemoveGroupAccountRequestDto form) throws Exception {
		// final RemoveGroupAccountForm form = context.getForm();
		final long groupId = form.getGroupId();
		final long accountTypeId = form.getAccountTypeId();
		if (groupId <= 0L || accountTypeId <= 0L) {
			throw new ValidationException();
		}
		RemoveGroupAccountResponseDto response = new RemoveGroupAccountResponseDto();
		try {
			groupService.removeAccountTypeRelationship(EntityHelper.reference(
					MemberGroup.class, groupId), EntityHelper.reference(
					MemberAccountType.class, accountTypeId));
			response.setMessage("group.account.removed");
		} catch (final Exception e) {
			response.setMessage("group.account.error.removing");
		}
		return response;
	}
}
