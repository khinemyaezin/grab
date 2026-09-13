package com.grab.store.shared.sse;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.grab.framework.id.Id;
import com.grab.framework.id.IdGenerator;
import com.grab.framework.id.impl.CommonId;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class WorkflowTerminalUiEventListenerTest {

    @Test
    void onTerminal_shouldPublishNamedWorkflowEvent() {
        RecordingSseHub hub = new RecordingSseHub();
        WorkflowTerminalUiEventListener listener = new WorkflowTerminalUiEventListener(hub, new ObjectMapper());

        listener.onTerminal(new WorkflowTerminalUiEvent(
                "actor-1",
                "merchant-1",
                "wf-1",
                "create-sellable-product",
                "COMPLETED",
                "idem-1",
                null,
                false
        ));

        assertThat(hub.namedEvent).isEqualTo("workflow");
        assertThat(hub.key).isEqualTo("actor-1:merchant-1");
        assertThat(hub.json).contains("\"workflowId\":\"wf-1\"");
        assertThat(hub.json).contains("\"status\":\"COMPLETED\"");
        assertThat(hub.json).contains("\"producerId\":\"backend\"");
        assertThat(hub.json).contains("\"idempotencyKey\":\"idem-1\"");
        assertThat(hub.json).doesNotContain("productId");
    }

    private static final class RecordingSseHub extends SseHub {
        private String key;
        private String namedEvent;
        private String json;

        private RecordingSseHub() {
            super(new IdGenerator() {
                @Override
                public Id generateId() {
                    return new CommonId("evt-1");
                }

                @Override
                public Id convertIdFrom(String id) {
                    return new CommonId(id);
                }
            });
        }

        @Override
        public void publish(String key, String namedEvent, String json) {
            this.key = key;
            this.namedEvent = namedEvent;
            this.json = json;
        }
    }
}
