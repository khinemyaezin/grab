package com.identity.application.model.read;

import com.grab.framework.id.Id;

public record ExternalUserView(
        Id userId,
        String externalId,
        String email,
        String status,
        long authenticationVersion
) {
}
