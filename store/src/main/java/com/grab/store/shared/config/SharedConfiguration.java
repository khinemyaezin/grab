package com.grab.store.shared.config;

import com.grab.framework.id.IdGenerator;
import com.grab.framework.id.impl.UuidGenerator;
import com.grab.framework.mapper.IdMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SharedConfiguration {
    @Bean
    public IdMapper idMapper() {
        return new IdMapper();
    }

    @Bean
    public IdGenerator idGenerator() {
        return new UuidGenerator();
    }
}
