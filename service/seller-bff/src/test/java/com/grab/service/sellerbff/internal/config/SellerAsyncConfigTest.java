package com.grab.service.sellerbff.internal.config;

import com.grab.service.sellerbff.internal.tracing.MdcCopyingTaskDecorator;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.beans.factory.support.StaticListableBeanFactory;
import org.springframework.boot.task.ThreadPoolTaskExecutorBuilder;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SellerAsyncConfigTest {

    @Test
    void sellerAsyncTaskExecutor_isConfiguredWithDecoratorAndMdcPropagation() throws InterruptedException {
        ThreadPoolTaskExecutorBuilder builder = new ThreadPoolTaskExecutorBuilder();
        StaticListableBeanFactory beanFactory = new StaticListableBeanFactory();
        beanFactory.addBean("mdcCopyingTaskDecorator", new MdcCopyingTaskDecorator());

        SellerAsyncConfig config = new SellerAsyncConfig(
                builder,
                beanFactory.getBeanProvider(org.springframework.core.task.TaskDecorator.class),
                2,
                4,
                50
        );

        ThreadPoolTaskExecutor executor = config.sellerAsyncTaskExecutor();
        assertNotNull(executor);
        assertEquals(2, executor.getCorePoolSize());
        assertEquals(4, executor.getMaxPoolSize());
        assertEquals(50, executor.getQueueCapacity());
        assertTrue(executor.getThreadNamePrefix().startsWith("seller-async-"));

        MDC.put("traceId", "async-trace-123");
        AtomicReference<String> observedTraceId = new AtomicReference<>();
        CountDownLatch latch = new CountDownLatch(1);

        executor.execute(() -> {
            observedTraceId.set(MDC.get("traceId"));
            latch.countDown();
        });

        assertTrue(latch.await(5, TimeUnit.SECONDS));
        assertEquals("async-trace-123", observedTraceId.get());
        MDC.clear();

        executor.shutdown();
    }
}
