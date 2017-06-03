package nl.strohalm.cyclos.webservices.rest.accounts.cards;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import nl.strohalm.cyclos.annotations.Inject;
import nl.strohalm.cyclos.controls.ActionContext;
import nl.strohalm.cyclos.controls.accounts.cards.CardForm;
import nl.strohalm.cyclos.controls.restapi.BaseRestController;
import nl.strohalm.cyclos.entities.access.Channel.Credentials;
import nl.strohalm.cyclos.entities.accounts.cards.Card;
import nl.strohalm.cyclos.entities.accounts.cards.Card.Relationships;
import nl.strohalm.cyclos.entities.accounts.cards.CardType.CardSecurityCode;
import nl.strohalm.cyclos.services.access.AccessService;
import nl.strohalm.cyclos.services.access.exceptions.BlockedCredentialsException;
import nl.strohalm.cyclos.services.access.exceptions.InvalidCredentialsException;
import nl.strohalm.cyclos.services.accounts.cards.CardService;
import nl.strohalm.cyclos.utils.validation.RequiredError;
import nl.strohalm.cyclos.utils.validation.ValidationError;
import nl.strohalm.cyclos.utils.validation.ValidationException;

@Controller
public class UpdateCardController extends BaseRestController {
	  private static CardService cardService;
	  

	    public static CardService getCardService() {
	        return cardService;
	    }

	    @Inject
	    public void setCardService(final CardService cardService) {
	        UpdateCardController.cardService = cardService;
	    }
	    public final AccessService getAccessService() {
			return accessService;
		}

		public final void setAccessService(AccessService accessService) {
			this.accessService = accessService;
		}
		private static AccessService accessService;
	    

	
	public static class UpdateCardRequestDTO {
		private long memberId;
		private long cardId;
		private String securityCode;
		private String securityCodeConfirmation;
		private String password;
		private String listOnly;
		private String operation;
		public final long getMemberId() {
			return memberId;
		}
		public final void setMemberId(long memberId) {
			this.memberId = memberId;
		}
		public final long getCardId() {
			return cardId;
		}
		public final void setCardId(long cardId) {
			this.cardId = cardId;
		}
		public final String getSecurityCode() {
			return securityCode;
		}
		public final void setSecurityCode(String securityCode) {
			this.securityCode = securityCode;
		}
		public final String getSecurityCodeConfirmation() {
			return securityCodeConfirmation;
		}
		public final void setSecurityCodeConfirmation(String securityCodeConfirmation) {
			this.securityCodeConfirmation = securityCodeConfirmation;
		}
		public final String getPassword() {
			return password;
		}
		public final void setPassword(String password) {
			this.password = password;
		}
		public final String getListOnly() {
			return listOnly;
		}
		public final void setListOnly(String listOnly) {
			this.listOnly = listOnly;
		}
		public final String getOperation() {
			return operation;
		}
		public final void setOperation(String operation) {
			this.operation = operation;
		}
		
	
	public static class UpdateCardResponseDTO{
		private String message;

		public final String getMessage() {
			return message;
		}

		public final UpdateCardResponseDTO setMessage(String message) {
			this.message = message;
			return null;
		}

		public UpdateCardResponseDTO sendError(String string) {
			// TODO Auto-generated method stub
			return null;
		}

		public Object getSession() {
			// TODO Auto-generated method stub
			return null;
		}

		public String getPathPrefix() {
			// TODO Auto-generated method stub
			return null;
		}

	
		
		
	}
	@RequestMapping(value = "/admin/managePasswords", method = RequestMethod.POST)
	@ResponseBody
	public UpdateCardResponseDTO handleSubmit(final @RequestBody UpdateCardRequestDTO form) throws Exception {
		
		//final CardForm form = form.getForm();
		final String code = form.getSecurityCode();
		final String password = form.getPassword();
		final long cardId = form.getCardId();
		final Card card = cardService.load(cardId);
		UpdateCardResponseDTO response = new UpdateCardResponseDTO();
		try {
			final boolean usesTransactionPassword = form.isTransactionPasswordEnabled();
			if (usesTransactionPassword) {
				accessService.checkTransactionPassword(password);
			}
			final String operation = form.getOperation();
			
			if (operation.equals("block")) {
				cardService.blockCard(card);
				response.setMessage("card.blocked");
			} else if (operation.equals("unblock")) {
				cardService.unblockCard(card);
				response.setMessage("card.unblocked");
			} else if (operation.equals("activate")) {
				cardService.activateCard(card, code);
				response.setMessage("card.activated");
			} else if (operation.equals("cancel")) {
				cardService.cancelCard(card);
				response.setMessage("card.canceled");
			} else if (operation.equals("changeCardCode")) {
				cardService.changeCardCode(card, code);
				response.setMessage("card.cardCodeChanged");
			} else if (operation.equals("unblockSecurityCode")) {
				cardService.unblockSecurityCode(card);
				response.setMessage("card.securityCodeUnblocked");
			} else {
				throw new ValidationException();
			}
			return response;
		} catch (final InvalidCredentialsException e) {
			return response.setMessage("card.updateCard.error.invalidTransactionPassword");
		} catch (final BlockedCredentialsException e) {
			if (e.getCredentialsType() == Credentials.TRANSACTION_PASSWORD) {
				response.getSession().setAttribute("returnTo", response.getPathPrefix() + "/manageExternalAcccess");
				return response.sendError("card.updateCard.error.blockedTransactionPassword");
			} else {
				response.getSession().invalidate();
				return response;
			}
		}

	}

	private boolean isTransactionPasswordEnabled() {
		// TODO Auto-generated method stub
		return false;
	}
	protected void validateForm(final ActionContext context) {
		final CardForm form = context.getForm();
		final boolean usesTransactionPassword = context.isTransactionPasswordEnabled();

		final ValidationException e = new ValidationException();
		e.setPropertyKey("securityCode", "card.changeCardCode.newCode1");
		e.setPropertyKey("securityCodeConfirmation", "card.changeCardCode.newCode2");
		e.setPropertyKey("login.transactionPassword", "login.transactionPassword");

		if (usesTransactionPassword) {
			context.validateTransactionPassword();
			if (StringUtils.isEmpty(form.getPassword())) {
				e.addPropertyError("login.transactionPassword", new RequiredError());
			}
		}

		final String operation = form.getOperation();
		final Card card = cardService.load(form.getCardId(), Relationships.CARD_TYPE);
		final boolean cardWithManualCodeActivation = card.getCardType().getCardSecurityCode() == CardSecurityCode.MANUAL
				&& operation.equals("activate");

		if (operation.equals("changeCardCode") || cardWithManualCodeActivation) {
			final String securityCode = form.getSecurityCode();
			if (StringUtils.isEmpty(securityCode)) {
				e.addPropertyError("securityCode", new RequiredError());
			}
			if (!card.getCardType().isShowCardSecurityCode()) {
				final String securityCodeConfirmation = form.getSecurityCodeConfirmation();
				if (StringUtils.isEmpty(securityCodeConfirmation)) {
					e.addPropertyError("securityCodeConfirmation", new RequiredError());
				}
				if (!ObjectUtils.equals(securityCode, securityCodeConfirmation)) {
					e.addGeneralError(new ValidationError("card.updateCard.cardCodesAreNotEqual"));
				}
			}
		}
		e.throwIfHasErrors();
	}
	}
}
