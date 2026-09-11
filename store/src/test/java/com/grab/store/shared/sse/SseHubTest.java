package com.grab.store.shared.sse;

import com.grab.framework.id.Id;
import com.grab.framework.id.IdGenerator;
import com.grab.framework.id.impl.CommonId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

class SseHubTest {

    private SseHub sseHub;

    @BeforeEach
    void setUp() {
        sseHub = new SseHub(new SequentialIdGenerator());
    }

    @Test
    void register_shouldTrackSubscriberUntilComplete() {
        String key = SseSubscriberKey.of("user-1", "merchant-1");
        SseEmitter emitter = sseHub.register("user-1", "merchant-1");

        assertThat(sseHub.subscriberCount(key)).isEqualTo(1);

        emitter.complete();

        assertThat(sseHub.subscriberCount(key)).isEqualTo(0);
    }

    @Test
    void sendHeartbeats_afterComplete_shouldNotThrow() {
        SseEmitter emitter = sseHub.register("user-1", "merchant-1");
        emitter.complete();

        assertThatCode(() -> sseHub.sendHeartbeats()).doesNotThrowAnyException();
    }

    @Test
    void sendHeartbeats_whenSendFails_shouldDropSubscriberWithoutCompleting() {
        String key = SseSubscriberKey.of("user-1", "merchant-1");
        FailingEmitter emitter = new FailingEmitter();
        sseHub.addSubscriber(key, emitter);

        assertThatCode(() -> sseHub.sendHeartbeats()).doesNotThrowAnyException();
        assertThat(sseHub.subscriberCount(key)).isEqualTo(0);
        assertThat(emitter.completeCalled).isFalse();
    }

    private static final class FailingEmitter extends SseEmitter {
        private boolean completeCalled;

        @Override
        public void send(SseEventBuilder builder) throws IOException {
            throw new IOException("broken pipe");
        }

        @Override
        public synchronized void complete() {
            completeCalled = true;
            super.complete();
        }
    }

    private static final class SequentialIdGenerator implements IdGenerator {
        private int counter;

        @Override
        public Id generateId() {
            return new CommonId("evt-" + (++counter));
        }

        @Override
        public Id convertIdFrom(String id) {
            return new CommonId(id);
        }
    }
}
