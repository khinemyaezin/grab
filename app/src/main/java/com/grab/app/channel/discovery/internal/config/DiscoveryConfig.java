package com.grab.app.channel.discovery.internal.config;

import com.grab.discovery.adapter.persistence.config.DiscoveryPersistenceConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration(proxyBeanMethods = false)
@Import({
        DiscoveryUseCaseConfig.class,
        DiscoveryPersistenceConfig.class,
        DiscoveryModuleDataSourceConfig.class
})
public class DiscoveryConfig {
}

