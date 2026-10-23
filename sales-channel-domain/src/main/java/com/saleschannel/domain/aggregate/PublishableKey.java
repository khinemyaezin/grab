package com.saleschannel.domain.aggregate;

import com.grab.framework.domain.AggregateRoot;
import com.grab.framework.id.Id;
import com.saleschannel.domain.event.PublishableKeyCreatedEvent;
import com.saleschannel.domain.event.PublishableKeyRevokedEvent;
import com.saleschannel.domain.exception.SalesChannelDomainError;
import com.saleschannel.domain.exception.SalesChannelDomainException;
import lombok.Getter;

import java.time.Instant;
import java.util.Objects;
import java.util.regex.Pattern;

@Getter
public class PublishableKey extends AggregateRoot<Id> {
    private static final Pattern DIGEST_PATTERN = Pattern.compile("[0-9a-f]{64}");
    private static final Pattern PREFIX_PATTERN = Pattern.compile("pk_[A-Za-z0-9_-]{8}");

    private final Id salesChannelId;
    private final String label;
    private final String tokenDigest;
    private final String displayPrefix;
    private final Instant createdAt;
    private Instant revokedAt;

    public PublishableKey(Id id,
                          Id salesChannelId,
                          String label,
                          String tokenDigest,
                          String displayPrefix,
                          Instant createdAt,
                          Instant revokedAt
    ) {
        super(id);
        Objects.requireNonNull(id, "id is required");
        this.salesChannelId = Objects.requireNonNull(salesChannelId, "salesChannelId is required");
        this.label = requireLabel(label);
        this.tokenDigest = requirePattern(tokenDigest, DIGEST_PATTERN, "tokenDigest");
        this.displayPrefix = requirePattern(displayPrefix, PREFIX_PATTERN, "displayPrefix");
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt is required");
        if (revokedAt != null && revokedAt.isBefore(createdAt)) {
            throw invalid("revokedAt");
        }
        this.revokedAt = revokedAt;
    }

    public static PublishableKey create(Id id, Id salesChannelId, String label,
                                        String tokenDigest, String displayPrefix, Instant now) {
        PublishableKey key = new PublishableKey(
                id, salesChannelId, label, tokenDigest, displayPrefix, now, null);
        String keyId = id.getValue();
        String channelId = salesChannelId.getValue();
        PublishableKeyCreatedEvent event = new PublishableKeyCreatedEvent(keyId, channelId, now);
        key.addEvent(event);
        return key;
    }

    public boolean isActive() {
        return revokedAt == null;
    }

    public void revoke(Instant now) {
        if (!isActive()) {
            return;
        }
        Objects.requireNonNull(now, "now is required");
        if (now.isBefore(createdAt)) {
            throw invalid("revokedAt");
        }
        revokedAt = now;
        String keyId = getId().getValue();
        String channelId = salesChannelId.getValue();
        PublishableKeyRevokedEvent event = new PublishableKeyRevokedEvent(keyId, channelId, now);
        addEvent(event);
    }

    private static String requireLabel(String value) {
        if (value == null) {
            throw invalid("label");
        }
        String trimmed = value.trim();
        if (trimmed.isBlank() || trimmed.length() > 100) {
            throw invalid("label");
        }
        return trimmed;
    }

    private static String requirePattern(String value, Pattern pattern, String field) {
        if (value == null || !pattern.matcher(value).matches()) {
            throw invalid(field);
        }
        return value;
    }

    private static SalesChannelDomainException invalid(String field) {
        SalesChannelDomainError error = new SalesChannelDomainError.InvalidPublishableKey(field);
        return new SalesChannelDomainException(error, "Invalid publishable key " + field);
    }
}
