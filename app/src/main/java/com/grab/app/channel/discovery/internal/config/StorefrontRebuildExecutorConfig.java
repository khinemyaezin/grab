package com.grab.app.channel.discovery.internal.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
public class StorefrontRebuildExecutorConfig {

    @Bean("storefrontRebuildExecutor")
    public ThreadPoolTaskExecutor storefrontRebuildExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(1);
        executor.setMaxPoolSize(1);
        executor.setQueueCapacity(1);
        executor.setThreadNamePrefix("storefront-rebuild-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        return executor;
    }
}
