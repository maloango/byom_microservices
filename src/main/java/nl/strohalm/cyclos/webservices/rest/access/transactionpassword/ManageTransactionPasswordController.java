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
	private AccessService accessService;

	public final AccessService getAccessService() {
		return accessService;
	}

	public final void setAccessService(AccessService accessService) {
		this.accessService = accessService;
	}

	public final ElementService getElementService() {
		return elementService;
	}

	public final void setElementService(ElementService elementService) {
		this.elementService = elementService;
	}

	private ElementService elementService;

	public static class ManageTransactionPasswordRequestDTO {
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

	public static class ManageTransactionPasswordResponseDTO {

		private User user;
		String message;

		public ManageTransactionPasswordResponseDTO(String message) {
			this.message = message;
		}

		public final User getUser() {
			return user;
		}

		public final void setUser(User user) {
			this.user = user;
		}

		public final boolean isCanBlock() {
			return canBlock;
		}

		public final void setCanBlock(boolean canBlock) {
			this.canBlock = canBlock;
		}

		private boolean canBlock;
	}

	@RequestMapping(value = "/member/manageTransactionPassword", method = RequestMethod.PUT)
	@ResponseBody
	protected ManageTransactionPasswordResponseDTO handleSubmit(final ManageTransactionPasswordRequestDTO form)
			throws Exception {
		// final ManageTransactionPasswordForm form = context.getForm();
		User user = retrieveUser(form);
		final boolean block = form.isBlock();
		final ResetTransactionPasswordDTO dto = new ResetTransactionPasswordDTO();
		dto.setUser(user);
		dto.setAllowGeneration(!block);
		user = accessService.resetTransactionPassword(dto);
		String message = null;
		if (block) {
			message = "transactionPassword.blocked";
		} else {
			message = "transactionPassword.reset";
		}
		ManageTransactionPasswordResponseDTO response = new ManageTransactionPasswordResponseDTO(message);
		return response;
	}

	private User retrieveUser(final ManageTransactionPasswordRequestDTO form) {
		// final HttpServletRequest request = context.getRequest();
		if (form.getAttribute("element") != null) {
			// The element may be already retrieved on the manage passwords
			return ((Element) request.getAttribute("element")).getUser();
		}

		final ManageTransactionPasswordForm form = context.getForm();
		User user;
		final long userId = form.getUserId();
		try {
			user = elementService.loadUser(userId,
					RelationshipHelper.nested(User.Relationships.ELEMENT, Element.Relationships.GROUP));
			if (user instanceof OperatorUser) {
				Element element = user.getElement();
				element = elementService.load(element.getId(),
						RelationshipHelper.nested(Operator.Relationships.MEMBER, Element.Relationships.GROUP));
			}
		} catch (final Exception e) {
			throw new ValidationException();
		}
		return user;
	}

}
