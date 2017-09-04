package nl.strohalm.cyclos.webservices.rest.members.agreements;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import nl.strohalm.cyclos.annotations.Inject;
import nl.strohalm.cyclos.controls.ActionContext;
//import nl.strohalm.cyclos.controls.members.agreements.EditRegistrationAgreementForm;
import nl.strohalm.cyclos.entities.members.RegistrationAgreement;
import nl.strohalm.cyclos.services.elements.RegistrationAgreementService;
import nl.strohalm.cyclos.utils.binding.BeanBinder;
import nl.strohalm.cyclos.utils.binding.DataBinder;
import nl.strohalm.cyclos.utils.binding.PropertyBinder;
import nl.strohalm.cyclos.utils.conversion.HtmlConverter;
import nl.strohalm.cyclos.utils.conversion.IdConverter;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;

@Controller
public class EditRegistrationAgreementController extends BaseRestController {
	private RegistrationAgreementService registrationAgreementService;
	private DataBinder<RegistrationAgreement> dataBinder;

	@Inject
	public void setRegistrationAgreementService(
			final RegistrationAgreementService registrationAgreementService) {
		this.registrationAgreementService = registrationAgreementService;
	}

	public static class EditRegistrationAgreementRequestDto {
		protected Map<String, Object> values;

		public Map<String, Object> getValues() {
			return values;
		}

		public void setValues(final Map<String, Object> values) {
			this.values = values;
		}

		public Map<String, Object> getRegistrationAgreement() {
			return values;
		}

		public Object getRegistrationAgreement(final String key) {
			return values.get(key);
		}

		public long getRegistrationAgreementId() {
			try {
				return (Long) getRegistrationAgreement("id");
			} catch (final Exception e) {
				return 0L;
			}
		}

		public void setRegistrationAgreement(final Map<String, Object> values) {
			this.values = values;
		}

		public void setRegistrationAgreement(final String key,
				final Object value) {
			values.put(key, value);
		}

		public void setRegistrationAgreementId(final long id) {
			setRegistrationAgreement("id", id);
		}

	}

	public static class EditRegistrationAgreementResponseDto {
		private String message;

		public String getMessage() {
			return message;
		}

		public void setMessage(String message) {
			this.message = message;
		}
	}
	@RequestMapping(value = "admin/editRegistrationAgreement", method = RequestMethod.POST)
	@ResponseBody
	protected EditRegistrationAgreementResponseDto formAction(@RequestBody EditRegistrationAgreementRequestDto form) throws Exception {
		EditRegistrationAgreementResponseDto response= null;
                try{
		final RegistrationAgreement registrationAgreement = getDataBinder()
				.readFromString(form);
		final boolean isInsert = registrationAgreement.isTransient();
		registrationAgreementService.save(registrationAgreement);
		
		if (isInsert) {
			response.setMessage("registrationAgreement.inserted");
		} else {
			response.setMessage("registrationAgreement.modified");
		}
                response = new EditRegistrationAgreementResponseDto(); }
                catch(Exception e){
                    e.printStackTrace();
                }
		return response;
	}

//	protected void prepareForm(final ActionContext context) throws Exception {
//		final HttpServletRequest request = context.getRequest();
//		final EditRegistrationAgreementForm form = context.getForm();
//		final long id = form.getRegistrationAgreementId();
//		RegistrationAgreement registrationAgreement;
//		final boolean isInsert = id <= 0L;
//		if (isInsert) {
//			registrationAgreement = new RegistrationAgreement();
//		} else {
//			registrationAgreement = registrationAgreementService.load(id);
//		}
//		getDataBinder().writeAsString(form, registrationAgreement);
//		request.setAttribute("registrationAgreement", registrationAgreement);
//		request.setAttribute("isInsert", isInsert);
//	}
//
//	protected void validateForm(final ActionContext context) {
//		final EditRegistrationAgreementForm form = context.getForm();
//		final RegistrationAgreement registrationAgreement = getDataBinder()
//				.readFromString(form);
//		registrationAgreementService.validate(registrationAgreement);
//	}

	private DataBinder<RegistrationAgreement> getDataBinder() {
		if (dataBinder == null) {
			final BeanBinder<RegistrationAgreement> binder = BeanBinder
					.instance(RegistrationAgreement.class,
							"registrationAgreement");
			binder.registerBinder(
					"id",
					PropertyBinder.instance(Long.class, "id",
							IdConverter.instance()));
			binder.registerBinder("name",
					PropertyBinder.instance(String.class, "name"));
			binder.registerBinder("contents", PropertyBinder.instance(
					String.class, "contents", HtmlConverter.instance()));
			dataBinder = binder;
		}
		return dataBinder;
	}
}
