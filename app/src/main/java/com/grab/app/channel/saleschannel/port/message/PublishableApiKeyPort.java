package com.grab.app.channel.saleschannel.port.message;

import com.grab.framework.id.Id;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Universal Java Port Interface for Publishable API Keys and Token Resolution.
 * Unifies PublishableKeyController and PublishableKeyQuery.
 */
public interface PublishableApiKeyPort {

    PublishableKeyResult createPublishableKey(CreatePublishableKeyMessage message);

    void revokePublishableKey(Id keyId);

    List<PublishableKeyResult> listKeysForChannel(Id salesChannelId);

    Optional<KeyResolutionResult> resolveKey(String rawToken);

    record CreatePublishableKeyMessage(
            Id salesChannelId,
            String title
    ) {}

    record PublishableKeyResult(
            Id id,
            Id salesChannelId,
            String title,
            String token,
            String status,
            Instant createdAt,
            Instant revokedAt
    ) {}

    record KeyResolutionResult(
            Id keyId,
            Id salesChannelId,
            boolean keyActive
    ) {}
}

