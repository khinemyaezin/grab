package com.grab.store.identity.internal.event;

import com.grab.framework.cqrs.command.CommandBus;
import com.grab.framework.id.Id;
import com.grab.framework.id.IdGenerator;
import com.grab.framework.logger.Logger;
import com.grab.framework.logger.Loggers;
import com.grab.store.identity.internal.command.ReplaceMerchantApplicantAccessCommand;
import com.merchant.domain.event.MerchantApprovedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MerchantAccountApprovedStatusEventListener {
    private static final Logger log = Loggers.getLogger(MerchantAccountApprovedStatusEventListener.class);

    private final CommandBus commandBus;
    private final IdGenerator idGenerator;

    @EventListener
    public void handleMerchantApproved(MerchantApprovedEvent event) {
        log.info(
                "Replacing merchant applicant access with owner access for merchantId={} and applicantUserId={}",
                event.merchantId(),
                event.applicantUserId()
        );

        replaceAccess(event.applicantUserId(), event.merchantId());
    }

    private void replaceAccess(String applicantUserIdValue, String merchantIdValue) {
        Id applicantUserId = idGenerator.convertIdFrom(applicantUserIdValue);
        Id merchantId = idGenerator.convertIdFrom(merchantIdValue);
        ReplaceMerchantApplicantAccessCommand command = new ReplaceMerchantApplicantAccessCommand(
                applicantUserId,
                merchantId
        );
        commandBus.dispatch(command);
    }
}
