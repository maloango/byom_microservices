/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.strohalm.cyclos.utils;

import nl.strohalm.cyclos.services.access.ChangeLoginPasswordDTO;
import org.codehaus.jackson.annotate.JsonIgnore;

/**
 *
 * @author Lue Infoservices
 */

public class ChangePasswordVO extends ChangeLoginPasswordDTO{
    private long userId;
    private boolean embed;
    private String oldPassword;
    private String newPassword;
    private String newPasswordConfirmation;

    public ChangePasswordVO() {
    }

    public ChangePasswordVO(long userId, boolean embed, String oldPassword, String newPassword, String newPasswordConfirmation) {
        this.userId = userId;
        this.embed = embed;
        this.oldPassword = oldPassword;
        this.newPassword = newPassword;
        this.newPasswordConfirmation = newPasswordConfirmation;
    }
    
    

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public boolean isEmbed() {
        return embed;
    }

    public void setEmbed(boolean embed) {
        this.embed = embed;
    }

    public String getOldPassword() {
        return oldPassword;
    }

    public void setOldPassword(String oldPassword) {
        this.oldPassword = oldPassword;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }

    public String getNewPasswordConfirmation() {
        return newPasswordConfirmation;
    }

    public void setNewPasswordConfirmation(String newPasswordConfirmation) {
        this.newPasswordConfirmation = newPasswordConfirmation;
    }
    
    
    
    
    
}
