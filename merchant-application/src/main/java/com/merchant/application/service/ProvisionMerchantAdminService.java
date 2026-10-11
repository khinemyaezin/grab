package com.merchant.application.service;

import com.grab.framework.id.IdGenerator;
import com.merchant.application.model.write.MerchantMemberResult;
import com.merchant.application.model.write.ProvisionMerchantAdminCommand;
import com.merchant.application.port.inbound.ProvisionMerchantAdminUseCase;
import com.merchant.application.port.outbound.IdentityAccessManagementPort;
import com.merchant.application.port.outbound.IdentityAccessManagementPort.ReplaceAccessRequest;
import com.merchant.application.security.MerchantAdminAccessProfile;
import com.merchant.domain.aggregate.MerchantMember;
import com.merchant.domain.port.outbound.MerchantMemberRepository;
import lombok.RequiredArgsConstructor;

import java.time.Instant;

@RequiredArgsConstructor
public class ProvisionMerchantAdminService implements ProvisionMerchantAdminUseCase {
    private final MerchantMemberRepository members;
    private final IdGenerator idGenerator;
    private final IdentityAccessManagementPort identityAccessManagementPort;

    @Override
    public MerchantMemberResult execute(ProvisionMerchantAdminCommand command) {
        if (members.existsByMerchantIdAndUserId(command.merchantId(), command.applicantUserId())) {
            return members.findByMerchantIdAndUserId(command.merchantId(), command.applicantUserId())
                    .map(MerchantMemberResult::from)
                    .orElse(null);
        }

        String userId = command.applicantUserId().getValue();
        String merchantId = command.merchantId().getValue();
        ReplaceAccessRequest accessRequest = new ReplaceAccessRequest(
                userId,
                null,
                MerchantAdminAccessProfile.ADMIN_ROLE_CODE,
                MerchantAdminAccessProfile.MERCHANT_SCOPE_KEY,
                merchantId
        );
        identityAccessManagementPort.replaceAccess(accessRequest);

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
