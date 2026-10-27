package com.grab.app.platform.seller.internal.adapter.inbound.rest.discovery.rest.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class StorefrontQueryWebConfig implements WebMvcConfigurer {
    private final StorefrontQueryRequestInterceptor requestInterceptor = new StorefrontQueryRequestInterceptor();

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(requestInterceptor)
                .addPathPatterns(
                        "/api/v1/storefront-query/products",
                        "/api/v1/storefront-query/products/**"
                );
    }
}
