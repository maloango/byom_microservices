package nl.strohalm.cyclos.webservices.rest.members.records;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import nl.strohalm.cyclos.annotations.Inject;
import nl.strohalm.cyclos.entities.members.records.MemberRecord;
import nl.strohalm.cyclos.entities.members.records.MemberRecordType;
import nl.strohalm.cyclos.entities.members.records.MemberRecordType.Layout;
import nl.strohalm.cyclos.exceptions.PermissionDeniedException;
import nl.strohalm.cyclos.services.elements.MemberRecordService;
import nl.strohalm.cyclos.utils.validation.ValidationException;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;

@Controller
public class RemoveMemberRecordController extends BaseRestController {
	private MemberRecordService memberRecordService;

	@Inject
	public void setMemberRecordService(
			final MemberRecordService memberRecordService) {
		this.memberRecordService = memberRecordService;
	}

	public static class RemoveMemberRecordRequestDto {
		private long memberRecordId;

		public long getMemberRecordId() {
			return memberRecordId;
		}

		public void setMemberRecordId(final long memberRecordId) {
			this.memberRecordId = memberRecordId;
		}
	}

	public static class RemoveMemberRecordResponseDto {
		private String typeName;
		Map<String, Object> params;

		public RemoveMemberRecordResponseDto(String typeName,
				Map<String, Object> params) {
			super();
			this.typeName = typeName;
			this.params = params;
		}

		public String getMessage() {
			return typeName;
		}

		public void setMessage(String message) {
			this.typeName = message;
		}
	}

	@RequestMapping(value = "", method = RequestMethod.DELETE)
	@ResponseBody
	protected RemoveMemberRecordResponseDto executeAction(
			@RequestBody RemoveMemberRecordRequestDto form) throws Exception {
		// final RemoveMemberRecordForm form = context.getForm();
		final long id = form.getMemberRecordId();
		if (id <= 0) {
			throw new ValidationException();
		}
		final MemberRecord record = memberRecordService.load(id,
				MemberRecord.Relationships.ELEMENT,
				MemberRecord.Relationships.TYPE);
		final MemberRecordType type = record.getType();
		final String typeName = type.getName();
		String message = null;
		try {
			memberRecordService.remove(id);
			message = "memberRecord.removed" + typeName;
		} catch (final PermissionDeniedException e) {
			throw e;
		} catch (final Exception e) {
			message = "memberRecord.error.removing" + typeName;
			// return
		}

		/*boolean isGlobal = false;
		final SearchMemberRecordsForm searchForm = (SearchMemberRecordsForm) context
				.getSession().getAttribute("searchMemberRecordsForm");
		if (searchForm != null && searchForm.isGlobal()) {
			isGlobal = true;
		}*/

		final Map<String, Object> params = new HashMap<String, Object>();
		final boolean isFlat = record.getType().getLayout() == Layout.FLAT;

		params.put("elementId", record.getElement().getId());
		params.put("typeId", type.getId());
		RemoveMemberRecordResponseDto response = new RemoveMemberRecordResponseDto(
				typeName, params);

		return response;
	}
}
