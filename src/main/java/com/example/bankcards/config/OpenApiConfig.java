package com.example.bankcards.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI bankOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Bank REST API")
                        .description("Bank Management System REST API")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Bank API Support")
                                .email("support@bank.com")));
    }
}