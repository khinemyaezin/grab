package com.grab.store.shared.sse;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class SseHeartbeatScheduler {

    private final SseHub sseHub;

    public SseHeartbeatScheduler(SseHub sseHub) {
        this.sseHub = sseHub;
    }

    @Scheduled(fixedRate = 15_000)
    public void ping() {
        sseHub.sendHeartbeats();
    }
}
