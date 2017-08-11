package nl.strohalm.cyclos.webservices.rest.accounts.external.filemappings;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import nl.strohalm.cyclos.annotations.Inject;
import nl.strohalm.cyclos.entities.accounts.external.filemapping.FieldMapping;
import nl.strohalm.cyclos.entities.accounts.external.filemapping.FileMapping;
import nl.strohalm.cyclos.services.accounts.external.filemapping.FieldMappingService;
import nl.strohalm.cyclos.utils.RelationshipHelper;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;

@Controller
public class RemoveFieldMappingController extends BaseRestController {
	private FieldMappingService fieldMappingService;

	public final FieldMappingService getFieldMappingService() {
		return fieldMappingService;
	}

	@Inject
	public void setFieldMappingService(
			final FieldMappingService fieldMappingService) {
		this.fieldMappingService = fieldMappingService;
	}

	public static class RemoveFieldMappingRequestDto {
		private long fieldMappingId;

		public long getFieldMappingId() {
			return fieldMappingId;
		}

		public void setFieldMappingId(final long fieldMappingId) {
			this.fieldMappingId = fieldMappingId;
		}
	}

	public static class RemoveFieldMappingResponseDto {
		public String message;

		public String getMessage() {
			return message;
		}

		public void setMessage(String message) {
			this.message = message;
		}
                public RemoveFieldMappingResponseDto(){
        }
	}

	@RequestMapping(value = "admin/removeFieldMapping", method = RequestMethod.POST)
	@ResponseBody
	protected RemoveFieldMappingResponseDto executeAction(
			@RequestBody RemoveFieldMappingRequestDto form) throws Exception {
		RemoveFieldMappingResponseDto response = null;
                try{
		final long fieldMappingId = form.getFieldMappingId();
		final FieldMapping fieldMapping = fieldMappingService.load(
				fieldMappingId, RelationshipHelper.nested(
						FieldMapping.Relationships.FILE_MAPPING,
						FileMapping.Relationships.EXTERNAL_ACCOUNT));
		final Long externalAccountId = fieldMapping.getFileMapping()
				.getAccount().getId();
		fieldMappingService.remove(fieldMappingId);
		response.setMessage("fieldMapping.removed");
                response = new RemoveFieldMappingResponseDto();
                }
               catch(Exception exe){
                   exe.printStackTrace();
               
               }
		return response;
	}
}
