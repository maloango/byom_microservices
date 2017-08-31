package nl.strohalm.cyclos.webservices.rest;

import java.io.Serializable;

/**
 *
 * @author Lue Infoservices
 */
public class GenericResponse implements Serializable{
    
    private int status;
    private String message;

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
    
    
    
}
