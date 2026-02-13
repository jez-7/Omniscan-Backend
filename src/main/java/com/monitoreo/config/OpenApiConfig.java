package com.monitoreo.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Omniscan API - Sistema de Monitoreo de Precios")
                        .version("1.0")
                        .description("API diseñada para el escaneo automático de productos, análisis de volatilidad y detección de ofertas mediante arquitecturas orientadas a eventos.")
                        .contact(new Contact()
                                .name("Emanuel Zamora")
                                .url("https://github.com/jez-7")
                                .email("juanemanuelzamora@gmail.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("http://springdoc.org")));
    }
}
