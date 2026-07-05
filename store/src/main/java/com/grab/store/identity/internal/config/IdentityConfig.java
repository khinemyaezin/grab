package com.grab.store.identity.internal.config;

import com.grab.store.identity.internal.policy.MerchantApprovalAccessPolicy;
import com.grab.store.identity.internal.policy.impl.DefaultMerchantApprovalAccessPolicy;
import com.identity.domain.repository.AuthorityRepository;
import com.identity.domain.repository.RoleDelegationRuleRepository;
import com.identity.domain.policy.impl.RoleAdministrationPolicy;
import com.identity.domain.policy.RoleDelegationPolicy;
import com.identity.domain.policy.impl.RuleBasedRoleDelegationPolicy;
import com.identity.infrastructure.configuration.IdentityInfraConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(IdentityInfraConfig.class)
public class IdentityConfig {
    @Bean
    public MerchantApprovalAccessPolicy merchantApprovalAccessPolicy() {
        return new DefaultMerchantApprovalAccessPolicy();
    }

    @Bean
    public RoleDelegationPolicy roleDelegationPolicy(RoleDelegationRuleRepository rules) {
        return new RuleBasedRoleDelegationPolicy(rules);
    }

    @Bean
    public RoleAdministrationPolicy roleAdministrationPolicy(AuthorityRepository authorities) {
        return new RoleAdministrationPolicy(authorities);
    }
}
