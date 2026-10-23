package com.saleschannel.application.model.write;

import com.grab.framework.id.Id;

import java.time.Instant;

public record CreatePublishableKeyResult(Id keyId, Id salesChannelId, String label,
                                         String rawToken, String displayPrefix, Instant createdAt) {
    @Override
    public String toString() {
        return "CreatePublishableKeyResult[keyId=" + keyId + ", rawToken=[REDACTED]]";
    }
}
