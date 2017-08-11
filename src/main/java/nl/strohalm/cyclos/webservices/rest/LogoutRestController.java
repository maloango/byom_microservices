/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.strohalm.cyclos.webservices.rest;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import nl.strohalm.cyclos.utils.RequestHelper;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 *
 * @author Lue Infoservices
 */


@Controller
public class LogoutRestController {
    private boolean isLogout;
    public void setIsLogout(boolean isLogout){
        this.isLogout=isLogout;
        
    }
    public boolean getIsLogout(){
        return isLogout;
    }
    
    

    @RequestMapping("/logout")
    @ResponseBody
    public LogoutRestController doLogout(HttpServletRequest request, HttpServletResponse response) {
        LogoutRestController logout=new LogoutRestController();
        final Cookie afterLogout = RequestHelper.getCookie(request, "afterLogout");
        if (afterLogout != null && StringUtils.isNotEmpty(afterLogout.getValue())) {
            String url = afterLogout.getValue();
            // Clear the cookie value
            afterLogout.setValue("");
            response.addCookie(afterLogout);
           logout.setIsLogout(true);
        }

        return logout;
    }

}
