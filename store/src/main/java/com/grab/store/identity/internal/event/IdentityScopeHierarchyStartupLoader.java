package com.grab.store.identity.internal.event;

import com.identity.domain.port.outbound.ScopeManifestRepository;
import com.identity.domain.valueobject.ScopeHierarchy;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class IdentityScopeHierarchyStartupLoader {
    private final ScopeManifestRepository scopes;

    @EventListener(ApplicationReadyEvent.class)
    public void loadPersistedHierarchy() {
        var declarations = scopes.loadActive();
        if (!declarations.isEmpty()) {
            ScopeHierarchy.registerAll(declarations);
        }
    }
}
