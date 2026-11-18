package com.identity.application.model.write;

import com.grab.framework.id.Id;

public record ExternalIdentityLinkResult(Id userId, String issuer, String subject, boolean alreadyLinked) {
}
