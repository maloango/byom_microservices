package nl.strohalm.cyclos.webservices.rest.customization.fields;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import nl.strohalm.cyclos.annotations.Inject;
import nl.strohalm.cyclos.entities.customization.fields.CustomField;
import nl.strohalm.cyclos.entities.members.Member;
import nl.strohalm.cyclos.services.customization.AdCustomFieldService;
import nl.strohalm.cyclos.services.customization.AdminCustomFieldService;
import nl.strohalm.cyclos.services.customization.BaseGlobalCustomFieldService;
import nl.strohalm.cyclos.services.customization.LoanGroupCustomFieldService;
import nl.strohalm.cyclos.services.customization.MemberCustomFieldService;
import nl.strohalm.cyclos.services.customization.OperatorCustomFieldService;
import nl.strohalm.cyclos.utils.validation.ValidationException;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class ListCustomFieldsController extends BaseRestController {
	private AdCustomFieldService adCustomFieldService;
	private AdminCustomFieldService adminCustomFieldService;
	public final AdCustomFieldService getAdCustomFieldService() {
		return adCustomFieldService;
	}

	public final AdminCustomFieldService getAdminCustomFieldService() {
		return adminCustomFieldService;
	}

	public final LoanGroupCustomFieldService getLoanGroupCustomFieldService() {
		return loanGroupCustomFieldService;
	}

	public final MemberCustomFieldService getMemberCustomFieldService() {
		return memberCustomFieldService;
	}

	public final OperatorCustomFieldService getOperatorCustomFieldService() {
		return operatorCustomFieldService;
	}

	private LoanGroupCustomFieldService loanGroupCustomFieldService;
	private MemberCustomFieldService memberCustomFieldService;
	private OperatorCustomFieldService operatorCustomFieldService;

	@Inject
	public void setAdCustomFieldService(
			final AdCustomFieldService adCustomFieldService) {
		this.adCustomFieldService = adCustomFieldService;
	}

	@Inject
	public void setAdminCustomFieldService(
			final AdminCustomFieldService adminCustomFieldService) {
		this.adminCustomFieldService = adminCustomFieldService;
	}

	@Inject
	public void setLoanGroupCustomFieldService(
			final LoanGroupCustomFieldService loanGroupCustomFieldService) {
		this.loanGroupCustomFieldService = loanGroupCustomFieldService;
	}

	@Inject
	public void setMemberCustomFieldService(
			final MemberCustomFieldService memberCustomFieldService) {
		this.memberCustomFieldService = memberCustomFieldService;
	}

	@Inject
	public void setOperatorCustomFieldService(
			final OperatorCustomFieldService operatorCustomFieldService) {
		this.operatorCustomFieldService = operatorCustomFieldService;
	}

	public static class ListCustomFieldsRequestDto {
		private String nature;
		private Member element;
                private long CustomField;
                
        public long getCustomField() {
            return CustomField;
        }

        public void setCustomField(long CustomField) {
            this.CustomField = CustomField;
        }
                
		

		public void setElement(Member element) {
			this.element = element;
		}
		

		public Member getElement() {
			return element;
		}


		public String getNature() {
			return nature;
		}

		public void setNature(final String nature) {
			this.nature = nature;
		}
                

	}

	public static class ListCustomFieldsResponseDto {
		//public String message;
		public Map<String,Object> map;
                private Member element;

        public Map<String, Object> getMap() {
            return map;
        }

        public void setMap(Map<String, Object> map) {
            this.map = map;
        }

        public Member getElement() {
            return element;
        }

        public void setElement(Member element) {
            this.element = element;
        }
                
		public ListCustomFieldsResponseDto(Map<String, Object> map) {
			super();
			this.map = map;
		}
                public ListCustomFieldsResponseDto(){
                }
		
	}

	@RequestMapping(value = "admin/listCustomFields/{CustomField}", method = RequestMethod.GET)
	@ResponseBody
	protected ListCustomFieldsResponseDto executeAction(@PathVariable ("CustomField") long CustomField) throws Exception {
			
		CustomField.Nature nature = null;
                ListCustomFieldsResponseDto response = new ListCustomFieldsResponseDto();
		try {
			//nature = CustomField.Nature.valueOf(CustomField.getNature());
		} catch (final Exception e) {
			throw new ValidationException();
		}
		List<? extends CustomField> fields = null;
		switch (nature) {
		case MEMBER_RECORD:
		case PAYMENT:
			// Member record and payment fields are listed in their owner entity
			// details, not here
			throw new ValidationException();
		case OPERATOR:
			final Member member = (Member) response.getElement();
			fields = operatorCustomFieldService.list(member);
			break;
		default:
			fields = resolveGlobalService(nature).list();
		}
		Map<String,Object> param = new HashMap<String, Object>();
		param.put("customFields", fields);
		param.put("nature", nature.name());
		response = new ListCustomFieldsResponseDto(param);
		return response;
	}

	@SuppressWarnings("unchecked")
	private <CF extends CustomField> BaseGlobalCustomFieldService<CF> resolveGlobalService(
			final CustomField.Nature nature) {
		switch (nature) {
		case AD:
			return (BaseGlobalCustomFieldService<CF>) adCustomFieldService;
		case ADMIN:
			return (BaseGlobalCustomFieldService<CF>) adminCustomFieldService;
		case LOAN_GROUP:
			return (BaseGlobalCustomFieldService<CF>) loanGroupCustomFieldService;
		case MEMBER:
			return (BaseGlobalCustomFieldService<CF>) memberCustomFieldService;
		}
		return null;
	}

}
