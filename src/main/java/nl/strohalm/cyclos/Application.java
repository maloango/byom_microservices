package nl.strohalm.cyclos;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ImportResource;

@ImportResource(value = {
        "classpath:nl/strohalm/cyclos/spring/dao.xml",
        "classpath:nl/strohalm/cyclos/spring/misc.xml",
        "classpath:nl/strohalm/cyclos/spring/persistence.xml",
        "classpath:nl/strohalm/cyclos/spring/rest_security.xml",

        "classpath:nl/strohalm/cyclos/spring/scheduling.xml",
        "classpath:nl/strohalm/cyclos/spring/security.xml",
        "classpath:nl/strohalm/cyclos/spring/service.xml",
        "classpath:nl/strohalm/cyclos/spring/web_beans.xml",
        "classpath:nl/strohalm/cyclos/spring/web_services.xml"
})
@SpringBootApplication(exclude = { DataSourceAutoConfiguration.class, HibernateJpaAutoConfiguration.class })
@EnableDiscoveryClient
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}