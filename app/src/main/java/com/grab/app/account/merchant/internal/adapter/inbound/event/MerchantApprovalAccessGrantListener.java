package com.grab.app.account.merchant.internal.adapter.inbound.event;

import com.grab.framework.cqrs.command.CommandBus;
import com.grab.framework.id.Id;
import com.grab.framework.id.IdGenerator;
import com.grab.framework.logger.Logger;
import com.grab.framework.logger.Loggers;
import com.merchant.application.model.write.ProvisionMerchantAdminCommand;
import com.merchant.domain.event.MerchantApprovedEvent;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class MerchantApprovalAccessGrantListener {
    private static final Logger log = Loggers.getLogger(MerchantApprovalAccessGrantListener.class);

    private final CommandBus commandBus;
    private final IdGenerator idGenerator;

    public MerchantApprovalAccessGrantListener(
            @Qualifier("accountCommandBus") CommandBus commandBus,
            IdGenerator idGenerator
    ) {
        this.commandBus = commandBus;
        this.idGenerator = idGenerator;
    }

    @EventListener
    public void onMerchantApproved(MerchantApprovedEvent event) {
        Id merchantId = idGenerator.convertIdFrom(event.merchantId());
        Id applicantUserId = idGenerator.convertIdFrom(event.applicantUserId());
        Instant occurredAt = event.occurredAt() != null ? event.occurredAt() : Instant.now();

        log.info("Provisioning initial merchant admin for merchant {} and applicant {}",
                merchantId.getValue(), applicantUserId.getValue());

        ProvisionMerchantAdminCommand command = new ProvisionMerchantAdminCommand(
                merchantId,
                applicantUserId,
                occurredAt
        );

        commandBus.dispatch(command);
    }
}

