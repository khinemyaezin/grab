package com.grab.app.platform.seller.internal.config;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.security.SellerSecurityConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration(proxyBeanMethods = false)
@Import({
        SellerBusConfig.class,
        SellerSecurityConfig.class
})
public class SellerConfig {
}

