package stripe.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.ArrayList;

@Configuration
@EnableSwagger2
public class SwaggerConfig {

    @Bean
    public Docket productApi() {
        return new Docket(DocumentationType.SWAGGER_2)
                .select()
                .apis(RequestHandlerSelectors.basePackage("stripe.backend.controller"))
                .paths(PathSelectors.any())
                .build()
                .apiInfo(metaData())
                .useDefaultResponseMessages(false);

    }


    private ApiInfo metaData() {
        ApiInfo apiInfo = new ApiInfo(
                "Networking Phoenix API",
                "Event Management",
                "1.0",
                "www.techavidus.com",
                new Contact("Rajesh Bhushan", "www.techavidus.com", "rajesh.bhushan@techavidus.com"),
                "Terms and Service",
                "Apache License Version 2.0",
                new ArrayList<>());
        return apiInfo;
    }

}
