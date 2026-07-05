package com.grab.store.merchant.internal.event;

import com.grab.framework.cqrs.command.CommandBus;
import com.grab.framework.id.Id;
import com.grab.framework.id.IdGenerator;
import com.grab.framework.logger.Logger;
import com.grab.framework.logger.Loggers;
import com.grab.store.merchant.internal.command.ChangeMerchantLifecycleCommand;
import com.merchant.domain.event.MerchantApplicationSubmittedEvent;
import com.merchant.domain.aggregate.MerchantAccount;
import com.merchant.domain.repository.MerchantAccountRepository;
import com.merchant.domain.service.MerchantApprovalPolicy;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MerchantAccountSubmitStatusEventListener {
    private static final Logger log = Loggers.getLogger(MerchantAccountSubmitStatusEventListener.class);

    private final CommandBus commandBus;
    private final IdGenerator idGenerator;
    private final MerchantAccountRepository merchantAccountRepository;
    private final MerchantApprovalPolicy approvalPolicy;

    @EventListener
    public void handleMerchantApplicationSubmitted(MerchantApplicationSubmittedEvent event) {
        Id merchantId = idGenerator.convertIdFrom(event.merchantId());
        MerchantAccount merchant = merchantAccountRepository.findById(merchantId).orElse(null);

        if (merchant != null && approvalPolicy.canAutoApprove(merchant)) {
            log.info(
                    "Auto-approving merchant application for merchantId={} and applicantUserId={}",
                    event.merchantId(),
                    event.applicantUserId()
            );
            autoApproveMerchant(event.applicantUserId(), event.merchantId());
        } else {
            log.info(
                    "Merchant application requires manual approval for merchantId={} and applicantUserId={}",
                    event.merchantId(),
                    event.applicantUserId()
            );
        }
    }

    private void autoApproveMerchant(String applicantUserIdValue, String merchantIdValue) {
        Id applicantUserId = idGenerator.convertIdFrom(applicantUserIdValue);
        Id merchantId = idGenerator.convertIdFrom(merchantIdValue);
        ChangeMerchantLifecycleCommand command = new ChangeMerchantLifecycleCommand(
                merchantId,
                applicantUserId,
                ChangeMerchantLifecycleCommand.Action.APPROVE,
                null
        );
        commandBus.dispatch(command);
    }
}
