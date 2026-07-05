package com.grab.store.identity.internal.event;

import com.grab.framework.cqrs.command.CommandBus;
import com.grab.framework.id.Id;
import com.grab.framework.id.IdGenerator;
import com.grab.framework.logger.Logger;
import com.grab.framework.logger.Loggers;
import com.grab.store.identity.internal.command.ReplaceAccessCommand;
import com.grab.store.identity.internal.policy.MerchantApprovalAccessPolicy;
import com.grab.store.merchant.events.MerchantApprovedIntegrationEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MerchantAccountApprovedStatusEventListener {
    private static final Logger log = Loggers.getLogger(MerchantAccountApprovedStatusEventListener.class);

    private final CommandBus commandBus;
    private final IdGenerator idGenerator;
    private final MerchantApprovalAccessPolicy accessPolicy;

    @EventListener
    public void handleMerchantApproved(MerchantApprovedIntegrationEvent event) {
        log.info(
                "Placing approved merchant access for merchantId={} and applicantUserId={}",
                event.merchantId(),
                event.applicantUserId()
        );

        placeAccess(event.applicantUserId(), event.merchantId());
    }

    private void placeAccess(String applicantUserIdValue, String merchantIdValue) {
        Id applicantUserId = idGenerator.convertIdFrom(applicantUserIdValue);
        Id merchantId = idGenerator.convertIdFrom(merchantIdValue);
        MerchantApprovalAccessPolicy.MerchantApprovalContext context =
                new MerchantApprovalAccessPolicy.MerchantApprovalContext(merchantId.getValue());

        accessPolicy.placementsFor(context)
                .forEach(placement ->
                        commandBus.dispatch(new ReplaceAccessCommand(
                                applicantUserId,
                                placement.platformCode(),
                                placement.placementCode(),
                                placement.scopeKey(),
                                placement.scopeId()
                        )));
    }
}
