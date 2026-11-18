package com.grab.service.sellerbff.internal.adapter.inbound.rest.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfiguration {

    @Bean
    public OpenAPI sellerOpenApi() {
        final String securitySchemeName = "sellerSession";
        return new OpenAPI()
                .info(new Info()
                        .title("Grab Seller API")
                        .description("Seller BFF APIs for catalog, inventory, merchants, pricing, sales channels, and auth")
                        .version("v1"))
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName,
                                new SecurityScheme()
                                        .name("GRAB_SELLER_SESSION")
                                        .type(SecurityScheme.Type.APIKEY)
                                        .in(SecurityScheme.In.COOKIE)));
    }
}
