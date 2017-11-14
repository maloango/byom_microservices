/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.strohalm.cyclos.webservices.rest.channels;

import nl.strohalm.cyclos.entities.access.Channel;
import nl.strohalm.cyclos.services.access.ChannelService;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;
import nl.strohalm.cyclos.webservices.rest.GenericResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 *
 * @author Lue Infoservices
 */
@Controller
public class GetChannelByIdController extends BaseRestController {
    
    public static class ChannelResponse extends GenericResponse {

        private ChannelEntity channel;
        
        public ChannelEntity getChannel() {
            return channel;
        }
        
        public void setChannel(ChannelEntity channel) {
            this.channel = channel;
        }
        
    }
    
    public static class ChannelEntity {

        private Long id;
        private String displayName;
        private String internalName;
        
        public Long getId() {
            return id;
        }
        
        public void setId(Long id) {
            this.id = id;
        }
        
        public String getDisplayName() {
            return displayName;
        }
        
        public void setDisplayName(String displayName) {
            this.displayName = displayName;
        }
        
        public String getInternalName() {
            return internalName;
        }
        
        public void setInternalName(String internalName) {
            this.internalName = internalName;
        }
        
    }
    
    @RequestMapping(value = "admin/getChannelById/{id}", method = RequestMethod.GET)
    @ResponseBody
    public ChannelResponse getChannel(@PathVariable("id") Long id) {
        ChannelResponse response = new ChannelResponse();
        Channel channel = channelService.load(id);
        ChannelEntity entity = new ChannelEntity();
        entity.setId(channel.getId());
        entity.setDisplayName(channel.getDisplayName());
        entity.setInternalName(channel.getInternalName());
        response.setChannel(entity);
        response.setStatus(0);
        return response;
    }
}
