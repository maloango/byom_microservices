package nl.strohalm.cyclos;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ImportResource;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

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
@EnableSwagger2
@ComponentScan
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }


    @Bean
    public Docket newsApi() {
        return new Docket(DocumentationType.SWAGGER_2)
                .select()
                .apis(RequestHandlerSelectors.any())
                .paths(PathSelectors.any())
                .build();
    }
    @Bean
    public ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("Spring Boot Aaxon REST Sample with Swagger")
                .description("Spring Boot Aaxon REST Sample with Swagger")
                .contact("Sravan Kumar")
                .license("Apache License Version 2.0")
                .build();
    }

}


