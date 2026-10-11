package com.grab.store.shared.events.identity;

import com.grab.framework.domain.Event;

public record ScopeDisplayChangedIntegrationEvent(
        String scopeId,
        String name
) implements Event {
}
