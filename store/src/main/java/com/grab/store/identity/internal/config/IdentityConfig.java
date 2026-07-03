package com.grab.store.identity.internal.config;

import com.grab.framework.id.IdGenerator;
import com.identity.domain.repository.AccessAssignmentRepository;
import com.identity.domain.repository.AuthorityRepository;
import com.identity.domain.repository.SessionStore;
import com.identity.domain.service.MerchantAccountAccessPolicy;
import com.identity.domain.service.RoleAdministrationPolicy;
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
    public RoleDelegationPolicy roleDelegationPolicy(RoleDelegationRuleRepository rules) {
        return new RuleBasedRoleDelegationPolicy(rules);
    }

    @Bean
    public MerchantAccountAccessPolicy merchantAccountAccessPolicy(
            AccessAssignmentRepository assignments,
            SessionStore sessions,
            IdGenerator ids
    ) {
        return new MerchantAccountAccessPolicy(assignments, sessions, ids);
    }

    @Bean
    public RoleAdministrationPolicy roleAdministrationPolicy(AuthorityRepository authorities) {
        return new RoleAdministrationPolicy(authorities);
    }
}
