package com.grab.store.shared.sse;

final class SseSubscriberKey {

    private SseSubscriberKey() {
    }

    static String of(String platformUserId, String scopeId) {
        if (platformUserId == null || platformUserId.isBlank()) {
            throw new IllegalArgumentException("platformUserId is required");
        }
        if (scopeId == null || scopeId.isBlank()) {
            return platformUserId;
        }
        return platformUserId + ":" + scopeId;
    }
}
