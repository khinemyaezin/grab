package com.grab.store.identity.internal.event;

import com.grab.framework.cqrs.command.CommandBus;
import com.grab.framework.id.IdGenerator;
import com.grab.framework.logger.Logger;
import com.grab.framework.logger.Loggers;
import com.grab.store.shared.events.identity.MerchantAdminAccessProvisionedIntegrationEvent;
import com.grab.store.shared.events.merchant.MerchantAdminAccessProvisionRequestedIntegrationEvent;
import com.identity.application.model.write.AccessAssignmentResult;
import com.identity.application.model.write.FulfillAdminAccessAssignmentCommand;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class IdentityAdminAccessProvisionListener {
    private static final Logger log = Loggers.getLogger(IdentityAdminAccessProvisionListener.class);

    private final CommandBus commandBus;
    private final IdGenerator idGenerator;
    private final ApplicationEventPublisher events;

    @EventListener
    public void onAdminAccessProvisionRequested(MerchantAdminAccessProvisionRequestedIntegrationEvent event) {
        log.info("Processing admin access provision request for merchantId={} applicantUserId={}",
                event.merchantId(), event.applicantUserId());

        FulfillAdminAccessAssignmentCommand command = new FulfillAdminAccessAssignmentCommand(
                event.eventId(),
                idGenerator.convertIdFrom(event.merchantId()),
                idGenerator.convertIdFrom(event.applicantUserId()),
                event.roleCode(),
                event.scopeKey(),
                event.memberVersion(),
                event.requestedAt()
        );

        try {
            AccessAssignmentResult result = commandBus.dispatch(command);
            log.info("Successfully provisioned admin access: assignmentId={}", result.id());
            events.publishEvent(new MerchantAdminAccessProvisionedIntegrationEvent(
                    UUID.randomUUID().toString(),
                    event.eventId(),
                    event.merchantId(),
                    event.applicantUserId(),
                    event.roleCode(),
                    event.scopeKey(),
                    "SUCCEEDED",
                    result.id(),
                    null,
                    event.memberVersion(),
                    Instant.now()
            ));
        } catch (Exception e) {
            log.error("Failed to provision admin access for merchantId={}: {}", event.merchantId(), e.getMessage());
            events.publishEvent(new MerchantAdminAccessProvisionedIntegrationEvent(
                    UUID.randomUUID().toString(),
                    event.eventId(),
                    event.merchantId(),
                    event.applicantUserId(),
                    event.roleCode(),
                    event.scopeKey(),
                    "FAILED",
                    null,
                    e.getMessage(),
                    event.memberVersion(),
                    Instant.now()
            ));
        }
    }
}
