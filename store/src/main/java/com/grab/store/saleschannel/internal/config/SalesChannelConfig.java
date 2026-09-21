package com.grab.store.saleschannel.internal.config;

import com.saleschannel.adapter.persistence.config.SalesChannelPersistenceConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({SalesChannelUseCaseConfig.class, SalesChannelPersistenceConfig.class})
public class SalesChannelConfig {
}
