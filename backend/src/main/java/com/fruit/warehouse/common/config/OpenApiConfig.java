package com.fruit.warehouse.common.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
    info = @Info(
        title = "Fruit Warehouse API",
        version = "1.0.0",
        description = "Spring Boot 3 + MyBatis-Plus + JWT + AI enhanced APIs",
        contact = @Contact(name = "Fruit Warehouse Team", email = "team@example.com")
    ),
    servers = {
        @Server(url = "http://localhost:8080", description = "Local Dev")
    }
)
public class OpenApiConfig {
}