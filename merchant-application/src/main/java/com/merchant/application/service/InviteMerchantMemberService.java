package com.merchant.application.service;

import com.grab.framework.id.Id;
import com.grab.framework.id.IdGenerator;
import com.merchant.application.exception.MerchantServiceError;
import com.merchant.application.exception.MerchantServiceException;
import com.merchant.application.model.write.InviteMerchantMemberCommand;
import com.merchant.application.model.write.MerchantMemberResult;
import com.merchant.application.port.inbound.InviteMerchantMemberUseCase;
import com.merchant.domain.aggregate.MerchantAccount;
import com.merchant.domain.aggregate.MerchantMember;
import com.merchant.domain.valueobject.MerchantRole;
import com.merchant.domain.exception.MerchantDomainError;
import com.merchant.domain.exception.MerchantDomainException;
import com.merchant.domain.port.outbound.MerchantAccountRepository;
import com.merchant.domain.port.outbound.MerchantMemberRepository;
import lombok.RequiredArgsConstructor;

import java.time.Instant;

@RequiredArgsConstructor
public class InviteMerchantMemberService implements InviteMerchantMemberUseCase {
    private final MerchantAccountRepository merchants;
    private final MerchantMemberRepository members;
    private final IdGenerator ids;

    @Override
    public MerchantMemberResult execute(InviteMerchantMemberCommand command) {
        MerchantAccount merchant = merchants.findById(command.merchantId())
                .orElseThrow(() -> notFound(command.merchantId()));

        if (!merchant.isOperational()) {
            throw new MerchantDomainException(
                    new MerchantDomainError.MerchantNotOperational(
                            command.merchantId().getValue(),
                            merchant.getStatus().name()
                    ),
                    "Cannot invite members to non-operational merchant"
            );
        }

        requireCanInvite(merchant, command.actorId(), command.role());

        if (members.existsByMerchantIdAndUserId(command.merchantId(), command.targetUserId())) {
            throw new MerchantDomainException(
                    new MerchantDomainError.DuplicateMerchantMember(
                            command.merchantId().getValue(),
                            command.targetUserId().getValue()
                    ),
                    "User is already a member of this merchant"
            );
        }

        Instant now = Instant.now();
        MerchantMember member = MerchantMember.invite(
                ids.generateId(),
                command.merchantId(),
                command.targetUserId(),
                command.role(),
                command.actorId(),
                command.expiresAt(),
                now
        );

        MerchantMember saved = members.save(member);
        return MerchantMemberResult.from(saved);
    }

    private void requireCanInvite(MerchantAccount merchant, Id actorId, MerchantRole targetRole) {
        if (merchant.isApplicant(actorId)) {
            return;
        }

        MerchantMember actorMember = members.findByMerchantIdAndUserId(merchant.getId(), actorId)
                .filter(m -> m.getStatus().isActive())
                .orElseThrow(() -> unauthorized(actorId, "INVITE_MEMBER"));

        if (!actorMember.getRole().canManage(targetRole)) {
            throw unauthorized(actorId, "INVITE_ROLE_" + targetRole.name());
        }
    }

    private MerchantServiceException notFound(Id merchantId) {
        return new MerchantServiceException(
                new MerchantServiceError.MerchantNotFound(merchantId.getValue()),
                "Merchant account not found"
        );
    }

    private MerchantServiceException unauthorized(Id actorId, String operation) {
        return new MerchantServiceException(
                new MerchantServiceError.UnauthorizedMemberOperation(actorId.getValue(), operation),
                "Actor is not authorized for operation: " + operation
        );
    }
}
