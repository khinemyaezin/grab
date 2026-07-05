package com.identity.infrastructure.configuration;

import com.identity.domain.policy.AccessPlacementPolicy;
import com.identity.domain.policy.AccessPlacementPolicyResolver;
import com.identity.domain.policy.RegistrationAccessPolicyResolver;
import com.identity.domain.policy.impl.MerchantOwnerAccessPlacementPolicy;
import com.identity.domain.policy.impl.SellerPlatformUserRegistrationAccessPolicy;
import com.identity.domain.policy.RegistrationAccessPolicy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class IdentityDomainConfig {
    @Bean
    public SellerPlatformUserRegistrationAccessPolicy registrationAccessPolicy() {
        return new SellerPlatformUserRegistrationAccessPolicy();
    }

    @Bean
    public RegistrationAccessPolicyResolver registrationAccessPolicyResolver(List<RegistrationAccessPolicy> policies) {
        return new RegistrationAccessPolicyResolver(policies);
    }

    @Bean
    public MerchantOwnerAccessPlacementPolicy merchantOwnerAccessPlacementPolicy() {
        return new MerchantOwnerAccessPlacementPolicy();
    }

    @Bean
    public AccessPlacementPolicyResolver accessPlacementPolicyResolver(List<AccessPlacementPolicy> policies) {
        return new AccessPlacementPolicyResolver(policies);
    }
}
