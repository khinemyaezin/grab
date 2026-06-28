package com.grab.store.identity.internal.config;

import com.identity.infrastructure.configuration.IdentityInfraConfig;
import com.identity.domain.service.AccessRoleDelegationPolicy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(IdentityInfraConfig.class)
public class IdentityConfig {
    @Bean
    AccessRoleDelegationPolicy accessRoleDelegationPolicy() {
        return new AccessRoleDelegationPolicy();
    }
}
