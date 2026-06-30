package com.grab.store.identity.internal.config;

import com.identity.domain.repository.PlatformRepository;
import com.identity.domain.service.GlobalUserRegistrationAccessPolicy;
import com.identity.domain.service.RegistrationAccessPolicy;
import com.identity.infrastructure.configuration.IdentityInfraConfig;
import com.identity.domain.repository.RoleDelegationRuleRepository;
import com.identity.domain.service.RoleDelegationPolicy;
import com.identity.domain.service.RuleBasedRoleDelegationPolicy;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(IdentityInfraConfig.class)
@EnableConfigurationProperties(IdentityRegistrationProperties.class)
public class IdentityConfig {
    @Bean
    public RoleDelegationPolicy roleDelegationPolicy(RoleDelegationRuleRepository rules) {
        return new RuleBasedRoleDelegationPolicy(rules);
    }

    @Bean
    public RegistrationAccessPolicy registrationAccessPolicy(IdentityRegistrationProperties properties) {
        return new GlobalUserRegistrationAccessPolicy(properties.platformCode(), properties.roleCode());
    }
}
