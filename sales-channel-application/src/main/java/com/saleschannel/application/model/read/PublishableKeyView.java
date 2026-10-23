package com.saleschannel.application.model.read;

import com.grab.framework.id.Id;

import java.time.Instant;

public record PublishableKeyView(Id keyId, Id salesChannelId, String label, String displayPrefix,
                                 Instant createdAt, Instant revokedAt) {
}
