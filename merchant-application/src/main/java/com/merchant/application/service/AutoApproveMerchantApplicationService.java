package com.merchant.application.service;

import com.grab.framework.logger.Logger;
import com.grab.framework.logger.Loggers;
import com.merchant.application.model.write.AutoApproveMerchantApplicationCommand;
import com.merchant.application.port.inbound.AutoApproveMerchantApplicationUseCase;
import com.merchant.domain.aggregate.MerchantAccount;
import com.merchant.domain.policy.MerchantApprovalPolicy;
import com.merchant.domain.port.outbound.MerchantAccountRepository;
import lombok.RequiredArgsConstructor;

import java.time.Instant;

@RequiredArgsConstructor
public class AutoApproveMerchantApplicationService implements AutoApproveMerchantApplicationUseCase {

    private static final Logger log = Loggers.getLogger(AutoApproveMerchantApplicationService.class);

    private final MerchantAccountRepository merchants;
    private final MerchantApprovalPolicy approvalPolicy;

    @Override
    public void execute(AutoApproveMerchantApplicationCommand command) {
        MerchantAccount merchant = merchants.findById(command.merchantId()).orElse(null);
        if (merchant == null || !approvalPolicy.canAutoApprove(merchant)) {
            log.info("Merchant application requires manual approval for merchantId={} and applicantUserId={}",
                    command.merchantId(), command.applicantUserId());
            return;
        }
        log.info("Auto-approving merchant application for merchantId={} and applicantUserId={}",
                command.merchantId(), command.applicantUserId());
        Instant now = Instant.now();
        merchant.approve(command.applicantUserId(), now);
        merchants.save(merchant);
    }
}
