package com.grab.store.identity.internal.event;

import com.grab.framework.cqrs.command.CommandBus;
import com.grab.framework.logger.Logger;
import com.grab.framework.logger.Loggers;
import com.grab.framework.security.SecurityManifestDeclaredIntegrationEvent;
import com.identity.application.model.write.RegisterSecurityManifestCommand;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.grab.store.shared.events.catalog.CatalogSecurityManifestDeclaredIntegrationEvent;
import com.grab.store.shared.events.identity.IdentitySecurityManifestDeclaredIntegrationEvent;
import com.grab.store.shared.events.inventory.InventorySecurityManifestDeclaredIntegrationEvent;
import com.grab.store.shared.events.merchant.MerchantSecurityManifestDeclaredIntegrationEvent;
import com.grab.store.shared.events.saleschannel.SalesChannelSecurityManifestDeclaredIntegrationEvent;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class IdentitySecurityManifestRegistrationListener {
    private static final Map<Class<?>, String> ALLOWED_OWNERS = Map.of(
            CatalogSecurityManifestDeclaredIntegrationEvent.class, "catalog",
            IdentitySecurityManifestDeclaredIntegrationEvent.class, "identity",
            InventorySecurityManifestDeclaredIntegrationEvent.class, "inventory",
            MerchantSecurityManifestDeclaredIntegrationEvent.class, "merchant",
            SalesChannelSecurityManifestDeclaredIntegrationEvent.class, "saleschannel"
    );
    private static final Logger log = Loggers.getLogger(IdentitySecurityManifestRegistrationListener.class);
    private final CommandBus commandBus;

    @EventListener
    public void onSecurityManifestDeclared(SecurityManifestDeclaredIntegrationEvent event) {
        String expectedOwner = ALLOWED_OWNERS.get(event.getClass());
        if (!event.moduleKey().equals(expectedOwner)) {
            throw new IllegalArgumentException("security manifest producer is not authorized for module " + event.moduleKey());
        }
        log.info("Registering complete security manifest moduleKey={} revision={} eventId={}",
                event.moduleKey(), event.securityRevision(), event.eventId());
        commandBus.dispatch(new RegisterSecurityManifestCommand(
                event.manifest(), event.eventId(), event.suppliedContentDigest(), event.publishedAt()));
    }
}
