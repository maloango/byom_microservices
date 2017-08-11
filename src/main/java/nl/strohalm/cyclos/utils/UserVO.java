package nl.strohalm.cyclos.utils;

import java.util.Calendar;
import nl.strohalm.cyclos.entities.Indexable;

/**
 *
 * @author Lue Infoservices
 */
public class UserVO extends EntityVO {
    
    private Calendar                    lastLogin;
    private Calendar                    passwordDate;
    private Calendar                    passwordBlockedUntil;
    
    public UserVO(long id, String name,Calendar passwordDate,Calendar passwordBlockedUntil,Calendar lastLogin) {
        super(id, name);
        this.passwordDate=passwordDate;
        this.passwordBlockedUntil=passwordBlockedUntil;
        this.lastLogin=lastLogin;
    }

    public Calendar getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(Calendar lastLogin) {
        this.lastLogin = lastLogin;
    }

    public Calendar getPasswordDate() {
        return passwordDate;
    }

    public void setPasswordDate(Calendar passwordDate) {
        this.passwordDate = passwordDate;
    }

    public Calendar getPasswordBlockedUntil() {
        return passwordBlockedUntil;
    }

    public void setPasswordBlockedUntil(Calendar passwordBlockedUntil) {
        this.passwordBlockedUntil = passwordBlockedUntil;
    }

    
}
