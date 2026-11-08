package com.grab.app.channel.region.internal.config;

import com.region.adapter.persistence.config.RegionPersistenceConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration(proxyBeanMethods = false)
@Import({
        RegionUseCaseConfig.class,
        RegionPersistenceConfig.class,
        RegionModuleDataSourceConfig.class,
        RegionModuleOutboxConfig.class
})
public class RegionConfig {
}

