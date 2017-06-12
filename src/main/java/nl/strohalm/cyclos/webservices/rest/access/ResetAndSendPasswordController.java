/*
    This file is part of Cyclos (www.cyclos.org).
    A project of the Social Trade Organisation (www.socialtrade.org).

    Cyclos is free software; you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation; either version 2 of the License, or
    (at your option) any later version.

    Cyclos is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with Cyclos; if not, write to the Free Software
    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA

 */
package nl.strohalm.cyclos.webservices.rest.access;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import nl.strohalm.cyclos.entities.access.MemberUser;
import nl.strohalm.cyclos.services.access.AccessService;
import nl.strohalm.cyclos.utils.EntityHelper;
import nl.strohalm.cyclos.utils.transaction.CurrentTransactionData;
import nl.strohalm.cyclos.utils.validation.ValidationException;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;

/**
 * Action used to reset a member's password and send it by mail
 * @author luis
 */

@Controller
public class ResetAndSendPasswordController extends BaseRestController {
	
	private AccessService accessService;
	
	public static class ResetAndSendPasswordRequestDto{
		 private long              userId;

		    public long getUserId() {
		        return userId;
		    }

		    public void setUserId(final long userId) {
		        this.userId = userId;
		    }
	}
	
	public static class ResetAndSendPasswordResponseDto{
		
		private long userId;
		private String message;
		public ResetAndSendPasswordResponseDto(long userId, String message) {
			super();
			this.userId = userId;
			this.message = message;
		}
		public final long getUserId() {
			return userId;
		}
		public final void setUserId(long userId) {
			this.userId = userId;
		}
		public final String getMessage() {
			return message;
		}
		public final void setMessage(String message) {
			this.message = message;
		}
		
		
	}

	@RequestMapping(value = "/member/resetAndSendPassword", method = RequestMethod.POST)
	@ResponseBody
    protected ResetAndSendPasswordResponseDto resetAndSendPassword(@RequestBody ResetAndSendPasswordRequestDto form) throws Exception {
       
        final long userId = form.getUserId();
        if (userId <= 0L) {
            throw new ValidationException();
        }
        accessService.resetPassword(EntityHelper.reference(MemberUser.class, userId));
        String key;
        if (CurrentTransactionData.hasMailError()) {
            key = "changePassword.resetAndErrorSending";
        } else {
            key = "changePassword.resetAndSent";
        }
       // context.sendMessage(key);
        
        ResetAndSendPasswordResponseDto response =new  ResetAndSendPasswordResponseDto(userId, key);
        
        return response;
    }

}
