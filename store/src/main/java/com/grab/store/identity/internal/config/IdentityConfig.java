package com.grab.store.identity.internal.config;

import com.identity.infrastructure.configuration.IdentityInfraConfig;
import com.identity.domain.repository.RoleDelegationRuleRepository;
import com.identity.domain.service.RoleDelegationPolicy;
import com.identity.domain.service.RuleBasedRoleDelegationPolicy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(IdentityInfraConfig.class)
public class IdentityConfig {
    @Bean
    RoleDelegationPolicy roleDelegationPolicy(RoleDelegationRuleRepository rules) {
        return new RuleBasedRoleDelegationPolicy(rules);
    }
}
