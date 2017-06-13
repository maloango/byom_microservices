package nl.strohalm.cyclos.webservices.rest.ads;

import java.util.HashMap;
import java.util.Map;

import org.apache.struts.action.ActionForward;
import org.apache.struts.upload.FormFile;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import nl.strohalm.cyclos.annotations.Inject;
import nl.strohalm.cyclos.controls.ActionContext;
import nl.strohalm.cyclos.controls.ads.AdForm;
import nl.strohalm.cyclos.services.ads.AdService;
import nl.strohalm.cyclos.utils.ActionHelper;
import nl.strohalm.cyclos.utils.binding.MapBean;
import nl.strohalm.cyclos.utils.validation.ValidationException;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;

@Controller
public class RemoveAdController extends BaseRestController {
	AdService adService;

	public AdService getAdService() {
		return adService;
	}

	@Inject
	public void setAdService(final AdService adService) {
		this.adService = adService;
	}

	public static class RemoveAdRequestDto {
		private long id;
		private long memberId;
		private FormFile picture;
		private String pictureCaption;

		public Map<String, Object> getAd() {
			return values;
		}

		public Object getAd(final String key) {
			return values.get(key);
		}

		public long getId() {
			return id;
		}

		public long getMemberId() {
			return memberId;
		}

		public FormFile getPicture() {
			return picture;
		}

		public String getPictureCaption() {
			return pictureCaption;
		}

		public void setAd(final Map<String, Object> map) {
			values = map;
		}

		public void setAd(final String key, final Object value) {
			values.put(key, value);
		}

		public void setId(final long memberId) {
			id = memberId;
		}

		public void setMemberId(final long memberId) {
			this.memberId = memberId;
		}

		public void setPicture(final FormFile picture) {
			this.picture = picture;
		}

		public void setPictureCaption(final String pictureCaption) {
			this.pictureCaption = pictureCaption;
		}

		protected Map<String, Object> values;

		public Map<String, Object> getValues() {
			return values;
		}

		public void setValues(final Map<String, Object> values) {
			this.values = values;
		}

		public boolean isAdmin() {
			// TODO Auto-generated method stub
			return false;
		}
	}

	public static class RemoveAdResponseDto {
		private String message;
		long memberId;

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

	@RequestMapping(value = "", method = RequestMethod.DELETE)
	@ResponseBody
	protected RemoveAdResponseDto executeAction(
			@RequestBody RemoveAdRequestDto form) throws Exception {
		// final AdForm form = context.getForm();
		String message=null;
		long memberId;
		RemoveAdResponseDto response = new RemoveAdResponseDto();
		if (form.getId() <= 0) {
			throw new ValidationException();
		}
		// Remove the advertisement
		adService.remove(form.getId());
		message ="ad.removed";
		response.setMessage(message);
		if (form.isAdmin()) {
			if (form.getMemberId() > 0) {
				memberId =form.getMemberId();
				response.setMemberId(memberId);
				return response;
			} else {
				//return 
			}
		} else {
			memberId =form.getMemberId();
			response.setMemberId(memberId);
			return response;
		}
		return response;
	}
}
