package com.identity.application.port.outbound;

import com.identity.domain.event.SecurityCatalogActivatedEvent;
import com.identity.domain.event.UserRegisteredEvent;

public interface IdentityIntegrationEventPublisher {

    void publishUserRegistered(UserRegisteredEvent event);

    void publishSecurityCatalogActivated(SecurityCatalogActivatedEvent event);
}

