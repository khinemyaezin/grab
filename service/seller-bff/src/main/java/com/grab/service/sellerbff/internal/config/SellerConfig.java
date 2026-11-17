package com.grab.service.sellerbff.internal.config;

import com.grab.framework.id.IdGenerator;
import com.grab.framework.id.impl.UuidGenerator;
import com.grab.framework.mapper.IdMapper;
import com.grab.service.sellerbff.internal.adapter.inbound.rest.security.SellerSecurityConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration(proxyBeanMethods = false)
@Import({
        SellerBusConfig.class,
        SellerSecurityConfig.class
})
public class SellerConfig {

    @Bean
    public IdGenerator idGenerator() {
        return new UuidGenerator();
    }

    @Bean
    public IdMapper idMapper(IdGenerator idGenerator) {
        return new IdMapper(idGenerator);
    }
}

