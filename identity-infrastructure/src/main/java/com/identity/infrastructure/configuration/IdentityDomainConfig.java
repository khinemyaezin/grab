package com.identity.infrastructure.configuration;

import com.identity.domain.service.RegistrationAccessPolicyResolver;
import com.identity.domain.service.SellerPlatformUserRegistrationAccessPolicy;
import com.identity.domain.service.RegistrationAccessPolicy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class IdentityDomainConfig {
    @Bean
    public RegistrationAccessPolicy registrationAccessPolicy() {
        return new SellerPlatformUserRegistrationAccessPolicy();
    }

    @Bean
    public RegistrationAccessPolicyResolver registrationAccessPolicyResolver(List<RegistrationAccessPolicy> policies) {
        return new RegistrationAccessPolicyResolver(policies);
    }
}
