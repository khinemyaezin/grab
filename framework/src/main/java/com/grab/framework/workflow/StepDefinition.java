package com.grab.framework.workflow;

import com.grab.framework.domain.Event;

import java.time.Duration;
import java.util.List;

public interface StepDefinition<C> {

    Duration DEFAULT_TIMEOUT = Duration.ofMinutes(10);

    String name();

    default List<Event> onEnter(String workflowId, C context) {
        return List.of();
    }

    default List<CorrelationKey> correlationsOnEnter(C context) {
        return List.of();
    }

    C onSignal(C context, InboundSignal signal);

    boolean isComplete(C context);

    default Object checkpointOutput(C context) {
        return name();
    }

    default List<Event> compensate(String workflowId, C context) {
        return List.of();
    }

    default C onCompensationAck(C context, InboundSignal signal) {
        return context;
    }

    default boolean isCompensated(C context) {
        return true;
    }

    default Duration timeout() {
        return DEFAULT_TIMEOUT;
    }
}
