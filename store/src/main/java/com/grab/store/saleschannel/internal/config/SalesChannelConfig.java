package com.grab.store.saleschannel.internal.config;

import com.saleschannel.adapter.persistence.config.SalesChannelPersistenceConfig;
import com.saleschannel.domain.policy.PublishableKeyManagementPolicy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({SalesChannelUseCaseConfig.class, SalesChannelPersistenceConfig.class})
public class SalesChannelConfig {

    @Bean
    public PublishableKeyManagementPolicy publishableKeyManagementPolicy(){
        return new PublishableKeyManagementPolicy();
    }
}
