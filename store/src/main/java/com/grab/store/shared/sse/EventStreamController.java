package com.grab.store.shared.sse;

import com.grab.framework.id.IdGenerator;
import com.grab.store.shared.security.SecurityPrincipal;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1/events")
public class EventStreamController {

    private final SseHub sseHub;
    private final IdGenerator idGenerator;

    public EventStreamController(SseHub sseHub, IdGenerator idGenerator) {
        this.sseHub = sseHub;
        this.idGenerator = idGenerator;
    }

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream(
            @AuthenticationPrincipal SecurityPrincipal principal,
            @RequestHeader(value = "Last-Event-ID", required = false) String lastEventId,
            HttpServletResponse response
    ) {
        if (principal == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }

        if (response != null) {
            response.setHeader("Cache-Control", "no-cache, no-transform");
            response.setHeader("X-Accel-Buffering", "no");
        }

        SseEmitter emitter = sseHub.register(
                principal.getPlatformUserId(),
                principal.getScopeId().orElse(null)
        );
        try {
            synchronized (emitter) {
                emitter.send(SseEmitter.event()
                        .id(idGenerator.generateId().getValue())
                        .name("ready")
                        .data("{\"producerId\":\"backend\"}"));
            }
        } catch (IOException exception) {
            emitter.completeWithError(exception);
        }
        return emitter;
    }

    public SseEmitter stream(SecurityPrincipal principal) {
        return stream(principal, null, null);
    }
}
