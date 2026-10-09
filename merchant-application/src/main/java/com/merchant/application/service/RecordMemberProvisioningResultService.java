package com.merchant.application.service;

import com.merchant.application.model.write.MerchantMemberResult;
import com.merchant.application.model.write.RecordMemberProvisioningResultCommand;
import com.merchant.application.port.inbound.RecordMemberProvisioningResultUseCase;
import com.merchant.domain.aggregate.MerchantMember;
import com.merchant.domain.enums.AccessProvisioningStatus;
import com.merchant.domain.port.outbound.MerchantMemberRepository;
import lombok.RequiredArgsConstructor;

import java.time.Instant;

@RequiredArgsConstructor
public class RecordMemberProvisioningResultService implements RecordMemberProvisioningResultUseCase {
    private final MerchantMemberRepository members;

    @Override
    public MerchantMemberResult execute(RecordMemberProvisioningResultCommand command) {
        return members.findByMerchantIdAndUserId(command.merchantId(), command.userId())
                .map(member -> {
                    Instant now = command.completedAt() != null ? command.completedAt() : Instant.now();
                    if (command.outcome() == AccessProvisioningStatus.ACTIVE) {
                        member.recordProvisioningSuccess(command.memberVersion(), now);
                    } else if (command.outcome() == AccessProvisioningStatus.FAILED) {
                        member.recordProvisioningFailure(command.errorCode(), command.memberVersion(), now);
                    }
                    MerchantMember saved = members.save(member);
                    return MerchantMemberResult.from(saved);
                })
                .orElse(null);
    }
}
