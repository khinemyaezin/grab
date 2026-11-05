package com.grab.app.channel.saleschannel.port.message;

import com.grab.framework.id.Id;

import java.time.Instant;
import java.util.Optional;

/**
 * Universal Java Port Interface for Publishable API Keys and Token Resolution.
 * Unifies PublishableKeyController and PublishableKeyQuery.
 */
public interface PublishableApiKeyPort {

    PublishableKeyResult createPublishableKey(CreatePublishableKeyMessage message);

    void revokePublishableKey(Id salesChannelId, Id keyId);

    Optional<KeyResolutionResult> resolveKey(String rawToken);

    record CreatePublishableKeyMessage(
            Id salesChannelId,
            String label
    ) {}

    record PublishableKeyResult(
            Id keyId,
            Id salesChannelId,
            String label,
            String rawToken,
            String displayPrefix,
            Instant createdAt
    ) {}

    record KeyResolutionResult(
            Id salesChannelId,
            boolean channelEnabled,
            boolean keyActive
    ) {}
}
