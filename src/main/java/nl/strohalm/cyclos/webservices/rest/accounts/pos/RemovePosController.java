package nl.strohalm.cyclos.webservices.rest.accounts.pos;

import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import nl.strohalm.cyclos.annotations.Inject;
import nl.strohalm.cyclos.exceptions.PermissionDeniedException;
import nl.strohalm.cyclos.services.accounts.pos.PosService;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;

@Controller
public class RemovePosController extends BaseRestController {

	private PosService posService;

	@Inject
	public void setPosService(final PosService posService) {
		this.posService = posService;
	}

	public static class RemovePosRequestDto {
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

	public static class RemovePosResponseDto {
		private String message;

		public String getMessage() {
			return message;
		}

		public void setMessage(String message) {
			this.message = message;
		}
	}

	@RequestMapping(value = "admin/removePos", method = RequestMethod.DELETE)
	@ResponseBody
	protected RemovePosResponseDto executeAction(
			@RequestBody RemovePosRequestDto form) throws Exception {

		// final EditPosForm form = context.getForm();
		final long id = form.getId();
		String message;
		RemovePosResponseDto response = new RemovePosResponseDto();
		try {
			posService.deletePos(id);
			message = "pos.removed";
			response.setMessage(message);
		} catch (final PermissionDeniedException e) {
			throw e;
		} catch (final Exception e) {
			message = "pos.error.removing";
			response.setMessage(message);
		}
		return response;
	}
}
