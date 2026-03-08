package com.grab.store.shared.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfiguration {

    @Bean
    public OpenAPI storeOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Grab Store API")
                        .description("Store APIs for catalog and inventory modules")
                        .version("v1"));
    }
}
