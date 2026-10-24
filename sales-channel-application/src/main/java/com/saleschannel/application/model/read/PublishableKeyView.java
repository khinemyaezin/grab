package com.saleschannel.application.model.read;

import com.grab.framework.id.Id;
import com.grab.framework.id.impl.CommonId;

import java.time.Instant;

public record PublishableKeyView(Id keyId, Id salesChannelId, String label, String displayPrefix,
                                 Instant createdAt, Instant revokedAt) {
    public PublishableKeyView(String keyId, String salesChannelId, String label, String displayPrefix,
                              Instant createdAt, Instant revokedAt) {
        this(new CommonId(keyId), new CommonId(salesChannelId), label, displayPrefix, createdAt, revokedAt);
    }
}
