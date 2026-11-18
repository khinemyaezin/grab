package com.grab.service.sellerbff.internal.adapter.outbound.message;

import com.grab.service.sellerbff.internal.adapter.inbound.rest.security.oidc.SellerOidcProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
class SellerAuthTransportConfig {
    @Bean
    SellerAccountTransport sellerAccountTransport(SellerOidcProperties properties) {
        return new SellerAccountTransport(properties);
    }
}
