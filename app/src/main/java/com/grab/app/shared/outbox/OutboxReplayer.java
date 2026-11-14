package com.grab.app.shared.outbox;

import com.grab.outbox.infrastructure.AbstractOutboxProcessor;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Operator replay of dead-lettered outbox rows ({@code DEAD -> NEW}) by module and id range. Every module's outbox
 * id is a {@code Long}. Not exposed over HTTP yet: the caller must enforce an ops-only permission.
 */
public class OutboxReplayer {

    public static final int MAX_LIMIT = 1000;

    private final Map<String, AbstractOutboxProcessor<?, ?>> processorsByModule;

    public OutboxReplayer(List<AbstractOutboxProcessor<?, ?>> processors) {
        this.processorsByModule = processors.stream()
                .collect(Collectors.toUnmodifiableMap(AbstractOutboxProcessor::module, Function.identity()));
    }

    public Set<String> modules() {
        return processorsByModule.keySet();
    }

    /**
     * @return the number of rows moved back to {@code NEW}
     */
    @SuppressWarnings("unchecked")
    public int replayDead(String module, long fromId, long toId, int limit) {
        AbstractOutboxProcessor<?, Long> processor = (AbstractOutboxProcessor<?, Long>) processorsByModule.get(module);
        if (processor == null) {
            throw new IllegalArgumentException("Unknown outbox module '" + module + "'; known: " + modules());
        }
        if (fromId > toId) {
            throw new IllegalArgumentException("fromId must be <= toId");
        }
        int boundedLimit = Math.max(1, Math.min(limit, MAX_LIMIT));
        return processor.replayDead(fromId, toId, boundedLimit);
    }
}
