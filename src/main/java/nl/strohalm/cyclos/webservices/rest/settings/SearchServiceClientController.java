/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.strohalm.cyclos.webservices.rest.settings;

import java.util.ArrayList;
import java.util.List;
import nl.strohalm.cyclos.entities.services.ServiceClient;
import nl.strohalm.cyclos.entities.services.ServiceClientQuery;
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
public class SearchServiceClientController extends BaseRestController{
    
    public static class ServiceClientResponse extends GenericResponse{
     private List<SeriviceClientEntity>serviceClients;

        public List<SeriviceClientEntity> getServiceClients() {
            return serviceClients;
        }

        public void setServiceClients(List<SeriviceClientEntity> serviceClients) {
            this.serviceClients = serviceClients;
        }
     
        
    }
    public static class SeriviceClientEntity{
        private Long id;
        private String name;
        private String hostName;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getHostName() {
            return hostName;
        }

        public void setHostName(String hostName) {
            this.hostName = hostName;
        }
        
    }
    @RequestMapping(value="admin/searchServiceClient",method=RequestMethod.GET)
    @ResponseBody
    public ServiceClientResponse search(){
        ServiceClientResponse response=new ServiceClientResponse();
        final ServiceClientQuery query = new ServiceClientQuery();
         final List<ServiceClient> clients = serviceClientService.search(query);
        List<SeriviceClientEntity> clientList=new ArrayList();
        for(ServiceClient client:clients){
            SeriviceClientEntity entity=new SeriviceClientEntity();
            entity.setId(client.getId());
            entity.setName(client.getName());
            entity.setHostName(client.getHostname());
            clientList.add(entity);
        }
        response.setServiceClients(clientList);
        response.setStatus(0);
        return response;
        
    }
}
