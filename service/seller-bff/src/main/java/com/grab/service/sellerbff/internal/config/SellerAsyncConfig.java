package com.grab.service.sellerbff.internal.config;

import com.grab.framework.logger.Logger;
import com.grab.framework.logger.Loggers;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.task.ThreadPoolTaskExecutorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskDecorator;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.time.Duration;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

@Configuration(proxyBeanMethods = false)
@EnableAsync
public class SellerAsyncConfig implements AsyncConfigurer {

    private static final Logger log = Loggers.getLogger(SellerAsyncConfig.class);

    private final int corePoolSize;
    private final int maxPoolSize;
    private final int queueCapacity;
    private final ThreadPoolTaskExecutor executor;

    public SellerAsyncConfig(
            ThreadPoolTaskExecutorBuilder builder,
            ObjectProvider<TaskDecorator> taskDecoratorProvider,
            @Value("${seller.async.core-pool-size:4}") int corePoolSize,
            @Value("${seller.async.max-pool-size:8}") int maxPoolSize,
            @Value("${seller.async.queue-capacity:200}") int queueCapacity
    ) {
        this.corePoolSize = corePoolSize;
        this.maxPoolSize = maxPoolSize;
        this.queueCapacity = queueCapacity;

        ThreadPoolTaskExecutorBuilder configuredBuilder = builder
                .threadNamePrefix("seller-async-")
                .corePoolSize(corePoolSize)
                .maxPoolSize(maxPoolSize)
                .queueCapacity(queueCapacity)
                .awaitTermination(true)
                .awaitTerminationPeriod(Duration.ofSeconds(30));

        TaskDecorator decorator = taskDecoratorProvider.getIfAvailable();
        if (decorator != null) {
            configuredBuilder = configuredBuilder.taskDecorator(decorator);
        }

        this.executor = configuredBuilder.build();
        this.executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        this.executor.initialize();
    }

    @Bean(name = "sellerAsyncTaskExecutor", destroyMethod = "shutdown")
    public ThreadPoolTaskExecutor sellerAsyncTaskExecutor() {
        return this.executor;
    }

    @Override
    public Executor getAsyncExecutor() {
        return this.executor;
    }

    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return (exception, method, params) ->
                log.error("Uncaught @Async failure in {}", method.toGenericString(), exception);
    }
}
