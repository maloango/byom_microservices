package nl.strohalm.cyclos.webservices.rest.access.transactionpassword;

import javax.servlet.http.HttpServletRequest;

import nl.strohalm.cyclos.controls.ActionContext;
import nl.strohalm.cyclos.controls.access.transactionpassword.ManageTransactionPasswordForm;
import nl.strohalm.cyclos.entities.access.OperatorUser;
import nl.strohalm.cyclos.entities.access.TransactionPassword;
import nl.strohalm.cyclos.entities.access.User;
import nl.strohalm.cyclos.entities.members.Element;
import nl.strohalm.cyclos.entities.members.Operator;
import nl.strohalm.cyclos.services.access.AccessService;
import nl.strohalm.cyclos.services.elements.ElementService;
import nl.strohalm.cyclos.services.elements.ResetTransactionPasswordDTO;
import nl.strohalm.cyclos.utils.ActionHelper;
import nl.strohalm.cyclos.utils.RelationshipHelper;
import nl.strohalm.cyclos.utils.RequestHelper;
import nl.strohalm.cyclos.utils.validation.ValidationException;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;

import org.apache.struts.action.ActionForward;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class ManageTransactionPasswordController extends BaseRestController {
	// later will be the implementation if required..

	private AccessService accessService;
	private ElementService elementService;

	public static class ManageTransactionPasswordRequestDto {
		private long userId;
		private boolean block;
		private boolean embed;

		public long getUserId() {
			return userId;
		}

		public boolean isBlock() {
			return block;
		}

		public boolean isEmbed() {
			return embed;
		}

		public void setBlock(final boolean block) {
			this.block = block;
		}

		public void setEmbed(final boolean standalone) {
			embed = standalone;
		}

		public void setUserId(final long userId) {
			this.userId = userId;
		}
	}

	public static class ManageTransactionPasswordResponseDto {
		private String message;
		private Long userId;

		public ManageTransactionPasswordResponseDto(String message, Long userId) {
			super();
			this.message = message;
			this.userId = userId;
		}

		public Long getUserId() {
			return userId;
		}

		public void setUserId(Long userId) {
			this.userId = userId;
		}

		public String getMessage() {
			return message;
		}

		public void setMessage(String message) {
			this.message = message;
		}
	}

	@RequestMapping(value = "", method = RequestMethod.POST)
	@ResponseBody
	protected ManageTransactionPasswordResponseDto handleSubmit(
			@RequestBody ManageTransactionPasswordRequestDto form)
			throws Exception {
		// final ManageTransactionPasswordForm form = context.getForm();
		User user = retrieveUser(form);
		final boolean block = form.isBlock();
		final ResetTransactionPasswordDTO dto = new ResetTransactionPasswordDTO();
		dto.setUser(user);
		dto.setAllowGeneration(!block);
		user = accessService.resetTransactionPassword(dto);
		String message = null;
		if (block)
			message = "transactionPassword.blocked";
		else
			message = "transactionPassword.reset";
		Long userId = user.getId();
		ManageTransactionPasswordResponseDto response = new ManageTransactionPasswordResponseDto(
				message, userId);
		return response;
	}

	private User retrieveUser(final ManageTransactionPasswordRequestDto form) {
		// final HttpServletRequest request = context.getRequest();
		// if (request.getAttribute("element") != null) {
		// The element may be already retrieved on the manage passwords
		// action
		// return ((Element) request.getAttribute("element")).getUser();
		// }

		// final ManageTransactionPasswordForm form = context.getForm();
		User user;
		final long userId = form.getUserId();
		try {
			user = elementService.loadUser(userId, RelationshipHelper.nested(
					User.Relationships.ELEMENT, Element.Relationships.GROUP));
			if (user instanceof OperatorUser) {
				Element element = user.getElement();
				element = elementService.load(element.getId(),
						RelationshipHelper.nested(
								Operator.Relationships.MEMBER,
								Element.Relationships.GROUP));
			}
		} catch (final Exception e) {
			throw new ValidationException();
		}
		return user;
	}

}