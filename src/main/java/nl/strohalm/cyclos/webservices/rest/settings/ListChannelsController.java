/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.strohalm.cyclos.webservices.rest.settings;

import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import nl.strohalm.cyclos.access.AdminSystemPermission;
import nl.strohalm.cyclos.entities.access.Channel;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;
import nl.strohalm.cyclos.webservices.rest.GenericResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 *
 * @author Lue Infoservices
 */
@Controller
public class ListChannelsController extends BaseRestController {

    public static class ListChannelsResponse extends GenericResponse {

        private List<ChannelVO> channels;
        // private List<Channel> builtin ;

//        public List<Channel> getBuiltin() {
//            return builtin;
//        }
//
//        public void setBuiltin(List<Channel> builtin) {
//            this.builtin = builtin;
//        }
        public List<ChannelVO> getChannels() {
            return channels;
        }

        public void setChannels(List<ChannelVO> channels) {
            this.channels = channels;
        }

    }

    @RequestMapping(value = "admin/listChannels", method = RequestMethod.GET)
    @ResponseBody
    public ListChannelsResponse listChannels() throws Exception {
        ListChannelsResponse response = new ListChannelsResponse();
        final List<Channel> channels = channelService.list();
        // final List<Channel> builtin = channelService.listBuiltin();
        List<ChannelVO> ch=new ArrayList();
        for(Channel channel:channels){
            ChannelVO vo=new ChannelVO();
            vo.setInternalName(channel.getInternalName());
            vo.setDisplayName(channel.getDisplayName());
            vo.setId(channel.getId());
            ch.add(vo);
        }
            
        response.setChannels(ch);
        // response.setBuiltin(builtin);
        response.setStatus(0);
        response.setMessage("List of channels");
        return response;
    }

    public static class ChannelVO {

        private String internalName;
        private String displayName;
         private Long              id;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }
         

        public String getInternalName() {
            return internalName;
        }

        public void setInternalName(String internalName) {
            this.internalName = internalName;
        }

        public String getDisplayName() {
            return displayName;
        }

        public void setDisplayName(String displayName) {
            this.displayName = displayName;
        }
        

    }

}
