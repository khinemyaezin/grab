package com.grab.redpanda.adapter.dispatcher;

import com.grab.framework.domain.Event;
import com.grab.framework.logger.Logger;
import com.grab.framework.logger.Loggers;
import com.grab.framework.outbox.OutboxDeliveryContext;
import com.grab.framework.outbox.OutboxDispatchMetadata;
import com.grab.framework.outbox.OutboxEventDispatcher;
import com.grab.framework.outbox.OutboxEventHeaders;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.header.internals.RecordHeaders;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public final class RedpandaOutboxEventDispatcher implements OutboxEventDispatcher {

    private static final Logger log = Loggers.getLogger(RedpandaOutboxEventDispatcher.class);

    private final ApplicationEventPublisher eventPublisher;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final boolean enabled;
    private final String topic;
    private final long sendTimeoutMs;

    public RedpandaOutboxEventDispatcher(
            ApplicationEventPublisher eventPublisher,
            KafkaTemplate<String, String> kafkaTemplate,
            boolean enabled,
            String topic,
            Duration sendTimeout
    ) {
        this.eventPublisher = eventPublisher;
        this.kafkaTemplate = kafkaTemplate;
        this.enabled = enabled;
        this.topic = topic;
        this.sendTimeoutMs = sendTimeout.toMillis();
    }

    @Override
    public void dispatch(Event event) {
        eventPublisher.publishEvent(event);
        if (!enabled) {
            return;
        }

        Optional<OutboxDispatchMetadata> currentMetadata = OutboxDeliveryContext.currentMetadata();
        OutboxDispatchMetadata metadata = currentMetadata
                .orElseThrow(() -> new IllegalStateException("Outbox dispatch metadata is unavailable"));
        ProducerRecord<String, String> record = createRecord(metadata);
        CompletableFuture<SendResult<String, String>> send = kafkaTemplate.send(record);
        try {
            send.get(sendTimeoutMs, TimeUnit.MILLISECONDS);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while publishing outbox event to Redpanda", exception);
        } catch (ExecutionException | TimeoutException exception) {
            log.warn("Redpanda publication failed for eventId={}", metadata.eventId(), exception);
            throw new IllegalStateException("Failed to publish outbox event to Redpanda", exception);
        }
    }

    private ProducerRecord<String, String> createRecord(OutboxDispatchMetadata metadata) {
        Headers headers = new RecordHeaders();
        String eventId = metadata.eventId().toString();
        String schemaVersion = Integer.toString(metadata.eventVersion());
        String occurredAt = metadata.occurredAt().toString();
        String traceId = OutboxEventHeaders.extractTraceId(metadata.headers());
        addHeader(headers, "event-id", eventId);
        addHeader(headers, "event-type", metadata.eventType());
        addHeader(headers, "aggregate-type", metadata.aggregateType());
        addHeader(headers, "schema-version", schemaVersion);
        addHeader(headers, "occurred-at", occurredAt);
        addHeader(headers, "trace-id", traceId);
        addHeader(headers, "content-type", "application/json");
        addHeader(headers, "outbox-headers", metadata.headers());
        long timestamp = metadata.occurredAt().toEpochMilli();
        String aggregateId = metadata.aggregateId();
        String payload = metadata.payload();
        return new ProducerRecord<>(topic, null, timestamp, aggregateId, payload, headers);
    }

    private void addHeader(Headers headers, String name, String value) {
        if (value == null) {
            return;
        }
        byte[] encodedValue = value.getBytes(StandardCharsets.UTF_8);
        headers.add(name, encodedValue);
    }
}
