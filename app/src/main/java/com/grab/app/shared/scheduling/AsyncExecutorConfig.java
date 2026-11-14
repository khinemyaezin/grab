package com.grab.app.shared.scheduling;

import com.grab.framework.logger.Logger;
import com.grab.framework.logger.Loggers;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Bounded executor for the remaining {@code @Async} methods. Boot's default queue is unbounded;
 * here a full queue falls back to {@link ThreadPoolExecutor.CallerRunsPolicy} (back-pressure, never drop).
 */
@Configuration
public class AsyncExecutorConfig implements AsyncConfigurer {

    private static final Logger log = Loggers.getLogger(AsyncExecutorConfig.class);

    private final int corePoolSize;
    private final int maxPoolSize;
    private final int queueCapacity;

    public AsyncExecutorConfig(
            @Value("${app.async.core-pool-size:4}") int corePoolSize,
            @Value("${app.async.max-pool-size:8}") int maxPoolSize,
            @Value("${app.async.queue-capacity:200}") int queueCapacity
    ) {
        this.corePoolSize = corePoolSize;
        this.maxPoolSize = maxPoolSize;
        this.queueCapacity = queueCapacity;
    }

    @Bean(name = "asyncTaskExecutor", destroyMethod = "shutdown")
    public ThreadPoolTaskExecutor asyncTaskExecutor() {
        var executor = new ThreadPoolTaskExecutor();
        executor.setThreadNamePrefix("app-async-");
        executor.setCorePoolSize(corePoolSize);
        executor.setMaxPoolSize(maxPoolSize);
        executor.setQueueCapacity(queueCapacity);
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);
        executor.initialize();
        return executor;
    }

    @Override
    public Executor getAsyncExecutor() {
        return asyncTaskExecutor();
    }

    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return (exception, method, params) ->
                log.error("Uncaught @Async failure in {}", method.toGenericString(), exception);
    }
}
