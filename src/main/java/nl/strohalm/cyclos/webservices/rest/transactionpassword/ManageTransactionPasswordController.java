package nl.strohalm.cyclos.webservices.rest.transactionpassword;

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

	@RequestMapping(value = "/member/manageTransactionPassword", method = RequestMethod.GET)
	@ResponseBody
	protected ActionForward handleDisplay(final ActionContext context)
			throws Exception {
		final HttpServletRequest request = context.getRequest();
		final ManageTransactionPasswordForm form = context.getForm();
		final User user = retrieveUser(context);
		boolean canReset = false;
		boolean canBlock = false;
		switch (user.getTransactionPasswordStatus()) {
		case ACTIVE:
			canReset = true;
			canBlock = true;
			break;
		case BLOCKED:
			canReset = true;
			break;
		case PENDING:
			canBlock = true;
			break;
		case NEVER_CREATED:
			if (user.getElement().getGroup().getBasicSettings()
					.getTransactionPassword() == TransactionPassword.MANUAL) {
				canReset = true;
			}
			break;
		}
		request.setAttribute("groupStatus", user.getElement().getGroup()
				.getBasicSettings().getTransactionPassword());
		request.setAttribute("user", user);
		request.setAttribute("canReset", canReset);
		request.setAttribute("canBlock", canBlock);
		RequestHelper.storeEnum(request, TransactionPassword.class,
				"globalTransactionPasswordStatus");
		RequestHelper.storeEnum(request, User.TransactionPasswordStatus.class,
				"userTransactionPasswordStatus");

		if (form.isEmbed()) {
			return new ActionForward(
					"/pages/access/transactionPassword/manageTransactionPassword.jsp");
		} else {
			return context.getInputForward();
		}

	}

	@RequestMapping(value = "/member/manageTransactionPassword", method = RequestMethod.POST)
	@ResponseBody
	protected ActionForward handleSubmit(final ActionContext context)
			throws Exception {
		final ManageTransactionPasswordForm form = context.getForm();
		User user = retrieveUser(context);
		final boolean block = form.isBlock();
		final ResetTransactionPasswordDTO dto = new ResetTransactionPasswordDTO();
		dto.setUser(user);
		dto.setAllowGeneration(!block);
		user = accessService.resetTransactionPassword(dto);
		context.sendMessage(block ? "transactionPassword.blocked"
				: "transactionPassword.reset");
		return ActionHelper.redirectWithParam(context.getRequest(),
				context.getSuccessForward(), "userId", user.getId());
	}

	private User retrieveUser(final ActionContext context) {
		final HttpServletRequest request = context.getRequest();
		if (request.getAttribute("element") != null) {
			// The element may be already retrieved on the manage passwords
			return ((Element) request.getAttribute("element")).getUser();
		}

		final ManageTransactionPasswordForm form = context.getForm();
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
