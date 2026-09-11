package com.grab.store.shared.sse;

import com.grab.framework.id.IdGenerator;
import com.grab.framework.logger.Logger;
import com.grab.framework.logger.Loggers;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
public class SseHub {

    static final long EMITTER_TIMEOUT_MS = 300_000L;

    private static final Logger log = Loggers.getLogger(SseHub.class);

    private final ConcurrentHashMap<String, CopyOnWriteArrayList<SseEmitter>> subscribers = new ConcurrentHashMap<>();
    private final IdGenerator idGenerator;

    public SseHub(IdGenerator idGenerator) {
        this.idGenerator = idGenerator;
    }

    public SseEmitter register(String platformUserId, String scopeId) {
        String key = SseSubscriberKey.of(platformUserId, scopeId);
        SseEmitter emitter = new SseEmitter(EMITTER_TIMEOUT_MS) {
            @Override
            public synchronized void complete() {
                try {
                    super.complete();
                } finally {
                    unregister(key, this);
                }
            }

            @Override
            public synchronized void completeWithError(Throwable ex) {
                try {
                    super.completeWithError(ex);
                } finally {
                    unregister(key, this);
                }
            }
        };
        subscribers.computeIfAbsent(key, ignored -> new CopyOnWriteArrayList<>()).add(emitter);
        emitter.onCompletion(() -> unregister(key, emitter));
        emitter.onTimeout(() -> completeQuietly(emitter));
        emitter.onError(error -> unregister(key, emitter));
        return emitter;
    }

    public void publish(String key, String namedEvent, String json) {
        CopyOnWriteArrayList<SseEmitter> emitters = subscribers.get(key);
        if (emitters == null || emitters.isEmpty()) {
            return;
        }
        String eventId = idGenerator.generateId().getValue();
        SseEmitter.SseEventBuilder event = SseEmitter.event()
                .id(eventId)
                .name(namedEvent)
                .data(json);
        for (SseEmitter emitter : emitters) {
            try {
                synchronized (emitter) {
                    emitter.send(event);
                }
            } catch (IOException | IllegalStateException exception) {
                log.debug("Dropping SSE subscriber after publish failure");
                unregister(key, emitter);
            }
        }
    }

    public void sendHeartbeats() {
        subscribers.forEach((key, emitters) -> {
            for (SseEmitter emitter : emitters) {
                try {
                    synchronized (emitter) {
                        emitter.send(SseEmitter.event().comment("ping"));
                    }
                } catch (IOException | IllegalStateException exception) {
                    unregister(key, emitter);
                }
            }
        });
    }

    int subscriberCount(String key) {
        List<SseEmitter> emitters = subscribers.get(key);
        return emitters == null ? 0 : emitters.size();
    }

    void addSubscriber(String key, SseEmitter emitter) {
        subscribers.computeIfAbsent(key, ignored -> new CopyOnWriteArrayList<>()).add(emitter);
    }

    private void unregister(String key, SseEmitter emitter) {
        subscribers.computeIfPresent(key, (k, emitters) -> {
            emitters.remove(emitter);
            return emitters.isEmpty() ? null : emitters;
        });
    }

    private static void completeQuietly(SseEmitter emitter) {
        try {
            emitter.complete();
        } catch (IllegalStateException ignored) {
            // Already completed or timed out.
        }
    }
}
