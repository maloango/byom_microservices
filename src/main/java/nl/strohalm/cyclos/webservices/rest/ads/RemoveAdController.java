package nl.strohalm.cyclos.webservices.rest.ads;

import java.util.Map;

import org.apache.struts.upload.FormFile;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import nl.strohalm.cyclos.annotations.Inject;
import nl.strohalm.cyclos.services.ads.AdService;
import nl.strohalm.cyclos.utils.validation.ValidationException;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;
import org.springframework.web.bind.annotation.PathVariable;

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
			
			return false;
		}
	}

	public static class RemoveAdResponseDto {
		private String message;
		long memberId;
                private boolean isAdmin;
                private long Id;

        public boolean isAdmin() {
            return isAdmin;
        }

        public void setIsAdmin(boolean isAdmin) {
            this.isAdmin = isAdmin;
        }

        public long getId() {
            return Id;
        }

        public void setId(long Id) {
            this.Id = Id;
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
                public RemoveAdResponseDto(){
                }


	}

	@RequestMapping(value = "admin/removeAd/{memberId}", method = RequestMethod.GET)
	@ResponseBody
	protected RemoveAdResponseDto executeAction(@PathVariable ("memberId") long memberId) throws Exception {
			
		RemoveAdResponseDto response = new RemoveAdResponseDto();
                try{
		String message=null;
		
		//response = new RemoveAdResponseDto();
		if (response.getId() <= 0) {
			throw new ValidationException();
		}
		// Remove the advertisement
		adService.remove(response.getId());
		message ="ad.removed";
		response.setMessage(message);
		if (response.isAdmin()) {
			if (response.getMemberId() > 0) {
				memberId =response.getMemberId();
				response.setMemberId(memberId);
				return response;
			} else {
				
			}
		} else {
			memberId =response.getMemberId();
			response.setMemberId(memberId);}
                        response = new RemoveAdResponseDto();}
                        
                catch(Exception e){
                        e.printStackTrace();
                        }
			
		
		return response;
	}
}
