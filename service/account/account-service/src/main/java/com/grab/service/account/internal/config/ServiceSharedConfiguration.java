package com.grab.service.account.internal.config;

import com.grab.framework.id.IdGenerator;
import com.grab.framework.id.impl.UuidGenerator;
import com.grab.framework.mapper.IdMapper;
import com.grab.framework.outbox.OutboxEventDispatcher;
import com.grab.outbox.infrastructure.adapter.out.spring.InProcessOutboxEventDispatcher;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class ServiceSharedConfiguration {

    @Bean
    public IdGenerator idGenerator() {
        return new UuidGenerator();
    }

    @Bean
    public IdMapper idMapper(IdGenerator idGenerator) {
        return new IdMapper(idGenerator);
    }

    @Bean
    public OutboxEventDispatcher outboxEventDispatcher(ApplicationEventPublisher publisher) {
        return new InProcessOutboxEventDispatcher(publisher);
    }
}
