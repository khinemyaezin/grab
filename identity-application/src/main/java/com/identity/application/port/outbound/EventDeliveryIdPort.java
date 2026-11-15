package com.identity.application.port.outbound;

import java.util.UUID;

@FunctionalInterface
public interface EventDeliveryIdPort {
    UUID requireEventId();
}
