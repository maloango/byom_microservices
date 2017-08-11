package nl.strohalm.cyclos.webservices.rest.accounts.external.filemappings;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import nl.strohalm.cyclos.annotations.Inject;
import nl.strohalm.cyclos.entities.accounts.external.filemapping.FileMapping;
import nl.strohalm.cyclos.services.accounts.external.filemapping.FieldMappingService;
import nl.strohalm.cyclos.services.accounts.external.filemapping.FileMappingService;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;

@Controller
public class SetFieldMappingsOrderController extends BaseRestController {
	private FieldMappingService fieldMappingService;
	private FileMappingService fileMappingService;

	public final FieldMappingService getFieldMappingService() {
		return fieldMappingService;
	}

	public final FileMappingService getFileMappingService() {
		return fileMappingService;
	}

	@Inject
	public void setFieldMappingService(
			final FieldMappingService fieldMappingService) {
		this.fieldMappingService = fieldMappingService;
	}

	@Inject
	public void setFileMappingService(
			final FileMappingService fileMappingService) {
		this.fileMappingService = fileMappingService;
	}

	public static class SetFieldMappingsOrderRequestDto {
		private long fileMappingId;
		private Long[] fieldsIds;

		public Long[] getFieldsIds() {
			return fieldsIds;
		}

		public long getFileMappingId() {
			return fileMappingId;
		}

		public void setFieldsIds(final Long[] fieldsIds) {
			this.fieldsIds = fieldsIds;
		}

		public void setFileMappingId(final long fileMappingId) {
			this.fileMappingId = fileMappingId;
		}
	}

	public static class SetFieldMappingsOrderResponseDto {
		public String message;

		public String getMessage() {
			return message;
		}

		public void setMessage(String message) {
			this.message = message;
		}

            /**
             *
             */
            public SetFieldMappingsOrderResponseDto(){
        }
	}

	@RequestMapping(value = "admin/SetFieldMappings", method = RequestMethod.POST)
	@ResponseBody
	protected SetFieldMappingsOrderResponseDto handleSubmit(
			@RequestBody SetFieldMappingsOrderRequestDto form) throws Exception {
		SetFieldMappingsOrderResponseDto response = null;
                try{
		final long fileMappingId = form.getFileMappingId();
		final FileMapping fileMapping = fileMappingService.load(fileMappingId,
				FileMapping.Relationships.EXTERNAL_ACCOUNT);
		final Long externalAccountId = fileMapping.getAccount().getId();
		fieldMappingService.setOrder(form.getFieldsIds());
		//SetFieldMappingsOrderResponseDto response = new SetFieldMappingsOrderResponseDto();
		response.setMessage("fieldMapping.orderModified");
                response = new SetFieldMappingsOrderResponseDto();}
                catch(Exception e){
                    e.printStackTrace();
                }
		return response;
	}

	/*protected void prepareForm(final ActionContext context) throws Exception {
		final HttpServletRequest request = context.getRequest();
		final SetFieldMappingsOrderForm form = context.getForm();
		final long fileMappingId = form.getFileMappingId();
		if (fileMappingId <= 0) {
			throw new ValidationException();
		}
		final FileMappingWithFields fileMapping = (FileMappingWithFields) fileMappingService
				.load(fileMappingId, FileMappingWithFields.Relationships.FIELDS);
		request.setAttribute("fieldMappings", fileMapping.getFields());
		request.setAttribute("fileMappingId", fileMappingId);
	}*/

}
