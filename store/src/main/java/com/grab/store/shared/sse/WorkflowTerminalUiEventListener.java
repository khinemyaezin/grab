package com.grab.store.shared.sse;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.grab.framework.logger.Logger;
import com.grab.framework.logger.Loggers;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class WorkflowTerminalUiEventListener {

    static final String SSE_EVENT_NAME = "workflow";

    private static final Logger log = Loggers.getLogger(WorkflowTerminalUiEventListener.class);

    private final SseHub sseHub;
    private final ObjectMapper objectMapper;

    public WorkflowTerminalUiEventListener(SseHub sseHub, ObjectMapper objectMapper) {
        this.sseHub = sseHub;
        this.objectMapper = objectMapper;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onTerminal(WorkflowTerminalUiEvent event) {
        if (event == null || event.platformUserId() == null || event.platformUserId().isBlank()) {
            return;
        }
        try {
            sseHub.publish(event.subscriberKey(), SSE_EVENT_NAME, objectMapper.writeValueAsString(event.toEnvelope()));
        } catch (JsonProcessingException exception) {
            log.warn("Failed to serialize workflow UI envelope for workflowId={}", event.workflowId());
        }
    }
}
