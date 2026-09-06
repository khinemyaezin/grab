package com.grab.store.shared.sse;

import com.grab.framework.id.Id;
import com.grab.framework.id.IdGenerator;
import com.grab.framework.id.impl.CommonId;
import com.grab.framework.security.AccessContext;
import com.grab.framework.security.AuthenticatedActor;
import com.grab.store.shared.security.SecurityPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EventStreamControllerTest {

    private RecordingSseHub sseHub;
    private EventStreamController controller;

    @BeforeEach
    void setUp() {
        sseHub = new RecordingSseHub();
        controller = new EventStreamController(sseHub, new FixedIdGenerator());
    }

    @Test
    void stream_withoutPrincipal_shouldUnauthorized() {
        assertThatThrownBy(() -> controller.stream(null))
                .isInstanceOfSatisfying(ResponseStatusException.class, exception ->
                        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED));
    }

    @Test
    void stream_withPrincipal_shouldRegisterAndWriteReady() {
        SseEmitter emitter = controller.stream(samplePrincipal());

        assertThat(emitter).isSameAs(sseHub.emitter);
        assertThat(sseHub.platformUserId).isEqualTo("user-1");
        assertThat(sseHub.scopeId).isEqualTo("merchant-1");
        String payload = String.join("", sseHub.sent);
        assertThat(payload).contains("event:ready");
        assertThat(payload).contains("\"producerId\":\"backend\"");
    }

    @Test
    void stream_withResponse_shouldSetProxyHeaders() {
        org.springframework.mock.web.MockHttpServletResponse response = new org.springframework.mock.web.MockHttpServletResponse();
        controller.stream(samplePrincipal(), "evt-123", response);

        assertThat(response.getHeader("Cache-Control")).isEqualTo("no-cache, no-transform");
        assertThat(response.getHeader("X-Accel-Buffering")).isEqualTo("no");
    }

    private static SecurityPrincipal samplePrincipal() {
        return new SecurityPrincipal(new AuthenticatedActor(
                "user-1",
                "local-issuer",
                "user-1",
                "seller@example.com",
                Set.of(),
                Set.of(),
                new AccessContext("SELLER_PORTAL", "assign-1", "MERCHANT_ACCOUNT", "merchant-1")
        ));
    }

    private static final class RecordingSseHub extends SseHub {
        private String platformUserId;
        private String scopeId;
        private RecordingEmitter emitter;
        private List<String> sent = List.of();

        private RecordingSseHub() {
            super(new FixedIdGenerator());
        }

        @Override
        public SseEmitter register(String platformUserId, String scopeId) {
            this.platformUserId = platformUserId;
            this.scopeId = scopeId;
            this.emitter = new RecordingEmitter();
            this.sent = emitter.sent;
            return emitter;
        }
    }

    private static final class RecordingEmitter extends SseEmitter {
        private final List<String> sent = new ArrayList<>();

        private RecordingEmitter() {
            super(SseHub.EMITTER_TIMEOUT_MS);
        }

        @Override
        public void send(SseEventBuilder builder) throws IOException {
            builder.build().forEach(item -> sent.add(String.valueOf(item.getData())));
        }
    }

    private static final class FixedIdGenerator implements IdGenerator {
        @Override
        public Id generateId() {
            return new CommonId("evt-1");
        }

        @Override
        public Id convertIdFrom(String id) {
            return new CommonId(id);
        }
    }
}
