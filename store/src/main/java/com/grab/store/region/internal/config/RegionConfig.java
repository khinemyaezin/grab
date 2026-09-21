package com.grab.store.region.internal.config;

import com.region.adapter.persistence.config.RegionPersistenceConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({RegionUseCaseConfig.class, RegionPersistenceConfig.class})
public class RegionConfig {
}
