package com.grab.redpanda.adapter.config;

import com.grab.framework.outbox.OutboxEventDispatcher;
import com.grab.redpanda.adapter.dispatcher.RedpandaOutboxEventDispatcher;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;

import java.time.Duration;

@Configuration(proxyBeanMethods = false)
public class RedpandaAdapterConfiguration {

    @Bean
    public OutboxEventDispatcher outboxEventDispatcher(
            ApplicationEventPublisher eventPublisher,
            KafkaTemplate<String, String> kafkaTemplate,
            @Value("${redpanda.enabled:false}") boolean enabled,
            @Value("${redpanda.topic:grab.events.v1}") String topic,
            @Value("${redpanda.send-timeout:10s}") Duration sendTimeout
    ) {
        return new RedpandaOutboxEventDispatcher(eventPublisher, kafkaTemplate, enabled, topic, sendTimeout);
    }
}
