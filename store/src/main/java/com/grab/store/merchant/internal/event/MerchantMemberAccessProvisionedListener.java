package com.grab.store.merchant.internal.event;

import com.grab.framework.cqrs.command.CommandBus;
import com.grab.framework.id.IdGenerator;
import com.grab.framework.logger.Logger;
import com.grab.framework.logger.Loggers;
import com.grab.store.shared.events.identity.MerchantAdminAccessProvisionedIntegrationEvent;
import com.merchant.application.model.write.RecordMemberProvisioningResultCommand;
import com.merchant.domain.enums.AccessProvisioningStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MerchantMemberAccessProvisionedListener {
    private static final Logger log = Loggers.getLogger(MerchantMemberAccessProvisionedListener.class);

    private final CommandBus commandBus;
    private final IdGenerator idGenerator;

    @EventListener
    public void onAdminAccessProvisioned(MerchantAdminAccessProvisionedIntegrationEvent event) {
        log.info("Received admin access provisioned event for merchantId={} userId={} outcome={}",
                event.merchantId(), event.applicantUserId(), event.outcome());

        AccessProvisioningStatus status;
        if ("SUCCEEDED".equalsIgnoreCase(event.outcome())) {
            status = AccessProvisioningStatus.ACTIVE;
        } else if ("FAILED".equalsIgnoreCase(event.outcome())) {
            status = AccessProvisioningStatus.FAILED;
        } else {
            status = AccessProvisioningStatus.PENDING;
        }

        RecordMemberProvisioningResultCommand command = new RecordMemberProvisioningResultCommand(
                idGenerator.convertIdFrom(event.merchantId()),
                idGenerator.convertIdFrom(event.applicantUserId()),
                status,
                event.errorCode(),
                event.memberVersion(),
                event.completedAt()
        );

        commandBus.dispatch(command);
    }
}
