package com.identity.domain.aggregate;

import com.grab.framework.id.Id;

import java.time.LocalDateTime;
import java.util.Objects;

public record ExternalIdentity(
        Id userId,
        String issuer,
        String subject,
        String directoryId,
        String resourceId,
        String linkedBy,
        String updatedBy,
        boolean enabled,
        long directoryRevision,
        LocalDateTime linkedAt,
        LocalDateTime directoryObservedAt
) {
    public ExternalIdentity {
        Objects.requireNonNull(userId);
        requireText("issuer", issuer);
        requireText("subject", subject);
        if ((directoryId == null) != (resourceId == null)) {
            throw new IllegalArgumentException("directoryId and resourceId must be provided together");
        }
        if (directoryId != null) {
            requireText("directoryId", directoryId);
            requireText("resourceId", resourceId);
        }
        if (directoryRevision < 0) {
            throw new IllegalArgumentException("directoryRevision must not be negative");
        }
        Objects.requireNonNull(linkedAt);
    }

    public static ExternalIdentity link(Id userId, String issuer, String subject, String directoryId,
                                        String resourceId, String linkedBy, LocalDateTime linkedAt) {
        return new ExternalIdentity(userId, issuer, subject, directoryId, resourceId, linkedBy, linkedBy,
                true, 0, linkedAt, null);
    }

    private static void requireText(String name, String value) {
        if (value == null || value.isBlank() || !value.equals(value.strip())) {
            throw new IllegalArgumentException(name + " is required and must be trimmed");
        }
    }
}
