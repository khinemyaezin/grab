package com.identity.application.model.write;

import com.grab.framework.id.Id;

public record ExternalUserProvisionResult(
        Id userId,
        String externalId,
        String email,
        String givenName,
        String familyName,
        String status,
        boolean newlyCreated
) {
}
