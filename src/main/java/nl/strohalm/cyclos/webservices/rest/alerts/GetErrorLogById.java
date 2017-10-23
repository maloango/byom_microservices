/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.strohalm.cyclos.webservices.rest.alerts;

/**
 *
 * @author Lue Infoservices
 */
import java.util.Calendar;
import nl.strohalm.cyclos.entities.alerts.ErrorLogEntry;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;
import nl.strohalm.cyclos.webservices.rest.GenericResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class GetErrorLogById extends BaseRestController {

    public static class ErrorLogResponse extends GenericResponse {

        private Calendar date;
        private String path;
        private String stackTrace;
        private String loggedUserName;
        private boolean removed;
        private Long id;

        public Calendar getDate() {
            return date;
        }

        public void setDate(Calendar date) {
            this.date = date;
        }

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }

        public String getStackTrace() {
            return stackTrace;
        }

        public void setStackTrace(String stackTrace) {
            this.stackTrace = stackTrace;
        }

        public String getLoggedUserName() {
            return loggedUserName;
        }

        public void setLoggedUserName(String loggedUserName) {
            this.loggedUserName = loggedUserName;
        }

        public boolean isRemoved() {
            return removed;
        }

        public void setRemoved(boolean removed) {
            this.removed = removed;
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

    }

    @RequestMapping(value = "admin/searchErrorHistoryById/{id}", method = RequestMethod.GET)
    @ResponseBody
    public ErrorLogResponse searchErrorLog(@PathVariable("id") Long id) {
        ErrorLogResponse response = new ErrorLogResponse();
        ErrorLogEntry log = errorLogService.load(id, ErrorLogEntry.Relationships.LOGGED_USER);
        response.setDate(log.getDate());
        response.setId(log.getId());
        response.setLoggedUserName(log.getLoggedUser().getUsername().toString());
        response.setPath(log.getPath());
        response.setStackTrace(log.getStackTrace());
        response.setStatus(0);
        response.setMessage("");
        return response;
    }

}
