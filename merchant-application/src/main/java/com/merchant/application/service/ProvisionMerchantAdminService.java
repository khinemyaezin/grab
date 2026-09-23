package com.merchant.application.service;

import com.grab.framework.id.Id;
import com.grab.framework.id.IdGenerator;
import com.merchant.application.model.write.MerchantMemberResult;
import com.merchant.application.model.write.ProvisionMerchantAdminCommand;
import com.merchant.application.port.inbound.ProvisionMerchantAdminUseCase;
import com.merchant.domain.aggregate.MerchantMember;
import com.merchant.domain.port.outbound.MerchantMemberRepository;
import lombok.RequiredArgsConstructor;

import java.time.Instant;

@RequiredArgsConstructor
public class ProvisionMerchantAdminService implements ProvisionMerchantAdminUseCase {
    private final MerchantMemberRepository members;
    private final IdGenerator idGenerator;

    @Override
    public MerchantMemberResult execute(ProvisionMerchantAdminCommand command) {
        if (members.existsByMerchantIdAndUserId(command.merchantId(), command.applicantUserId())) {
            return members.findByMerchantIdAndUserId(command.merchantId(), command.applicantUserId())
                    .map(MerchantMemberResult::from)
                    .orElse(null);
        }

        Instant occurredAt = command.occurredAt() != null ? command.occurredAt() : Instant.now();
        MerchantMember initialAdmin = MerchantMember.createAdmin(
                idGenerator.generateId(),
                command.merchantId(),
                command.applicantUserId(),
                occurredAt
        );

        MerchantMember saved = members.save(initialAdmin);
        return MerchantMemberResult.from(saved);
    }
}
