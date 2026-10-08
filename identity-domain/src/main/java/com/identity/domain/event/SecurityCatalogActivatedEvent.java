package com.identity.domain.event;

import com.grab.framework.domain.Event;

public record SecurityCatalogActivatedEvent(long catalogRevision, String moduleKey, int securityRevision,
                                            String contentDigest) implements Event {
}
