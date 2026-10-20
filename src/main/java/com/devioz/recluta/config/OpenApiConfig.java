package com.devioz.recluta.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI deviozReclutaOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("DeviozRecluta Express API")
                        .description("API REST para la gestión integral de reclutamiento y postulaciones de DeviozRecluta Express.")
                        .version("v1.0.0")
                        .contact(new Contact()
                                .name("Devioz Tech Team")
                                .email("contacto@devioz.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0")));
    }
}
