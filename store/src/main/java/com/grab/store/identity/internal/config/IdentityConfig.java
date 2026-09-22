package com.grab.store.identity.internal.config;

import com.grab.store.identity.internal.policy.CustomerRegistrationAccessPolicy;
import com.grab.store.identity.internal.policy.impl.DefaultCustomerRegistrationAccessPolicy;
import com.identity.adapter.persistence.configuration.IdentityPersistenceConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({IdentityUseCaseConfig.class, IdentityPersistenceConfig.class})
public class IdentityConfig {

    @Bean
    public CustomerRegistrationAccessPolicy customerRegistrationAccessPolicy() {
        return new DefaultCustomerRegistrationAccessPolicy();
    }
}
