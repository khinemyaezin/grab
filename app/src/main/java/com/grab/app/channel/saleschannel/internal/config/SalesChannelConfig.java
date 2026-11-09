package com.grab.app.channel.saleschannel.internal.config;

import com.saleschannel.adapter.persistence.config.SalesChannelPersistenceConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration(proxyBeanMethods = false)
@Import({
        SalesChannelUseCaseConfig.class,
        SalesChannelPersistenceConfig.class,
        SalesChannelModuleDataSourceConfig.class,
        SalesChannelModuleOutboxConfig.class,
        SalesChannelSecurityManifestPublicationConfig.class
})
public class SalesChannelConfig {
}

