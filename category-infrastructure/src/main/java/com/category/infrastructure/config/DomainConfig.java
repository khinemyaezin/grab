package com.category.infrastructure.config;

import com.grab.framework.mapper.CommonMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DomainConfig {
    @Bean
    public CommonMapper getCommonMapper() {
        return new CommonMapper();
    }
}
