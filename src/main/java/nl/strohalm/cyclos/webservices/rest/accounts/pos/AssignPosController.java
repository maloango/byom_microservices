package nl.strohalm.cyclos.webservices.rest.accounts.pos;

import java.util.HashMap;
import java.util.Map;

import nl.strohalm.cyclos.annotations.Inject;
import nl.strohalm.cyclos.controls.ActionContext;
import nl.strohalm.cyclos.controls.accounts.pos.EditPosForm;
import nl.strohalm.cyclos.entities.accounts.pos.Pos;
import nl.strohalm.cyclos.entities.exceptions.EntityNotFoundException;
import nl.strohalm.cyclos.entities.members.Member;
import nl.strohalm.cyclos.exceptions.PermissionDeniedException;
import nl.strohalm.cyclos.services.accounts.pos.PosService;
import nl.strohalm.cyclos.services.elements.ElementService;
import nl.strohalm.cyclos.utils.ActionHelper;
import nl.strohalm.cyclos.utils.binding.MapBean;
import nl.strohalm.cyclos.utils.transaction.CurrentTransactionData;
import nl.strohalm.cyclos.utils.validation.ValidationException;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;

import org.apache.struts.action.ActionForward;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class AssignPosController extends BaseRestController {
	private PosService posService;
	private ElementService elementService;

	public void setElementService(ElementService elementService) {
		this.elementService = elementService;
	}

	@Inject
	public void setPosService(final PosService posService) {
		this.posService = posService;
	}

	public static class AssignPosRequestDto {
		// POS Pk
		private long id;
		// POS identifier set by the admin/broker
		private String posId;
		private long memberId;
		private String operation;
		private String assignTo;
		private String pin;
		protected Map<String, Object> values;

		public Map<String, Object> getValues() {
			return values;
		}

		public void setValues(final Map<String, Object> values) {
			this.values = values;
		}

		public String getAssignTo() {
			return assignTo;
		}

		public long getId() {
			return id;
		}

		public long getMemberId() {
			return memberId;
		}

		public String getOperation() {
			return operation;
		}

		public String getPin() {
			return pin;
		}

		public Map<String, Object> getPos() {
			return values;
		}

		public Object getPos(final String key) {
			return values.get(key);
		}

		public String getPosId() {
			return posId;
		}

		public void setAssignTo(final String assignTo) {
			this.assignTo = assignTo;
		}

		public void setId(final long id) {
			this.id = id;
		}

		public void setMemberId(final long memberId) {
			this.memberId = memberId;
		}

		public void setOperation(final String operation) {
			this.operation = operation;
		}

		public void setPin(final String pin) {
			this.pin = pin;
		}

		public void setPos(final Map<String, Object> map) {
			values = map;
		}

		public void setPos(final String key, final Object value) {
			values.put(key, value);
		}

		public void setPosId(final String posId) {
			this.posId = posId;
		}
	}

	public static class AssignPosResponseDto {
		private String message;
		String userName;
		long memberId;

		public AssignPosResponseDto(String message, String userName,
				long memberId) {
			super();
			this.message = message;
			this.userName = userName;
			this.memberId = memberId;
		}

		public String getUserName() {
			return userName;
		}

		public void setUserName(String userName) {
			this.userName = userName;
		}

		public long getMemberId() {
			return memberId;
		}

		public void setMemberId(long memberId) {
			this.memberId = memberId;
		}

		public String getMessage() {
			return message;
		}

		public void setMessage(String message) {
			this.message = message;
		}
	}

	@RequestMapping(value = "/admin/assignPos", method = RequestMethod.POST)
	@ResponseBody
	protected AssignPosResponseDto handleSubmit(
			@RequestBody AssignPosRequestDto form) throws Exception {
		// final EditPosForm form = context.getForm();
		final String posId = form.getPosId();
		final long memberId = form.getMemberId();
		if (memberId <= 0) {
			throw new PermissionDeniedException();
		}
		final Member member = elementService.load(memberId);

		Pos pos;
		String message = null;
		String userName = null;
		AssignPosResponseDto response = new AssignPosResponseDto(message,
				userName, memberId);
		try {
			pos = posService.loadByPosId(posId);
			pos = posService.assignPos(member, pos.getId());
			message = "pos.assigned";
			member.getUsername();
		} catch (final EntityNotFoundException e) {
			// Clear the current transaction error, to avoid a rollback
			CurrentTransactionData.clearError();
			pos = new Pos();
			pos.setPosId(posId);
			pos = posService.save(pos);
			pos = posService.assignPos(member, pos.getId());
			message = "pos.createdAndAssigned";
			userName = member.getUsername();
		}

		return response;
	}

	protected void validateForm(final ActionContext context) {
		final EditPosForm form = context.getForm();
		if (form.getMemberId() <= 0) {
			throw new ValidationException();
		}
	}
}
