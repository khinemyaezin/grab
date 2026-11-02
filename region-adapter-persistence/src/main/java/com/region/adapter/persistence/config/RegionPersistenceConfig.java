package com.region.adapter.persistence.config;

import com.grab.framework.support.PersistenceExecutor;
import com.region.adapter.persistence.adapter.RegionPersistenceExecutor;
import com.region.adapter.persistence.adapter.RegionQueryAdapter;
import com.region.adapter.persistence.repository.RegionJpaRepository;
import com.region.application.port.outbound.RegionQueryPort;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RegionPersistenceConfig {

    @Bean("regionPersistenceExecutor")
    PersistenceExecutor regionPersistenceExecutor() {
        return new RegionPersistenceExecutor();
    }

    @Bean
    RegionQueryPort regionQueryPort(
            RegionJpaRepository regions,
            @Qualifier("regionPersistenceExecutor") PersistenceExecutor executor
    ) {
        return new RegionQueryAdapter(regions, executor);
    }
}
